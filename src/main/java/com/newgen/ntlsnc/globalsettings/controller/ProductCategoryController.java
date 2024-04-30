package com.newgen.ntlsnc.globalsettings.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.dto.ProductCategoryDto;
import com.newgen.ntlsnc.globalsettings.dto.ProductCategoryTypeDto;
import com.newgen.ntlsnc.globalsettings.service.ProductCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author sagor
 * @date ৬/৪/২২
 */
@RestController
@RequestMapping("/api/product-category")
public class ProductCategoryController {

    private static final String SCOPE = "Product Category";

    @Autowired
    ProductCategoryService productCategoryService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProductCategoryDto productCategoryDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(productCategoryService.create(productCategoryDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody ProductCategoryDto productCategoryDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(productCategoryService.updateSingleProductCategory(productCategoryDto.getId(), productCategoryDto));
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
            response.setSuccess(productCategoryService.deleteSingleProductCategory(id));
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
            response.setSuccess(productCategoryService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(productCategoryService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @GetMapping("/get-all-category-list")
    public ResponseEntity<?> getAllCategoryList(@RequestParam ("companyId") Long companyId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(productCategoryService.findAllCategoryList(companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all-sub-category-list")
    public ResponseEntity<?> getAllSubCategoryList(@RequestParam ("parentCategoryId") Long parentCategoryId ) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(productCategoryService.findAllSubCategoryList(parentCategoryId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/create-all")
    public ResponseEntity<?> createAll(@RequestBody ProductCategoryDto productCategoryDto){
        ApiResponse response = new ApiResponse(false);
        try{

            response.setSuccess(productCategoryService.createAll(productCategoryDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);

        }catch (Exception ex){
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/update-all")
    public ResponseEntity<?> updateAll(@RequestBody ProductCategoryDto productCategoryDto){
        ApiResponse response = new ApiResponse(false);
        try{

            response.setSuccess(productCategoryService.createAll(productCategoryDto));
            response.setMessage(SCOPE + UPDATE_SUCCESS_MESSAGE);

        }catch (Exception ex){
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/list-info/{companyId}")
    public ResponseEntity<?> getAllProductCategoryTreeByCompanyWise(@PathVariable Long companyId){
        ApiResponse response = new ApiResponse(false);

        try{
            response.setSuccess(productCategoryService.getAllProductCategoryTreeByCompanyWise(companyId));
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all-product")
    public ResponseEntity<?> getAllProductProfileListByProductCategoryWise(@RequestParam("ids") Long[] ids){
        ApiResponse response = new ApiResponse(false);
        try{
            response.setSuccess(productCategoryService.getAllProductProfileListByProductCategoryWise(ids));
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/child")
    public ResponseEntity<?> getChildCategoryOfACompany(@RequestParam("companyId") Long companyId){
        ApiResponse response = new ApiResponse(false);

        response.setSuccess(productCategoryService.getChildCategoryOfACompany(companyId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @GetMapping("/top-parent")
    public ResponseEntity<?> getTopParentByChild(@RequestParam("productCategoryId") Long productCategoryId){
        ApiResponse response = new ApiResponse(false);

        response.setSuccess(productCategoryService.getTopParentByChild(productCategoryId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
