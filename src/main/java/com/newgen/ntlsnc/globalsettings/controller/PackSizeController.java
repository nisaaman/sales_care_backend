package com.newgen.ntlsnc.globalsettings.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.dto.PackSizeDto;
import com.newgen.ntlsnc.globalsettings.service.PackSizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author marzia
 * Created on 5/4/22 03:00 PM
 */

@RestController
@RequestMapping("/api/pack-size")
public class PackSizeController {

    private static final String SCOPE = "PackSize";

    @Autowired
    PackSizeService packSizeService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid PackSizeDto packSizeDto){
        ApiResponse response = new ApiResponse(false);

        try{
            response.setSuccess(packSizeService.create(packSizeDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody @Valid PackSizeDto packSizeDto){
        ApiResponse response = new ApiResponse(false);

        try{
            response.setSuccess(packSizeService.update(packSizeDto.getId(), packSizeDto));
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
            response.setSuccess(packSizeService.delete(id));
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
            response.setSuccess(packSizeService.findById(id));
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll(){
        ApiResponse response = new ApiResponse(false);

        try{
            response.setSuccess(packSizeService.findAll());
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
