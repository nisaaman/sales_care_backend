package com.newgen.ntlsnc.usermanagement.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.usermanagement.dto.ApplicationUserCompanyMappingDto;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserCompanyMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author anika
 * @Date ২০/৬/২২
 */
@RestController
@RequestMapping("/api/application-user-company-mapping")
public class ApplicationUserCompanyMappingController {

    private static final String SCOPE = "Application User Company Mapping";
    @Autowired
    ApplicationUserCompanyMappingService applicationUserCompanyMappingService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ApplicationUserCompanyMappingDto applicationUserCompanyMappingDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(applicationUserCompanyMappingService.create(applicationUserCompanyMappingDto));
            response.setMessage(SCOPE + CREATE_SAVE_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody ApplicationUserCompanyMappingDto applicationUserCompanyMappingDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(applicationUserCompanyMappingService.update(applicationUserCompanyMappingDto.getId(), applicationUserCompanyMappingDto));
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
            response.setSuccess(applicationUserCompanyMappingService.delete(id));
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
            response.setSuccess(applicationUserCompanyMappingService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(applicationUserCompanyMappingService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all-company-by-user/{userId}")
    public ResponseEntity<?> getAllCompanyByUser(@PathVariable Long userId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(applicationUserCompanyMappingService.getAllCompanyByUser(userId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
