package com.atlas.flight.messaging;

import com.atlas.flight.entity.OutboxEvent;
import com.atlas.flight.repository.OutboxRepository;
import com.atlas.flight.shared.messaging.EventEnvelope;
import com.atlas.flight.shared.messaging.EventType;
import com.atlas.flight.shared.web.CorrelationIdFilter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

/**
 * Writes catalog events to the Transactional Outbox (EVT-009).
 * <p>
 * Called from inside a {@code @Transactional} Service method so the outbox row is
 * committed atomically with the flight state change — no Kafka call happens here,
 * avoiding the dual-write (coding-standards §Outbox & Event Publishing). The
 * {@code OutboxRelay} publishes the row afterwards. Catalog events are not part of a saga,
 * so {@code sagaId} is null; the envelope still carries traceId/correlationId (OBS-002).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventWriter {

    private static final String PRODUCER = "flight-service";
    private static final String AGGREGATE_TYPE = "Flight";
    private static final int EVENT_VERSION = 1;

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    /**
     * Builds the full event envelope (message-envelope.md) and stores it as a PENDING
     * outbox row. Partition key for publication is {@code aggregateId} = flightId
     * (partitioning.md).
     *
     * @param aggregateId the Flight id (also the Kafka partition key)
     * @param eventType   produced event type, e.g. {@code FLIGHT_CREATED}
     * @param payload     the business payload (never null, never carries metadata)
     */
    public void write(UUID aggregateId, EventType eventType, Object payload) {
        try {
            var envelope = new EventEnvelope<>(
                    UUID.randomUUID(),
                    eventType.name(),
                    EVENT_VERSION,
                    Instant.now(),
                    resolveTraceId(),
                    resolveCorrelationId(),
                    null,
                    PRODUCER,
                    payload);

            outboxRepository.save(new OutboxEvent(
                    UUID.randomUUID(), AGGREGATE_TYPE, aggregateId, eventType, EVENT_VERSION, serialize(envelope)));
        } catch (DuplicateKeyException ignored) {
            log.error("Failed to write outbox event: aggregateId={}, eventType={}", aggregateId, eventType);
        }
    }

    private String serialize(EventEnvelope<?> envelope) {
        try {
            return objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize event envelope for outbox: eventType=" + envelope.eventType(), e);
        }
    }

    private String resolveTraceId() {
        return resolveMdc(CorrelationIdFilter.TRACE_ID_MDC_KEY);
    }

    private String resolveCorrelationId() {
        return resolveMdc(CorrelationIdFilter.MDC_KEY);
    }

    /** Reads a value from MDC (set by {@link CorrelationIdFilter}), falls back to a new UUID. */
    private String resolveMdc(String key) {
        String value = MDC.get(key);
        return (value != null && !value.isBlank()) ? value : UUID.randomUUID().toString();
    }
}
