package com.swiggysystem.orderservice.service;

import com.swiggysystem.orderservice.client.AssignmentServiceClient;
import com.swiggysystem.orderservice.dto.RiderCandidateResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentServiceCaller {

    private final AssignmentServiceClient assignmentServiceClient;

    @CircuitBreaker(name = "assignmentService", fallbackMethod = "fallbackNearbyRiders")
    public List<RiderCandidateResponse> findNearbyRiders(double lat, double lng, double radius) {
        return assignmentServiceClient.findNearbyRiders(lat, lng, radius);
    }

    private List<RiderCandidateResponse> fallbackNearbyRiders(double lat, double lng, double radius, Throwable t) {
        log.warn("Circuit breaker fallback triggered for assignment-service: {}", t.getMessage());
        return List.of();
    }
}