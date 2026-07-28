package com.ecommerce.cart.service;

import com.ecommerce.cart.domain.Cart;
import com.ecommerce.cart.domain.CartItem;
import com.ecommerce.cart.dto.AddToCartRequest;
import com.ecommerce.cart.dto.CartResponse;
import com.ecommerce.cart.dto.UpdateQuantityRequest;
import com.ecommerce.cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartServiceImpl(cartRepository);
        ReflectionTestUtils.setField(cartService, "ttlSeconds", 604800L);
    }

    @Test
    void getCart_EmptyWhenNotExists() {
        when(cartRepository.findById("user1")).thenReturn(Optional.empty());

        CartResponse response = cartService.getCart("user1");

        assertNotNull(response);
        assertEquals("user1", response.getUserId());
        assertTrue(response.getItems().isEmpty());
        assertEquals(BigDecimal.ZERO, response.getTotalPrice());
    }

    @Test
    void addItem_NewItem_CreatesCartAndItem() {
        when(cartRepository.findById("user1")).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        AddToCartRequest request = AddToCartRequest.builder()
                .productId("p1")
                .productName("Product 1")
                .unitPrice(new BigDecimal("100.00"))
                .quantity(2)
                .build();

        CartResponse response = cartService.addItem("user1", request);

        assertEquals("user1", response.getUserId());
        assertEquals(1, response.getItems().size());
        assertEquals(new BigDecimal("200.00"), response.getTotalPrice());
        assertEquals(2, response.getTotalItems());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void addItem_ExistingItem_IncreasesQuantity() {
        Cart existingCart = Cart.builder()
                .userId("user1")
                .items(new ArrayList<>())
                .build();
        existingCart.getItems().add(CartItem.builder()
                .productId("p1")
                .productName("Product 1")
                .unitPrice(new BigDecimal("100.00"))
                .quantity(1)
                .build());

        when(cartRepository.findById("user1")).thenReturn(Optional.of(existingCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        AddToCartRequest request = AddToCartRequest.builder()
                .productId("p1")
                .productName("Product 1")
                .unitPrice(new BigDecimal("100.00"))
                .quantity(2)
                .build();

        CartResponse response = cartService.addItem("user1", request);

        assertEquals(1, response.getItems().size());
        assertEquals(3, response.getTotalItems());
        assertEquals(new BigDecimal("300.00"), response.getTotalPrice());
    }

    @Test
    void updateQuantity_Success() {
        Cart existingCart = Cart.builder()
                .userId("user1")
                .items(new ArrayList<>())
                .build();
        existingCart.getItems().add(CartItem.builder()
                .productId("p1")
                .productName("Product 1")
                .unitPrice(new BigDecimal("100.00"))
                .quantity(1)
                .build());

        when(cartRepository.findById("user1")).thenReturn(Optional.of(existingCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateQuantityRequest request = new UpdateQuantityRequest(5);
        CartResponse response = cartService.updateQuantity("user1", "p1", request);

        assertEquals(5, response.getTotalItems());
        assertEquals(new BigDecimal("500.00"), response.getTotalPrice());
    }

    @Test
    void updateQuantity_ItemNotFound_ThrowsNotFound() {
        when(cartRepository.findById("user1")).thenReturn(Optional.of(Cart.builder().userId("user1").items(new ArrayList<>()).build()));

        assertThrows(ResponseStatusException.class,
                () -> cartService.updateQuantity("user1", "p99", new UpdateQuantityRequest(1)));
    }

    @Test
    void removeItem_Success() {
        Cart existingCart = Cart.builder()
                .userId("user1")
                .items(new ArrayList<>())
                .build();
        existingCart.getItems().add(CartItem.builder()
                .productId("p1")
                .productName("Product 1")
                .unitPrice(new BigDecimal("100.00"))
                .quantity(1)
                .build());

        when(cartRepository.findById("user1")).thenReturn(Optional.of(existingCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        CartResponse response = cartService.removeItem("user1", "p1");

        assertTrue(response.getItems().isEmpty());
        assertEquals(BigDecimal.ZERO, response.getTotalPrice());
    }

    @Test
    void clearCart_CallsDelete() {
        cartService.clearCart("user1");
        verify(cartRepository).deleteById("user1");
    }
}
