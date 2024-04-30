package com.newgen.ntlsnc.globalsettings.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.dto.LocationTypeDto;
import com.newgen.ntlsnc.globalsettings.service.LocationTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.newgen.ntlsnc.common.CommonConstant.*;

@RestController
@RequestMapping("/api/location-type")
public class LocationTypeController {

    private static final String SCOPE = "Location Type";

    @Autowired
    LocationTypeService locationTypeService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody LocationTypeDto locationTypeDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(locationTypeService.create(locationTypeDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody LocationTypeDto locationTypeDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(locationTypeService.update(locationTypeDto.getId(), locationTypeDto));
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
            response.setSuccess(locationTypeService.delete(id));
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
            response.setSuccess(locationTypeService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(locationTypeService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("get-location-type-by-company-id/{companyId}")
    public ResponseEntity<?> findAllByCompanyId(@PathVariable Long companyId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(locationTypeService.findAllByCompanyId(companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("get-location-type/{locationTreeId}")
    public ResponseEntity<?> findAllByLocationTreeId(@PathVariable Long locationTreeId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(locationTypeService.findAllByLocationTreeId(locationTreeId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("add-depot-location-level-map/{locationTypeId}")
    public ResponseEntity<?> addDepotLocationLevelMap(@PathVariable Long locationTypeId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(locationTypeService.addDepotLocationLevelMap(locationTypeId));
            response.setMessage("Depot Location Level Map Created Successfully.");
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("get-all-depot-configured-location-level-list-and-location-tree-list-by-login-organization")
    public ResponseEntity<?> getAllDepotConfiguredLocationLevelListAndLocationTreeListByLoginOrganization() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(locationTypeService.getAllDepotConfiguredLocationLevelListAndLocationTreeListByLoginOrganization());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/delete-location-level-of-depot/{locationTypeId}")
    public ResponseEntity<?> deleteLocationLevelOfDepot(@PathVariable Long locationTypeId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(locationTypeService.deleteLocationLevelOfDepot(locationTypeId));
            response.setMessage("Depot Location Level Map Deleted Successfully.");
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping({"/getAllLocationType/{companyId}"})
    public ResponseEntity<?> getAllLocationType(@PathVariable Long companyId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(locationTypeService.getAllLocationType(companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
