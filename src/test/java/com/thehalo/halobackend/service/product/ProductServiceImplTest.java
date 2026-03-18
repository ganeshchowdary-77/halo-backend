package com.thehalo.halobackend.service.product;

import com.thehalo.halobackend.dto.product.request.CreateProductRequest;
import com.thehalo.halobackend.dto.product.request.UpdateProductRequest;
import com.thehalo.halobackend.dto.product.response.ProductDetailResponse;
import com.thehalo.halobackend.dto.product.response.ProductSummaryResponse;
import com.thehalo.halobackend.exception.domain.product.ProductNotFoundException;
import com.thehalo.halobackend.mapper.product.ProductMapper;
import com.thehalo.halobackend.model.policy.Product;
import com.thehalo.halobackend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .basePremium(BigDecimal.valueOf(100))
                .coverageLimitLegal(BigDecimal.valueOf(50000))
                .coverageLimitPR(BigDecimal.valueOf(30000))
                .coverageLimitMonitoring(BigDecimal.valueOf(20000))
                .active(true)
                .build();
    }

    @Test
    void getActiveSummaries_ShouldReturnAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(testProduct));
        when(productMapper.toSummary(testProduct)).thenReturn(new ProductSummaryResponse());

        List<ProductSummaryResponse> result = productService.getActiveSummaries();

        assertThat(result).hasSize(1);
        verify(productRepository).findAll();
    }

    @Test
    void getDetail_ShouldReturnProduct_WhenExists() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productMapper.toDetail(testProduct)).thenReturn(new ProductDetailResponse());

        ProductDetailResponse result = productService.getDetail(1L);

        assertThat(result).isNotNull();
        verify(productRepository).findById(1L);
    }

    @Test
    void getDetail_ShouldThrowException_WhenNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getDetail(1L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void create_ShouldCreateProduct_WhenValidRequest() {
        CreateProductRequest request = CreateProductRequest.builder()
                .name("New Product")
                .basePremium(BigDecimal.valueOf(150))
                .coverageLimitLegal(BigDecimal.valueOf(60000))
                .coverageLimitPR(BigDecimal.valueOf(40000))
                .coverageLimitMonitoring(BigDecimal.valueOf(25000))
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(productMapper.toDetail(testProduct)).thenReturn(new ProductDetailResponse());

        ProductDetailResponse result = productService.create(request);

        assertThat(result).isNotNull();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void update_ShouldUpdateProduct_WhenExists() {
        UpdateProductRequest request = UpdateProductRequest.builder()
                .name("Updated Product")
                .basePremium(BigDecimal.valueOf(200))
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(productMapper.toDetail(testProduct)).thenReturn(new ProductDetailResponse());

        ProductDetailResponse result = productService.update(1L, request);

        assertThat(result).isNotNull();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void update_ShouldThrowException_WhenNotFound() {
        UpdateProductRequest request = UpdateProductRequest.builder()
                .name("Updated Product")
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(1L, request))
                .isInstanceOf(ProductNotFoundException.class);
    }

    // Note: These methods don't exist in the current ProductService interface
    // Removing these tests as they're not implemented
}
