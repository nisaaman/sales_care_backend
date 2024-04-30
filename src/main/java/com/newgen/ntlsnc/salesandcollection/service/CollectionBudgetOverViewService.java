package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.enums.BudgetType;
import com.newgen.ntlsnc.globalsettings.service.AccountingYearService;
import com.newgen.ntlsnc.globalsettings.service.LocationManagerMapService;
import com.newgen.ntlsnc.globalsettings.service.LocationService;
import com.newgen.ntlsnc.salesandcollection.repository.CollectionBudgetDetailsRepository;
import com.newgen.ntlsnc.salesandcollection.repository.CollectionBudgetRepository;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import com.newgen.ntlsnc.usermanagement.service.ReportingManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * @author Newaz Sharif
 * @since 27th July, 22
 */
@Service
public class CollectionBudgetOverViewService {

    @Autowired
    CollectionBudgetRepository collectionBudgetRepository;
    @Autowired
    DistributorService distributorService;
    @Autowired
    CreditLimitService creditLimitService;
    @Autowired
    LocationService locationService;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    DistributorCompanyMapService distributorCompanyMapService;
    @Autowired
    ReportingManagerService reportingManagerService;
    @Autowired
    CollectionBudgetDetailsRepository collectionBudgetDetailsRepository;
    @Autowired
    AccountingYearService accountingYearService;

    public List<Map<String,Object>> getCollectionBudgetByTargetType(
            String targetType,
            Long accountingYearId, Integer month,
            Long companyId, Long locationId,Long userLoginId) {

        List<Long> distributors;
        List<Long> salesOfficers = new ArrayList<>();

        if (locationId == null) {
            distributors = distributorService.getDistributorListFromSalesOfficer(
                    userLoginId, companyId);
            if(distributors == null)
                distributors = distributorCompanyMapService.getDistributorListFromCompany(companyId);

        }else
            distributors = distributorService.getDistributorListFromSalesOfficer(
                    locationService.getLocationManager(companyId, locationId),companyId);

        /*if(locationId == null) {
            salesOfficers = locationManagerMapService.
                    getSalesOfficerListFromUserLoginIdOrLocationId(
                            userLoginId, locationId, companyId);

            if(salesOfficers == null)
                salesOfficers = reportingManagerService.getAllSalesOfficeFromCompany(companyId);

        } else
            salesOfficers = locationManagerMapService.
                    getSalesOfficerListFromUserLoginIdOrLocationId(
                            userLoginId, locationId, companyId);*/

        salesOfficers.add(userLoginId);

        if(accountingYearId == null)
            accountingYearId  = accountingYearService.getCurrentAccountingYearId(
                    companyId, LocalDate.now());

        if(String.valueOf(BudgetType.DISTRIBUTOR).equalsIgnoreCase(targetType))
            return getDistributorWiseCollectionBudget(accountingYearId, month,
                    companyId, distributors, salesOfficers);
        else
            return getSalesOfficerWiseCollectionBudget(accountingYearId, month,
                    companyId, distributors, salesOfficers);
    }

    public List<Map<String,Object>> getDistributorWiseCollectionBudget(
            Long accountingYearId, Integer month,
            Long companyId, List<Long> distributors, List<Long> salesOfficers) {

        List<Map<String,Object>> resultList = new ArrayList<>();

        Object isItMonthlyCollectionBudget = collectionBudgetDetailsRepository
                                    .isItMonthlyCollectionBudget(companyId, accountingYearId);
        List<Map<String,Object>> distributorWiseCollectionBudget = new ArrayList<>();
        Map<String, LocalDate> accountingYear = accountingYearService.getAccountingYearDate(
                accountingYearId);

        /*if(isItMonthlyCollectionBudget != null
                && Integer.parseInt(String.valueOf(isItMonthlyCollectionBudget)) == 1) {*/
            distributorWiseCollectionBudget = collectionBudgetRepository
                    .getDistributorWiseCollectionBudgetMonthly(
                            accountingYearId, month, companyId, distributors, salesOfficers,
                            accountingYear.get("startDate"), accountingYear.get("endDate"));
        /*} else {

            distributorWiseCollectionBudget = collectionBudgetRepository
                                .getDistributorOrSalesOfficerWiseCollectionBudgetYearly(
                    accountingYearId, month, companyId, distributors, salesOfficers,
                    accountingYear.get("startDate"), accountingYear.get("endDate")
            );
        }*/

        distributorWiseCollectionBudget
                .forEach(budget -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("distributorId", budget.get("distributorId"));
                    map.put("distributorName", budget.get("distributorName"));
                    map.put("creditLimit", creditLimitService.getDistributorLimit(
                            Long.parseLong(String.valueOf(budget.get("distributorId"))), companyId));
                    map.put("salesOfficer", budget.get("salesOfficer"));
                    map.put("salesOfficerDesignation", budget.get("salesOfficerDesignation"));
                    map.put("salesOfficerLocation", budget.get("salesOfficerLocation"));
                    map.put("collectionBudget", budget.get("collectionBudget"));
                    map.put("collectionAmount", budget.get("collectionAmount"));

                    resultList.add(map);
                });

