package com.ecommerce.order.service;

import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.PagedResponse;

public interface OrderService {

    OrderResponse createOrder(String userId, CreateOrderRequest request,
                              String requesterId, String roles);

    OrderResponse getOrderById(Long orderId,
                               String requesterId, String roles);

    PagedResponse<OrderResponse> getUserOrders(String userId, int page, int size,
                                              String requesterId, String roles);

    OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus,
                                    String requesterId, String roles);

    // Backwards-compatible overload for internal callers (e.g., Kafka consumers)
    default OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        return updateOrderStatus(orderId, newStatus, null, null);
    }
}
