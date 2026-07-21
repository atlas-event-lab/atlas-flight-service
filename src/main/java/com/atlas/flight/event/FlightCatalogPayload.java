package com.atlas.flight.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Payload for {@code FlightCreated} / {@code FlightUpdated} (flight-events.yaml
 * FlightCatalogPayload). Denormalized (airline/airport display fields) so Search needs no
 * cross-service join (ARCH-004); carries {@code totalSeats} so Inventory can seed seat
 * availability. Never carries live availability (data ownership).
 */
public record FlightCatalogPayload(
        @NotNull UUID flightId,
        String flightNumber,
        String airlineCode,
        String airlineName,
        String originAirportCode,
        String destinationAirportCode,
        Instant departureTime,
        Instant arrivalTime,
        int totalSeats,
        @Valid @NotNull MoneyEvent basePrice,
        @Valid List<FlightSegmentEvent> segments) {}
