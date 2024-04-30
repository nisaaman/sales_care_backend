package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.repository.OrganizationRepository;
import com.newgen.ntlsnc.globalsettings.service.AccountingYearService;
import com.newgen.ntlsnc.globalsettings.service.LocationManagerMapService;
import com.newgen.ntlsnc.salesandcollection.dto.SalesReturnOverViewDto;
import com.newgen.ntlsnc.supplychainmanagement.repository.SalesReturnRepository;
import com.newgen.ntlsnc.usermanagement.service.ReportingManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/**
 * @author Newaz Sharif
 * @since 13th June, 22
 */
@Service
public class SalesReturnOverViewService {

    @Autowired
    LocationManagerMapService locationManagerMapService;
    @Autowired
    SalesReturnRepository salesReturnRepository;
    @Autowired
    AccountingYearService accountingYearService;
    @Autowired
    OrganizationRepository organizationRepository;
    @Autowired
    UserLocationTreeService userLocationTreeService;
    @Autowired
    ReportingManagerService reportingManagerService;

    public SalesReturnOverViewDto getSalesReturnOverView (
            Long userLoginId, Long locationId, Long accountingYearId, Long companyId) throws Exception{

        return new SalesReturnOverViewDto(userLocationTreeService.getUserWiseLocationTree(
                                                userLoginId,companyId),
                getSalesReturnData(userLoginId, locationId, accountingYearId, companyId));

    }

    public List<Map<String, Object>> getSalesReturnData(
            Long userLoginId, Long locationId, Long accountingYearId, Long companyId) {

//        Long organizationId = null;
//        Optional<Organization> organization = organizationRepository.findByIdAndIsDeletedFalseAndIsActiveTrue
//                (companyId);
//
//        if(organization.isPresent()) {
//            organizationId = organization.get().getParent() == null ? companyId :
//                    organization.get().getParent().getId();
//        }

//        locationId = locationId == null ? locationManagerMapService
//                .getLoggedInUserLocationId(userLoginId, organizationId, companyId) : locationId;
//
//        List<Long> salesOfficerUserLoginId  = locationManagerMapService.
//                    getSalesOfficerListFromManager(userLoginId);

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

        Map<String, LocalDate> accountingYear = accountingYearService.getAccountingYearDate(
                accountingYearId);
        return salesReturnRepository.getSalesReturnOverView(salesOfficerUserLoginId,
                accountingYear.get("startDate"), accountingYear.get("endDate"), companyId);
    }

}
