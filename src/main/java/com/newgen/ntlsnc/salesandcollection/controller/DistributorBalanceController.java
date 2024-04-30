package com.newgen.ntlsnc.salesandcollection.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.common.ExcelFileDownloadService;
import com.newgen.ntlsnc.salesandcollection.dto.DistributorOpeningBalanceDto;
import com.newgen.ntlsnc.salesandcollection.service.CollectionBudgetDataService;
import com.newgen.ntlsnc.salesandcollection.service.DistributorBalanceService;
import com.newgen.ntlsnc.salesandcollection.service.DistributorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/api/distributor-balance")
public class DistributorBalanceController  {

    final DistributorBalanceService distributorBalanceService;
    final DistributorService distributorService;

    public DistributorBalanceController(DistributorBalanceService distributorBalanceService, CollectionBudgetDataService collectionBudgetDataService, DistributorService distributorService) {
        this.distributorBalanceService = distributorBalanceService;
        this.distributorService = distributorService;
    }

    @GetMapping("/opening-balance-format/download")
    public void downloadOpeningBalanceTemplate( @RequestParam Long companyId,
                                              HttpServletResponse response) {

        List<Map<String,Object>> dataList =
                distributorService.getDistributorListByCompany(companyId);

        ExcelFileDownloadService excelExporter = new ExcelFileDownloadService(dataList);
        List<String> columnNameList = distributorBalanceService
                .getDistributorOpeneingBalanceColumnNameList();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet; charset=UTF-8");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Distributor_Opening_Balance"  + ".xlsx";
        response.setHeader(headerKey, headerValue);

        excelExporter.export(dataList,columnNameList, "Distributor Opening Balance",
                Collections.singletonList(0), response);
    }

    @PostMapping(path = "/upload-opening-balance", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> save(
            @RequestPart(value = "openingBalanceData") @Valid DistributorOpeningBalanceDto distributorOpeningBalanceDto,
            @RequestPart(value = "openingBalanceFile") MultipartFile openingBalanceFile) {
        ApiResponse response = new ApiResponse(false);
        if(openingBalanceFile != null){
            distributorOpeningBalanceDto.setOpeningBalanceFile(openingBalanceFile);
            String status = distributorBalanceService.openingBalanceUpload(distributorOpeningBalanceDto);
            if ("success".equalsIgnoreCase(status)) {
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
