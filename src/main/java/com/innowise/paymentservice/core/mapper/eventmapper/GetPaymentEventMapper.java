package com.innowise.paymentservice.core.mapper.eventmapper;

import com.innowise.paymentservice.api.dto.eventdto.PaymentEventDto;
import com.innowise.paymentservice.core.entity.Payment;
import com.innowise.paymentservice.core.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapper.class)
public interface GetPaymentEventMapper extends BaseMapper<Payment, PaymentEventDto> {

    @Mapping(source = "id", target = "paymentId")
    PaymentEventDto toDto(Payment payment);
}
