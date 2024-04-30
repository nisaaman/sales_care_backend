package com.newgen.ntlsnc.supplychainmanagement.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.service.DepotService;
import com.newgen.ntlsnc.supplychainmanagement.dto.BatchDto;
import com.newgen.ntlsnc.supplychainmanagement.dto.InvReceiveDto;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvReceive;
import com.newgen.ntlsnc.supplychainmanagement.service.InvReceiveService;
import com.newgen.ntlsnc.supplychainmanagement.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author kamal
 * @Date ১৭/৪/২২
 */

@RestController
@RequestMapping("/api/inv-receive")
public class InvReceiveController {
    private static final String SCOPE = "Inventory ";

    @Autowired
    InvReceiveService invReceiveService;


    @PostMapping
    public ResponseEntity<?> create(@RequestBody InvReceiveDto invReceiveDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(invReceiveService.create(invReceiveDto));
            response.setMessage(SCOPE + RECEIVE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody InvReceiveDto invReceiveDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(invReceiveService.update(invReceiveDto.getId(), invReceiveDto));
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
            response.setSuccess(invReceiveService.delete(id));
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
            response.setSuccess(invReceiveService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(invReceiveService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/store/list/{companyId}")
    public ResponseEntity<?> getProductionStoreList(@PathVariable Long companyId) {
        ApiResponse response = new ApiResponse(false);
        response.setSuccess(invReceiveService.getProductionReceiveStoreList(companyId));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/details/data")
    public ResponseEntity<?> getInvProductionReceiveDetailsDataList(
            @RequestParam Long companyId, @RequestParam(required = false) Long accountingYearId) {
        ApiResponse response = new ApiResponse(false);
        response.setSuccess(invReceiveService.getInvProductionReceiveDetailsDataList(
                                companyId, accountingYearId));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/transfer-transaction-list-to-receive")
    public ResponseEntity<?> getTransferTransactionListToReceive(
            @RequestParam(value = "companyId") Long companyId,
            @RequestParam(value = "accountingYearId", required = false) Long accountingYearId,
            @RequestParam(value = "depotId", required = false) Long depotId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(invReceiveService.
                    getTransferTransactionListToReceive(companyId,
                            accountingYearId, depotId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/set-production-manufacturing-cost")
    public ResponseEntity<?> setProductionManufacturingCost(@RequestBody @Valid ArrayList<Map> objects) {
        ApiResponse response = new ApiResponse(false);
        try {
            invReceiveService.setProductionManufacturingCost(objects);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
