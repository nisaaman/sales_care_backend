package com.newgen.ntlsnc.usermanagement.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.usermanagement.dto.ApplicationUserDto;
import com.newgen.ntlsnc.usermanagement.repository.ApplicationUserRepository;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import com.sun.istack.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author liton
 * Created on 4/17/22 3:18 PM
 */

@RestController
@RequestMapping("/auth")
public class ApplicationUserController {

    private static final String SCOPE = "Application User";

    @Autowired
    ApplicationUserService applicationUserService;

    @Autowired
    OrganizationService organizationService;

    @GetMapping("/me")
    public ResponseEntity<?> getMe() {
        try {
            return ResponseEntity.ok(applicationUserService.getMe());
        } catch (Exception ex) {
            return ResponseEntity.ok(ex.getMessage());
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(@RequestPart(value = "applicationUserDto") @Valid ApplicationUserDto applicationUserDto,
                                    @RequestPart(value = "profileImage", required = false) @Nullable MultipartFile profileImage) {
        ApiResponse response = new ApiResponse(false);

        try {
            if (applicationUserDto != null) {
                applicationUserDto.setProfileImage(profileImage);
            }

            response.setSuccess(applicationUserService.create(applicationUserDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(@RequestPart(value = "applicationUserDto") @Valid ApplicationUserDto applicationUserDto,
                                    @RequestPart(value = "profileImage", required = false) @Nullable MultipartFile profileImage) {
        ApiResponse response = new ApiResponse(false);

        try {
            if (applicationUserDto != null) {
                applicationUserDto.setProfileImage(profileImage);
            }
            response.setSuccess(applicationUserService.update(applicationUserDto.getId(), applicationUserDto));
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
            response.setSuccess(applicationUserService.delete(id));
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
            response.setSuccess(applicationUserService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(applicationUserService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-active-users")
    public ResponseEntity<?> findAllActiveUser() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(applicationUserService.findAllActiveUser());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-SO-by-location/{locationId}/{companyId}")
    public ResponseEntity<?> getSoListByLocation(@PathVariable(name = "locationId") Long locationId, @PathVariable(name = "companyId") Long companyId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(applicationUserService.getSoDetailsByLocation(locationId, companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping({"/get-so-details", "/get-so-details/{companyId}"})
    public ResponseEntity<?> getSoDetails(@PathVariable(name = "companyId", required = false) Long companyId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(applicationUserService.getSoDetails(companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/application-user-details-with-profile-image/{id}")
    public ResponseEntity<?> getApplicationUserDetailsWithProfileImage(@PathVariable Long id) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(applicationUserService.getApplicationUserDetailsWithProfileImage(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/update-status")
    public ResponseEntity<?> update(@RequestBody @Valid ApplicationUserDto applicationUserDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(applicationUserService.updateUserStatus(applicationUserDto.getId(), applicationUserDto));
            response.setMessage("User Status" + UPDATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all-user-company-id/{companyId}")
    public ResponseEntity<?> findAllByCompany(@PathVariable(name = "companyId") Long companyId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(applicationUserService.findAllByCompany(companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all-user-company-id-without-zonal-manager/{companyId}")
    public ResponseEntity<?> getByCompanyWithoutZonalManager(@PathVariable(name = "companyId") Long companyId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(applicationUserService.getByCompanyWithoutZonalManager(companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/search-user")
    public ResponseEntity<?> getTicketCompanyWise(@RequestParam String searchString) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(applicationUserService.getSearchUser(searchString));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-user-list-company-wise")
    public ResponseEntity<?> getUserListByCompanyWise(@RequestParam List<Long> companyIds) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(applicationUserService.getUserListByCompanyWise(companyIds));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-sales-officer-location/{locationId}/{companyId}")
    public ResponseEntity<?> getSalesOfficerByLocation(@PathVariable(name = "locationId") Long locationId, @PathVariable(name = "companyId") Long companyId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(applicationUserService.getSalesOfficerByLocation(locationId, companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @GetMapping("/get-sales-officer-by-location-wise")
    public ResponseEntity<?> getSalesOfficerByLocationWise(@RequestParam Long companyId,
                                                           @RequestParam(required = false) List<Long> locationIds) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(applicationUserService.getSalesOfficerByLocationWise(companyId, locationIds));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/add-user-fcm-id")
    public ResponseEntity<?> addFCMId(@RequestBody @Valid Map fcmKey) {
        ApiResponse response = new ApiResponse(false);
        try {
            applicationUserService.addUserFCMId(fcmKey);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
