package com.newgen.ntlsnc.subscriptions.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.subscriptions.dto.PaymentDto;
import com.newgen.ntlsnc.subscriptions.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author Mou
 * Created on 5/3/22 10:27 AM
 */

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    private static final String SCOPE = "Payment";
    @Autowired
    PaymentService paymentService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody PaymentDto paymentDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(paymentService.create(paymentDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @PutMapping
    public ResponseEntity<?> update(@RequestBody PaymentDto paymentDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(paymentService.update(paymentDto.getId(), paymentDto));
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
            response.setSuccess(paymentService.delete(id));
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
            response.setSuccess(paymentService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(paymentService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
