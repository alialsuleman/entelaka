package com.ali.antelaka.mail;

import com.ali.antelaka.mail.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

//    @GetMapping("/send")
//    public String sendOtp(@RequestParam String to) {
//        emailService.sendEmail(to, "OTP Verification", "Your OTP code is: 123456");
//        return "Email sent!";
//    }
}
