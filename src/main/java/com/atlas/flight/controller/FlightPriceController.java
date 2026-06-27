package com.atlas.flight.controller;

import com.atlas.flight.dto.FlightPriceResponse;
import com.atlas.flight.service.FlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Service-readable flight price endpoint (ADR-0004, flight.yaml operationId: getFlightPrice).
 * Available to any authenticated caller; no ADMIN role required.
 */
@RestController
@RequestMapping("/api/v1/flights")
@RequiredArgsConstructor
public class FlightPriceController {

    private final FlightService flightService;

    /** GET /api/v1/flights/{flightId}/price — returns base price and status. */
    @GetMapping("/{flightId}/price")
    public ResponseEntity<FlightPriceResponse> getFlightPrice(@PathVariable UUID flightId) {
        return ResponseEntity.ok(flightService.getFlightPrice(flightId));
    }
}
