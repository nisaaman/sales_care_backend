package com.newgen.ntlsnc.notification.email.controller;

import com.newgen.ntlsnc.notification.email.dto.EmailDto;
import com.newgen.ntlsnc.notification.email.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author nisa
 * @Date ৩০/৮/২৩ , ৯:৫৬ AM
 */
@RestController
public class EmailController {
    @Autowired
    private EmailService emailService;

    // Sending a simple Email
    @PostMapping("/sendMail")
    public String sendMail(@RequestBody EmailDto emailDto) {
        return emailService.sendSimpleMail(emailDto);
    }

    // Sending email with attachment
    @PostMapping("/sendMailWithAttachment")
    public String sendMailWithAttachment(@ModelAttribute EmailDto emailDto) {
        return emailService.sendMailWithAttachment(emailDto);
    }
}
