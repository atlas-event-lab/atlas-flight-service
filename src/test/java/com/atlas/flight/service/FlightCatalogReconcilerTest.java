package com.atlas.flight.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlas.flight.entity.Flight;
import com.atlas.flight.entity.FlightStatus;
import com.atlas.flight.event.FlightDeletedPayload;
import com.atlas.flight.event.FlightEventPayloadFactory;
import com.atlas.flight.messaging.OutboxEventWriter;
import com.atlas.flight.repository.FlightRepository;
import com.atlas.flight.shared.messaging.EventType;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FlightCatalogReconcilerTest {

    private final FlightRepository flightRepository = mock(FlightRepository.class);
    private final OutboxEventWriter outboxEventWriter = mock(OutboxEventWriter.class);
    private final FlightEventPayloadFactory payloadFactory = mock(FlightEventPayloadFactory.class);
    private final FlightCatalogReconciler reconciler =
            new FlightCatalogReconciler(flightRepository, outboxEventWriter, payloadFactory);

    @Test
    void resyncAll_reEmitsActiveAsCreatedAndWithdrawnAsDeleted() {
        UUID activeId = UUID.randomUUID();
        UUID withdrawnId = UUID.randomUUID();
        Flight active = mock(Flight.class);
        Flight withdrawn = mock(Flight.class);
        when(active.getId()).thenReturn(activeId);
        when(withdrawn.getId()).thenReturn(withdrawnId);
        when(flightRepository.findByStatus(FlightStatus.ACTIVE)).thenReturn(List.of(active));
        when(flightRepository.findByStatus(FlightStatus.WITHDRAWN)).thenReturn(List.of(withdrawn));

        ResyncResult result = reconciler.resyncAll();

        verify(outboxEventWriter).write(eq(activeId), eq(EventType.FLIGHT_CREATED), any());
        verify(outboxEventWriter).write(eq(withdrawnId), eq(EventType.FLIGHT_DELETED), any(FlightDeletedPayload.class));
        assertThat(result.active()).isEqualTo(1);
        assertThat(result.withdrawn()).isEqualTo(1);
        assertThat(result.total()).isEqualTo(2);
    }
}
