package com.innowise.paymentservice.core.mapper.paymentmapper;

import com.innowise.paymentservice.api.dto.GetPaymentDto;
import com.innowise.paymentservice.core.entity.Payment;
import com.innowise.paymentservice.core.mapper.BaseMapper;
import org.mapstruct.Mapper;

@Mapper(config = BaseMapper.class)
public interface GetPaymentMapper extends BaseMapper<Payment, GetPaymentDto> {
}
