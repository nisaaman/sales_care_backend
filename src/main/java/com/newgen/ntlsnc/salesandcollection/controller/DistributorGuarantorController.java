package com.newgen.ntlsnc.salesandcollection.controller;


import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.common.FileDownloadService;
import com.newgen.ntlsnc.globalsettings.service.DocumentService;
import com.newgen.ntlsnc.salesandcollection.dto.DistributorGuarantorDto;
import com.newgen.ntlsnc.salesandcollection.entity.DistributorGuarantor;
import com.newgen.ntlsnc.salesandcollection.entity.Proprietor;
import com.newgen.ntlsnc.salesandcollection.service.DistributorGuarantorService;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author marziah
 * Created on 5/4/22 10:29 AM
 */
@RestController
@RequestMapping("/api/distributor-guarantor")
public class DistributorGuarantorController {

    private static final String SCOPE = "DistributorGuarantor";

    @Autowired
    DistributorGuarantorService distributorGuarantorService;
    @Autowired
    DocumentService documentService;
    @Autowired
    FileDownloadService fileDownloadService;

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> create(@ModelAttribute DistributorGuarantorDto distributorGuarantorDto){
        ApiResponse response = new ApiResponse(false);

        try{
            response.setSuccess(distributorGuarantorService.create(distributorGuarantorDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody DistributorGuarantorDto distributorGuarantorDto){
        ApiResponse response = new ApiResponse(false);

        try{
            response.setSuccess(distributorGuarantorService.update(distributorGuarantorDto.getId(), distributorGuarantorDto));
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
            response.setSuccess(distributorGuarantorService.delete(id));
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
            response.setSuccess(distributorGuarantorService.findById(id));
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll(){
        ApiResponse response = new ApiResponse(false);

        try{
            response.setSuccess(distributorGuarantorService.findAll());
        }catch (Exception ex){
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/logo/{guarantorId}")
    public ResponseEntity<?> viewLogo(@PathVariable Long guarantorId) {
        ApiResponse response = new ApiResponse(false);

        DistributorGuarantor distributorGuarantor = distributorGuarantorService.findById(guarantorId);

        String s= new String(Base64.encodeBase64(fileDownloadService.fileDownload(
                documentService.getFilePath(distributorGuarantor.getId(),GUARANTOR_LOGO_UPLOAD_DIRECTORY))));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(documentService.getFileMimeType(
                        distributorGuarantor.getId(), GUARANTOR_LOGO_UPLOAD_DIRECTORY)))
                .header(HttpHeaders.CONTENT_DISPOSITION,"inline;fileName=" + documentService
                        .getFileName(distributorGuarantor.getId(),GUARANTOR_LOGO_UPLOAD_DIRECTORY))
                .body(s);
    }

}
