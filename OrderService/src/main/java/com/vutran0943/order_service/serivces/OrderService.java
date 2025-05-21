package com.vutran0943.order_service.serivces;

import com.vutran0943.order_service.dto.request.OrderCreationRequest;
import com.vutran0943.order_service.dto.request.OrderLineItemRequest;
import com.vutran0943.order_service.dto.response.*;
import com.vutran0943.order_service.entities.Order;
import com.vutran0943.order_service.entities.OrderLineItem;
import com.vutran0943.order_service.enums.OrderStatus;
import com.vutran0943.order_service.repository.OrderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class OrderService {
    OrderRepository orderRepository;

    public OrderResponse createOrder(OrderCreationRequest request) throws Exception {
        String userId = request.getUserId();
        List<OrderLineItem> orderLineItemList = new ArrayList<>();
        List<OrderLineItemRequest> orderLineItemRequestList = request.getOrderLineItemList();

        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        for(OrderLineItemRequest orderLineItemRequest : orderLineItemRequestList){
            OrderLineItem orderLineItem = mapToOrderLineItem(orderLineItemRequest);
            orderLineItem.setOrder(order);
            orderLineItemList.add(orderLineItem);
        }

        order.setUserId(userId);
        order.setTotalPrice(request.getTotalPrice());
        order.setOrderLineItemList(orderLineItemList);
        order.setStatus(OrderStatus.PENDING.toString());

        orderRepository.save(order);

        List<OrderLineItemResponse> orderLineItemsResponses = order
                .getOrderLineItemList()
                .stream()
                .map(this::mapToOrderLineItemResponse)
                .toList();

        return OrderResponse.builder()
                .orderNumber(order.getOrderNumber())
                .orderLineItemList(orderLineItemsResponses)
                .totalPrice(request.getTotalPrice())
                .userId(userId)
                .build();
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

    public void updateOrder(OrderResponse orderResponse) {
        System.out.println(">>>>> " + mapToOrder(orderResponse));
        orderRepository.save(mapToOrder(orderResponse));
    }

    private Order mapToOrder(OrderResponse orderResponse) {
        List<OrderLineItem> orderLineItemList = new ArrayList<>();
        for(OrderLineItemResponse  orderLineItemResponse : orderResponse.getOrderLineItemList()){
            OrderLineItem orderLineItem = mapToOrderLineItem(orderLineItemResponse);
            orderLineItem.setOrder(Order.builder().orderNumber(orderResponse.getOrderNumber()).build());
            orderLineItemList.add(orderLineItem);
        }

        return Order.builder()
                .userId(orderResponse.getUserId())
                .discountId(orderResponse.getDiscountId())
                .totalPrice(orderResponse.getTotalPrice())
                .orderNumber(orderResponse.getOrderNumber())
                .orderLineItemList(orderLineItemList)
                .status(OrderStatus.PENDING.toString())
                .build();
    }

    private OrderLineItem mapToOrderLineItem(OrderLineItemRequest orderLineItemRequest) {
        return OrderLineItem.builder()
                .price(orderLineItemRequest.getPrice())
                .finalPrice(orderLineItemRequest.getFinalPrice())
                .quantity(orderLineItemRequest.getQuantity())
                .productId(orderLineItemRequest.getProductId())
                .shopId(orderLineItemRequest.getShopId())
                .appliedDiscount(orderLineItemRequest.isAppliedDiscount())
                .build();
    }

    private OrderLineItem mapToOrderLineItem(OrderLineItemResponse orderLineItemResponse) {
        return OrderLineItem.builder()
                .id(orderLineItemResponse.getId())
                .price(orderLineItemResponse.getPrice())
                .finalPrice(orderLineItemResponse.getFinalPrice())
                .quantity(orderLineItemResponse.getQuantity())
                .productId(orderLineItemResponse.getProductId())
                .shopId(orderLineItemResponse.getShopId())
                .appliedDiscount(orderLineItemResponse.isAppliedDiscount())
                .build();
    }

    private OrderLineItemResponse mapToOrderLineItemResponse(OrderLineItem orderLineItem) {
        return OrderLineItemResponse.builder()
                .id(orderLineItem.getId())
                .shopId(orderLineItem.getShopId())
                .price(orderLineItem.getPrice())
                .quantity(orderLineItem.getQuantity())
                .productId(orderLineItem.getProductId())
                .finalPrice(orderLineItem.getFinalPrice())
                .appliedDiscount(orderLineItem.isAppliedDiscount())
                .build();
    }
}


