package com.newgen.ntlsnc.globalsettings.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.dto.PaymentBookDto;
import com.newgen.ntlsnc.globalsettings.service.PaymentBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author anika
 * @Date ১/৬/২২
 */

@RestController
@RequestMapping("/api/payment-book")
public class PaymentBookController {

    private static final String SCOPE = "Payment Book";

    @Autowired
    PaymentBookService paymentBookService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid PaymentBookDto paymentBookDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(paymentBookService.create(paymentBookDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody @Valid PaymentBookDto paymentBookDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(paymentBookService.update(paymentBookDto.getId(), paymentBookDto));
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
            response.setSuccess(paymentBookService.delete(id));
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
            response.setSuccess(paymentBookService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(paymentBookService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/find-payment-book-by-company-id/{companyId}")
    public ResponseEntity<?> getPaymentBookByCompanyId(@PathVariable Long companyId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(paymentBookService.findPaymentBookByCompanyId(companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/paymentBookList/{companyId}/{paymentBookLocationId}")
    public ResponseEntity<?> getAllCompanyIDAndLocationId(@PathVariable("companyId") Long companyId, @PathVariable("paymentBookLocationId") Long paymentBookLocationId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(paymentBookService.findAllByCompanyIdAndLocationId(companyId, paymentBookLocationId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/update-active-status/{id}")
    public ResponseEntity<?> updateActiveStatus(@PathVariable String id) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(paymentBookService.updateActiveStatus(Long.parseLong(id)));
            response.setMessage(SCOPE +" status"+UPDATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/find-latest-money-receipt-no/{paymentBookId}")
    public ResponseEntity<?> getLatestMoneyReceiptNo(@PathVariable Long paymentBookId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(paymentBookService.findLatestMoneyReceiptNo(paymentBookId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
