package com.newgen.ntlsnc.globalsettings.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.dto.LocationManagerMapDto;
import com.newgen.ntlsnc.globalsettings.dto.PaymentBookDto;
import com.newgen.ntlsnc.globalsettings.service.LocationManagerMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author anika
 * @Date ২/৬/২২
 */
@RestController
@RequestMapping("/api/location-manager-map")
public class LocationManagerMapController {

    private static final String SCOPE = "Location Manager Map";

    @Autowired
    LocationManagerMapService locationManagerMapService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody LocationManagerMapDto locationManagerMapDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(locationManagerMapService.create(locationManagerMapDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody LocationManagerMapDto locationManagerMapDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(locationManagerMapService.update(locationManagerMapDto.getId(), locationManagerMapDto));
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
            response.setSuccess(locationManagerMapService.delete(id));
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
            response.setSuccess(locationManagerMapService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(locationManagerMapService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all-by-company-id/{companyId}")
    public ResponseEntity<?> getAllByCompany(@PathVariable(name = "companyId") Long companyId)  {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(locationManagerMapService.getAllByCompany(companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

