package com.newgen.ntlsnc.globalsettings.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.dto.InvoiceNatureDto;
import com.newgen.ntlsnc.globalsettings.dto.LocationDto;
import com.newgen.ntlsnc.globalsettings.service.InvoiceNatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author kamal
 */

@RestController
@RequestMapping("/api/invoice-nature")
public class InvoiceNatureController {
    private static final String SCOPE = "InvoiceNature";

    @Autowired
    InvoiceNatureService invoiceNatureService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody InvoiceNatureDto invoiceNatureDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(invoiceNatureService.create(invoiceNatureDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody InvoiceNatureDto invoiceNatureDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(invoiceNatureService.update(invoiceNatureDto.getId(), invoiceNatureDto));
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
            response.setSuccess(invoiceNatureService.delete(id));
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
            response.setSuccess(invoiceNatureService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(invoiceNatureService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
