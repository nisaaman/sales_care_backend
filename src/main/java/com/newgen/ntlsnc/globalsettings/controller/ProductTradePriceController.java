package com.newgen.ntlsnc.globalsettings.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.dto.ProductTradePriceDto;
import com.newgen.ntlsnc.globalsettings.service.ProductTradePriceService;
import com.newgen.ntlsnc.salesandcollection.dto.DistributorOpeningBalanceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

import static com.newgen.ntlsnc.common.CommonConstant.*;

@RestController
@RequestMapping("/api/product-trade-price")
public class ProductTradePriceController {
    private static final String SCOPE = "Product Trade Price";


    @Autowired
    ProductTradePriceService productTradePriceService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProductTradePriceDto productTradePriceDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(productTradePriceService.create(productTradePriceDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody ProductTradePriceDto productTradePriceDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(productTradePriceService.update(productTradePriceDto.getId(), productTradePriceDto));
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
            boolean isSuccess = productTradePriceService.delete(id);
            response.setSuccess(isSuccess);
            String message = "";
            if (isSuccess) {
                message = SCOPE + DELETE_SUCCESS_MESSAGE;
            } else {
                message = SCOPE + DELETE_EXIST_MESSAGE + "Sales Booking";
            }
            response.setMessage(message);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(productTradePriceService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(productTradePriceService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-trade-price-list-view")
    public ResponseEntity<?> getProductCategoryWiseTradePrice(@RequestParam Long companyId, @RequestParam(required = false) List<Long> productCategoryIds) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(productTradePriceService.findAllListByOrganizationIdAndProductCategoryId(companyId, productCategoryIds));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/sum/{companyId}")
    public ResponseEntity<?> getTradePriceSum(@PathVariable Long companyId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(productTradePriceService.getTradePriceSumWithProductCountByCompany(companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all/{productId}")
    public ResponseEntity<?> getAllByProductId(@PathVariable Long productId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(productTradePriceService.getAllByProductId(productId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(path = "/upload-trade-price", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> save(
            @RequestPart(value = "tradePriceFile") MultipartFile tradePriceFile) {
        ApiResponse response = new ApiResponse(false);
        if(tradePriceFile != null){
            String status = productTradePriceService.tradePriceUpload(tradePriceFile);
            if ("success".equalsIgnoreCase(status)) {
                response.setSuccess(true);
            } else {
                response.setError(status);
            }
        } else {
            response.setError("File Not Found");
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
