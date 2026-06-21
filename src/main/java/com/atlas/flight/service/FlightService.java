package com.atlas.flight.service;

import com.atlas.flight.dto.CreateFlightRequest;
import com.atlas.flight.dto.FlightListResponse;
import com.atlas.flight.dto.FlightResponse;
import com.atlas.flight.dto.UpdateFlightRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

/**
 * Admin catalog operations. Authorization (RBAC {@code ADMIN}) is enforced here, in the
 * business service layer (SEC-004), via {@code @PreAuthorize}.
 */
public interface FlightService {

    @PreAuthorize("hasRole('ADMIN')")
    FlightResponse createFlight(CreateFlightRequest request);

    @PreAuthorize("hasRole('ADMIN')")
    FlightResponse updateFlight(UUID flightId, UpdateFlightRequest request);

    @PreAuthorize("hasRole('ADMIN')")
    void withdrawFlight(UUID flightId);

    @PreAuthorize("hasRole('ADMIN')")
    FlightResponse getFlight(UUID flightId);

    @PreAuthorize("hasRole('ADMIN')")
    FlightListResponse listFlights(Pageable pageable);
}
