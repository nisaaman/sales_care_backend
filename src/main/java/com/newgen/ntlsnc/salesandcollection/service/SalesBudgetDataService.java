package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.enums.Month;
import com.newgen.ntlsnc.common.enums.DistributorWiseSalesBudgetTemplate;
import com.newgen.ntlsnc.globalsettings.service.AccountingYearService;
import com.newgen.ntlsnc.salesandcollection.repository.SalesBudgetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Newaz Sharif
 * @since 16th Aug,22
 */
@Service
public class SalesBudgetDataService {

    @Autowired
    SalesBudgetRepository salesBudgetRepository;
    @Autowired
    AccountingYearService accountingYearService;

    public List<Map<String, Object>> getDistributorWiseSalesBudgetData(
            Long companyId, Long accountingYearId) {

        Long previousAccYear = accountingYearService.getPreviousAccountingYear(
                accountingYearId, companyId);

        if(previousAccYear != null) {
            Map<String, LocalDate> accountingYear = accountingYearService
                    .getAccountingYearDate(previousAccYear);
            return salesBudgetRepository.getDistributorWiseSalesBudgetExcelDownloadData(
                    companyId,accountingYear.get("startDate"), accountingYear.get("endDate"));
        }else {
            return salesBudgetRepository.getDistributorWiseSalesBudgetExcelDownloadData(
                    companyId,null, null);
        }

    }

    public List<String> getDistributorWiseSalesBudgetColumnNameList() {

        List<String> columnNameList = Stream.of(DistributorWiseSalesBudgetTemplate.values()).map(DistributorWiseSalesBudgetTemplate::getName).collect(Collectors.toList());
        columnNameList
                .addAll(Stream.of(Month.values()).map(Month::getName).collect(Collectors.toList()));

        return columnNameList;
    }
}
