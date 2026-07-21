package com.swiggysystem.catalogservice.service;

import com.swiggysystem.catalogservice.document.MenuItem;
import com.swiggysystem.catalogservice.document.Restaurant;
import com.swiggysystem.catalogservice.dto.CreateRestaurantRequest;
import com.swiggysystem.catalogservice.dto.RestaurantResponse;
import com.swiggysystem.catalogservice.exception.RestaurantNotFoundException;
import com.swiggysystem.catalogservice.repository.RestaurantRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    public RestaurantResponse createRestaurant(CreateRestaurantRequest request) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.getName());
        restaurant.setCuisine(request.getCuisine());
        restaurant.setOpen(true);              // naya restaurant default open maana jayega
        restaurant.setRating(0.0);               // shuru mein koi rating nahi
        restaurant.setMenuItems(new ArrayList<>());  // khali menu se shuru

        Restaurant saved = restaurantRepository.save(restaurant);
        return RestaurantResponse.fromDocument(saved);
    }

    public RestaurantResponse getRestaurantById(String restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));
        return RestaurantResponse.fromDocument(restaurant);
    }

    public RestaurantResponse addMenuItem(String restaurantId, MenuItem menuItem) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        restaurant.getMenuItems().add(menuItem);
        Restaurant saved = restaurantRepository.save(restaurant);   // poora document dobara save hota hai
        return RestaurantResponse.fromDocument(saved);
    }

    public RestaurantResponse toggleOpenStatus(String restaurantId, boolean isOpen) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        restaurant.setOpen(isOpen);
        Restaurant saved = restaurantRepository.save(restaurant);
        return RestaurantResponse.fromDocument(saved);
    }

    public List<RestaurantResponse> searchByName(String name) {
        return restaurantRepository.findByNameContainingIgnoreCase(name).stream()
                .map(RestaurantResponse::fromDocument)
                .toList();
    }
}