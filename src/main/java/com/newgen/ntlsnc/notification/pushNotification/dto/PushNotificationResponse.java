package com.newgen.ntlsnc.notification.pushNotification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author nisa
 * @Date ২৯/৮/২৩ , ১১:১১ AM
 */
@Data
@AllArgsConstructor
public class PushNotificationResponse {
    int httpStatus;
    String message;
}
