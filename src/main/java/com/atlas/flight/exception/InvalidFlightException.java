package com.atlas.flight.exception;

/**
 * Thrown when a flight request fails a business validation rule that bean validation
 * cannot express (e.g. {@code arrivalTime} not after {@code departureTime}, or a
 * referenced airline/airport does not exist) → 400 Bad Request (API-004).
 */
public class InvalidFlightException extends RuntimeException {

    public InvalidFlightException(String message) {
        super(message);
    }
}
