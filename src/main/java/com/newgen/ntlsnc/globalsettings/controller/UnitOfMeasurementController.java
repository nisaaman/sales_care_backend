package com.newgen.ntlsnc.globalsettings.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.dto.UnitOfMeasurementDto;
import com.newgen.ntlsnc.globalsettings.service.UnitOfMeasurementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.newgen.ntlsnc.common.CommonConstant.*;

@RestController
@RequestMapping("/api/unit-of-measurement")
public class UnitOfMeasurementController {

    private static final String SCOPE = "Unit Of Measurement";

    @Autowired
    UnitOfMeasurementService unitOfMeasurementService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid UnitOfMeasurementDto unitOfMeasurementDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(unitOfMeasurementService.create(unitOfMeasurementDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody @Valid UnitOfMeasurementDto unitOfMeasurementDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(unitOfMeasurementService.update(unitOfMeasurementDto.getId(), unitOfMeasurementDto));
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
            response.setSuccess(unitOfMeasurementService.delete(id));
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
            response.setSuccess(unitOfMeasurementService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(unitOfMeasurementService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping({"/getAllActiveUOM"})
    public ResponseEntity<?> getAllActiveUOM() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(unitOfMeasurementService.findAllActiveUOM());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
