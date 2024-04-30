package com.newgen.ntlsnc.salesandcollection.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.salesandcollection.dto.SalesReturnProposalDto;
import com.newgen.ntlsnc.salesandcollection.service.SalesReturnProposalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author marziah
 * @Date 20/04/22
 */

@RestController
@RequestMapping("/api/sales-return-proposal")
public class SalesReturnProposalController {
    private static final String SCOPE = "Sales Return Proposal";

    @Autowired
    SalesReturnProposalService salesReturnProposalService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody SalesReturnProposalDto salesReturnProposalDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(salesReturnProposalService.create(salesReturnProposalDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody SalesReturnProposalDto salesReturnProposalDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(salesReturnProposalService.update(salesReturnProposalDto.getId(), salesReturnProposalDto));
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
            response.setSuccess(salesReturnProposalService.delete(id));
            response.setMessage(SCOPE + DELETE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @GetMapping("/child")
    public ResponseEntity<?> removeChild(@RequestParam Long deliveryChallanId, @RequestParam Long productId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(salesReturnProposalService.inactiveChild(deliveryChallanId, productId));
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
            response.setSuccess(salesReturnProposalService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(salesReturnProposalService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-product-wise-proposal-details-list")
    public ResponseEntity<?> getReturnProductsByDeliveryChallanId(@RequestParam Long deliveryChallanId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(salesReturnProposalService.getProductWiseSalesReturnProposalDetailsListByDeliveryChallanId(deliveryChallanId));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @GetMapping("/get-approval-status-summary")
    public ResponseEntity<?> getApprovalStatusSummary(@RequestParam Long companyId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(salesReturnProposalService.getApprovalStatusSummaryByCompanyAndLoginSalesOfficer(companyId));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //use in mobile's sales return proposal
    @GetMapping("/get-return-proposal-products")
    public ResponseEntity<?> getReturnProposalProductsById(@RequestParam Long deliveryChallanId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(salesReturnProposalService.getReturnProposalProductListByDeliveryChallanId(deliveryChallanId));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-summary")
    public ResponseEntity<?> getReturnProposalSummary(@RequestParam Long salesReturnProposalId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(salesReturnProposalService.getSummaryBySalesReturn(salesReturnProposalId));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-proposal-list-for-return")
    public ResponseEntity<?> getProposalListForReturn(@RequestParam Long companyId,
                                                      @RequestParam(required = false) Long locationId,
                                                      @RequestParam(required = false) Long accountingYearId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(salesReturnProposalService
                    .getProposalListForReturnByCompanyAndLocationAndAccountingYear(companyId, locationId, accountingYearId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-summary-and-details")
    public ResponseEntity<?> getReturnProposalSummaryAndDetails(
                 @RequestParam Long salesReturnProposalId
                ,@RequestParam(required = false) String invoiceFromDate
                ,@RequestParam(required = false) String invoiceToDate
                ,@RequestParam(required = false) Long distributorId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(
                    salesReturnProposalService.getSalesReturnProposalSummaryAndDetailsInfoById(
                            salesReturnProposalId, invoiceFromDate!=null ? LocalDate.parse(invoiceFromDate) : null,
                            invoiceToDate != null ? LocalDate.parse(invoiceToDate) : null, distributorId));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-customer-invoice-list-batch")
    public ResponseEntity<?> getCustomerInvoiceListBatch(
            @RequestParam Long batchId
            ,@RequestParam(required = false) String invoiceFromDate
            ,@RequestParam(required = false) String invoiceToDate
            ,@RequestParam(required = false) Long distributorId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(
                    salesReturnProposalService.getCustomerInvoiceListBatch(
                            batchId, LocalDate.parse(invoiceFromDate),
                            LocalDate.parse(invoiceToDate), distributorId));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @GetMapping("/get-customer-challan-list-invoice")
    public ResponseEntity<?> getCustomerChallanListInvoice(
            @RequestParam Long invoiceId
            ,@RequestParam(required = false) String invoiceFromDate
            ,@RequestParam(required = false) String invoiceToDate
            ,@RequestParam(required = false) Long distributorId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(
                    salesReturnProposalService.getCustomerChallanListInvoice(
                            invoiceId, LocalDate.parse(invoiceFromDate),
                            LocalDate.parse(invoiceToDate), distributorId));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-customer-received-batch-list-product")
    public ResponseEntity<?> getCustomerReceivedBatchListProduct(
            @RequestParam Long productId
            ,@RequestParam Long companyId
            ,@RequestParam(required = false) String invoiceFromDate
            ,@RequestParam(required = false) String invoiceToDate
            ,@RequestParam(required = false) Long distributorId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(
                    salesReturnProposalService.getCustomerReceivedBatchListProduct(
                            companyId, productId, LocalDate.parse(invoiceFromDate),
                            LocalDate.parse(invoiceToDate), distributorId));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
