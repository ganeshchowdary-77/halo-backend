package com.thehalo.halobackend.dto.common;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
@Builder
public class PaginationLinks {
    private String first;
    private String last;
    private String next;
    private String previous;

    public static PaginationLinks from(Page<?> page, String baseUrl) {
        int currentPage = page.getNumber();
        int totalPages = page.getTotalPages();
        int size = page.getSize();

        return PaginationLinks.builder()
            .first(buildUrl(baseUrl, 0, size))
            .last(buildUrl(baseUrl, Math.max(0, totalPages - 1), size))
            .next(page.hasNext() ? buildUrl(baseUrl, currentPage + 1, size) : null)
            .previous(page.hasPrevious() ? buildUrl(baseUrl, currentPage - 1, size) : null)
            .build();
    }

    private static String buildUrl(String baseUrl, int page, int size) {
        return String.format("%s?page=%d&size=%d", baseUrl, page, size);
    }
}
