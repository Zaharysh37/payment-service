package com.innowise.paymentservice.core.service.eventservice;

import com.innowise.paymentservice.api.dto.CreatePaymentDto;
import com.innowise.paymentservice.api.dto.eventdto.OrderEventDto;
import com.innowise.paymentservice.core.mapper.eventmapper.CreatePaymentEventMapper;
import com.innowise.paymentservice.core.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentConsumer {

    private final PaymentService paymentService;

    private final CreatePaymentEventMapper createPaymentEventMapper;

    @KafkaListener(topics = "${kafka.topics.consumer}",
        groupId = "${spring.kafka.consumer.group-id}")
    public void handleCreateOrderEvent(OrderEventDto eventDto) {

        log.info("Received CreateOrderEvent from Kafka: {}", eventDto);

        CreatePaymentDto createPaymentDto = createPaymentEventMapper.toEntity(eventDto);

        paymentService.createPayment(createPaymentDto);
    }
}
