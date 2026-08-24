package com.ecommerce.product.service;

import com.ecommerce.product.domain.Category;
import com.ecommerce.product.domain.Product;
import com.ecommerce.product.dto.CreateProductRequest;
import com.ecommerce.product.dto.PagedResponse;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.dto.UpdateProductRequest;
import com.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final MeterRegistry meterRegistry;

    @PostConstruct
    public void initMetrics() {
        Gauge.builder("ecommerce.products.low_stock", productRepository,
                repo -> repo.countByStockLessThan(5))
                .register(meterRegistry);
    }

    @Override
    public ProductResponse create(CreateProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A product with SKU '" + request.getSku() + "' already exists");
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .stock(request.getStock())
                .sku(request.getSku())
                .imageUrl(request.getImageUrl())
                .attributes(request.getAttributes())
                .active(true)
                .build();

        return toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse findById(String id) {
        return toResponse(getProductOrThrow(id));
    }

    @Override
    public ProductResponse findBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Product with SKU '" + sku + "' not found"));
        return toResponse(product);
    }

    @Override
    public PagedResponse<ProductResponse> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> result = productRepository.findByActiveTrue(pageable);
        return toPagedResponse(result);
    }

    @Override
    public PagedResponse<ProductResponse> findByCategory(Category category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> result = productRepository.findByCategoryAndActiveTrue(category, pageable);
        return toPagedResponse(result);
    }

    @Override
    public PagedResponse<ProductResponse> search(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Product> result = productRepository
                .findByNameContainingIgnoreCaseAndActiveTrue(name, pageable);
        return toPagedResponse(result);
    }

    @Override
    public PagedResponse<ProductResponse> findByCategoryAndPriceRange(
            Category category, BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("price").ascending());
        Page<Product> result = productRepository
                .findByCategoryAndPriceRange(category, minPrice, maxPrice, pageable);
        return toPagedResponse(result);
    }

    @Override
    public ProductResponse update(String id, UpdateProductRequest request) {
        Product product = getProductOrThrow(id);

        // Only update fields present in request (PATCH pattern)
        if (request.getName() != null)
            product.setName(request.getName());
        if (request.getDescription() != null)
            product.setDescription(request.getDescription());
        if (request.getPrice() != null)
            product.setPrice(request.getPrice());
        if (request.getCategory() != null)
            product.setCategory(request.getCategory());
        if (request.getStock() != null)
            product.setStock(request.getStock());
        if (request.getImageUrl() != null)
            product.setImageUrl(request.getImageUrl());
        if (request.getAttributes() != null)
            product.setAttributes(request.getAttributes());
        if (request.getActive() != null)
            product.setActive(request.getActive());

        return toResponse(productRepository.save(product));
    }

    @Override
    public void reserveStock(String id, int quantity) {
        Product product = getProductOrThrow(id);

        if (!product.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Product '" + product.getName() + "' is no longer available");
        }
        if (product.getStock() < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Insufficient stock for product '" + product.getName()
                            + "'. Requested: " + quantity + ", Available: " + product.getStock());
        }

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }

    @Override
    public void updateStock(String id, int quantity) {
        Product product = getProductOrThrow(id);
        int newStock = product.getStock() + quantity;

        if (newStock < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Insufficient stock. Available: " + product.getStock());
        }

        product.setStock(newStock);
        productRepository.save(product);
    }

    @Override
    public void delete(String id) {
        // Soft delete: marca como inactivo en lugar de borrar el documento
        Product product = getProductOrThrow(id);
        product.setActive(false);
        productRepository.save(product);
    }

    // ---- Helpers ----

    private Product getProductOrThrow(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Product with id '" + id + "' not found"));
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .stock(product.getStock())
                .sku(product.getSku())
                .imageUrl(product.getImageUrl())
                .attributes(product.getAttributes())
                .active(product.isActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private PagedResponse<ProductResponse> toPagedResponse(Page<Product> page) {
        return PagedResponse.<ProductResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
