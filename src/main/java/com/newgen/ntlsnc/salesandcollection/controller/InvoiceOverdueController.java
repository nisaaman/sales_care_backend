package com.newgen.ntlsnc.salesandcollection.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.salesandcollection.dto.InvoiceOverdueDto;
import com.newgen.ntlsnc.salesandcollection.service.InvoiceOverdueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.newgen.ntlsnc.common.CommonConstant.CREATE_SUCCESS_MESSAGE;
import static com.newgen.ntlsnc.common.CommonConstant.UPDATE_SUCCESS_MESSAGE;

/**
 * @author kamal
 * @Date ২৪/৮/২২
 */


@RestController
@RequestMapping("/api/invoice-overdue")
public class InvoiceOverdueController {
    private static final String SCOPE = "Invoice Overdue";

    @Autowired
    InvoiceOverdueService invoiceOverdueService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(invoiceOverdueService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/create-all")
    public ResponseEntity<?> createAll(@RequestBody List<InvoiceOverdueDto> invoiceOverdueDtoList) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(invoiceOverdueService.createAll(invoiceOverdueDtoList));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/update-all")
    public ResponseEntity<?> updateAll(@RequestBody List<InvoiceOverdueDto> invoiceOverdueDtoList) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(invoiceOverdueService.updateAll(invoiceOverdueDtoList));
            response.setMessage(SCOPE + UPDATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all/{companyId}")
    public ResponseEntity<?> getAllByCompany(@PathVariable Long companyId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(invoiceOverdueService.getAllByCompany(companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
