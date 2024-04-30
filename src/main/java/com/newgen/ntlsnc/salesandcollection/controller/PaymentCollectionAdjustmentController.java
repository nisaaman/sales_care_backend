package com.newgen.ntlsnc.salesandcollection.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.salesandcollection.dto.paymentAdjustment.AdjustPaymentCollectionDto;
import com.newgen.ntlsnc.salesandcollection.dto.PaymentCollectionAdjustmentDto;
import com.newgen.ntlsnc.salesandcollection.dto.paymentAdjustment.OrdSettlementDto;
import com.newgen.ntlsnc.salesandcollection.service.PaymentCollectionAdjustmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author kamal
 * @Date ২১/৪/২২
 */

@RestController
@RequestMapping("/api/payment-collection-adjustment")
public class PaymentCollectionAdjustmentController {
    private static final String SCOPE = "Payment Collection Adjustment";
    private static final String PAYMENT_COLLECTION = "Payment Collection";
    private static final String ORD_ADJUSTMENT = "ORD";
    @Autowired
    PaymentCollectionAdjustmentService paymentCollectionAdjustmentService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid AdjustPaymentCollectionDto adjustPaymentCollectionDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(paymentCollectionAdjustmentService.create(adjustPaymentCollectionDto));
            response.setMessage(PAYMENT_COLLECTION + CREATE_ADJUST_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/adjust-ord")
    public ResponseEntity<?> adjustOrd(@RequestBody @Valid OrdSettlementDto ordSettlementDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(paymentCollectionAdjustmentService.adjustOrd(ordSettlementDto));
            response.setMessage(ORD_ADJUSTMENT + CREATE_ADJUST_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /*@PutMapping
    public ResponseEntity<?> update(@RequestBody PaymentCollectionAdjustmentDto paymentCollectionAdjustmentDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(paymentCollectionAdjustmentService.update(paymentCollectionAdjustmentDto.getId(), paymentCollectionAdjustmentDto));
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
            response.setSuccess(paymentCollectionAdjustmentService.delete(id));
            response.setMessage(SCOPE + DELETE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }*/

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(paymentCollectionAdjustmentService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(paymentCollectionAdjustmentService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/overriding-discount-status/{paymentId}")
    public ResponseEntity<?> getOverridingDiscountStatus(@PathVariable Long paymentId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(
                    paymentCollectionAdjustmentService.getOverridingDiscountStatus(paymentId));

        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("distributor-wise-payment-collection-info-list")
    public ResponseEntity<?> getDistributorWisePaymentCollectionInfoList(@RequestParam Map<String, Object> params) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(paymentCollectionAdjustmentService.getDistributorWisePaymentCollectionInfoList(params));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("payment-collection-adjusted-history")
    public ResponseEntity<?> getDistributorWisePaymentCollectionAdjustmentHistory(@RequestParam Map<String, Object> params) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(paymentCollectionAdjustmentService.getDistributorWisePaymentCollectionAdjustmentHistory(params));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("payment-collection-adjusted-list-for-ord-settlement")
    public ResponseEntity<?> getDistributorWisePaymentCollectionAdjustmentListForOrdSettlement(@RequestParam Map<String, Object> params) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(paymentCollectionAdjustmentService.getDistributorWisePaymentCollectionAdjustmentListForOrdSettlement(params));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("payment-list-by-distributor")
    public ResponseEntity<?> getPaymentAndInvoiceListByDistributorId(
           @RequestParam(value = "distributorId") long distributorId,
           @RequestParam(value = "companyId") long companyId,
           @RequestParam(value = "fiscalYearId") String fiscalYearId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(paymentCollectionAdjustmentService
                .getPaymentListByDistributorId(distributorId, companyId, fiscalYearId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("invoice-list-by-distributor")
    public ResponseEntity<?> getUnadjustedInvoiceListByDistributorAndPaymentDate(
            @RequestParam(value = "distributorId") long distributorId,
            @RequestParam(value = "companyId") long companyId,
            @RequestParam(value = "paymentId") long paymentId,
            @RequestParam(value = "adjustType") String adjustType,
            @RequestParam(value = "fiscalYearId") String fiscalYearId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(paymentCollectionAdjustmentService.getUnadjustedInvoiceListByDistributorAndPaymentDate(
                distributorId, companyId, paymentId, adjustType, fiscalYearId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @GetMapping("invoice-list-opening-by-distributor")
    public ResponseEntity<?> getUnadjustedOpeningInvoiceListByDistributor(
            @RequestParam(value = "distributorId") long distributorId,
            @RequestParam(value = "companyId") long companyId,
            @RequestParam(value = "adjustType") String adjustType,
            @RequestParam(value = "fiscalYearId") String fiscalYearId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(paymentCollectionAdjustmentService.getUnadjustedOpeningInvoiceListByDistributor(
                    distributorId, companyId, adjustType, fiscalYearId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
