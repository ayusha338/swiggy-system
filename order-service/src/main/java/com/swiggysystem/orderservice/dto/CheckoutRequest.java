package com.swiggysystem.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CheckoutRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "restaurantId is required")
    private Long restaurantId;

    @NotBlank(message = "idempotencyKey is required")
    private String idempotencyKey;

    @NotBlank(message = "paymentMethod is required")
    private String paymentMethod;

    @NotEmpty(message = "cart must contain at least one item")
    @Valid
    private List<CheckoutItem> items;

    @Getter
    @Setter
    public static class CheckoutItem {

        @NotBlank(message = "menuItemId is required")
        private String menuItemId;

        @NotBlank(message = "itemName is required")
        private String itemName;

        @NotNull
        @Positive(message = "quantity must be positive")
        private Integer quantity;

        @NotNull
        @Positive(message = "pricePerUnit must be positive")
        private BigDecimal pricePerUnit;
    }
}