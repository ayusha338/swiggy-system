package com.swiggysystem.catalogservice.exception;

public class RestaurantNotFoundException extends RuntimeException {
    public RestaurantNotFoundException(String restaurantId) {
        super("Restaurant not found with id: " + restaurantId);
    }
}