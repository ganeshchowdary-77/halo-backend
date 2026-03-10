package com.thehalo.halobackend.dto.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

public class ResponseFactory {
    public static <T> ResponseEntity<ApiResponse<T>> success(
            T data,
            String message,
            HttpStatus status
    ) {

        ApiResponse<T> response = ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .build();

        return ResponseEntity.status(status).body(response);
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(
            T data,
            String message
    ) {
        return success(data, message, HttpStatus.OK);
    }

    public static ResponseEntity<ApiResponse<Void>> success(
            String message
    ) {

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

    public static ResponseEntity<ApiResponse<Void>> error(
            String message,
            int statusCode
    ) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .status(statusCode)
                .build();

        return ResponseEntity.status(statusCode).body(response);
    }
}
