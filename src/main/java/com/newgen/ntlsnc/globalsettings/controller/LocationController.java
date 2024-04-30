package com.newgen.ntlsnc.globalsettings.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.dto.LocationDto;
import com.newgen.ntlsnc.globalsettings.entity.LocationType;
import com.newgen.ntlsnc.globalsettings.service.LocationService;
import com.newgen.ntlsnc.globalsettings.service.LocationTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.newgen.ntlsnc.common.CommonConstant.*;

@RestController
@RequestMapping("/api/location")
public class LocationController {
    @Autowired
    LocationService locationService;
    @Autowired
    LocationTypeService locationTypeService;

    private static final String SCOPE = "Location";

    @PostMapping
    public ResponseEntity<?> create(@RequestBody LocationDto locationDto){
        ApiResponse response = new ApiResponse(false);

        try{
            response.setSuccess(locationService.create(locationDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody LocationDto locationDto){
        ApiResponse response = new ApiResponse(false);

        try{
            response.setSuccess(locationService.update(locationDto.getId(), locationDto));
            response.setMessage(SCOPE + UPDATE_SUCCESS_MESSAGE);
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        ApiResponse response = new ApiResponse(false);

        try{
            response.setSuccess(locationService.delete(id));
            response.setMessage(SCOPE + DELETE_SUCCESS_MESSAGE);
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id){
        ApiResponse response = new ApiResponse(false);

        try{
            response.setSuccess(locationService.findById(id));
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll(){
        ApiResponse response = new ApiResponse(false);

        try{
            response.setSuccess(locationService.findAll());
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/sales-officer-location/{companyId}/{salesOfficerId}")
    public ResponseEntity<?> getSoLocation(
            @PathVariable(name = "companyId") Long companyId,
            @PathVariable(name = "salesOfficerId") Long salesOfficerId
    ) {

        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(locationService.getSoLocation(companyId, salesOfficerId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-last-location-by-company/{companyId}")
    public ResponseEntity<?> getLastLocationByCompany ( @PathVariable(name = "companyId") Long companyId){
        ApiResponse response = new ApiResponse(false);
        try{
            response.setSuccess(locationService.findAllTerritoryByCompanyId(companyId));
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-down-to-up-location-hierarchy/{companyId}/{distributorId}")
    public ResponseEntity<?> getLocationDownToUpHierarchyByCompanyIdAndDistributorId(@PathVariable(name = "companyId") Long companyId,
                                                                                     @PathVariable(name = "distributorId") Long distributorId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(locationService.getLocationDownToUpHierarchyByCompanyIdAndDistributorId(companyId, distributorId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/all-of-a-company/{companyId}")
    public ResponseEntity<?> getCompanyLocationTree(@PathVariable(name = "companyId") Long companyId) {
        ApiResponse response = new ApiResponse(false);

        response.setSuccess(locationService.getAllChildLocationOfACompany(companyId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all-depot-level-location/{companyId}")
    public ResponseEntity<?> getAllDepotLevelLocation(@PathVariable(name = "companyId") Long companyId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(locationService.getAllDepotLevelLocation(companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all-by-location-type/{locationTypeId}")
    public ResponseEntity<?> getAllByLocationType(@PathVariable(name = "locationTypeId") Long locationTypeId) {
        ApiResponse response = new ApiResponse(false);
        try {
            LocationType locationType = locationTypeService.findById(locationTypeId);
            if(locationType!=null){
                response.setSuccess(locationService.findAllByLocationType(locationType));
            }
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-login-user-location-list")
    public ResponseEntity<?> getLoginUserLocationListByCompany(@RequestParam Long companyId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(locationService.getLoginUserLocationListByCompany(companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all-depot-level-location-with-out-depot-location-map/{companyId}")
    public ResponseEntity<?> getLocationListWithoutDepotLocationMap(@PathVariable(name = "companyId") Long companyId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(locationService.getLocationListWithoutDepotLocationMap(companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
