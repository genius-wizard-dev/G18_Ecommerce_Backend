package com.vutran0943.payment_service.mappers;

import com.vutran0943.payment_service.dto.events.StockReservationSuccessEvent;
import com.vutran0943.payment_service.entities.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    Payment toPayment(StockReservationSuccessEvent event);
}
