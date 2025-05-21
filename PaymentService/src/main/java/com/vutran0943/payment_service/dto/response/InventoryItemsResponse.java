package com.vutran0943.payment_service.dto.response;

import com.vutran0943.payment_service.dto.events.InventoryItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class InventoryItemsResponse {
    String orderNumber;
    List<InventoryItem> inventoryItems;
}
