package com.thehalo.halobackend.repository;

import com.thehalo.halobackend.model.policy.Product;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByNameIgnoreCase(@NotBlank String name);
    List<Product> findAllByActiveTrue();
    Page<Product> findAllByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Product> findAllByActive(Boolean active, Pageable pageable);
}
