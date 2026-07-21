package com.swiggysystem.orderservice.entity;

public enum OrderStatus {

    PLACED,           // order just placed
    CONFIRMED,        // payment successful
    PREPARING,        // restaurant preparing food
    RIDER_ASSIGNED,   // rider matched
    PICKED_UP,        // rider picked up food
    DELIVERED,        // completed
    CANCELLED         // cancelled

}