        return resultList;
    }

    public List<Map<String,Object>> getSalesOfficerWiseCollectionBudget(
            Long accountingYearId, Integer month,
            Long companyId, List<Long> distributors, List<Long> salesOfficers) {

        Object isItMonthlyCollectionBudget = collectionBudgetDetailsRepository
                .isItMonthlyCollectionBudget(companyId, accountingYearId);

        if (isItMonthlyCollectionBudget != null
                && Integer.valueOf(String.valueOf(isItMonthlyCollectionBudget)) == 1)
            return collectionBudgetRepository
                        .getSalesOfficerWiseCollectionBudgetMonthly(
                            accountingYearId, month, companyId, distributors, salesOfficers);
        else {

            Map<String, LocalDate> accountingYear = accountingYearService.getAccountingYearDate(
                    accountingYearId);
            return collectionBudgetRepository
                      .getDistributorOrSalesOfficerWiseCollectionBudgetYearly(
                              accountingYearId, month, companyId, distributors, salesOfficers,
                              accountingYear.get("startDate"), accountingYear.get("endDate"));
        }

    }
    /**get monthly collection target salesofficer*/
    public Map<String, Object> getSalesOfficerOrManagerBudgetAndCollectionAmount(
            Long salesOfficerId, Long companyId,
            Long accountingYearId, Integer month) {
        Map<String, Object> collectionData = new HashMap<>();

        LocalDate now = LocalDate.now();
        if (month == null) {
            month = now.getMonthValue();
        }
        if (salesOfficerId == null)
            salesOfficerId = applicationUserService.getApplicationUserIdFromLoginUser();

        List<Map<String, Object>> collectionDataList =
                getCollectionBudgetByTargetType(
                        BudgetType.DISTRIBUTOR.getCode(), accountingYearId, month,
                        companyId, null, salesOfficerId);

        Double collectionAmount = collectionDataList.stream().filter(
                        cb -> Objects.nonNull(cb.get("collectionAmount")))
                .mapToDouble(cb -> (Double) cb.get("collectionAmount")).sum();

        Double collectionBudget = getSalesOfficerCollectionBudgetOrTarget(
                companyId, accountingYearId, month, salesOfficerId);

        collectionData.put("collectionBudget", collectionBudget);
        collectionData.put("collectionAmount", collectionAmount);

        return collectionData;
    }

    public Double getSalesOfficerCollectionBudgetOrTarget(
            Long companyId, Long accountingYearId, Integer month,
            Long userLoginId) {

        List<Long> salesOfficers = new ArrayList<>();
        ArrayList monthList = new ArrayList<>();
        if (month != null)
            monthList.add(month);

        List<Long> distributors = distributorService.getDistributorListFromSalesOfficer(
                    userLoginId, companyId);
        if(distributors == null)
                distributors = distributorCompanyMapService.getDistributorListFromCompany(companyId);

        salesOfficers.add(userLoginId);

        if(accountingYearId == null)
            accountingYearId  = accountingYearService.getCurrentAccountingYearId(
                    companyId, LocalDate.now());

        Map<String, LocalDate> accountingYear = accountingYearService.getAccountingYearDate(
                accountingYearId);

        Double collectionTarget = collectionBudgetRepository.getCollectionTarget(
                companyId, distributors, monthList,
                accountingYear.get("startDate"), accountingYear.get("endDate"),
                LocalDate.now());

        return collectionTarget;
    }

}
