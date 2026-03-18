package com.thehalo.halobackend.dto.common;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class HaloApiResponse <T>{
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private int status;
}
