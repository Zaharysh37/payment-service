package com.innowise.paymentservice.service.unit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.innowise.paymentservice.api.client.RandomNumberClient;
import com.innowise.paymentservice.api.dto.CreatePaymentDto;
import com.innowise.paymentservice.api.dto.GetPaymentDto;
import com.innowise.paymentservice.api.dto.GetPaymentTotalResultDto;
import com.innowise.paymentservice.api.dto.eventdto.PaymentEventDto;
import com.innowise.paymentservice.core.dao.PaymentRepository;
import com.innowise.paymentservice.core.entity.Payment;
import com.innowise.paymentservice.core.entity.PaymentStatus;
import com.innowise.paymentservice.core.mapper.eventmapper.GetPaymentEventMapper;
import com.innowise.paymentservice.core.mapper.eventmapper.GetPaymentEventMapperImpl;
import com.innowise.paymentservice.core.mapper.paymentmapper.CreatePaymentMapper;
import com.innowise.paymentservice.core.mapper.paymentmapper.CreatePaymentMapperImpl;
import com.innowise.paymentservice.core.mapper.paymentmapper.GetPaymentMapper;
import com.innowise.paymentservice.core.mapper.paymentmapper.GetPaymentMapperImpl;
import com.innowise.paymentservice.core.security.SecurityHelper;
import com.innowise.paymentservice.core.service.eventservice.PaymentProducer;
import com.innowise.paymentservice.core.service.impl.PaymentServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private RandomNumberClient randomNumberClient;

    @Mock
    private PaymentRepository paymentRepository;

    @Spy
    private GetPaymentMapper getPaymentMapper = new GetPaymentMapperImpl();

    @Spy
    private CreatePaymentMapper createPaymentMapper = new CreatePaymentMapperImpl();

    @Spy
    private GetPaymentEventMapper getPaymentEventMapper = new GetPaymentEventMapperImpl();

    @Mock
    private PaymentProducer paymentProducer;

    @Mock
    private SecurityHelper securityHelper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void test_createPayment_evenExternalApiResponse() {

        Integer EVEN_EXTERNAL_API_RESPONSE = 2;
        PaymentStatus EXPECTED_PAYMENT_STATUS = PaymentStatus.SUCCESS;

        CreatePaymentDto createPaymentDto = new CreatePaymentDto(
            1L, 1L, new BigDecimal(1000));

        Payment payment = new Payment(
            null,
            createPaymentDto.userId(), createPaymentDto.orderId(), null,
            LocalDateTime.now(), createPaymentDto.amount());

        when(createPaymentMapper.toEntity(createPaymentDto)).thenReturn(payment);

        when(randomNumberClient.getRandomNumber()).thenReturn(EVEN_EXTERNAL_API_RESPONSE);

        when(paymentRepository.save(any(Payment.class))).thenAnswer(
            invocation -> {
                Payment paymentLocal = invocation.getArgument(0);
                paymentLocal.setId("694a6081723088150e7cf74c");
                return paymentLocal;
            }
        );

        when(getPaymentEventMapper.toDto(any(Payment.class))).thenAnswer(
            invocation -> {
                Payment paymentLocal = invocation.getArgument(0);
                return new PaymentEventDto(
                    paymentLocal.getId(),
                    paymentLocal.getOrderId(),
                    paymentLocal.getUserId(),
                    paymentLocal.getStatus()
                    );
            }
        );
        doNothing().when(paymentProducer).sendPaymentCreatedEvent(any(PaymentEventDto.class));

        when(getPaymentMapper.toDto(any(Payment.class))).thenAnswer(
            invocation -> {
                Payment paymentLocal = invocation.getArgument(0);
                return new GetPaymentDto(
                    paymentLocal.getId(),
                    paymentLocal.getOrderId(),
                    paymentLocal.getUserId(),
                    paymentLocal.getStatus(),
                    paymentLocal.getCreationDate(),
                    paymentLocal.getAmount()
                );
            }
        );

        GetPaymentDto result = paymentService.createPayment(createPaymentDto);

        assertThat(result.status()).isEqualTo(EXPECTED_PAYMENT_STATUS);

        verify(paymentProducer, times(1)).sendPaymentCreatedEvent(any(PaymentEventDto.class));
    }

    @Test
    void test_createPayment_oddExternalApiResponse() {

        Integer ODD_EXTERNAL_API_RESPONSE = 1;
        PaymentStatus EXPECTED_PAYMENT_STATUS = PaymentStatus.FAILED;

        CreatePaymentDto createPaymentDto = new CreatePaymentDto(
            1L, 1L, new BigDecimal(1000));

        Payment payment = new Payment(
            null,
            createPaymentDto.userId(), createPaymentDto.orderId(), null,
            LocalDateTime.now(), createPaymentDto.amount());

        when(createPaymentMapper.toEntity(createPaymentDto)).thenReturn(payment);

        when(randomNumberClient.getRandomNumber()).thenReturn(ODD_EXTERNAL_API_RESPONSE);

        when(paymentRepository.save(any(Payment.class))).thenAnswer(
            invocation -> {
                Payment paymentLocal = invocation.getArgument(0);
                paymentLocal.setId("694a6081723088150e7cf74c");
                return paymentLocal;
            }
        );

        doNothing().when(paymentProducer).sendPaymentCreatedEvent(any(PaymentEventDto.class));

        GetPaymentDto result = paymentService.createPayment(createPaymentDto);

        assertThat(result.status()).isEqualTo(EXPECTED_PAYMENT_STATUS);

        verify(getPaymentEventMapper, times(1)).toDto(any(Payment.class));
        verify(getPaymentMapper, times(1)).toDto(any(Payment.class));
        verify(paymentProducer, times(1)).sendPaymentCreatedEvent(any(PaymentEventDto.class));
    }

    @Test
    void test_createPayment_noExternalApiResponse() {

        Integer NO_EXTERNAL_API_RESPONSE = 0;
        PaymentStatus EXPECTED_PAYMENT_STATUS = PaymentStatus.PENDING;

        CreatePaymentDto createPaymentDto = new CreatePaymentDto(
            1L, 1L, new BigDecimal(1000));

        Payment payment = new Payment(
            null,
            createPaymentDto.userId(), createPaymentDto.orderId(), null,
            LocalDateTime.now(), createPaymentDto.amount());

        when(createPaymentMapper.toEntity(createPaymentDto)).thenReturn(payment);

        when(randomNumberClient.getRandomNumber()).thenReturn(NO_EXTERNAL_API_RESPONSE);

        when(paymentRepository.save(any(Payment.class))).thenAnswer(
            invocation -> {
                Payment paymentLocal = invocation.getArgument(0);
                paymentLocal.setId("694a6081723088150e7cf74c");
                return paymentLocal;
            }
        );

        doNothing().when(paymentProducer).sendPaymentCreatedEvent(any(PaymentEventDto.class));

        GetPaymentDto result = paymentService.createPayment(createPaymentDto);

        assertThat(result.status()).isEqualTo(EXPECTED_PAYMENT_STATUS);

        verify(getPaymentEventMapper, times(1)).toDto(any(Payment.class));
        verify(getPaymentMapper, times(1)).toDto(any(Payment.class));
        verify(paymentProducer, times(1)).sendPaymentCreatedEvent(any(PaymentEventDto.class));
    }

    @Test
    void test_getPaymentById_returnPaymentById_ResourceBelongTo () {

        String paymentId = "694a6081723088150e7cf74c";

        Payment payment = new Payment(
            paymentId,
            1L, 1L, PaymentStatus.PENDING,
            LocalDateTime.now(), new BigDecimal(1000)
        );

        GetPaymentDto getPaymentDto = new GetPaymentDto(
            payment.getId(),
            payment.getOrderId(), payment.getUserId(), payment.getStatus(),
            payment.getCreationDate(), payment.getAmount()
        );

        when(securityHelper.GetCurrentUserId()).thenReturn(payment.getUserId());
        when(securityHelper.isAdmin()).thenReturn(false);

        when(paymentRepository.findById(paymentId)).thenReturn(
            Optional.of(payment));

        GetPaymentDto result = paymentService.getPaymentById(paymentId);

        assertThat(result).isEqualTo(getPaymentDto);
        verify(getPaymentMapper, times(1)).toDto(payment);
    }

    @Test
    void test_getPaymentById_ResourceDontBelongTo () {

        String paymentId = "694a6081723088150e7cf74c";

        Payment payment = new Payment(
            paymentId,
            1L, 1L, PaymentStatus.PENDING,
            LocalDateTime.now(), new BigDecimal(1000)
        );

        Long userIdFromToken = 2L;

        GetPaymentDto getPaymentDto = new GetPaymentDto(
            payment.getId(),
            payment.getOrderId(), payment.getUserId(), payment.getStatus(),
            payment.getCreationDate(), payment.getAmount()
        );

        when(securityHelper.GetCurrentUserId()).thenReturn(userIdFromToken);
        when(securityHelper.isAdmin()).thenReturn(false);

        when(paymentRepository.findById(paymentId)).thenReturn(
            Optional.of(payment));

        assertThatThrownBy(() -> paymentService.getPaymentById(paymentId))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("You define access to this resource");
    }

    @Test
    void test_getPaymentById_paymentByIdNotFound () {

        String paymentId = "694a6081723088150e7cf74c";

        Payment payment = new Payment(
            paymentId,
            1L, 1L, PaymentStatus.PENDING,
            LocalDateTime.now(), new BigDecimal(1000)
        );

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentById(paymentId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Payment not found with id: " + paymentId);

        verify(getPaymentMapper, times(0)).toDto(payment);
    }

    @Test
    void test_getAllPayments_returnAllPayments () {

        Pageable pageable = PageRequest.of(0, 10);

        List<Payment> payments = List.of(
            new Payment(
                "694a6081723088150e7cf74c",
                1L, 1L, PaymentStatus.PENDING,
                LocalDateTime.now(), new BigDecimal(1000))
        );

        Page<Payment> paymentsPage = new PageImpl<>(payments, pageable, payments.size());

        List<GetPaymentDto> getPaymentDtoList = List.of(
            new GetPaymentDto(
                payments.get(0).getId(),
                payments.get(0).getOrderId(), payments.get(0).getUserId(), payments.get(0).getStatus(),
                payments.get(0).getCreationDate(), payments.get(0).getAmount())
        );

        Page<GetPaymentDto> getPaymentDtosPage = new PageImpl<>(getPaymentDtoList, pageable, getPaymentDtoList.size());

        when(paymentRepository.findAll(pageable)).thenReturn(paymentsPage);

        Page<GetPaymentDto> result = paymentService.getAllPayments(pageable);

        assertThat(result).isEqualTo(getPaymentDtosPage);

        verify(getPaymentMapper, times(payments.size())).toDto(any(Payment.class));
    }

    @Test
    void test_getPaymentsByUserId_returnPaymentsByUserId_ResourceBelongTo () {

        Long userId = 1L;

        when(securityHelper.GetCurrentUserId()).thenReturn(userId);
        when(securityHelper.isAdmin()).thenReturn(false);

        Pageable pageable = PageRequest.of(0, 10);

        List<Payment> payments = List.of(
            new Payment(
                "694a6081723088150e7cf74c",
                userId, 1L, PaymentStatus.PENDING,
                LocalDateTime.now(), new BigDecimal(1000))
        );

        Page<Payment> paymentsPage = new PageImpl<>(payments, pageable, payments.size());

        List<GetPaymentDto> getPaymentDtoList = List.of(
            new GetPaymentDto(
                payments.get(0).getId(),
                payments.get(0).getOrderId(), payments.get(0).getUserId(), payments.get(0).getStatus(),
                payments.get(0).getCreationDate(), payments.get(0).getAmount())
        );

        Page<GetPaymentDto> getPaymentDtosPage = new PageImpl<>(getPaymentDtoList, pageable, getPaymentDtoList.size());

        when(paymentRepository.findByUserId(userId, pageable)).thenReturn(paymentsPage);

        Page<GetPaymentDto> result = paymentService.getPaymentsByUserId(userId, pageable);

        assertThat(result).isEqualTo(getPaymentDtosPage);

        verify(getPaymentMapper, times(payments.size())).toDto(any(Payment.class));
    }

    @Test
    void test_getPaymentsByUserId_ResourceDontBelongTo () {

        Long userId = 1L;
        Long userIdFromToken = 2L;

        when(securityHelper.GetCurrentUserId()).thenReturn(userIdFromToken);
        when(securityHelper.isAdmin()).thenReturn(false);

        assertThatThrownBy(() -> paymentService.getPaymentsByUserId(userId, Pageable.unpaged()))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("You define access to this resource");
    }

    @Test
    void test_getPaymentsByOrderId_returnPaymentsByOrderId () {

        Long orderId = 1L;

        Pageable pageable = PageRequest.of(0, 10);

        List<Payment> payments = List.of(
            new Payment(
                "694a6081723088150e7cf74c",
                1L, orderId, PaymentStatus.PENDING,
                LocalDateTime.now(), new BigDecimal(1000))
        );

        Page<Payment> paymentsPage = new PageImpl<>(payments, pageable, payments.size());

        List<GetPaymentDto> getPaymentDtoList = List.of(
            new GetPaymentDto(
                payments.get(0).getId(),
                payments.get(0).getOrderId(), payments.get(0).getUserId(), payments.get(0).getStatus(),
                payments.get(0).getCreationDate(), payments.get(0).getAmount())
        );

        Page<GetPaymentDto> getPaymentDtosPage = new PageImpl<>(getPaymentDtoList, pageable, getPaymentDtoList.size());

        when(paymentRepository.findByOrderId(orderId, pageable)).thenReturn(paymentsPage);

        Page<GetPaymentDto> result = paymentService.getPaymentsByOrderId(orderId, pageable);

        assertThat(result).isEqualTo(getPaymentDtosPage);

        verify(getPaymentMapper, times(payments.size())).toDto(any(Payment.class));
    }

    @Test
    void test_getPaymentsByStatus_returnPaymentsByStatuses () {

        List<PaymentStatus> statuses = List.of(PaymentStatus.PENDING);

        Pageable pageable = PageRequest.of(0, 10);

        List<Payment> payments = List.of(
            new Payment(
                "694a6081723088150e7cf74c",
                1L, 1L, PaymentStatus.PENDING,
                LocalDateTime.now(), new BigDecimal(1000))
        );

        Page<Payment> paymentsPage = new PageImpl<>(payments, pageable, payments.size());

        List<GetPaymentDto> getPaymentDtoList = List.of(
            new GetPaymentDto(
                payments.get(0).getId(),
                payments.get(0).getOrderId(), payments.get(0).getUserId(), payments.get(0).getStatus(),
                payments.get(0).getCreationDate(), payments.get(0).getAmount())
        );

        Page<GetPaymentDto> getPaymentDtosPage = new PageImpl<>(getPaymentDtoList, pageable, getPaymentDtoList.size());

        when(paymentRepository.findAllByStatusIn(statuses, pageable)).thenReturn(paymentsPage);

        Page<GetPaymentDto> result = paymentService.getPaymentsByStatus(statuses, pageable);

        assertThat(result).isEqualTo(getPaymentDtosPage);

        verify(getPaymentMapper, times(payments.size())).toDto(any(Payment.class));
    }

    @Test
    void test_getPaymentTotalResult_returnPaymentTotalResult () {

        LocalDateTime paymentCreationDate = LocalDateTime.now();
        BigDecimal totalResult = new BigDecimal(1000);

        GetPaymentTotalResultDto getPaymentTotalResultDto = new GetPaymentTotalResultDto(
            totalResult
        );

        when(paymentRepository.sumPaymentInPeriod(paymentCreationDate, paymentCreationDate)).thenReturn(getPaymentTotalResultDto);

        BigDecimal result = paymentService.getPaymentTotalResult(paymentCreationDate, paymentCreationDate);

        assertThat(result).isEqualTo(getPaymentTotalResultDto.total());
    }

    @Test
    void test_deletePayment_deletePaymentById () {

        String paymentId = "694a6081723088150e7cf74c";

        doNothing().when(paymentRepository).deleteById(paymentId);

        paymentService.deletePayment(paymentId);

        verify(paymentRepository, times(1)).deleteById(paymentId);
    }
}
