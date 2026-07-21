package com.atlas.flight.repository;

import com.atlas.flight.entity.Airport;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for seeded Airport reference data. */
public interface AirportRepository extends JpaRepository<Airport, UUID> {}
