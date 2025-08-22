package com.example.ticket.User.Model;

import jakarta.persistence.*;
import com.example.ticket.Admin.Model.LoginAdminuser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
@Getter
@Setter
@NoArgsConstructor
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private Long bookingId;

    @Column(nullable = false)
    private String routeName;

    @Column(nullable = false)
    private String fromLocation;

    @Column(nullable = false)
    private String toLocation;

    @Column(nullable = false)
    private Integer rating; // 1-5 stars

    @Column(length = 1000)
    private String comments;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    // Constructors
    public Feedback(LoginAdminuser user, Booking booking, Integer rating, String comments) {
        this.userId = (long) user.getId();
        this.userName = user.getUsername();
        this.userEmail = user.getEmail();
        this.bookingId = booking.getId();
        this.routeName = booking.getRouteName();
        this.fromLocation = booking.getFromLocation();
        this.toLocation = booking.getToLocation();
        this.rating = rating;
        this.comments = comments;
        this.createdAt = LocalDateTime.now();
    }

    // Pre-update method to set updated timestamp
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Validation method
    public boolean isValidRating() {
        return rating != null && rating >= 1 && rating <= 5;
    }
}
