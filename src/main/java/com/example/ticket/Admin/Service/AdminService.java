package com.example.ticket.Admin.Service;

import com.example.ticket.Admin.Model.LoginAdminuser;
import com.example.ticket.Admin.Model.Route;
import com.example.ticket.Admin.Repository.Adminrepo;
import com.example.ticket.Admin.Repository.RouteRepository;
import com.example.ticket.User.Repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService implements UserDetailsService {

    @Autowired
    private Adminrepo adminrepo;

    @Autowired
    private RouteRepository routeRepository;
    
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LoginAdminuser user = adminrepo.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        return User.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .roles(user.getRole())
            .build();
    }

    public LoginAdminuser findByUsername(String username) {
        return adminrepo.findByUsername(username);
    }

    public void saveUser(LoginAdminuser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        adminrepo.save(user);
    }

    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    public void saveRoute(Route route) {
        routeRepository.save(route);
    }

    public Route getRouteById(int id) {
        return routeRepository.findById(id).orElse(null);
    }

    @Transactional
    public void deleteRouteById(int id) {
        // First, check if there are any bookings for this route
        List<com.example.ticket.User.Model.Booking> relatedBookings = bookingRepository.findByRouteId((long) id);
        
        if (!relatedBookings.isEmpty()) {
            // If there are bookings, mark them as cancelled instead of deleting
            for (com.example.ticket.User.Model.Booking booking : relatedBookings) {
                booking.setStatus(com.example.ticket.User.Model.Booking.BookingStatus.CANCELLED);
                bookingRepository.save(booking);
            }
            System.out.println("Marked " + relatedBookings.size() + " bookings as cancelled for route ID: " + id);
        }
        
        // Now delete the route
        routeRepository.deleteById(id);
        System.out.println("Deleted route with ID: " + id);
    }
}