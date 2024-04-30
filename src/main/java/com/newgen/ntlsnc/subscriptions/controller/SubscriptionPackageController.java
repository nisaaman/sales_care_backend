package com.newgen.ntlsnc.subscriptions.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.dto.AccountingYearDto;
import com.newgen.ntlsnc.subscriptions.dto.SubscriptionPackageDto;
import com.newgen.ntlsnc.subscriptions.service.SubscriptionPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author Mou
 * Created on 4/3/22 12:40 AM
 */

@RestController
@RequestMapping("/api/subscription-package")
public class SubscriptionPackageController {
    private static final String SCOPE = "Subscription Package";
    @Autowired
    SubscriptionPackageService subscriptionPackageService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody SubscriptionPackageDto subscriptionPackageDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(subscriptionPackageService.create(subscriptionPackageDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @PutMapping
    public ResponseEntity<?> update(@RequestBody SubscriptionPackageDto subscriptionPackageDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(subscriptionPackageService.update(subscriptionPackageDto.getId(), subscriptionPackageDto));
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
            response.setSuccess(subscriptionPackageService.delete(id));
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
            response.setSuccess(subscriptionPackageService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(subscriptionPackageService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
