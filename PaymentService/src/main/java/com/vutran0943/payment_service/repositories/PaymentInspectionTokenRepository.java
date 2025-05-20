package com.vutran0943.payment_service.repositories;

import com.vutran0943.payment_service.entities.PaymentInspectionToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentInspectionTokenRepository extends JpaRepository<PaymentInspectionToken, String> {
}
