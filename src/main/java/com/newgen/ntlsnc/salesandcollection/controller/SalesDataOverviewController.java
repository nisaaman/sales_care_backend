package com.newgen.ntlsnc.salesandcollection.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.salesandcollection.service.SalesOverviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Newaz Sharif
 * @since 31st May,22
 */

@RestController
@RequestMapping("/api/sales-data")
public class SalesDataOverviewController {

    @Autowired
    SalesOverviewService salesOverviewService;

    @GetMapping("/overView")
    public ResponseEntity<?> getSalesOverView(
            @RequestParam Long userLoginId, @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) Long productCategoryId, @RequestParam Long accountingYearId,
            @RequestParam Long companyId) {

        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(salesOverviewService.getSalesOverView(userLoginId, locationId,
                    productCategoryId, accountingYearId, companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/location-wise")
    public ResponseEntity<?> getLocationWiseSalesData(
            @RequestParam Long userLoginId, @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) Long productCategoryId,
            @RequestParam(required = false) Long accountingYearId,
            @RequestParam Long companyId) {

        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(salesOverviewService.getSalesData(userLoginId, locationId,
                    productCategoryId, accountingYearId, companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/sales-budget-monthly")
    public ResponseEntity<?> getSalesBudgetMonthly(
            @RequestParam Long companyId,
            @RequestParam(required = false) Long salesOfficerId,
            @RequestParam(required = false) Long accountingYearId,
            @RequestParam(required = false) Integer month) {

        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(salesOverviewService.getSalesBudgetMonthly(companyId, accountingYearId,
                    salesOfficerId, month));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/sales-officer-data-monthly")
    public ResponseEntity<?> getSalesOfficerSalesDataMonthly(
            @RequestParam Long companyId,
            @RequestParam(required = false) Long salesOfficerId,
            @RequestParam(required = false) Long accountingYearId,
            @RequestParam(required = false) Integer month) {

        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(salesOverviewService.getSalesDataSalesOfficerMonthly(companyId,
                    salesOfficerId, accountingYearId, month));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-sales-vs-target")
    public ResponseEntity<?> getSalesVsTarget(@RequestParam(value = "companyId") Long companyId,
                                              @RequestParam(value = "accountingYearId",required = false) Long accountingYearId,
                                              @RequestParam(value = "semesterId",required = false) Long semesterId,
                                              @RequestParam(value = "fromDate", required = false) String fromDate,
                                              @RequestParam(value = "toDate",required = false) String toDate) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(salesOverviewService.findSalesVsTarget(companyId, accountingYearId, semesterId, fromDate, toDate));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
