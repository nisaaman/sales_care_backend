package com.newgen.ntlsnc.globalsettings.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.dto.OverridingDiscountDto;
import com.newgen.ntlsnc.globalsettings.service.OverridingDiscountSetupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author sagor
 * @date ৯/৪/২২
 */

@RestController
@RequestMapping("/api/overriding-discount")
public class OverridingDiscountController {
    private static final String SCOPE = "Overriding Discount";

    @Autowired
    OverridingDiscountSetupService overridingDiscountService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody OverridingDiscountDto overridingDiscountDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(overridingDiscountService.create(overridingDiscountDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/create-all")
    public ResponseEntity<?> createAll(@RequestBody List<OverridingDiscountDto> overridingDiscountDtoList) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(overridingDiscountService.createAll(overridingDiscountDtoList));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody OverridingDiscountDto overridingDiscountDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(overridingDiscountService.update(overridingDiscountDto.getId(), overridingDiscountDto));
            response.setMessage(SCOPE + UPDATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/update-all")
    public ResponseEntity<?> updateAll(@RequestBody List<OverridingDiscountDto> overridingDiscountDtoList) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(overridingDiscountService.updateAll(overridingDiscountDtoList));
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
            response.setSuccess(overridingDiscountService.delete(id));
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
            response.setSuccess(overridingDiscountService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(overridingDiscountService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-invoice-wise-overriding-discount-list")
    public ResponseEntity<?> getInvoiceWiseOverridingDiscountList(@RequestParam Long companyId,@RequestParam(required = false) Long salesOfficerId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(overridingDiscountService.getInvoiceWiseOverridingDiscountList(companyId,salesOfficerId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-overriding-discount-details-of-a-sales-invoice")
    public ResponseEntity<?> getOverridingDiscountDetailsOfASalesInvoice(@RequestParam Long companyId,@RequestParam Long salesInvoiceId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(overridingDiscountService.getOverridingDiscountDetailsOfASalesInvoice(companyId,salesInvoiceId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-list-with-related-info/{companyId}")
    public ResponseEntity<?> getOrdListWithRelatedInfoByCompany(@PathVariable Long companyId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(overridingDiscountService.getOrdListForListPageByCompany(companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
