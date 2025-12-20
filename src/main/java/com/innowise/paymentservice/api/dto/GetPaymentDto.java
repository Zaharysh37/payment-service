package com.innowise.paymentservice.api.dto;

import com.innowise.paymentservice.core.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record GetPaymentDto (
    String id,
    Long orderId,
    Long userId,
    PaymentStatus status,
    LocalDateTime creationDate,
    BigDecimal amount
) { }
