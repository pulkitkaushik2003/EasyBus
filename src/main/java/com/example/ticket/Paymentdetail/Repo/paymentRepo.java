package com.example.ticket.Paymentdetail.Repo;
import com.example.ticket.Paymentdetail.Model.PaymentDetails;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface paymentRepo extends JpaRepository<PaymentDetails, Long> {
    PaymentDetails findByBookingId(Long bookingId);
    
}
