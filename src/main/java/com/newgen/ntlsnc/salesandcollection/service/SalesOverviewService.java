package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.globalsettings.entity.AccountingYear;
import com.newgen.ntlsnc.globalsettings.entity.Semester;
import com.newgen.ntlsnc.globalsettings.repository.OrganizationRepository;
import com.newgen.ntlsnc.globalsettings.service.AccountingYearService;
import com.newgen.ntlsnc.globalsettings.service.LocationManagerMapService;
import com.newgen.ntlsnc.globalsettings.service.ProductTradePriceService;
import com.newgen.ntlsnc.globalsettings.service.SemesterService;
import com.newgen.ntlsnc.salesandcollection.dto.SalesDataDto;
import com.newgen.ntlsnc.salesandcollection.dto.SalesOverViewDto;
import com.newgen.ntlsnc.salesandcollection.repository.DistributorRepository;
import com.newgen.ntlsnc.salesandcollection.repository.SalesInvoiceRepository;
import com.newgen.ntlsnc.salesandcollection.repository.SalesOverviewRepository;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import com.newgen.ntlsnc.usermanagement.service.ReportingManagerService;
import com.newgen.ntlsnc.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Newaz Sharif
 * @since 30th May,22
 */

@Service
public class SalesOverviewService {
    @Autowired
    LocationManagerMapService locationManagerMapService;
    @Autowired
    SalesInvoiceRepository salesInvoiceRepository;
    @Autowired
    AccountingYearService accountingYearService;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    UserLocationTreeService userLocationTreeService;
    @Autowired
    ReportingManagerService reportingManagerService;
    @Autowired
    DistributorRepository distributorRepository;
    @Autowired
    SalesOverviewRepository salesOverviewRepository;
    @Autowired
    DistributorService distributorService;
    @Autowired
    SemesterService semesterService;
    @Autowired
    MonthWiseSalesAndCollectionBudgetService monthWiseSalesAndCollectionBudgetService;

    public SalesOverViewDto getSalesOverView(
            Long userLoginId, Long locationId, Long productCategoryId, Long accountingYearId,
            Long companyId) {

        SalesOverViewDto salesOverViewDto = new SalesOverViewDto();
        salesOverViewDto.setLocationTreeData(userLocationTreeService.getUserWiseLocationTree(
                userLoginId, companyId));
        salesOverViewDto.setSalesData(getSalesData(
                userLoginId, locationId, productCategoryId, accountingYearId, companyId));

        return salesOverViewDto;

    }

    public SalesDataDto getSalesData(
            Long userLoginId, Long locationId, Long productCategoryId, Long accountingYearId,
            Long companyId) {

//        Long organizationId = null;
//        Optional <Organization> organization = organizationRepository.findByIdAndIsDeletedFalseAndIsActiveTrue
//                (companyId);
//
//        if(organization.isPresent()) {
//            organizationId = organization.get().getParent() == null ? companyId :
//                    organization.get().getParent().getId();
//        }

//        locationId = locationId == null ? locationManagerMapService
//                        .getLoggedInUserLocationId(userLoginId, organizationId, companyId) : locationId;
        List<Long> salesOfficerUserLoginId;
        if (locationId == null) {
            salesOfficerUserLoginId = locationManagerMapService.getSalesOfficerListFromUserLoginIdOrLocationId(
                    userLoginId, locationId, companyId);

            if (salesOfficerUserLoginId == null) {
                salesOfficerUserLoginId = reportingManagerService.getAllSalesOfficeFromCompany(companyId);
            }
        } else
            salesOfficerUserLoginId = locationManagerMapService.getSalesOfficerListFromUserLoginIdOrLocationId(
                    userLoginId, locationId, companyId);

//        if(locationId != null) {
//            List<Map<String, Object>> salesOfficerList = locationManagerMapRepository.
//                    getSalesOfficerListFromSalesManager(userLoginId);
//
//            salesOfficerUserLoginId = salesOfficerList.stream()
//                                                      .filter(Objects::nonNull)
//                                                      .map( e -> Long.parseLong(String.valueOf(e.get("salesOfficer"))))
//                                                      .collect(Collectors.toList());
//        }


        Map<String, LocalDate> accountingYear;
        if (accountingYearId == null) {
            Long accId = accountingYearService.getCurrentAccountingYearId(
                    companyId, LocalDate.now());

            if (accId != null)
                accountingYear = accountingYearService.getAccountingYearDate(accId);
            else
                return new SalesDataDto();
        } else
            accountingYear = accountingYearService.getAccountingYearDate(
                    accountingYearId);

        return new SalesDataDto((salesInvoiceRepository.getSalesOverView(salesOfficerUserLoginId, productCategoryId,
                accountingYear.get("startDate"), accountingYear.get("endDate"), companyId)));
    }

