package com.swiggysystem.trackingservice.controller;

import com.swiggysystem.trackingservice.service.LocationProducerService;
import com.swiggysystem.trackingservice.service.SseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class LocationController {

    private final LocationProducerService locationProducerService;

    private final SseEmitterRegistry sseEmitterRegistry;

    @PostMapping("/{riderId}/location")
    public void pushLocation(
            @PathVariable String riderId,
            @RequestParam double latitude,
            @RequestParam double longitude) {
        locationProducerService.publishLocation(riderId, latitude, longitude);
    }

    @GetMapping("/{riderId}/stream")
    public SseEmitter streamLocation(@PathVariable String riderId) {
        return sseEmitterRegistry.register(riderId);
    }
}