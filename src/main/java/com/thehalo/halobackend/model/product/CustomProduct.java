package com.thehalo.halobackend.model.product;

import com.thehalo.halobackend.enums.QuoteStatus;
import com.thehalo.halobackend.model.base.BaseEntity;
import com.thehalo.halobackend.model.policy.Product;
import com.thehalo.halobackend.model.user.UserPlatform;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "custom_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomProduct extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_product_id")
    private Product baseProduct;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_platform_id")
    private UserPlatform userPlatform;
    
    @OneToMany(mappedBy = "customProduct", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CustomCharge> customCharges = new ArrayList<>();
    
    @Column(name = "total_custom_premium", precision = 19, scale = 2)
    private BigDecimal totalCustomPremium;
    
    @Enumerated(EnumType.STRING)
    private QuoteStatus status;
    
    @Column(name = "underwriter_notes", length = 1000)
    private String underwriterNotes;
}