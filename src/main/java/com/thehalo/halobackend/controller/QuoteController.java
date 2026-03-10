package com.thehalo.halobackend.controller;

import com.thehalo.halobackend.dto.common.ApiResponse;
import com.thehalo.halobackend.dto.common.ResponseFactory;
import com.thehalo.halobackend.dto.quote.request.ReviewQuoteRequest;
import com.thehalo.halobackend.dto.quote.request.SubmitQuoteRequest;
import com.thehalo.halobackend.dto.quote.response.QuoteDetailResponse;
import com.thehalo.halobackend.dto.quote.response.QuoteSummaryResponse;
import com.thehalo.halobackend.service.quote.QuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Quotes retrieved successfully")
    public ResponseEntity<ApiResponse<List<QuoteSummaryResponse>>> getMyQuotes() {
        return ResponseFactory.success(quoteService.getMyQuotes(), "Quotes loaded");
    }

    @PostMapping
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Submit quote request", description = "Submits a new custom quotation request for a specific product.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Quote requested successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<ApiResponse<QuoteDetailResponse>> submitQuote(
            @Valid @RequestBody SubmitQuoteRequest request) {
        return ResponseFactory.success(quoteService.submit(request), "Quote requested successfully",
                HttpStatus.CREATED);
    }

    // SHARED ENDPOINT

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('INFLUENCER', 'UNDERWRITER')")
    @Operation(summary = "Get quote detail", description = "Retrieves full details of a specific quote.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Quote details retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Quote not found")
    })
    public ResponseEntity<ApiResponse<QuoteDetailResponse>> getQuote(@PathVariable Long id) {
        return ResponseFactory.success(quoteService.getDetail(id), "Quote details loaded");
    }

    // UNDERWRITER ENDPOINTS

    @GetMapping("/queue")
    @PreAuthorize("hasRole('UNDERWRITER')")
    @Operation(summary = "Get quote queue", description = "Retrieves a queue of SUBMITTED or UNDER_REVIEW quotes for underwriters.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Queue loaded successfully")
    public ResponseEntity<ApiResponse<List<QuoteSummaryResponse>>> getQueue() {
        return ResponseFactory.success(quoteService.getUnderwriterQueue(), "Underwriter queue loaded");
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('UNDERWRITER')")
    @Operation(summary = "Approve quote", description = "Approves a quote and sets the offered premium.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Quote approved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or quote already processed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Quote not found")
    })
    public ResponseEntity<ApiResponse<QuoteDetailResponse>> approve(
            @PathVariable Long id, @Valid @RequestBody ReviewQuoteRequest request) {
        return ResponseFactory.success(quoteService.reviewQuote(id, request, true),
                "Quote approved with offered premium");
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('UNDERWRITER')")
    @Operation(summary = "Reject quote", description = "Rejects a quote with written justification.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Quote rejected"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or quote already processed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Quote not found")
    })
    public ResponseEntity<ApiResponse<QuoteDetailResponse>> reject(
            @PathVariable Long id, @Valid @RequestBody ReviewQuoteRequest request) {
        return ResponseFactory.success(quoteService.reviewQuote(id, request, false), "Quote rejected");
    }
}
