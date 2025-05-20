package com.vutran0943.order_service.dto.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderCreatedEvent {
    String orderNumber;
    String userId;
    String description;
    String paymentMethod;
    String currency;
    double amount;
    String ipAddress;
    List<InventoryItem> inventoryItems;
}
