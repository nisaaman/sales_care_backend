package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.enums.DistributorWiseCollectionBudgetTemplate;
import com.newgen.ntlsnc.common.enums.Month;
import com.newgen.ntlsnc.globalsettings.service.AccountingYearService;
import com.newgen.ntlsnc.salesandcollection.repository.CollectionBudgetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Newaz Sharif
 * @since 25th Aug, 22
 */
@Service
public class CollectionBudgetDataService {

    @Autowired
    CollectionBudgetRepository collectionBudgetRepository;
    @Autowired
    AccountingYearService accountingYearService;

    public List<Map<String, Object>> getDistributorWiseCollectionBudgetData(
            Long companyId, Long accountingYearId) {
        Long previousAccYear = accountingYearService.getPreviousAccountingYear(
                accountingYearId, companyId);
//        if(previousAccYear != null) {
//            Map<String, LocalDate> accountingYear = accountingYearService
//                    .getAccountingYearDate(previousAccYear);
//            return collectionBudgetRepository.
//                    getDistributorWiseCollectionBudgetExcelDownloadData(
//                            companyId,accountingYear.get("startDate"), accountingYear.get("endDate"));
//        }else {
//            return collectionBudgetRepository.
//                    getDistributorWiseCollectionBudgetExcelDownloadData(
//                            companyId,null, null);
//        }
        return collectionBudgetRepository.
                    getDistributorWiseCollectionBudgetExcelDownloadData(
                            companyId);

    }

    public List<String> getDistributorWiseCollectionBudgetColumnNameList() {

        List<String> columnNameList = Stream.of(DistributorWiseCollectionBudgetTemplate.values())
                                            .map(DistributorWiseCollectionBudgetTemplate::getName)
                                            .collect(Collectors.toList());
        columnNameList
                .addAll(Stream.of(Month.values()).map(Month::getName).collect(Collectors.toList()));

        return columnNameList;
    }
}
