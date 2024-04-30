package com.newgen.ntlsnc.salesandcollection.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.common.FileDownloadService;
import com.newgen.ntlsnc.globalsettings.service.DocumentService;
import com.newgen.ntlsnc.salesandcollection.dto.ProprietorDto;
import com.newgen.ntlsnc.salesandcollection.entity.Distributor;
import com.newgen.ntlsnc.salesandcollection.entity.Proprietor;
import com.newgen.ntlsnc.salesandcollection.service.ProprietorService;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static com.newgen.ntlsnc.common.CommonConstant.*;

@RestController
@RequestMapping("/api/proprietor")
public class ProprietorController {

    private static final String SCOPE = "Proprietor";

    @Autowired
    ProprietorService proprietorService;
    @Autowired
    DocumentService documentService;
    @Autowired
    FileDownloadService fileDownloadService;

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> create(@ModelAttribute ProprietorDto proprietorDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(proprietorService.create(proprietorDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody ProprietorDto proprietorDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(proprietorService.update(proprietorDto.getId(), proprietorDto));
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
            response.setSuccess(proprietorService.delete(id));
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
            response.setSuccess(proprietorService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(proprietorService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/logo/{proprietorId}")
    public ResponseEntity<?> viewLogo(@PathVariable Long proprietorId) {
        ApiResponse response = new ApiResponse(false);

        Proprietor proprietor = proprietorService.findById(proprietorId);

        String s= new String(Base64.encodeBase64(fileDownloadService.fileDownload(
                documentService.getFilePath(proprietor.getId(),PROPRIETOR_LOGO_UPLOAD_DIRECTORY))));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(documentService.getFileMimeType(
                        proprietor.getId(), PROPRIETOR_LOGO_UPLOAD_DIRECTORY)))
                .header(HttpHeaders.CONTENT_DISPOSITION,"inline;fileName=" + documentService
                        .getFileName(proprietor.getId(),PROPRIETOR_LOGO_UPLOAD_DIRECTORY))
                .body(s);
    }
}
