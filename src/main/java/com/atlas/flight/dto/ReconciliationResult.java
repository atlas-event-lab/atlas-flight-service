package com.atlas.flight.dto;

public record ReconciliationResult(
    int processedFlights,
    int createdOutboxEvents
) {}