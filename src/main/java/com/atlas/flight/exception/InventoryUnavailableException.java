package com.atlas.flight.exception;

import java.util.UUID;

/**
 * Thrown when the capacity-shrink check cannot reach Inventory's availability API.
 * A transient query failure is treated as a failed precondition (the check is never
 * silently skipped — feature.md §Capacity-Shrink Validation) → 503 Service Unavailable.
 */
public class InventoryUnavailableException extends RuntimeException {

    public InventoryUnavailableException(UUID flightId, Throwable cause) {
        super("Unable to verify reserved capacity with Inventory for flight " + flightId, cause);
    }
}
