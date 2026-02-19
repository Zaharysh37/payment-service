package com.innowise.paymentservice.service.integration;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.innowise.paymentservice.api.dto.eventdto.OrderEventDto;
import com.innowise.paymentservice.api.dto.eventdto.PaymentEventDto;
import com.innowise.paymentservice.core.dao.PaymentRepository;
import com.innowise.paymentservice.core.entity.Payment;
import com.innowise.paymentservice.core.entity.PaymentStatus;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class PaymentServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private PaymentRepository paymentRepository;

    private Consumer<String, PaymentEventDto> testConsumer;

    @BeforeEach
    void setup() {

        stubFor(WireMock.get(urlPathMatching("/integers/.*"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody("50")));

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
            kafkaContainer.getBootstrapServers(),
            "test-group",
            "true");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<PaymentEventDto> deserializer = new JsonDeserializer<>(PaymentEventDto.class);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeHeaders(false);

        DefaultKafkaConsumerFactory<String, PaymentEventDto> cf = new DefaultKafkaConsumerFactory<>(
            consumerProps,
            new StringDeserializer(),
            deserializer
        );

        testConsumer = cf.createConsumer();
        testConsumer.subscribe(Collections.singletonList("create-payment"));
    }

    @AfterEach
    void cleanup() {

        paymentRepository.deleteAll();

        if (testConsumer != null) {
            testConsumer.close();
        }
    }

    @Test
    void shouldCreatePaymentAndSendEvent_whenOrderCreatedEventReceived() {

        Long orderId = 123L;
        Long userId = 456L;
        BigDecimal amount = new BigDecimal("100.50");
        OrderEventDto orderEvent = new OrderEventDto(orderId, userId, amount);

        kafkaTemplate.send("create-order", orderEvent);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<Payment> payments = paymentRepository.findAll();
            assertThat(payments).hasSize(1);
            Payment payment = payments.get(0);
            assertThat(payment.getOrderId()).isEqualTo(orderId);
            assertThat(payment.getUserId()).isEqualTo(userId);
            assertThat(payment.getAmount()).isEqualByComparingTo(amount);
            //assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        });

        ConsumerRecord<String, PaymentEventDto> record = KafkaTestUtils.getSingleRecord(testConsumer, "create-payment", Duration.ofSeconds(10));

        PaymentEventDto paymentEvent = record.value();
        assertThat(paymentEvent).isNotNull();
        assertThat(paymentEvent.orderId()).isEqualTo(orderId);
        //assertThat(paymentEvent.status()).isEqualTo(PaymentStatus.SUCCESS);
    }
}