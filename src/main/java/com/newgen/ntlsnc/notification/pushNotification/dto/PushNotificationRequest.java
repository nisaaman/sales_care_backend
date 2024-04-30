package com.newgen.ntlsnc.notification.pushNotification.dto;

import lombok.Data;

/**
 * @author nisa
 * @Date ২৯/৮/২৩ , ১১:১০ AM
 */
@Data
public class PushNotificationRequest {
    String topic;
    String token;
    String title;
    String message;
    String image;
}
