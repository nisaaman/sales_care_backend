package com.newgen.ntlsnc.globalsettings.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.service.EnumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author kamal
 * @Date ২৪/৭/২২
 */

@RestController
@RequestMapping("/api/constants")
public class EnumController {
    @Autowired
    EnumeService enumeService;

    @GetMapping("/intact-types")
    public ResponseEntity<?> getAllIntactType() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(enumeService.findAllIntactType());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/approval-status")
    public ResponseEntity<?> getAllApprovalStatus() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(enumeService.findAllApprovalStatus());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/approval-status-except-draft")
    public ResponseEntity<?> getAllApprovalStatusExceptDraft() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(enumeService.findAllApprovalStatusExceptDraft());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/calculation-type")
    public ResponseEntity<?> getAllCalculationType() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(enumeService.findAllCalculationType());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/credit_debit_transaction_type")
    public ResponseEntity<?> findAllCreditDebitTransactionType() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(enumeService.findAllCreditDebitTransactionType());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/note_type")
    public ResponseEntity<?> getAllNoteType() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(enumeService.getAllNoteType());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/store_type")
    public ResponseEntity<?> getAllStoreType() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(enumeService.getAllStoreType());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/approval-feature")
    public ResponseEntity<?> getAllApprovalFeature() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(enumeService.getAllApprovalFeature());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/approval-actor")
    public ResponseEntity<?> getAllApprovalActor() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(enumeService.getAllApprovalActor());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/all-type-vehicles")
    public ResponseEntity<?> getAllTypeVehicles() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(enumeService.getAllTypeVehicles());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/all-activity-feature")
    public ResponseEntity<?> getAllActivityFeature() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(enumeService.getAllActivityFeature());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/all-ownership-vehicles")
    public ResponseEntity<?> getAllVehicleOwnership() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(enumeService.getAllVehicleOwnership());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
