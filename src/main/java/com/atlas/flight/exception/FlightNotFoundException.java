package com.atlas.flight.exception;

import java.util.UUID;

/** Thrown when a flight cannot be found by id (get/update/withdraw → 404). */
public class FlightNotFoundException extends RuntimeException {

    public FlightNotFoundException(UUID flightId) {
        super("Flight not found: " + flightId);
    }
}
