package com.ecommerce.order.client;

import com.ecommerce.order.client.dto.ProductClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "product-service", path = "/api/products")
public interface ProductClient {

    @GetMapping("/{id}")
    ProductClientResponse getProductById(@PathVariable("id") String id);

    /**
     * Valida stock y lo descuenta en una sola operación atómica.
     * La lógica de validación de stock pertenece al dominio de product-service.
     * Lanza FeignException (400) si el stock es insuficiente o el producto no está activo.
     */
    @PostMapping("/{id}/reserve")
    void reserveStock(@PathVariable("id") String id, @RequestParam("quantity") int quantity);

    /**
     * Restaura stock (usado al cancelar una orden).
     * Ajuste positivo de cantidad.
     */
    @org.springframework.web.bind.annotation.PatchMapping("/{id}/stock")
    void restoreStock(@PathVariable("id") String id, @RequestParam("quantity") int quantity);
}
