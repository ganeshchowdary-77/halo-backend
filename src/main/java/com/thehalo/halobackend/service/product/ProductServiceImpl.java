package com.thehalo.halobackend.service.product;

import com.thehalo.halobackend.dto.product.request.CreateProductRequest;
import com.thehalo.halobackend.dto.product.request.UpdateProductRequest;
import com.thehalo.halobackend.dto.product.response.ProductDetailResponse;
import com.thehalo.halobackend.dto.product.response.ProductSummaryResponse;
import com.thehalo.halobackend.enums.PolicyStatus;
import com.thehalo.halobackend.exception.business.DuplicateResourceException;
import com.thehalo.halobackend.exception.business.ResourceNotFoundException;
import com.thehalo.halobackend.mapper.product.ProductMapper;
import com.thehalo.halobackend.model.policy.Product;
import com.thehalo.halobackend.repository.PolicyRepository;
import com.thehalo.halobackend.repository.ProductRepository;
import com.thehalo.halobackend.service.system.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

// Handles all insurance product (plan) lifecycle management
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final PolicyRepository policyRepository;
    private final ProductMapper productMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public List<ProductSummaryResponse> getActiveSummaries() {
        return productRepository.findAllByActiveTrue()
                .stream().map(this::enrichSummary).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getDetail(Long id) {
        Product p = findOrThrow(id);
        ProductDetailResponse detail = productMapper.toDetail(p);
        detail.setActivePolicyCount(policyRepository.countByProductIdAndStatus(id, PolicyStatus.ACTIVE));
        return detail;
    }

    @Override
    @Transactional
    public ProductDetailResponse create(CreateProductRequest request) {
        if (productRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Product '" + request.getName() + "'");
        }
        Product saved = productRepository.save(productMapper.toEntity(request));
        auditLogService.logAction("PRODUCT", saved.getId().toString(), "CREATE",
                "Created new insurance product: " + saved.getName());
        return productMapper.toDetail(saved);
    }

    @Override
    @Transactional
    public ProductDetailResponse update(Long id, UpdateProductRequest request) {
        Product product = findOrThrow(id);
        if (request.getName() != null
                && !product.getName().equalsIgnoreCase(request.getName())
                && productRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Product '" + request.getName() + "'");
        }
        productMapper.updateEntity(request, product);
        Product saved = productRepository.save(product);
        auditLogService.logAction("PRODUCT", saved.getId().toString(), "UPDATE",
                "Updated insurance product: " + saved.getName());
        return productMapper.toDetail(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // Entity has @SQLDelete — this triggers soft delete
        Product product = findOrThrow(id);
        productRepository.delete(product);
        auditLogService.logAction("PRODUCT", id.toString(), "DELETE", "Soft deleted product: " + product.getName());
    }

    // Decorate product summary with human-readable feature bullets
    private ProductSummaryResponse enrichSummary(Product p) {
        ProductSummaryResponse s = productMapper.toSummary(p);
        List<String> features = new ArrayList<>();
        if (Boolean.TRUE.equals(p.getCoveredLegal()) && p.getCoverageLimitLegal() != null)
            features.add("Legal defence up to $" + p.getCoverageLimitLegal().toPlainString());
        if (Boolean.TRUE.equals(p.getCoveredPR()) && p.getCoverageLimitPR() != null)
            features.add("PR crisis cover up to $" + p.getCoverageLimitPR().toPlainString());
        if (Boolean.TRUE.equals(p.getCoveredMonitoring()) && p.getCoverageLimitMonitoring() != null)
            features.add("Reputation monitoring up to $" + p.getCoverageLimitMonitoring().toPlainString());
        s.setKeyFeatures(features);
        return s;
    }

    private Product findOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }
}