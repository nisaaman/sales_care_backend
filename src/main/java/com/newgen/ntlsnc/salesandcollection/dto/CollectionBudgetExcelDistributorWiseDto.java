package com.newgen.ntlsnc.salesandcollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Newaz Sharif
 * @since 27th Aug, 22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectionBudgetExcelDistributorWiseDto {

    private String Product_Id;
    private String Product_Name;
    private String Distributor_Id;
    private String Distributor_Name;
    private String Product_Trade_price;
    private Double Jan;
    private Double Feb;
    private Double Mar;
    private Double Apr;
    private Double May;
    private Double Jun;
    private Double Jul;
    private Double Aug;
    private Double Sep;
    private Double Oct;
    private Double Nov;
    private Double Dec;
}
