package com.newgen.ntlsnc.supplychainmanagement.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.service.DepotQualityAssuranceMapService;
import com.newgen.ntlsnc.supplychainmanagement.dto.QualityInspectionDto;
import com.newgen.ntlsnc.supplychainmanagement.entity.QualityInspection;
import com.newgen.ntlsnc.supplychainmanagement.service.QualityInspectionService;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @author Newaz Sharif
 * @since 30th Oct, 22
 */

@RestController
@RequestMapping("/api/quality-inspection")
public class QualityInspectionController {

    @Autowired
    QualityInspectionService qualityInspectionService;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    DepotQualityAssuranceMapService depotQualityAssuranceMapService;

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> create(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "qualityInspectionData") QualityInspectionDto qualityInspectionDto) {
        ApiResponse response = new ApiResponse(false);

        if(file != null)
            qualityInspectionDto.setFile(file);
        QualityInspection qualityInspection = qualityInspectionService.create(qualityInspectionDto);
        if (qualityInspection == null)
            response.setError("Unable to Execute QA Process...");
        else {
            response.setSuccess(true);
            response.setMessage("Successfully Execute QA Process.");
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/info")
    public ResponseEntity<?> getQualityInspectionInfo(
            @RequestParam Long companyId,
            @RequestParam(required = false) Long depotId,
            @RequestParam(required = false) Long productCategoryId,
            @RequestParam(required = false) Long accountingYearId,
            @RequestParam(required = false) String qaStatus) {

        ApiResponse response = new ApiResponse(false);

        Map qaDepot = depotQualityAssuranceMapService.getQADepotByQAId(
                applicationUserService.getApplicationUserIdFromLoginUser());

        depotId = qaDepot.get("id") != null ? Long.parseLong(String.valueOf(
                qaDepot.get("id"))) : depotId;

        response.setSuccess(qualityInspectionService.getQualityInspectionInfo(
                    companyId,depotId,productCategoryId,accountingYearId,qaStatus));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/product-batch-available-qc-stock/{companyId}/{productId}/{batchId}/{depotId}")
    public ResponseEntity<?> getProductBatchAvailableQcStock(
            @PathVariable Long companyId,
            @PathVariable Long productId,
            @PathVariable Long batchId,
            @PathVariable Long depotId){

        ApiResponse response = new ApiResponse(false);
        response.setSuccess(qualityInspectionService.getProductBatchAvailableQcStock(
                companyId, productId, batchId, depotId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
