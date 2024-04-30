package com.newgen.ntlsnc.salesandcollection.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.common.CommonConstant;
import com.newgen.ntlsnc.common.FileDownloadService;
import com.newgen.ntlsnc.common.FileUploadService;
import com.newgen.ntlsnc.globalsettings.entity.Document;
import com.newgen.ntlsnc.globalsettings.service.DocumentService;
import com.newgen.ntlsnc.salesandcollection.dto.SalesInvoiceDto;
import com.newgen.ntlsnc.salesandcollection.entity.PaymentCollection;
import com.newgen.ntlsnc.salesandcollection.entity.SalesInvoice;
import com.newgen.ntlsnc.salesandcollection.service.SalesInvoiceOverviewService;
import com.newgen.ntlsnc.salesandcollection.service.SalesInvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author anika
 * @Date ১৯/৪/২২
 */

@RestController
@RequestMapping("/api/sales-invoice")
public class SalesInvoiceController {

    @Autowired
    SalesInvoiceService salesInvoiceService;
    @Autowired
    SalesInvoiceOverviewService salesInvoiceOverviewService;
    @Autowired
    FileDownloadService fileDownloadService;
    @Autowired
    DocumentService documentService;
    @Autowired
    FileUploadService fileUploadService;

    private static final String SCOPE = "Sales Invoice";

    @PostMapping
    public ResponseEntity<?> create(@RequestBody SalesInvoiceDto salesInvoiceDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            SalesInvoice salesInvoice = salesInvoiceService.create(salesInvoiceDto);
            response.setSuccess(salesInvoice);
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE + " Invoice No.:" + salesInvoice.getInvoiceNo());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody SalesInvoiceDto salesInvoiceDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(salesInvoiceService.update(salesInvoiceDto.getId(), salesInvoiceDto));
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
            response.setSuccess(salesInvoiceService.delete(id));
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
            response.setSuccess(salesInvoiceService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(salesInvoiceService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all-ord-adjustedAmount-challan-of-invoice")
    public ResponseEntity<?> getAllOrdAndAdjustedAmountAndChallanOfInvoice(
            @RequestParam Long companyId,
            @RequestParam Long distributorId,
            @RequestParam Long invoiceNatureId
            ,@RequestParam(required = false) String invoiceFromDate
            ,@RequestParam(required = false) String invoiceToDate) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(
                    salesInvoiceService.getAllInvoiceOrdAndAdjustedAmountAndChallanByDistributorAndInvoiceNature(
                            companyId, distributorId, invoiceNatureId, invoiceFromDate, invoiceToDate
                    ));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/distributor-wise/over-view")
    public ResponseEntity<?> getDistributorWiseSalesInvoiceOverview(
            @RequestParam Long companyId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String asOnDateStr,
            @RequestParam Long userLoginId,
            @RequestParam(required = false) Integer dueStatusValue) {

        ApiResponse response = new ApiResponse(false);
        response.setSuccess(salesInvoiceOverviewService.getDistributorWiseSalesInvoiceOverview(
                companyId, locationId, asOnDateStr, userLoginId, dueStatusValue));

        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    @GetMapping("/distributor/invoice/details")
    public ResponseEntity<?> getDistributorWiseSalesInvoiceOverview(
            @RequestParam Long distributorId,
            @RequestParam Long companyId,
            @RequestParam(required = false) String invoiceNature,
            @RequestParam(required = false) List<String> overDueIntervals,
            @RequestParam String asOnDate,
            @RequestParam(required = false) Integer notDueStatus,
            @RequestParam(required = false) String isAcknowledged) {

        ApiResponse response = new ApiResponse(false);
        response.setSuccess(salesInvoiceOverviewService.getDistributorSalesInvoiceDetails(
                distributorId, companyId, invoiceNature, overDueIntervals,
                LocalDate.parse(asOnDate), notDueStatus, isAcknowledged));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all/{companyId}/{distributorId}")
    public ResponseEntity<?> getAllByCompanyAndDistributor(@PathVariable(name = "companyId") Long companyId,
                                                           @PathVariable(name = "distributorId") Long distributorId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(salesInvoiceService.getAllByCompanyAndDistributor(companyId, distributorId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-list-page-all-filter-list")
    public ResponseEntity<?> getInvoiceListPageAllFilterList(@RequestParam Long companyId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(salesInvoiceService.getInvoiceListPageAllFilterList(companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all-invoiceable-products/{deliveryChallanId}")
    public ResponseEntity<?> getAllInvoiceableProductsWithTotalAmountByDeliveryChallanId(@PathVariable Long deliveryChallanId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(salesInvoiceService.getAllInvoiceableProductsWithTotalAmountByDeliveryChallanId(deliveryChallanId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all-by-company-and-distributor/{companyId}/{distributorId}")
    public ResponseEntity<?> getAllByCompanyAndDistributorAndIsAcceptedFalse(@PathVariable(name = "companyId") Long companyId,
                                                           @PathVariable(name = "distributorId") Long distributorId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(salesInvoiceService.getAllByCompanyAndDistributorAndIsAcceptedFalse(companyId, distributorId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(value = "/summit-invoice-acknowledgement-document",consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> saveDocument(@ModelAttribute SalesInvoiceDto salesInvoiceDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(salesInvoiceService.saveDocument(salesInvoiceDto));
            response.setMessage(SCOPE + " Acknowledgement Document submit successfully.");
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/data/{invoiceNo}")
    public ResponseEntity<?> getSalesInvoiceByInvoiceNo(@PathVariable String invoiceNo) {

        ApiResponse response = new ApiResponse(false);
        response.setSuccess(salesInvoiceService.getSalesInvoiceByInvoiceNo(invoiceNo));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @GetMapping("/download-acknowledge-document")
    public ResponseEntity<?> downloadAcknowledgementDocument(@RequestParam Long invoiceId) {
        ApiResponse response = new ApiResponse(false);
        Document document = salesInvoiceService.getAcknowledgementDocumentInfo(invoiceId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileUploadService.getFileMimeType(document.getFileName())))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline;fileName=" + document.getFileName())
                .body(fileDownloadService.fileDownload(document.getFilePath()));
    }

    @GetMapping("/get-acknowledge-document-info")
    public ResponseEntity<?> getAcknowledgementDocumentInfo(@RequestParam Long invoiceId) {
        ApiResponse response = new ApiResponse(false);
        try {
            Document document = salesInvoiceService.getAcknowledgementDocumentInfo(invoiceId);
            response.setSuccess(document);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
