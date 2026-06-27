package com.atlas.flight.service;

import com.atlas.flight.client.InventoryClient;
import com.atlas.flight.client.dto.AvailabilityResponse;
import com.atlas.flight.dto.CreateFlightRequest;
import com.atlas.flight.dto.FlightListResponse;
import com.atlas.flight.dto.FlightPriceResponse;
import com.atlas.flight.dto.FlightResponse;
import com.atlas.flight.dto.FlightSegmentInput;
import com.atlas.flight.dto.MoneyRequest;
import com.atlas.flight.dto.MoneyResponse;
import com.atlas.flight.dto.UpdateFlightRequest;
import com.atlas.flight.entity.Flight;
import com.atlas.flight.entity.FlightSegment;
import com.atlas.flight.entity.FlightStatus;
import com.atlas.flight.entity.Money;
import com.atlas.flight.event.FlightDeletedPayload;
import com.atlas.flight.shared.messaging.EventType;
import com.atlas.flight.event.FlightEventPayloadFactory;
import com.atlas.flight.exception.CapacityBelowReservedException;
import com.atlas.flight.exception.DuplicateFlightException;
import com.atlas.flight.exception.FlightNotFoundException;
import com.atlas.flight.exception.InvalidFlightException;
import com.atlas.flight.exception.InventoryUnavailableException;
import com.atlas.flight.mapper.FlightMapper;
import com.atlas.flight.messaging.OutboxEventWriter;
import com.atlas.flight.repository.AirlineRepository;
import com.atlas.flight.repository.AirportRepository;
import com.atlas.flight.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Flight catalog service. Validates input, persists the catalog entity and its event in one
 * transaction (Transactional Outbox, EVT-009/EVT-010), and enforces the capacity-shrink rule
 * against Inventory before lowering capacity (ARCH-006). All entity-to-DTO mapping happens
 * here (coding-standards §Layer Responsibilities).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {

    private static final String FLIGHT_RESOURCE_TYPE = "FLIGHT";

    private final FlightRepository flightRepository;
    private final AirlineRepository airlineRepository;
    private final AirportRepository airportRepository;
    private final InventoryClient inventoryClient;
    private final OutboxEventWriter outboxEventWriter;
    private final FlightEventPayloadFactory payloadFactory;
    private final FlightMapper flightMapper;

    @Override
    @Transactional
    public FlightResponse createFlight(CreateFlightRequest request) {
        validateSchedule(request.departureTime(), request.arrivalTime());
        validateReferences(request.airlineId(), request.originAirportId(),
                request.destinationAirportId(), request.segments());

        if (flightRepository.existsByFlightNumberAndDepartureTime(
                request.flightNumber(), request.departureTime())) {
            throw new DuplicateFlightException(request.flightNumber(), request.departureTime());
        }

        Flight flight = new Flight(
                UUID.randomUUID(),
                request.flightNumber(),
                request.airlineId(),
                request.originAirportId(),
                request.destinationAirportId(),
                request.departureTime(),
                request.arrivalTime(),
                request.totalSeats(),
                toMoney(request.basePrice()),
                FlightStatus.ACTIVE);
        applySegments(flight, request.segments());

        Flight saved = flightRepository.save(flight);
        outboxEventWriter.write(saved.getId(), EventType.FLIGHT_CREATED, payloadFactory.toCatalogPayload(saved));

        log.info("Flight created: flightId={}, flightNumber={}, totalSeats={}",
                saved.getId(), saved.getFlightNumber(), saved.getTotalSeats());

        return flightMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public FlightResponse updateFlight(UUID flightId, UpdateFlightRequest request) {
        validateSchedule(request.departureTime(), request.arrivalTime());
        validateReferences(request.airlineId(), request.originAirportId(),
                request.destinationAirportId(), request.segments());

        Flight flight = findFlight(flightId);

        // Capacity-shrink guard (feature.md): only when capacity decreases (ARCH-006).
        if (request.totalSeats() < flight.getTotalSeats()) {
            assertCapacityNotBelowReserved(flightId, request.totalSeats());
        }

        if (flightRepository.existsByFlightNumberAndDepartureTimeAndIdNot(
                request.flightNumber(), request.departureTime(), flightId)) {
            throw new DuplicateFlightException(request.flightNumber(), request.departureTime());
        }

        flight.update(
                request.flightNumber(),
                request.airlineId(),
                request.originAirportId(),
                request.destinationAirportId(),
                request.departureTime(),
                request.arrivalTime(),
                request.totalSeats(),
                toMoney(request.basePrice()));
        applySegments(flight, request.segments());

        outboxEventWriter.write(flight.getId(), EventType.FLIGHT_UPDATED, payloadFactory.toCatalogPayload(flight));

        log.info("Flight updated: flightId={}, flightNumber={}, totalSeats={}",
                flight.getId(), flight.getFlightNumber(), flight.getTotalSeats());

        return flightMapper.toResponse(flight);
    }

    @Override
    @Transactional
    public void withdrawFlight(UUID flightId) {
        Flight flight = findFlight(flightId);
        flight.withdraw();
        outboxEventWriter.write(flight.getId(), EventType.FLIGHT_DELETED, new FlightDeletedPayload(flight.getId()));

        log.info("Flight withdrawn: flightId={}", flight.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public FlightResponse getFlight(UUID flightId) {
        return flightMapper.toResponse(findFlight(flightId));
    }

    @Override
    @Transactional(readOnly = true)
    public FlightPriceResponse getFlightPrice(UUID flightId) {
        Flight flight = findFlight(flightId);
        return new FlightPriceResponse(
                flight.getId(),
                new MoneyResponse(flight.getBasePrice().getAmount(), flight.getBasePrice().getCurrency()),
                flight.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public FlightListResponse listFlights(Pageable pageable) {
        Page<FlightResponse> page = flightRepository.findAll(pageable).map(flightMapper::toResponse);
        return FlightListResponse.from(page);
    }

    // -------------------------------------------------------------------------
    // Validation & helpers
    // -------------------------------------------------------------------------

    private void validateSchedule(Instant departureTime, Instant arrivalTime) {
        if (!arrivalTime.isAfter(departureTime)) {
            throw new InvalidFlightException("arrivalTime must be after departureTime");
        }
    }

    private void validateReferences(UUID airlineId, UUID originAirportId, UUID destinationAirportId,
                                    List<FlightSegmentInput> segments) {
        if (!airlineRepository.existsById(airlineId)) {
            throw new InvalidFlightException("Airline not found: " + airlineId);
        }
        requireAirport(originAirportId);
        requireAirport(destinationAirportId);
        if (segments != null) {
            for (FlightSegmentInput segment : segments) {
                requireAirport(segment.originAirportId());
                requireAirport(segment.destinationAirportId());
                if (!segment.arrivalTime().isAfter(segment.departureTime())) {
                    throw new InvalidFlightException(
                            "segment arrivalTime must be after departureTime (sequence " + segment.sequence() + ")");
                }
            }
        }
    }

    private void requireAirport(UUID airportId) {
        if (!airportRepository.existsById(airportId)) {
            throw new InvalidFlightException("Airport not found: " + airportId);
        }
    }

    private void assertCapacityNotBelowReserved(UUID flightId, int newTotalSeats) {
        AvailabilityResponse availability;
        try {
            availability = inventoryClient.getAvailability(FLIGHT_RESOURCE_TYPE, flightId);
        } catch (Exception e) {
            // A transient failure is a failed precondition — never silently skip the check.
            throw new InventoryUnavailableException(flightId, e);
        }
        if (newTotalSeats < availability.reservedCount()) {
            throw new CapacityBelowReservedException(flightId, newTotalSeats, availability.reservedCount());
        }
    }

    private void applySegments(Flight flight, List<FlightSegmentInput> segments) {
        List<FlightSegment> mapped = (segments == null) ? List.of() : segments.stream()
                .map(s -> new FlightSegment(
                        UUID.randomUUID(),
                        s.sequence(),
                        s.originAirportId(),
                        s.destinationAirportId(),
                        s.departureTime(),
                        s.arrivalTime()))
                .toList();
        flight.replaceSegments(mapped);
    }

    private Money toMoney(MoneyRequest money) {
        return new Money(money.amount(), money.currency());
    }

    private Flight findFlight(UUID flightId) {
        return flightRepository.findById(flightId)
                .orElseThrow(() -> new FlightNotFoundException(flightId));
    }
}
