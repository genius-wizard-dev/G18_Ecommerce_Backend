package com.vutran0943.basket_service.client;

import com.vutran0943.basket_service.dto.request.UserApplyDiscountRequest;
import com.vutran0943.basket_service.dto.response.ApiResponse;
import com.vutran0943.basket_service.dto.response.CartResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="discount-service", url="${other.discount-service-url}")
public interface DiscountClient {
    @PostMapping("/api/discounts/user-apply")
    public ApiResponse<CartResponse> applyDiscount(@RequestBody UserApplyDiscountRequest body);
}