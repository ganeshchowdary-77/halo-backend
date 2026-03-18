package com.thehalo.halobackend.dto.payment.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodResponse {

    private Long id;
    private String cardBrand;
    private String cardLast4;
    private Integer expiryMonth;
    private Integer expiryYear;
    private Boolean isDefault;

}
