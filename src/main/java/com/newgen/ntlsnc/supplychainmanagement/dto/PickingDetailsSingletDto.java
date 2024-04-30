package com.newgen.ntlsnc.supplychainmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PickingDetailsSingletDto {
    private  Long pickingDetailsId;
    private  Integer goodQty;
    private  Integer badQty;
    private  String reason;
}
