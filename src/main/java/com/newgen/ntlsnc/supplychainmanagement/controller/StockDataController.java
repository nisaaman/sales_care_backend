package com.newgen.ntlsnc.supplychainmanagement.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.common.ExcelFileDownloadService;
import com.newgen.ntlsnc.globalsettings.service.DepotService;
import com.newgen.ntlsnc.salesandcollection.dto.SalesBudgetUploadDto;
import com.newgen.ntlsnc.salesandcollection.dto.StockOpeningDataUploadDto;
import com.newgen.ntlsnc.supplychainmanagement.service.StockService;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Newaz Sharif
 * @since 25th Sept, 22
 */
@RestController
@RequestMapping("/api/stock")
public class StockDataController {

    @Autowired
    StockService stockService;
    @Autowired
    DepotService depotService;


    @GetMapping("/depot-wise-stock-data")
    public ResponseEntity<?> getDepotWiseStockInfo(
            @RequestParam(value = "companyId") Long companyId,
            @RequestParam(value = "userLoginId") Long userLoginId,
            @RequestParam(required = false, value = "depotId") Long depotId,
            @RequestParam(value = "productCategoryId", required = false) Long productCategoryId) {
        ApiResponse response = new ApiResponse(false);

        if (depotId == null) {
            Map depotMap = depotService.getDepotByLoginUserId(companyId, userLoginId);
            if (depotMap.size() != 0) {
                Long depotManagerId = Long.parseLong(String.valueOf(depotMap.get("id")));
                response.setSuccess(stockService.getDepotWiseStockDetailsInfo(companyId, depotManagerId,
                        productCategoryId));
            } else
                response.setSuccess(new ArrayList<>());
        } else {
            response.setSuccess(stockService.getDepotWiseStockDetailsInfo(companyId, depotId,
                    productCategoryId));
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/depot-stock-info")
    public ResponseEntity<?> getDepotStockInfo(
            @RequestParam(value = "companyId") Long companyId,
            @RequestParam(value = "userLoginId") Long userLoginId) {
        ApiResponse response = new ApiResponse(false);


        response.setSuccess(stockService.getDepotStockInfo(companyId, userLoginId));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/product-wise-batch-stock-info")
    public ResponseEntity<?> getProductWiseBatchStockInfo(
            @RequestParam(value = "companyId") Long companyId,
            @RequestParam(value = "productId") Long productId,
            @RequestParam(value = "storeType") String storeType) {
        ApiResponse response = new ApiResponse(false);

        response.setSuccess(stockService.getProductWiseBatchStockInfo(companyId, productId, storeType));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/product-blocked-quantity/{companyId}/{depotId}/{productId}")
    public ResponseEntity<?> getProductBlockedQuantityInStock(
            @PathVariable Long companyId,
            @PathVariable Long depotId,
            @PathVariable Long productId) {
        ApiResponse response = new ApiResponse(false);

        response.setSuccess(stockService.getProductBlockedQuantityInStock(
                companyId,depotId, productId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @GetMapping("/opening-stock-format/download")
    public void downloadOpeningStockTemplate( @RequestParam Long companyId,
                                              HttpServletResponse response) {

        List<Map<String,Object>> dataList =
                stockService.getProductDetailsListForStockOpening(companyId);

        ExcelFileDownloadService excelExporter = new ExcelFileDownloadService(dataList);
        List<String> columnNameList = dataList.size() > 0 ? new ArrayList<>(dataList.get(1).keySet()) : new ArrayList<>();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet; charset=UTF-8");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Sales_Budget"  + ".xlsx";
        response.setHeader(headerKey, headerValue);

        excelExporter.export(dataList,columnNameList, "Stock Opening Balance",
                Arrays.asList(0,2), response);
    }

    @PostMapping(path = "/opening-balance", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadStockData(
            @RequestPart(value = "stockOpeningData") @Valid StockOpeningDataUploadDto stockOpeningDataUploadDto,
            @RequestPart(value = "stockFile") MultipartFile stockFile) {
        ApiResponse response = new ApiResponse(false);
        if(stockFile != null){

            stockOpeningDataUploadDto.setStockOpeningFile(stockFile);
            String status = stockService.stockOpeningDataUpload(stockOpeningDataUploadDto);
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
