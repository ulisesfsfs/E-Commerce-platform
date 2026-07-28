package com.ecommerce.cart.service;

import com.ecommerce.cart.dto.AddToCartRequest;
import com.ecommerce.cart.dto.CartResponse;
import com.ecommerce.cart.dto.UpdateQuantityRequest;

public interface CartService {

    CartResponse getCart(String userId);

    CartResponse addItem(String userId, AddToCartRequest request);

    CartResponse updateQuantity(String userId, String productId, UpdateQuantityRequest request);

    CartResponse removeItem(String userId, String productId);

    void clearCart(String userId);
}
