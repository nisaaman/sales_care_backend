package com.newgen.ntlsnc.globalsettings.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.service.DepotQualityAssuranceMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Newaz Sharif
 * @since 3rd Nov, 22
 */
@RestController
@RequestMapping("/api/depot-quality-assurance-map")
public class DepotQualityAssuranceMapController {

    @Autowired
    DepotQualityAssuranceMapService depotQualityAssuranceMapService;

    @GetMapping("/qa-depot-or-all/{userLoginId}")
    ResponseEntity<?> getAllQADepotOrDepotByQA(@PathVariable Long userLoginId) {
        ApiResponse response = new ApiResponse();

        response.setSuccess(depotQualityAssuranceMapService.getAllQADepotOrQADepotByQAId(
                userLoginId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/qa-depot/{userLoginId}")
    ResponseEntity<?> getQADepotByQA(@PathVariable Long userLoginId) {
        ApiResponse response = new ApiResponse();

        response.setSuccess(depotQualityAssuranceMapService.getQADepotByQAId(
                userLoginId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
