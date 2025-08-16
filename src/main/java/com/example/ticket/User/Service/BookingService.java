package com.example.ticket.User.Service;

import com.example.ticket.Admin.Model.Route;
import com.example.ticket.Admin.Model.LoginAdminuser;
import com.example.ticket.Admin.Repository.RouteRepository;
import com.example.ticket.User.Model.Booking;
import com.example.ticket.User.Repository.BookingRepository;
import com.example.ticket.User.exception.BookingException;
import com.example.ticket.User.exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // ------------ CREATE NEW BOOKING ------------
    @Transactional
    public Booking createBooking(int seatsToBook, int routeId, LoginAdminuser currentUser) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route with ID " + routeId + " not found!"));

        if (seatsToBook <= 0) {
            throw new BookingException("Number of seats to book must be positive.");
        }
        if (seatsToBook > route.getAvailableSeat()) {
            throw new BookingException(
                    "Not enough seats available. Only " + route.getAvailableSeat() + " seats left."
            );
        }

        Booking booking = new Booking(route, currentUser, seatsToBook);
        booking.setStatus(Booking.BookingStatus.PENDING);

        Booking savedBooking = bookingRepository.save(booking);

        route.setAvailableSeat(route.getAvailableSeat() - seatsToBook);
        routeRepository.save(route);

        return savedBooking;
    }

    // ------------ FIND BY ID ------------
    public Optional<Booking> findBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId);
    }

    // ------------ FIND BY USER ------------
    public List<Booking> findBookingsByUser(LoginAdminuser currentUser) {
        return bookingRepository.findByUserId((long) currentUser.getId());
    }

    // ------------ FIND BY ID AND USER ------------
    public Optional<Booking> findBookingByIdAndUser(Long bookingId, LoginAdminuser user) {
        return bookingRepository.findById(bookingId)
                .filter(booking -> booking.getUserId().equals((long) user.getId()));
    }

    // ------------ CONFIRM CASH PAYMENT ------------
    @Transactional
    public void confirmCashBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new BookingException("Booking cannot be confirmed because its status is " +
                    booking.getStatus() + ", not PENDING.");
        }

        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
    }

    // ------------ MARK AS PAID (Card/UPI) ------------
    @Transactional
    public void markAsPaid(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getStatus() == Booking.BookingStatus.PENDING) {
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
        }
    }

    // ------------ CANCEL BOOKING ------------
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new BookingException("Booking is already cancelled.");
        }

        // Restore available seats
        Route route = routeRepository.findById(Math.toIntExact(booking.getRouteId()))
                .orElseThrow(() -> new ResourceNotFoundException("Route not found for booking"));

        route.setAvailableSeat(route.getAvailableSeat() + booking.getSeatsBooked());
        routeRepository.save(route);

        // Set booking status to CANCELLED
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }
}
