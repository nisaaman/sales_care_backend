package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.globalsettings.service.AccountingYearService;
import com.newgen.ntlsnc.globalsettings.service.LocationManagerMapService;
import com.newgen.ntlsnc.salesandcollection.dto.SalesReturnProposalDetailsViewDto;
import com.newgen.ntlsnc.salesandcollection.entity.SalesReturnProposal;
import com.newgen.ntlsnc.salesandcollection.repository.SalesReturnProposalRepository;
import com.newgen.ntlsnc.usermanagement.service.ReportingManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;

/**
 * @author Newaz Sharif
 * @since 4th July, 22
 */

@Service
public class SalesReturnProposalOverViewService {

    @Autowired
    LocationManagerMapService locationManagerMapService;
    @Autowired
    AccountingYearService accountingYearService;
    @Autowired
    SalesReturnProposalRepository salesReturnProposalRepository;
    @Autowired
    SalesReturnProposalService salesReturnProposalService;
    @Autowired
    DistributorService distributorService;
    @Autowired
    ReportingManagerService reportingManagerService;

    public List<Map<String, Object>> getSalesReturnProposalOverView(
            Long userLoginId, Long locationId, Long accountingYearId,
            Long companyId, String approvalStatus) {

        List<Long> salesOfficerUserLoginId;
        if (locationId == null) {
            salesOfficerUserLoginId = locationManagerMapService.getSalesOfficerListFromUserLoginIdOrLocationId(
                    userLoginId, locationId, companyId);

            if(salesOfficerUserLoginId == null) {
                salesOfficerUserLoginId =  reportingManagerService.getAllSalesOfficeFromCompany(companyId);
            }
        } else
            salesOfficerUserLoginId = locationManagerMapService.getSalesOfficerListFromUserLoginIdOrLocationId(
                    userLoginId, locationId, companyId);

        Map<String, LocalDate> accountingYear;
        if(accountingYearId == null) {
            Long accId  = accountingYearService.getCurrentAccountingYearId(
                    companyId, LocalDate.now());

            if(accId != null)
                accountingYear = accountingYearService.getAccountingYearDate(accId);
            else
                return new ArrayList<>();
        }else
            accountingYear = accountingYearService.getAccountingYearDate(
                    accountingYearId);

        return salesReturnProposalRepository.getSalesReturnProposalOverView(salesOfficerUserLoginId,
                    accountingYear.get("startDate"), accountingYear.get("endDate"), companyId, approvalStatus);

    }

    public SalesReturnProposalDetailsViewDto getSalesReturnProposalDetails(
            Long salesReturnProposalId) throws Exception {

        SalesReturnProposal salesReturnProposal = salesReturnProposalService.findById(
                                    salesReturnProposalId);

        List<Map<String, Object>> distributorDetailsList = distributorService.getDistributorsDetailsWithLedgerBalance(
                Arrays.asList(salesReturnProposal.getDistributor().getId()),
                salesReturnProposal.getCompany().getId(), null, LocalDate.now());

        Map<String, Object> distributorInfo = distributorDetailsList != null ? distributorDetailsList.get(0) :
                            new HashMap<>();
        return new SalesReturnProposalDetailsViewDto(
                salesReturnProposalRepository.getSalesReturnProposalLiftCycle(salesReturnProposalId,
                        salesReturnProposal.getCompany().getId()),
                salesReturnProposalRepository.getSalesReturnProposalDetails(salesReturnProposalId),
                salesReturnProposalRepository.getSalesReturnProposalSummary(salesReturnProposalId,
                        salesReturnProposal.getCompany().getId()),
                distributorInfo);
    }
}
