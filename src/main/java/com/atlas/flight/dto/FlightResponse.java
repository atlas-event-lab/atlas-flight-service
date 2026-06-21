package com.atlas.flight.dto;

import com.atlas.flight.entity.FlightStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Flight API response (flight.yaml FlightResponse). */
public record FlightResponse(
        UUID flightId,
        String flightNumber,
        UUID airlineId,
        UUID originAirportId,
        UUID destinationAirportId,
        Instant departureTime,
        Instant arrivalTime,
        int totalSeats,
        MoneyResponse basePrice,
        FlightStatus status,
        List<FlightSegmentResponse> segments
) {}
