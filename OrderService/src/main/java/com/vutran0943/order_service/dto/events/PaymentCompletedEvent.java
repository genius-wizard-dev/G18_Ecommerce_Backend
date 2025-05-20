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
public class PaymentCompletedEvent {
    String orderNumber;
    List<InventoryItem> inventoryItems;
}