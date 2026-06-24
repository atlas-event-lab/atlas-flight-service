package com.atlas.flight.service;

import com.atlas.flight.entity.Flight;
import com.atlas.flight.event.FlightEventPayloadFactory;
import com.atlas.flight.messaging.OutboxEventWriter;
import com.atlas.flight.repository.FlightRepository;
import com.atlas.flight.shared.messaging.EventType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightCatalogReconciler {

  private final FlightRepository flightRepository;
  private final OutboxEventWriter outboxEventWriter;
  private final FlightEventPayloadFactory payloadFactory;

  @Transactional
  public void reconcile() {

    List<Flight> flights = flightRepository.findFlightsWithoutCreatedEvent();
    log.info("{} flights found without Created Event", flights.size());

    flights.forEach(flight ->
      outboxEventWriter.write(
          flight.getId(),
          EventType.FLIGHT_CREATED,
          payloadFactory.toCatalogPayload(flight)
    ));
  }
}
