package com.atlas.flight.config;

import com.atlas.flight.shared.messaging.EventTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka producer configuration.
 * KafkaTemplate and ProducerFactory are auto-configured from application.yml
 * (spring.kafka.producer.*). This class declares all flight.* topics owned by
 * Flight Service; KafkaAdmin creates them on startup if they do not exist (topics.md).
 */
@Configuration
public class KafkaProducerConfig {

    @Bean
    public NewTopic flightCreatedTopic() {
        return TopicBuilder.name(EventTopics.FLIGHT_CREATED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic flightUpdatedTopic() {
        return TopicBuilder.name(EventTopics.FLIGHT_UPDATED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic flightDeletedTopic() {
        return TopicBuilder.name(EventTopics.FLIGHT_DELETED)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
