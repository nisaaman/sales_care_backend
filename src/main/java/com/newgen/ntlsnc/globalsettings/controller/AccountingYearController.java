package com.newgen.ntlsnc.globalsettings.controller;

import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.dto.AccountingYearDto;
import com.newgen.ntlsnc.globalsettings.service.AccountingYearService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author Mou
 * Created on 4/3/22 12:40 AM
 */

@RestController
@RequestMapping("/api/accounting-year")
public class AccountingYearController {
    private static final String SCOPE = "Fiscal Year";
    @Autowired
    AccountingYearService accountingYearService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody AccountingYearDto accountingYearDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(accountingYearService.create(accountingYearDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody AccountingYearDto accountingYearDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(accountingYearService.update(accountingYearDto.getId(), accountingYearDto));
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
            response.setSuccess(accountingYearService.delete(id));
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
            response.setSuccess(accountingYearService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping()
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(accountingYearService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/companyWise/{companyId}")
    public ResponseEntity<?> getAllOrganizationAndCompany(@PathVariable Long companyId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(accountingYearService.findAllByCompanyId(companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all-with-semester/{companyId}")
    public ResponseEntity<?> getAllWithSemesterList(@PathVariable Long companyId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(accountingYearService.getAllAccountingYearWithSemesterListByCompanyId(companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-with-semesters-by-id/{accountingYearId}")
    public ResponseEntity<?> getWithSemestersById(@PathVariable Long accountingYearId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(accountingYearService.getWithSemestersByAccountingYear(accountingYearId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/recent/{companyId}")
    public ResponseEntity<?> getRecentAccountingYear(@PathVariable Long companyId) {
        ApiResponse response = new ApiResponse(false);
        response.setSuccess(accountingYearService.getRecentAccountingYear(companyId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/current/{companyId}")
    public ResponseEntity<?> getCurrentAccountingYearId(@PathVariable Long companyId) {
        ApiResponse response = new ApiResponse(false);
        response.setSuccess(accountingYearService.getCurrentAccountingYearId(
                companyId, LocalDate.now()));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/currentAccountingYear/{companyId}")
    public ResponseEntity<?> getCurrentAccountingYear(@PathVariable Long companyId) {
        ApiResponse response = new ApiResponse(false);
        response.setSuccess(accountingYearService.getCurrentAccountingYear(
                companyId, LocalDate.now()));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/all/{companyId}")
    public ResponseEntity<?> getAllAccountingYear(@PathVariable Long companyId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(accountingYearService.getAllByCompanyId(companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/accounting-year-by-any-date/{companyId}/{date}")
    public ResponseEntity<?> getAccountingYearByAnyDate(@PathVariable Long companyId, @PathVariable String date) {
        ApiResponse response = new ApiResponse(false);
        response.setSuccess(accountingYearService.getCurrentAccountingYear(
                companyId, LocalDate.parse(date)));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
