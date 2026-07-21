package com.atlas.flight.repository;

import com.atlas.flight.entity.Airline;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for seeded Airline reference data. */
public interface AirlineRepository extends JpaRepository<Airline, UUID> {}
