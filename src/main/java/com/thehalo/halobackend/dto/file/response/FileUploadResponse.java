package com.thehalo.halobackend.dto.file.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO returned after a successful file upload.
 * Contains the fileId (relative path) needed for subsequent download requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {

    /** The UUID-based relative path serving as the unique file identifier */
    private String fileId;

    /** Original filename preserved for display purposes */
    private String originalFilename;

    /** File size in bytes */
    private long fileSize;

    /** Detected MIME type */
    private String contentType;
}
