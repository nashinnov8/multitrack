package org.nashinnov8.multitrack.common.dto;

import org.springframework.data.domain.Page;
import java.util.List;

public record PaginatedResponse<T>(
    List<T> content,
    int pageNumber,
    int pageSize,
    long totalElements,
    int totalPages,
    boolean isLast
) {
    /**
     * Helper method to map a Spring Data Page object to a PaginatedResponse.
     * Use this when you don't need to convert the inner objects.
     */
    public static <T> PaginatedResponse<T> from(Page<T> page) {
        return new PaginatedResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast()
        );
    }
}
