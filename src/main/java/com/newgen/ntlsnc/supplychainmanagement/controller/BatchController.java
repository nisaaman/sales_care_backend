package com.newgen.ntlsnc.supplychainmanagement.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.common.enums.StoreType;
import com.newgen.ntlsnc.supplychainmanagement.dto.BatchDto;
import com.newgen.ntlsnc.supplychainmanagement.dto.QRCodeDto;
import com.newgen.ntlsnc.supplychainmanagement.entity.Batch;
import com.newgen.ntlsnc.supplychainmanagement.service.BatchDetailsService;
import com.newgen.ntlsnc.supplychainmanagement.service.BatchService;
import com.newgen.ntlsnc.supplychainmanagement.service.QRCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author kamal
 * @Date ১৩/৪/২২
 */

@RestController
@RequestMapping("/api/batch")
public class BatchController {
    private static final String SCOPE = "Batch";

    @Autowired
    BatchService batchService;
    @Autowired
    QRCodeService qrCodeService;
    @Autowired
    BatchDetailsService batchDetailsService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody BatchDto batchDto) {
        ApiResponse response = new ApiResponse(false);

        try {

            Batch batch = batchService.create(batchDto);
            if(batch == null) {
                response.setError("Duplicate Batch No...");

            } else {
                response.setSuccess(batch.getBatchNo());
                response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
            }


        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody BatchDto batchDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(batchService.update(batchDto.getId(), batchDto));
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
            response.setSuccess(batchService.delete(id));
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
            response.setSuccess(batchService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(batchService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-list-by-challan-and-product")
    public ResponseEntity<?> getReturnProductsById(@RequestParam Long deliveryChallanId, @RequestParam Long productId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(batchService.getAllByDeliveryChallanIdAndProductId(deliveryChallanId, productId));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/number-list-company-wise/{companyId}")
    public ResponseEntity<?> getBatchNoListFromACompany(@PathVariable Long companyId){

        ApiResponse response = new ApiResponse(false);
        response.setSuccess(batchService.getBatchNoListFromACompany(companyId));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/wise-qr-code/{batchId}")
    public ResponseEntity<?> getBatchWiseQRCode(@PathVariable Long batchId){

        ApiResponse response = new ApiResponse(false);
        QRCodeDto qrCodeDto = new QRCodeDto();

        qrCodeDto.setBatchDetails(batchDetailsService.getBatchDetails(batchId));
        qrCodeDto.setMqrByte(qrCodeService.getMQRCode(batchId));
        qrCodeDto.setIqrByte(qrCodeService.getIQRCode(batchId));
        qrCodeDto.setIqrPrintQuantity(qrCodeService.getIqrPrintQuantity(batchId));
        qrCodeDto.setMqrPrintQuantity(qrCodeService.getMqrPrintQuantity(batchId));
        response.setSuccess(qrCodeDto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/availability-check/{productId}")
    public ResponseEntity<?> checkProductBatchAvailability(@PathVariable Long productId){

        ApiResponse response = new ApiResponse(false);
        boolean availabilityStatus = false;
        if (availabilityStatus == true) {
            response.setSuccess(true);
        } else {
            response.setError("");
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/product-wise-latest-batch-info/{productId}")
    public ResponseEntity<?> getProductLatestBatchInfo(
            @PathVariable Long productId){

        ApiResponse response = new ApiResponse(false);
        response.setSuccess(batchService.getProductLatestBatchInfo(productId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/product-and-batch-wise-batch-info/{productId}/{batchId}")
    public ResponseEntity<?> getProductBatchInfoByProductIdAndBatchId(
            @PathVariable Long productId, @PathVariable Long batchId){

        ApiResponse response = new ApiResponse(false);
        response.setSuccess(batchService.getProductBatchInfoByProductIdAndBatchId(
                productId,batchId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/auto-complete-data")
    public ResponseEntity<?> getBatchNoList(
            @RequestParam(value = "productId", required = true) Long productId,
            @RequestParam(value = "searchString", required = false) String searchString) {

        ApiResponse response = new ApiResponse(false);
        response.setSuccess(batchService.getBatchNoAutoCompleteList(productId,searchString));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product-wise-all-batch-info/{productId}")
    public ResponseEntity<?> getAllBatchFromAProduct(
            @PathVariable Long productId){

        ApiResponse response = new ApiResponse(false);
        response.setSuccess(batchService.getAllBatchFromAProduct(productId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/depot-and-product-and-store-wise-info")
    public ResponseEntity<?> getDepotAndStoreWiseBatchAndStockInfo(
            @RequestParam(value = "companyId") Long companyId,
            @RequestParam(value = "depotId") Long depotId,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "storeType") String storeType,
            @RequestParam(value = "searchString", required = false) String searchString) {

        ApiResponse response = new ApiResponse(false);
        response.setSuccess(batchService.getDepotAndStoreWiseBatchAndStockInfo(
                companyId, depotId, productId, storeType, searchString));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/batch-wise-batch-info/{batchNo}")
    public ResponseEntity<?> getProductBatchInfoByBatchNo(
            @PathVariable String batchNo){

        ApiResponse response = new ApiResponse(false);
        response.setSuccess(batchService.getProductBatchInfoByBatchNo(batchNo));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
