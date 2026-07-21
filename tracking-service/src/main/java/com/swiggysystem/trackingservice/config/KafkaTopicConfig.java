package com.swiggysystem.trackingservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic riderLocationTopic() {
        return org.springframework.kafka.config.TopicBuilder
                .name("rider-location-updates")
                .partitions(3)          // parallelism ke liye multiple partitions
                .replicas(1)             // sirf 1 broker hai humare paas, isliye replication factor 1
                .build();
    }
}