package com.newgen.ntlsnc.salesandcollection.controller;


import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.salesandcollection.dto.CreditLimitProposalDto;
import com.newgen.ntlsnc.salesandcollection.service.CreditLimitProposalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author marziah
 */
@RestController
@RequestMapping("/api/credit-limit-proposal")
public class CreditLimitProposalController {
    private static final String SCOPE = "CreditLimitProposal";

    @Autowired
    CreditLimitProposalService creditLimitProposalService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreditLimitProposalDto creditLimitProposalDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(creditLimitProposalService.create(creditLimitProposalDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody CreditLimitProposalDto creditLimitProposalDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(creditLimitProposalService.update(creditLimitProposalDto.getId(), creditLimitProposalDto));
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
            response.setSuccess(creditLimitProposalService.delete(id));
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
            response.setSuccess(creditLimitProposalService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(creditLimitProposalService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-list")
    public ResponseEntity<?> getCreditLimitProposalList(@RequestParam Map<String, Object> searchParams) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(creditLimitProposalService.getCreditLimitProposalList(searchParams));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-sales-vs-target-for-a-distributor")
    public ResponseEntity<?> getSalesVsTargetForADistributor(@RequestParam Long companyId, @RequestParam Long distributorId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(creditLimitProposalService.getSalesVsTargetForADistributor(companyId, distributorId));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-proposal-status-wise-count-for-mobile")
    public ResponseEntity<?> getProposalStatusWiseCount(@RequestParam Long companyId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(creditLimitProposalService.getProposalStatusWiseCount(companyId));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
