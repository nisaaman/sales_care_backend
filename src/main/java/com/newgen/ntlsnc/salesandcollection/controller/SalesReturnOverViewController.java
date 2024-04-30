package com.newgen.ntlsnc.salesandcollection.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.salesandcollection.service.SalesReturnOverViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author Newaz Sharif
 * @since 14th June,22
 */
@RestController
@RequestMapping("/api/sales-return-data")
public class SalesReturnOverViewController {

    @Autowired
    SalesReturnOverViewService salesReturnOverViewService;

    @GetMapping("/overView")
    public ResponseEntity<?> getSalesReturnOverView(
            @RequestParam Long userLoginId, @RequestParam(required = false) Long locationId,
            @RequestParam Long accountingYearId,
            @RequestParam Long companyId) {

        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(salesReturnOverViewService.getSalesReturnOverView(userLoginId,
                    locationId, accountingYearId, companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/salesReturn/{userLoginId}/{locationId}/{accountingYearId}/{companyId}")
    public ResponseEntity<?> getSalesReturnData(
            @PathVariable Long userLoginId, @PathVariable Long locationId,
            @PathVariable Long accountingYearId,
            @PathVariable Long companyId) {

        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(salesReturnOverViewService.getSalesReturnData(userLoginId,
                    locationId, accountingYearId, companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
