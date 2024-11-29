package com.cosmetics.restcontroller;

import com.cosmetics.dto.ShareRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin("*")
public class ShareController {

    @Autowired
    private MailSender mailSender;

    @PostMapping("/email")
    public ResponseEntity<String> shareViaEmail(@RequestBody ShareRequest shareRequest) {
        String email = shareRequest.getEmail();
        String body = shareRequest.getBody();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Chia sẻ sản phẩm");
        message.setText(body);

        try {
            mailSender.send(message);
            return ResponseEntity.ok("Email sent successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error sending email: " + e.getMessage());
        }
    }

}