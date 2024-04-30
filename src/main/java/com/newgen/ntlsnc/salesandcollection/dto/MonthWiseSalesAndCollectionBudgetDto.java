package com.newgen.ntlsnc.salesandcollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author anika
 * @Date ১৩/৪/২২
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthWiseSalesAndCollectionBudgetDto {

    private Long id;

    @NotNull(message = "Budget Date  field is required.")
    private String budgetDate;

    @NotNull(message = "Month field is required.")
    private Integer month;

    @NotNull(message = "Year field is required.")
    private Integer year;
    private Double targetValue;

    @NotNull(message = "Approval Status  field is required.")
    private String approvalStatus;
    private String targetType;
    private Long companyId;
    private Long distributorId;  // Bhai bhai
    private Long salesOfficerId; // null
    private Long locationId; // null

    private List<MonthWiseSalesAndCollectionBudgetDetailsDto> monthWiseSalesAndCollectionBudgetDetailsDtoList;

}
