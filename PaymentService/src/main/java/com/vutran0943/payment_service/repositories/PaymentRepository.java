package com.vutran0943.payment_service.repositories;

import com.vutran0943.payment_service.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    Payment findPaymentById(String paymentId);
}
