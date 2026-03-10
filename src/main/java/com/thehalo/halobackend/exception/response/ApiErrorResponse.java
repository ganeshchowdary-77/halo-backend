package com.thehalo.halobackend.exception.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class ApiErrorResponse {

    private boolean success;
    private String message;
    private String errorCode;
    private int status;
    private LocalDateTime timestamp;
    private String path;
    private String correlationId;
    private List<FieldErrorResponse> errors;
    private Map<String, String> fieldErrors;
}
