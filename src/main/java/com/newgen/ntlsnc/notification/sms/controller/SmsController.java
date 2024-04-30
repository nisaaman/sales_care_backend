package com.newgen.ntlsnc.notification.sms.controller;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.newgen.ntlsnc.notification.sms.service.SmsService;
import com.newgen.ntlsnc.notification.sms.dto.SmsDto;
import org.springframework.web.bind.annotation.*;

/**
 * @author nisa
 * @Date ২৯/৮/২৩ , ৫:৪৭ PM
 */
@RestController
public class SmsController {

    final private SmsService smsService;


    public SmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    @RequestMapping("/send-sms")
    @ResponseBody
    public String send(@RequestBody SmsDto smsDto) throws FirebaseMessagingException {
        return smsService.sendSms(smsDto);
    }

}
