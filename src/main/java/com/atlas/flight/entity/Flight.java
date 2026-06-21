package com.atlas.flight.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate Root for the flight catalog. Owns descriptive data, schedule, total seat
 * capacity, base price and its segments (services/flight/service.md). Live seat
 * availability is NOT owned here — Inventory owns that (ARCH-002, DB-001).
 */
@Entity
@Table(name = "flights")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Flight {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID id;

    @Column(name = "flight_number", nullable = false, length = 20)
    @ToString.Include
    private String flightNumber;

    @Column(name = "airline_id", nullable = false)
    private UUID airlineId;

    @Column(name = "origin_airport_id", nullable = false)
    private UUID originAirportId;

    @Column(name = "destination_airport_id", nullable = false)
    private UUID destinationAirportId;

    @Column(name = "departure_time", nullable = false)
    private Instant departureTime;

    @Column(name = "arrival_time", nullable = false)
    private Instant arrivalTime;

    @Column(name = "total_seats", nullable = false)
    private int totalSeats;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount",   column = @Column(name = "base_price_amount", nullable = false, precision = 19, scale = 2)),
            @AttributeOverride(name = "currency", column = @Column(name = "currency",          nullable = false, length = 3))
    })
    private Money basePrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FlightStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sequence ASC")
    private List<FlightSegment> segments = new ArrayList<>();

    public Flight(UUID id, String flightNumber, UUID airlineId,
                  UUID originAirportId, UUID destinationAirportId,
                  Instant departureTime, Instant arrivalTime,
                  int totalSeats, Money basePrice, FlightStatus status) {
        this.id = id;
        this.flightNumber = flightNumber;
        this.airlineId = airlineId;
        this.originAirportId = originAirportId;
        this.destinationAirportId = destinationAirportId;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.totalSeats = totalSeats;
        this.basePrice = basePrice;
        this.status = status;
    }

    /** Replaces catalog/schedule/price/capacity fields on update (PUT semantics). */
    public void update(String flightNumber, UUID airlineId,
                       UUID originAirportId, UUID destinationAirportId,
                       Instant departureTime, Instant arrivalTime,
                       int totalSeats, Money basePrice) {
        this.flightNumber = flightNumber;
        this.airlineId = airlineId;
        this.originAirportId = originAirportId;
        this.destinationAirportId = destinationAirportId;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.totalSeats = totalSeats;
        this.basePrice = basePrice;
    }

    /** Soft-deactivates the flight (no hard delete); it is no longer bookable. */
    public void withdraw() {
        this.status = FlightStatus.WITHDRAWN;
    }

    public void addSegment(FlightSegment segment) {
        segments.add(segment);
        segment.setFlight(this);
    }

    /** Replaces all segments (PUT semantics); orphanRemoval deletes the old rows. */
    public void replaceSegments(List<FlightSegment> newSegments) {
        segments.clear();
        for (FlightSegment segment : newSegments) {
            addSegment(segment);
        }
    }

    public List<FlightSegment> getSegments() {
        return Collections.unmodifiableList(segments);
    }
}
