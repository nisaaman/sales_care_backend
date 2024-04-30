package com.newgen.ntlsnc.globalsettings.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.dto.LocationTreeDto;
import com.newgen.ntlsnc.globalsettings.service.LocationTreeService;
import com.newgen.ntlsnc.salesandcollection.service.UserLocationTreeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author kamal
 * @Date ২৫/৫/২২
 */

@RestController
@RequestMapping("/api/location-tree")
public class LocationTreeController {
    private static final String SCOPE = "Location Tree";
    @Autowired
    LocationTreeService locationTreeService;
    @Autowired
    UserLocationTreeService userLocationTreeService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody LocationTreeDto locationTreeDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(locationTreeService.create(locationTreeDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody LocationTreeDto locationTreeDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(locationTreeService.update(locationTreeDto.getId(), locationTreeDto));
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
            response.setSuccess(locationTreeService.delete(id));
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
            response.setSuccess(locationTreeService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(locationTreeService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/list")
    public ResponseEntity<?> getList(@RequestBody Map searchParams) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(locationTreeService.list(searchParams));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/update-active-status/{id}/{checked}")
    public ResponseEntity<?> updateActiveStatus(@PathVariable String id,
                                                @PathVariable Boolean checked) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(locationTreeService.updateActiveStatus(Long.parseLong(id), checked));
            response.setMessage(SCOPE + UPDATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/info/{id}")
    public ResponseEntity<?> getLocationTreeRelatedInfo(@PathVariable Long id) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(locationTreeService.findLocationTreeRelatedInfoByLocationTreeId(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-by-organization-id/{organizationId}")
    public ResponseEntity<?> getAllByOrganizationId(@PathVariable Long organizationId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(locationTreeService.findAllByOrganizationId(organizationId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/locationTree/{userLoginId}/{companyId}")
    public ResponseEntity<?> getUserLocationTree(@PathVariable Long userLoginId,
                                                 @PathVariable Long companyId) {

        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(userLocationTreeService.getUserWiseLocationTree(userLoginId,
                    companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/report-location-tree-info/{companyId}")
    public ResponseEntity<?> getLocationTreeRelatedInfoForReport(@PathVariable Long companyId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(locationTreeService.findLocationTreeRelatedInfoByCompanyId(companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
