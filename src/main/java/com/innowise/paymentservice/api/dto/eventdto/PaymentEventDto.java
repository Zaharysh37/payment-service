package com.innowise.paymentservice.api.dto.eventdto;

import com.innowise.paymentservice.core.entity.PaymentStatus;

public record PaymentEventDto(
    String paymentId,
    Long orderId,
    Long userId,
    PaymentStatus status
) {}
