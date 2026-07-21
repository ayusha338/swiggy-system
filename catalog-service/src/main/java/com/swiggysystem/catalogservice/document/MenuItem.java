package com.swiggysystem.catalogservice.document;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

// yeh apni khud ki collection nahi hai - Restaurant document ke andar nested rahega
// this is NOT its own collection - it lives nested inside the Restaurant document
@Getter
@Setter
public class MenuItem {

    private String menuItemId;
    private String name;
    private BigDecimal price;
    private boolean available;
    private String description;

}