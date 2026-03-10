package com.thehalo.halobackend.model.claim;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Supporting evidence uploaded by the influencer when filing or supplementing a
 * claim.
 * Examples: screenshots, lawyer correspondence PDFs, news articles.
 */
@Entity
@Table(name = "claim_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    /** Human-readable file name, e.g. "lawyer_advice.pdf" */
    @Column(nullable = false)
    private String fileName;

    /** Server-side storage path / key (S3 key or local path in dev) */
    @Column(nullable = false)
    private String filePath;

    /**
     * Category: SCREENSHOT, LEGAL_DOCUMENT, INVOICE, NEWS_ARTICLE, OTHER
     */
    @Column(nullable = false)
    private String documentType;

    /** File size in bytes for UI display */
    private Long fileSizeBytes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
}
