package com.swiggysystem.trackingservice.service;

import com.swiggysystem.trackingservice.dto.LocationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class LocationProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String TOPIC = "rider-location-updates";

    public void publishLocation(String riderId, double latitude, double longitude) {
        LocationEvent event = new LocationEvent(riderId, latitude, longitude, System.currentTimeMillis());

        try {
            String json = objectMapper.writeValueAsString(event);   // object ko JSON string mein convert karo
            kafkaTemplate.send(TOPIC, riderId, json);                  // riderId as key - same rider ka order guarantee
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize location event", e);
        }
    }
}