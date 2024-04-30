package com.newgen.ntlsnc.globalsettings.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.dto.TermsAndConditionsDto;
import com.newgen.ntlsnc.globalsettings.service.TermsAndConditionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author kamal
 * @Date ১৯/৪/২২
 */

@RestController
@RequestMapping("/api/terms-and-conditions")
public class TermsAndConditionsController {
    private static final String SCOPE = "Terms And Conditions";

    @Autowired
    TermsAndConditionsService termsAndConditionsService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TermsAndConditionsDto termsAndConditionsDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(termsAndConditionsService.create(termsAndConditionsDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody TermsAndConditionsDto termsAndConditionsDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(termsAndConditionsService.update(termsAndConditionsDto.getId(), termsAndConditionsDto));
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
            response.setSuccess(termsAndConditionsService.delete(id));
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
            response.setSuccess(termsAndConditionsService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(termsAndConditionsService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-by-company/{companyId}")
    public ResponseEntity<?> getByCompanyId(@PathVariable Long companyId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(termsAndConditionsService.findByCompanyId(companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
