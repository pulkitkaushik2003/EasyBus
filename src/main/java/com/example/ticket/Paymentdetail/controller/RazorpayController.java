package com.example.ticket.Paymentdetail.controller;

import com.example.ticket.Paymentdetail.Model.PaymentDetails;
import com.example.ticket.Paymentdetail.Service.PaymentService;
import com.example.ticket.Paymentdetail.Service.RazorpayService;
import com.example.ticket.User.Model.Booking;
import com.example.ticket.User.Service.BookingService;
import com.example.ticket.User.Service.EmailService;
import com.example.ticket.User.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/razorpay")
public class RazorpayController {

    @Autowired
    private RazorpayService razorpayService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/create-order")
    public ResponseEntity<?> createRazorpayOrder(@RequestBody Map<String, Long> request) {
        Long bookingId = request.get("bookingId");
        if (bookingId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Booking ID is required"));
        }

        try {
            Booking booking = bookingService.findBookingById(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

            BigDecimal amount = BigDecimal.valueOf(booking.getTotalFare());
            
            Map<String, Object> orderData = razorpayService.createOrder(
                    amount, 
                    bookingId.toString(), 
                    booking.getUserEmail()
            );

            // Save initial payment record
            PaymentDetails paymentDetails = new PaymentDetails();
            paymentDetails.setBookingId(bookingId);
            paymentDetails.setPaymentMethod("RAZORPAY");
            paymentDetails.setAmount(amount);
            paymentDetails.setStatus("PENDING");
            paymentDetails.setTransactionId(orderData.get("orderId").toString());
            paymentDetails.setCreatedAt(LocalDateTime.now());
            
            paymentService.savePaymentDetails(paymentDetails);

            return ResponseEntity.ok(orderData);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("error", "Booking not found"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to create order: " + e.getMessage()));
        }
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifyRazorpayPayment(@RequestBody Map<String, String> request) {
        try {
            String razorpayOrderId = request.get("razorpay_order_id");
            String razorpayPaymentId = request.get("razorpay_payment_id");
            String razorpaySignature = request.get("razorpay_signature");
            String bookingId = request.get("booking_id");

            if (razorpayOrderId == null || razorpayPaymentId == null || 
                razorpaySignature == null || bookingId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Missing required payment details"));
            }

            boolean isValid = razorpayService.verifyPayment(
                    razorpayOrderId, 
                    razorpayPaymentId, 
                    razorpaySignature
            );

            if (isValid) {
                Long bookingIdLong = Long.parseLong(bookingId);

                // Update payment status
                PaymentDetails paymentDetails = paymentService.findPaymentByBookingId(bookingIdLong);
                if (paymentDetails != null) {
                    paymentDetails.setStatus("SUCCESS");
                    paymentDetails.setTransactionId(razorpayPaymentId);
                    paymentService.savePaymentDetails(paymentDetails);
                    
                    bookingService.markAsPaid(bookingIdLong);

                    // ✅ Email trigger
                    Booking booking = bookingService.findBookingById(bookingIdLong)
                            .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
                    emailService.sendBookingConfirmationEmail(booking, paymentDetails);
                }

                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Payment verified successfully"
                ));
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid payment signature"));
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Payment verification failed: " + e.getMessage()));
        }
    }

    @GetMapping("/payment-status/{bookingId}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable Long bookingId) {
        try {
            PaymentDetails paymentDetails = paymentService.findPaymentByBookingId(bookingId);
            if (paymentDetails == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", paymentDetails.getStatus());
            response.put("transactionId", paymentDetails.getTransactionId());
            response.put("amount", paymentDetails.getAmount());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get payment status"));
        }
    }
    
}
