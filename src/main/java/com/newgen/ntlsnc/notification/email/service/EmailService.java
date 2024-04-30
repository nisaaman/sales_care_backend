package com.newgen.ntlsnc.notification.email.service;

import com.newgen.ntlsnc.notification.email.dto.EmailDto;

/**
 * @author nisa
 * @Date ৩০/৮/২৩ , ১০:০২ AM
 */
public interface EmailService {

    // To send a simple email
    String sendSimpleMail(EmailDto emailDto);

    // To send an email with attachment
    String sendMailWithAttachment(EmailDto emailDto);
}
