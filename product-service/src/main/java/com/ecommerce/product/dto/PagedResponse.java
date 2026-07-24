package com.ecommerce.product.dto;

import lombok.*;

import java.util.List;

/**
 * Wrapper for paginated responses.
 * Includes page data along with pagination metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
