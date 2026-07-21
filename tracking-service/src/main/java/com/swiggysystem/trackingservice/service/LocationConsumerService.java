package com.swiggysystem.trackingservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiggysystem.trackingservice.dto.LocationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationConsumerService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String REDIS_GEO_KEY = "riders:locations";

    @KafkaListener(topics = "rider-location-updates", groupId = "tracking-service-group")
    public void consumeLocationUpdate(String message) {
        try {
            LocationEvent event = objectMapper.readValue(message, LocationEvent.class);

            Point point = new Point(event.getLongitude(), event.getLatitude());
            redisTemplate.opsForGeo().add(REDIS_GEO_KEY, point, event.getRiderId());

            String channel = "rider-updates:" + event.getRiderId();
            redisTemplate.convertAndSend(channel, message);

            log.info("Updated location for rider {} at ({}, {})",
                    event.getRiderId(), event.getLatitude(), event.getLongitude());

        } catch (Exception e) {
            log.error("Failed to process location update: {}", message, e);
        }
    }
}