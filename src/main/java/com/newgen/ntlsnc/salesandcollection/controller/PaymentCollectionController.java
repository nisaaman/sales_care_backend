package com.newgen.ntlsnc.salesandcollection.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.common.FileDownloadService;
import com.newgen.ntlsnc.globalsettings.entity.Document;
import com.newgen.ntlsnc.salesandcollection.dto.PaymentCollectionDto;
import com.newgen.ntlsnc.salesandcollection.entity.PaymentCollection;
import com.newgen.ntlsnc.salesandcollection.service.PaymentCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author kamal
 * @Date ২০/৪/২২
 */

@RestController
@RequestMapping("/api/payment-collection")
public class PaymentCollectionController {
    private static final String SCOPE = "Payment Collection";
    @Autowired
    PaymentCollectionService paymentCollectionService;
    @Autowired
    FileDownloadService fileDownloadService;

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> create(@ModelAttribute PaymentCollectionDto paymentCollectionDto) {

        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(paymentCollectionService.create(paymentCollectionDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody PaymentCollectionDto paymentCollectionDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(paymentCollectionService.update(paymentCollectionDto.getId(), paymentCollectionDto));
            response.setMessage(SCOPE + UPDATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(paymentCollectionService.approve(id));
            response.setMessage(SCOPE + APPROVE_SUCCESS_MESSAGE);
            if (response.success == true) {
                paymentCollectionService.sendPushNotificationWhenPaymentVerifyToSo(response.data);
            }
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<?> reject(@PathVariable Long id, @RequestBody Map model) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(paymentCollectionService.reject(id, model));
            response.setMessage(SCOPE + REJECT_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(paymentCollectionService.delete(id));
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
            response.setSuccess(paymentCollectionService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(paymentCollectionService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/payment-list")
    public ResponseEntity<?> getPaymentList(
            @RequestParam(name = "companyId") Long companyId,
            @RequestParam(name = "accountingYearId", required = false) Long accountingYearId,
            @RequestParam(name = "semesterId", required = false) Long semesterId,
            @RequestParam(name = "locationId", required = false) Long locationId,
            @RequestParam(name = "paymentCollectionStatus", required = false)
                    String paymentCollectionStatus
    ) {

        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(paymentCollectionService.getPaymentList( companyId,
                    accountingYearId, semesterId, locationId, paymentCollectionStatus));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/payment-adjustment-status/{id}")
    public ResponseEntity<?> getPaymentAdjustmentStatus(
            @PathVariable(name = "id") Long paymentId
    ) {

        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(paymentCollectionService.getPaymentAdjustmentStatus(paymentId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/payment-type-payment-nature")
    public ResponseEntity<?> getPaymentTypeAndPaymentNature() {

        ApiResponse response = new ApiResponse(false);

        response.setSuccess(paymentCollectionService.getPaymentTypeAndPaymentNature());
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    /**
     * Newly added to return enum map list;
     */
    @GetMapping("/payment-type-and-nature")
    public ResponseEntity<?> getPaymentTypeAndNature() {

        ApiResponse response = new ApiResponse(false);

        response.setSuccess(paymentCollectionService.getPaymentTypeAndNature());
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    @GetMapping("/document")
    public ResponseEntity<?> getPaymentCollectionDocument(@RequestParam Long paymentCollectionId) {
        ApiResponse response = new ApiResponse(false);

        PaymentCollection paymentCollection = paymentCollectionService.findById(paymentCollectionId);

        /*return ResponseEntity.status(HttpStatus.OK).body(
                new HashMap<String,Object>(){{
                    put("fileExtension", paymentCollectionService.getPaymentCollectionFileTypeExtension(
                            paymentCollection.getId()));
                    put("file",fileDownloadService.fileDownload(
                            paymentCollectionService.getPaymentCollectionFilePath(paymentCollection.getId())));
                }}
        );*/

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(paymentCollectionService.getPaymentCollectionFileMimeType(
                        paymentCollection.getId())))
                .header(HttpHeaders.CONTENT_DISPOSITION,"inline;fileName=" + paymentCollectionService
                        .getPaymentCollectionFileName(paymentCollection.getId()))
                .body(fileDownloadService.fileDownload(
                        paymentCollectionService.getPaymentCollectionFilePath(paymentCollection.getId())));


    }

    @GetMapping("getPaymentCollectionFileNameForDownload/{paymentCollectionId}")
    public ResponseEntity<?> getPaymentCollectionFileNameForDownload(@PathVariable Long paymentCollectionId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(paymentCollectionService.getPaymentCollectionFileNameForDownload(paymentCollectionId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("payment-collection-list-to-verify")
    public ResponseEntity<?> getPaymentCollectionListToVerify(@RequestParam Map<String, Object> params){
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(paymentCollectionService.getPaymentCollectionListToVerify(params));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("ledger-list-by-distributor")
    public ResponseEntity<?> getLedgerListByDistributorId(@RequestParam(value = "distributorId") long distributorId,
                                                          @RequestParam(value = "companyId") long companyId,
                                                          @RequestParam(value = "status",required = false) String status,
                                                          @RequestParam(value = "accountingYearId") Long accountingYearId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(paymentCollectionService.findLedgerListByDistributorId(distributorId,companyId,status,accountingYearId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-acknowledge-document-info")
    public ResponseEntity<?> getAcknowledgementDocumentInfo(@RequestParam Long paymentCollectionId) {
        ApiResponse response = new ApiResponse(false);
        try {
            Document document = paymentCollectionService.getAcknowledgementDocumentInfo(paymentCollectionId);
            response.setSuccess(document);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
