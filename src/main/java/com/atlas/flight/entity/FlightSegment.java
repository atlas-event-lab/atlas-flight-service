package com.atlas.flight.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * A single leg of a {@link Flight} (one or more per flight; see domain/trip.md).
 * Airport references are local lookups (ARCH-004), never cross-service FKs.
 */
@Entity
@Table(name = "flight_segments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class FlightSegment {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @Setter
    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @Column(name = "sequence", nullable = false)
    private int sequence;

    @Column(name = "origin_airport_id", nullable = false)
    private UUID originAirportId;

    @Column(name = "destination_airport_id", nullable = false)
    private UUID destinationAirportId;

    @Column(name = "departure_time", nullable = false)
    private Instant departureTime;

    @Column(name = "arrival_time", nullable = false)
    private Instant arrivalTime;

    public FlightSegment(UUID id, int sequence, UUID originAirportId, UUID destinationAirportId,
                         Instant departureTime, Instant arrivalTime) {
        this.id = id;
        this.sequence = sequence;
        this.originAirportId = originAirportId;
        this.destinationAirportId = destinationAirportId;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }
}
