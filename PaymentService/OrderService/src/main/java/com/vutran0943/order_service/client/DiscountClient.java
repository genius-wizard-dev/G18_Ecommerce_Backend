package com.vutran0943.order_service.client;

import com.vutran0943.order_service.dto.request.ApplyDiscountRequest;
import com.vutran0943.order_service.dto.response.ApiResponse;
import com.vutran0943.order_service.dto.response.DiscountResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="discount-service", url="${other.discount-service-url}")
public interface DiscountClient {
    @GetMapping("/api/discounts/{discountId}")
    public ApiResponse<DiscountResponse> getDisCountById(@PathVariable("discountId") String discountId);
    @PostMapping("/api/discounts/user-apply")
    public ApiResponse<Boolean> applyDiscount(@RequestBody ApplyDiscountRequest body);
}
