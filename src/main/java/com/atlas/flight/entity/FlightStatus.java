package com.atlas.flight.entity;

/**
 * Lifecycle status of a catalog {@link Flight} (services/flight/service.md).
 * A withdrawn flight is soft-deactivated (no hard delete) and no longer bookable.
 */
public enum FlightStatus {

    /** Bookable; present in the catalog. */
    ACTIVE,

    /** Withdrawn from sale (soft delete); no longer bookable. */
    WITHDRAWN
}
