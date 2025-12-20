package com.innowise.paymentservice.core.mapper.paymentmapper;

import com.innowise.paymentservice.api.dto.CreatePaymentDto;
import com.innowise.paymentservice.core.entity.Payment;
import com.innowise.paymentservice.core.mapper.BaseMapper;
import org.mapstruct.Mapper;

@Mapper(config = BaseMapper.class)
public interface CreatePaymentMapper extends BaseMapper<Payment, CreatePaymentDto> {
}
