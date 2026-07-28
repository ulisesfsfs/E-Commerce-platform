package com.ecommerce.cart.service;

import com.ecommerce.cart.domain.Cart;
import com.ecommerce.cart.domain.CartItem;
import com.ecommerce.cart.dto.AddToCartRequest;
import com.ecommerce.cart.dto.CartItemResponse;
import com.ecommerce.cart.dto.CartResponse;
import com.ecommerce.cart.dto.UpdateQuantityRequest;
import com.ecommerce.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;

    @Value("${cart.ttl-seconds:604800}")
    private Long ttlSeconds;

    @Override
    public CartResponse getCart(String userId) {
        Cart cart = getOrCreateCart(userId);
        return toResponse(cart);
    }

    @Override
    public CartResponse addItem(String userId, AddToCartRequest request) {
        Cart cart = getOrCreateCart(userId);

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            item.setUnitPrice(request.getUnitPrice()); // actualizar precio por si cambió
        } else {
            CartItem newItem = CartItem.builder()
                    .productId(request.getProductId())
                    .productName(request.getProductName())
                    .unitPrice(request.getUnitPrice())
                    .quantity(request.getQuantity())
                    .imageUrl(request.getImageUrl())
                    .build();
            cart.getItems().add(newItem);
        }

        cart.setTimeToLive(ttlSeconds);
        Cart savedCart = cartRepository.save(cart);
        return toResponse(savedCart);
    }

    @Override
    public CartResponse updateQuantity(String userId, String productId, UpdateQuantityRequest request) {
        Cart cart = getOrCreateCart(userId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Product with id '" + productId + "' not found in cart"));

        item.setQuantity(request.getQuantity());
        cart.setTimeToLive(ttlSeconds);
        Cart savedCart = cartRepository.save(cart);
        return toResponse(savedCart);
    }

    @Override
    public CartResponse removeItem(String userId, String productId) {
        Cart cart = getOrCreateCart(userId);

        boolean removed = cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Product with id '" + productId + "' not found in cart");
        }

        cart.setTimeToLive(ttlSeconds);
        Cart savedCart = cartRepository.save(cart);
        return toResponse(savedCart);
    }

    @Override
    public void clearCart(String userId) {
        cartRepository.deleteById(userId);
    }

    // ---- Helpers ----

    private Cart getOrCreateCart(String userId) {
        return cartRepository.findById(userId)
                .orElseGet(() -> Cart.builder()
                        .userId(userId)
                        .items(new ArrayList<>())
                        .timeToLive(ttlSeconds)
                        .build());
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> CartItemResponse.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .imageUrl(item.getImageUrl())
                        .subtotal(item.getSubtotal())
                        .build())
                .toList();

        int totalItems = cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        return CartResponse.builder()
                .userId(cart.getUserId())
                .items(itemResponses)
                .totalPrice(cart.getTotalPrice())
                .totalItems(totalItems)
                .build();
    }
}
