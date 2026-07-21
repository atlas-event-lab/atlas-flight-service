package com.atlas.flight.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Paginated flights (flight.yaml FlightListResponse = PageResponse + content[]).
 * Built from a Spring Data {@link Page} (API-006).
 */
public record FlightListResponse(List<FlightResponse> content, int page, int size, long totalElements, int totalPages) {
    public static FlightListResponse from(Page<FlightResponse> page) {
        return new FlightListResponse(
                page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
