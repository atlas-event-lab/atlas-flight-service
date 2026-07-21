package com.atlas.flight.messaging;

import com.atlas.flight.service.FlightCatalogReconciler;
import com.atlas.flight.service.ResyncResult;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

/**
 * Operational endpoint to resync the flight catalog for a read-model rebuild (ADR-0025, Experiment
 * 07): {@code POST /actuator/resync} re-emits the current state of every flight through the outbox.
 * <p>
 * Same operational-control pattern as {@code dlqreplay} (ADR-0022): exposed on the internal
 * management port only (9090, not published via ingress); access is network- + RBAC-gated through
 * the Kubernetes API proxy. Deliberate, no redeploy. Intended to run in a maintenance window as part
 * of an orchestrated rebuild (catalog resync before availability resync).
 */
@Component
@Endpoint(id = "resync")
@RequiredArgsConstructor
public class FlightResyncEndpoint {

    private final FlightCatalogReconciler reconciler;

    @WriteOperation
    public ResyncResult resync() {
        return reconciler.resyncAll();
    }
}
