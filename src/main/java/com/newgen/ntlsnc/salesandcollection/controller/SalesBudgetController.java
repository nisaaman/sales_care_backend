package com.newgen.ntlsnc.salesandcollection.controller;

import com.newgen.ntlsnc.ExcelLargeFileDownloadService;
import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.common.ExcelFileDownloadService;
import com.newgen.ntlsnc.common.enums.BudgetType;
import com.newgen.ntlsnc.salesandcollection.dto.MonthWiseSalesAndCollectionBudgetDto;
import com.newgen.ntlsnc.salesandcollection.dto.SalesBudgetUploadDto;
import com.newgen.ntlsnc.salesandcollection.service.MonthWiseSalesAndCollectionBudgetService;
import com.newgen.ntlsnc.salesandcollection.service.SalesBudgetDataService;
import com.newgen.ntlsnc.salesandcollection.service.SalesBudgetOverViewService;
import com.newgen.ntlsnc.salesandcollection.service.SalesBudgetService;
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
import java.util.concurrent.TimeUnit;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author anika
 * @Date ১৩/৪/২২
 */

@RestController
@CrossOrigin(origins = {"*"},exposedHeaders = {"Content-Disposition"})
@RequestMapping("/api/sales-budget")
public class SalesBudgetController {

    private static final String SCOPE = "Sales Budget";

    @Autowired
    MonthWiseSalesAndCollectionBudgetService monthWiseSalesAndCollectionBudgetService;
    @Autowired
    SalesBudgetOverViewService salesBudgetOverViewService;
    @Autowired
    SalesBudgetDataService salesBudgetDataService;
    @Autowired
    SalesBudgetService salesBudgetService;

    /*@PostMapping
    public ResponseEntity<?> create(@RequestBody MonthWiseSalesAndCollectionBudgetDto monthWiseSalesAndCollectionBudgetDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(monthWiseSalesAndCollectionBudgetService.create(monthWiseSalesAndCollectionBudgetDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }*/

    @PutMapping
    public ResponseEntity<?> update(@RequestBody MonthWiseSalesAndCollectionBudgetDto monthWiseSalesAndCollectionBudgetDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(monthWiseSalesAndCollectionBudgetService.update(monthWiseSalesAndCollectionBudgetDto.getId(), monthWiseSalesAndCollectionBudgetDto));
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
            response.setSuccess(monthWiseSalesAndCollectionBudgetService.delete(id));
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
            response.setSuccess(monthWiseSalesAndCollectionBudgetService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(monthWiseSalesAndCollectionBudgetService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/product-wise")
    public ResponseEntity<?> productWiseSalesBudget(
            @RequestParam(required = false) Long productCategoryId,
            @RequestParam(required = false) Long accountingYearId,
            @RequestParam(required = false) Integer month,
            @RequestParam Long companyId,
            @RequestParam(required = false) Long locationId,
            @RequestParam Long userLoginId) {

        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(salesBudgetOverViewService.getSalesBudgetByTargetType(
                    "PRODUCT", productCategoryId, accountingYearId, month,
                    companyId, locationId, userLoginId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/distributor-wise")
    public ResponseEntity<?> distributorWiseSalesBudget(
            @RequestParam(required = false) Long accountingYearId,
            @RequestParam(required = false) Integer month,
            @RequestParam Long companyId,
            @RequestParam(required = false) Long locationId,
            @RequestParam Long userLoginId) {

        ApiResponse response = new ApiResponse(false);

        response.setSuccess(salesBudgetOverViewService.getSalesBudgetByTargetType(
                    BudgetType.DISTRIBUTOR.getCode(), null, accountingYearId, month,
                    companyId, locationId, userLoginId));


        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/sales-officer-wise")
    public ResponseEntity<?> salesOfficerWiseSalesBudget(
            @RequestParam(required = false) Long accountingYearId,
            @RequestParam(required = false) Integer month,
            @RequestParam Long companyId,
            @RequestParam(required = false) Long locationId,
            @RequestParam Long userLoginId) {

        ApiResponse response = new ApiResponse(false);

        response.setSuccess(salesBudgetOverViewService.getSalesBudgetByTargetType(
                    BudgetType.SALES_OFFICER.getCode(), null, accountingYearId,
                    month, companyId, locationId, userLoginId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/location-wise")
    public ResponseEntity<?> locationWiseSalesBudget(
            @RequestParam Long accountingYearId,
            @RequestParam(required = false) Integer month,
            @RequestParam Long companyId,
            @RequestParam(required = false) Long locationId,
            @RequestParam Long userLoginId) {

        ApiResponse response = new ApiResponse(false);

        response.setSuccess(salesBudgetOverViewService.getSalesBudgetByTargetType(
                    BudgetType.LOCATION.getCode(), null, accountingYearId,
                    month, companyId, locationId, userLoginId));


        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/template/download")
    public void downloadDistributorWiseSalesBudgetTemplate(
            @RequestParam Long accountingYearId,
            @RequestParam Long companyId,
            HttpServletResponse response) {
        final long startTime = System.currentTimeMillis();
        List<Map<String,Object>> data =
                salesBudgetDataService.getDistributorWiseSalesBudgetData(companyId, accountingYearId);

        ExcelLargeFileDownloadService excelExporter = new ExcelLargeFileDownloadService(data);
        List<String> columnNameList = salesBudgetDataService
                                .getDistributorWiseSalesBudgetColumnNameList();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet; charset=UTF-8");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Sales_Budget"  + ".xlsx";
        response.setHeader(headerKey, headerValue);

        excelExporter.export(data,columnNameList, "Sales Budget",
                 Arrays.asList(0,2), response);
        final long endTime = System.currentTimeMillis();
        final long exeTime = endTime - startTime;
        final long min = TimeUnit.MILLISECONDS.toMinutes(exeTime)
                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(exeTime));
        System.out.println(String.format(
                "Total time taken to execute 20000 records  %d Minutes ",
                min));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadSalesBudget(
            @RequestPart(value = "salesBudgetDto") @Valid SalesBudgetUploadDto salesBudgetUploadDto,
            @RequestPart(value = "salesBudgetFile") MultipartFile salesBudgetFile) {

        ApiResponse response = new ApiResponse(false);

        if(salesBudgetFile != null){
            salesBudgetUploadDto.setSalesBudgetFile(salesBudgetFile);
            String status = salesBudgetService.distributorWiseSalesBudgetDataUpload(
                    salesBudgetUploadDto);
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
}
