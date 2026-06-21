package com.atlas.flight.repository;

import com.atlas.flight.entity.Airline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/** Repository for seeded Airline reference data. */
public interface AirlineRepository extends JpaRepository<Airline, UUID> {
}
