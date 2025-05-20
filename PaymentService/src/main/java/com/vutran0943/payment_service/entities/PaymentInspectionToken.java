package com.vutran0943.payment_service.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class PaymentInspectionToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;
    @Column(nullable = false)
    String orderNumber;
    @Column(nullable = false)
    String productId;
    @Column(nullable = false)
    int quantity;
}
