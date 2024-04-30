package com.newgen.ntlsnc.notification.sms.service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.newgen.ntlsnc.notification.sms.dto.SmsDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author nisa
 * @Date ২৯/৮/২৩ , ৫:৪৮ PM
 */
@Service
public class SmsService {

    private Logger logger = LoggerFactory.getLogger(SmsService.class);

    public String sendSms(SmsDto smsDto) {
       try {
           HttpResponse<String> response = Unirest.post("https://api.sms.net.bd/sendsms")
                    .field("api_key", "1kNdfk02XN8rKExz1nEJA35azn0r2SVc0NxXAk6b")
                   .field("msg", smsDto.getMessage())
                   .field("to", smsDto.getPhoneNumber())
                    .asString();

           System.out.println(response.getBody());
           return response.getBody();
       } catch (UnirestException e) {
           logger.error(e.getMessage());
           throw new RuntimeException(e);
        }
    }
}
