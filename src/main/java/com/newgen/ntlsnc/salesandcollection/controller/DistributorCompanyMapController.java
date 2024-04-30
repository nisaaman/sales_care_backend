package com.newgen.ntlsnc.salesandcollection.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.salesandcollection.dto.DistributorCompanyMapDto;
import com.newgen.ntlsnc.salesandcollection.service.DistributorCompanyMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author anika
 * @Date ২২/৬/২২
 */
@RestController
@RequestMapping("/api/distributor-company-map")
public class DistributorCompanyMapController {

    private static final String SCOPE = "Distributor Company Map";

    @Autowired
    DistributorCompanyMapService distributorCompanyMapService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody DistributorCompanyMapDto distributorCompanyMapDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(distributorCompanyMapService.create(distributorCompanyMapDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody DistributorCompanyMapDto distributorCompanyMapDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(distributorCompanyMapService.update(distributorCompanyMapDto.getId(), distributorCompanyMapDto));
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
            response.setSuccess(distributorCompanyMapService.delete(id));
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
            response.setSuccess(distributorCompanyMapService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(distributorCompanyMapService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
