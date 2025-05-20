package com.vutran0943.basket_service.client;

import com.vutran0943.basket_service.dto.request.OrderCreationRequest;
import com.vutran0943.basket_service.dto.response.ApiResponse;
import com.vutran0943.basket_service.dto.response.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="order-service", url="${other.order-service-url}")
public interface OrderClient {
    @PostMapping("/api/orders")
    public ApiResponse<OrderResponse> placeOrder(@RequestBody OrderCreationRequest body);
}
