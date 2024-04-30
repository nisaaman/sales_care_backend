package com.newgen.ntlsnc.salesandcollection.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.salesandcollection.dto.MaterialReceivePlanDto;
import com.newgen.ntlsnc.salesandcollection.entity.SalesBookingDetails;
import com.newgen.ntlsnc.salesandcollection.service.MaterialReceivePlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author sagor
 * @date ১২/৪/২২
 */
@RestController
@RequestMapping("/api/material-receive-plan")
public class MaterialReceivePlanController {
    private static final String SCOPE = "Material Receive Plan";

    @Autowired
    MaterialReceivePlanService materialReceivePlanService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody MaterialReceivePlanDto materialReceivePlanDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(materialReceivePlanService.create(materialReceivePlanDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody MaterialReceivePlanDto materialReceivePlanDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(materialReceivePlanService.update(materialReceivePlanDto.getId(), materialReceivePlanDto));
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
            response.setSuccess(materialReceivePlanService.delete(id));
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
            response.setSuccess(materialReceivePlanService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(materialReceivePlanService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-ticket/{booking_item_id}")
    public ResponseEntity<?> getTicket(@PathVariable("booking_item_id") Long bookingItemId) {
        ApiResponse response = new ApiResponse(false);
        try {
            Map<String, Object> ticket = materialReceivePlanService.getTicket(bookingItemId);
            response.setSuccess(ticket);
            response.setData(ticket);

        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-ticket-company-wise")
    public ResponseEntity<?> getTicketCompanyWise(@RequestParam Long companyId,@RequestParam(required = false) Long depotId, @RequestParam(required = false) Long semesterId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(materialReceivePlanService.getTicketCompanyWise(companyId, depotId, semesterId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
