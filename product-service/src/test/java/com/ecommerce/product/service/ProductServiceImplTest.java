package com.ecommerce.product.service;

import com.ecommerce.product.domain.Category;
import com.ecommerce.product.domain.Product;
import com.ecommerce.product.dto.CreateProductRequest;
import com.ecommerce.product.dto.PagedResponse;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.dto.UpdateProductRequest;
import com.ecommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;

    private MeterRegistry meterRegistry = new SimpleMeterRegistry();
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productRepository, mongoTemplate, meterRegistry);
    }

    private Product buildSampleProduct() {
        return Product.builder()
                .id("abc123")
                .name("Notebook Pro")
                .description("High performance laptop")
                .price(new BigDecimal("350000.00"))
                .category(Category.ELECTRONICS)
                .stock(20)
                .sku("NB-PRO-001")
                .active(true)
                .attributes(Map.of("ram", "16GB", "storage", "512GB SSD"))
                .build();
    }

    // --- CREATE ---

    @Test
    void create_Success() {
        CreateProductRequest request = CreateProductRequest.builder()
                .name("Notebook Pro")
                .price(new BigDecimal("350000.00"))
                .category(Category.ELECTRONICS)
                .stock(20)
                .sku("NB-PRO-001")
                .attributes(Map.of("ram", "16GB"))
                .build();

        when(productRepository.existsBySku("NB-PRO-001")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(buildSampleProduct());

        ProductResponse response = productService.create(request);

        assertNotNull(response);
        assertEquals("abc123", response.getId());
        assertEquals("NB-PRO-001", response.getSku());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void create_DuplicateSku_ThrowsBadRequest() {
        CreateProductRequest request = CreateProductRequest.builder()
                .name("Notebook Pro")
                .price(new BigDecimal("350000.00"))
                .category(Category.ELECTRONICS)
                .stock(5)
                .sku("NB-PRO-001")
                .build();

        when(productRepository.existsBySku("NB-PRO-001")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> productService.create(request));
        verify(productRepository, never()).save(any());
    }

    // --- FIND ---

    @Test
    void findById_Success() {
        when(productRepository.findById("abc123")).thenReturn(Optional.of(buildSampleProduct()));

        ProductResponse response = productService.findById("abc123");

        assertEquals("abc123", response.getId());
        assertEquals("Notebook Pro", response.getName());
    }

    @Test
    void findById_NotFound_ThrowsNotFound() {
        when(productRepository.findById("nope")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> productService.findById("nope"));
    }

    @Test
    void findAll_ReturnsPaged() {
        Page<Product> page = new PageImpl<>(List.of(buildSampleProduct()),
                PageRequest.of(0, 10), 1);
        when(productRepository.findByActiveTrue(any(Pageable.class))).thenReturn(page);

        PagedResponse<ProductResponse> result = productService.findAll(0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
    }

    // --- UPDATE ---

    @Test
    void update_PatchesOnlyProvidedFields() {
        Product existing = buildSampleProduct();
        when(productRepository.findById("abc123")).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateProductRequest request = UpdateProductRequest.builder()
                .price(new BigDecimal("299000.00"))
                .build();

        ProductResponse response = productService.update("abc123", request);

        assertEquals(new BigDecimal("299000.00"), response.getPrice());
        assertEquals("Notebook Pro", response.getName()); // name unchanged
    }

    // --- STOCK ---

    @Test
    void updateStock_Increase() {
        Product product = buildSampleProduct(); // stock = 20
        when(productRepository.findById("abc123")).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        productService.updateStock("abc123", 5); // add 5

        assertEquals(25, product.getStock());
    }

    @Test
    void updateStock_InsufficientStock_ThrowsBadRequest() {
        Product product = buildSampleProduct(); // stock = 20
        when(productRepository.findById("abc123")).thenReturn(Optional.of(product));

        // Attempt to reduce more than available stock
        assertThrows(ResponseStatusException.class,
                () -> productService.updateStock("abc123", -100));
    }

    // --- DELETE ---

    @Test
    void delete_SoftDelete() {
        Product product = buildSampleProduct();
        when(productRepository.findById("abc123")).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        productService.delete("abc123");

        assertFalse(product.isActive()); // must be deactivated
        verify(productRepository).save(product);
    }
}
