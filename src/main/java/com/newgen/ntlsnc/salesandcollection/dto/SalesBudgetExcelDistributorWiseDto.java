package com.newgen.ntlsnc.salesandcollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Newaz Sharif
 * @since 18th Aug,22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesBudgetExcelDistributorWiseDto {

    private String Product_Id;
    private String Product_Name;
    private String Distributor_Id;
    private String Distributor_Name;
    private String Manufacturing_Cost;
    private String Product_Trade_price;
    private Integer Jan;
    private Integer Feb;
    private Integer Mar;
    private Integer Apr;
    private Integer May;
    private Integer Jun;
    private Integer Jul;
    private Integer Aug;
    private Integer Sep;
    private Integer Oct;
    private Integer Nov;
    private Integer Dec;

}
