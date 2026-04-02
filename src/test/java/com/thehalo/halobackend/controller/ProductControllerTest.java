package com.thehalo.halobackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thehalo.halobackend.dto.product.request.CreateProductRequest;
import com.thehalo.halobackend.dto.product.response.ProductDetailResponse;
import com.thehalo.halobackend.dto.product.response.ProductSummaryResponse;
import com.thehalo.halobackend.dto.product.response.PublicProductResponse;
import com.thehalo.halobackend.service.product.ProductService;
import com.thehalo.halobackend.service.product.PublicProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.thehalo.halobackend.security.util.JwtUtil;
import com.thehalo.halobackend.security.service.CustomUserDetailsService;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private PublicProductService publicProductService;

    @Test
    void getPublicProducts_ShouldReturnProducts() throws Exception {
        when(publicProductService.getPublicProducts()).thenReturn(List.of(new PublicProductResponse()));

        mockMvc.perform(get("/api/v1/products/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "PRODUCT_ADMIN")
    void getAllProducts_ShouldReturnAllProducts() throws Exception {
        when(productService.getActiveSummaries()).thenReturn(List.of(new ProductSummaryResponse()));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "PRODUCT_ADMIN")
    void createProduct_ShouldCreateProduct() throws Exception {
        CreateProductRequest request = CreateProductRequest.builder()
                .name("Test Product")
                .basePremium(BigDecimal.valueOf(100))
                .coverageLimitLegal(BigDecimal.valueOf(50000))
                .build();

        when(productService.create(any(CreateProductRequest.class)))
                .thenReturn(new ProductDetailResponse());

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "PRODUCT_ADMIN")
    void getProductDetail_ShouldReturnDetail() throws Exception {
        when(productService.getDetail(anyLong())).thenReturn(new ProductDetailResponse());

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
