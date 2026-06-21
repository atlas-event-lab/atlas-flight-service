package com.atlas.flight.event;

import java.util.UUID;

/** Payload for {@code FlightDeleted} (flight-events.yaml FlightDeletedPayload). */
public record FlightDeletedPayload(UUID flightId) {}
