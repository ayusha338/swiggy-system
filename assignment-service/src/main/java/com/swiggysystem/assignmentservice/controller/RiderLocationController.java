package com.swiggysystem.assignmentservice.controller;

import com.swiggysystem.assignmentservice.service.RiderCandidate;
import com.swiggysystem.assignmentservice.service.RiderLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/riders")
@RequiredArgsConstructor
public class RiderLocationController {

    private final RiderLocationService riderLocationService;

    @PostMapping("/{riderId}/location")
    public void updateLocation(
            @PathVariable String riderId,
            @RequestParam double latitude,
            @RequestParam double longitude) {
        riderLocationService.updateRiderLocation(riderId, latitude, longitude);
    }

    @DeleteMapping("/{riderId}/location")
    public void removeLocation(@PathVariable String riderId) {
        riderLocationService.removeRiderLocation(riderId);
    }

    @PostMapping("/{riderId}/available")
    public void markAvailable(@PathVariable String riderId) {
        riderLocationService.setRiderAvailable(riderId);
    }

    @PostMapping("/{riderId}/busy")
    public void markBusy(@PathVariable String riderId) {
        riderLocationService.setRiderBusy(riderId);
    }

    @PostMapping("/{riderId}/rating")
    public void setRating(@PathVariable String riderId, @RequestParam double rating) {
        riderLocationService.setRiderRating(riderId, rating);
    }

    @GetMapping("/nearby")
    public List<RiderCandidate> findBestRiders(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "3") double radiusKm) {
        return riderLocationService.findBestRiders(latitude, longitude, radiusKm);
    }
}