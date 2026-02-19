package com.innowise.paymentservice.api.controller;

import com.innowise.paymentservice.api.dto.CreatePaymentDto;
import com.innowise.paymentservice.api.dto.GetPaymentDto;
import com.innowise.paymentservice.core.entity.PaymentStatus;
import com.innowise.paymentservice.core.service.PaymentService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GetPaymentDto> createPayment(@Valid @RequestBody CreatePaymentDto paymentDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(paymentService.createPayment(paymentDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetPaymentDto> getPaymentById(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(paymentService.getPaymentById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<GetPaymentDto>> getAllPayments(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(paymentService.getAllPayments(pageable));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<GetPaymentDto>> getPaymentsByUserId(@PathVariable Long userId, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(paymentService.getPaymentsByUserId(userId, pageable));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<GetPaymentDto>> getPaymentsByOrderId(@PathVariable Long orderId, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(paymentService.getPaymentsByOrderId(orderId, pageable));
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<GetPaymentDto>> getPaymentsByStatuses(@RequestParam List<PaymentStatus> statuses, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(paymentService.getPaymentsByStatus(statuses, pageable));
    }

    @GetMapping("/total")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BigDecimal> getTotalPaymentByDate(@RequestParam LocalDateTime from,
                                                            @RequestParam LocalDateTime to) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(paymentService.getPaymentTotalResult(from, to));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePayment(@PathVariable String id) {
        paymentService.deletePayment(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
