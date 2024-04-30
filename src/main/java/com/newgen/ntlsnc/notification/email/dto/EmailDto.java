package com.newgen.ntlsnc.notification.email.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author nisa
 * @Date ৩০/৮/২৩ , ৯:৫৬ AM
 */
@Data
@NoArgsConstructor
public class EmailDto {
    private String recipient;
    private String subject;
    private String msgBody;
    private MultipartFile attachment;
}
