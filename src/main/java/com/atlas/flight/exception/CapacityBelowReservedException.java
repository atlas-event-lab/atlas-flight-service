package com.atlas.flight.exception;

import java.util.UUID;

/**
 * Thrown when an update lowers {@code totalSeats} below the count already reserved in
 * Inventory → 409 Conflict (feature.md §Capacity-Shrink Validation; ARCH-006).
 */
public class CapacityBelowReservedException extends RuntimeException {

    public CapacityBelowReservedException(UUID flightId, int newTotalSeats, int reservedCount) {
        super("Cannot lower totalSeats to " + newTotalSeats + " for flight " + flightId
                + ": Inventory has " + reservedCount + " seats reserved");
    }
}
