package com.newgen.ntlsnc.globalsettings.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.service.DocumentService;
import com.newgen.ntlsnc.salesandcollection.service.CreditDebitNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author sagor
 * @date ১৭/৫/২২
 */

@RestController
@RequestMapping("/api/document")
public class DocumentController {
    private static final String SCOPE = "Document";

    @Autowired
    DocumentService documentService;

    @GetMapping("/get-info")
    public ResponseEntity<?> getDocumentInfo(@RequestParam Long refId, @RequestParam String refTable) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(documentService.getDocumentInfoByRefIdAndRefTable(refId, refTable));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
