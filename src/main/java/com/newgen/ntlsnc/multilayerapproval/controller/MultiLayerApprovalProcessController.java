package com.newgen.ntlsnc.multilayerapproval.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.multilayerapproval.dto.MultiLayerApprovalProcessDto;
import com.newgen.ntlsnc.multilayerapproval.service.MultiLayerApprovalProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.newgen.ntlsnc.common.CommonConstant.CREATE_SAVE_MESSAGE;
import static com.newgen.ntlsnc.common.CommonConstant.CREATE_SUCCESS_MESSAGE;

/**
 * @author nisa
 * @date 9/14/22
 * @time 6:40 PM
 */
@RestController
@RequestMapping("/api/multilayer-approval-process")
public class MultiLayerApprovalProcessController {

    private static final String SCOPE = "Approval Process";

    @Autowired
    MultiLayerApprovalProcessService multiLayerApprovalProcessService;

    @GetMapping
    public ResponseEntity<?> get(@RequestParam("companyId") Long companyId,
                                 @RequestParam("approvalFeature") String approvalFeature) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(multiLayerApprovalProcessService.getApprovalProcessList(companyId, approvalFeature));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody MultiLayerApprovalProcessDto multiLayerApprovalProcessDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(multiLayerApprovalProcessService.create(multiLayerApprovalProcessDto));
            response.setMessage(SCOPE + CREATE_SAVE_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


}
