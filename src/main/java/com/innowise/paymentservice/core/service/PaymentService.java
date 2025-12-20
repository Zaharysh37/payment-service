package com.innowise.paymentservice.core.service;

import com.innowise.paymentservice.api.dto.CreatePaymentDto;
import com.innowise.paymentservice.api.dto.GetPaymentDto;
import com.innowise.paymentservice.api.dto.GetPaymentTotalResultDto;
import com.innowise.paymentservice.core.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    GetPaymentDto createPayment(CreatePaymentDto createPaymentDto);

    GetPaymentDto getPaymentById(String id);

    Page<GetPaymentDto> getAllPayments(Pageable pageable);

    Page<GetPaymentDto> getPaymentsByUserId(Long userId, Pageable pageable);

    Page<GetPaymentDto> getPaymentsByOrderId(Long orderId, Pageable pageable);

    Page<GetPaymentDto> getPaymentsByStatus(Collection<PaymentStatus> status, Pageable pageable);

    BigDecimal getPaymentTotalResult(LocalDateTime from, LocalDateTime to);

    void deletePayment(Long id);
}
