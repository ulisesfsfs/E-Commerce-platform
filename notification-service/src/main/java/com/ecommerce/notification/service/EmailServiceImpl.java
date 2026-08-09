package com.ecommerce.notification.service;

import com.ecommerce.notification.event.PaymentProcessedEvent;
import com.ecommerce.notification.event.StockReservationFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@ecommerce.com}")
    private String fromEmail;

    @Override
    public void sendPaymentSuccessNotification(String recipientEmail, PaymentProcessedEvent event) {
        String subject = "Order #" + event.getOrderId() + " Confirmed - Payment Approved";

        String body = "Hello,\n\n" +
                "Great news! Your payment of $" + event.getAmount() + " for Order #" + event.getOrderId() + " was processed successfully.\n\n" +
                "Payment Details:\n" +
                "- Payment Method: " + event.getPaymentMethod() + "\n" +
                "- Transaction Ref: " + event.getTransactionReference() + "\n" +
                "- Amount Paid: $" + event.getAmount() + "\n\n" +
                "Your order status is now CONFIRMED and is being prepared for shipment.\n\n" +
                "Best regards,\nE-Commerce Team";

        sendEmail(recipientEmail, subject, body);
    }

    @Override
    public void sendPaymentFailureNotification(String recipientEmail, PaymentProcessedEvent event) {
        String subject = "Order #" + event.getOrderId() + " Cancelled - Payment Failed";

        String body = "Hello,\n\n" +
                "Unfortunately, your payment of $" + event.getAmount() + " for Order #" + event.getOrderId() + " could not be processed.\n\n" +
                "Reason: Payment declined or transaction error.\n" +
                "Your order has been updated to CANCELLED. Please try placing your order again with a valid payment method.\n\n" +
                "Best regards,\nE-Commerce Team";

        sendEmail(recipientEmail, subject, body);
    }

    @Override
    public void sendStockFailureNotification(String recipientEmail, StockReservationFailedEvent event) {
        String subject = "Order #" + event.getOrderId() + " Cancelled - Out of Stock";

        String body = "Hello,\n\n" +
                "We regret to inform you that Order #" + event.getOrderId() + " was cancelled due to stock unavailability.\n\n" +
                "Reason: " + event.getReason() + "\n" +
                "No charges were made to your account.\n\n" +
                "Best regards,\nE-Commerce Team";

        sendEmail(recipientEmail, subject, body);
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            log.info("Sending email to '{}' with subject: '{}'", to, subject);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("Email successfully sent to '{}'", to);
        } catch (Exception e) {
            log.error("Failed to send email to '{}' (Reason: {}). Is SMTP configured correctly?", to, e.getMessage());
        }
    }
}
