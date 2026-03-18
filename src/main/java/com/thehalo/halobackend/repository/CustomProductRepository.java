package com.thehalo.halobackend.repository;

import com.thehalo.halobackend.enums.QuoteStatus;
import com.thehalo.halobackend.model.product.CustomProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomProductRepository extends JpaRepository<CustomProduct, Long> {
    
    List<CustomProduct> findByStatus(QuoteStatus status);
    
    List<CustomProduct> findByUserPlatformId(Long userPlatformId);
    
    List<CustomProduct> findByUserPlatformUserId(Long userId);
}