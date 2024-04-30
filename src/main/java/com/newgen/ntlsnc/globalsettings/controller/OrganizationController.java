package com.newgen.ntlsnc.globalsettings.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.dto.OrganizationDto;
import com.newgen.ntlsnc.globalsettings.service.FileStorageService;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.sun.istack.Nullable;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import java.io.IOException;

import static com.newgen.ntlsnc.common.CommonConstant.*;

@RestController
@RequestMapping("/api/organization")
public class OrganizationController {

    private static final String SCOPE = "Organization";

    @Autowired
    OrganizationService organizationService;
    @Autowired
    FileStorageService fileStorageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(@RequestPart(value = "organization") @Valid OrganizationDto organizationDto,
                                    @RequestPart(value = "logo", required = false) @Nullable MultipartFile files ){
        ApiResponse response = new ApiResponse(false);

        try{
            if(files != null){
                organizationDto.setFiles(files);
            }
            response.setSuccess(organizationService.create(organizationDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(@RequestPart(value = "organization") @Valid OrganizationDto organizationDto,
                                    @RequestPart(value = "logo", required = false) @Nullable MultipartFile files){
        ApiResponse response = new ApiResponse(false);

        try{
            if(files != null){
                organizationDto.setFiles(files);
            }
            response.setSuccess(organizationService.update(organizationDto.getId(), organizationDto));
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
            response.setSuccess(organizationService.delete(id));
            response.setMessage(SCOPE + DELETE_SUCCESS_MESSAGE);
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getByIdWithLogo(@PathVariable("id") Long id) {
        return organizationService.getByIdWithLogo(id);
    }

    @GetMapping
    public ResponseEntity<?> getAll(){
        ApiResponse response = new ApiResponse(false);
        try{
            response.setSuccess(organizationService.findAll());
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("companies-by-login-user-organization")
    public ResponseEntity<?> getAllCompanies(){
        ApiResponse response = new ApiResponse(false);
        try{
            response.setSuccess(organizationService.findAllCompanyByLoginUserOrganization());
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/update-active-status/{id}")
    public ResponseEntity<?> updateActiveStatus(@PathVariable String id) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(organizationService.updateActiveStatus(Long.parseLong(id)));
            response.setMessage(SCOPE + UPDATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all-company-with-logo")
    public ResponseEntity<?> getAllWithLogo(){
        ApiResponse response = new ApiResponse(false);

        try{
            response.setSuccess(organizationService.getAllWithLogo());
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/view")
    public ResponseEntity<?> viewLogo(@RequestParam("filePath") String filePath, HttpServletRequest request){
        Resource resource = null;
        String mimeType = null;
        try {
            resource = fileStorageService.downloadFile(filePath);
        } catch (ServiceException e) {
            System.err.println(e.getMessage());
        }

        try {
            mimeType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException e) {
            mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        if(mimeType == null){
            mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_DISPOSITION,"inline;fileName=" + resource.getFilename())
                .body(resource);
    }

    @GetMapping("/children/{parentId}")
    public ResponseEntity<?> getAllChildByParentId(@PathVariable Long parentId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(organizationService.getAllChildByParentId(parentId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("get-all-company-by-organization")
    public ResponseEntity<?> getAllCompanyByOrganization() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(organizationService.findAllCompanyByLoginUserOrganization());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
