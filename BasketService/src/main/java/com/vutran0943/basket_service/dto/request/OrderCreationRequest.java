package com.vutran0943.basket_service.dto.request;

import com.vutran0943.basket_service.dto.response.CartItemResponse;
import com.vutran0943.basket_service.entities.CartItem;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderCreationRequest {
    String cartId;
    String userId;
    String paymentMethod;
    String currency;
    String description;
    List<CartItemResponse> orderLineItemList;
    double totalPrice;
}
