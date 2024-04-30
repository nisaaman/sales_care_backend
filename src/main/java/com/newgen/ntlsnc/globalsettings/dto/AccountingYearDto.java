package com.newgen.ntlsnc.globalsettings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountingYearDto {

    private Long id;

    @NotNull(message = "Fiscal Year name field is required.")
    private String fiscalYearName;

    @NotNull(message = "Start Date field is required.")
    private String startDate;

    @NotNull(message = "End Date field is required.")
    private String endDate;

    @NotNull(message = "Company field is required.")
    private Long companyId;

    private List<SemesterDto> semesterList;
}
