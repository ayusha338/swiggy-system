package com.swiggysystem.catalogservice.controller;

import com.swiggysystem.catalogservice.document.MenuItem;
import com.swiggysystem.catalogservice.dto.CreateRestaurantRequest;
import com.swiggysystem.catalogservice.dto.RestaurantResponse;
import com.swiggysystem.catalogservice.service.RestaurantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @PostMapping
    public ResponseEntity<RestaurantResponse> createRestaurant(@Valid @RequestBody CreateRestaurantRequest request) {
        RestaurantResponse response = restaurantService.createRestaurant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{restaurantId}")
    public ResponseEntity<RestaurantResponse> getRestaurant(@PathVariable String restaurantId) {
        RestaurantResponse response = restaurantService.getRestaurantById(restaurantId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{restaurantId}/menu-items")
    public ResponseEntity<RestaurantResponse> addMenuItem(
            @PathVariable String restaurantId,
            @RequestBody MenuItem menuItem) {
        RestaurantResponse response = restaurantService.addMenuItem(restaurantId, menuItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{restaurantId}/status")
    public ResponseEntity<RestaurantResponse> toggleOpenStatus(
            @PathVariable String restaurantId,
            @RequestParam boolean isOpen) {
        RestaurantResponse response = restaurantService.toggleOpenStatus(restaurantId, isOpen);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<RestaurantResponse>> searchByName(@RequestParam String name) {
        List<RestaurantResponse> results = restaurantService.searchByName(name);
        return ResponseEntity.ok(results);
    }
}