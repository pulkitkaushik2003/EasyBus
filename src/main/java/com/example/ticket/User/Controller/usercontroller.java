package com.example.ticket.User.Controller;
import com.example.ticket.Admin.Model.LoginAdminuser;
import com.example.ticket.Admin.Model.Route;
import com.example.ticket.Admin.Service.AdminService;
import com.example.ticket.User.Model.Booking;
import com.example.ticket.User.Repository.BookingRepository;
import com.example.ticket.User.Service.BookingExpirationService;
import com.example.ticket.User.Service.BookingService;
import com.example.ticket.User.exception.BookingException;
import com.example.ticket.User.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
public class usercontroller {
     @Autowired
    private AdminService adminService;
    @Autowired
    private BookingExpirationService bookingExpirationService;
    @Autowired
    private  BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    

    // ------------------ 1. Show all available routes ------------------
    @GetMapping("/userroutes")
    public String showRoutes(Model model) {
        model.addAttribute("routes", adminService.getAllRoutes());
        return "userroute"; // view: userroute.html
    }

    // ------------------ 2. Show booking form ------------------
    @GetMapping("/booking/{routeId}")
    public String bookingForm(@PathVariable("routeId") int routeId,
                               Model model,
                               @RequestParam(value = "error", required = false) String error) {

        // Authentication check
        LoginAdminuser currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        Route route = adminService.getRouteById(routeId);
        if (route == null) {
            return "redirect:/userroutes";
        }

        Booking booking = new Booking();
        booking.setUserName(currentUser.getUsername());
        booking.setUserEmail(currentUser.getEmail());
        booking.setUserId((long) currentUser.getId());

        model.addAttribute("route", route);
        model.addAttribute("booking", booking);
        if (error != null) {
            model.addAttribute("error", error);
        }

        return "booking-form"; // view: booking-form.html
    }

    // ------------------ 3. Handle booking submission ------------------
     @PostMapping("/booking/{routeId}")
    public String processBooking(@PathVariable("routeId") int routeId,
                                 @ModelAttribute("booking") Booking booking,
                                 RedirectAttributes redirectAttributes) {

        try {
            LoginAdminuser currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }

            if (booking.getSeatsBooked() == null || booking.getSeatsBooked() <= 0) {
                redirectAttributes.addAttribute("error", "Please select a valid number of seats.");
                return "redirect:/booking/" + routeId;
            }

            // Create booking with PENDING status
            Booking savedBooking = bookingService.createBooking(
                    booking.getSeatsBooked(),
                    routeId,
                    currentUser
            );

            // Redirect to payment page instead of confirmation
            return "redirect:/payment/booking/" + savedBooking.getId();

        } catch (BookingException | ResourceNotFoundException e) {
            redirectAttributes.addAttribute("error", e.getMessage());
            return "redirect:/booking/" + routeId;
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "An error occurred while processing your booking. Please try again.");
            return "redirect:/userroutes";
        }
    }
    // ------------------ 4. Booking confirmation ------------------
    @GetMapping("/booking-confirmation")
    public String bookingConfirmation(@RequestParam(value = "bookingId", required = false) Long bookingId,
                                      Model model) {

        if (bookingId == null) {
            model.addAttribute("error", "No booking ID provided. Please access this page from 'My Bookings' or after creating a booking.");
            return "booking-confirmation";
        }

        LoginAdminuser currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        bookingService.findBookingByIdAndUser(bookingId, currentUser)
                .ifPresentOrElse(
                        booking -> model.addAttribute("booking", booking),
                        () -> model.addAttribute("error", "Booking not found or you do not have permission to view it.")
                );

        return "booking-confirmation";
    }

    // ------------------ 5. Show user's own bookings ------------------
    @GetMapping("/my-bookings")
public String showMyBookings(Model model) {
    LoginAdminuser currentUser = getCurrentUser();
    if (currentUser == null) {
        return "redirect:/login";
    }

    List<Booking> userBookings = bookingRepository.findByUserId((long) currentUser.getId());

    System.out.println("DEBUG: User " + currentUser.getUsername() + " ID=" + currentUser.getId() +
                       " bookings=" + userBookings.size());

    model.addAttribute("bookings", userBookings);
    model.addAttribute("username", currentUser.getUsername());

    return "my-bookings";
}


    // ------------------ Utility: Get logged in user ------------------
    private LoginAdminuser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        return adminService.findByUsername(auth.getName());
    }
    
}
