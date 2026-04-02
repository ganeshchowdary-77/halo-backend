package com.thehalo.halobackend.service.common;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Strategy interface for file storage operations.
 *
 * Architecture: Strategy Pattern — allows swapping implementations
 * (e.g., LocalFileStorageServiceImpl → S3FileStorageServiceImpl)
 * via Spring @Profile without changing any callers.
 *
 * SECURITY: Files are stored OUTSIDE the web root with UUID-based filenames
 * to prevent directory traversal and filename guessing attacks.
 * All downloads are funneled through a secured controller endpoint
 * that enforces JWT + RBAC before streaming bytes.
 */
public interface FileStorageStrategy {

    /**
     * Stores a file in a category/user-scoped directory.
     *
     * @param file     The multipart file to store
     * @param category Logical grouping (e.g., "claims", "platforms", "general")
     * @param userId   Owner's user ID for directory isolation
     * @return The relative path (category/userId/uuid.ext) — used as the fileId for retrieval
     */
    String storeFile(MultipartFile file, String category, Long userId);

    /**
     * Loads a stored file as a Spring Resource for streaming.
     * The caller (controller) is responsible for RBAC enforcement.
     *
     * @param relativePath The relative path returned by storeFile()
     * @return A readable Resource pointing to the physical file
     * @throws com.thehalo.halobackend.exception.business.BusinessException if file not found
     */
    Resource loadFileAsResource(String relativePath);

    /**
     * Deletes a file from storage.
     *
     * @param relativePath The relative path of the file to delete
     */
    void deleteFile(String relativePath);

    /**
     * Resolves the MIME type for a stored file.
     *
     * @param relativePath The relative path of the file
     * @return The detected MIME type, defaulting to application/octet-stream
     */
    String resolveContentType(String relativePath);
}
