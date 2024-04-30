package com.newgen.ntlsnc.salesandcollection.controller;

import com.newgen.ntlsnc.ExcelLargeFileDownloadService;
import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.common.ExcelFileDownloadService;
import com.newgen.ntlsnc.common.enums.BudgetType;
import com.newgen.ntlsnc.salesandcollection.dto.CollectionBudgetUploadDto;
import com.newgen.ntlsnc.salesandcollection.dto.SalesBudgetUploadDto;
import com.newgen.ntlsnc.salesandcollection.service.CollectionBudgetDataService;
import com.newgen.ntlsnc.salesandcollection.service.CollectionBudgetOverViewService;
import com.newgen.ntlsnc.salesandcollection.service.CollectionBudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Newaz Sharif
 * @since 28th July,22
 */


@RestController
@RequestMapping("/api/collection-budget")
public class CollectionBudgetController {

    @Autowired
    CollectionBudgetOverViewService collectionBudgetOverViewService;
    @Autowired
    CollectionBudgetService collectionBudgetService;
    @Autowired
    CollectionBudgetDataService collectionBudgetDataService;

    @GetMapping("/distributor-wise")
    public ResponseEntity<?> distributorWiseCollectionBudget(
            @RequestParam(required = false) Long accountingYearId,
            @RequestParam(required = false) Integer month,
            @RequestParam Long companyId,
            @RequestParam(required = false) Long locationId,
            @RequestParam Long userLoginId)  {

        ApiResponse response = new ApiResponse(false);

        /*response.setSuccess(collectionBudgetOverViewService.getCollectionBudgetByTargetType(
                    BudgetType.DISTRIBUTOR.getCode(), accountingYearId, month,
                    companyId, locationId, userLoginId));*/

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/sales-officer-wise")
    public ResponseEntity<?> salesOfficerWiseCollectionBudget(
            @RequestParam(required = false) Long accountingYearId,
            @RequestParam(required = false) Integer month,
            @RequestParam Long companyId,
            @RequestParam(required = false) Long locationId,
            @RequestParam Long userLoginId) {

        ApiResponse response = new ApiResponse(false);

        /*response.setSuccess(collectionBudgetOverViewService.getCollectionBudgetByTargetType(
                    BudgetType.SALES_OFFICER.getCode(), accountingYearId,
                    month, companyId, locationId, userLoginId));*/

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/so-co-budget")
    public ResponseEntity<?> getPaymentList(
            @RequestParam(name = "salesOfficerId") Long salesOfficerId,
            @RequestParam(name = "companyId") Long companyId,
            @RequestParam(name = "accountingYearId") Long accountingYearId,
            @RequestParam(name = "month", required = false) Integer month) {

        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(collectionBudgetService.
                    getSalesOfficerOrManagerCollectionTargetVSAchievment(salesOfficerId, companyId,
                    accountingYearId, month));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-payment-collection-target-vs-achieve-budget")
    public ResponseEntity<?> getPaymentCollectionTargetVsAchieveBudget(
            @RequestParam(name = "salesOfficerId",required = false) Long salesOfficerId,
            @RequestParam(name = "companyId") Long companyId,
            @RequestParam(name = "accountingYearId") Long accountingYearId,
            @RequestParam(name = "month", required = false) Integer month) {

        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(collectionBudgetService.
                    getSalesOfficerOrManagerCollectionTargetVSAchievment(salesOfficerId, companyId,
                            accountingYearId, month));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/template/download")
    public void downloadDistributorWiseCollectionBudgetTemplate(
            @RequestParam Long accountingYearId,
            @RequestParam Long companyId,
            HttpServletResponse response) {

        List<Map<String,Object>> data =
                collectionBudgetDataService.getDistributorWiseCollectionBudgetData(companyId, accountingYearId);

        ExcelLargeFileDownloadService excelExporter = new ExcelLargeFileDownloadService(data);
        List<String> columnNameList = collectionBudgetDataService
                .getDistributorWiseCollectionBudgetColumnNameList();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet; charset=UTF-8");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Collection_Budget"  + ".xlsx";
        response.setHeader(headerKey, headerValue);

        excelExporter.export(data,columnNameList, "Collection Budget",
                Arrays.asList(0,2), response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadCollectionBudget(
            @RequestPart(value = "collectionBudgetDto") @Valid CollectionBudgetUploadDto collectionBudgetUploadDto,
            @RequestPart(value = "collectionBudgetFile") MultipartFile collectionBudgetFile) {

        ApiResponse response = new ApiResponse(false);

        if(collectionBudgetFile != null){
            collectionBudgetUploadDto.setCollectionBudgetFile(collectionBudgetFile);
            String status = collectionBudgetService.distributorWiseCollectionBudgetDataUpload(
                    collectionBudgetUploadDto);
            if ("success".equalsIgnoreCase(status) == true) {
                response.setSuccess(true);
            } else {
                response.setError(status);
            }
        } else {
            response.setError("File Not Found");
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/sales-officer-collection-and-budget-monthly")
    public ResponseEntity<?> getSalesOfficerCollectionAndBudgetMonthly(
            @RequestParam Long companyId,
            @RequestParam Long accountingYearId,
            @RequestParam(required = false) Long salesOfficerId,
            @RequestParam(required = false) Integer month) {

        ApiResponse response = new ApiResponse(false);
        response.setSuccess(
                collectionBudgetOverViewService.getSalesOfficerOrManagerBudgetAndCollectionAmount(
                null, companyId, accountingYearId, month));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
