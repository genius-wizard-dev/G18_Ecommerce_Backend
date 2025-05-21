package com.vutran0943.order_service.dto.request;

import com.vutran0943.order_service.dto.response.OrderLineItemResponse;
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
    private String orderNumber;
    private String userId;
    List<OrderLineItemResponse> orderLineItemList;
}
