package com.thehalo.halobackend.exception.handler;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;
import com.thehalo.halobackend.exception.response.ApiErrorResponse;
import com.thehalo.halobackend.exception.security.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiErrorResponse> handleBaseException(
            BaseException ex,
            HttpServletRequest request) {

        ErrorCode code = ex.getErrorCode();
        String correlationId = UUID.randomUUID().toString();

        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .errorCode(code.name())
                .status(code.getStatus())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .correlationId(correlationId)
                .build();

        log.warn("Business exception [{}]: {} - {}", correlationId, code.name(), ex.getMessage());

        // Add Retry-After header for rate limiting
        if (ex instanceof RateLimitExceededException) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Retry-After", String.valueOf(((RateLimitExceededException) ex).getRetryAfterSeconds()));
            return new ResponseEntity<>(response, headers, HttpStatus.valueOf(code.getStatus()));
        }

        return ResponseEntity.status(code.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String correlationId = UUID.randomUUID().toString();

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .message("Input validation failed")
                .errorCode("VALIDATION_ERROR")
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .correlationId(correlationId)
                .fieldErrors(fieldErrors)
                .build();

        log.warn("Validation error [{}]: {}", correlationId, fieldErrors);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnknown(
            Exception ex,
            HttpServletRequest request) {

        String path = request.getRequestURI();

        // 🚨 Do NOT intercept Swagger/OpenAPI endpoints
        if (path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")) {

            throw new RuntimeException(ex);
        }

        String correlationId = UUID.randomUUID().toString();

        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .message("An unexpected error occurred. Please contact support with correlation ID.")
                .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.name())
                .status(500)
                .timestamp(LocalDateTime.now())
                .path(path)
                .correlationId(correlationId)
                .build();

        log.error("Unexpected exception [{}]: ", correlationId, ex);

        return ResponseEntity.internalServerError().body(response);
    }
}
