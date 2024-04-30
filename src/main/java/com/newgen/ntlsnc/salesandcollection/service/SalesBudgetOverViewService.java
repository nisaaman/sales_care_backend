package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.enums.BudgetType;
import com.newgen.ntlsnc.globalsettings.service.AccountingYearService;
import com.newgen.ntlsnc.globalsettings.service.LocationManagerMapService;
import com.newgen.ntlsnc.globalsettings.service.LocationService;
import com.newgen.ntlsnc.salesandcollection.repository.SalesBudgetRepository;
import com.newgen.ntlsnc.usermanagement.service.ReportingManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Newaz Sharif
 * @since 19th July, 22
 */

@Service
public class SalesBudgetOverViewService {

    @Autowired
    SalesBudgetRepository salesBudgetRepository;
    @Autowired
    DistributorService distributorService;
    @Autowired
    LocationManagerMapService locationManagerMapService;
    @Autowired
    LocationService locationService;
    @Autowired
    CreditLimitService creditLimitService;
    @Autowired
    DistributorCompanyMapService distributorCompanyMapService;
    @Autowired
    ReportingManagerService reportingManagerService;
    @Autowired
    AccountingYearService accountingYearService;

    public List<Map<String,Object>> getSalesBudgetByTargetType(
            String targetType, Long productCategoryId,
            Long accountingYearId, Integer month,
            Long companyId, Long locationId,Long userLoginId) {

        List<Long> distributors;
        List<Long> salesOfficers;
        List<Long> locations = null;

        if (locationId == null) {
            distributors = distributorService.getDistributorListFromSalesOfficer(
                    userLoginId, companyId);
            if(distributors == null) {
                distributors = distributorCompanyMapService.getDistributorListFromCompany(companyId);
            }
        } else
            distributors = distributorService.getDistributorListFromSalesOfficer(
                    locationService.getLocationManager(companyId, locationId),companyId);

        if(locationId == null) {
            salesOfficers = locationManagerMapService.
                    getSalesOfficerListFromUserLoginIdOrLocationId(
                            userLoginId, locationId, companyId);

            if(salesOfficers == null) {
                salesOfficers = reportingManagerService.getAllSalesOfficeFromCompany(companyId);
            }
        }else
            salesOfficers = locationManagerMapService.
                    getSalesOfficerListFromUserLoginIdOrLocationId(
                            userLoginId, locationId, companyId);

        if (locationId != null)
            locations = locationService.getLocationListFromLocationHierarchy(locationId);

        if(accountingYearId == null)
            accountingYearId  = accountingYearService.getCurrentAccountingYearId(
                    companyId, LocalDate.now());

        if("PRODUCT".equalsIgnoreCase(targetType))
            return getProductWiseSalesBudget(productCategoryId,accountingYearId, month,
                    companyId, distributors, salesOfficers, locations);

        else if(String.valueOf(BudgetType.DISTRIBUTOR).equalsIgnoreCase(targetType))
            return getDistributorWiseSalesBudget(accountingYearId, month,
                    companyId, distributors, salesOfficers, locations);

         else if(String.valueOf(BudgetType.SALES_OFFICER).equalsIgnoreCase(targetType))
            return getSalesOfficerWiseSalesBudget(accountingYearId, month,
                    companyId, distributors, salesOfficers, locations);
         else
            return getLocationWiseSalesBudget(accountingYearId, month,
                    companyId, distributors, salesOfficers, locations);

    }
    public List<Map<String,Object>> getProductWiseSalesBudget(
            Long productCategoryId, Long accountingYearId, Integer month,
            Long companyId, List<Long> distributors, List<Long> salesOfficers,
            List<Long> locations) {

        return salesBudgetRepository.getProductWiseSalesBudget(productCategoryId,
               accountingYearId, month, companyId, distributors, salesOfficers,
                locations);
    }

    public List<Map<String,Object>> getDistributorWiseSalesBudget(
            Long accountingYearId, Integer month,
            Long companyId, List<Long> distributors, List<Long> salesOfficers,
            List<Long> locations) {

        List<Map<String,Object>> resultList = new ArrayList<>();
        List<Map<String,Object>> distributorWiseSalesBudget = salesBudgetRepository
                                .getDistributorOrLocationWiseBudget(
                accountingYearId, month, companyId, distributors, salesOfficers, locations);

        distributorWiseSalesBudget
                .forEach(salesBudget -> {
                    Map<String, Object> map = new HashMap<>();

                    map.put("distributorId", salesBudget.get("distributorId"));
                    map.put("distributorName", salesBudget.get("distributorName"));
                    map.put("creditLimit", creditLimitService.getDistributorLimit(
                            Long.parseLong(String.valueOf(salesBudget.get("distributorId"))), companyId));
                    map.put("salesOfficer", salesBudget.get("salesOfficer"));
                    map.put("salesOfficerDesignation", salesBudget.get("salesOfficerDesignation"));
                    map.put("salesOfficerLocation", salesBudget.get("salesOfficerLocation"));
                    map.put("salesBudget", salesBudget.get("salesBudget"));

                    resultList.add(map);
                });

        return resultList;
    }

    public List<Map<String,Object>> getSalesOfficerWiseSalesBudget(
            Long accountingYearId, Integer month,
            Long companyId, List<Long> distributors, List<Long> salesOfficers,
            List<Long> locations) {

        return salesBudgetRepository
                .getSalesOfficerOrLocationWiseBudget(
                        accountingYearId, month, companyId, distributors, salesOfficers, locations);

    }

    public List<Map<String,Object>> getLocationWiseSalesBudget(
            Long accountingYearId, Integer month,
            Long companyId, List<Long> distributors, List<Long> salesOfficers,
            List<Long> locations) {

        return salesBudgetRepository
                .getDistributorOrLocationWiseBudget(
                        accountingYearId, month, companyId, distributors,
                        salesOfficers, locations);

    }
}
