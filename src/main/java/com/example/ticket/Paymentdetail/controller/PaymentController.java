package com.example.ticket.Paymentdetail.controller;

import com.example.ticket.Paymentdetail.Model.PaymentDetails;
import com.example.ticket.Paymentdetail.Service.PaymentService;
import com.example.ticket.User.Model.Booking;
import com.example.ticket.User.Service.BookingService;
import com.example.ticket.User.exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequestMapping("/api")
public class PaymentController {

    private final BookingService bookingService;
    private final PaymentService paymentService;

    @Autowired
    public PaymentController(BookingService bookingService, PaymentService paymentService) {
        this.bookingService = bookingService;
        this.paymentService = paymentService;
    }

    // -------- CASH PAYMENT (AJAX JSON) --------
    @PostMapping("/cash-payment")
    @ResponseBody
    public ResponseEntity<?> confirmCashPayment(@RequestBody Map<String, Long> request) {
        Long bookingId = request.get("bookingId");
        if (bookingId == null) return ResponseEntity.badRequest().body("Booking ID is required");

        try {
            Booking booking = bookingService.findBookingById(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

            PaymentDetails pd = new PaymentDetails();
            pd.setBookingId(bookingId);
            pd.setPaymentMethod("CASH");
            pd.setAmount(BigDecimal.valueOf(booking.getTotalFare()));
            pd.setStatus("SUCCESS");
            pd.setTransactionId("TXN-" + bookingId + "-" + System.currentTimeMillis());
            pd.setCreatedAt(LocalDateTime.now());

            paymentService.savePaymentDetails(pd);
            bookingService.confirmCashBooking(bookingId);

            return ResponseEntity.ok("Cash payment confirmed successfully with Transaction ID: " + pd.getTransactionId());

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body("Booking not found");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    // -------- DEMO ORDER (AJAX JSON) --------
    @PostMapping("/orders")
    @ResponseBody
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Long> request) {
        Long bookingId = request.get("bookingId");
        if (bookingId == null) return ResponseEntity.badRequest().body("Booking ID is required");

        try {
            Booking booking = bookingService.findBookingById(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

            Map<String, String> order = Map.of(
                    "orderId", "ORDER_" + bookingId + "_" + System.currentTimeMillis(),
                    "amount", String.valueOf(booking.getTotalFare())
            );
            return ResponseEntity.ok(order);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body("Booking not found");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error creating order");
        }
    }

    // -------- VERIFY PAYMENT (AJAX JSON) --------
    @PostMapping("/verify")
    @ResponseBody
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> request) {
        try {
            return ResponseEntity.ok("VERIFIED");  // Demo verification
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Verification failed");
        }
    }

    // -------- LOAD PAYMENT PAGE --------
    @GetMapping("/payonline")
    public String payonline(@RequestParam("bookingId") Long bookingId, Model model) {
        Booking booking = bookingService.findBookingById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        model.addAttribute("bookingId", bookingId);
        model.addAttribute("totalFare", booking.getTotalFare());
        model.addAttribute("routeName", booking.getRouteName());
        model.addAttribute("seats", booking.getSeatsBooked());

        PaymentDetails pd = new PaymentDetails();
        pd.setBookingId(bookingId);
        model.addAttribute("paymentDetails", pd);

        return "bookingpayment-enhanced";
    }

    // -------- CARD PAYMENT --------
    @PostMapping("/process-payment")
    public String processPayment(@ModelAttribute PaymentDetails paymentDetails) {
        if (paymentDetails.getBookingId() == null) return "redirect:/my-bookings";

        if (paymentDetails.getAmount() == null || paymentDetails.getAmount().compareTo(BigDecimal.ZERO) == 0) {
            Booking booking = bookingService.findBookingById(paymentDetails.getBookingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
            paymentDetails.setAmount(BigDecimal.valueOf(booking.getTotalFare()));
        }

        paymentDetails.setPaymentMethod("CARD");
        paymentDetails.setStatus("SUCCESS");
        paymentDetails.setTransactionId("TXN-" + paymentDetails.getBookingId() + "-" + System.currentTimeMillis());
        paymentDetails.setCreatedAt(LocalDateTime.now());

        paymentService.savePaymentDetails(paymentDetails);
        bookingService.markAsPaid(paymentDetails.getBookingId());

        return "redirect:/api/booking-confirmation?bookingId=" + paymentDetails.getBookingId();
    }

    // -------- UPI PAYMENT --------
    @PostMapping("/pay/upi")
    public String processUpiPayment(@ModelAttribute PaymentDetails paymentDetails) {
        if (paymentDetails.getBookingId() == null) return "redirect:/my-bookings";
        if (paymentDetails.getUpiId() == null || paymentDetails.getUpiId().isBlank()) {
            return "redirect:/api/payonline?bookingId=" + paymentDetails.getBookingId();
        }

        if (paymentDetails.getAmount() == null || paymentDetails.getAmount().compareTo(BigDecimal.ZERO) == 0) {
            Booking booking = bookingService.findBookingById(paymentDetails.getBookingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
            paymentDetails.setAmount(BigDecimal.valueOf(booking.getTotalFare()));
        }

        paymentDetails.setPaymentMethod("UPI");
        paymentDetails.setStatus("SUCCESS");
        paymentDetails.setTransactionId("TXN-" + paymentDetails.getBookingId() + "-" + System.currentTimeMillis());
        paymentDetails.setCreatedAt(LocalDateTime.now());

        paymentService.savePaymentDetails(paymentDetails);
        bookingService.markAsPaid(paymentDetails.getBookingId());

        return "redirect:/api/booking-confirmation?bookingId=" + paymentDetails.getBookingId();
    }

    // -------- BOOKING CONFIRMATION PAGE --------
    @GetMapping("/booking-confirmation")
    public String bookingConfirmation(@RequestParam("bookingId") Long bookingId, Model model) {
        Booking booking = bookingService.findBookingById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        model.addAttribute("booking", booking);

        PaymentDetails pd = paymentService.findPaymentByBookingId(bookingId);
        model.addAttribute("paymentDetails", pd);

        return "booking-confirmation";
    }
    @PostMapping("/cancel-booking")
    public String cancelBooking(@RequestParam("bookingId") Long bookingId) {
        Booking booking = bookingService.findBookingById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        bookingService.cancelBooking(bookingId);
        return "redirect:/api/booking-confirmation?bookingId=" + bookingId;
    }
    
}
