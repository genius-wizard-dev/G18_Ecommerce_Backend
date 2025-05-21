package com.vutran0943.order_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderLineItemRequest {
    String shopId;
    String productId;
    int quantity;
    double price;
    double finalPrice;
    boolean appliedDiscount;
}
