package com.atlas.flight.client.dto;

import java.util.UUID;

/**
 * Inventory availability query response (inventory.yaml AvailabilityResponse).
 * Only {@code reservedCount} is consumed here, for capacity-shrink validation.
 */
public record AvailabilityResponse(
        String resourceType,
        UUID resourceId,
        int totalCapacity,
        int reservedCount,
        int available,
        String status
) {}
