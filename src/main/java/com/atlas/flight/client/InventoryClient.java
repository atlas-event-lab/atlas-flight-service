package com.atlas.flight.client;

import com.atlas.flight.client.dto.AvailabilityResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Read-only REST client for the Inventory Service (ARCH-003, ARCH-006). Used solely to read
 * the reserved seat count when an update lowers a flight's capacity (capacity-shrink
 * validation, feature.md). Flight never reads Inventory's database.
 */
@FeignClient(name = "inventory-service", url = "${clients.inventory.base-url}")
public interface InventoryClient {

    /** Calls Inventory {@code GET /inventory/{resourceType}/{resourceId}}. */
    @GetMapping("/api/v1/inventory/{resourceType}/{resourceId}")
    AvailabilityResponse getAvailability(@PathVariable String resourceType, @PathVariable UUID resourceId);
}
