package com.newgen.ntlsnc.salesandcollection.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.common.FileDownloadService;
import com.newgen.ntlsnc.globalsettings.service.DocumentService;
import com.newgen.ntlsnc.globalsettings.service.SemesterService;
import com.newgen.ntlsnc.salesandcollection.dto.DistributorDto;
import com.newgen.ntlsnc.salesandcollection.dto.DistributorUploadDto;
import com.newgen.ntlsnc.salesandcollection.entity.Distributor;
import com.newgen.ntlsnc.salesandcollection.service.DistributorService;
import com.newgen.ntlsnc.salesandcollection.service.DistributorUploadService;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import com.sun.istack.Nullable;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author sagor
 * Created on 5/4/22 10:29 AM
 */
@RestController
@RequestMapping("/api/distributor")
public class DistributorController {
    private static final String SCOPE = "Distributor";

    @Autowired
    DistributorService distributorService;
    @Autowired
    SemesterService semesterService;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    FileDownloadService fileDownloadService;
    @Autowired
    DocumentService documentService;
    @Autowired
    DistributorUploadService distributorUploadService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(@RequestPart(value = "distributorDto") @Valid DistributorDto distributorDto,
                                    @RequestPart(value = "distributorLogo", required = false) @Nullable MultipartFile distributorLogo,
                                    @RequestPart(value = "proprietorLogoList", required = false) @Nullable MultipartFile[] proprietorLogoList,
                                    @RequestPart(value = "distributorGuarantorLogoList", required = false) @Nullable MultipartFile[] distributorGuarantorLogoList) {
        ApiResponse response = new ApiResponse(false);

        try {
            if (distributorLogo != null) {
                distributorDto.setDistributorLogo(distributorLogo);
            }

            if (proprietorLogoList != null) {
                distributorDto.setProprietorLogoList(proprietorLogoList);
            }
            if (distributorGuarantorLogoList != null) {
                distributorDto.setDistributorGuarantorLogoList(distributorGuarantorLogoList);
            }

            response.setSuccess(distributorService.create(distributorDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody DistributorDto distributorDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(distributorService.update(distributorDto.getId(), distributorDto));
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
            response.setSuccess(distributorService.delete(id));
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
            response.setSuccess(distributorService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/distributorList/{userLoginId}/{locationId}/{companyId}")
    public ResponseEntity<?> getAll(@PathVariable Long userLoginId, @PathVariable Long locationId, @PathVariable Long companyId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(distributorService.findList(userLoginId, locationId, companyId));

        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all-distributor-list-by-so/{companyId}")
    public ResponseEntity<?> getAllDistributorList(@PathVariable Long companyId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(distributorService.findAllDistributorListByCompanyId(companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-distributor-credit-limit-by-bookingno/{bookingId}")
    public ResponseEntity<?> getDistributorCreditLimitByBookingNo(
            @PathVariable Long bookingId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(
                    distributorService.findDistributorCreditLimitByBookingNo(bookingId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/credit-limit/{distributorId}/{semesterId}")
    public ResponseEntity<?> getDistributorCreditLimit(
            @PathVariable(required = true) Long distributorId,
            @PathVariable(required = true) Long semesterId) {

        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(
                    distributorService.getDistributorCreditLimit(distributorId, semesterId));

        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/details/with-ledger-balance/{companyId}/")
    public ResponseEntity<?> getDistributorsDetailsWithLedgerBalance(
            @PathVariable Long companyId,
            @RequestParam String asonDateStr) {

        ApiResponse response = new ApiResponse(false);

        try {

            ApplicationUser applicationUser = applicationUserService
                    .getApplicationUserFromLoginUser();

            response.setSuccess(distributorService.
                    getSalesOfficerWiseDistributorsDetailsWithLedgerBalance(
                            applicationUser.getId(), companyId, null, LocalDate.parse(asonDateStr)));

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);

    }


    @GetMapping("/ledger-balance")
    public ResponseEntity<?> getDistributorLedgerBalance(
            @RequestParam(value = "companyId") Long companyId, @RequestParam(value = "distributorId") Long distributorId,
            @RequestParam(value = "asOnDate") String asonDateStr) {

        ApiResponse response = new ApiResponse(false);

        try {

            response.setSuccess(distributorService.getDistributorLedgerBalance(
                    distributorId, companyId, LocalDate.parse(asonDateStr)));

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    @GetMapping("/info")
    public ResponseEntity<?> getInfo(@RequestParam Long companyId, @RequestParam Long distributorId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(distributorService.getDistributorDetailsInfo(companyId, distributorId));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/details-info-with-depot-location")
    public ResponseEntity<?> getDistributorInfoWithDepotAndLocation(@RequestParam Long companyId, @RequestParam Long distributorId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(distributorService.getDistributorWithDepotAndLocation(companyId, distributorId));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/details-with-proprietor-and-guarantor/{id}")// by using distributor id, fetch proprietor and guarantor
    public ResponseEntity<?> getDistributorInfoWithProprietorAndGuarantor(@PathVariable Long id) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(distributorService.getDistributorInfoWithProprietorAndGuarantor(id));//go to distributorService where the perameter is id
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/logo/{distributorId}")
    public ResponseEntity<?> viewLogo(@PathVariable Long distributorId) {
        Distributor distributor = distributorService.findById(distributorId);
        String s = new String(Base64.encodeBase64(fileDownloadService.fileDownload(
                documentService.getFilePath(distributor.getId(), DISTRIBUTOR_LOGO_UPLOAD_DIRECTORY))));
        if (s.equals("")) {
            return ResponseEntity.ok()
                    .body(s);
        } else {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(documentService.getFileMimeType(
                            distributor.getId(), DISTRIBUTOR_LOGO_UPLOAD_DIRECTORY)))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline;fileName=" + documentService
                            .getFileName(distributor.getId(), DISTRIBUTOR_LOGO_UPLOAD_DIRECTORY))
                    .body(s);
        }
    }

    @GetMapping("/list-with-as-on-balance-and-current-credit-limit")
    public ResponseEntity<?> getDistributorListWithAsOnDateBalanceAndCurrentSemesterCreditLimit(@RequestParam Long companyId, @RequestParam(required = false) List<Long> locationIds) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(distributorService.getDistributorListWithAsOnDateBalanceAndCurrentSemesterCreditLimit(companyId, locationIds));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/ledger-balance-and-credit-limit-by-distributor-id-and-company-id")
    public ResponseEntity<?> getLedgerBalanceAndCreditLimitByDistributorIdAndCompanyId(@RequestParam Long companyId, @RequestParam Long distributorId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(distributorService.getLedgerBalanceAndCreditLimitByDistributorIdAndCompanyId(companyId, distributorId));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-sales-officer-wise-distributors-details-with-ledger-balance-with-total-balance")
    public ResponseEntity<?> getSalesOfficerWiseDistributorsDetailsWithLedgerBalanceWithTotalBalance(
            @RequestParam Long companyId,
            @RequestParam Long accountingYearId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(distributorService.
                    getSalesOfficerWiseDistributorsDetailsWithLedgerBalanceWithTotalBalance(
                            companyId, accountingYearId));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    @GetMapping("/get-distributor-without-credit-limit/{companyId}")
    public ResponseEntity<?> getDistributorWithoutCreditLimit(@PathVariable Long companyId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(distributorService.
                    getDistributorWithoutCreditLimit(companyId));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/list/{userLoginId}/{companyId}")
    public ResponseEntity<?> getAllList(@PathVariable Long userLoginId, @PathVariable Long companyId) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(distributorService.findList(userLoginId, null, companyId));

        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-distributor-list-company")
    public ResponseEntity<?> getDistributorListOfCompany(
            @RequestParam(value = "companyId", required = false) Long companyId,
            @RequestParam("searchString") String searchString
    ) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(distributorService.getDistributorListOfCompany(companyId, searchString, null));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-distributor-list-by-company-sales-officer-wise")
    public ResponseEntity<?> getDistributorListByCompanyAndSalesOfficerWise(
            @RequestParam(value = "companyId") Long companyId,
            @RequestParam(value = "salesOfficerIds") List<Long> salesOfficerIds
    ) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(distributorService.getDistributorListByCompanyAndSalesOfficerWise(companyId, salesOfficerIds));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-so-assigned-status-distributor/{distributorId}/{companyId}")
    public ResponseEntity<?> getSoAssignedStatusDistributor(
            @PathVariable(name = "distributorId") Long distributorId,
            @PathVariable(name = "companyId") Long companyId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(
                    distributorService.findSoAssignedStatusDistributor(distributorId, companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/status-update")
    public ResponseEntity<?> updateStatus(@RequestBody @Valid DistributorDto distributorDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(distributorService.updateDistributorStatus(distributorDto.getId(), distributorDto));
            response.setMessage("Distributor Status" + UPDATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(path = "/upload-distributor", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(
            @RequestPart(value = "distributorData") @Valid DistributorUploadDto distributorUploadDto,
            @RequestPart(value = "distributorFile") MultipartFile file) {
        ApiResponse response = new ApiResponse(false);
        String message = "";
        if (distributorUploadService.hasExcelFormat(file)) {
            try {
                if(file != null) {
                    distributorUploadDto.setDistributorUploadFile(file);
                    distributorUploadService.save(file, distributorUploadDto);

                    response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE + file.getOriginalFilename());
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                }
            } catch (Exception e) {
                message = "Could not upload the file: " + file.getOriginalFilename() + "!";
                response.setMessage(SCOPE + message + file.getOriginalFilename()+ "!");
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
            }
        }
        message = "Please upload an excel file!";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

}
