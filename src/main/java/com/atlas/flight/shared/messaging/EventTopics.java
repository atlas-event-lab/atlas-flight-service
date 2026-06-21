package com.atlas.flight.shared.messaging;

/**
 * Kafka topic name constants (topics.md, naming: domain.entity.event).
 * Topics prefixed flight.* are owned by Flight Service.
 * Topic names are immutable — never rename or reuse a topic.
 */
public final class EventTopics {

    // ── Flight Service produces ───────────────────────────────────────────────
    public static final String FLIGHT_CREATED = "flight.created";
    public static final String FLIGHT_UPDATED = "flight.updated";
    public static final String FLIGHT_DELETED = "flight.deleted";

    private EventTopics() {}
}
