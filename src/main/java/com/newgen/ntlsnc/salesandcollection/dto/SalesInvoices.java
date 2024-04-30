package com.newgen.ntlsnc.salesandcollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Newaz Sharif
 * @since 10th Aug,22
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesInvoices {

    private String locationName;
    private Double totalInvoiceAmount;
    private Double totalInvoiceBalance;
    private Double totalOrdAmount;
    private Double totalOverdueAmount;

}
