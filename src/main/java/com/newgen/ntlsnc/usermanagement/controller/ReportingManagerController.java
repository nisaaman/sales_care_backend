package com.newgen.ntlsnc.usermanagement.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.usermanagement.dto.PermissionDto;
import com.newgen.ntlsnc.usermanagement.dto.ReportingManagerDto;
import com.newgen.ntlsnc.usermanagement.service.ReportingManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author anika
 * @Date ২১/৬/২২
 */
@RestController
@RequestMapping("/api/reporting-manager")
public class ReportingManagerController {
    private static final String SCOPE = "Reporting Manager";

    @Autowired
    ReportingManagerService reportingManagerService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ReportingManagerDto reportingManagerDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(reportingManagerService.create(reportingManagerDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody ReportingManagerDto reportingManagerDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(reportingManagerService.update(reportingManagerDto.getId(), reportingManagerDto));
            response.setMessage(SCOPE + UPDATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{companyId}/{salesOfficerId}")
    public ResponseEntity<?> delete(@PathVariable Long companyId,
                                    @PathVariable Long salesOfficerId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(reportingManagerService.deleteReportingManager(companyId, salesOfficerId));
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
            response.setSuccess(reportingManagerService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(reportingManagerService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all-by-company-id/{companyId}")
    public ResponseEntity<?> getAllByCompany(@PathVariable(name = "companyId") Long companyId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(reportingManagerService.getAllByCompany(companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
