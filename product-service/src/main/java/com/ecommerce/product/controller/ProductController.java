package com.ecommerce.product.controller;

import com.ecommerce.product.domain.Category;
import com.ecommerce.product.dto.CreateProductRequest;
import com.ecommerce.product.dto.PagedResponse;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.dto.UpdateProductRequest;
import com.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // POST /api/products
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    // GET /api/products/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    // GET /api/products/sku/{sku}
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponse> findBySku(@PathVariable String sku) {
        return ResponseEntity.ok(productService.findBySku(sku));
    }

    // GET /api/products?page=0&size=10
    @GetMapping
    public ResponseEntity<PagedResponse<ProductResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.findAll(page, size));
    }

    // GET /api/products/category/{category}?page=0&size=10
    @GetMapping("/category/{category}")
    public ResponseEntity<PagedResponse<ProductResponse>> findByCategory(
            @PathVariable Category category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.findByCategory(category, page, size));
    }

    // GET /api/products/search?name=notebook&page=0&size=10
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<ProductResponse>> search(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.search(name, page, size));
    }

    // GET /api/products/category/{category}/price-range?min=100&max=500
    @GetMapping("/category/{category}/price-range")
    public ResponseEntity<PagedResponse<ProductResponse>> findByPriceRange(
            @PathVariable Category category,
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                productService.findByCategoryAndPriceRange(category, min, max, page, size));
    }

    // PATCH /api/products/{id}
    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    // POST /api/products/{id}/reserve?quantity=2
    // Called when checking out order — validates and decrements in single operation
    @PostMapping("/{id}/reserve")
    public ResponseEntity<Void> reserveStock(
            @PathVariable String id,
            @RequestParam int quantity) {
        productService.reserveStock(id, quantity);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/products/{id}/stock?quantity=-5  (negative = decrease, positive = increase) - admin usage
    @PatchMapping("/{id}/stock")
    public ResponseEntity<Void> updateStock(
            @PathVariable String id,
            @RequestParam int quantity) {
        productService.updateStock(id, quantity);
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/products/{id}  → soft delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
