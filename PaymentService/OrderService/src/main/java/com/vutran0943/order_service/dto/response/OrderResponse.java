package com.vutran0943.order_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {
    String userId;
    String discountId;
    String orderNumber;
    List<OrderLineItemResponse> orderLineItemList;
    double totalPrice;
}
