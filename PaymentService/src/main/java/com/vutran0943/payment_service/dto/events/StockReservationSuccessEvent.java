package com.vutran0943.payment_service.dto.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class StockReservationSuccessEvent {
    String userId;
    String paymentMethod;
    String orderId;
    String currency;
    double amount;
    String description;
    String ipAddress;
    String bankCode;
    List<InventoryItem> inventoryItems;
}
