package com.thehalo.halobackend.controller;

import com.thehalo.halobackend.dto.common.HaloApiResponse;
import com.thehalo.halobackend.dto.common.ResponseFactory;
import com.thehalo.halobackend.dto.quote.request.SubmitQuoteRequest;
import com.thehalo.halobackend.dto.quote.response.QuoteDetailResponse;
import com.thehalo.halobackend.dto.quote.response.QuoteSummaryResponse;
import com.thehalo.halobackend.dto.quote.request.QuotePricingRequest;
import com.thehalo.halobackend.service.quote.QuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quotes")
@RequiredArgsConstructor
@Tag(name = "Quote Management", description = "Endpoints for influencers to request custom quotes and underwriters to review them")
public class QuoteController {

    private final QuoteService quoteService;

    // INFLUENCER ENDPOINTS

    @GetMapping
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Get my quotes", description = "Retrieves all quote requests submitted by the current influencer.")
    @ApiResponse(responseCode = "200", description = "Quotes retrieved successfully")
    public ResponseEntity<HaloApiResponse<List<QuoteSummaryResponse>>> getMyQuotes() {
        return ResponseFactory.success(quoteService.getMyQuotes(), "Quotes loaded");
    }

    @PostMapping
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Submit quote request", description = "Submits a new custom quotation request for a specific product.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Quote requested successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<HaloApiResponse<QuoteDetailResponse>> submitQuote(
            @Valid @RequestBody SubmitQuoteRequest request) {
        return ResponseFactory.success(quoteService.submit(request), "Quote requested successfully",
                HttpStatus.CREATED);
    }

    // SHARED ENDPOINT

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Get quote detail", description = "Retrieves full details of a specific quote.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quote details retrieved"),
            @ApiResponse(responseCode = "404", description = "Quote not found")
    })
    public ResponseEntity<HaloApiResponse<QuoteDetailResponse>> getQuote(@PathVariable Long id) {
        return ResponseFactory.success(quoteService.getDetail(id), "Quote details loaded");
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Accept calculated quote", description = "User accepts the calculated premium and submits for approval.")
    @ApiResponse(responseCode = "200", description = "Quote accepted successfully")
    public ResponseEntity<HaloApiResponse<QuoteDetailResponse>> acceptQuote(@PathVariable Long id) {
        return ResponseFactory.success(quoteService.acceptQuote(id), "Quote accepted successfully");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Update quote status", description = "Allows influencer to reject an approved quote.")
    public ResponseEntity<HaloApiResponse<QuoteDetailResponse>> updateQuoteStatus(
            @PathVariable Long id,
            @Valid @RequestBody com.thehalo.halobackend.dto.quote.request.QuoteStatusUpdateRequest request) {
        return ResponseFactory.success(quoteService.updateStatus(id, request), "Quote updated successfully");
    }
}
