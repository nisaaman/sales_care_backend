package com.newgen.ntlsnc.globalsettings.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.dto.DepotCompanyMappingDto;
import com.newgen.ntlsnc.globalsettings.service.DepotCompanyMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author anika
 * @Date ২০/৬/২২
 */
@RestController
@RequestMapping("/api/depot-company-mapping")
public class DepotCompanyMappingController {
    private static final String SCOPE = "Depot Company Mapping";

    @Autowired
    DepotCompanyMappingService depotCompanyMappingService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody DepotCompanyMappingDto depotCompanyMappingDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(depotCompanyMappingService.create(depotCompanyMappingDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody DepotCompanyMappingDto depotCompanyMappingDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(depotCompanyMappingService.update(depotCompanyMappingDto.getId(), depotCompanyMappingDto));
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
            response.setSuccess(depotCompanyMappingService.delete(id));
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
            response.setSuccess(depotCompanyMappingService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(depotCompanyMappingService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
