package com.thehalo.halobackend.service.product;

import com.thehalo.halobackend.dto.product.request.CreateProductRequest;
import com.thehalo.halobackend.dto.product.request.UpdateProductRequest;
import com.thehalo.halobackend.dto.product.response.ProductDetailResponse;
import com.thehalo.halobackend.dto.product.response.ProductSummaryResponse;
import com.thehalo.halobackend.enums.PolicyStatus;
import com.thehalo.halobackend.exception.business.DuplicateResourceException;
import com.thehalo.halobackend.exception.domain.product.ProductNotFoundException;
import com.thehalo.halobackend.mapper.product.ProductMapper;
import com.thehalo.halobackend.model.policy.Product;
import com.thehalo.halobackend.repository.PolicyRepository;
import com.thehalo.halobackend.repository.ProductRepository;
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

    @Override
    public List<ProductSummaryResponse> getActiveSummaries() {
        return productRepository.findAllByActiveTrue()
                .stream().map(this::enrichSummary).toList();
    }

    @Override
    public ProductDetailResponse getDetail(Long id) {
        Product p = findOrThrow(id);
        ProductDetailResponse detail = productMapper.toDetail(p);
        detail.setActivePolicyCount(policyRepository.countByProductIdAndStatus(id, PolicyStatus.ACTIVE));
        detail.setKeyFeatures(generateFeatures(p));
        return detail;
    }

    @Override
    @Transactional
    public ProductDetailResponse create(CreateProductRequest request) {
        if (productRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Product '" + request.getName() + "'");
        }
        Product saved = productRepository.save(productMapper.toEntity(request));
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
        return productMapper.toDetail(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // Entity has @SQLDelete — this triggers soft delete
        Product product = findOrThrow(id);
        productRepository.delete(product);
    }

    // Decorate product summary with human-readable feature bullets
    private ProductSummaryResponse enrichSummary(Product p) {
        ProductSummaryResponse s = productMapper.toSummary(p);
        s.setKeyFeatures(generateFeatures(p));
        return s;
    }

    private List<String> generateFeatures(Product p) {
        List<String> features = new ArrayList<>();
        if (Boolean.TRUE.equals(p.getCoveredLegal()) && p.getCoverageLimitLegal() != null)
            features.add("Legal defence up to $" + p.getCoverageLimitLegal().toPlainString());
        if (Boolean.TRUE.equals(p.getCoveredPR()) && p.getCoverageLimitPR() != null)
            features.add("PR crisis cover up to $" + p.getCoverageLimitPR().toPlainString());
        if (Boolean.TRUE.equals(p.getCoveredMonitoring()) && p.getCoverageLimitMonitoring() != null)
            features.add("Reputation monitoring up to $" + p.getCoverageLimitMonitoring().toPlainString());
        return features;
    }

    private Product findOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
