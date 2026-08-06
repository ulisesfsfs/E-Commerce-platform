package com.ecommerce.order.service;

import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.PagedResponse;

public interface OrderService {

    OrderResponse createOrder(String userId, CreateOrderRequest request);

    OrderResponse getOrderById(Long orderId);

    PagedResponse<OrderResponse> getUserOrders(String userId, int page, int size);

    OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus);
}
