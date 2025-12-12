package com.innowise.paymentservice.core.dao;

import com.innowise.paymentservice.api.dto.PaymentTotalResult;
import com.innowise.paymentservice.core.entity.Payment;
import com.innowise.paymentservice.core.entity.PaymentStatus;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentRepository extends MongoRepository<Payment, String> {

    List<Payment> findByOrderId(String orderId);

    List<Payment> findByUserId(String userId);

    List<Payment> findAllByStatusIn(Collection<PaymentStatus> statuses);

    @Aggregation(pipeline = {
        "{ $match: { timestamp:  { $gte: ?0, $lte: ?1 } } }",
        "{ $group: { _id: null, total: { $sum: 'payment_amount' } } }"
    })
    PaymentTotalResult sumPaymentInPeriod(LocalDate from, LocalDate to);
}
