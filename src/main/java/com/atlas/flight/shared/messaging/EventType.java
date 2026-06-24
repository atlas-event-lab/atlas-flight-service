package com.atlas.flight.shared.messaging;

/**
 * Produced event types (flight-events.yaml message names). Stored on each outbox row and used by
 * {@code OutboxRelay} to resolve the destination topic. Names are past-tense, completed facts
 * (events.md §Naming Rules).
 */
public enum EventType {
    FLIGHT_CREATED,
    FLIGHT_UPDATED,
    FLIGHT_DELETED
}
