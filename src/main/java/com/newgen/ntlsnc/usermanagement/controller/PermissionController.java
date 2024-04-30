package com.newgen.ntlsnc.usermanagement.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.usermanagement.dto.PermissionDto;
import com.newgen.ntlsnc.usermanagement.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author anika
 * @Date ১৯/৬/২২
 */
@RestController
@RequestMapping("/api/permission")
public class PermissionController {

    private static final String SCOPE = "Permission";
    @Autowired
    PermissionService permissionService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody PermissionDto permissionDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(permissionService.create(permissionDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody PermissionDto permissionDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(permissionService.update(permissionDto.getId(), permissionDto));
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
            response.setSuccess(permissionService.delete(id));
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
            response.setSuccess(permissionService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(permissionService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/create-all")
    public ResponseEntity<?> createAll(@RequestBody PermissionDto[] permissionDtoList) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(permissionService.createAll(permissionDtoList));
            response.setMessage(SCOPE + CREATE_SAVE_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all-permission-list-by-role")
    public ResponseEntity<?> getAllPermissionListByRole(@RequestParam("roleId") Long roleId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(permissionService.getAllPermissionListByRole(roleId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
