package com.newgen.ntlsnc.salesandcollection.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.salesandcollection.service.SalesReturnProposalOverViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author Newaz Sharif
 * @since 4th July,22
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/sales-return-proposal-data")
public class SalesReturnProposalOverViewController {

    @Autowired
    SalesReturnProposalOverViewService salesReturnProposalOverViewService;

    @GetMapping("/over-view")
    public ResponseEntity<?> getSalesReturnProposalOverView(
            @RequestParam Long userLoginId, @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) Long accountingYearId, @RequestParam Long companyId,
            @RequestParam(required = false) String approvalStatus) {
        ApiResponse response = new ApiResponse(false);

        response.setSuccess(salesReturnProposalOverViewService.getSalesReturnProposalOverView(
                userLoginId, locationId, accountingYearId, companyId, approvalStatus));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/details-view/{salesReturnProposalId}")
    public ResponseEntity<?> getSalesReturnProposalDetailsView(
            @PathVariable Long salesReturnProposalId) {

        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(salesReturnProposalOverViewService.getSalesReturnProposalDetails(
                    salesReturnProposalId));
        } catch (Exception ex) {
            ex.printStackTrace();
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
