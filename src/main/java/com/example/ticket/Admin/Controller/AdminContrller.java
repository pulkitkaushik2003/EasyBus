package com.example.ticket.Admin.Controller;

import com.example.ticket.Admin.Model.LoginAdminuser;
import com.example.ticket.Admin.Model.Route;
import com.example.ticket.Admin.Service.AdminService;
import com.example.ticket.User.Repository.BookingRepository;
import com.example.ticket.User.Service.BookingExpirationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AdminContrller {

    private final BookingRepository bookingRepository;
    private final AdminService adminService;
    private final BookingExpirationService bookingExpirationService;

    @Autowired
    public AdminContrller(BookingRepository bookingRepository, AdminService adminService, BookingExpirationService bookingExpirationService) {
        this.bookingRepository = bookingRepository;
        this.adminService = adminService;
        this.bookingExpirationService = bookingExpirationService;
    }

    @GetMapping("/")
    public String home() {
        return "Login";
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
    public String saveUser(@ModelAttribute("loginAdminuser") LoginAdminuser loginAdminuser, RedirectAttributes redirectAttributes) {
        adminService.saveUser(loginAdminuser);
        redirectAttributes.addFlashAttribute("message", "User registered successfully!");
        return "redirect:/login";
    }
    @GetMapping("/dashboard")
    public String showdash(){
        return "dashboard";
    }

    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        int totalRoutes = adminService.getAllRoutes().size();
        model.addAttribute("totalRoutes", totalRoutes);

        return "Admindashboard";
    }

    @GetMapping("/AddRoute")
    public String showAddRouteForm(Model model) {
        model.addAttribute("route", new Route());
        return "addroute";
    }

    @PostMapping("/addRoute")
    public String saveRoute(@ModelAttribute("route") Route route, RedirectAttributes redirectAttributes) {
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
        model.addAttribute("route", route);
        return "editroute";
    }

    @PostMapping("/updateRoute")
    public String updateRoute(@ModelAttribute("route") Route route, RedirectAttributes redirectAttributes) {
        adminService.saveRoute(route);
        redirectAttributes.addFlashAttribute("message", "Route updated successfully!");
        return "redirect:/viewRoutes";
    }

    @GetMapping("/deleteRoute/{id}")
    public String deleteRoute(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            // Check if route exists
            Route route = adminService.getRouteById(id);
            if (route == null) {
                redirectAttributes.addFlashAttribute("error", "Route not found!");
                return "redirect:/viewRoutes";
            }
            
            // Delete the route (this will also handle related bookings)
            adminService.deleteRouteById(id);
            redirectAttributes.addFlashAttribute("message", "Route '" + route.getRouteName() + "' has been deleted successfully!");
            
        } catch (Exception e) {
            System.err.println("Error deleting route: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "An error occurred while deleting the route. Please try again.");
        }
        
        return "redirect:/viewRoutes";
    }

    @GetMapping("/default")
    public String redirectAfterLogin(org.springframework.security.core.Authentication auth) {
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin";
        } else {
            return "redirect:/dashboard";
        }
    }
    
    @GetMapping("/Showbooking")
    public String showBooking(Model model) {
        // Note: This logic changes state and is not idempotent.
        // It is better to move this to a @Scheduled task for background processing.
        int expiredCount = bookingExpirationService.markExpiredBookingsManually();
        if (expiredCount > 0) {
            System.out.println("Admin: Marked " + expiredCount + " expired bookings");
        }
        
        model.addAttribute("booking", bookingRepository.findAll());
        return "showbooking";
    }
}
