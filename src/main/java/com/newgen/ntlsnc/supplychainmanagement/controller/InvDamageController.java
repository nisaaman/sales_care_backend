package com.newgen.ntlsnc.supplychainmanagement.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.supplychainmanagement.dto.InvDamageDto;
import com.newgen.ntlsnc.supplychainmanagement.service.InvDamageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author anika
 * @Date ৩১/৫/২২
 */
@RestController
@RequestMapping("/api/inv-damage")
public class InvDamageController {

    private static final String SCOPE = "Inventory Damage";

    @Autowired
    InvDamageService invDamageService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody InvDamageDto invDamageDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(invDamageService.create(invDamageDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody InvDamageDto invDamageDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(invDamageService.update(invDamageDto.getId(), invDamageDto));
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
            response.setSuccess(invDamageService.delete(id));
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
            response.setSuccess(invDamageService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(invDamageService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/damage-declaration-list")
    public ResponseEntity<?> getDamageDeclarationList(
            @RequestParam(value = "companyId") long companyId,
            @RequestParam(value = "fiscalYearId") String fiscalYearId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(invDamageService
                    .getDamageDeclarationList(companyId, fiscalYearId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
