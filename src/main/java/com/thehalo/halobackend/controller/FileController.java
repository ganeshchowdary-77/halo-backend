package com.thehalo.halobackend.controller;

import com.thehalo.halobackend.dto.common.HaloApiResponse;
import com.thehalo.halobackend.dto.common.ResponseFactory;
import com.thehalo.halobackend.dto.file.response.FileUploadResponse;
import com.thehalo.halobackend.security.service.CustomUserDetails;
import com.thehalo.halobackend.service.common.FileStorageStrategy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Secure file management controller.
 *
 * SECURITY ARCHITECTURE:
 * ─────────────────────────────────────────────────────────────────────
 * 1. UPLOAD: POST /api/v1/files/upload
 *    - Requires JWT authentication (any authenticated user)
 *    - Files are stored with UUID filenames outside web root
 *    - Original filename is never used for storage (prevents traversal)
 *
 * 2. DOWNLOAD: GET /api/v1/files/download/{category}/{userId}/{fileId}
 *    - Requires JWT authentication + role check via @PreAuthorize
 *    - Returns binary stream via ResponseEntity<Resource>
 *    - Content-Disposition: inline allows browser preview (e.g., PDFs)
 *    - Files are NEVER accessible via public URL; this endpoint is the
 *      ONLY way to retrieve a file, ensuring every access is auditable
 *
 * WHY NOT <a href>?
 *    Standard HTML links bypass the Angular JWT interceptor.
 *    The frontend must use HttpClient.get with responseType: 'blob'
 *    to include the Authorization header, then createObjectURL for display.
 * ─────────────────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "File Management", description = "Secure file upload and download endpoints")
public class FileController {

    // Injects via Strategy Pattern — currently LocalFileStorageServiceImpl
    private final FileStorageStrategy fileStorageStrategy;

    /**
     * Upload a file to secure storage.
     *
     * RBAC: Any authenticated user can upload.
     * The category parameter allows logical grouping (claims, platforms, general).
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('INFLUENCER', 'UNDERWRITER', 'POLICY_ADMIN', 'CLAIMS_OFFICER', 'IAM_ADMIN')")
    @Operation(summary = "Upload a file", description = "Uploads a file to secure server-side storage")
    public ResponseEntity<HaloApiResponse<FileUploadResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "general") String category,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // Store file with UUID filename
        String fileId = fileStorageStrategy.storeFile(file, category, userDetails.getUserId());

        FileUploadResponse response = FileUploadResponse.builder()
                .fileId(fileId)
                .originalFilename(file.getOriginalFilename())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();

        log.info("User {} uploaded file: {} → {}", userDetails.getUserId(), file.getOriginalFilename(), fileId);

        return ResponseFactory.success(response, "File uploaded successfully", HttpStatus.CREATED);
    }

    /**
     * Download a file from secure storage.
     *
     * RBAC: Any authenticated user can download.
     * In a production system, you would add ownership checks here
     * (e.g., verify the requesting user owns the claim/policy the file belongs to).
     *
     * SECURITY: The fileId is a path like "claims/42/uuid.pdf".
     * Spring's ** path matching captures the full path after /download/.
     * The storage implementation validates path traversal.
     *
     * Returns raw bytes with Content-Disposition: inline for browser rendering.
     */
    @GetMapping("/download/**")
    @PreAuthorize("hasAnyRole('INFLUENCER', 'UNDERWRITER', 'POLICY_ADMIN', 'CLAIMS_OFFICER', 'IAM_ADMIN')")
    @Operation(summary = "Download a file", description = "Securely streams a file through JWT-authenticated endpoint")
    public ResponseEntity<Resource> downloadFile(
            HttpServletRequest request
    ) {
        // Extract the fileId from the full request path (everything after /download/)
        String requestUri = request.getRequestURI();
        String downloadPrefix = "/api/v1/files/download/";
        int prefixIndex = requestUri.indexOf(downloadPrefix);
        
        String fileId = "";
        if (prefixIndex != -1) {
             fileId = requestUri.substring(prefixIndex + downloadPrefix.length());
        }

        log.info("Download requested for file: {}", fileId);

        // Load the file as a Resource — throws BusinessException if not found
        Resource resource = fileStorageStrategy.loadFileAsResource(fileId);

        // Dynamically resolve content type from the actual file
        String contentType = fileStorageStrategy.resolveContentType(fileId);

        // Extract original filename for Content-Disposition header
        String filename = resource.getFilename() != null ? resource.getFilename() : "download";

        return ResponseEntity.ok()
                // SECURITY: inline disposition allows browser to render PDFs/images
                // without forcing a download. The frontend controls the UX.
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}