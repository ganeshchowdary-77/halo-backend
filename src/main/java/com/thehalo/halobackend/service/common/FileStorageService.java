package com.thehalo.halobackend.service.common;

import com.thehalo.halobackend.exception.business.BusinessException;
import com.thehalo.halobackend.exception.codes.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

/**
 * Unified service for handling file uploads across the application.
 * Supports categorization (e.g., platforms, claims) and unique file naming.
 */
@Service
@Slf4j
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.upload-dir:uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new BusinessException("Could not create the directory where the uploaded files will be stored.", ErrorCode.FILE_STORAGE_ERROR);
        }
    }

    /**
     * Stores a file in a specific category subdirectory.
     *
     * @param file     The file to store
     * @param category The category (e.g., "platforms", "claims")
     * @param userId   The ID of the user uploading the file (for sub-folder organization)
     * @return The relative path to the stored file
     */
    public String storeFile(MultipartFile file, String category, Long userId) {
        // Get original file name
        String originalFileName = Objects.requireNonNull(file.getOriginalFilename());
        
        try {
            // Check if name contains path traversal sequences
            if (originalFileName.contains("..") || originalFileName.contains("/") || originalFileName.contains("\\")) {
                throw new BusinessException("Sorry! Filename contains invalid path sequence " + originalFileName, ErrorCode.INVALID_FILE_NAME);
            }

            // Create target directory: uploads/{category}/{userId}/
            Path targetLocation = this.fileStorageLocation.resolve(category).resolve(String.valueOf(userId));
            Files.createDirectories(targetLocation);

            // Generate unique file name with original extension
            String extension = "";
            int i = originalFileName.lastIndexOf('.');
            if (i > 0) {
                extension = originalFileName.substring(i);
            }
            String fileName = UUID.randomUUID().toString() + extension;

            Path finalPath = targetLocation.resolve(fileName);
            Files.copy(file.getInputStream(), finalPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Stored file {} (original: {}) for user {} in category {}", fileName, originalFileName, userId, category);

            // Return relative path: category/userId/fileName
            return category + "/" + userId + "/" + fileName;

        } catch (IOException ex) {
            throw new BusinessException("Could not store file " + originalFileName + ". Please try again!", ErrorCode.FILE_STORAGE_ERROR);
        }
    }

    /**
     * Deletes a file from storage.
     * @param relativePath The relative path of the file to delete.
     */
    public void deleteFile(String relativePath) {
        try {
            Path filePath = this.fileStorageLocation.resolve(relativePath).normalize();
            Files.deleteIfExists(filePath);
            log.info("Deleted file at {}", relativePath);
        } catch (IOException ex) {
            log.error("Could not delete file at {}: {}", relativePath, ex.getMessage());
        }
    }
}
