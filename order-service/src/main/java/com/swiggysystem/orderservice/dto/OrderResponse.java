package com.swiggysystem.orderservice.dto;

import com.swiggysystem.orderservice.entity.Order;
import com.swiggysystem.orderservice.entity.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class OrderResponse {

    private Long orderId;
    private Long userId;
    private Long restaurantId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private List<ItemResponse> items;
    private PaymentResponse payment;
    private Instant createdAt;

    @Getter
    @Setter
    public static class ItemResponse {
        private String itemName;
        private Integer quantity;
        private BigDecimal pricePerUnit;
    }

    @Getter
    @Setter
    public static class PaymentResponse {
        private String paymentMethod;
        private String status;
        private String gatewayTransactionId;
    }

    // entity -> DTO conversion yahin karte hain, controller/service mein entity kabhi expose nahi hoti
    public static OrderResponse fromEntity(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getId());
        response.setUserId(order.getUserId());
        response.setRestaurantId(order.getRestaurantId());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setCreatedAt(order.getCreatedAt());

        response.setItems(order.getItems().stream()
                .map(item -> {
                    ItemResponse itemResponse = new ItemResponse();
                    itemResponse.setItemName(item.getItemName());
                    itemResponse.setQuantity(item.getQuantity());
                    itemResponse.setPricePerUnit(item.getPricePerUnit());
                    return itemResponse;
                })
                .collect(Collectors.toList()));

        if (order.getPayment() != null) {
            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.setPaymentMethod(order.getPayment().getPaymentMethod());
            paymentResponse.setStatus(order.getPayment().getStatus().name());
            paymentResponse.setGatewayTransactionId(order.getPayment().getGatewayTransactionId());
            response.setPayment(paymentResponse);
        }

        return response;
    }
}