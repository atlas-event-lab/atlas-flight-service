package com.atlas.flight.service;

import com.atlas.flight.client.InventoryClient;
import com.atlas.flight.shared.messaging.EventType;
import com.atlas.flight.client.dto.AvailabilityResponse;
import com.atlas.flight.event.MoneyEvent;
import com.atlas.flight.dto.CreateFlightRequest;
import com.atlas.flight.entity.Flight;
import com.atlas.flight.entity.FlightStatus;
import com.atlas.flight.event.FlightCatalogPayload;
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
import com.atlas.flight.support.FlightTestData;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FlightServiceImplTest {

    @Mock FlightRepository flightRepository;
    @Mock AirlineRepository airlineRepository;
    @Mock AirportRepository airportRepository;
    @Mock InventoryClient inventoryClient;
    @Mock OutboxEventWriter outboxEventWriter;
    @Mock FlightEventPayloadFactory payloadFactory;
    @Mock FlightMapper flightMapper;

    @InjectMocks
    FlightServiceImpl service;

    @BeforeEach
    void referencesExist() {
        when(airlineRepository.existsById(any())).thenReturn(true);
        when(airportRepository.existsById(any())).thenReturn(true);
        when(payloadFactory.toCatalogPayload(any())).thenReturn(mock_payload());
        when(flightMapper.toResponse(any())).thenReturn(FlightTestData.aFlightResponse());
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    void createFlight_valid_persists_and_writes_FlightCreated_to_outbox() {
        when(flightRepository.existsByFlightNumberAndDepartureTime(any(), any())).thenReturn(false);
        when(flightRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.createFlight(FlightTestData.aCreateFlightRequest());

        verify(flightRepository).save(any(Flight.class));
        verify(outboxEventWriter).write(any(UUID.class), eq(EventType.FLIGHT_CREATED), any());
    }

    @Test
    void createFlight_duplicateBusinessKey_throwsConflict_and_writes_no_event() {
        when(flightRepository.existsByFlightNumberAndDepartureTime(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> service.createFlight(FlightTestData.aCreateFlightRequest()))
                .isInstanceOf(DuplicateFlightException.class);

        verify(flightRepository, never()).save(any());
        verifyNoInteractions(outboxEventWriter);
    }

    @Test
    void createFlight_arrivalNotAfterDeparture_throwsInvalid_and_does_not_persist() {
        CreateFlightRequest invalid = new CreateFlightRequest(
                FlightTestData.FLIGHT_NUMBER, FlightTestData.AIRLINE_ID,
                FlightTestData.ORIGIN_ID, FlightTestData.DEST_ID,
                FlightTestData.ARRIVAL, FlightTestData.DEPARTURE, // reversed
                180, FlightTestData.aMoneyRequest(), null);

        assertThatThrownBy(() -> service.createFlight(invalid))
                .isInstanceOf(InvalidFlightException.class);

        verify(flightRepository, never()).save(any());
        verifyNoInteractions(outboxEventWriter);
    }

    @Test
    void createFlight_unknownAirline_throwsInvalid() {
        when(airlineRepository.existsById(any())).thenReturn(false);

        assertThatThrownBy(() -> service.createFlight(FlightTestData.aCreateFlightRequest()))
                .isInstanceOf(InvalidFlightException.class);

        verify(flightRepository, never()).save(any());
    }

    // ── update / capacity-shrink ───────────────────────────────────────────────

    @Test
    void updateFlight_notFound_throws404() {
        when(flightRepository.findById(FlightTestData.FLIGHT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateFlight(
                FlightTestData.FLIGHT_ID, FlightTestData.anUpdateFlightRequest(180)))
                .isInstanceOf(FlightNotFoundException.class);
    }

    @Test
    void updateFlight_capacityDecreaseBelowReserved_throwsConflict_and_writes_no_event() {
        when(flightRepository.findById(FlightTestData.FLIGHT_ID)).thenReturn(Optional.of(FlightTestData.aFlight()));
        when(inventoryClient.getAvailability(eq("FLIGHT"), eq(FlightTestData.FLIGHT_ID)))
                .thenReturn(new AvailabilityResponse("FLIGHT", FlightTestData.FLIGHT_ID, 180, 150, 30, "ACTIVE"));

        assertThatThrownBy(() -> service.updateFlight(
                FlightTestData.FLIGHT_ID, FlightTestData.anUpdateFlightRequest(100)))
                .isInstanceOf(CapacityBelowReservedException.class);

        verify(outboxEventWriter, never()).write(any(), any(), any());
    }

    @Test
    void updateFlight_capacityDecreaseAtOrAboveReserved_proceeds_and_writes_FlightUpdated() {
        when(flightRepository.findById(FlightTestData.FLIGHT_ID)).thenReturn(Optional.of(FlightTestData.aFlight()));
        when(flightRepository.existsByFlightNumberAndDepartureTimeAndIdNot(any(), any(), any())).thenReturn(false);
        when(inventoryClient.getAvailability(eq("FLIGHT"), eq(FlightTestData.FLIGHT_ID)))
                .thenReturn(new AvailabilityResponse("FLIGHT", FlightTestData.FLIGHT_ID, 180, 100, 80, "ACTIVE"));

        service.updateFlight(FlightTestData.FLIGHT_ID, FlightTestData.anUpdateFlightRequest(120));

        verify(outboxEventWriter).write(eq(FlightTestData.FLIGHT_ID), eq(EventType.FLIGHT_UPDATED), any());
    }

    @Test
    void updateFlight_capacityIncrease_skips_inventory_check() {
        when(flightRepository.findById(FlightTestData.FLIGHT_ID)).thenReturn(Optional.of(FlightTestData.aFlight()));
        when(flightRepository.existsByFlightNumberAndDepartureTimeAndIdNot(any(), any(), any())).thenReturn(false);

        service.updateFlight(FlightTestData.FLIGHT_ID, FlightTestData.anUpdateFlightRequest(220));

        verifyNoInteractions(inventoryClient);
        verify(outboxEventWriter).write(eq(FlightTestData.FLIGHT_ID), eq(EventType.FLIGHT_UPDATED), any());
    }

    @Test
    void updateFlight_inventoryUnreachable_throwsUnavailable_and_writes_no_event() {
        when(flightRepository.findById(FlightTestData.FLIGHT_ID)).thenReturn(Optional.of(FlightTestData.aFlight()));
        when(inventoryClient.getAvailability(any(), any())).thenThrow(mock_feignException());

        assertThatThrownBy(() -> service.updateFlight(
                FlightTestData.FLIGHT_ID, FlightTestData.anUpdateFlightRequest(100)))
                .isInstanceOf(InventoryUnavailableException.class);

        verify(outboxEventWriter, never()).write(any(), any(), any());
    }

    // ── withdraw ───────────────────────────────────────────────────────────────

    @Test
    void withdrawFlight_softDeactivates_and_writes_FlightDeleted() {
        Flight flight = FlightTestData.aFlight();
        when(flightRepository.findById(FlightTestData.FLIGHT_ID)).thenReturn(Optional.of(flight));

        service.withdrawFlight(FlightTestData.FLIGHT_ID);

        assertThat(flight.getStatus()).isEqualTo(FlightStatus.WITHDRAWN);
        verify(outboxEventWriter).write(eq(FlightTestData.FLIGHT_ID), eq(EventType.FLIGHT_DELETED), any());
    }

    @Test
    void getFlight_notFound_throws404() {
        when(flightRepository.findById(FlightTestData.FLIGHT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getFlight(FlightTestData.FLIGHT_ID))
                .isInstanceOf(FlightNotFoundException.class);
    }

    private static FlightCatalogPayload mock_payload() {
        return new FlightCatalogPayload(
                FlightTestData.FLIGHT_ID, FlightTestData.FLIGHT_NUMBER, "AT", "Atlas Airways",
                "LIM", "MAD", Instant.now(), Instant.now(), 180,
                new MoneyEvent(new BigDecimal("1200.00"), "USD"), java.util.List.of());
    }

    private static FeignException mock_feignException() {
        return new FeignException.ServiceUnavailable(
                "inventory down", feign.Request.create(
                        feign.Request.HttpMethod.GET, "/api/v1/inventory/FLIGHT/x",
                        java.util.Map.of(), null, null, null), null, null);
    }
}
