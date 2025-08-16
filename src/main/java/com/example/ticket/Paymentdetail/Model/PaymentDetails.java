package com.example.ticket.Paymentdetail.Model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class PaymentDetails {

    @Id
    private Long bookingId;

    private String paymentMethod; // CARD / UPI / CASH
    private BigDecimal amount = BigDecimal.ZERO; // Default 0

    // ---- Card Payment Fields ----
    private String cardNumber;
    private String expiryDate;
    private String cvv;
    private String cardHolderName;

    // ---- UPI Payment Fields ----
    private String upiId;

    // ---- Extra Fields ----
    private String transactionId;
    private String status; // PENDING / SUCCESS / FAILED
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.transactionId == null || this.transactionId.isEmpty()) {
            this.transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.amount == null) {
            this.amount = BigDecimal.ZERO;
        }
    }
}
