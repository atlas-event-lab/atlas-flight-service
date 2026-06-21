package com.atlas.flight.repository;

import com.atlas.flight.entity.Flight;
import com.atlas.flight.entity.FlightStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;

/**
 * Repository for the flight catalog. Accesses only local entities (ARCH-003, DB-004).
 */
public interface FlightRepository extends JpaRepository<Flight, UUID> {

  /**
   * Business-key uniqueness check for create (services/flight/service.md).
   */
  boolean existsByFlightNumberAndDepartureTime(String flightNumber, Instant departureTime);

  /**
   * Business-key uniqueness check for update, excluding the flight being updated.
   */
  boolean existsByFlightNumberAndDepartureTimeAndIdNot(String flightNumber, Instant departureTime,
      UUID id);

  /**
   * Seeded flights eligible for bootstrap publishing.
   */
  List<Flight> findByStatus(FlightStatus status);

  @Query(
      value = """
          SELECT f.*
          FROM flights f
          LEFT JOIN outbox o
            ON o.aggregate_id = f.id
           AND o.event_type = 'FlightCreated'
          WHERE o.id IS NULL
          AND f.status = 'ACTIVE'
          """, nativeQuery = true)
  List<Flight> findFlightsWithoutCreatedEvent();
}
