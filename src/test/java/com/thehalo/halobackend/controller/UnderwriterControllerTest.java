package com.thehalo.halobackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thehalo.halobackend.enums.QuoteStatus;
import com.thehalo.halobackend.model.policy.QuoteRequest;
import com.thehalo.halobackend.repository.QuoteRequestRepository;
import com.thehalo.halobackend.repository.RiskParameterRepository;
import com.thehalo.halobackend.service.quote.QuotePricingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UnderwriterController.class)
@AutoConfigureMockMvc(addFilters = false)
class UnderwriterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RiskParameterRepository riskParameterRepository;

    @MockBean
    private QuoteRequestRepository quoteRequestRepository;

    @MockBean
    private QuotePricingService quotePricingService;

    @Test
    @WithMockUser(roles = "UNDERWRITER")
    void getPendingQuotes_ShouldReturnPage() throws Exception {
        when(quoteRequestRepository.findBySearchAndStatus(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/underwriter/quotes/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "UNDERWRITER")
    void assignQuote_ShouldReturnSuccess() throws Exception {
        QuoteRequest quote = QuoteRequest.builder().id(1L).build();
        when(quoteRequestRepository.findById(anyLong())).thenReturn(Optional.of(quote));

        mockMvc.perform(post("/api/v1/underwriter/quotes/1/assign"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "UNDERWRITER")
    void claimQuote_ShouldReturnSuccess() throws Exception {
        QuoteRequest quote = QuoteRequest.builder().id(1L).status(QuoteStatus.PENDING).build();
        when(quoteRequestRepository.findById(anyLong())).thenReturn(Optional.of(quote));
        when(quoteRequestRepository.save(any())).thenReturn(quote);

        mockMvc.perform(post("/api/v1/underwriter/quotes/1/claim"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "UNDERWRITER")
    void releaseQuote_ShouldReturnSuccess() throws Exception {
        QuoteRequest quote = QuoteRequest.builder().id(1L).status(QuoteStatus.PENDING).build(); // Changed from UNDER_REVIEW to PENDING
        when(quoteRequestRepository.findById(anyLong())).thenReturn(Optional.of(quote));
        when(quoteRequestRepository.save(any())).thenReturn(quote);

        mockMvc.perform(post("/api/v1/underwriter/quotes/1/release"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "UNDERWRITER")
    void getAssignedQuotes_ShouldReturnPage() throws Exception {
        when(quoteRequestRepository.findByStatus(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/underwriter/quotes/assigned"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "UNDERWRITER")
    void getPriorityQueue_ShouldReturnList() throws Exception {
        when(quoteRequestRepository.findByStatus(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/underwriter/queue/priority"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "UNDERWRITER")
    void getQueueStats_ShouldReturnStats() throws Exception {
        when(quoteRequestRepository.countByStatus(any())).thenReturn(5L);
        when(quoteRequestRepository.countByStatusAndCreatedAtAfter(any(), any())).thenReturn(2L);

        mockMvc.perform(get("/api/v1/underwriter/queue/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "UNDERWRITER")
    void approveQuote_ShouldReturnSuccess() throws Exception {
        QuoteRequest quote = QuoteRequest.builder().id(1L).build();
        when(quoteRequestRepository.findById(anyLong())).thenReturn(Optional.of(quote));
        when(quoteRequestRepository.save(any())).thenReturn(quote);

        UnderwriterController.ApproveQuoteRequest request = new UnderwriterController.ApproveQuoteRequest();

        mockMvc.perform(post("/api/v1/underwriter/quotes/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "UNDERWRITER")
    void rejectQuote_ShouldReturnSuccess() throws Exception {
        QuoteRequest quote = QuoteRequest.builder().id(1L).build();
        when(quoteRequestRepository.findById(anyLong())).thenReturn(Optional.of(quote));
        when(quoteRequestRepository.save(any())).thenReturn(quote);

        UnderwriterController.RejectQuoteRequest request = new UnderwriterController.RejectQuoteRequest();
        request.setReason("Test reason");

        mockMvc.perform(post("/api/v1/underwriter/quotes/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "UNDERWRITER")
    void getAllQuotes_ShouldReturnPage() throws Exception {
        when(quoteRequestRepository.findBySearchAndStatus(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/underwriter/quotes/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
