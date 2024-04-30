package com.newgen.ntlsnc.globalsettings.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.dto.AccountingYearDto;
import com.newgen.ntlsnc.globalsettings.dto.BankAccountDto;
import com.newgen.ntlsnc.globalsettings.service.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author Mou
 * Created on 6/3/22 11:16 AM
 */

@RestController
@RequestMapping("/api/bank-account")
public class BankAccountController {
    private static final String SCOPE = "Bank Account";
    @Autowired
    BankAccountService bankAccountService;
    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid BankAccountDto bankAccountDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(bankAccountService.create(bankAccountDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @PutMapping
    public ResponseEntity<?> update(@RequestBody BankAccountDto bankAccountDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(bankAccountService.update(bankAccountDto.getId(), bankAccountDto));
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
            response.setSuccess(bankAccountService.delete(id));
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
            response.setSuccess(bankAccountService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(bankAccountService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

