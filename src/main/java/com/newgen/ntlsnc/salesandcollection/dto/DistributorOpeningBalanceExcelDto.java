package com.newgen.ntlsnc.salesandcollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Tanjela Aman
 * @since 8th Aug,23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DistributorOpeningBalanceExcelDto {

    private Long Distributor_Id;
    private String Distributor_Name;
    private Float Credit;
    private Float Cash;
    private Float Advance;
    private String Reference_No;
    private Float Balance;
}
