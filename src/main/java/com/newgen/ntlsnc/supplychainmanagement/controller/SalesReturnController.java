package com.newgen.ntlsnc.supplychainmanagement.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.supplychainmanagement.dto.SalesReturnDto;
import com.newgen.ntlsnc.supplychainmanagement.service.SalesReturnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.newgen.ntlsnc.common.CommonConstant.CREATE_SUCCESS_MESSAGE;
import static com.newgen.ntlsnc.common.CommonConstant.DELETE_SUCCESS_MESSAGE;

/**
 * @author marziah
 * @Date 25/04/22
 */

@RestController
@RequestMapping("/api/sales-return")
public class SalesReturnController {
    private static final String SCOPE = "Sales Return";

    @Autowired
    SalesReturnService salesReturnService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody SalesReturnDto salesReturnDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(salesReturnService.createWithProposal(salesReturnDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
            if (response.success == true){
                salesReturnService.sendPushNotificationWhenSalesRetunConvertToSo(response.data);
            }
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody SalesReturnDto salesReturnDto) {
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(salesReturnService.delete(id));
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
            response.setSuccess(salesReturnService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(salesReturnService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-distributor-wise-list")
    public ResponseEntity<?> getApprovalStatusSummary(@RequestParam Long companyId, @RequestParam Long salesOfficerId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(salesReturnService.getDistributorWiseSalesReturnListByCompanyAndSalesPerson(companyId, salesOfficerId));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
