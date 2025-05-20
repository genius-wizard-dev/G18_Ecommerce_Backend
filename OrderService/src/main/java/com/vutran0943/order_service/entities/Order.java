package com.vutran0943.order_service.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "orders")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @Column(name = "order_number", nullable = false, unique = true)
    String orderNumber;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "order")
    List<OrderLineItem> orderLineItemList;
    @Column(name = "user_id", nullable = false)
    String userId;
    @Column(name = "discount_id")
    String discountId;
    @Column(nullable = false)
    String status;
    @Column(length=10000)
    String paymentUrl;
    String failureReason;
    @Column(name="total_price", nullable = false)
    double totalPrice;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;
    @UpdateTimestamp
    LocalDateTime updatedAt;
}
