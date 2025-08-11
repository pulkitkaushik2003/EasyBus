package com.example.ticket.User.Service;

import com.example.ticket.Admin.Model.Route;
import com.example.ticket.User.Model.Booking;
import com.example.ticket.Admin.Repository.RouteRepository;
import com.example.ticket.User.Repository.BookingRepository;

import com.example.ticket.Admin.Model.LoginAdminuser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RouteRepository routeRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository, RouteRepository routeRepository) {
        this.bookingRepository = bookingRepository;
        this.routeRepository = routeRepository;
    }

    @Transactional // Use Spring's annotation for proper transaction management with Spring Data JPA
    public Booking createBooking(int seatsToBook, int routeId, LoginAdminuser currentUser) {
        // Lock the route row for update to prevent race conditions
        Route route = routeRepository.findByIdForUpdate(routeId)
                .orElseThrow(() -> new com.example.ticket.User.exception.ResourceNotFoundException("Route with ID " + routeId + " not found!"));

        // Validate seats
        if (seatsToBook <= 0) {
            throw new com.example.ticket.User.exception.BookingException("Number of seats to book must be positive.");
        }
        if (seatsToBook > route.getAvailableSeat()) {
            throw new com.example.ticket.User.exception.BookingException("Not enough seats available. Only "
                    + route.getAvailableSeat() + " seats left.");
        }

        // Create booking
        Booking booking = new Booking(route, currentUser, seatsToBook);

        // Save booking
        Booking savedBooking = bookingRepository.save(booking);

        // Update available seats
        route.setAvailableSeat(route.getAvailableSeat() - seatsToBook);
        // Because the 'route' object is a managed entity within a transaction, this change will be automatically saved to the database on commit.

        return savedBooking;
    }

    public Optional<Booking> findBookingByIdAndUser(Long bookingId, LoginAdminuser currentUser) {
        return bookingRepository.findById(bookingId)
                .filter(booking -> booking.getUserId().equals((long) currentUser.getId()));
    }

    public List<Booking> findBookingsByUser(LoginAdminuser currentUser) {
        return bookingRepository.findByUserId((long) currentUser.getId());
    }
}
