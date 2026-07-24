package com.ecommerce.product.repository;

import com.ecommerce.product.domain.Category;
import com.ecommerce.product.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    // Search by category with pagination
    Page<Product> findByCategoryAndActiveTrue(Category category, Pageable pageable);

    // List only active products
    Page<Product> findByActiveTrue(Pageable pageable);

    // Search by SKU
    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    // Search by name (case-insensitive) with pagination
    Page<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    // Filter by price range within a category
    @Query("{ 'category': ?0, 'price': { $gte: ?1, $lte: ?2 }, 'active': true }")
    Page<Product> findByCategoryAndPriceRange(Category category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable);
}
