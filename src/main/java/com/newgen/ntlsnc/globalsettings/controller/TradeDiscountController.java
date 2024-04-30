package com.newgen.ntlsnc.globalsettings.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.dto.TradeDiscountDto;
import com.newgen.ntlsnc.globalsettings.service.TradeDiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author marziah
 */

@RestController
@RequestMapping("/api/trade-discount")
public class TradeDiscountController {
    private static final String SCOPE = "Trade Discount";

    @Autowired
    TradeDiscountService tradeDiscountService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TradeDiscountDto tradeDiscountDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(tradeDiscountService.create(tradeDiscountDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody TradeDiscountDto tradeDiscountDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(tradeDiscountService.update(tradeDiscountDto.getId(), tradeDiscountDto));
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
            response.setSuccess(tradeDiscountService.delete(id));
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
            response.setSuccess(tradeDiscountService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(tradeDiscountService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all-product")
    public ResponseEntity<?> getAllTradeDiscountListByProductCategoryWise(@RequestParam("categoryIds") Long[] categoryIds, @RequestParam(value = "accYearId",required = false) Long accYearId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(tradeDiscountService.getAllTradeDiscountListByProductCategoryWise(categoryIds, accYearId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-current-discount-by-invoice-nature")
    public ResponseEntity<?> getProductCategoryWiseTradePrice(@RequestParam Long companyId, @RequestParam Long productId, @RequestParam Long invoiceNatureId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(tradeDiscountService.findCurrentSemesterDiscountByCompanyIdAndProductId(companyId, productId, invoiceNatureId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllByProductAndInvoiceNature(@RequestParam Long productId, @RequestParam Long invoiceNatureId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(tradeDiscountService.getAllByProductAndInvoiceNature(productId, invoiceNatureId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
