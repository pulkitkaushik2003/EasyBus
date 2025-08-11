package com.example.ticket.User.Service;

import com.example.ticket.User.Model.Booking;
import com.example.ticket.User.Repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class BookingExpirationService {

    @Autowired
    private BookingRepository bookingRepository;

    /**
     * Scheduled task to run daily at 12:00 AM to mark expired bookings
     */
    @Scheduled(cron = "0 0 0 * * ?") // Run daily at midnight
    @Transactional
    public void markExpiredBookings() {
        LocalDate currentDate = LocalDate.now();
        int updatedCount = bookingRepository.markExpiredBookings(currentDate);
        
        if (updatedCount > 0) {
            System.out.println("Marked " + updatedCount + " bookings as expired");
        }
    }

    /**
     * Manually mark expired bookings (can be called from controllers)
     */
    @Transactional
    public int markExpiredBookingsManually() {
        LocalDate currentDate = LocalDate.now();
        return bookingRepository.markExpiredBookings(currentDate);
    }

    /**
     * Get all expired bookings
     */
    public List<Booking> getExpiredBookings() {
        return bookingRepository.findExpiredBookings(LocalDate.now());
    }

    /**
     * Check if a booking is expired
     */
    public boolean isBookingExpired(Booking booking) {
        return booking.getTravelDate() != null && booking.getTravelDate().isBefore(LocalDate.now());
    }

    /**
     * Get active bookings (not expired or cancelled)
     */
    public List<Booking> getActiveBookings() {
        return bookingRepository.findActiveBookings();
    }
}
