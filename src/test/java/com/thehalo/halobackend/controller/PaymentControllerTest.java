package com.thehalo.halobackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thehalo.halobackend.dto.payment.request.AddPaymentMethodRequest;
import com.thehalo.halobackend.dto.payment.request.ProcessPaymentRequest;
import com.thehalo.halobackend.dto.payment.response.PaymentMethodResponse;
import com.thehalo.halobackend.dto.payment.response.PaymentSummaryResponse;
import com.thehalo.halobackend.dto.payment.response.SurrenderQuoteResponse;
import com.thehalo.halobackend.dto.payment.response.TransactionResponse;
import com.thehalo.halobackend.service.payment.PaymentService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @Test
    @WithMockUser(roles = "INFLUENCER")
    void addPaymentMethod_ShouldReturnSuccess() throws Exception {
        AddPaymentMethodRequest request = new AddPaymentMethodRequest();
        when(paymentService.addPaymentMethod(any())).thenReturn(new PaymentMethodResponse());

        mockMvc.perform(post("/api/v1/payments/methods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "INFLUENCER")
    void getMyPaymentMethods_ShouldReturnList() throws Exception {
        when(paymentService.getMyPaymentMethods()).thenReturn(List.of(new PaymentMethodResponse()));

        mockMvc.perform(get("/api/v1/payments/methods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "INFLUENCER")
    void deletePaymentMethod_ShouldReturnSuccess() throws Exception {
        doNothing().when(paymentService).deletePaymentMethod(anyLong());

        mockMvc.perform(delete("/api/v1/payments/methods/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "INFLUENCER")
    void getPaymentSummary_ShouldReturnSummary() throws Exception {
        when(paymentService.getPaymentSummary(anyLong())).thenReturn(new PaymentSummaryResponse());

        mockMvc.perform(get("/api/v1/payments/policies/1/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "INFLUENCER")
    void processPremiumPayment_ShouldReturnTransaction() throws Exception {
        ProcessPaymentRequest request = new ProcessPaymentRequest();
        when(paymentService.processPremiumPayment(anyLong(), any())).thenReturn(new TransactionResponse());

        mockMvc.perform(post("/api/v1/payments/policies/1/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "INFLUENCER")
    void getMyTransactionHistory_ShouldReturnList() throws Exception {
        when(paymentService.getMyTransactionHistory()).thenReturn(List.of(new TransactionResponse()));

        mockMvc.perform(get("/api/v1/payments/ledger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "INFLUENCER")
    void getSurrenderQuote_ShouldReturnQuote() throws Exception {
        when(paymentService.getSurrenderQuote(anyLong())).thenReturn(new SurrenderQuoteResponse());

        mockMvc.perform(get("/api/v1/payments/policies/1/surrender-quote"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "INFLUENCER")
    void processSurrender_ShouldReturnTransaction() throws Exception {
        when(paymentService.processSurrender(anyLong())).thenReturn(new TransactionResponse());

        mockMvc.perform(post("/api/v1/payments/policies/1/surrender"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
