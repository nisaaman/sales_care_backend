package com.newgen.ntlsnc.supplychainmanagement.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.supplychainmanagement.dto.PickingDetailsListDto;
import com.newgen.ntlsnc.supplychainmanagement.dto.PickingDto;
import com.newgen.ntlsnc.supplychainmanagement.entity.Picking;
import com.newgen.ntlsnc.supplychainmanagement.service.PickingDetailsService;
import com.newgen.ntlsnc.supplychainmanagement.service.PickingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author kamal
 * @Date ১৮/৪/২২
 */

@RestController
@RequestMapping("/api/picking")
public class PickingController {
    private static final String SCOPE = "Picking";

    @Autowired
    PickingService pickingService;

    @Autowired
    PickingDetailsService pickingDetailsService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody PickingDto pickingDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            Picking pickingObj = pickingService.create(pickingDto);
            if (pickingObj==null) {
                response.setError(SCOPE + CREATE_ERROR_MESSAGE);
            }
            else {
                response.setSuccess(pickingObj);
                response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
            }

        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody PickingDto pickingDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(pickingService.update(pickingDto.getId(), pickingDto));
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
            response.setSuccess(pickingService.delete(id));
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
            response.setSuccess(pickingService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(pickingService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-order-list-by-picking-wise/{pickingId}")
    public ResponseEntity<?> getOrderListByPickingIdWise(@PathVariable Long pickingId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(pickingService.getOrderListByPickingIdWise(pickingId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @GetMapping("/get-picking-list-distributor-company-wise/{companyId}/{distributorId}")
    public ResponseEntity<?> getPickingListByDistributorWise(@PathVariable Long companyId, @PathVariable Long distributorId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(pickingService.getPickingListByDistributorWise(companyId, distributorId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @GetMapping("/get-product-list-picking-id-wise/{pickingId}")
    public ResponseEntity<?> getProductListByPickingId(@PathVariable Long pickingId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(pickingService.getProductListByPickingId(pickingId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/confirm-picking")
    public ResponseEntity<?> confirmPicking(@RequestBody PickingDetailsListDto pickingDetailsListDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(true);
            response.setMessage(pickingDetailsService.confirmPicking(pickingDetailsListDto));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/cancel-picking")
    public ResponseEntity<?> rejectPicking(@RequestBody PickingDto pickingDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            Picking picking = pickingService.rejectPicking(pickingDto);
            if (picking!=null) {
                response.setSuccess(picking);
                response.setMessage(SCOPE + " Successfully Rejected!");
            }
            else {
                response.setMessage(SCOPE + PICKING_DELIVERED_MESSAGE);
            }

        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
