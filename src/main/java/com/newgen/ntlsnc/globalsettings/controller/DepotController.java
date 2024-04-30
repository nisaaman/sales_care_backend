package com.newgen.ntlsnc.globalsettings.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.dto.DepotDto;
import com.newgen.ntlsnc.globalsettings.service.DepotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.newgen.ntlsnc.common.CommonConstant.*;

@RestController
@RequestMapping("/api/depot")
public class DepotController {
    private static final String SCOPE = "Depot Profile";

    @Autowired
    DepotService depotService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody DepotDto depotDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(depotService.create(depotDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody DepotDto depotDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(depotService.update(depotDto.getId(), depotDto));
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
            response.setSuccess(depotService.delete(id));
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
            response.setSuccess(depotService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(depotService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/list")
    public ResponseEntity<?> getList(@RequestBody Map searchParams) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(depotService.list(searchParams));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/update-active-status/{id}")
    public ResponseEntity<?> updateActiveStatus(@PathVariable String id) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(depotService.updateActiveStatus(Long.parseLong(id)));
            response.setMessage(SCOPE + UPDATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/depot-info/{id}")
    public ResponseEntity<?> getDepotInfo(@PathVariable Long id) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(depotService.findDepotInfoById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-depot-by-sales-officer-id/{companyId}/{salesOfficerId}")
    public ResponseEntity<?> getDepotByDistributorId(@PathVariable Long companyId,
                                                     @PathVariable Long salesOfficerId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(depotService.getSalesOfficerDepotInfo(companyId, salesOfficerId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/area-list/{companyId}/{userLoginId}")
    public ResponseEntity<?> getDepotAreaList(@PathVariable Long companyId,
                                              @PathVariable Long userLoginId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(depotService.findDepotAreaList(companyId, userLoginId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/user-depot/{companyId}/{userLoginId}")
    public ResponseEntity<?> getDepotByLoginUserId(@PathVariable Long companyId,
                                              @PathVariable Long userLoginId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(depotService.getDepotByLoginUserId(companyId, userLoginId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/central-warehouse/{companyId}")
    public ResponseEntity<?> getCompanyCentralWareHouseInfo(@PathVariable Long companyId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(depotService.getCompanyCentralWareHouseInfo(companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/all-of-a-company/{companyId}")
    public ResponseEntity<?> getCompanyAllDepots(@PathVariable Long companyId) {
        ApiResponse response = new ApiResponse(false);

        response.setSuccess(depotService.getCompanyAllDepots(companyId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/central-warehouse/{companyId}/{userLoginId}")
    public ResponseEntity<?> getCompanyUserCentralWareHouseInfo(
            @PathVariable Long companyId, @PathVariable Long userLoginId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(depotService.getCompanyUserCentralWareHouseInfo(companyId,userLoginId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-depot-filter")
    public ResponseEntity<?> getDepotFiltered(
            @RequestParam(value = "companyId") Long companyId,
            @RequestParam(value = "salesOfficerList", required = false) List salesOfficerList) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(depotService.getDepotListFiltered(companyId, salesOfficerList));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/check-user-assigned-for-qa")
    public ResponseEntity<?> getUserQaAssignedStatus(
            @RequestParam(value = "companyId") Long companyId,
            @RequestParam(value = "userId") Long userId,
            @RequestParam(value = "depotId", required = false) Long depotId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(depotService.getUserQaAssignedStatus(companyId, userId, depotId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/check-user-assigned-for-depot-incharge")
    public ResponseEntity<?> getUserDepotInchargeAssignedStatus(
            @RequestParam(value = "companyId") Long companyId,
            @RequestParam(value = "userId") Long userId,
            @RequestParam(value = "depotId", required = false) Long depotId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(depotService.getUserDepotInchargeAssignedStatus(companyId, userId, depotId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
