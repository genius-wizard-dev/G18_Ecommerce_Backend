package com.vutran0943.payment_service.enums;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    VNPAY("VNPayService"),
    PAYPAL("PaypalService");

    private String serviceName;

    PaymentMethod(String serviceName) {
        this.serviceName = serviceName;
    }
}
