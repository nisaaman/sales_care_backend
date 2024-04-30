package com.newgen.ntlsnc.globalsettings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SemesterDto {

    private Long id;

    @NotNull(message = "Semester Name field is required.")
    private String semesterName;

    @NotNull(message = "Start Date field is required.")
    private String startDate;

    @NotNull(message = "End Date field is required.")
    private String endDate;

    private Long organizationId;

    private Long accountingYearId;
}
