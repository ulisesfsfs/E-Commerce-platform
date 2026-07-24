package com.ecommerce.product.dto;

import com.ecommerce.product.domain.Category;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private Category category;
    private Integer stock;
    private String sku;
    private String imageUrl;
    private Map<String, Object> attributes;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
