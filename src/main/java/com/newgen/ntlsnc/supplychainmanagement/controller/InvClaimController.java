package com.newgen.ntlsnc.supplychainmanagement.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.supplychainmanagement.dto.InvClaimDto;
import com.newgen.ntlsnc.supplychainmanagement.dto.InvReceiveDto;
import com.newgen.ntlsnc.supplychainmanagement.service.InvClaimService;
import com.newgen.ntlsnc.supplychainmanagement.service.InvDamageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.newgen.ntlsnc.common.CommonConstant.RECEIVE_SUCCESS_MESSAGE;

/**
 * @author sunipa
 * @date ২২/৯/২২
 */
@RestController
@RequestMapping("api/inv-claim")
public class InvClaimController {
    private static final String SCOPE = "Inventory Claim";
    @Autowired
    InvClaimService invClaimService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody InvClaimDto invClaimDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(invClaimService.create(invClaimDto));
            response.setMessage(SCOPE + RECEIVE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
