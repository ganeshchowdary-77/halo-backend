package com.thehalo.halobackend.dto.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

public class ResponseFactory {
    public static <T> ResponseEntity<HaloApiResponse<T>> success(
            T data,
            String message,
            HttpStatus status
    ) {

        HaloApiResponse<T> response = HaloApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .build();

        return ResponseEntity.status(status).body(response);
    }

    public static <T> ResponseEntity<HaloApiResponse<T>> success(
            T data,
            String message
    ) {
        return success(data, message, HttpStatus.OK);
    }

    public static ResponseEntity<HaloApiResponse<Void>> success(
            String message
    ) {

        HaloApiResponse<Void> response = HaloApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

    public static <T> ResponseEntity<HaloApiResponse<T>> error(
            String message,
            int statusCode
    ) {
        HaloApiResponse<T> response = HaloApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .status(statusCode)
                .build();

        return ResponseEntity.status(statusCode).body(response);
    }
}
