package com.vutran0943.basket_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyDiscountRequest {
    private String discountId;
    private String cartId;
    private List<String> productIdList;
}