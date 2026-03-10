package com.thehalo.halobackend.dto.claim.response;

import lombok.*;

import java.time.LocalDateTime;

// Embedded in ClaimDetailResponse — single supporting document
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimDocumentResponse {

        private Long id;
        private String fileName;
        // SCREENSHOT | LEGAL_DOCUMENT | INVOICE | NEWS_ARTICLE | OTHER
        private String documentType;
        private Long fileSizeBytes;
        private LocalDateTime uploadedAt;
}
