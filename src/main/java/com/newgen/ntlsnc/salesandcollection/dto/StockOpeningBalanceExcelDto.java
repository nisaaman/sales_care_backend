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
public class StockOpeningBalanceExcelDto {

    private Long Id;
    private String Category;
    private String Sub_Category;
    private Long Product_Id;
    private String Product_Name;
    private String Product_Sku;
    private Integer Pack_Size;
    private Integer Carton_Size;
    private String UoM;
    private Integer Opening_Stock;
    private Float Rate;
    private Float Value;

}
