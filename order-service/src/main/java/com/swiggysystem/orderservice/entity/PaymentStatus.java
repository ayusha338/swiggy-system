package com.swiggysystem.orderservice.entity;

public enum PaymentStatus {
    PENDING,   // gateway se response ka wait
    SUCCESS,   // payment complete
    FAILED     // payment fail hui
}