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

    @Column(nullable = false)
    private Boolean coveredPR;

    @Column(nullable = false)
    private Boolean coveredMonitoring;

    // Coverage Limits
    @Column(precision = 19, scale = 2)
    private BigDecimal coverageLimitLegal;

    @Column(precision = 19, scale = 2)
    private BigDecimal coverageLimitPR;

    @Column(precision = 19, scale = 2)
    private BigDecimal coverageLimitMonitoring;

    // Premium
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal basePremium;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    // Advanced Accounting Fields
    @Column(nullable = false)
    @Builder.Default
    private Integer maturityTermMonths = 12;

    @Column(precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal latePaymentDailyInterestRate = new BigDecimal("0.0005"); // 0.05% daily

    @Column(precision = 19, scale = 2)
    private BigDecimal guaranteedMaturityBenefit;

    @Column(precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal surrenderValueMultiplier = new BigDecimal("0.50"); // 50% early withdrawal return

    // Helper method to get total coverage amount
    public BigDecimal getCoverageAmount() {
        BigDecimal total = BigDecimal.ZERO;
        if (coverageLimitLegal != null) {
            total = total.add(coverageLimitLegal);
        }
        if (coverageLimitPR != null) {
            total = total.add(coverageLimitPR);
        }
        if (coverageLimitMonitoring != null) {
            total = total.add(coverageLimitMonitoring);
        }
        return total;
    }

}