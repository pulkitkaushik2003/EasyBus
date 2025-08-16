package com.example.ticket.Paymentdetail.Service;

import com.example.ticket.Paymentdetail.Model.PaymentDetails;
import com.example.ticket.Paymentdetail.Repo.paymentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    @Autowired
    private paymentRepo paymentRepository;

    public PaymentDetails savePaymentDetails(PaymentDetails paymentDetails) {
        return paymentRepository.save(paymentDetails);
    }

    public PaymentDetails findPaymentByBookingId(Long bookingId) {
        return paymentRepository.findById(bookingId).orElse(null);
    }
}
