package com.vutran0943.payment_service.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.vutran0943.payment_service.dto.events.InventoryItem;
import com.vutran0943.payment_service.dto.events.PaymentCompletedEvent;
import com.vutran0943.payment_service.dto.events.PaymentFailedEvent;
import com.vutran0943.payment_service.dto.response.InventoryItemsResponse;
import com.vutran0943.payment_service.entities.PaymentInspectionToken;
import com.vutran0943.payment_service.enums.PaymentStatus;
import com.vutran0943.payment_service.exceptions.AppException;
import com.vutran0943.payment_service.exceptions.ErrorCode;
import com.vutran0943.payment_service.factory.PaymentServiceFactory;
import com.vutran0943.payment_service.services.PaymentService;
import com.vutran0943.payment_service.enums.PaymentMethod;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentServiceFactory paymentServiceFactory;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @GetMapping("/{payment-method}/inspect/{paymentId}")
    public String successHandler(
            @PathVariable("payment-method") String paymentMethod,
            @PathVariable("paymentId") String paymentId,
            HttpServletRequest http
    ) throws Exception {
        String provider = PaymentMethod.valueOf(paymentMethod).getServiceName();
        PaymentService paymentService = paymentServiceFactory.getPaymentService(provider);
        InventoryItemsResponse inventoryItemsResponse = this.getInventoryItems(paymentService, paymentId);
        List<InventoryItem> inventoryItems = inventoryItemsResponse.getInventoryItems();
        String orderNumber = inventoryItemsResponse.getOrderNumber();

        try {
            String status = paymentService.inspectPaymentStatus(paymentId, http);

            if(status.equals(PaymentStatus.FAILED.toString())) throw new AppException(ErrorCode.INVALID_PAYMENT);

            PaymentCompletedEvent paymentCompletedEvent = PaymentCompletedEvent.builder()
                    .inventoryItems(inventoryItems)
                    .orderNumber(orderNumber)
                    .build();

            kafkaTemplate.send("payment-completed", objectMapper.writeValueAsString(paymentCompletedEvent));
            return status;
        } catch (Exception e) {
            PaymentFailedEvent paymentFailedEvent = PaymentFailedEvent.builder()
                    .inventoryItems(inventoryItems)
                    .orderNumber(orderNumber)
                    .failureReason("processing payment failed")
                    .build();

            kafkaTemplate.send("payment-failed", objectMapper.writeValueAsString(paymentFailedEvent));
            return "error";
        }
    }

    @GetMapping("/{payment-method}/cancel/{paymentId}")
    public String cancelHandler(
            @PathVariable("payment-method") String paymentMethod,
            @PathVariable("paymentId") String paymentId,
            HttpServletRequest http
    ) throws Exception {
        String provider = PaymentMethod.valueOf(paymentMethod).getServiceName();
        PaymentService paymentService = paymentServiceFactory.getPaymentService(provider);
        InventoryItemsResponse inventoryItemsResponse = this.getInventoryItems(paymentService, paymentId);
        List<InventoryItem> inventoryItems = inventoryItemsResponse.getInventoryItems();
        String orderNumber = inventoryItemsResponse.getOrderNumber();

        PaymentFailedEvent paymentFailedEvent = PaymentFailedEvent.builder()
                .inventoryItems(inventoryItems)
                .orderNumber(orderNumber)
                .failureReason("processing payment cancel")
                .build();

        kafkaTemplate.send("payment-failed", objectMapper.writeValueAsString(paymentFailedEvent));
        return paymentService.discardPayment(paymentId, http);
    }

    @GetMapping("/error")
    public String errorHandler() {
        return "error";
    }

    private InventoryItemsResponse getInventoryItems(PaymentService paymentService, String paymentId) throws Exception {
        String orderNumber = null;
        List<PaymentInspectionToken> paymentInspectionTokens = paymentService.getPaymentInspectionTokens(paymentId);
        List<InventoryItem> inventoryItems = new ArrayList<>();

        for(PaymentInspectionToken paymentInspectionToken : paymentInspectionTokens) {
            if(orderNumber == null) orderNumber = paymentInspectionToken.getOrderNumber();
            inventoryItems.add(InventoryItem.builder()
                    .productId(paymentInspectionToken.getProductId())
                    .quantity(paymentInspectionToken.getQuantity())
                    .build());
        }

        return InventoryItemsResponse.builder().inventoryItems(inventoryItems).orderNumber(orderNumber).build();
    }

}
