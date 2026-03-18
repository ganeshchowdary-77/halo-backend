package com.thehalo.halobackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thehalo.halobackend.dto.quote.request.SubmitQuoteRequest;
import com.thehalo.halobackend.dto.quote.response.QuoteDetailResponse;
import com.thehalo.halobackend.dto.quote.response.QuoteSummaryResponse;
import com.thehalo.halobackend.service.quote.QuoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuoteController.class)
@AutoConfigureMockMvc(addFilters = false)
class QuoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QuoteService quoteService;

    @Test
    @WithMockUser(roles = "INFLUENCER")
    void getMyQuotes_ShouldReturnQuotes() throws Exception {
        when(quoteService.getMyQuotes()).thenReturn(List.of(new QuoteSummaryResponse()));

        mockMvc.perform(get("/api/v1/quotes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "INFLUENCER")
    void submitQuote_ShouldCreateQuote() throws Exception {
        SubmitQuoteRequest request = SubmitQuoteRequest.builder()
                .productId(1L)
                .profileId(1L)
                .notes("Custom coverage notes")
                .build();

        when(quoteService.submit(any(SubmitQuoteRequest.class)))
                .thenReturn(new QuoteDetailResponse());

        mockMvc.perform(post("/api/v1/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "INFLUENCER")
    void getQuote_ShouldReturnQuoteDetail() throws Exception {
        when(quoteService.getDetail(anyLong())).thenReturn(new QuoteDetailResponse());

        mockMvc.perform(get("/api/v1/quotes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
