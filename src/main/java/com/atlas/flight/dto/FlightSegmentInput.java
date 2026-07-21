package com.atlas.flight.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

/** Optional leg for multi-segment flights (flight.yaml FlightSegmentInput; see domain/trip.md). */
public record FlightSegmentInput(
        @NotNull @Min(1) Integer sequence,
        @NotNull UUID originAirportId,
        @NotNull UUID destinationAirportId,
        @NotNull Instant departureTime,
        @NotNull Instant arrivalTime) {}
