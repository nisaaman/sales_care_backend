package com.newgen.ntlsnc.salesandcollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author Newaz Sharif
 * @since 12th June,22
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesOrderDetailsView {

    Map<String, Object> salesOrderLifeCycle;
    List<Map<String, Object>> salesOrderDetails;
    Map<String, Object> salesOrderSummary;
    Map<String, Object> distributorInfo;
}
