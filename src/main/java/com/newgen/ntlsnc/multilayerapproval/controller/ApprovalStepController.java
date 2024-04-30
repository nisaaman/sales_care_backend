package com.newgen.ntlsnc.multilayerapproval.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.multilayerapproval.dto.ApprovalStepDto;
import com.newgen.ntlsnc.multilayerapproval.service.ApprovalStepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author nisa
 * @date 9/11/22
 * @time 10:18 AM
 */
@RestController
@RequestMapping("/api/approval-step")
public class ApprovalStepController {
    private static final String SCOPE = "Approval Step";

    @Autowired
    ApprovalStepService approvalStepService;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ApprovalStepDto approvalStepDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(approvalStepService.create(approvalStepDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody @Valid ApprovalStepDto approvalStepDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(approvalStepService.update(approvalStepDto.getId(), approvalStepDto));
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
            response.setSuccess(approvalStepService.delete(id));
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
            response.setSuccess(approvalStepService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(approvalStepService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
