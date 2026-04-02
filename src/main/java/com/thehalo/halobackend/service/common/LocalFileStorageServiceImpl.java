package com.thehalo.halobackend.service.common;

import com.thehalo.halobackend.exception.business.BusinessException;
import com.thehalo.halobackend.exception.codes.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

/**
 * LOCAL implementation of the FileStorageStrategy.
 *
 * SECURITY DESIGN:
 * - Files are stored in a configurable directory OUTSIDE the web root (e.g., "./uploads").
 *   Spring Boot's static resource handler does NOT serve from this path.
 * - Filenames are replaced with UUIDs to prevent:
 *     (a) Directory traversal attacks ("../../etc/passwd")
 *     (b) Filename collisions
 *     (c) Information leakage via original filenames
 * - Files can ONLY be retrieved through the secured FileController endpoint,
 *   which enforces JWT authentication + @PreAuthorize RBAC checks.
 *
 * STRATEGY PATTERN: Annotated with @Profile("local") (currently the only active profile).
 * To swap to S3, create an S3FileStorageServiceImpl with @Profile("aws") and change
 * the active profile — zero code changes in controllers or services.
 */
@Service
@Profile("!aws") // Active by default; disabled only when "aws" profile is explicitly active
@Slf4j
public class LocalFileStorageServiceImpl implements FileStorageStrategy {

    private final Path fileStorageLocation;

    public LocalFileStorageServiceImpl(@Value("${file.upload-dir:uploads}") String uploadDir) {
        // Resolve to absolute path and normalize to prevent traversal
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("File storage initialized at: {}", this.fileStorageLocation);
        } catch (IOException ex) {
            throw new BusinessException(
                    "Could not create the directory where the uploaded files will be stored.",
                    ErrorCode.FILE_STORAGE_ERROR
            );
        }
    }

    @Override
    public String storeFile(MultipartFile file, String category, Long userId) {
        String originalFileName = Objects.requireNonNull(file.getOriginalFilename());

        // SECURITY: Reject any path traversal attempts in the original filename
        if (originalFileName.contains("..") || originalFileName.contains("/") || originalFileName.contains("\\")) {
            throw new BusinessException(
                    "Filename contains invalid path sequence: " + originalFileName,
                    ErrorCode.INVALID_FILE_NAME
            );
        }

        try {
            // Isolate files by category and user: uploads/{category}/{userId}/
            Path targetLocation = this.fileStorageLocation
                    .resolve(category)
                    .resolve(String.valueOf(userId));
            Files.createDirectories(targetLocation);

            // SECURITY: Replace original filename with UUID to prevent guessing and collisions
            String extension = "";
            int dotIndex = originalFileName.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalFileName.substring(dotIndex);
            }
            String storedFileName = UUID.randomUUID().toString() + extension;

            Path finalPath = targetLocation.resolve(storedFileName);
            Files.copy(file.getInputStream(), finalPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Stored file {} (original: {}) for user {} in category {}", 
                      storedFileName, originalFileName, userId, category);

            // Return relative path as the file identifier — this is what gets persisted in DB
            return category + "/" + userId + "/" + storedFileName;

        } catch (IOException ex) {
            throw new BusinessException(
                    "Could not store file " + originalFileName + ". Please try again!",
                    ErrorCode.FILE_STORAGE_ERROR
            );
        }
    }

    @Override
    public Resource loadFileAsResource(String relativePath) {
        try {
            // SECURITY: Normalize the resolved path and verify it stays within the storage root
            Path filePath = this.fileStorageLocation.resolve(relativePath).normalize();
            if (!filePath.startsWith(this.fileStorageLocation)) {
                throw new BusinessException(
                        "Cannot access file outside storage directory",
                        ErrorCode.INVALID_FILE_NAME
                );
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new BusinessException(
                        "File not found: " + relativePath,
                        ErrorCode.FILE_NOT_FOUND
                );
            }
        } catch (MalformedURLException ex) {
            throw new BusinessException(
                    "File not found: " + relativePath,
                    ErrorCode.FILE_NOT_FOUND
            );
        }
    }

    @Override
    public void deleteFile(String relativePath) {
        try {
            Path filePath = this.fileStorageLocation.resolve(relativePath).normalize();
            // SECURITY: Verify path stays within storage root before deleting
            if (!filePath.startsWith(this.fileStorageLocation)) {
                log.error("Attempt to delete file outside storage directory: {}", relativePath);
                return;
            }
            Files.deleteIfExists(filePath);
            log.info("Deleted file at {}", relativePath);
        } catch (IOException ex) {
            log.error("Could not delete file at {}: {}", relativePath, ex.getMessage());
        }
    }

    @Override
    public String resolveContentType(String relativePath) {
        try {
            Path filePath = this.fileStorageLocation.resolve(relativePath).normalize();
            String contentType = Files.probeContentType(filePath);
            // SECURITY: Default to binary stream if type cannot be determined
            // This prevents browsers from interpreting files as HTML (XSS vector)
            return contentType != null ? contentType : "application/octet-stream";
        } catch (IOException ex) {
            return "application/octet-stream";
        }
    }
}
