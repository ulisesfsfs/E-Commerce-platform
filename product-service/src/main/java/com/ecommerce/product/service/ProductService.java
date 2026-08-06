package com.ecommerce.product.service;

import com.ecommerce.product.domain.Category;
import com.ecommerce.product.dto.CreateProductRequest;
import com.ecommerce.product.dto.PagedResponse;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.dto.UpdateProductRequest;

import java.math.BigDecimal;

public interface ProductService {

    ProductResponse create(CreateProductRequest request);

    ProductResponse findById(String id);

    ProductResponse findBySku(String sku);

    PagedResponse<ProductResponse> findAll(int page, int size);

    PagedResponse<ProductResponse> findByCategory(Category category, int page, int size);

    PagedResponse<ProductResponse> search(String name, int page, int size);

    PagedResponse<ProductResponse> findByCategoryAndPriceRange(
            Category category, BigDecimal minPrice, BigDecimal maxPrice, int page, int size);

    ProductResponse update(String id, UpdateProductRequest request);

    /**
     * Valida que haya stock suficiente y lo descuenta atomicamente.
     * Lanza 400 si el stock es insuficiente.
     * Solo debe llamarlo el order-service al hacer checkout.
     */
    void reserveStock(String id, int quantity);

    void updateStock(String id, int quantity);  // ajuste manual de stock (admin)

    void delete(String id);  // soft delete (active = false)
}
