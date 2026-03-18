package com.thehalo.halobackend.model.product;

import com.thehalo.halobackend.enums.ChargeType;
import com.thehalo.halobackend.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "custom_charges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomCharge extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custom_product_id")
    private CustomProduct customProduct;
    
    @Enumerated(EnumType.STRING)
    private ChargeType chargeType;
    
    @Column(name = "charge_amount", precision = 19, scale = 2)
    private BigDecimal chargeAmount;
    
    @Column(name = "description", length = 500)
    private String description;
}