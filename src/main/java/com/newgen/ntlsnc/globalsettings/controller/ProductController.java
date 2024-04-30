package com.newgen.ntlsnc.globalsettings.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.dto.ProductDto;
import com.newgen.ntlsnc.globalsettings.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author kamal
 */
@RestController
@RequestMapping("/api/product")

public class ProductController {
    private static final String SCOPE = "Product Profile";

    @Autowired
    ProductService productService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProductDto productDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(productService.create(productDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody ProductDto productDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(productService.update(productDto.getId(), productDto));
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
            response.setSuccess(productService.delete(id));
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
            response.setSuccess(productService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(productService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all-product-list")
    public ResponseEntity<?> getAllProductList(@RequestParam(value = "semesterId",required = false) Long semesterId ,@RequestParam ("companyId") Long companyId,@RequestParam("productCategoryId") Long productCategoryId, @RequestParam("invoiceNatureId") Long invoiceNatureId, @RequestParam(required = false) Long salesBookingId ) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(productService.findAllProductList(semesterId,companyId,productCategoryId,invoiceNatureId,salesBookingId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/all/company-wise")
    public ResponseEntity<?> getAllProductOfACompany(
            @RequestParam Long companyId,
                    @RequestParam(required = false) Long productCategoryId) {
        ApiResponse response = new ApiResponse(false);
        response.setSuccess(productService.getAllProductOfACompany(companyId,productCategoryId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-product-wise-depot")
    public ResponseEntity<?> getProductWiseDepotList(@RequestParam(value = "companyId") Long companyId ,
                                                     @RequestParam("productId") Long productId ) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(productService.getProductWiseDepotList(companyId,productId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-product-category-wise-product")
    public ResponseEntity<?> getProductCategoryWiseProductList(@RequestParam(value = "companyId") Long companyId ,
                                                     @RequestParam("productCategoryId") Long productCategoryId ) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(productService.getProductCategoryWiseProductList(companyId,productCategoryId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/all/production-receivable")
    public ResponseEntity<?> getAllProductionRecievableProduct(
            @RequestParam Long companyId,
            @RequestParam(required = false) Long productCategoryId) {
        ApiResponse response = new ApiResponse(false);
        response.setSuccess(productService.getAllProductionRecievableProduct(companyId, productCategoryId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
