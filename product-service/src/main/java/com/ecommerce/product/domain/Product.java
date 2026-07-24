package com.ecommerce.product.domain;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * MongoDB document which represents a product in the catalog.
 *
 * The `attributes` field is a flexible map (Map<String, Object>) that allows
 * storing different attributes depending on the product category, without the
 * need
 * for a rigid schema. For example:
 * - Clothing: { "size": "M", "color": "red", "material": "cotton" }
 * - Electronics: { "ram": "16GB", "processor": "i7", "screen": "15 inches" }
 */
@Document(collection = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    private String id;

    @NotBlank
    @TextIndexed
    private String name;

    @TextIndexed
    private String description;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 10, fraction = 2)
    private BigDecimal price;

    @NotNull
    private Category category;

    @NotNull
    @Min(0)
    private Integer stock;

    @Indexed(unique = true)
    private String sku;

    private String imageUrl;

    private Map<String, Object> attributes;

    private boolean active = true;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
