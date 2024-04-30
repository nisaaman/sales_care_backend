package com.newgen.ntlsnc.supplychainmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PickingDetailsListDto {
    private Long pickingId;
    private List<PickingDetailsSingletDto> pickingDetailsSingletDtoList;
    private String status;
}
