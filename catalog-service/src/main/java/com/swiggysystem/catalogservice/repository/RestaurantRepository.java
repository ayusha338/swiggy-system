package com.swiggysystem.catalogservice.repository;

import com.swiggysystem.catalogservice.document.Restaurant;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RestaurantRepository extends MongoRepository<Restaurant, String> {

    // restaurantId ke bajaye naam se search karne ke liye (case-insensitive contains match)
    List<Restaurant> findByNameContainingIgnoreCase(String name);

    // sirf abhi open restaurants dhundo
    List<Restaurant> findByIsOpenTrue();

    // ek specific cuisine wale restaurants dhundo
    List<Restaurant> findByCuisineContaining(String cuisine);
}