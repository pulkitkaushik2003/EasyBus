package com.example.ticket.Admin.Controller;

import com.example.ticket.Admin.Model.LoginAdminuser;
import com.example.ticket.Admin.Model.Route;
import com.example.ticket.Admin.Service.AdminService;
import com.example.ticket.User.Model.Booking;
import com.example.ticket.User.Model.Feedback;
import com.example.ticket.User.Repository.BookingRepository;
import com.example.ticket.User.Repository.FeedbackRepository;
import com.example.ticket.User.Service.BookingExpirationService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class AdminContrller {

    private final BookingRepository bookingRepository;
    private final AdminService adminService;
    private final BookingExpirationService bookingExpirationService;
    private final FeedbackRepository feedbackRepository;

    public AdminContrller(BookingRepository bookingRepository,
                          AdminService adminService,
                          BookingExpirationService bookingExpirationService,
                          FeedbackRepository feedbackRepository) {
        this.bookingRepository = bookingRepository;
        this.adminService = adminService;
        this.bookingExpirationService = bookingExpirationService;
        this.feedbackRepository = feedbackRepository;
    }

    // ===================== AUTH & LOGIN =====================
    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String adminLogin() {
        return "Login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("loginAdminuser", new LoginAdminuser());
        return "register";
    }

    @PostMapping("/saveUser")
    public String saveUser(@ModelAttribute("loginAdminuser") LoginAdminuser loginAdminuser,
                           RedirectAttributes redirectAttributes) {
        adminService.saveUser(loginAdminuser);
        redirectAttributes.addFlashAttribute("message", "User registered successfully!");
        return "redirect:/login";
    }

    @GetMapping("/default")
    public String redirectAfterLogin(Authentication auth) {
        if (auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin";
        }
        return "redirect:/dashboard";
    }

    // ===================== ADMIN DASHBOARD =====================
    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        LocalDate today = LocalDate.now();

        double dailyRevenue = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED)
                .mapToDouble(b -> b.getTotalFare() != null ? b.getTotalFare() : 0)
                .sum();

        model.addAttribute("dailyRevenue", dailyRevenue);
        model.addAttribute("totalRoutes", adminService.getAllRoutes().size());
        model.addAttribute("today", today);

        return "Admindashboard";
    }

    // ===================== ROUTE MANAGEMENT =====================
    @GetMapping("/AddRoute")
    public String showAddRouteForm(Model model) {
        model.addAttribute("route", new Route());
        return "addroute";
    }

    @PostMapping("/addRoute")
    public String saveRoute(@ModelAttribute("route") Route route,
                            RedirectAttributes redirectAttributes) {
        adminService.saveRoute(route);
        redirectAttributes.addFlashAttribute("message", "Route added successfully!");
        return "redirect:/admin";
    }

    @GetMapping("/viewRoutes")
    public String viewRoutes(Model model) {
        model.addAttribute("routes", adminService.getAllRoutes());
        return "routelist";
    }

    @GetMapping("/editRoute/{id}")
    public String editRoute(@PathVariable int id, Model model) {
        Route route = adminService.getRouteById(id);
        if (route == null) {
            model.addAttribute("error", "Route not found!");
            return "redirect:/viewRoutes";
        }
        model.addAttribute("route", route);
        return "editroute";
    }

    @PostMapping("/updateRoute")
    public String updateRoute(@ModelAttribute("route") Route route,
                              RedirectAttributes redirectAttributes) {
        adminService.saveRoute(route);
        redirectAttributes.addFlashAttribute("message", "Route updated successfully!");
        return "redirect:/viewRoutes";
    }

    @GetMapping("/deleteRoute/{id}")
    public String deleteRoute(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            Route route = adminService.getRouteById(id);
            if (route == null) {
                redirectAttributes.addFlashAttribute("error", "Route not found!");
            } else {
                adminService.deleteRouteById(id);
                redirectAttributes.addFlashAttribute("message",
                        "Route '" + route.getRouteName() + "' deleted successfully!");
            }
        } catch (Exception e) {
            System.err.println("Error deleting route: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                    "An error occurred while deleting the route. Please try again.");
        }
        return "redirect:/viewRoutes";
    }

    // ===================== USER DASHBOARD =====================
    @GetMapping("/dashboard")
    public String showUserDashboard(Model model) {
        LoginAdminuser currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<Booking> userBookings = bookingRepository.findByUserId((long) currentUser.getId());
        model.addAttribute("bookings", userBookings);
        model.addAttribute("username", currentUser.getUsername());

        return "dashboard";
    }

    // ===================== ADMIN: ALL BOOKINGS =====================
    @GetMapping("/Showbooking")
    public String showAllBookings(Model model) {
        int expiredCount = bookingExpirationService.markExpiredBookingsManually();
        if (expiredCount > 0) {
            System.out.println("Admin: Marked " + expiredCount + " expired bookings");
        }
        model.addAttribute("bookings", bookingRepository.findAll());
        return "showbooking";
    }

    // ===================== ADMIN: FEEDBACK MANAGEMENT =====================
    @GetMapping("/admin/feedback")
    public String showAllFeedback(Model model) {
        List<Feedback> allFeedback = feedbackRepository.findAll();

        long totalFeedback = allFeedback.size();
        double averageRating = allFeedback.stream()
                .mapToInt(Feedback::getRating)
                .average()
                .orElse(0.0);

        model.addAttribute("feedbacks", allFeedback);
        model.addAttribute("totalFeedback", totalFeedback);
        model.addAttribute("averageRating", String.format("%.1f", averageRating));
        model.addAttribute("routes", adminService.getAllRoutes());

        return "admin-feedback";
    }

    // ===================== HELPER METHOD =====================
    private LoginAdminuser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        return adminService.findByUsername(auth.getName());
    }
}
