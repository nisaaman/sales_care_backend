package com.newgen.ntlsnc.salesandcollection.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.salesandcollection.dto.SalesOrderDto;
import com.newgen.ntlsnc.salesandcollection.service.SalesOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author kamal
 * @Date ১২/৪/২২
 */

@RestController
@RequestMapping("/api/sales-order")
public class SalesOrderController {
    private static final String SCOPE = "Sales Order";

    @Autowired
    SalesOrderService salesOrderService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody SalesOrderDto salesOrderDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(salesOrderService.create(salesOrderDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody SalesOrderDto salesOrderDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(salesOrderService.update(salesOrderDto.getSalesOrderId(), salesOrderDto));
            response.setMessage(SCOPE + UPDATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(salesOrderService.delete(id));
            response.setMessage(SCOPE + DELETE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(salesOrderService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(salesOrderService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/creation-list")
    public ResponseEntity<?> getSalesBookingListForSalesOrderCreation(
            @RequestParam Long companyId, @RequestParam(required = false) Long accountingYearId,
            @RequestParam(required = false) Long semesterId, Long userLoginId,
            @RequestParam(required = false) Long locationId) {

        ApiResponse response = new ApiResponse(false);
        response.setSuccess(salesOrderService.getSalesBookingDetailsForSalesOrderCreation(
                companyId, accountingYearId, semesterId, userLoginId, locationId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/booking-data/{salesBookingId}")
    public ResponseEntity<?> getSalesBookingAndSalesOrderDetails(
            @PathVariable Long salesBookingId) {

        ApiResponse response = new ApiResponse(false);
        response.setSuccess(salesOrderService.getSalesBookingAndSalesOrderDetails(
                salesBookingId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/booking-data/{salesBookingId}/{salesOrderId}")
    public ResponseEntity<?> getSalesBookingDetailsInSalesOrder(
            @PathVariable Long salesBookingId,
            @PathVariable Long salesOrderId) {

        ApiResponse response = new ApiResponse(false);
        response.setSuccess(salesOrderService.getSalesBookingDetailsInSalesOrder(
                salesBookingId,salesOrderId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/undelivered-list")
    public ResponseEntity<?> getUndeliveredSalesOrderList(
            @RequestParam Long companyId, @RequestParam Long accountingYearId,
            @RequestParam(required = false) Long semesterId) {

        ApiResponse response = new ApiResponse(false);
        response.setSuccess(salesOrderService.getUndeliveredSalesOrderList(
                companyId, accountingYearId, semesterId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
