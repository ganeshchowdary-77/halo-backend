package com.thehalo.halobackend.dto.payment.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddPaymentMethodRequest {

    @NotBlank(message = "Card brand is required")
    private String cardBrand;

    @NotBlank(message = "Last 4 digits are required")
    private String cardLast4;

    @NotNull(message = "Expiry month is required")
    @Min(value = 1, message = "Expiry month must be between 1 and 12")
    @Max(value = 12, message = "Expiry month must be between 1 and 12")
    private Integer expiryMonth;

    @NotNull(message = "Expiry year is required")
    private Integer expiryYear;

    private Boolean isDefault = false;

}