    public Double getSalesBudgetMonthly(Long companyId, Long accountingYearId,
                                       Long userId, Integer month) {

        List salesOfficerIds = new ArrayList<>();
        List monthList = new ArrayList<>();
        if (month == null)
            month = LocalDate.now().getMonth().getValue();
        monthList.add(month);

        if (accountingYearId == null)
            accountingYearId = accountingYearService.getCurrentAccountingYearId(
                    companyId, LocalDate.now());
        if (userId == null)
            userId = applicationUserService.getApplicationUserIdFromLoginUser();

        salesOfficerIds = locationManagerMapService.getSalesOfficerListFromUserLoginIdOrLocationId(userId, null, companyId);

        salesOfficerIds.add(userId);
        Float budgetAmount = 0.f;
        List<Map<String, Object>> distributorList =
                distributorRepository.findList(companyId, salesOfficerIds);
        List<Long> distributorIds =
                distributorList.stream().map(s ->
                        Long.parseLong(s.get("distributorId").toString())).collect(Collectors.toList());

        Map budgetData = salesOverviewRepository.findSalesBudgetSalesOfficer(companyId,
                distributorIds, accountingYearId, monthList);

        return (Double) budgetData.get("totalBudget");
    }

    public Double getSalesDataSalesOfficerMonthly(
            Long companyId, Long salesOfficerId, Long accountingYearId, Integer month) {

        List<Long> salesOfficerIds = new ArrayList<>();
        LocalDate firstDayOfMonth = null;
        LocalDate lastDayOfMonth = null;

        if (salesOfficerId == null)
            salesOfficerId = applicationUserService.getApplicationUserIdFromLoginUser();

        salesOfficerIds = locationManagerMapService.getSalesOfficerListFromUserLoginIdOrLocationId(salesOfficerId, null, companyId);

        //salesOfficerIds.add(salesOfficerId);

        if (month == null) {
            LocalDate now = LocalDate.now();
            firstDayOfMonth = now.withDayOfMonth(1);
            YearMonth yearMonth = YearMonth.now();
            lastDayOfMonth = yearMonth.atEndOfMonth();
        }

        Double salesData = salesOverviewRepository.getSalesSummarySalesOfficer(
                salesOfficerIds, companyId, firstDayOfMonth, lastDayOfMonth);

        return salesData;
    }

    public Map<String, Object> findSalesVsTarget(
            Long companyId, Long accountingYearId, Long semesterId,
            String fromDate, String toDate) {
        Map<String, Object> salesTarget = new HashMap<String, Object>();
        ApplicationUser applicationUser = applicationUserService.getApplicationUserFromLoginUser();
        LocalDate startDate = null;
        LocalDate endDate = null;
        AccountingYear accountingYear = null;
        Double totalBudget = 0.0D;
        List salesOfficerIds = new ArrayList<>();
        if (accountingYearId != null) {
            accountingYear =
                    accountingYearService.findById(accountingYearId);

            if (accountingYear != null) {
                startDate = accountingYear.getStartDate();
                endDate = accountingYear.getEndDate();
            }
        } else if (semesterId != null) {
            Semester semester = semesterService.findById(semesterId);
            if (semester != null) {
                startDate = semester.getStartDate();
                endDate = semester.getEndDate();
                accountingYear = semester.getAccountingYear();
            }
        } else {
            if (fromDate != null) {
                startDate = LocalDate.parse(fromDate);
            }
            if (toDate != null) {
                endDate = LocalDate.parse(toDate);
            }
            if (fromDate != null && toDate != null) {
                accountingYear = accountingYearService.getAccountingYearByDateRange(
                        startDate, endDate, companyId);
            }
        }
        //salesOfficerIds.add(applicationUserService.getApplicationUserIdFromLoginUser());
        salesOfficerIds =
                locationManagerMapService.getSalesOfficerListFromUserLoginIdOrLocationId(
                        applicationUserService.getApplicationUserIdFromLoginUser(), null, companyId);

        if (startDate != null && endDate != null) {
            salesTarget.put("totalSales",
                    salesOverviewRepository.getSalesSummarySalesOfficer(
                            salesOfficerIds, companyId, startDate, endDate));
        }

        List<Map<String, Object>> distributorList =
                distributorRepository.findList(companyId, salesOfficerIds);
        List<Long> distributorIds =
                distributorList.stream().map(s ->
                        Long.parseLong(s.get("distributorId").toString())).collect(Collectors.toList());

        if (accountingYear != null) {
            List<Integer> monthList = DateUtil.getMonthListByDateRange(startDate, endDate);
            Map<String, Object>budgetData = salesOverviewRepository
                    .findSalesBudgetSalesOfficer(companyId, distributorIds, accountingYear.getId(), monthList);
            totalBudget = (Double) budgetData.get("totalBudget");
        }

        salesTarget.put("totalBudget", totalBudget);

        return salesTarget;
    }

}
