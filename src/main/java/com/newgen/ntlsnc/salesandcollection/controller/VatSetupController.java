package com.newgen.ntlsnc.salesandcollection.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.salesandcollection.dto.VatSetupDto;
import com.newgen.ntlsnc.salesandcollection.service.VatSetupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author anika
 * @Date ২৪/৪/২২
 */
@RestController
@RequestMapping("/api/vat-setup")
public class VatSetupController {
    private static final String SCOPE = "Vat";
    @Autowired
    VatSetupService vatSetupService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody VatSetupDto vatSetupDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(vatSetupService.create(vatSetupDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody VatSetupDto vatSetupDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(vatSetupService.update(vatSetupDto.getId(), vatSetupDto));
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
            response.setSuccess(vatSetupService.delete(id));
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
            response.setSuccess(vatSetupService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(vatSetupService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllByProductId(@RequestParam Long productId, @RequestParam Boolean vatIncluded) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(vatSetupService.getAllByProductAndVatIncluded(productId, vatIncluded));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all-with-current")
    public ResponseEntity<?> getAllWithCurrentByProductId(@RequestParam Long productId, @RequestParam Boolean vatIncluded) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(vatSetupService.getAllWithCurrentVatByProductAndVatIncluded(productId, vatIncluded));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
