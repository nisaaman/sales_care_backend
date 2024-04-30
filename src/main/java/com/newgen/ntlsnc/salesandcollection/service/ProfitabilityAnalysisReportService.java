package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.globalsettings.entity.AccountingYear;
import com.newgen.ntlsnc.globalsettings.entity.Designation;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.repository.LocationRepository;
import com.newgen.ntlsnc.globalsettings.service.*;
import com.newgen.ntlsnc.reports.service.CommonReportsService;
import com.newgen.ntlsnc.salesandcollection.repository.SalesBudgetRepository;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import com.newgen.ntlsnc.usermanagement.service.ReportingManagerService;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author Newaz Sharif
 * @since 12th Jan, 23
 */

@Service
public class ProfitabilityAnalysisReportService {

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
    ApplicationUserService applicationUserService;
    @Autowired
    CommonReportsService commonReportsService;

    public JasperPrint getProfitabilityReportParams(Map<String, Object> params, HttpServletResponse response) throws ParseException {
        //Map<String, Object> params = new HashMap<String, Object>();
        Organization company = organizationService.findById(Long.parseLong(params.get("companyId").toString()));
        params.put("companyName", company.getName() + " (" + company.getShortName() + ")");
        params.put("companyAddress", company.getAddress());
        params.put("companyEmail", company.getEmail());
        params.put("companyWeb", company.getWebAddress());
        params.put("companyLogo", new ByteArrayInputStream(organizationService.getOrganizationLogoByteData(company.getId())));
        params.put("companyId", company.getId());

        Map<String, Object> loginUser = applicationUserService.getMe();
        params.put("printedBy", loginUser.get("userName"));
        Object o = loginUser.get("designation");
        params.put("printedByDesignation", ((Designation) o).getName());
        params.put("productOwner", commonReportsService.getProductOwner());

        //date params
        LocalDate localStartDate = null;
        LocalDate localEndDate = null;
        if (params.get("startDate").toString().equals("")|| params.get("startDate").toString().equals("Invalid date")) {
            params.put("startDate", null);
        } else {
            localStartDate = LocalDate.parse(params.get("startDate").toString(), DateTimeFormatter.ofPattern("yyyy-MM-d"));
            params.put("startDate", params.get("startDate").toString());
        }
        if (params.get("endDate").toString().equals("")|| params.get("endDate").toString().equals("Invalid date")) {
            params.put("endDate", null);
        } else {
            localEndDate = LocalDate.parse(params.get("endDate").toString(), DateTimeFormatter.ofPattern("yyyy-MM-d"));
            params.put("endDate", params.get("endDate").toString());
        }
        params.put("dateHeader", commonReportsService.getReportDate(localStartDate, localEndDate, params.get("dateType").toString()));

        params.put("locationIds", Arrays.asList(params.get("locationIds").toString().split(",")));
        params.put("isDistributorExist", params.get("distributorIds").toString().equals("") ? false : true);
        params.put("isSoExist", params.get("salesOfficerIds").toString().equals("") ? false : true);

        params.put("distributorIds", Arrays.asList(params.get("distributorIds").toString().split(",")));
        params.put("salesOfficerIds", Arrays.asList(params.get("salesOfficerIds").toString().split(",")));
        params.put("isNational", params.get("nationalLocationChecked").toString().equals("true") ? Boolean.TRUE : Boolean.FALSE);

        // jasper file selection
        String jasperFileName = "";
        if ("DETAILS".equals(params.get("reportType").toString())) {
            if (params.get("isWithSum").toString().equals("true")) {
                if (params.get("reportFormat").equals("PDF")) {
                    params.put("reportHeader", "Profitability Analysis(Details)");
                    jasperFileName = "Profitability_Analysis"; // details with sum report
                } else {
                    params.put("reportHeader", "Profitability Analysis(Details)");
                    jasperFileName = "Profitability_AnalysisExcel"; // details with sum report excel
                }

            } else {
                if (params.get("reportFormat").equals("PDF")) {
                    params.put("reportHeader", "Profitability Analysis(Details)");
                    jasperFileName = "Profitability_AnalysisWithOutSumPDF"; // details without sum report
                } else {
                    params.put("reportHeader", "Profitability Analysis(Details)");
                    jasperFileName = "Profitability_AnalysisWithOutSumExcel"; // details without sum report excel
                }

            }
        } else {
            if (params.get("isDistributorExist").toString().equals("true") && params.get("isSoExist").toString().equals("true")){
                params.put("reportHeader", "Profitability Analysis(Sales Officer And Distributor)");
                jasperFileName = "Profitability_AnalysisReportBySoAndDistributor"; //  Distributor So wise
            } else if (params.get("isDistributorExist").toString().equals("true")) {
                params.put("reportHeader", "Profitability Analysis(Distributor)");
                jasperFileName = "Profitability_AnalysisReportByDistributor"; // Distributor wise
            } else if (params.get("isSoExist").toString().equals("true")) {
                params.put("reportHeader", "Profitability Analysis(Sales Officer)");
                jasperFileName = "Profitability_AnalysisReportBySo"; // So wise

            }
            //type wise
            else if (params.get("nationalLocationChecked").toString().equals("true") && "Zone".equals(params.get("locationTypeData"))) {
                params.put("reportHeader", "Profitability Analysis(All Zone)");// national
                jasperFileName = "Profitability_AnalysisReportByZone"; // zone wise
            } else if (params.get("nationalLocationChecked").toString().equals("true") && "Area".equals(params.get("locationTypeData"))) {
                params.put("reportHeader", "Profitability Analysis(All Area)");
                jasperFileName = "Profitability_AnalysisReportByArea"; // Area wise
            } else if (params.get("nationalLocationChecked").toString().equals("true") && "Territory".equals(params.get("locationTypeData"))) {
                params.put("reportHeader", "Profitability Analysis(All Territory)");
                jasperFileName = "Profitability_AnalysisReportByTerritory"; // Territory wise
            }
            //
            //all wise
            else if (params.get("nationalLocationChecked").toString().equals("true") && params.get("allChecked").toString().equals("true")) {
                params.put("reportHeader", "Profitability Analysis(All Zone)");// national
                jasperFileName = "Profitability_AnalysisReportByZone"; // zone wise
            } else if (params.get("locationTypeLevel").toString().equals("1") && params.get("allChecked").toString().equals("true")) {
                params.put("reportHeader", "Profitability Analysis(All Area)");
                jasperFileName = "Profitability_AnalysisReportByArea"; // Area wise
            } else if (params.get("locationTypeLevel").toString().equals("2") && params.get("allChecked").toString().equals("true")) {
                params.put("reportHeader", "Profitability Analysis(All Territory)");
                jasperFileName = "Profitability_AnalysisReportByTerritory"; // Territory wise
            } else if (params.get("locationTypeLevel").toString().equals("3") && params.get("allChecked").toString().equals("true")) {
                params.put("reportHeader", "Profitability Analysis(All Sales Officer)");
                jasperFileName = "Profitability_AnalysisReportBySo"; // So wise
            }
            //
            else if (params.get("nationalLocationChecked").toString().equals("true")) {
                params.put("reportHeader", "Profitability Analysis(National)");// national
                jasperFileName = "Profitability_AnalysisReportByNational"; // national wise

            } else if (params.get("locationTypeLevel").toString().equals("1")) {
                params.put("reportHeader", "Profitability Analysis(Zone)");
                jasperFileName = "Profitability_AnalysisReportByZone"; // zone wise
            }
            else if (params.get("locationTypeLevel").toString().equals("2")) {
                params.put("reportHeader", "Profitability Analysis(Area)");
                jasperFileName = "Profitability_AnalysisReportByArea"; // Area wise
            }
            else if (params.get("locationTypeLevel").toString().equals("3")) {
                params.put("reportHeader", "Profitability Analysis(Territory)");
                jasperFileName = "Profitability_AnalysisReportByTerritory"; // Territory wise
            }
        }
        return reportService.getReport(jasperFileName, "/reports/profitabilityanalysis/", params);

    }

}
