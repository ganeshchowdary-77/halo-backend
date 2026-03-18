package com.thehalo.halobackend.service.product;

import com.thehalo.halobackend.dto.product.response.PublicProductResponse;
import com.thehalo.halobackend.dto.product.response.ProductDetailResponse;
import com.thehalo.halobackend.exception.domain.product.ProductNotFoundException;
import com.thehalo.halobackend.mapper.product.ProductMapper;
import com.thehalo.halobackend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicProductServiceImpl implements PublicProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public List<PublicProductResponse> getPublicProducts() {
        return productRepository.findAllByActiveTrue()
                .stream()
                .map(productMapper::toPublicResponse)
                .toList();
    }

    @Override
    public ProductDetailResponse getPublicProductDetail(Long productId) {
        return productRepository.findById(productId)
                .map(productMapper::toDetail)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}