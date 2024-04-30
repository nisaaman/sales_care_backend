package com.newgen.ntlsnc.supplychainmanagement.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.supplychainmanagement.dto.InvDeliveryChallanDto;
import com.newgen.ntlsnc.supplychainmanagement.service.InvDeliveryChallanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author anika
 * @Date ১৭/৪/২২
 */
@RestController
@RequestMapping("/api/delivery-challan")
public class InvDeliveryChallanController {
    private static final String SCOPE = "Inventory Delivery Challan";

    @Autowired
    InvDeliveryChallanService invDeliveryChallanService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody InvDeliveryChallanDto invDeliveryChallanDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            /*response.setSuccess(invDeliveryChallanService.create(invDeliveryChallanDto));*/
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody InvDeliveryChallanDto invDeliveryChallanDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(invDeliveryChallanService.update(invDeliveryChallanDto.getId(), invDeliveryChallanDto));
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
            response.setSuccess(invDeliveryChallanService.delete(id));
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
            response.setSuccess(invDeliveryChallanService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(invDeliveryChallanService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-distributor-list-with-balance-and-total-challan-no")
    public ResponseEntity<?> getDistributorListWithBalanceAndTotalChallanNo(@RequestParam Long companyId,
                                                                            @RequestParam List<Long> locationIds,
                                                                            @RequestParam(required = false) Long accountingYearId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(invDeliveryChallanService
                    .getDistributorListWithTotalChallanNoByCompanyAndLocationAndAccountingYear(companyId, locationIds, accountingYearId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all-delivery-challan-list")
    public ResponseEntity<?> getAllDeliveryChallanList(@RequestParam Long companyId,
                                                       @RequestParam Long invoiceNatureId,
                                                       @RequestParam Long distributorId,
                                                       @RequestParam(required = false) Long accountingYearId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(invDeliveryChallanService
                    .getAllDeliveryChallanByCompanyAndInvoiceNatureAndDistributorAndAccountingYearId(companyId, invoiceNatureId, distributorId, accountingYearId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @GetMapping("/distributor-list-for-challan")
    public ResponseEntity<?> getDepotDistributorListForChallan(
            @RequestParam Map<String, Object> searchParams) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(
                    invDeliveryChallanService.getDepotDistributorListForChallan(searchParams));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @GetMapping("/order-product-list/{orderId}/{depotId}")
    public ResponseEntity<?> getProductList(@PathVariable Long orderId,
                                            @PathVariable Long depotId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(
                    invDeliveryChallanService.getProductList(orderId, depotId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    @GetMapping("/driver-list")
    public ResponseEntity<?> getDriverList() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(
                    invDeliveryChallanService.getDriverList());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }
    @GetMapping("/distributor-wise-delivery-challan/{companyId}/{distributorId}")
    public ResponseEntity<?> getDistributorWiseDeliveryChallanList(@PathVariable ("companyId") Long companyId,@PathVariable ("distributorId") Long distributorId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(
                    invDeliveryChallanService.getDistributorWiseDeliveryChallanList(companyId,distributorId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    @GetMapping("/picking-order-product-list")
    public ResponseEntity<?> getProductListByPickingIdAndOrderId(@RequestParam Long pickingId, @RequestParam Long orderId, @RequestParam Long depotId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(
                    invDeliveryChallanService.getProductListByPickingIdAndOrderId(pickingId, orderId, depotId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    }
