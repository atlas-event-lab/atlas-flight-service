package com.atlas.flight.dto;

import java.time.Instant;
import java.util.UUID;

/** A flight leg in API responses (flight.yaml FlightSegmentInput shape). */
public record FlightSegmentResponse(
        int sequence,
        UUID originAirportId,
        UUID destinationAirportId,
        Instant departureTime,
        Instant arrivalTime
) {}
