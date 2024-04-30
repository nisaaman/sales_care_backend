package com.newgen.ntlsnc.supplychainmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author kamal
 * @Date ১৮/৪/২২
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PickingDto {
    private Long id;

    @NotNull(message = "Company field is required.")
    private Long companyId;

    private String reason;

    private String status;

    private List<PickingDetailsDto> pickingDetailsDtoList;
}
