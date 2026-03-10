package com.thehalo.halobackend.model.policy;

import com.thehalo.halobackend.enums.QuoteStatus;
import com.thehalo.halobackend.model.base.BaseEntity;
import com.thehalo.halobackend.model.profile.AppUser;
import com.thehalo.halobackend.model.profile.UserProfile;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "quote_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE quote_requests SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class QuoteRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String quoteNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private UserProfile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuoteStatus status = QuoteStatus.PENDING;

    // Custom requests from influencer
    @Column(length = 2000)
    private String notes;

    // Premium offered by underwriter
    @Column(precision = 19, scale = 2)
    private BigDecimal offeredPremium;

    @Column(length = 2000)
    private String underwriterNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "underwriter_id")
    private AppUser assignedUnderwriter;

    private LocalDateTime reviewedAt;
}
