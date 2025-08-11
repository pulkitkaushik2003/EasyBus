package com.example.ticket.User.Controller;

import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;


import com.example.ticket.Admin.Service.AdminService;
import com.example.ticket.User.Model.Booking;
import com.example.ticket.User.Repository.BookingRepository;
import com.example.ticket.User.Service.BookingExpirationService;
import com.example.ticket.User.Service.BookingService;
import com.example.ticket.User.exception.BookingException;
import com.example.ticket.User.exception.ResourceNotFoundException;
import com.example.ticket.Admin.Model.Route;
import com.example.ticket.Admin.Model.LoginAdminuser;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class usercontroller {
    private final AdminService adminService;
    private final BookingExpirationService bookingExpirationService;
    private final BookingService bookingService;
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    public usercontroller(AdminService adminService, BookingExpirationService bookingExpirationService, BookingService bookingService) {
        this.adminService = adminService;
        this.bookingExpirationService = bookingExpirationService;
        this.bookingService = bookingService;
    }

    // Show all available routes
    @GetMapping("/userroutes")
    public String showRoutes(Model model) {
        model.addAttribute("routes", adminService.getAllRoutes());
        return "userroute"; // points to userroute.html
    }


    // Show booking form for a selected route
    @GetMapping("/booking/{routeId}")
    public String booking(@PathVariable("routeId") int routeId, Model model,
                         @RequestParam(value = "error", required = false) String error) {
        // Check if user is authenticated
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }
       
        Route route = adminService.getRouteById(routeId);
        if (route == null) {
            return "redirect:/userroutes";
        }
       
        // Get current user details
        LoginAdminuser currentUser = adminService.findByUsername(auth.getName());
        if (currentUser == null) {
            return "redirect:/login";
        }
       
        model.addAttribute("route", route);
        Booking booking = new Booking();
        // Pre-fill user information
        booking.setUserName(currentUser.getUsername());
        booking.setUserEmail(currentUser.getEmail());
        booking.setUserId((long) currentUser.getId());
        model.addAttribute("booking", booking);
       
        // Add error message if present
        if (error != null) {
            model.addAttribute("error", error);
        }
       
        return "booking-form"; // points to booking-form.html
    }


    // Handle booking submission
    @PostMapping("/booking/{routeId}")
    public String processBooking(@PathVariable("routeId") int routeId,
                                 @ModelAttribute("booking") Booking booking,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return "redirect:/login";
            }
           
            LoginAdminuser currentUser = adminService.findByUsername(auth.getName());
            if (currentUser == null) {
                return "redirect:/login";
            }
           
            if (booking.getSeatsBooked() == null || booking.getSeatsBooked() <= 0) {
                redirectAttributes.addAttribute("error", "Please select a valid number of seats.");
                return "redirect:/booking/" + routeId;
            }
           
            // The error indicates the wrong arguments are being passed.
            // The fix is to pass the number of seats (an int) instead of the whole booking object.
            Booking savedBooking = bookingService.createBooking(booking.getSeatsBooked(), routeId, currentUser);
            return "redirect:/booking-confirmation?bookingId=" + savedBooking.getId();
        } catch (BookingException | ResourceNotFoundException e) {
            redirectAttributes.addAttribute("error", e.getMessage()); // Pass specific error message back to the form
            return "redirect:/booking/" + routeId;
        } catch (Exception e) {
            System.err.println("[ERROR] Booking error: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "An error occurred while processing your booking. Please try again.");
            return "redirect:/userroutes"; // Use flash attribute for redirect
        }
    }


    // GET for booking confirmation page
    @GetMapping("/booking-confirmation")
public String bookingConfirmation(
        @RequestParam(value = "bookingId", required = false) Long bookingId,
        Model model) {
    
    if (bookingId == null) {
        model.addAttribute("error", "No booking ID provided. Please access this page from 'My Bookings' or after creating a booking.");
        return "booking-confirmation";
    }

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
        return "redirect:/login";
    }

    LoginAdminuser currentUser = adminService.findByUsername(auth.getName());
    if (currentUser == null) {
        return "redirect:/login?error";
    }

    bookingService.findBookingByIdAndUser(bookingId, currentUser)
            .ifPresentOrElse(
                    booking -> model.addAttribute("booking", booking),
                    () -> model.addAttribute("error", "Booking not found or you do not have permission to view it.")
            );
    
    return "booking-confirmation";
}
   
    // Show user's own bookings
    @GetMapping("/my-bookings")
    public String showMyBookings(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }
       
        LoginAdminuser currentUser = adminService.findByUsername(auth.getName());
        if (currentUser == null) {
            return "redirect:/login";
        }
       
        List<Booking> userBookings = bookingService.findBookingsByUser(currentUser);
        model.addAttribute("bookings", userBookings);
        model.addAttribute("username", currentUser.getUsername());
        return "my-bookings";
    }
     
    

    
}