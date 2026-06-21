package com.atlas.flight.exception;

import java.time.Instant;

/**
 * Thrown when a create would violate the flight uniqueness key
 * ({@code flightNumber} + {@code departureTime}) → 409 Conflict
 * (services/flight/service.md §Flight uniqueness key).
 */
public class DuplicateFlightException extends RuntimeException {

    public DuplicateFlightException(String flightNumber, Instant departureTime) {
        super("Flight already exists for flightNumber=" + flightNumber + " departureTime=" + departureTime);
    }
}
