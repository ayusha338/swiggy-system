package com.swiggysystem.orderservice.client;

import com.swiggysystem.orderservice.dto.RiderCandidateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "assignment-service", url = "${assignment-service.base-url}")
public interface AssignmentServiceClient {

    @GetMapping("/api/riders/nearby")
    List<RiderCandidateResponse> findNearbyRiders(
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam(value = "radiusKm", defaultValue = "3") double radiusKm);

}
