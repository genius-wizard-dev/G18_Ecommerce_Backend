package com.vutran0943.order_service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vutran0943.order_service.dto.events.CreatedPaymentEvent;
import com.vutran0943.order_service.dto.events.PaymentCompletedEvent;
import com.vutran0943.order_service.dto.events.PaymentFailedEvent;
import com.vutran0943.order_service.dto.events.StockReservationFailedEvent;
import com.vutran0943.order_service.entities.Order;
import com.vutran0943.order_service.enums.OrderStatus;
import com.vutran0943.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class KafkaConsumer {
    final private OrderRepository orderRepository;
    final private  ObjectMapper objectMapper;
    @KafkaListener(topics = "stock-reservation-failed")
    public void handleStockReservationFailed(String message) throws JsonProcessingException {
        StockReservationFailedEvent event = objectMapper.readValue(message, StockReservationFailedEvent.class);

        Order order = orderRepository.findOrderByOrderNumber(event.getOrderNumber());
            order.setStatus(OrderStatus.CANCELLED.toString());
            order.setFailureReason("Inventory: " + event.getReason());
            orderRepository.save(order);
    }

    @KafkaListener(topics = "created-payment")
    public void handleCreatedPayment(String message) throws JsonProcessingException {
        String json = objectMapper.readValue(message, String.class);
        CreatedPaymentEvent createdPaymentEvent = objectMapper.readValue(json, CreatedPaymentEvent.class);

        Order order = orderRepository.findOrderByOrderNumber(createdPaymentEvent.getOrderNumber());
        order.setPaymentUrl(createdPaymentEvent.getPaymentUrl());
        order.setStatus(OrderStatus.CREATED_PAYMENT.toString());
        orderRepository.save(order);
    }

    @KafkaListener(topics = "payment-completed", groupId = "order-service")
    public void handlePaymentCompleted(String message) throws JsonProcessingException {
        String json =  objectMapper.readValue(message, String.class);
        PaymentCompletedEvent paymentCompletedEvent = objectMapper.readValue(json, PaymentCompletedEvent.class);

        Order order = orderRepository.findOrderByOrderNumber(paymentCompletedEvent.getOrderNumber());
        order.setPaymentUrl(null);
        order.setStatus(OrderStatus.COMPLETED.toString());
        orderRepository.save(order);
    }

    @KafkaListener(topics = "payment-failed", groupId = "order-service")
    public void handlePaymentFailed(String message) throws JsonProcessingException {
        String json =  objectMapper.readValue(message, String.class);
        PaymentFailedEvent paymentFailedEvent = objectMapper.readValue(json, PaymentFailedEvent.class);

        Order order = orderRepository.findOrderByOrderNumber(paymentFailedEvent.getOrderNumber());
        order.setPaymentUrl(null);
        order.setFailureReason(paymentFailedEvent.getFailureReason());
        order.setStatus(OrderStatus.CANCELLED.toString());
        orderRepository.save(order);
    }
}

