package com.vutran0943.payment_service.dto.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CreatedPaymentEvent {
    String orderNumber;
    String paymentUrl;
}
