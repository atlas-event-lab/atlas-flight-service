package com.atlas.flight.messaging;

import com.atlas.flight.entity.OutboxEvent;
import com.atlas.flight.entity.OutboxStatus;
import com.atlas.flight.repository.OutboxRepository;
import com.atlas.flight.shared.messaging.EventTopics;
import com.atlas.flight.shared.messaging.EventType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxRelayTest {

    @Mock OutboxRepository outboxRepository;
    @Mock KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    OutboxRelay relay;

    private OutboxEvent pendingFlightCreated() {
        UUID flightId = UUID.randomUUID();
        String envelope = "{\"eventId\":\"" + UUID.randomUUID()
                + "\",\"eventType\":\"FLIGHT_CREATED\",\"payload\":{\"flightId\":\"" + flightId + "\"}}";
        return new OutboxEvent(UUID.randomUUID(), "Flight", flightId, EventType.FLIGHT_CREATED, 1, envelope);
    }

    @Test
    void publishPending_sendsToTopic_andMarksPublished() {
        OutboxEvent event = pendingFlightCreated();
        when(outboxRepository.claimBatchForPublishing()).thenReturn(List.of(event));
        CompletableFuture<SendResult<String, Object>> ok = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq(EventTopics.FLIGHT_CREATED), eq(event.getAggregateId().toString()), any()))
                .thenReturn(ok);

        relay.publishPending();

        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
    }

    @Test
    void publishPending_sendFails_marksFailed() {
        OutboxEvent event = pendingFlightCreated();
        when(outboxRepository.claimBatchForPublishing()).thenReturn(List.of(event));
        CompletableFuture<SendResult<String, Object>> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("broker down"));
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(failed);

        relay.publishPending();

        assertThat(event.getStatus()).isEqualTo(OutboxStatus.FAILED);
    }
}
