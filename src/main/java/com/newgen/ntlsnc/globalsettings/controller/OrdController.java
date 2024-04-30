package com.newgen.ntlsnc.globalsettings.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.service.OrdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author kamal
 * @Date ৪/৭/২২
 */

@RestController
@RequestMapping("/api/ord")
public class OrdController {
    private static final String SCOPE = "ORD";

    @Autowired
    OrdService ordService;

    @GetMapping("/calculator-list-view")
    public ResponseEntity<?> getOrdCalculatorListView(@RequestParam Long companyId,
                                                      @RequestParam(required = false) List<Long> locationIds,
                                                      @RequestParam String fromDate,
                                                      @RequestParam String toDate) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(ordService.getOrdCalculatorList(companyId, locationIds, fromDate, toDate));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/calculable-invoice-list")
    public ResponseEntity<?> getOrdCalculableInvoiceList(@RequestParam Long companyId, @RequestParam Long distributorId, @RequestParam String ordCalculationDate) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(ordService.getOrdCalculableData(companyId, distributorId, ordCalculationDate));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @GetMapping("/list")
    public ResponseEntity<?> getOrdList(
            @RequestParam Long companyId,
            @RequestParam(required = false) Long locationId,
            @RequestParam Long accountingYearId,
            @RequestParam(required = false) Long semesterId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(ordService.getOrdList(companyId,
                    locationId, accountingYearId, semesterId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
