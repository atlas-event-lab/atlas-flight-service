package com.atlas.flight.event;

import com.atlas.flight.entity.Airline;
import com.atlas.flight.entity.Airport;
import com.atlas.flight.entity.Flight;
import com.atlas.flight.entity.FlightSegment;
import com.atlas.flight.exception.InvalidFlightException;
import com.atlas.flight.repository.AirlineRepository;
import com.atlas.flight.repository.AirportRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Builds denormalized catalog event payloads (ARCH-004, EVT-006). Resolves the airline and
 * airport IATA codes/names from the seeded reference tables so {@code FlightCreated} /
 * {@code FlightUpdated} carry display fields and Search needs no cross-service join.
 * Shared by the admin service and the bootstrap publisher so the payload shape is identical.
 */
@Component
@RequiredArgsConstructor
public class FlightEventPayloadFactory {

    private final AirlineRepository airlineRepository;
    private final AirportRepository airportRepository;

    public FlightCatalogPayload toCatalogPayload(Flight flight) {
        Airline airline = airlineRepository
                .findById(flight.getAirlineId())
                .orElseThrow(() -> new InvalidFlightException("Airline not found: " + flight.getAirlineId()));

        return new FlightCatalogPayload(
                flight.getId(),
                flight.getFlightNumber(),
                airline.getIataCode(),
                airline.getName(),
                airportCode(flight.getOriginAirportId()),
                airportCode(flight.getDestinationAirportId()),
                flight.getDepartureTime(),
                flight.getArrivalTime(),
                flight.getTotalSeats(),
                new MoneyEvent(
                        flight.getBasePrice().getAmount(), flight.getBasePrice().getCurrency()),
                flight.getSegments().stream().map(this::toSegmentEvent).toList());
    }

    private FlightSegmentEvent toSegmentEvent(FlightSegment segment) {
        return new FlightSegmentEvent(
                segment.getSequence(),
                airportCode(segment.getOriginAirportId()),
                airportCode(segment.getDestinationAirportId()),
                segment.getDepartureTime(),
                segment.getArrivalTime());
    }

    private String airportCode(UUID airportId) {
        return airportRepository
                .findById(airportId)
                .map(Airport::getIataCode)
                .orElseThrow(() -> new InvalidFlightException("Airport not found: " + airportId));
    }
}
