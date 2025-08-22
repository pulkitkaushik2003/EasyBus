package com.example.ticket.User.Repository;

import com.example.ticket.User.Model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    
    // Find all feedback by user ID
    List<Feedback> findByUserId(Long userId);
    
    // Find feedback by booking ID
    Optional<Feedback> findByBookingId(Long bookingId);
    
    // Find all feedback for a specific route
    List<Feedback> findByRouteName(String routeName);
    
    // Find feedback by rating range
    List<Feedback> findByRatingBetween(Integer minRating, Integer maxRating);
    
    // Get average rating for a route
    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.routeName = :routeName")
    Double findAverageRatingByRouteName(@Param("routeName") String routeName);
    
    // Get feedback count by route
    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.routeName = :routeName")
    Long countByRouteName(@Param("routeName") String routeName);
    
    // Get recent feedback with pagination
    List<Feedback> findTop10ByOrderByCreatedAtDesc();
    
    // Check if feedback exists for booking
    boolean existsByBookingId(Long bookingId);
    
    // Find feedback by user ID and booking ID
    Optional<Feedback> findByUserIdAndBookingId(Long userId, Long bookingId);
}
