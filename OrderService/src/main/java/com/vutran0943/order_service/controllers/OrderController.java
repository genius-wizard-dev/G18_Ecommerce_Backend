package com.vutran0943.order_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vutran0943.order_service.client.DiscountClient;
import com.vutran0943.order_service.dto.events.InventoryItem;
import com.vutran0943.order_service.dto.events.OrderCreatedEvent;
import com.vutran0943.order_service.dto.request.ApplyDiscountRequest;
import com.vutran0943.order_service.dto.request.OrderCreationRequest;
import com.vutran0943.order_service.dto.request.UserApplyDiscountRequest;
import com.vutran0943.order_service.dto.response.ApiResponse;
import com.vutran0943.order_service.dto.response.DiscountResponse;
import com.vutran0943.order_service.dto.response.OrderLineItemResponse;
import com.vutran0943.order_service.dto.response.OrderResponse;
import com.vutran0943.order_service.serivces.OrderService;
import com.vutran0943.order_service.utils.AppUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {
    OrderService orderService;
    KafkaTemplate<String, String> kafkaTemplate;
    ObjectMapper objectMapper;
    DiscountClient discountClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderResponse> placeOrder(@RequestBody OrderCreationRequest request, HttpServletRequest req) throws Exception {
        OrderResponse data = orderService.createOrder(request);

        List<InventoryItem> inventoryItems = data.getOrderLineItemList()
                .stream()
                .map((item) -> new InventoryItem(item.getProductId(), item.getQuantity())).toList();

        OrderCreatedEvent orderCreatedEvent = OrderCreatedEvent.builder()
                .orderNumber(data.getOrderNumber())
                .userId(data.getUserId())
                .description(request.getDescription())
                .paymentMethod(request.getPaymentMethod())
                .currency(request.getCurrency())
                .amount(data.getTotalPrice())
                .inventoryItems(inventoryItems)
                .ipAddress(AppUtils.getIpAddress(req))
                .build();

        kafkaTemplate.send("order-created", objectMapper.writeValueAsString(orderCreatedEvent));

        ApiResponse<OrderResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Place Order successful");
        apiResponse.setData(data);
        return apiResponse;
    }

    @GetMapping("/{orderNumber}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<OrderResponse> getOrderByOrderNumber(@PathVariable String orderNumber) {
        ApiResponse<OrderResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Get Order By Order Number");
        apiResponse.setData(orderService.getOrder(orderNumber));

        return apiResponse;
    }

    @PostMapping("/apply-discount")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<OrderResponse> applyDiscount(@RequestBody ApplyDiscountRequest request)  {
        OrderResponse orderResponse = orderService.getOrder(request.getOrderNumber());

        List<OrderLineItemResponse> newOrderLineItemList = orderResponse.getOrderLineItemList().stream().map(item -> {
            item.setAppliedDiscount(request.getProductIdList().contains(item.getProductId()));
            return item;
        }).toList();

        UserApplyDiscountRequest userApplyDiscountRequest = UserApplyDiscountRequest.builder()
                .orderNumber(request.getOrderNumber())
                .userId(orderResponse.getUserId())
                .discountId(request.getDiscountId())
                .orderLineItemList(newOrderLineItemList)
                .build();

        ApiResponse<OrderResponse> res = discountClient.applyDiscount(userApplyDiscountRequest);
        if(res.getData() != null) orderService.updateOrder(res.getData());
        ApiResponse<OrderResponse> apiResponse = new ApiResponse<>();
        apiResponse.setCode(res.getCode());
        apiResponse.setMessage(res.getMessage());
        apiResponse.setData(res.getData());

        return apiResponse;
    }
}
