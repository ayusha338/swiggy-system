package com.swiggysystem.catalogservice.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "restaurants")
@Getter
@Setter
public class Restaurant {

    @Id
    private String id;

    private String name;
    private List<String> cuisine;
    private boolean isOpen;
    private Double rating;
    private List<MenuItem> menuItems;

}