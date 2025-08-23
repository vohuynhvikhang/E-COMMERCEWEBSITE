package com.poly.asm.services;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailerService {

    @Autowired
    private JavaMailSender mailSender;

    // Gửi email đơn giản (không có file đính kèm)
    public void sendEmail(String to, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);  // Sử dụng HTML nếu cần

        mailSender.send(message);  // Gửi email và ném exception nếu có lỗi xảy ra
    }

    // Gửi email với file đính kèm
    public void sendEmailWithAttachment(String to, String subject, String body, String attachmentPath) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        try {
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);  // Sử dụng HTML nếu cần

            // Kiểm tra sự tồn tại của file đính kèm
            java.io.File attachmentFile = new java.io.File(attachmentPath);
            if (attachmentFile.exists()) {
                helper.addAttachment("Attachment", attachmentFile);
            } else {
                throw new MessagingException("Attachment file not found at: " + attachmentPath);
            }

            mailSender.send(message);  // Gửi email với file đính kèm

        } catch (Exception e) {
            // Nếu có lỗi, ném ra exception
            throw new MessagingException("Error sending email with attachment: " + e.getMessage(), e);
        }
    }
}
