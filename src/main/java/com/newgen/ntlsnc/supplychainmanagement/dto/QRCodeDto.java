package com.newgen.ntlsnc.supplychainmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author Newaz Sharif
 * @since 30th Aug, 22
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QRCodeDto {

    private Map<String, Object> batchDetails;
    private String mqrByte;
    private String iqrByte;
    private Integer iqrPrintQuantity;
    private Integer mqrPrintQuantity;

}
