package com.newgen.ntlsnc.salesandcollection.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.common.FileDownloadService;
import com.newgen.ntlsnc.common.FileUploadService;
import com.newgen.ntlsnc.globalsettings.entity.Document;
import com.newgen.ntlsnc.salesandcollection.dto.CreditDebitNoteDto;
import com.newgen.ntlsnc.salesandcollection.entity.PaymentCollection;
import com.newgen.ntlsnc.salesandcollection.service.CreditDebitNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author anika
 * @Date ৩০/৫/২২
 */
@RestController
@RequestMapping("/api/credit-debit-note")
public class CreditDebitNoteController {
    private static final String SCOPE = "Credit Debit Note";

    @Autowired
    CreditDebitNoteService creditDebitNoteService;
    @Autowired
    FileUploadService fileUploadService;
    @Autowired
    FileDownloadService fileDownloadService;


    @PostMapping
    public ResponseEntity<?> create(@RequestPart(value = "creditDebitNoteDto") CreditDebitNoteDto creditDebitNoteDto,
                                    @RequestPart(value = "creditDebitNoteFileList", required = false) MultipartFile[] creditDebitNoteFileList) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(creditDebitNoteService.createWithFile(creditDebitNoteDto, creditDebitNoteFileList));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody CreditDebitNoteDto creditDebitNoteDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(creditDebitNoteService.update(creditDebitNoteDto.getId(), creditDebitNoteDto));
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
            response.setSuccess(creditDebitNoteService.delete(id));
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
            response.setSuccess(creditDebitNoteService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(creditDebitNoteService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all-by-company-distributor-date-range")
    public ResponseEntity<?> getAllByCompanyAndDistributorAndDateRange(@RequestParam Long companyId, @RequestParam Long distributorId,
                                                                       @RequestParam String fromDate, @RequestParam String toDate) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(creditDebitNoteService.getAllByCompanyAndDistributorAndDateRange(companyId, distributorId, fromDate, toDate));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-approval-page-all-filter-list")
    public ResponseEntity<?> getDebitCreditNoteApprovalPageAllFilterList(@RequestParam Long companyId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(creditDebitNoteService.getDebitCreditNoteApprovalPageAllFilterList(companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all-with-distributor-balance")
    public ResponseEntity<?> getAllWithDistributorBalance(@RequestParam Long companyId, @RequestParam(required = false) Long locationId,
                                                          @RequestParam(required = false) String noteType, @RequestParam(required = false) String status,
                                                          @RequestParam(required = false) Long accountingYearId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(creditDebitNoteService
                    .getAllWithDistributorBalanceByCompanyAndNoteTypeAndApprovalStatusAndLocationAndAccountingYear(companyId, locationId, noteType, status, accountingYearId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-document-info-by-note-id/{id}")
    public ResponseEntity<?> getUploadedDocumentInfoByCreditDebitNoteId(@PathVariable Long id) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(creditDebitNoteService.getCreditDebitDocumentInfoByRefId(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/file-download/{creditDebitNoteId}")
    public ResponseEntity<?> getUploadedFile(@PathVariable Long creditDebitNoteId) {
        Document document = creditDebitNoteService.getCreditDebitDocumentInfoByRefId(creditDebitNoteId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileUploadService.getFileMimeType(document.getFileName())))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline;fileName=" + document.getFileName())
                .body(fileDownloadService.fileDownload(document.getFilePath()));
    }
}
