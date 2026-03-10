package com.thehalo.halobackend.mapper.payment;

import com.thehalo.halobackend.dto.payment.response.PaymentMethodResponse;
import com.thehalo.halobackend.dto.payment.response.TransactionResponse;
import com.thehalo.halobackend.model.payment.PaymentMethod;
import com.thehalo.halobackend.model.payment.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentMethodResponse toDto(PaymentMethod paymentMethod);

    @Mapping(source = "policy.policyNumber", target = "policyNumber")
    @Mapping(source = "transactionType", target = "type")
    @Mapping(source = "paymentMethod.cardLast4", target = "paymentMethodLast4")
    TransactionResponse toDto(Transaction transaction);
}
