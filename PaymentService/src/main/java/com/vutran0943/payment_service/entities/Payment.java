package com.vutran0943.payment_service.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @Column(unique = true, nullable = false)
    private String id;
    @Column(name = "user_id", nullable = false)
    private String userId;
    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;
    @Column(name = "order_id", nullable = false)
    private String orderId;
    @Column(nullable = false)
    private Double amount;
    @Column(nullable = false)
    private String currency;
    private String description;
    @Column(nullable = false)
    private String status;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentInspectionToken> inspectionTokens = new ArrayList<>();
}
