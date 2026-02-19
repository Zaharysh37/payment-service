package com.innowise.paymentservice.core.mapper.eventmapper;

import com.innowise.paymentservice.api.dto.CreatePaymentDto;
import com.innowise.paymentservice.api.dto.eventdto.OrderEventDto;
import com.innowise.paymentservice.core.mapper.BaseMapper;
import org.mapstruct.Mapper;

@Mapper(config = BaseMapper.class)
public interface CreatePaymentEventMapper extends BaseMapper<CreatePaymentDto, OrderEventDto> {
}
