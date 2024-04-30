package com.newgen.ntlsnc.salesandcollection.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.salesandcollection.service.SalesOrderOverViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author Newaz Sharif
 * @since 9th June,22
 */

@RestController
@RequestMapping("/api/sales-order-data")
public class SalesOrderOverViewController {

    @Autowired
    SalesOrderOverViewService salesOrderOverViewService;

    @GetMapping("/over-view")
    public ResponseEntity<?> getSalesOrderOverView(
            @RequestParam Long userLoginId, @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) Long accountingYearId,
            @RequestParam Long companyId) {

        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(salesOrderOverViewService.getSalesOrderOverView(userLoginId,
                    locationId, accountingYearId, companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/details-view/{salesOrderId}")
    public ResponseEntity<?> getSalesOrderDetailsView(@PathVariable Long salesOrderId) {

        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(salesOrderOverViewService.getSalesOrderDetailsView(salesOrderId));
        } catch (Exception ex) {
            ex.printStackTrace();
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
