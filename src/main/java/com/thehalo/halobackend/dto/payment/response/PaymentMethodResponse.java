package com.thehalo.halobackend.dto.payment.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentMethodResponse {

    private Long id;
    private String cardBrand;
    private String cardLast4;
    private Integer expiryMonth;
    private Integer expiryYear;
    private Boolean isDefault;

}
