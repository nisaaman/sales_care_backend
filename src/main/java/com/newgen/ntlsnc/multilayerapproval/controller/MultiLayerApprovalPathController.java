package com.newgen.ntlsnc.multilayerapproval.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.multilayerapproval.dto.ApprovalStepDto;
import com.newgen.ntlsnc.multilayerapproval.dto.MultiLayerApprovalPathDto;
import com.newgen.ntlsnc.multilayerapproval.service.ApprovalStepFeatureMapService;
import com.newgen.ntlsnc.multilayerapproval.service.MultiLayerApprovalPathService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author sagor
 * @date 9/12/22
 * @time 12:40 AM
 */
@RestController
@RequestMapping("/api/multilayer-approval-path")
public class MultiLayerApprovalPathController {
    private static final String SCOPE = "MultiLayer Approval Path";

    @Autowired
    MultiLayerApprovalPathService multiLayerApprovalPathService;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody MultiLayerApprovalPathDto multiLayerApprovalPathDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(multiLayerApprovalPathService.create(multiLayerApprovalPathDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody @Valid MultiLayerApprovalPathDto multiLayerApprovalPathDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(multiLayerApprovalPathService.update(multiLayerApprovalPathDto.getId(), multiLayerApprovalPathDto));
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
            response.setSuccess(multiLayerApprovalPathService.delete(id));
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
            response.setSuccess(multiLayerApprovalPathService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(multiLayerApprovalPathService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
