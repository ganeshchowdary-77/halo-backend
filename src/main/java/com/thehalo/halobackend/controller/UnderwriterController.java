package com.thehalo.halobackend.controller;

import com.thehalo.halobackend.dto.common.ResponseFactory;
import com.thehalo.halobackend.dto.underwriter.response.QueueItemResponse;
import com.thehalo.halobackend.service.underwriter.UnderwriterQueueService;
import com.thehalo.halobackend.service.underwriter.UnderwriterAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/underwriter")
@RequiredArgsConstructor
@Tag(name = "Underwriter", description = "Underwriter queue and assignment management")
@SecurityRequirement(name = "bearerAuth")
public class UnderwriterController {

    private final UnderwriterQueueService queueService;
    private final UnderwriterAssignmentService assignmentService;

    @GetMapping("/queue/available")
    @Operation(summary = "Get available quotes for assignment")
    @PreAuthorize("hasRole('UNDERWRITER')")
    public ResponseEntity<?> getAvailableQuotes(Pageable pageable) {
        Page<QueueItemResponse> quotes = queueService.getAvailableQuotes(pageable);
        return ResponseFactory.success(quotes, "Available quotes retrieved successfully");
    }

    @GetMapping("/queue/assigned")
    @Operation(summary = "Get quotes assigned to current underwriter")
    @PreAuthorize("hasRole('UNDERWRITER')")
    public ResponseEntity<?> getAssignedQuotes(Authentication auth, Pageable pageable) {
        Long underwriterId = getUserIdFromAuth(auth);
        Page<QueueItemResponse> quotes = queueService.getAssignedQuotes(underwriterId, pageable);
        return ResponseFactory.success(quotes, "Assigned quotes retrieved successfully");
    }

    @GetMapping("/queue/priority")
    @Operation(summary = "Get high priority quotes")
    @PreAuthorize("hasRole('UNDERWRITER')")
    public ResponseEntity<?> getHighPriorityQuotes() {
        List<QueueItemResponse> quotes = queueService.getHighPriorityQuotes();
        return ResponseFactory.success(quotes, "High priority quotes retrieved successfully");
    }

    @PostMapping("/queue/{quoteId}/claim")
    @Operation(summary = "Claim a quote for review")
    @PreAuthorize("hasRole('UNDERWRITER')")
    public ResponseEntity<?> claimQuote(@PathVariable Long quoteId, Authentication auth) {
        Long underwriterId = getUserIdFromAuth(auth);
        boolean success = queueService.claimQuote(quoteId, underwriterId);
        
        if (success) {
            return ResponseFactory.success(null, "Quote claimed successfully");
        } else {
            return ResponseFactory.error("Failed to claim quote - it may already be assigned", 409);
        }
    }

    @PostMapping("/queue/{quoteId}/release")
    @Operation(summary = "Release a quote back to the queue")
    @PreAuthorize("hasRole('UNDERWRITER')")
    public ResponseEntity<?> releaseQuote(@PathVariable Long quoteId, Authentication auth) {
        Long underwriterId = getUserIdFromAuth(auth);
        boolean success = queueService.releaseQuote(quoteId, underwriterId);
        
        if (success) {
            return ResponseFactory.success(null, "Quote released successfully");
        } else {
            return ResponseFactory.error("Failed to release quote", 400);
        }
    }

    @GetMapping("/stats")
    @Operation(summary = "Get underwriter statistics")
    @PreAuthorize("hasRole('UNDERWRITER')")
    public ResponseEntity<?> getUnderwriterStats(Authentication auth) {
        Long underwriterId = getUserIdFromAuth(auth);
        Map<String, Object> stats = queueService.getUnderwriterStats(underwriterId);
        return ResponseFactory.success(stats, "Statistics retrieved successfully");
    }

    @GetMapping("/queue/all")
    @Operation(summary = "Get all quotes (admin view)")
    @PreAuthorize("hasRole('IAM_ADMIN') or hasRole('POLICY_ADMIN')")
    public ResponseEntity<?> getAllQuotes(Pageable pageable) {
        // Implementation for admin view of all quotes
        return ResponseFactory.success(null, "All quotes retrieved successfully");
    }

    private Long getUserIdFromAuth(Authentication auth) {
        // Extract user ID from JWT token
        // This would be implemented based on your JWT structure
        return 1L; // Placeholder
    }
}