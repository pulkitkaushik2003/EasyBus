package com.example.ticket.User.Model;

import jakarta.persistence.*;
import com.example.ticket.Admin.Model.Route;
import com.example.ticket.Admin.Model.LoginAdminuser;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private Long userId; // Add user ID to associate booking with specific user

    @Column(nullable = false)
    private Long routeId;

    @Column(nullable = false)
    private String routeName;

    @Column(nullable = false)
    private String fromLocation;

    @Column(nullable = false)
    private String toLocation;

    @Column(nullable = false)
    private LocalDate travelDate;

    @Column(nullable = false)
    private LocalTime departureTime;

    @Column(nullable = false)
    private Double fare;

    @Column(nullable = false)
    private Integer seatsBooked;

    @Column(nullable = false)
    private LocalDateTime bookingTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false)
    private BookingStatus status;

    public enum BookingStatus {
        CONFIRMED, CANCELLED, PENDING, EXPIRED
    }

    // Constructors

    /**
     * Constructs a new Booking based on a selected route and the current user.
     * @param route The route being booked.
     * @param user The user making the booking.
     * @param seats The number of seats to book.
     */
    public Booking(Route route, LoginAdminuser user, Integer seats) {
    this.userName = user.getUsername();
    this.userEmail = user.getEmail();
    this.userId = (long) user.getId();
    this.routeId = (long) route.getId();
    this.routeName = route.getRouteName();
    this.fromLocation = route.getFromLocation();
    this.toLocation = route.getDestination();
    this.travelDate = route.getDate();
    this.departureTime = route.getDepartureTime();
    this.fare = route.getFare();
    this.seatsBooked = seats;
    this.bookingTime = LocalDateTime.now(); // ✅ set booking time
    this.status = BookingStatus.CONFIRMED;  // ✅ set default status
}


    /**
     * Calculates the total fare for the booking.
     * Marked as @Transient so JPA doesn't try to map it to a database column.
     * @return The total fare as a Double.
     */
    @Transient
    public Double getTotalFare() {
        if (this.fare == null || this.seatsBooked == null) {
            return 0.0;
        }
        return this.fare * this.seatsBooked;
    }
}
