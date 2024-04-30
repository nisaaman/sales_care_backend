package com.newgen.ntlsnc.supplychainmanagement.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.supplychainmanagement.dto.InvTransferDto;
import com.newgen.ntlsnc.supplychainmanagement.service.InvTransactionService;
import com.newgen.ntlsnc.supplychainmanagement.service.InvTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author marziah
 * @Date 17/04/22
 */

@RestController
@RequestMapping("/api/inv-transfer")
public class InvTransferController {
    private static final String SCOPE = "Inventory Transfer";

    @Autowired
    InvTransferService invTransferService;
    @Autowired
    InvTransactionService invTransactionService;
    @PostMapping
    public ResponseEntity<?> create(@RequestBody InvTransferDto invTransferDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(invTransferService.create(invTransferDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody InvTransferDto invTransferDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(invTransferService.update(invTransferDto.getId(), invTransferDto));
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
            response.setSuccess(invTransferService.delete(id));
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
            response.setSuccess(invTransferService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(invTransferService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/product-wise-batch-stock-info/{companyId}/{productId}/{depotId}")
    public ResponseEntity<?> getProductBatchListWithStock(
            @PathVariable Long companyId,
            @PathVariable Long productId,
            @PathVariable Long depotId){

        ApiResponse response = new ApiResponse(false);
        response.setSuccess(invTransactionService.getProductBatchListWithStock(
                companyId,productId,depotId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/details/data")
    public ResponseEntity<?> getInvTransferDetailsDataList(
            @RequestParam Long companyId, @RequestParam(required = false) Long accountingYearId) {
        ApiResponse response = new ApiResponse(false);
        response.setSuccess(invTransferService.getInvTransferDetails(
                companyId, accountingYearId));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
