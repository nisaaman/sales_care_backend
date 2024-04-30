package com.newgen.ntlsnc.supplychainmanagement.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.service.EnumeService;
import com.newgen.ntlsnc.globalsettings.service.ProductService;
import com.newgen.ntlsnc.supplychainmanagement.dto.InvTransactionDto;
import com.newgen.ntlsnc.supplychainmanagement.service.InvTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author anika
 * @Date ২৬/৫/২২
 */
@RestController
@RequestMapping("/api/inv-transaction")
public class InvTransactionController {
    private static final String SCOPE = "Inventory Transaction";

    @Autowired
    InvTransactionService invTransactionService;
    @Autowired
    EnumeService enumeService;
    @Autowired
    ProductService productService;
    @PostMapping
    public ResponseEntity<?> create(@RequestBody InvTransactionDto invTransactionDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(invTransactionService.create(invTransactionDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody InvTransactionDto invTransactionDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(invTransactionService.update(invTransactionDto.getId(), invTransactionDto));
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
            response.setSuccess(invTransactionService.delete(id));
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
            response.setSuccess(invTransactionService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(invTransactionService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/product-wise-weighted-avg-rate/{companyId}/{productId}")
    public ResponseEntity<?> getProductBatchListWithStock(
            @PathVariable Long companyId,
            @PathVariable Long productId){

        ApiResponse response = new ApiResponse(false);
        response.setSuccess(productService.getWeightedAverageRate(companyId,productId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-transfer-receive-status/{invTransactionId}")
    public ResponseEntity<?> getTransferReceiveStatus(
            @PathVariable Long invTransactionId){

        ApiResponse response = new ApiResponse(false);
        response.setSuccess(invTransactionService.getTransferReceivedStatus(invTransactionId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/claim-type-list")
    public ResponseEntity<?> getAllClaimType(){

        ApiResponse response = new ApiResponse(false);
        response.setSuccess(enumeService.getAllClaimType());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
