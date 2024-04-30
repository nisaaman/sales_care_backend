package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.globalsettings.repository.LocationManagerMapRepository;
import com.newgen.ntlsnc.globalsettings.repository.OrganizationRepository;
import com.newgen.ntlsnc.globalsettings.service.AccountingYearService;
import com.newgen.ntlsnc.globalsettings.service.DepotService;
import com.newgen.ntlsnc.globalsettings.service.LocationManagerMapService;
import com.newgen.ntlsnc.globalsettings.service.LocationService;
import com.newgen.ntlsnc.salesandcollection.dto.SalesDataDto;
import com.newgen.ntlsnc.salesandcollection.dto.SalesOrderDetailsView;
import com.newgen.ntlsnc.salesandcollection.repository.DistributorRepository;
import com.newgen.ntlsnc.salesandcollection.repository.SalesOrderRepository;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import com.newgen.ntlsnc.usermanagement.service.ReportingManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/**
 * @@author Newaz Sharif
 * @since 9th June, 22
 */

@Service
public class SalesOrderOverViewService {

    @Autowired
    LocationManagerMapService locationManagerMapService;
    @Autowired
    LocationManagerMapRepository locationManagerMapRepository;
    @Autowired
    SalesOrderRepository salesOrderRepository;
    @Autowired
    AccountingYearService accountingYearService;
    @Autowired
    OrganizationRepository organizationRepository;
    @Autowired
    DistributorRepository distributorRepository;
    @Autowired
    ReportingManagerService reportingManagerService;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    DepotService depotService;
    @Autowired
    LocationService locationService;

    public List<Map<String, Object>> getSalesOrderOverView(
            Long userLoginId, Long locationId, Long accountingYearId, Long companyId) {

        List<Long> salesOfficerUserLoginId;
        Map<Long, Object> childLocationMap = new HashMap<>();

        if(locationId == null) {
            salesOfficerUserLoginId = locationManagerMapService.getSalesOfficerListFromUserLoginIdOrLocationId(
                    userLoginId, locationId, companyId);

            if(salesOfficerUserLoginId == null) {
                salesOfficerUserLoginId =  reportingManagerService.getAllSalesOfficeFromCompany(companyId);
            }

            Boolean isDepotManager =
                    applicationUserService.checkLoginUserIsDepotManager(companyId, userLoginId);
            if (isDepotManager) {
                List<Map> areaList = depotService.findDepotAreaList(companyId, userLoginId);
                for (Map area : areaList) {
                    locationId = Long.parseLong(area.get("id").toString());
                    Map<Long, Object> childLocationMap1 =
                            locationService.getChildLocationsByParent(companyId, locationId,
                                    childLocationMap);
                    childLocationMap.putAll(childLocationMap1);
                }
                salesOfficerUserLoginId = locationService.getSoListByLocation(companyId, childLocationMap);
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

        return salesOrderRepository.getSalesOrderOverView(salesOfficerUserLoginId,
                accountingYear.get("startDate"), accountingYear.get("endDate"),companyId);
    }

    public SalesOrderDetailsView getSalesOrderDetailsView(Long salesOrderId) throws Exception {

        return new SalesOrderDetailsView(
                salesOrderRepository.getSalesOrderLifeCycle(salesOrderId),
                salesOrderRepository.getSalesOrderDetails(salesOrderId),
                salesOrderRepository.getSalesOrderSummary(salesOrderId),
                distributorRepository.getDistributorDetailsInSalesOrder(salesOrderId, LocalDate.now()));
    }
}
