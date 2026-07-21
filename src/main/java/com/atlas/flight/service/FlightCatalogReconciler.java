package com.atlas.flight.service;

import com.atlas.flight.entity.Flight;
import com.atlas.flight.entity.FlightStatus;
import com.atlas.flight.event.FlightDeletedPayload;
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

        flights.forEach(flight -> outboxEventWriter.write(
                flight.getId(), EventType.FLIGHT_CREATED, payloadFactory.toCatalogPayload(flight)));
    }

    /**
     * Full catalog resync for a read-model rebuild (ADR-0025, Experiment 07). Re-emits the current
     * state of <b>every</b> flight from {@code flight_db} through the outbox — ACTIVE flights as
     * {@code FLIGHT_CREATED} (Search upserts created/updated identically), WITHDRAWN flights as
     * {@code FLIGHT_DELETED}. Unlike {@link #reconcile()} (which only backfills flights missing a
     * created event), this republishes unconditionally so a wiped read model can be fully rebuilt.
     */
    @Transactional
    public ResyncResult resyncAll() {
        List<Flight> active = flightRepository.findByStatus(FlightStatus.ACTIVE);
        List<Flight> withdrawn = flightRepository.findByStatus(FlightStatus.WITHDRAWN);

        active.forEach(flight -> outboxEventWriter.write(
                flight.getId(), EventType.FLIGHT_CREATED, payloadFactory.toCatalogPayload(flight)));
        withdrawn.forEach(flight -> outboxEventWriter.write(
                flight.getId(), EventType.FLIGHT_DELETED, new FlightDeletedPayload(flight.getId())));

        log.warn(
                "Catalog resync: re-emitted {} active (CREATED) + {} withdrawn (DELETED) flight events",
                active.size(),
                withdrawn.size());
        return new ResyncResult(active.size(), withdrawn.size());
    }
}
