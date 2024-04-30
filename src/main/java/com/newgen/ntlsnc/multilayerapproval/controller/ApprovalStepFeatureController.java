package com.newgen.ntlsnc.multilayerapproval.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.dto.ProductCategoryTypeDto;
import com.newgen.ntlsnc.multilayerapproval.dto.ApprovalStepDto;
import com.newgen.ntlsnc.multilayerapproval.dto.ApprovalStepFeatureMapDto;
import com.newgen.ntlsnc.multilayerapproval.service.ApprovalStepFeatureMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.newgen.ntlsnc.common.CommonConstant.CREATE_SUCCESS_MESSAGE;
import static com.newgen.ntlsnc.common.CommonConstant.UPDATE_SUCCESS_MESSAGE;

/**
 * @author sagor
 * @date 9/12/22
 * @time 12:40 AM
 */
@RestController
@RequestMapping("/api/approval-step-feature-map")
public class ApprovalStepFeatureController {
    private static final String SCOPE = "Approval Step Feature";
    @Autowired
    ApprovalStepFeatureMapService approvalStepFeatureMapService;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ApprovalStepFeatureMapDto approvalStepFeatureMapDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(approvalStepFeatureMapService.create(approvalStepFeatureMapDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(approvalStepFeatureMapService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @GetMapping("/get-all-approval-step-feature-by-company-id/{companyId}")
    public ResponseEntity<?> getAllApprovalStepFeatureByCompanyId(@PathVariable Long companyId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(approvalStepFeatureMapService.getAllApprovalStepFeatureByCompanyId(companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all-approval-step-by-company-id-and-approval-step-feature/{companyId}/{approvalStepFeature}")
    public ResponseEntity<?> getAllApprovalStepFeatureByCompanyIdAndApprovalStepFeature(@PathVariable Long companyId,@PathVariable String approvalStepFeature) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(approvalStepFeatureMapService.getAllApprovalStepFeatureByCompanyIdAndApprovalStepFeature(companyId,approvalStepFeature));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/create-all")
    public ResponseEntity<?> createAll(@RequestBody @Valid ApprovalStepFeatureMapDto approvalStepFeatureMapDto){
        ApiResponse response = new ApiResponse(false);

        try{
            response.setSuccess(approvalStepFeatureMapService.createAll(approvalStepFeatureMapDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllByOrganizationAsPerDesign() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(approvalStepFeatureMapService.getAllByOrganizationAsPerDesign());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all-by-company-id-and-approval-feature/{companyId}/{feature}")
    public ResponseEntity<?> getAllByCompanyIdAndApprovalFeature(@PathVariable Long companyId, @PathVariable String feature) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(approvalStepFeatureMapService.getAllByCompanyIdAndApprovalFeature(companyId, feature));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @PostMapping("/update-all")
    public ResponseEntity<?> updateAll(@RequestBody @Valid ApprovalStepFeatureMapDto approvalStepFeatureMapDto){
        ApiResponse response = new ApiResponse(false);

        try{
            response.setSuccess(approvalStepFeatureMapService.updateAll(approvalStepFeatureMapDto));
            response.setMessage(SCOPE + UPDATE_SUCCESS_MESSAGE);
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
