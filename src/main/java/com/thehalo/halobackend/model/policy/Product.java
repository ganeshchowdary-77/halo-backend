package com.thehalo.halobackend.model.policy;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import com.thehalo.halobackend.model.base.BaseEntity;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE products SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Short tagline shown on pricing card e.g. "Protect your brand, protect your
    // future"
    @Column(length = 200)
    private String tagline;

    @Column(length = 2000)
    private String description;

    // Coverage Flags
    @Column(nullable = false)
    private Boolean coveredLegal;

    @Column(name = "covered_reputation", nullable = false)
    private Boolean coveredReputation;

    @Column(name = "covered_cyber", nullable = false)
    private Boolean coveredCyber;

    // Coverage Limits
    @Column(precision = 19, scale = 2)
    private BigDecimal coverageLimitLegal;

    @Column(name = "coverage_limit_reputation", precision = 19, scale = 2)
    private BigDecimal coverageLimitReputation;

    @Column(name = "coverage_limit_cyber", precision = 19, scale = 2)
    private BigDecimal coverageLimitCyber;

    // Premium
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal basePremium;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    // Helper method to get total coverage amount
    public BigDecimal getCoverageAmount() {
        BigDecimal total = BigDecimal.ZERO;
        if (coverageLimitLegal != null) {
            total = total.add(coverageLimitLegal);
        }
        if (coverageLimitReputation != null) {
            total = total.add(coverageLimitReputation);
        }
        if (coverageLimitCyber != null) {
            total = total.add(coverageLimitCyber);
        }
        return total;
    }

}