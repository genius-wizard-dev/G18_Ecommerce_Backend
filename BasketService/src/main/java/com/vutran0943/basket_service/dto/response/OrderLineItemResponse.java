package com.vutran0943.basket_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderLineItemResponse {
    String id;
    String shopId;
    String productId;
    int quantity;
    double price;
    double finalPrice;
    boolean appliedDiscount;
}