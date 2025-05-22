package com.vutran0943.order_service.serivces;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vutran0943.order_service.client.DiscountClient;
import com.vutran0943.order_service.client.InventoryClient;
import com.vutran0943.order_service.dto.request.ApplyDiscountRequest;
import com.vutran0943.order_service.dto.request.OrderCreationRequest;
import com.vutran0943.order_service.dto.request.OrderLineItemRequest;
import com.vutran0943.order_service.dto.response.*;
import com.vutran0943.order_service.entities.Order;
import com.vutran0943.order_service.entities.OrderLineItem;
import com.vutran0943.order_service.exceptions.AppException;
import com.vutran0943.order_service.exceptions.ErrorCode;
import com.vutran0943.order_service.kafka.events.InventorySubtractionEvent;
import com.vutran0943.order_service.repository.OrderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class OrderService {
    OrderRepository orderRepository;
    InventoryClient inventoryClient;
    DiscountClient discountClient;

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OrderResponse createOrder(OrderCreationRequest request) throws Exception {
        String discountId = request.getDiscountId();
        DiscountResponse discount = null;

        if(discountId != null) discount = discountClient.getDisCountById(discountId).getData();

        String userId = request.getUserId();
        double totalPrice = 0;

        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        MultiValueMap<String, String> quantityPerProductId = new LinkedMultiValueMap<>();

        int occurrences = 0;
        int quantityPerUser = 0;
        double discountValue = 0;
        String discountType = "";
        String appliedProductType = "";
        List<String> appliedProductList = new ArrayList<>();
        List<OrderLineItem> newOrderLineItemList = new ArrayList<>();
        List<OrderLineItemRequest> orderLineItemRequestList = request.getOrderLineItemList();
        List<String> userIdList = new ArrayList<>();

        if(discount != null) {
            appliedProductList = discount.getApplied_product_list();
            occurrences = Collections.frequency(discount.getUsed_user_list(), userId);
            discountValue = discount.getDiscount_value();
            discountType = discount.getDiscount_type();
            quantityPerUser = discount.getQuantity_per_user();
            appliedProductType = discount.getApplied_product_type();
        }

        int appliedDiscountNum = 0;

        for(OrderLineItemRequest orderLineItemRequest : orderLineItemRequestList) {
            System.out.println(orderLineItemRequest.isAppliedDiscount());
            if(orderLineItemRequest.isAppliedDiscount()) appliedDiscountNum += 1;
        }

        if(occurrences + appliedDiscountNum > quantityPerUser) throw new AppException(ErrorCode.DISCOUNT_NOT_ENOUGH);

        for(OrderLineItemRequest orderLineItemRequest : orderLineItemRequestList) {
            quantityPerProductId.add(orderLineItemRequest.getProductId(), String.valueOf(orderLineItemRequest.getQuantity()));
            OrderLineItem orderLineItem = mapToOrderLineItem(orderLineItemRequest);
            orderLineItem.setOrder(order);

            if(discount != null && (appliedProductType.equals("all") || appliedProductList.contains(orderLineItem.getProductId())) && orderLineItem.isAppliedDiscount()) {
                userIdList.add(userId);
                double finalPrice = discountType.equals("fixed") ? orderLineItem.getPrice() - discountValue : orderLineItem.getPrice() * (1-discountValue);
                if(finalPrice < 0) finalPrice = 0;
                orderLineItem.setDiscountId(discountId);
                orderLineItem.setFinalPrice(finalPrice);
            } else orderLineItem.setFinalPrice(orderLineItem.getPrice());

            totalPrice += orderLineItem.getFinalPrice();

            newOrderLineItemList.add(orderLineItem);
        }

        order.setOrderLineItemList(newOrderLineItemList);
        order.setTotalPrice(totalPrice);
        order.setUserId(userId);

        ApiResponse<List<InventoryStockResponse>> res = inventoryClient.isInStock(quantityPerProductId);
        List<InventoryStockResponse> inventoryStockLists = res.getData();

        boolean isInStock = inventoryStockLists.stream().allMatch(InventoryStockResponse::isInStock);

        if(isInStock) {
            for (OrderLineItem item : order.getOrderLineItemList()) {
                InventorySubtractionEvent event = new InventorySubtractionEvent(item.getProductId(), item.getQuantity());
                try {
                    kafkaTemplate.send("subtract-inventory-topic", objectMapper.writeValueAsString(event));
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }

            orderRepository.save(order);
            ApplyDiscountRequest applyDiscountRequest = ApplyDiscountRequest.builder()
                    .discountId(discountId)
                    .userIdList(userIdList)
                    .build();

//            discountClient.applyDiscount(applyDiscountRequest);

            List<OrderLineItemResponse> orderLineItemsResponses = order
                    .getOrderLineItemList()
                    .stream()
                    .map(this::mapToOrderLineItemResponse)
                    .toList();

            return OrderResponse.builder()
                    .orderNumber(order.getOrderNumber())
                    .orderLineItemList(orderLineItemsResponses)
                    .totalPrice(totalPrice)
                    .userId(userId)
                    .build();
        } else throw new AppException(ErrorCode.OUT_OF_STOCK);
    }

    public OrderResponse getOrder(String orderNumber) {
        Order order = orderRepository.findOrderByOrderNumber(orderNumber);

        List<OrderLineItemResponse> orderLineItemsResponseList = order
                .getOrderLineItemList()
                .stream()
                .map(this::mapToOrderLineItemResponse).toList();

        return OrderResponse.builder()
                .orderNumber(order.getOrderNumber())
                .orderLineItemList(orderLineItemsResponseList)
                .totalPrice(order.getTotalPrice())
                .userId(order.getUserId())
                .build();
    }

    private OrderLineItem mapToOrderLineItem(OrderLineItemRequest orderLineItemRequest) {
        return OrderLineItem.builder()
                .price(orderLineItemRequest.getPrice())
                .quantity(orderLineItemRequest.getQuantity())
                .productId(orderLineItemRequest.getProductId())
                .appliedDiscount(orderLineItemRequest.isAppliedDiscount())
                .build();
    }

    private OrderLineItemResponse mapToOrderLineItemResponse(OrderLineItem orderLineItem) {
        return OrderLineItemResponse.builder()
                .price(orderLineItem.getPrice())
                .quantity(orderLineItem.getQuantity())
                .productId(orderLineItem.getProductId())
                .finalPrice(orderLineItem.getFinalPrice())
                .build();
    }
}


