package com.atlas.flight.event;

import java.time.Instant;

/**
 * Denormalized flight leg inside catalog event payloads (flight-events.yaml FlightSegment).
 * Airport references are carried as IATA codes (not ids) so consumers need no join.
 */
public record FlightSegmentEvent(
        int sequence,
        String originAirportCode,
        String destinationAirportCode,
        Instant departureTime,
        Instant arrivalTime) {}
