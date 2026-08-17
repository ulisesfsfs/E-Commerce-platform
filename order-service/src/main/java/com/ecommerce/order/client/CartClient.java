package com.ecommerce.order.client;

import com.ecommerce.order.client.dto.CartClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cart-service", path = "/api/carts", fallback = CartClientFallback.class)
public interface CartClient {

    @GetMapping("/{userId}")
    CartClientResponse getCart(@PathVariable("userId") String userId);

    @DeleteMapping("/{userId}")
    void clearCart(@PathVariable("userId") String userId);
}
