package com.atlas.flight.controller;

import com.atlas.flight.dto.CreateFlightRequest;
import com.atlas.flight.dto.FlightListResponse;
import com.atlas.flight.dto.FlightResponse;
import com.atlas.flight.dto.UpdateFlightRequest;
import com.atlas.flight.service.FlightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for the admin flight catalog (flight.yaml).
 * Contains no business logic; delegates entirely to {@link FlightService}, which also
 * enforces RBAC and performs entity-to-DTO mapping (coding-standards §Layer Responsibilities).
 */
@RestController
@RequestMapping("/api/v1/flights")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class FlightController {

    private final FlightService flightService;

    /** POST /api/v1/flights — creates a flight (flight.yaml operationId: createFlight). */
    @PostMapping
    public ResponseEntity<FlightResponse> createFlight(@RequestBody @Valid CreateFlightRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(flightService.createFlight(request));
    }

    /** GET /api/v1/flights — paginated list (flight.yaml operationId: listFlights). */
    @GetMapping
    public ResponseEntity<FlightListResponse> listFlights(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(flightService.listFlights(pageable));
    }

    /** GET /api/v1/flights/{flightId} — retrieves a flight (flight.yaml operationId: getFlight). */
    @GetMapping("/{flightId}")
    public ResponseEntity<FlightResponse> getFlight(@PathVariable UUID flightId) {
        return ResponseEntity.ok(flightService.getFlight(flightId));
    }

    /** PUT /api/v1/flights/{flightId} — updates a flight (flight.yaml operationId: updateFlight). */
    @PutMapping("/{flightId}")
    public ResponseEntity<FlightResponse> updateFlight(
            @PathVariable UUID flightId,
            @RequestBody @Valid UpdateFlightRequest request) {
        return ResponseEntity.ok(flightService.updateFlight(flightId, request));
    }

    /** DELETE /api/v1/flights/{flightId} — withdraws a flight (flight.yaml operationId: withdrawFlight). */
    @DeleteMapping("/{flightId}")
    public ResponseEntity<Void> withdrawFlight(@PathVariable UUID flightId) {
        flightService.withdrawFlight(flightId);
        return ResponseEntity.noContent().build();
    }
}
