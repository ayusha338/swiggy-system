package com.swiggysystem.catalogservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateRestaurantRequest {

    @NotBlank(message = "name is required")
    private String name;

    @NotEmpty(message = "cuisine list cannot be empty")
    private List<String> cuisine;

}