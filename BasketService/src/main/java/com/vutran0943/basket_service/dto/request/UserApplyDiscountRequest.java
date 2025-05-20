package com.vutran0943.basket_service.dto.request;

import com.vutran0943.basket_service.dto.response.CartItemResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserApplyDiscountRequest {
    private String discountId;
    private String cartId;
    private String userId;
    List<CartItemResponse> cartItems;
}
