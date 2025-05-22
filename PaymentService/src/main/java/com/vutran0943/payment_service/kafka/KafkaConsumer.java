package com.vutran0943.payment_service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vutran0943.payment_service.dto.events.CreatedPaymentEvent;
import com.vutran0943.payment_service.dto.events.PaymentFailedEvent;
import com.vutran0943.payment_service.dto.events.StockReservationSuccessEvent;
import com.vutran0943.payment_service.dto.response.ApiResponse;
import com.vutran0943.payment_service.dto.response.PaymentProcessResponse;
import com.vutran0943.payment_service.entities.Payment;
import com.vutran0943.payment_service.entities.PaymentInspectionToken;
import com.vutran0943.payment_service.factory.PaymentServiceFactory;
import com.vutran0943.payment_service.repositories.PaymentInspectionTokenRepository;
import com.vutran0943.payment_service.repositories.PaymentRepository;
import com.vutran0943.payment_service.services.PaymentService;
import com.vutran0943.payment_service.enums.PaymentMethod;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KafkaConsumer {
    @NonFinal
    @Value("${url.error-url}")
    String errorUrl;
    ObjectMapper objectMapper;
    KafkaTemplate<String, String> kafkaTemplate;
    PaymentServiceFactory paymentServiceFactory;
    PaymentInspectionTokenRepository  paymentInspectionTokenRepository;
    PaymentRepository paymentRepository;

    @KafkaListener(topics = "stock-reservation-success")
    public void handleStockReservationSuccess(String message) throws JsonProcessingException {
        StockReservationSuccessEvent event = objectMapper.readValue(message, StockReservationSuccessEvent.class);
        try {
            PaymentMethod paymentMethod = PaymentMethod.valueOf(event.getPaymentMethod());
            String provider = paymentMethod.getServiceName();

            PaymentService paymentService = paymentServiceFactory.getPaymentService(provider);
            PaymentProcessResponse res = paymentService.processPayment(event);

            CreatedPaymentEvent createdPaymentEvent = CreatedPaymentEvent.builder()
                    .orderNumber(event.getOrderId())
                    .paymentUrl(res.getPaymentUrl())
                    .build();

            Payment payment = paymentRepository.findPaymentById(res.getPaymentId());
            event.getInventoryItems().forEach(inventoryItem -> {
                PaymentInspectionToken paymentInspectionToken = PaymentInspectionToken.builder()
                        .orderNumber(event.getOrderId())
                        .payment(payment)
                        .productId(inventoryItem.getProductId())
                        .quantity(inventoryItem.getQuantity())
                        .build();
                paymentInspectionTokenRepository.save(paymentInspectionToken);
            });

            kafkaTemplate.send("created-payment", objectMapper.writeValueAsString(createdPaymentEvent));
        } catch(Exception e) {
            PaymentFailedEvent paymentFailedEvent = PaymentFailedEvent.builder()
                    .orderNumber(event.getOrderId())
                    .inventoryItems(event.getInventoryItems())
                    .failureReason("Create payment failed")
                    .paymentUrl(errorUrl)
                    .build();
            kafkaTemplate.send("payment-failed", objectMapper.writeValueAsString(paymentFailedEvent));
        }
    }
}
