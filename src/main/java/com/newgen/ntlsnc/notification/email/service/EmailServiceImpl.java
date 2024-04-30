package com.newgen.ntlsnc.notification.email.service;

import com.newgen.ntlsnc.common.FIleUtility;
import com.newgen.ntlsnc.common.FileUploadService;
import com.newgen.ntlsnc.notification.email.dto.EmailDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;

/**
 * @author nisa
 * @Date ৩০/৮/২৩ , ৯:৫৬ AM
 */
@Service
public class EmailServiceImpl implements EmailService {

    private Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    FileUploadService fileUploadService;

    @Value("${spring.mail.username}")
    private String sender;

    @Override
    public String sendSimpleMail(EmailDto emailDto) {
        // Try block to check for exceptions
        try {

            // Creating a simple mail message
            SimpleMailMessage mailMessage
                    = new SimpleMailMessage();

            // Setting up necessary details
            mailMessage.setFrom(sender);
            mailMessage.setTo(emailDto.getRecipient());
            mailMessage.setText(emailDto.getMsgBody());
            mailMessage.setSubject(emailDto.getSubject());

            // Sending the mail
            javaMailSender.send(mailMessage);
            return "Mail Sent Successfully...";
        }

        // Catch block to handle the exceptions
        catch (Exception e) {
            logger.error(e.getMessage());
            return "Error while Sending Mail";
        }
    }

    @Override
    public String sendMailWithAttachment(EmailDto emailDto) {
        // Creating a mime message
        MimeMessage mimeMessage
                = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper;

        try {

            // Setting multipart as true for attachments to
            // be send
            mimeMessageHelper
                    = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(sender);
            mimeMessageHelper.setTo(emailDto.getRecipient());
            mimeMessageHelper.setText(emailDto.getMsgBody());
            mimeMessageHelper.setSubject(
                    emailDto.getSubject());

            // Adding the attachment
            MultipartFile attachment = emailDto.getAttachment();
            String fileName = fileUploadService.getFileNameWithoutExtension(
                    attachment.getOriginalFilename())
                    + "." +fileUploadService.getFileExtension(attachment.getOriginalFilename());
            mimeMessageHelper.addAttachment(fileName,  FIleUtility.convertMultiPartToFile(attachment));

            // Sending the mail
            javaMailSender.send(mimeMessage);
            return "Mail sent Successfully with attachment";
        }

        // Catch block to handle MessagingException
        catch (MessagingException e) {
            logger.error(e.getMessage());
            // Display message when exception occurred
            return "Error while sending mail!!!";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
