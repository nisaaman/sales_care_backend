package com.newgen.ntlsnc.salesandcollection.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.salesandcollection.dto.DistributorSalesOfficerMapDto;
import com.newgen.ntlsnc.salesandcollection.service.DistributorSalesOfficerMapService;
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
@RequestMapping("/api/distributor-sales-officer-map")
public class DistributorSalesOfficerMapController {

    private static final String SCOPE = "Distributor Sales Officer Map";

    @Autowired
    DistributorSalesOfficerMapService distributorSalesOfficerMapService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody DistributorSalesOfficerMapDto distributorSalesOfficerMapDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(distributorSalesOfficerMapService.create(distributorSalesOfficerMapDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody DistributorSalesOfficerMapDto distributorSalesOfficerMapDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(distributorSalesOfficerMapService.update(distributorSalesOfficerMapDto.getId(), distributorSalesOfficerMapDto));
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
            response.setSuccess(distributorSalesOfficerMapService.delete(id));
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
            response.setSuccess(distributorSalesOfficerMapService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(distributorSalesOfficerMapService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("sales-officer-id/{id}")
    public ResponseEntity<?> getSalesOfficer(@PathVariable Long id) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(distributorSalesOfficerMapService.findBySalesOfficerId(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
