package com.atlas.flight.service;

/**
 * Summary of a catalog resync (ADR-0025): how many current-state catalog events were re-emitted
 * through the outbox for a read-model rebuild.
 *
 * @param active    ACTIVE flights re-emitted as {@code FLIGHT_CREATED} (upsert)
 * @param withdrawn WITHDRAWN flights re-emitted as {@code FLIGHT_DELETED}
 */
public record ResyncResult(int active, int withdrawn) {
    public int total() {
        return active + withdrawn;
    }
}
