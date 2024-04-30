package com.newgen.ntlsnc.supplychainmanagement.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.service.DepotService;
import com.newgen.ntlsnc.supplychainmanagement.dto.InterStoreStockMovementDto;
import com.newgen.ntlsnc.supplychainmanagement.service.InterStoreStockMovementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;

import static com.newgen.ntlsnc.common.CommonConstant.CREATE_SUCCESS_MESSAGE;
import static com.newgen.ntlsnc.common.CommonConstant.STOCK_MOVEMENT_SUCCESS_MESSAGE;


/**
 * @author Newaz Sharif
 * @since 3rd Oct, 22
 */
@RestController
@RequestMapping("/api/inter-store-stock-movement")
public class InterStoreStockMovementController {

    @Autowired
    InterStoreStockMovementService interStoreStockMovementService;
    @Autowired
    DepotService depotService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody InterStoreStockMovementDto interStoreStockMovementDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(interStoreStockMovementService.create(interStoreStockMovementDto));
            response.setMessage(STOCK_MOVEMENT_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/details/data")
    public ResponseEntity<?> getInterStoreStockMovementDetails(
            @RequestParam Long companyId,
            @RequestParam Long userLoginId,
            @RequestParam(required = false) Long accountingYearId) {
        ApiResponse response = new ApiResponse(false);

        Map depotMap = depotService.getDepotByLoginUserId(companyId, userLoginId);
        if(depotMap.size() != 0) {
            Long depotId =  Long.parseLong(String.valueOf(depotMap.get("id")));
                    response.setSuccess(interStoreStockMovementService.getInterStoreStockMovementDetails(
                            companyId, depotId, accountingYearId));
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        response.setSuccess(new ArrayList<>());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @GetMapping("/depot-and-product-and-store-wise-qc-info")
    public ResponseEntity<?> getDepotAndStoreWiseBatchAndStockInfo(
            @RequestParam(value = "companyId") Long companyId,
            @RequestParam(value = "depotId") Long depotId,
            @RequestParam(value = "productId",required = false) Long productId,
            @RequestParam(value = "storeType") String storeType,
            @RequestParam("batchId") Long batchId,
            @RequestParam("qaStatus") String qaStatus) {

        ApiResponse response = new ApiResponse(false);
        response.setSuccess(interStoreStockMovementService.getQCStockQCInfo(
                companyId, depotId, productId, storeType, batchId, qaStatus));
        return ResponseEntity.ok(response);
    }
}
