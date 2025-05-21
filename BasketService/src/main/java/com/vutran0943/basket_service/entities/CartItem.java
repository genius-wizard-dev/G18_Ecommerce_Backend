package com.vutran0943.basket_service.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Cart cart;
    @Column(name = "product_id", nullable = false)
    private String productId;
    @Column(name = "shop_id", nullable = false)
    private String shopId;
    private int quantity;
    @Column(nullable = false)
    private double price;
    @Column(nullable = false)
    private double finalPrice;
    private boolean appliedDiscount;
}
