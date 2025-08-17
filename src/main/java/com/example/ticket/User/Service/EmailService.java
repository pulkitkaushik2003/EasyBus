package com.example.ticket.User.Service;

import com.example.ticket.User.Model.Booking;
import com.example.ticket.Paymentdetail.Model.PaymentDetails;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Send booking confirmation email
     */
    @Async
    public void sendBookingConfirmationEmail(Booking booking, PaymentDetails payment) {
        final String userEmail = booking.getUserEmail();
        final Long bookingId = booking.getId();

        log.info("Sending booking confirmation email to {} for booking ID {}", userEmail, bookingId);

        try {
            Context context = new Context();
            context.setVariable("booking", booking);
            context.setVariable("payment", payment);

            String htmlContent = templateEngine.process("booking-email", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(userEmail);
            helper.setSubject("Your Booking is Confirmed! Booking ID: " + bookingId);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);

            log.info("Booking confirmation email sent to {}", userEmail);

        } catch (MessagingException e) {
            log.error("Failed to send booking confirmation email to {} for booking ID {}: {}",
                    userEmail, bookingId, e.getMessage());
        }
    }

    /**
     * Send booking cancellation email
     */
    @Async
    public void sendBookingCancellationEmail(Booking booking) {
        final String userEmail = booking.getUserEmail();
        final Long bookingId = booking.getId();

        log.info("Sending booking cancellation email to {} for booking ID {}", userEmail, bookingId);

        try {
            Context context = new Context();
            context.setVariable("booking", booking);

           String htmlContent = templateEngine.process("booking-cancellation-email", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(userEmail);
            helper.setSubject("Your Booking has been Cancelled - ID: " + bookingId);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);

            log.info("Booking cancellation email sent to {}", userEmail);

        } catch (MessagingException e) {
            log.error("Failed to send booking cancellation email to {} for booking ID {}: {}",
                    userEmail, bookingId, e.getMessage());
        }
    }
}
