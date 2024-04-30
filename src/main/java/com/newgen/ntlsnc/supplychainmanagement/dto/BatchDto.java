package com.newgen.ntlsnc.supplychainmanagement.dto;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * @author kamal
 * @Date ১৩/৪/২২
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchDto {
    private Long id;

    @NotNull(message = "Consignment No is required.")
    private String consignmentNo;
    @NotNull(message = "Batch Quantity is required.")
    private Integer batchQuantity;
    @NotNull(message = "Production Date field is required.")
    private String productionDate;

    @NotNull(message = "Company field is required.")
    private Long companyId;

    private Long organizationId;

    private String remarks;

    private BatchDetailsDto batchDetailsDto;
}
