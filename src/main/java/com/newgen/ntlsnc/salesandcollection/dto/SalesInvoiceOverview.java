package com.newgen.ntlsnc.salesandcollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author Newaz Sharif
 * @since 9th Aug,22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesInvoiceOverview {
    private double totalReceivable;
    private List<Map<String,Object>> salesInvoiceList;
}
