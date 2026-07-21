package com.swiggysystem.catalogservice.dto;

import com.swiggysystem.catalogservice.document.MenuItem;
import com.swiggysystem.catalogservice.document.Restaurant;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RestaurantResponse {

    private String id;
    private String name;
    private List<String> cuisine;
    private boolean isOpen;
    private Double rating;
    private List<MenuItem> menuItems;

    public static RestaurantResponse fromDocument(Restaurant restaurant) {
        RestaurantResponse response = new RestaurantResponse();
        response.setId(restaurant.getId());
        response.setName(restaurant.getName());
        response.setCuisine(restaurant.getCuisine());
        response.setOpen(restaurant.isOpen());
        response.setRating(restaurant.getRating());
        response.setMenuItems(restaurant.getMenuItems());
        return response;
    }
}