package com.innowise.paymentservice.core.dao;

import com.innowise.paymentservice.api.dto.GetPaymentTotalResultDto;
import com.innowise.paymentservice.core.entity.Payment;
import com.innowise.paymentservice.core.entity.PaymentStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentRepository extends MongoRepository<Payment, String> {

    Page<Payment> findByOrderId(Long orderId, Pageable pageable);

    Page<Payment> findByUserId(Long userId, Pageable pageable);

    Page<Payment> findAllByStatusIn(Collection<PaymentStatus> statuses, Pageable pageable);

    @Aggregation(pipeline = {
        "{ $match: { creation_date:  { $gte: ?0, $lte: ?1 } } }",
        "{ $group: { _id: null, total: { $sum: '$amount' } } }"
    })
    GetPaymentTotalResultDto sumPaymentInPeriod(LocalDateTime from, LocalDateTime to);
}
