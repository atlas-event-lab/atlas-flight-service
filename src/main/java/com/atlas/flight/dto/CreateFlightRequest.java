package com.atlas.flight.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Request body for POST /flights (flight.yaml CreateFlightRequest).
 * Cross-field rule {@code arrivalTime} after {@code departureTime} is validated in the
 * service before execution (API-004).
 */
public record CreateFlightRequest(
        @NotBlank String flightNumber,
        @NotNull UUID airlineId,
        @NotNull UUID originAirportId,
        @NotNull UUID destinationAirportId,
        @NotNull Instant departureTime,
        @NotNull Instant arrivalTime,
        @NotNull @Min(1) Integer totalSeats,
        @NotNull @Valid MoneyRequest basePrice,
        @Valid List<FlightSegmentInput> segments) {}
