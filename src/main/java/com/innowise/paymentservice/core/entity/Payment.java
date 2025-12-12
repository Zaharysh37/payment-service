package com.innowise.paymentservice.core.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "payment")
public class Payment {

    @Id
    private String id;

    @Field("user_id")
    private String userId;

    @Field("order_id")
    private String orderId;

    private PaymentStatus status;

    @CreatedDate
    private LocalDateTime paymentDate;

    @Field(targetType = FieldType.DECIMAL128, name = "payment_amount")
    private BigDecimal paymentAmount;
}
