package com.swiggysystem.orderservice.service;

import com.swiggysystem.orderservice.client.AssignmentServiceClient;
import com.swiggysystem.orderservice.dto.CheckoutRequest;
import com.swiggysystem.orderservice.dto.OrderResponse;
import com.swiggysystem.orderservice.dto.RiderCandidateResponse;
import com.swiggysystem.orderservice.entity.Order;
import com.swiggysystem.orderservice.entity.OrderItem;
import com.swiggysystem.orderservice.entity.OrderStatus;
import com.swiggysystem.orderservice.entity.Payment;
import com.swiggysystem.orderservice.entity.PaymentStatus;
import com.swiggysystem.orderservice.exception.OrderNotFoundException;
import com.swiggysystem.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final AssignmentServiceCaller assignmentServiceCaller;   // ab seedha AssignmentServiceClient nahi, is naye bean ke through jaate hain

    @Transactional
    public OrderResponse checkout(CheckoutRequest request) {

        // Step 1: idempotency check - agar yeh key pehle se process ho chuki hai,
        // to naya order banane ke bajaye purana wapas kar do
        Optional<Order> existingOrder = orderRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existingOrder.isPresent()) {
            return OrderResponse.fromEntity(existingOrder.get());
        }

        // Step 2: naya Order object banao
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setRestaurantId(request.getRestaurantId());
        order.setIdempotencyKey(request.getIdempotencyKey());
        order.setStatus(OrderStatus.PLACED);

        // Step 3: har cart item ke liye OrderItem banao, aur total calculate karo
        BigDecimal total = BigDecimal.ZERO;
        for (CheckoutRequest.CheckoutItem itemRequest : request.getItems()) {
            OrderItem item = new OrderItem();
            item.setMenuItemId(itemRequest.getMenuItemId());
            item.setItemName(itemRequest.getItemName());
            item.setQuantity(itemRequest.getQuantity());
            item.setPricePerUnit(itemRequest.getPricePerUnit());

            order.addItem(item);   // yeh helper method dono taraf relationship sync karta hai

            BigDecimal subtotal = itemRequest.getPricePerUnit()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            total = total.add(subtotal);
        }
        order.setTotalAmount(total);

        // Step 4: Payment record banao, PENDING status ke saath
        Payment payment = new Payment();
        payment.setAmount(total);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod(request.getPaymentMethod());

        order.assignPayment(payment);   // yeh bhi dono taraf relationship sync karta hai

        // Step 5: save karo - cascade = ALL hone ki wajah se items aur payment bhi save ho jayenge
        Order savedOrder = orderRepository.save(order);

        // Step 6: nearby riders dhundo - ab circuit breaker ke through, alag bean se
        try {
            List<RiderCandidateResponse> nearbyRiders =
                    assignmentServiceCaller.findNearbyRiders(12.9716, 77.5946, 3);
            log.info("Found {} nearby riders for order {}", nearbyRiders.size(), savedOrder.getId());
        } catch (Exception e) {
            log.warn("Unexpected error calling assignment-service: {}", e.getMessage());
        }

        return OrderResponse.fromEntity(savedOrder);
    }

    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return OrderResponse.fromEntity(order);
    }
}