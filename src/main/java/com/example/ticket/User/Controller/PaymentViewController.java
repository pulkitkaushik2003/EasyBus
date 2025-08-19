package com.example.ticket.User.Controller;
import com.example.ticket.Admin.Model.LoginAdminuser;
import com.example.ticket.Admin.Service.AdminService;
import com.example.ticket.User.Model.Booking;
import com.example.ticket.User.Service.BookingService;
import com.example.ticket.User.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PaymentViewController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private AdminService adminService;

    // ✅ Display payment page for a booking
    @GetMapping("/payment/booking/{bookingId}")
    public String showPaymentPage(@PathVariable Long bookingId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }

        LoginAdminuser currentUser = adminService.findByUsername(auth.getName());
        if (currentUser == null) {
            return "redirect:/login";
        }

        Booking booking = bookingService.findBookingByIdAndUser(bookingId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        model.addAttribute("bookingId", booking.getId());
        model.addAttribute("routeName", booking.getRouteName());
        model.addAttribute("seats", booking.getSeatsBooked());
        model.addAttribute("totalFare", booking.getTotalFare());
        model.addAttribute("userEmail", currentUser.getEmail());
        model.addAttribute("userName", currentUser.getUsername());

        return "razorpay-payment";
    }
}
