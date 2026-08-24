package com.ecommerce.order.service;

import com.ecommerce.order.client.CartClient;
import com.ecommerce.order.client.dto.CartClientResponse;
import com.ecommerce.order.client.dto.CartItemClientResponse;
import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderItem;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.kafka.OrderKafkaProducer;
import com.ecommerce.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartClient cartClient;

    @Mock
    private OrderKafkaProducer orderKafkaProducer;

    private MeterRegistry meterRegistry = new SimpleMeterRegistry();

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(orderRepository, cartClient, orderKafkaProducer, meterRegistry);
    }

    @Test
    void createOrder_Success() {
        String userId = "user1";
        CreateOrderRequest request = new CreateOrderRequest("Main St 123");

        CartItemClientResponse cartItem = CartItemClientResponse.builder()
                .productId("p1")
                .productName("Laptop")
                .unitPrice(new BigDecimal("1000.00"))
                .quantity(2)
                .subtotal(new BigDecimal("2000.00"))
                .build();

        CartClientResponse cart = CartClientResponse.builder()
                .userId(userId)
                .items(List.of(cartItem))
                .totalPrice(new BigDecimal("2000.00"))
                .totalItems(2)
                .build();

        when(cartClient.getCart(userId)).thenReturn(cart);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(100L);
            return o;
        });

        OrderResponse response = orderService.createOrder(userId, request, userId, null);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(userId, response.getUserId());
        assertEquals(OrderStatus.PENDING, response.getStatus());
        assertEquals(new BigDecimal("2000.00"), response.getTotalAmount());
        assertEquals("Main St 123", response.getShippingAddress());

        verify(orderKafkaProducer).sendOrderCreatedEvent(any());
        verify(cartClient).clearCart(userId);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_EmptyCart_ThrowsBadRequest() {
        when(cartClient.getCart("user1")).thenReturn(CartClientResponse.builder().items(List.of()).build());

        assertThrows(ResponseStatusException.class,
                () -> orderService.createOrder("user1", new CreateOrderRequest("Main St 123"), "user1", null));

        verify(orderKafkaProducer, never()).sendOrderCreatedEvent(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void updateOrderStatus_Cancelled_PublishesOrderCancelledEvent() {
        Order existingOrder = Order.builder()
                .id(1L)
                .userId("user1")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("1000.00"))
                .shippingAddress("Main St 123")
                .items(new ArrayList<>())
                .build();

        OrderItem item = OrderItem.builder()
                .id(10L)
                .productId("p1")
                .quantity(3)
                .subtotal(new BigDecimal("300.00"))
                .unitPrice(new BigDecimal("100.00"))
                .productName("Product 1")
                .build();
        existingOrder.addItem(item);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.updateOrderStatus(1L, OrderStatus.CANCELLED);

        assertEquals(OrderStatus.CANCELLED, response.getStatus());
        verify(orderKafkaProducer).sendOrderCancelledEvent(any());
    }
}
