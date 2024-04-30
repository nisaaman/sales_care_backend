package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.enums.BudgetType;
import com.newgen.ntlsnc.globalsettings.entity.AccountingYear;
import com.newgen.ntlsnc.globalsettings.entity.Designation;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.repository.LocationRepository;
import com.newgen.ntlsnc.globalsettings.service.*;
import com.newgen.ntlsnc.reports.service.CommonReportsService;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import com.newgen.ntlsnc.usermanagement.service.ReportingManagerService;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


/**
 * @author Newaz Sharif
 * @since 4th Jan, 22
 */
@Service
public class SalesBudgetReportService {

    @Autowired
    DistributorService distributorService;
    @Autowired
    LocationManagerMapService locationManagerMapService;
    @Autowired
    LocationService locationService;
    @Autowired
    DistributorCompanyMapService distributorCompanyMapService;
    @Autowired
    ReportingManagerService reportingManagerService;
    @Autowired
    AccountingYearService accountingYearService;
    @Autowired
    ReportService reportService;
    @Autowired
    LocationRepository locationRepository;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    CommonReportsService commonReportsService;
    @Autowired
    ApplicationUserService applicationUserService;
    BudgetType budgetType;

    public JasperPrint getSalesBudgetReport(Long companyId, List<Long> locationIds, List<Long> salesOfficerIds,
                                            List<Long> distributorIds, Long accountingYearId, Long userLoginId,
                                            String startDate, String endDate, String dateType, HttpServletResponse response, String reportFormat) {

        locationRepository.SNC_CHILD_LOCATION_HIERARCHY(companyId);
        Map<String, Object> params = new HashMap<>();
        //date params
        LocalDate localStartDate = null;
        LocalDate localEndDate = null;
        if (!"".equals(startDate)) {
            localStartDate = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-d"));
        }
        if (!"".equals(endDate)) {
            localEndDate = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-d"));
        }
        params.put("dateHeader", commonReportsService.getReportDate(localStartDate, localEndDate, dateType));

        Integer fromMonthId = startDate != "" ?
                Integer.valueOf(String.valueOf(startDate).split("-")[1]) : null;
        Integer toMonthId = endDate != "" ?
                Integer.valueOf(String.valueOf(endDate).split("-")[1]) : null;

        String fromMonth = startDate != "" ? new DateFormatSymbols().getMonths()[Integer.valueOf(
                startDate.split("-")[1])-1] : "";
        String toMonth = endDate != "" ? new DateFormatSymbols().getMonths()[Integer.valueOf(
                String.valueOf(endDate).split("-")[1])-1] : "";
        List<Long> distributors = distributorIds;
        List<Long> salesOfficers = salesOfficerIds;
        List<Long> locations = null;
        locations = locationIds;
        List<Long> soIds = null;
        List<Long> disIds = null;
        ///
        if (locations.size() == 0) {

            if (distributors.size() == 0 && salesOfficers.size() == 0) {
                distributors = distributorCompanyMapService.getDistributorListFromCompany(companyId);

            } else if (distributors.size() == 0 && salesOfficers.size() != 0) {
                distributors = distributorService.getDistributorListOfSo(salesOfficers,companyId);
            }
        } else if (locations.size() != 0) {

            if (distributors.size() == 0 && salesOfficers.size() == 0) {
                for (Long locationId : locations) {
                    disIds = distributorService.getDistributorListFromSalesOfficer(
                            locationService.getLocationManager(companyId, locationId), companyId);
                    if (disIds != null) {
                        distributors.addAll(disIds);
                    }
                }
            } else if (distributors.size() == 0 && salesOfficers.size() != 0) {
                distributors = distributorService.getDistributorListOfSo(salesOfficers,companyId);
            }
        }
        ///
        Organization organization = organizationService.findById(companyId);
        AccountingYear accountingYear = accountingYearService.findById(accountingYearId);
        params.put("companyLogo", new ByteArrayInputStream(
                    organizationService.getOrganizationLogoByteData(companyId)));
        params.put("companyAddress", organization.getAddress());
        params.put("companyName", organization.getName() + " [" + organization.getShortName() + "] ");
        params.put("accountingYearId", accountingYearId);
        params.put("companyId", companyId);

        Map<String, Object> loginUser = applicationUserService.getMe();
        params.put("printedBy", loginUser.get("userName"));
        Object o = loginUser.get("designation");
        params.put("printedByDesignation", ((Designation) o).getName());
        params.put("productOwner", commonReportsService.getProductOwner());

        params.put("distributors", distributors.size() == 0 ? Arrays.asList(0) : distributors);
        params.put("salesOfficers", salesOfficers.size() == 0 ? Arrays.asList(0) : salesOfficers);
        params.put("locations", locations.size() == 0 ? Arrays.asList(0) : locations);

        params.put("fromMonthId", fromMonthId == null ? 1 : fromMonthId);
        params.put("toMonthId", toMonthId == null ? LocalDate.now().getMonthValue() : toMonthId);

        params.put("fromMonth", "".equals(fromMonth) ? "January" : fromMonth );
        params.put("toMonth", "".equals(toMonth) ? "December" : toMonth);

        params.put("budgetPeriod", accountingYear.getFiscalYearName());
        String jasperFileName = "";
        if ("EXCEL".equals(reportFormat)) {
            jasperFileName = "Sales_BudgetExcel";
        } else {
            jasperFileName = "Sales_Budget";

        }
        return reportService.getReport(jasperFileName, "/reports/", params);
    }

}
