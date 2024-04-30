package com.newgen.ntlsnc.salesandcollection.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.salesandcollection.dto.CreditLimitDto;
import com.newgen.ntlsnc.salesandcollection.dto.DistributorTypeDto;
import com.newgen.ntlsnc.salesandcollection.service.CreditLimitService;
import com.newgen.ntlsnc.salesandcollection.service.DistributorTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author anika
 * @Date ২৫/৫/২২
 */

@RestController
@RequestMapping("/api/distributor-type")
public class DistributorTypeController {

    private static final String SCOPE = "Distributor Type";

    @Autowired
    DistributorTypeService distributorTypeService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody DistributorTypeDto distributorTypeDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(distributorTypeService.create(distributorTypeDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody DistributorTypeDto distributorTypeDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(distributorTypeService.update(distributorTypeDto.getId(), distributorTypeDto));
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
            response.setSuccess(distributorTypeService.delete(id));
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
            response.setSuccess(distributorTypeService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(distributorTypeService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
