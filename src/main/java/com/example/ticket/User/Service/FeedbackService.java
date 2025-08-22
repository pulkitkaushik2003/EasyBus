package com.example.ticket.User.Service;

import com.example.ticket.User.Model.Feedback;
import com.example.ticket.User.Repository.FeedbackRepository;
import com.example.ticket.Admin.Model.LoginAdminuser;
import com.example.ticket.User.Model.Booking;
import com.example.ticket.User.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    // Submit new feedback
    public Feedback submitFeedback(LoginAdminuser user, Booking booking, Integer rating, String comments) {
        // Validate rating
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5 stars");
        }

        // Check if feedback already exists for this booking
        if (feedbackRepository.existsByBookingId(booking.getId())) {
            throw new IllegalStateException("Feedback already submitted for this booking");
        }

        // Create and save feedback
        Feedback feedback = new Feedback(user, booking, rating, comments);
        return feedbackRepository.save(feedback);
    }

    // Get feedback by ID
    public Feedback getFeedbackById(Long feedbackId) {
        return feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found with id: " + feedbackId));
    }

    // Get all feedback by user
    public List<Feedback> getUserFeedback(Long userId) {
        return feedbackRepository.findByUserId(userId);
    }

    // Get feedback for a specific booking
    public Optional<Feedback> getFeedbackByBookingId(Long bookingId) {
        return feedbackRepository.findByBookingId(bookingId);
    }

    // Update existing feedback
    public Feedback updateFeedback(Long feedbackId, Integer rating, String comments) {
        Feedback feedback = getFeedbackById(feedbackId);
        
        if (rating != null) {
            if (rating < 1 || rating > 5) {
                throw new IllegalArgumentException("Rating must be between 1 and 5 stars");
            }
            feedback.setRating(rating);
        }
        
        if (comments != null) {
            feedback.setComments(comments);
        }
        
        return feedbackRepository.save(feedback);
    }

    // Delete feedback
    public void deleteFeedback(Long feedbackId) {
        if (!feedbackRepository.existsById(feedbackId)) {
            throw new ResourceNotFoundException("Feedback not found with id: " + feedbackId);
        }
        feedbackRepository.deleteById(feedbackId);
    }

    // Get average rating for a route
    public Double getAverageRatingForRoute(String routeName) {
        Double average = feedbackRepository.findAverageRatingByRouteName(routeName);
        return average != null ? Math.round(average * 10.0) / 10.0 : 0.0; // Round to 1 decimal place
    }

    // Get feedback count for a route
    public Long getFeedbackCountForRoute(String routeName) {
        return feedbackRepository.countByRouteName(routeName);
    }

    // Get recent feedback
    public List<Feedback> getRecentFeedback() {
        return feedbackRepository.findTop10ByOrderByCreatedAtDesc();
    }

    // Check if user can submit feedback for a booking
    public boolean canSubmitFeedback(Long bookingId) {
        return !feedbackRepository.existsByBookingId(bookingId);
    }

    // Get feedback statistics for user
    public FeedbackStats getUserFeedbackStats(Long userId) {
        List<Feedback> userFeedbacks = feedbackRepository.findByUserId(userId);
        
        double averageRating = userFeedbacks.stream()
                .mapToInt(Feedback::getRating)
                .average()
                .orElse(0.0);
        
        return new FeedbackStats(
            userFeedbacks.size(),
            Math.round(averageRating * 10.0) / 10.0,
            userFeedbacks.stream().filter(f -> f.getRating() >= 4).count()
        );
    }

    // Statistics class
    public static class FeedbackStats {
        private final long totalFeedbacks;
        private final double averageRating;
        private final long positiveFeedbacks;

        public FeedbackStats(long totalFeedbacks, double averageRating, long positiveFeedbacks) {
            this.totalFeedbacks = totalFeedbacks;
            this.averageRating = averageRating;
            this.positiveFeedbacks = positiveFeedbacks;
        }

        public long getTotalFeedbacks() { return totalFeedbacks; }
        public double getAverageRating() { return averageRating; }
        public long getPositiveFeedbacks() { return positiveFeedbacks; }
    }
}
