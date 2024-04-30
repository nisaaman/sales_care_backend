package com.newgen.ntlsnc.usermanagement.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.usermanagement.dto.ApplicationUserRoleMapDto;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserRoleMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author anika
 * @Date ৫/৬/২২
 */
@RestController
@RequestMapping("/api/application-user-role-map")
public class ApplicationUserRoleMapController {

    private static final String SCOPE = "Application User Role Mapping";

    @Autowired
    ApplicationUserRoleMapService applicationUserRoleMapService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ApplicationUserRoleMapDto applicationUserRoleMapDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(applicationUserRoleMapService.create(applicationUserRoleMapDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody ApplicationUserRoleMapDto applicationUserRoleMapDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(applicationUserRoleMapService.update(applicationUserRoleMapDto.getId(), applicationUserRoleMapDto));
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
            response.setSuccess(applicationUserRoleMapService.delete(id));
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
            response.setSuccess(applicationUserRoleMapService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(applicationUserRoleMapService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all-role-by-user/{userId}")
    public ResponseEntity<?> getAllRoleByUser(@PathVariable Long userId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(applicationUserRoleMapService.getAllRoleByUser(userId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}


