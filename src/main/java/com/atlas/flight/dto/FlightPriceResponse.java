package com.atlas.flight.dto;

import com.atlas.flight.entity.FlightStatus;

import java.util.UUID;

/** Minimal price read for service callers (flight.yaml FlightPriceResponse, ADR-0004). */
public record FlightPriceResponse(
        UUID flightId,
        MoneyResponse basePrice,
        FlightStatus status
) {}
