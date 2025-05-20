package com.vutran0943.payment_service.services;

import com.vutran0943.payment_service.dto.events.StockReservationSuccessEvent;
import com.vutran0943.payment_service.dto.response.PaymentProcessResponse;
import com.vutran0943.payment_service.entities.PaymentInspectionToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PaymentService {
    PaymentProcessResponse processPayment(StockReservationSuccessEvent event) throws Exception;
    String inspectPaymentStatus(String paymentId, HttpServletRequest http) throws Exception;
    String discardPayment(String paymentId, HttpServletRequest http) throws Exception;
    List<PaymentInspectionToken> getPaymentInspectionTokens(String paymentId) throws Exception;
}

