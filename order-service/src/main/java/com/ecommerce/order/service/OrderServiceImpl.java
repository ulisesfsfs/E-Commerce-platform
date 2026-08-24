package com.ecommerce.order.service;

import com.ecommerce.order.client.CartClient;
import com.ecommerce.order.client.dto.CartClientResponse;
import com.ecommerce.order.client.dto.CartItemClientResponse;
import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderItem;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderItemResponse;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.PagedResponse;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import io.micrometer.core.instrument.MeterRegistry;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartClient cartClient;
    private final com.ecommerce.order.kafka.OrderKafkaProducer orderKafkaProducer;
    private final MeterRegistry meterRegistry;

    @Override
    @Transactional
    public OrderResponse createOrder(String userId, CreateOrderRequest request, String requesterId, String roles) {
        validateOwnerOrAdmin(userId, requesterId, roles);

        // 1. Fetch user's cart from cart-service
        CartClientResponse cart = fetchCartOrThrow(userId);

        // 2. Build order locally in PENDING state
        Order order = buildOrder(userId, request.getShippingAddress(), cart);

        // 3. Save order in PostgreSQL (order_db)
        Order savedOrder = orderRepository.save(order);

        // 4. Publish OrderCreatedEvent to Kafka (decoupled from product-service)
        publishOrderCreatedEvent(savedOrder);

        // 5. Clear cart in cart-service
        clearCartQuietly(userId);

        meterRegistry.counter("ecommerce.orders.created", "status", "success").increment();

        meterRegistry.counter("ecommerce.orders.amount", "currency", "ARS")
                .increment(order.getTotalAmount().doubleValue());

        return toResponse(savedOrder);
    }

    private void publishOrderCreatedEvent(Order order) {
        var itemEvents = order.getItems().stream()
                .map(item -> com.ecommerce.order.event.OrderCreatedEvent.OrderItemEvent.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .build())
                .toList();

        com.ecommerce.order.event.OrderCreatedEvent event = com.ecommerce.order.event.OrderCreatedEvent.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .items(itemEvents)
                .build();

        orderKafkaProducer.sendOrderCreatedEvent(event);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, String requesterId, String roles) {
        Order order = getOrderOrThrow(orderId);

        validateOwnerOrAdmin(order.getUserId(), requesterId, roles);
        return toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> getUserOrders(String userId, int page, int size, String requesterId,
            String roles) {
        validateOwnerOrAdmin(userId, requesterId, roles);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> ordersPage = orderRepository.findByUserId(userId, pageable);
        return toPagedResponse(ordersPage);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus, String requesterId, String roles) {
        Order order = getOrderOrThrow(orderId);

        // allow internal/system callers when both requesterId and roles are null
        if (requesterId != null || roles != null) {
            // Only admin can update order status when called from external users
            if (!isAdmin(roles)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can update order status");
            }
        }

        // If the order is cancelled, publish OrderCancelledEvent to Kafka so
        // product-service restores stock asynchronously
        if (newStatus == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.CANCELLED) {
            publishOrderCancelledEvent(order);
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        return toResponse(updatedOrder);
    }

    private void publishOrderCancelledEvent(Order order) {
        var itemEvents = order.getItems().stream()
                .map(item -> com.ecommerce.order.event.OrderCancelledEvent.OrderItemEvent.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build())
                .toList();

        com.ecommerce.order.event.OrderCancelledEvent event = com.ecommerce.order.event.OrderCancelledEvent.builder()
                .orderId(order.getId())
                .items(itemEvents)
                .build();

        orderKafkaProducer.sendOrderCancelledEvent(event);
    }

    // ---- Orchestration Helpers ----

    private CartClientResponse fetchCartOrThrow(String userId) {
        CartClientResponse cart = cartClient.getCart(userId);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot place an order with an empty cart");
        }
        return cart;
    }

    private Order buildOrder(String userId, String shippingAddress, CartClientResponse cart) {
        Order order = Order.builder()
                .userId(userId)
                .shippingAddress(shippingAddress)
                .status(OrderStatus.PENDING)
                .totalAmount(cart.getTotalPrice())
                .items(new ArrayList<>())
                .build();

        for (CartItemClientResponse cartItem : cart.getItems()) {
            BigDecimal subtotal = cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .productId(cartItem.getProductId())
                    .productName(cartItem.getProductName())
                    .unitPrice(cartItem.getUnitPrice())
                    .quantity(cartItem.getQuantity())
                    .subtotal(subtotal)
                    .build();

            order.addItem(orderItem);
        }
        return order;
    }

    private void clearCartQuietly(String userId) {
        try {
            cartClient.clearCart(userId);
        } catch (Exception e) {
            log.error("Failed to clear cart for user: {}. Order was placed successfully.", userId, e);
        }
    }

    private Order getOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Order with id '" + orderId + "' not found"));
    }

    private OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .shippingAddress(order.getShippingAddress())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(order.getItems().stream()
                        .map(item -> OrderItemResponse.builder()
                                .id(item.getId())
                                .productId(item.getProductId())
                                .productName(item.getProductName())
                                .unitPrice(item.getUnitPrice())
                                .quantity(item.getQuantity())
                                .subtotal(item.getSubtotal())
                                .build())
                        .toList())
                .build();
    }

    private PagedResponse<OrderResponse> toPagedResponse(Page<Order> page) {
        return PagedResponse.<OrderResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    // Other helpers//

    private void validateOwnerOrAdmin(String resourceOwnerId, String requesterId, String roles) {
        if (isAdmin(roles)) {
            return;
        }
        if (requesterId == null || !requesterId.equals(resourceOwnerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

    }

    private boolean isAdmin(String rolesHeader) {
        if (rolesHeader == null || rolesHeader.isBlank()) {
            return false;
        }
        return Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
    }
}
