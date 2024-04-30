package com.newgen.ntlsnc.globalsettings.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.dto.BankBranchDto;
import com.newgen.ntlsnc.globalsettings.service.BankBranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author Mou
 * Created on 5/3/22 02:04 PM
 */

@RestController
@RequestMapping("/api/bank-branch")
public class BankBranchController {
    private static final String SCOPE = "Bank Branch";
    @Autowired
    BankBranchService bankBranchService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid BankBranchDto bankBranchDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(bankBranchService.create(bankBranchDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody BankBranchDto bankBranchDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(bankBranchService.update(bankBranchDto.getId(), bankBranchDto));
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
            response.setSuccess(bankBranchService.delete(id));
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
            response.setSuccess(bankBranchService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(bankBranchService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/bank-all-branch/{bankId}")
    public ResponseEntity<?> getBranchListOfBank(@PathVariable Long bankId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(bankBranchService.findAllBranchByBank(bankId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/all-active-bank-branch/{bankId}")
    public ResponseEntity<?> getAllActiveBankListByBranch(@PathVariable Long bankId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(bankBranchService.getAllActiveBankListByBranch(bankId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
