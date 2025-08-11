package com.example.ticket.User.Repository;

import com.example.ticket.User.Model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    
    // Find bookings by user email
    List<Booking> findByUserEmail(String userEmail);
    
    // Find bookings by user ID
    List<Booking> findByUserId(Long userId);
    
    // Find bookings by user ID and status
    List<Booking> findByUserIdAndStatus(Long userId, Booking.BookingStatus status);
    
    // Find bookings by route ID
    List<Booking> findByRouteId(Long routeId);
    
    // Find bookings by status
    List<Booking> findByStatus(Booking.BookingStatus status);
    
    // Find bookings by user email and status
    List<Booking> findByUserEmailAndStatus(String userEmail, Booking.BookingStatus status);
    
    // Find expired bookings (travel date is before today)
    @Query("SELECT b FROM Booking b WHERE b.travelDate < :currentDate AND b.status IN ('CONFIRMED', 'PENDING')")
    List<Booking> findExpiredBookings(@Param("currentDate") LocalDate currentDate);
    
    // Update expired bookings status
    @Modifying
    @Query("UPDATE Booking b SET b.status = 'EXPIRED' WHERE b.travelDate < :currentDate AND b.status IN ('CONFIRMED', 'PENDING')")
    int markExpiredBookings(@Param("currentDate") LocalDate currentDate);
    
    // Find active bookings (not expired or cancelled)
    @Query("SELECT b FROM Booking b WHERE b.status IN ('CONFIRMED', 'PENDING')")
    List<Booking> findActiveBookings();
    
    // Custom query to find total seats booked for a route
    @Query("SELECT COALESCE(SUM(b.seatsBooked), 0) FROM Booking b WHERE b.routeId = :routeId AND b.status = 'CONFIRMED'")
    Integer getTotalBookedSeatsForRoute(@Param("routeId") Long routeId);
    
    // Find bookings by travel date
    @Query("SELECT b FROM Booking b WHERE b.travelDate = :travelDate")
    List<Booking> findByTravelDate(@Param("travelDate") LocalDate travelDate);
}
