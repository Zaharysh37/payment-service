package com.innowise.paymentservice.core.service.impl;

import com.innowise.paymentservice.api.client.RandomNumberClient;
import com.innowise.paymentservice.api.dto.CreatePaymentDto;
import com.innowise.paymentservice.api.dto.GetPaymentDto;
import com.innowise.paymentservice.api.dto.GetPaymentTotalResultDto;
import com.innowise.paymentservice.api.dto.eventdto.PaymentEventDto;
import com.innowise.paymentservice.core.dao.PaymentRepository;
import com.innowise.paymentservice.core.entity.Payment;
import com.innowise.paymentservice.core.entity.PaymentStatus;
import com.innowise.paymentservice.core.mapper.eventmapper.GetPaymentEventMapper;
import com.innowise.paymentservice.core.mapper.paymentmapper.CreatePaymentMapper;
import com.innowise.paymentservice.core.mapper.paymentmapper.GetPaymentMapper;
import com.innowise.paymentservice.core.service.PaymentService;
import com.innowise.paymentservice.core.service.eventservice.PaymentProducer;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final RandomNumberClient randomNumberClient;

    private final PaymentRepository paymentRepository;

    private final GetPaymentMapper getPaymentMapper;

    private final CreatePaymentMapper createPaymentMapper;

    private final GetPaymentEventMapper getPaymentEventMapper;

    private final PaymentProducer paymentProducer;

    @Override
    @Transactional
    public GetPaymentDto createPayment(CreatePaymentDto createPaymentDto) {

        Payment payment = createPaymentMapper.toEntity(createPaymentDto);

        Integer externalApiResponse = randomNumberClient.getRandomNumber();
        if (externalApiResponse == 0) {
            payment.setStatus(PaymentStatus.PENDING);
        } else if (externalApiResponse % 2 == 0) {
            payment.setStatus(PaymentStatus.SUCCESS);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        Payment savedPayment = paymentRepository.save(payment);

        PaymentEventDto paymentEventDto = getPaymentEventMapper.toDto(payment);
        paymentProducer.sendPaymentCreatedEvent(paymentEventDto);

        return getPaymentMapper.toDto(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public GetPaymentDto getPaymentById(String id) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        return getPaymentMapper.toDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GetPaymentDto> getAllPayments(Pageable pageable) {
        Page<Payment> payments = paymentRepository.findAll(pageable);
        return payments.map(getPaymentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GetPaymentDto> getPaymentsByUserId(Long userId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByUserId(userId, pageable);
        return payments.map(getPaymentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GetPaymentDto> getPaymentsByOrderId(Long orderId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByOrderId(orderId, pageable);
        return payments.map(getPaymentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GetPaymentDto> getPaymentsByStatus(Collection<PaymentStatus> status, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findAllByStatusIn(status, pageable);
        return payments.map(getPaymentMapper::toDto);
    }

    @Override
    public BigDecimal getPaymentTotalResult(LocalDateTime from, LocalDateTime to) {

        GetPaymentTotalResultDto getPaymentTotalResultDto = paymentRepository.sumPaymentInPeriod(from, to);

        return (getPaymentTotalResultDto != null && getPaymentTotalResultDto.total() != null)
        ? getPaymentTotalResultDto.total()
            : BigDecimal.ZERO;
    }

    @Override
    @Transactional
    public void deletePayment(String id) {
        paymentRepository.deleteById(id);
    }
}
