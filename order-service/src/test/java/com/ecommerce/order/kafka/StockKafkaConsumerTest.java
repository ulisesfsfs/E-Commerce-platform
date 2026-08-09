package com.ecommerce.order.kafka;

import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.event.StockReservationFailedEvent;
import com.ecommerce.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockKafkaConsumerTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private StockKafkaConsumer stockKafkaConsumer;

    @Test
    void consumeStockReservationFailed_PendingOrder_CancelsOrder() {
        Order order = Order.builder()
                .id(1L)
                .userId("user1")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("1000.00"))
                .shippingAddress("Main St 123")
                .items(new ArrayList<>())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        StockReservationFailedEvent event = StockReservationFailedEvent.builder()
                .orderId(1L)
                .productId("p1")
                .requestedQuantity(5)
                .reason("Insufficient stock for product 'Laptop'. Requested: 5, Available: 0")
                .build();

        stockKafkaConsumer.consumeStockReservationFailed(event);

        verify(orderRepository).save(argThat(saved -> saved.getStatus() == OrderStatus.CANCELLED));
    }

    @Test
    void consumeStockReservationFailed_AlreadyCancelled_DoesNotSaveAgain() {
        Order order = Order.builder()
                .id(1L)
                .userId("user1")
                .status(OrderStatus.CANCELLED)
                .totalAmount(new BigDecimal("1000.00"))
                .shippingAddress("Main St 123")
                .items(new ArrayList<>())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        StockReservationFailedEvent event = StockReservationFailedEvent.builder()
                .orderId(1L)
                .productId("p1")
                .requestedQuantity(5)
                .reason("Insufficient stock")
                .build();

        stockKafkaConsumer.consumeStockReservationFailed(event);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void consumeStockReservationFailed_OrderNotFound_DoesNothing() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        StockReservationFailedEvent event = StockReservationFailedEvent.builder()
                .orderId(99L)
                .productId("p1")
                .requestedQuantity(2)
                .reason("Insufficient stock")
                .build();

        stockKafkaConsumer.consumeStockReservationFailed(event);

        verify(orderRepository, never()).save(any());
    }
}
