package com.example.ticket.User.Controller;
import com.example.ticket.Admin.Model.LoginAdminuser;
import com.example.ticket.Admin.Model.Route;
import com.example.ticket.Admin.Service.AdminService;
import com.example.ticket.User.Model.Booking;
import com.example.ticket.User.Model.Feedback;
import com.example.ticket.User.Repository.BookingRepository;
import com.example.ticket.User.Repository.FeedbackRepository;
import com.example.ticket.User.Service.BookingExpirationService;
import com.example.ticket.User.Service.BookingService;
import com.example.ticket.User.Service.FeedbackService;
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
import java.util.Optional;

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

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private FeedbackRepository feedbackRepository;

    

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

    // ------------------ 6. Show feedback form ------------------
    @GetMapping("/feedback/{bookingId}")
    public String showFeedbackForm(@PathVariable("bookingId") Long bookingId, Model model) {
        LoginAdminuser currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Check if booking exists and belongs to user
        Optional<Booking> bookingOpt = bookingService.findBookingByIdAndUser(bookingId, currentUser);
        if (bookingOpt.isEmpty()) {
            model.addAttribute("error", "Booking not found or you don't have permission to provide feedback.");
            return "redirect:/my-bookings";
        }

        Booking booking = bookingOpt.get();
        
        // Check if feedback already exists
        if (feedbackRepository.existsByBookingId(bookingId)) {
            model.addAttribute("error", "Feedback already submitted for this booking.");
            return "redirect:/my-bookings";
        }

        model.addAttribute("booking", booking);
        return "feedback-form";
    }

    // ------------------ 7. Submit feedback ------------------
    @PostMapping("/feedback/{bookingId}")
    public String submitFeedback(@PathVariable("bookingId") Long bookingId,
                                @RequestParam("rating") Integer rating,
                                @RequestParam(value = "comments", required = false) String comments,
                                RedirectAttributes redirectAttributes) {
        LoginAdminuser currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            // Check if booking exists and belongs to user
            Optional<Booking> bookingOpt = bookingService.findBookingByIdAndUser(bookingId, currentUser);
            if (bookingOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Booking not found.");
                return "redirect:/my-bookings";
            }

            Booking booking = bookingOpt.get();
            
            // Submit feedback
            Feedback feedback = feedbackService.submitFeedback(currentUser, booking, rating, comments);
            redirectAttributes.addFlashAttribute("success", "Thank you for your feedback! Your review has been submitted for admin review.");
            return "redirect:/my-bookings";
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/feedback/" + bookingId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred while submitting feedback.");
            return "redirect:/feedback/" + bookingId;
        }
    }

    // ------------------ 8. Show user's feedback ------------------
    @GetMapping("/my-feedback")
    public String showMyFeedback(Model model) {
        LoginAdminuser currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<Feedback> userFeedback = feedbackService.getUserFeedback((long) currentUser.getId());
        FeedbackService.FeedbackStats stats = feedbackService.getUserFeedbackStats((long) currentUser.getId());

        model.addAttribute("feedbacks", userFeedback);
        model.addAttribute("stats", stats);
        model.addAttribute("username", currentUser.getUsername());
        
        return "my-feedback";
    }

    // ------------------ 9. View single feedback ------------------
    @GetMapping("/feedback/view/{feedbackId}")
    public String viewFeedback(@PathVariable("feedbackId") Long feedbackId, Model model) {
        LoginAdminuser currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            Feedback feedback = feedbackService.getFeedbackById(feedbackId);
            
            // Check if feedback belongs to user
            if (!feedback.getUserId().equals((long) currentUser.getId())) {
                model.addAttribute("error", "You don't have permission to view this feedback.");
                return "redirect:/my-feedback";
            }

            model.addAttribute("feedback", feedback);
            return "view-feedback";
            
        } catch (ResourceNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/my-feedback";
        }
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
