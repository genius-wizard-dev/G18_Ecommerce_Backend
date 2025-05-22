package com.vutran0943.order_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DiscountResponse {
    String _id;
    String shop;
    String name;
    String code;
    Date start_time;
    Date expiry_time;
    String discount_type;
    double discount_value;
    double min_price_product;
    int quantity;
    int quantity_per_user;
    List<String> used_user_list;
    String applied_product_type;
    List<String> applied_product_list;
    boolean is_private;
    boolean is_active;
}
