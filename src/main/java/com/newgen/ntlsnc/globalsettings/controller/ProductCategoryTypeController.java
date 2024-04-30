package com.newgen.ntlsnc.globalsettings.controller;

import antlr.collections.List;
import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.dto.ProductCategoryTypeDto;
import com.newgen.ntlsnc.globalsettings.service.ProductCategoryTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author marziah
 */

@RestController
@RequestMapping("/api/product-category-type")
public class ProductCategoryTypeController {

    private static final String SCOPE = "Product Category Type";

    @Autowired
    ProductCategoryTypeService categoryTypeService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProductCategoryTypeDto categoryTypeDto){
        ApiResponse response = new ApiResponse(false);

        try{
            response.setSuccess(categoryTypeService.create(categoryTypeDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody ProductCategoryTypeDto categoryTypeDto){
        ApiResponse response = new ApiResponse(false);

        try{
            response.setSuccess(categoryTypeService.update(categoryTypeDto.getId(), categoryTypeDto));
            response.setMessage(SCOPE + UPDATE_SUCCESS_MESSAGE);
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        ApiResponse response = new ApiResponse(false);

        try{
            response.setSuccess(categoryTypeService.delete(id));
            response.setMessage(SCOPE + DELETE_SUCCESS_MESSAGE);
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id){
        ApiResponse response = new ApiResponse(false);

        try{
            response.setSuccess(categoryTypeService.findById(id));
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll(){
        ApiResponse response = new ApiResponse(false);

        try{
            response.setSuccess(categoryTypeService.findAll());
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/create-all")
    public ResponseEntity<?> createAll(@RequestBody ProductCategoryTypeDto categoryTypeDto){
        ApiResponse response = new ApiResponse(false);

        try{
            response.setSuccess(categoryTypeService.createAll(categoryTypeDto.getProductCategoryTypeDtoList()));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }

            return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/update-all")
    public ResponseEntity<?> updateAll(@RequestBody ProductCategoryTypeDto categoryTypeDto){
        ApiResponse response = new ApiResponse(false);

        try{
            response.setSuccess(categoryTypeService.createAll(categoryTypeDto.getProductCategoryTypeDtoList()));
            response.setMessage(SCOPE + UPDATE_SUCCESS_MESSAGE);
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }

            return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/list-info/{id}")
    public ResponseEntity<?> getAllProductCategoryTypeByCompanyWise(@PathVariable Long id){
        ApiResponse response = new ApiResponse(false);

        try{
            response.setSuccess(categoryTypeService.getAllProductCategoryTypeDtoByCompanyWise(id));
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

