package com.newgen.ntlsnc.globalsettings.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.dto.DepartmentDto;
import com.newgen.ntlsnc.globalsettings.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author liton
 * Created on 4/3/22 11:51 AM
 */

@RestController
@RequestMapping("/api/department")
public class DepartmentController {

    private static final String SCOPE = "Department";

    @Autowired
    DepartmentService departmentService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid DepartmentDto departmentDto){
        ApiResponse response = new ApiResponse(false);

        try{
            response.setSuccess(departmentService.create(departmentDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody @Valid DepartmentDto departmentDto){
        ApiResponse response = new ApiResponse(false);

        try{
            response.setSuccess(departmentService.update(departmentDto.getId(), departmentDto));
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
            response.setSuccess(departmentService.delete(id));
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
            response.setSuccess(departmentService.findById(id));
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll(){
        ApiResponse response = new ApiResponse(false);

        try{
            response.setSuccess(departmentService.findAll());
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping({"/getAllActiveDepartment"})
    public ResponseEntity<?> getAllActiveDepartment(){
        ApiResponse response = new ApiResponse(false);

        try{
            response.setSuccess(departmentService.findAllActiveDepartment());
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
