package com.newgen.ntlsnc.globalsettings.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.dto.SemesterDto;
import com.newgen.ntlsnc.globalsettings.dto.StoreDto;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.globalsettings.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.List;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author kamal
 * @Date ১৬/৪/২২
 */

@RestController
@RequestMapping("/api/store")
public class StoreController {
    private static final String SCOPE = "Store";

    @Autowired
    StoreService storeService;
    @Autowired
    OrganizationService organizationService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid StoreDto storeDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(storeService.create(storeDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody @Valid StoreDto storeDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(storeService.update(storeDto.getId(), storeDto));
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
            response.setSuccess(storeService.delete(id));
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
            response.setSuccess(storeService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(storeService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-store-type-selected")
    public ResponseEntity<?> getStoreTypeSelected() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(storeService.findStoreTypeSelected());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @GetMapping("/store-type-wise")
    public ResponseEntity<?> getStoreByStoreType(@RequestParam("storeTypes")
                                                             List<String> storeTypes) {
        ApiResponse response = new ApiResponse(false);

        try {
            Long organizationId = organizationService.getOrganizationIdFromLoginUser();
            response.setSuccess(storeService.getStoreList(organizationId, storeTypes));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
