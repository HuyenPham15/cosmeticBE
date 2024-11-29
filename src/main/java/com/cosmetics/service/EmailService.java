package com.cosmetics.service;

import com.cosmetics.dto.VerificationRequestDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

  private VerificationRequestDTO verificationRequestDTO;
    @Autowired
    private JavaMailSender mailSender;
    // Biến để lưu trữ mã xác nhận
    // Phương thức tạo mã xác nhận ngẫu nhiên 6 chữ số
    public String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // Tạo số ngẫu nhiên từ 100000 đến 999999
        return String.valueOf(code);
    }

    // Xác nhận đăng kí
    public void sendVerificationEmail(String toEmail) {
//        // Mã hóa email và code bằng Base64
//        String encodedEmail = Base64.getEncoder().encodeToString(toEmail.getBytes());
//        String encode = Base64.getEncoder().encodeToString(generateVerificationCode().getBytes());
        String subject = "Xác Nhận Đăng Ký Tài Khoản";
        String confirmationLink = "http://localhost:4200/confirm-register?email=" + toEmail;

        String body = "<html>" +
                "<head>" +
                "<style>" +
                "    body { font-family: Arial, sans-serif; }" +
                "    .button {" +
                "        background-color: #007bff;" +
                "        color: white;" +
                "        padding: 10px 20px;" +
                "        text-align: center;" +
                "        text-decoration: none;" +
                "        display: inline-block;" +
                "        border-radius: 5px;" +
                "        border: 1px solid #007bff;" +
                "        font-size: 16px;" +
                "        margin: 10px 0;" +
                "    }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<p>Vui lòng nhấn vào nút bên dưới để xác nhận đăng ký tài khoản của bạn:</p>" +
                "<a href='" + confirmationLink + "' target='_blank' class='button text-white'>Xác Nhận Tài Khoản</a>" +
                "<p>Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi!</p>" +
                "</body>" +
                "</html>";

        // Tạo MimeMessage để gửi email HTML
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
            logger.info("Email sent successfully to {}", toEmail);
        } catch (MessagingException e) {
            logger.error("Error while sending email: {}", e.getMessage());
        }
    }

    public void sendPasswordResetEmail(String toEmail, String verificationCode) {
        long expirationTime = System.currentTimeMillis() + (2 * 60 * 1000); // Thời gian hết hạn là 2 phút
        storeVerificationCode(toEmail, verificationCode, expirationTime); // Lưu mã xác nhận

        String subject = "Mã xác nhận đăng nhập của bạn";
        String body = "Mã xác nhận của bạn là: " + verificationCode + "\nMã này sẽ hết hạn sau 2 phút.";

        // Tạo MimeMessage để gửi email HTML
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true); // true để gửi email dưới dạng HTML
            mailSender.send(message);
            logger.info("Password reset email sent successfully to {}", toEmail);
        } catch (MessagingException e) {
            logger.error("Error while sending email: {}", e.getMessage());
        }
    }

    public boolean validateVerificationCode(VerificationRequestDTO request) {
        String email = request.getEmail();
        VerificationRequestDTO storedRequest = getStoredCodeForEmail(email);

        // Kiểm tra mã xác nhận và thời gian hết hạn
        if (storedRequest != null) {
            String storedCode = storedRequest.getCode();
            long expirationTime = storedRequest.getExpirationTime();

            // Kiểm tra mã xác nhận và thời gian hết hạn
            if (storedCode != null && storedCode.equals(request.getCode()) && expirationTime > System.currentTimeMillis()) {
                return true; // Mã xác nhận hợp lệ
            }
        }
        return false; // Mã xác nhận không hợp lệ hoặc đã hết hạn
    }

    private Map<String, VerificationRequestDTO> verificationCodes = new HashMap<>();

    public void storeVerificationCode(String email, String code, long expirationTime) {
        verificationCodes.put(email, new VerificationRequestDTO(email, code, expirationTime));
    }

    public VerificationRequestDTO getStoredCodeForEmail(String email) {
        return verificationCodes.get(email);
    }


}
