package com.atlas.flight.repository;

import com.atlas.flight.entity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/** Repository for seeded Airport reference data. */
public interface AirportRepository extends JpaRepository<Airport, UUID> {
}
