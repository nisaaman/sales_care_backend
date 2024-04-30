package com.newgen.ntlsnc.reports.service;

import com.newgen.ntlsnc.common.FileDownloadService;
import com.newgen.ntlsnc.globalsettings.entity.AccountingYear;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.service.*;
import com.newgen.ntlsnc.salesandcollection.entity.Distributor;
import com.newgen.ntlsnc.salesandcollection.repository.SalesInvoiceRepository;
import com.newgen.ntlsnc.salesandcollection.service.*;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvDeliveryChallan;
import com.newgen.ntlsnc.supplychainmanagement.entity.Picking;
import com.newgen.ntlsnc.supplychainmanagement.service.InvDeliveryChallanService;
import com.newgen.ntlsnc.supplychainmanagement.service.PickingService;
import com.newgen.ntlsnc.supplychainmanagement.service.StockService;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author nisa
 * @date 9/21/22
 * @time 12:27 PM
 */
@Service
public class CommonReportsService {
    final DistributorService distributorService;
    final OrganizationService organizationService;

    final AccountingYearService accountingYearService;

    final ReportService reportService;

    final FinanceReportsService financeReportsService;

    final InvoiceNatureService invoiceNatureService;

    final InvDeliveryChallanService invDeliveryChallanService;

    final DepotService depotService;

    final StockService stockService;

    final PickingService pickingService;
    final ProductService productService;
    final CreditLimitService creditLimitService;
    @Autowired
    SalesInvoiceOverviewService salesInvoiceOverviewService;
    @Autowired
    SalesInvoiceService salesInvoiceService;
    @Autowired
    MaterialReceivePlanService receivePlanService;
    @Autowired
    ProductCategoryService productCategoryService;
    @Autowired
    ApplicationUserService applicationUserService;

    @Autowired
    DocumentService documentService;

    @Autowired
    FileDownloadService fileDownloadService;
    @Autowired
    SalesInvoiceRepository salesInvoiceRepository;

    public CommonReportsService(DistributorService distributorService,
                                OrganizationService organizationService,
                                AccountingYearService accountingYearService,
                                ReportService reportService,
                                FinanceReportsService financeReportsService,
                                InvoiceNatureService invoiceNatureService,
                                InvDeliveryChallanService invDeliveryChallanService,
                                DepotService depotService, StockService stockService,
                                PickingService pickingService,
                                SalesInvoiceService salesInvoiceService,
                                ProductService productService,
                                CreditLimitService creditLimitService) {
        this.distributorService = distributorService;
        this.organizationService = organizationService;
        this.accountingYearService = accountingYearService;
        this.reportService = reportService;
        this.financeReportsService = financeReportsService;
        this.invoiceNatureService = invoiceNatureService;
        this.invDeliveryChallanService = invDeliveryChallanService;
        this.depotService = depotService;
        this.stockService = stockService;
        this.pickingService = pickingService;
        this.productService = productService;
        this.creditLimitService = creditLimitService;
    }

    public ResponseEntity<byte[]> getDistributorLedgerDetailsReport(
            Long distributorId, Long companyId, String startDateStr, String endDateStr) {

        try {
            Map<String, Object> parameters = new HashMap<String, Object>();
            Organization organization = organizationService.findById(companyId);
            Distributor distributor = distributorService.findById(distributorId);
            //AccountingYear accountingYear = accountingYearService.findById(accountingYearId);
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);

            parameters.put("companyName", organization.getName() + " [" + organization.getShortName() + "] ");
            parameters.put("address", organization.getAddress());
            parameters.put("email", organization.getEmail());
            parameters.put("webAddress", organization.getWebAddress());
            parameters.put("fromDate", startDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
            parameters.put("toDate", endDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
            parameters.put("distributorName", distributor.getDistributorName());
            parameters.put("distributorAddress", distributor.getBillToAddress());
            parameters.put("distributor", distributor.getBillToAddress());
            parameters.put("contactNo", distributor.getContactNo());

            JRBeanCollectionDataSource dataSource = new
                    JRBeanCollectionDataSource(distributorService.findLedgerDetails(distributorId, companyId, startDate, endDate));

            Resource resource = new ClassPathResource("reports/distributorLedger.jrxml");
            InputStream inputStream = resource.getInputStream();
            JasperDesign jasperDesign = JRXmlLoader.load(inputStream);

            JasperPrint empReport =
                    JasperFillManager.fillReport
                            (
                                    JasperCompileManager.compileReport(jasperDesign)
                                    , parameters
                                    , dataSource
                            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "distributorLedger.pdf");
            return new ResponseEntity<byte[]>
                    (JasperExportManager.exportReportToPdf(empReport), headers, HttpStatus.OK);

        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<Map<String, Object>> getDistributorLedgerBalanceViewDataSource(
            Long distributorId, LocalDate startDate, LocalDate endDate, Long companyId) {
        return distributorService.findLedgerDetails(distributorId, companyId, startDate, endDate);
    }

    public ResponseEntity<byte[]> getReceivableInvoiceStatementCashReport(
            Long companyId, Long invoiceNatureId,
            Long distributorId, String reportFormat, String startDateVal, String endDateVal, HttpServletResponse response) {

        try {
            Map<String, Object> parameters = new HashMap<String, Object>();
            Organization organization = organizationService.findById(companyId);
//            AccountingYear accountingYear = accountingYearService.findById(accountingYearId);
//            LocalDate startDate = accountingYear.getStartDate();
//            LocalDate endDate = accountingYear.getEndDate();
            LocalDate startDate = LocalDate.parse(startDateVal);
            LocalDate endDate = LocalDate.parse(endDateVal);

            parameters.put("companyName", organization.getName()
                    + " [" + organization.getShortName() + "] ");
            parameters.put("address", organization.getAddress());
            parameters.put("email", organization.getEmail());
            parameters.put("webAddress", organization.getWebAddress());
            parameters.put("fromDate", startDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
            parameters.put("toDate", startDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
            parameters.put("invoiceNature", invoiceNatureService.findById(invoiceNatureId).getName());
            parameters.put("dateHeader", getReportDate(startDate, endDate, "Date"));
            parameters.put("productOwner", getProductOwner());
            parameters.put("companyLogo", new ByteArrayInputStream(
                    organizationService.getOrganizationLogoByteData(companyId)));
            ApplicationUser applicationUser = applicationUserService.getApplicationUserFromLoginUser();
            parameters.put("user", applicationUser.getName());

            JRBeanCollectionDataSource dataSource = new
                    JRBeanCollectionDataSource(
                    financeReportsService.getReceivableInvoiceStatementReport(
                            companyId, invoiceNatureId, distributorId, startDate, endDate));

            if (!"pdf".equals(reportFormat)) {
                String fileName = "ReceivableInvoiceStatement";
                reportService.getReportsXLS(parameters, dataSource,
                        "reports/", fileName, response);
                return null;
            } else {
                String fileName = "ReceivableInvoiceStatementPdf";
                return reportService.getReportsPDF(parameters, dataSource,
                        "reports/", fileName, response);
            }

        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public ResponseEntity<byte[]> getDeliveryChallanReport(Long deliveryChallanId) {

        try {
            Map<String, Object> parameters = new HashMap<String, Object>();
            InvDeliveryChallan invDeliveryChallan = invDeliveryChallanService.findById(deliveryChallanId);

            parameters.put("invoiceNo", invDeliveryChallan.getChallanNo());
            Organization organization = invDeliveryChallan.getCompany();

            parameters.put("companyName", organization.getName() + " [" + organization.getShortName() + "] ");
            parameters.put("companyLocation", organization.getAddress());
            parameters.put("email", organization.getEmail());
            parameters.put("webAddress", organization.getWebAddress());

            parameters.put("challanNo", invDeliveryChallan.getChallanNo());
            //parameters.put("deliveryDate", invDeliveryChallan.getDeliveryDate());
            parameters.put("deliveryDate", invDeliveryChallan.getDeliveryDate().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
            parameters.put("driverName", invDeliveryChallan.getDriverName());
            parameters.put("driverMobileNo", invDeliveryChallan.getDriverContactNo());
            parameters.put("vehicleType", invDeliveryChallan.getVehicle().getVehicleType().getName().toString());
            parameters.put("vehicleNo", invDeliveryChallan.getVehicle().getRegistrationNo());

            Distributor distributor = invDeliveryChallan.getDistributor();
            parameters.put("customerName", distributor.getDistributorName());
            parameters.put("address", distributor.getShipToAddress());

            JRBeanCollectionDataSource dataSource = new
                    JRBeanCollectionDataSource(invDeliveryChallanService.getDeliveryChallanDetails(deliveryChallanId));

            Resource resource = new ClassPathResource("reports/DeliveryChallan.jrxml");
            InputStream inputStream = resource.getInputStream();
            JasperDesign jasperDesign = JRXmlLoader.load(inputStream);

            JasperPrint empReport = JasperFillManager.fillReport(JasperCompileManager.compileReport(jasperDesign), parameters, dataSource);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "DeliveryChallan.pdf");
            return new ResponseEntity<byte[]>(JasperExportManager.exportReportToPdf(empReport), headers, HttpStatus.OK);

        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<byte[]> getPickingListReport(Long pickingId, Long companyId) {

        try {
            Map<String, Object> parameters = new HashMap<String, Object>();
            Organization organization = organizationService.findById(companyId);
            Picking picking = pickingService.findById(pickingId);
            parameters.put("companyName", organization.getName() + " [" + organization.getShortName() + "] ");
            parameters.put("address", organization.getAddress());
            parameters.put("pickingNo", picking.getPickingNo());
            parameters.put("pickingDate", picking.getPickingDate().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
            JRBeanCollectionDataSource dataSource = new
                    JRBeanCollectionDataSource(pickingService.getPickingList(pickingId, companyId));

            Resource resource = new ClassPathResource("reports/pickingList.jrxml");
            InputStream inputStream = resource.getInputStream();
            JasperDesign jasperDesign = JRXmlLoader.load(inputStream);

            JasperPrint jasperPrint =
                    JasperFillManager.fillReport
                            (
                                    JasperCompileManager.compileReport(jasperDesign)
                                    , parameters
                                    , dataSource
                            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "pickingList.pdf");
            return new ResponseEntity<byte[]>
                    (JasperExportManager.exportReportToPdf(jasperPrint), headers, HttpStatus.OK);

        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<byte[]> getRestritedListReport(
            Long companyId, List<Long> categoryIds, List<Long> productIds, List<Long> depotIds,
            LocalDate fromDate, LocalDate endDate, String dateType,
            String reportFormat, String reportType,
            Boolean isWithSum, HttpServletResponse response) {

        try {
            /*List<Long> categories = productCategoryService.getProductCategoryListFromCategoryHierarchy(
                    productCategoryId);*/
            Map<String, Object> parameters = new HashMap<String, Object>();
            Organization organization = organizationService.findById(companyId);
            ApplicationUser applicationUser = applicationUserService.getApplicationUserFromLoginUser();
            parameters.put("companyLogo", new ByteArrayInputStream(
                    organizationService.getOrganizationLogoByteData(companyId)));
            parameters.put("companyName", organization.getName() + " [" + organization.getShortName() + "] ");
            parameters.put("fromDate", fromDate == null ? "" : fromDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            parameters.put("endDate", endDate == null ? "" : endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            parameters.put("companyAddress", organization.getAddress());
            parameters.put("email", organization.getEmail());
            parameters.put("webAddress", organization.getWebAddress());
            parameters.put("dateHeader", getReportDate(fromDate, endDate, dateType));
            parameters.put("printedBy", applicationUser.getName());
            parameters.put("productOwner", getProductOwner());
            parameters.put("reportHeader", "Restricted Products Details");

            JRBeanCollectionDataSource dataSource = new
                    JRBeanCollectionDataSource(productService.getRestrictedList(companyId,
                    categoryIds, productIds, depotIds, fromDate, endDate));

            if (!"PDF".equals(reportType)) {
                String fileName = "restrictedReportXlsx";
                reportService.getReportsXLS(parameters, dataSource, "reports/restrictedreport/",
                        fileName, response);
                return null;
            } else {
                String fileName = "restrictedReport";
                return reportService.getReportsPDF(parameters, dataSource, "reports/restrictedreport/",
                        fileName, null);
            }


        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<byte[]> getInvoiceTypeWiseSummaryReport(
            Long companyId, Long salesOfficerId, String reportFormat,
            String fromDateStr, String endDateStr,
            Long distributorId, List<Long> locationIds,
            HttpServletResponse response) {
        try {
            Map<String, Object> parameters = new HashMap<String, Object>();
            Organization organization = organizationService.findById(companyId);

            LocalDate fromDate = null;
            LocalDate endDate = null;
            if (fromDateStr != null && fromDateStr != "") {
                fromDate = LocalDate.parse(fromDateStr);
            }

            if (endDateStr != null && endDateStr != "") {
                endDate = LocalDate.parse(endDateStr);
            }
            parameters.put("companyName", organization.getName() + " [" + organization.getShortName() + "] ");
            parameters.put("address", organization.getAddress());
            parameters.put("email", organization.getEmail());
            parameters.put("webAddress", organization.getWebAddress());
            parameters.put("dateHeader", getReportDate(fromDate, endDate, ""));
            parameters.put("reportTitle", "Invoice Type Wise Summary Report");
            JRBeanCollectionDataSource dataSource = new
                    JRBeanCollectionDataSource(financeReportsService.getInvoiceTypeWiseSummaryReport(companyId,
                    salesOfficerId, fromDate, endDate, distributorId, locationIds));


            if (!"PDF".equals(reportFormat)) {
                String fileName = "invoiceTypeWiseSummaryReport";
                reportService.getReportsXLS(parameters, dataSource, "reports/", fileName, response);
                return null;
            } else {
                String fileName = "invoiceTypeWiseSummaryReportPdf";
                return reportService.getReportsPDF(parameters, dataSource, "reports/", fileName, response);
            }

            //return new ResponseEntity<byte[]>(HttpStatus.OK);

        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<byte[]> getSalesAndCollectionData(
            Long companyId, Boolean nationalLocationChecked,
            Long locationTypeLevel, String locationTypeData, List<Long> locationIds,
            List<Long> salesOfficerIds, List<Long> distributorIds,
            Long accountingYearId, String startDateStr, String endDateStr,
            String dateType, String reportFormat, String reportType,
            Boolean isWithSum, Boolean allChecked,
            String allFilterType, HttpServletResponse response) {
        try {
            Map<String, Object> parameters = new HashMap<String, Object>();
            Organization organization = organizationService.findById(companyId);
            LocalDate startDate = null;
            LocalDate endDate = null;
            if (startDateStr != null && startDateStr != "") {
                startDate = LocalDate.parse(startDateStr);
            }
            if (endDateStr != null && endDateStr != "") {
                endDate = LocalDate.parse(endDateStr);
            }
            parameters.put("companyName", organization.getName() + " [" + organization.getShortName() + "] ");
            parameters.put("address", organization.getAddress());
            parameters.put("email", organization.getEmail());
            parameters.put("webAddress", organization.getWebAddress());
            parameters.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            ApplicationUser applicationUser = applicationUserService.getApplicationUserFromLoginUser();
            parameters.put("user", applicationUser.getName());
            parameters.put("dateHeader", getReportDate(startDate, endDate, dateType));
            parameters.put("productOwner", getProductOwner());
            parameters.put("companyLogo", new ByteArrayInputStream(
                    organizationService.getOrganizationLogoByteData(companyId)));

            Map<String, Object> reportMap = salesInvoiceService.getSalesAndCollectionData(
                    companyId, locationIds, salesOfficerIds, distributorIds,
                    accountingYearId, startDate, endDate, reportType, dateType, parameters);

            parameters = (Map<String, Object>) reportMap.get("parameters");

            List<Map<String, Object>> salesOverViewList =
                    (List<Map<String, Object>>) reportMap.get("salesOverViewList");

            JRBeanCollectionDataSource dataSource = new
                    JRBeanCollectionDataSource(salesOverViewList);

            Map reportData = getSalesAndCollectionFileName(reportType,
                    reportFormat, isWithSum, allChecked, nationalLocationChecked,
                    locationTypeData, locationTypeLevel,
                    locationIds, salesOfficerIds, distributorIds, allFilterType);
            String fileName = reportData.get("fileName").toString();
            String reportHeader = reportData.get("reportHeader").toString();
            parameters.put("reportHeader", reportHeader);

            if (!"PDF".equals(reportFormat) && "" != reportFormat) {
                reportService.getReportsXLS(parameters,
                        dataSource, "reports/salesandcollection/", fileName, response);
                return null;
            } else {
                return
                        reportService.getReportsPDF(parameters,
                                dataSource, "reports/salesandcollection/", fileName, response);
            }

        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Map<String, String> getSalesAndCollectionFileName(
            String reportType, String reportFormat,
            Boolean isWithSum, Boolean allChecked, Boolean nationalLocationChecked,
            String locationTypeData, Long locationTypeLevel, List<Long> locationIds,
            List<Long> salesOfficerIds, List<Long> distributorIds,
            String allFilterType) {
        Map<String, String> reportData = new HashMap<>();
        String fileName = "";
        String reportHeader = "";
        if ("DETAILS".equals(reportType)) {
            if (!"PDF".equals(reportFormat) && "" != reportFormat) {
                fileName = "salesAndCollectionXls";
                reportHeader = "Sales And Collection Details Report";
            } else {
                if (isWithSum) {
                    fileName = "salesAndCollectionSalesOfficer";
                    reportHeader = "Sales And Collection Details Report";
                } else {
                    fileName = "salesAndCollectionWithoutSubtotal";
                    reportHeader = "Sales And Collection Details Report";
                }
            }
        } else {
            if (nationalLocationChecked.toString().equals("true")) {
                if (!"".equals(locationTypeData)) {
                    if ("Zone".equals(locationTypeData)) {
                        fileName = "salesAndCollectionSummaryZone";
                        reportHeader = "Sales And Collection Summary (All Zone)";
                    } else if ("Area".equals(locationTypeData)) {
                        fileName = "salesAndCollectionSummaryArea";
                        reportHeader = "Sales And Collection Summary (All Area)";
                    } else if ("Territory".equals(locationTypeData)) {
                        fileName = "salesAndCollectionSummaryTerritory";
                        reportHeader = "Sales And Collection Summary (All Territory)";
                    }
                }  else {
                    fileName = "salesAndCollectionSummaryNational";
                    reportHeader = "Sales And Collection Summary (National)";
                }
            }

            if (locationTypeLevel > 0 &&
                    salesOfficerIds.size() == 0 && distributorIds.size() == 0) {
                if (1 == locationTypeLevel) {
                    if (allChecked) {
                        if ("SO".equals(allFilterType)) {
                            fileName = "salesAndCollectionSummarySo";
                            reportHeader = "Sales And Collection Summary (All Sales Officer)";
                        }
                        else if ("DIS".equals(allFilterType)) {
                            fileName = "salesAndCollectionSummarySoAndDistributor";
                            reportHeader = "Sales And Collection Summary (All Distributor)";
                        }
                        else if ("TERRITORY".equals(allFilterType)) {
                            fileName = "salesAndCollectionSummaryTerritory";
                            reportHeader = "Sales And Collection Summary (All Territory)";
                        }
                        else {
                            fileName = "salesAndCollectionSummaryArea";
                            reportHeader = "Sales And Collection Summary (All Area)";
                        }
                    }
                    else {
                        fileName = "salesAndCollectionSummaryZone";
                        reportHeader = "Sales And Collection Summary (Zone)";
                    }
                } else if (2 == locationTypeLevel) {
                    if (allChecked) {
                        if ("SO".equals(allFilterType)) {
                            fileName = "salesAndCollectionSummarySo";
                            reportHeader = "Sales And Collection Summary (All Sales Officer)";
                        }
                        else if ("DIS".equals(allFilterType)) {
                            fileName = "salesAndCollectionSummarySoAndDistributor";
                            reportHeader = "Sales And Collection Summary (All Distributor)";
                        }
                        else if ("TERRITORY".equals(allFilterType)) {
                            fileName = "salesAndCollectionSummaryTerritory";
                            reportHeader = "Sales And Collection Summary (All Territory)";
                        }
                        else {
                            fileName = "salesAndCollectionSummaryTerritory";
                            reportHeader = "Sales And Collection Summary (All Territory)";
                        }
                    } else {
                        fileName = "salesAndCollectionSummaryArea";
                        reportHeader = "Sales And Collection Summary (Area)";
                    }
                } else if (3 == locationTypeLevel) {
                    if (allChecked) {
                        if ("SO".equals(allFilterType)) {
                            fileName = "salesAndCollectionSummarySo";
                            reportHeader = "Sales And Collection Summary (All Sales Officer)";
                        }
                        else if ("DIS".equals(allFilterType)) {
                            fileName = "salesAndCollectionSummarySoAndDistributor";
                            reportHeader = "Sales And Collection Summary (All Distributor)";
                        }
                        else if ("TERRITORY".equals(allFilterType)) {
                            fileName = "salesAndCollectionSummaryTerritory";
                            reportHeader = "Sales And Collection Summary (All Territory)";
                        }
                        else {
                            fileName = "salesAndCollectionSummarySo";
                            reportHeader = "Sales And Collection Summary (All Sales Officer)";
                        }
                    } else {
                        fileName = "salesAndCollectionSummaryTerritory";
                        reportHeader = "Sales And Collection Summary (Territory)";
                    }
                }
            } else if (salesOfficerIds.size() > 0 && distributorIds.size() > 0) {
                fileName = "salesAndCollectionSummarySoAndDistributor";
                reportHeader = "Sales And Collection Summary (So and Distributor)";
            } else if (salesOfficerIds.size() > 0) {
                if (allChecked) {
                    fileName = "salesAndCollectionSummarySoAndDistributor";
                    reportHeader = "Sales And Collection Summary (All Distributor)";
                } else {
                    fileName = "salesAndCollectionSummarySo";
                    reportHeader = "Sales And Collection Summary (Sales Officer)";
                }
            } else if (distributorIds.size() > 0) {
                fileName = "salesAndCollectionSummaryDistributor";
                reportHeader = "Sales And Collection Summary (Distributor)";
            }
        }

        reportData.put("fileName", fileName);
        reportData.put("reportHeader", reportHeader);
        return reportData;
    }

    public ResponseEntity<byte[]> getMaterialPlannerTicket(
            Boolean nationalLocationChecked,
            Long companyId,
            List<Long> locationIds, List<Long> categoryIds, List<Long> depotIds,
            String startDateStr, String endDateStr,
            String reportType, String dateType,
            Boolean isWithSum,
            String reportFormat,
            Long locationTypeLevel,
            String locationTypeData,
            Long categoryTypeLevel,
            HttpServletResponse response) {
        try {
            Map<String, Object> parameters = new HashMap<String, Object>();
            Organization organization = organizationService.findById(companyId);
            LocalDate startDate = null;
            LocalDate endDate = null;
            if (startDateStr != null && startDateStr != "") {
                startDate = LocalDate.parse(startDateStr);
            }
            if (endDateStr != null && endDateStr != "") {
                endDate = LocalDate.parse(endDateStr);
            }
            parameters.put("companyName", organization.getName() + " [" + organization.getShortName() + "] ");
            parameters.put("companyAddress", organization.getAddress());
            parameters.put("email", organization.getEmail());
            parameters.put("webAddress", organization.getWebAddress());
            parameters.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            ApplicationUser applicationUser = applicationUserService.getApplicationUserFromLoginUser();
            parameters.put("user", applicationUser.getName());
            parameters.put("dateHeader", getReportDate(startDate, endDate, dateType));
            parameters.put("productOwner", getProductOwner());
            parameters.put("companyLogo", new ByteArrayInputStream(
                    organizationService.getOrganizationLogoByteData(companyId)));

            JRBeanCollectionDataSource dataSource = new
                    JRBeanCollectionDataSource(receivePlanService.getMaterialPlannerTicket(companyId, locationIds,
                    categoryIds, depotIds, startDate, endDate));

            String fileName = "materialPlannerReport";
            String reportHeader = "Material Planner Report";

            if ("DETAILS".equals(reportType)) {
                fileName = "materialPlannerReport";
            }
            else {
                if ("true".equals(nationalLocationChecked.toString())
                        && (categoryTypeLevel == 0)) {
                    if (!"".equals(locationTypeData)) {
                        if ("Zone".equals(locationTypeData)) {
                            fileName = "materialPlannerReportZone";
                            reportHeader = "Material Planner Report (All Zone)";
                        } else if ("Area".equals(locationTypeData)) {
                            fileName = "materialPlannerReportArea";
                            reportHeader = "Material Planner Report (All Area)";
                        } else if ("Territory".equals(locationTypeData)) {
                            fileName = "materialPlannerReportTerritory";
                            reportHeader = "Material Planner Report (All Territory)";
                        }
                    }  else {
                        fileName = "materialPlannerReportNational";
                        reportHeader = "Material Planner Report (National)";
                    }
                }
                else {
                    fileName = "materialPlannerReport";
                }
            }
            parameters.put("fileName", fileName);
            parameters.put("reportHeader", reportHeader);

            if (!"PDF".equals(reportFormat)) {
                reportService.getReportsXLS(parameters, dataSource, "reports/materialplanner/", fileName, response);
                return null;
            } else {
                return
                        reportService.getReportsPDF(parameters, dataSource, "reports/materialplanner/", fileName, response);
            }

        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<byte[]> getCreditLimitHistory(
            Long companyId, Boolean nationalLocationChecked, Long locationTypeLevel, List<Long> locationIds,
            List<Long> salesOfficerIds, List<Long> distributorIds, String fromDateStr, String endDateStr,
            String reportType, Boolean isWithSum, String reportFormat, String dateType, Boolean allChecked,
            String locationTypeData, HttpServletResponse response) {

        try {
            Map<String, Object> parameters = new HashMap<String, Object>();
            Organization organization = organizationService.findById(companyId);
            LocalDate startDate = null;
            LocalDate endDate = null;
            if (fromDateStr != null && fromDateStr != "") {
                startDate = LocalDate.parse(fromDateStr);
            }

            if (endDateStr != null && endDateStr != "") {
                endDate = LocalDate.parse(endDateStr);
            }
            parameters.put("companyName", organization.getName()
                    + " [" + organization.getShortName() + "] ");
            parameters.put("companyLogo", new ByteArrayInputStream(
                    organizationService.getOrganizationLogoByteData(companyId)));
            ApplicationUser applicationUser = applicationUserService.getApplicationUserFromLoginUser();
            parameters.put("user", applicationUser.getName());
            parameters.put("companyAddress", organization.getAddress());
            parameters.put("email", organization.getEmail());
            parameters.put("webAddress", organization.getWebAddress());
            parameters.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
            parameters.put("dateHeader", getReportDate(startDate, endDate, dateType));
            parameters.put("productOwner", getProductOwner());
            String reportHeader = "Credit Limit History Report";
            JRBeanCollectionDataSource dataSource = new
                    JRBeanCollectionDataSource(
                    creditLimitService.getCreditLimitHistoryList(companyId,
                            locationIds, salesOfficerIds, distributorIds, startDate, endDate));

            String fileName = "creditLimitHistoryReport";
            parameters.put("reportHeader", reportHeader);

            if (!"PDF".equals(reportFormat)) {
                reportService.getReportsXLS(parameters, dataSource, "reports/", fileName, response);
                return null;
            } else {
                return
                        reportService.getReportsPDF(parameters, dataSource, "reports/", fileName, response);
            }

        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public ResponseEntity<byte[]> depotToDepotMovementHistoryReport(
            Long companyId, List<Long> categoryIds, List<Long> depotIds,
            List<Long> productIds, String fromDateStr, String endDateStr,
            String reportFormat, String reportType,
            Boolean isWithSum, String dateType, HttpServletResponse response) {
        try {
            Map<String, Object> parameters = new HashMap<String, Object>();
            Organization organization = organizationService.findById(companyId);
            LocalDate startDate = null;
            LocalDate endDate = null;
            if (fromDateStr != null && fromDateStr != "") {
                startDate = LocalDate.parse(fromDateStr);
            }

            if (endDateStr != null && endDateStr != "") {
                endDate = LocalDate.parse(endDateStr);
            }
            /*LocalDate startDate =
                    LocalDate.parse(String.format(startDateStr,
                            DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            LocalDate endDate =
                    LocalDate.parse(String.format(endDateStr,
                            DateTimeFormatter.ofPattern("dd/MM/yyyy")));*/
            parameters.put("companyName", organization.getName()
                    + " [" + organization.getShortName() + "] ");
            ApplicationUser applicationUser = applicationUserService.getApplicationUserFromLoginUser();
            parameters.put("user", applicationUser.getName());
            parameters.put("companyName", organization.getName() + " [" + organization.getShortName() + "] ");
            parameters.put("address", organization.getAddress());
            parameters.put("email", organization.getEmail());
            parameters.put("webAddress", organization.getWebAddress());
            parameters.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
            parameters.put("dateHeader", getReportDate(startDate, endDate, dateType));
            parameters.put("productOwner", getProductOwner());
            //parameters.put("fromDate", startDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
            //parameters.put("toDate", startDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));

            List<Map<String, Object>> movementList =
                    depotService.depotToDepotMovementHistoryReport(
                            companyId, categoryIds, depotIds, productIds,
                            startDate, endDate);
            JRBeanCollectionDataSource dataSource = new
                    JRBeanCollectionDataSource(movementList);

            String fileName = "";
            String reportHeader = "Depot To Depot Movement Report";
            parameters.put("reportHeader", reportHeader);

            if (!"PDF".equals(reportFormat)) {
                fileName = "DepotToDepotMovementReportXls";
                reportService.getReportsXLS(parameters, dataSource,
                        "reports/depottodepot/", fileName, response);
                return null;
            } else {
                fileName = "DepotToDepotMovementReport";
                return reportService.getReportsPDF(parameters,
                        dataSource, "reports/depottodepot/", fileName, response);
            }

        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<byte[]> getInvoiceAgingReport(
            Long companyId, Boolean nationalLocationChecked,
            Long locationTypeLevel, List<Long> locationIds,
            List<Long> categoryIds, Long categoryTypeLevel, List<Long> productIds,
            List<Long> salesOfficerIds, List<Long> distributorIds,
            String fromDateStr, String endDateStr,
            String reportType, Boolean isWithSum, String reportFormat,
            String dateType, Boolean allChecked, String locationTypeData,
            String allFilterType, HttpServletResponse response) {

        try {
            Map<String, Object> parameters = new HashMap<String, Object>();
            Organization organization = organizationService.findById(companyId);
            LocalDate startDate = null;
            LocalDate endDate = null;
            if (fromDateStr != null && fromDateStr != "") {
                startDate = LocalDate.parse(fromDateStr);
            }

            if (endDateStr != null && endDateStr != "") {
                endDate = LocalDate.parse(endDateStr);
            }

            parameters.put("companyName", organization.getName()
                    + " [" + organization.getShortName() + "] ");
            parameters.put("companyLogo", new ByteArrayInputStream(
                    organizationService.getOrganizationLogoByteData(companyId)));
            parameters.put("address", organization.getAddress());
            parameters.put("email", organization.getEmail());
            parameters.put("webAddress", organization.getWebAddress());
            parameters.put("dateHeader", getReportDate(startDate, endDate, dateType));
            parameters.put("productOwner", getProductOwner());

            ApplicationUser applicationUser = applicationUserService.getApplicationUserFromLoginUser();
            parameters.put("user", applicationUser.getName());

            JRBeanCollectionDataSource dataSource = new
                    JRBeanCollectionDataSource(
                    financeReportsService.getInvoiceAgingData(
                            companyId, locationIds,
                            salesOfficerIds, distributorIds, startDate, endDate, reportType));

            Map reportData = getInvoiceAgeingReportFile(reportType,
                    reportFormat, isWithSum, allChecked, nationalLocationChecked,
                    locationTypeData, locationTypeLevel, categoryTypeLevel,
                    locationIds, salesOfficerIds, distributorIds, allFilterType);
            String fileName = reportData.get("fileName").toString();
            String reportHeader = reportData.get("reportHeader").toString();
            parameters.put("reportHeader", reportHeader);

            if (!"PDF".equals(reportFormat) && "" != reportFormat) {
                reportService.getReportsXLS(parameters,
                        dataSource, "reports/invoiceageing/", fileName, response);
                return null;
            } else {
                return
                        reportService.getReportsPDF(parameters,
                                dataSource, "reports/invoiceageing/", fileName, response);
            }
        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<byte[]> getReceivableOverdueReport(
            Long companyId, Boolean nationalLocationChecked,
            Long locationTypeLevel, List<Long> locationIds,
            List<Long> categoryIds, Long categoryTypeLevel, List<Long> productIds,
            List<Long> salesOfficerIds, List<Long> distributorIds,
            String fromDateStr, String endDateStr,
            String reportType, Boolean isWithSum, String reportFormat,
            String dateType, Boolean allChecked, String locationTypeData,
            String allFilterType, HttpServletResponse response) {

        try {
            Map<String, Object> parameters = new HashMap<String, Object>();
            Organization organization = organizationService.findById(companyId);
            LocalDate startDate = null;
            LocalDate endDate = null;
            if (fromDateStr != null && fromDateStr != "") {
                startDate = LocalDate.parse(fromDateStr);
            }

            if (endDateStr != null && endDateStr != "") {
                endDate = LocalDate.parse(endDateStr);
            }

            parameters.put("companyName", organization.getName()
                    + " [" + organization.getShortName() + "] ");
            parameters.put("companyLogo", new ByteArrayInputStream(
                    organizationService.getOrganizationLogoByteData(companyId)));
            parameters.put("address", organization.getAddress());
            parameters.put("email", organization.getEmail());
            parameters.put("webAddress", organization.getWebAddress());
            parameters.put("dateHeader", getReportDate(startDate, endDate, dateType));
            parameters.put("productOwner", getProductOwner());

            ApplicationUser applicationUser = applicationUserService.getApplicationUserFromLoginUser();
            parameters.put("user", applicationUser.getName());

            JRBeanCollectionDataSource dataSource = new
                    JRBeanCollectionDataSource(
                    getReceivableOverdueData(
                            companyId, locationIds, salesOfficerIds, distributorIds, startDate, endDate, reportType));

            String fileName = "";
            String reportHeader = "";
            if (!"PDF".equals(reportFormat) && "" != reportFormat) {
                if ("DETAILS".equals(reportType)) {
                    fileName = "receivableOverdueReport";
                    reportHeader = "Receivable Overdue Report";
                } else {
                    if (nationalLocationChecked.toString().equals("true")) {
                        fileName = "receivableOverdueSummaryReport";
                        reportHeader = "Receivable Overdue Summary Report";
                    }
                }
                parameters.put("reportHeader", reportHeader);
                reportService.getReportsXLS(parameters,
                        dataSource, "reports/receivableoverdue/", fileName, response);
                return null;
            } else {
                if ("DETAILS".equals(reportType)) {
                    if (isWithSum) {
                        fileName = "receivableOverdueReportPdf";
                        reportHeader = "Receivable Overdue Report (Details)";
                    } else {
                        fileName = "receivableOverdueReportWithoutSumPdf";
                        reportHeader = "Receivable Overdue Report (Without Sum)";
                    }
                } else {
                    if (nationalLocationChecked.toString().equals("true")
                            && (salesOfficerIds == null || salesOfficerIds.size() == 0)
                            && (distributorIds == null || distributorIds.size() == 0)) {
                        if (!"".equals(locationTypeData)) {
                            if ("Zone".equals(locationTypeData)) {
                                fileName = "receivableOverdueSummaryReportZonePdf";
                                reportHeader = "Receivable Overdue Report (All Zone)";
                            } else if ("Area".equals(locationTypeData)) {
                                fileName = "receivableOverdueSummaryReportAreaPdf";
                                reportHeader = "Receivable Overdue Report (All Area)";
                            } else if ("Territory".equals(locationTypeData)) {
                                fileName = "receivableOverdueSummaryReportTerritoryPdf";
                                reportHeader = "Receivable Overdue Report (All Territory)";
                            }
                        } else {
                            fileName = "receivableOverdueSummaryReportNationalPdf";
                            reportHeader = "Receivable Overdue Report (National)";
                        }

                    }
                    if (locationTypeLevel > 0
                            && (salesOfficerIds == null || salesOfficerIds.size() == 0)
                            && (distributorIds == null || distributorIds.size() == 0)) {
                        if (1 == locationTypeLevel) {
                            if (allChecked) {
                                if ("SO".equals(allFilterType)) {
                                    fileName = "receivableOverdueSummaryReportSoPdf";
                                    reportHeader = "Receivable Overdue Report (All Sales Officer)";
                                }
                                else if ("DIS".equals(allFilterType)) {
                                    fileName = "receivableOverdueSummaryReportDistributorPdf";
                                    reportHeader = "Receivable Overdue Report (All Distributor)";
                                }
                                else if ("TERRITORY".equals(allFilterType)) {
                                    fileName = "receivableOverdueSummaryReportTerritoryPdf";
                                    reportHeader = "Receivable Overdue Report (All Territory)";
                                } else {
                                    fileName = "receivableOverdueSummaryReportAreaPdf";
                                    reportHeader = "Receivable Overdue Report (All Area)";
                                }
                            } else {
                                fileName = "receivableOverdueSummaryReportZonePdf";
                                reportHeader = "Receivable Overdue Report (Zone)";
                            }
                        } else if (2 == locationTypeLevel) {
                            if (allChecked) {
                                if ("SO".equals(allFilterType)) {
                                    fileName = "receivableOverdueSummaryReportSoPdf";
                                    reportHeader = "Receivable Overdue Report (All Sales Officer)";
                                }
                                else if ("DIS".equals(allFilterType)) {
                                    fileName = "receivableOverdueSummaryReportDistributorPdf";
                                    reportHeader = "Receivable Overdue Report (All Distributor)";
                                }
                                else if ("TERRITORY".equals(allFilterType)) {
                                    fileName = "receivableOverdueSummaryReportTerritoryPdf";
                                    reportHeader = "Receivable Overdue Report (All Territory)";
                                }
                                else {
                                    fileName = "receivableOverdueSummaryReportTerritoryPdf";
                                    reportHeader = "Receivable Overdue Report (All Territory)";
                                }
                            } else {
                                fileName = "receivableOverdueSummaryReportAreaPdf";
                                reportHeader = "Receivable Overdue Report (Area)";
                            }
                        } else if (3 == locationTypeLevel) {
                            if (allChecked) {
                                if ("SO".equals(allFilterType)) {
                                    fileName = "receivableOverdueSummaryReportSoPdf";
                                    reportHeader = "Receivable Overdue Report (All Sales Officer)";
                                }
                                else if ("DIS".equals(allFilterType)) {
                                    fileName = "receivableOverdueSummaryReportDistributorPdf";
                                    reportHeader = "Receivable Overdue Report (All Distributor)";
                                }
                                else if ("TERRITORY".equals(allFilterType)) {
                                    fileName = "receivableOverdueSummaryReportTerritoryPdf";
                                    reportHeader = "Receivable Overdue Report (Territory)";
                                } else {
                                    fileName = "receivableOverdueSummaryReportSoPdf";
                                    reportHeader = "Receivable Overdue Report (All Sales Officer)";
                                }
                            } else {
                                fileName = "receivableOverdueSummaryReportTerritoryPdf";
                                reportHeader = "Receivable Overdue Report (Territory)";
                            }
                        }
                    } else if (salesOfficerIds.size() > 0 && distributorIds.size() > 0) {
                        fileName = "receivableOverdueSummaryReportSoDistributorPdf";
                        reportHeader = "Receivable Overdue Report (So and Distributor)";
                    } else if (salesOfficerIds.size() > 0) {
                        if (allChecked) {
                            fileName = "receivableOverdueSummaryReportSoDistributorPdf";
                            reportHeader = "Receivable Overdue Report (All Distributor)";
                        } else {
                            fileName = "receivableOverdueSummaryReportSoPdf";
                            reportHeader = "Receivable Overdue Report (Sales Officer)";
                        }
                    } else if (distributorIds.size() > 0) {
                        fileName = "receivableOverdueSummaryReportDistributorPdf";
                        reportHeader = "Receivable Overdue Report (Distributor)";
                    }
                }

                parameters.put("reportHeader", reportHeader);
                return reportService.getReportsPDF(parameters,
                        dataSource, "reports/receivableoverdue/", fileName, response);
            }

        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public ResponseEntity<byte[]> getAcknowledgedInvoiceReportList(
            Long companyId, Long distributorId,
            String startDateStr, String endDateStr, String dateType) {
        try {
            Map<String, Object> parameters = new HashMap<String, Object>();
            Organization organization = organizationService.findById(companyId);
            Distributor distributor = distributorService.findById(distributorId);
            ApplicationUser applicationUser = applicationUserService.getApplicationUserFromLoginUser();
            LocalDate startDate = null;
            if (startDateStr != null && startDateStr != "") {
                startDate = LocalDate.parse(startDateStr);
                //parameters.put("startDate", startDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
            }
            LocalDate endDate = null;
            if (endDateStr != null && endDateStr != "") {
                endDate = LocalDate.parse(endDateStr);
                //parameters.put("endDate", endDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
            }

            parameters.put("dateHeader", getReportDate(startDate, endDate, dateType));
            parameters.put("user", applicationUser.getName());
            parameters.put("companyName", organization.getName() + " [" + organization.getShortName() + "] ");
            parameters.put("companyLogo", new ByteArrayInputStream(
                    organizationService.getOrganizationLogoByteData(companyId)));
            parameters.put("address", organization.getAddress());
            parameters.put("email", organization.getEmail());
            parameters.put("webAddress", organization.getWebAddress());
            parameters.put("distributorName", distributor.getDistributorName());
            parameters.put("distributorAddress", distributor.getBillToAddress());
            parameters.put("distributor", distributor.getBillToAddress());
            parameters.put("contactNo", distributor.getContactNo());
            parameters.put("productOwner", getProductOwner());

            List<Long> salesInvoiceList =
                    salesInvoiceService.getInvoiceListByDistributorId(
                            companyId, distributorId, startDate, endDate);
            List<Map<String, Object>> documentList = new ArrayList<>();

            if (salesInvoiceList.size() > 0) {
                documentList =
                        documentService.getDocumentFileByDistributorIdAndCompanyId(salesInvoiceList);
            }

            List<Map<String, ByteArrayInputStream>> documentFileList = new ArrayList<>();
            for (Map<String, Object> document : documentList) {
                Map<String, ByteArrayInputStream> documentFile = new HashMap<>();
                if (document.get("file_path") != null) {
                    documentFile.put("file_path", new ByteArrayInputStream(
                            fileDownloadService.fileDownload(document.get("file_path").toString())));
                }
                documentFileList.add(documentFile);
            }

            JRBeanCollectionDataSource dataSource = new
                    JRBeanCollectionDataSource(documentFileList);

            Resource resource = new ClassPathResource("reports/acknowledgedInvoiceReport.jrxml");
            InputStream inputStream = resource.getInputStream();
            JasperDesign jasperDesign = JRXmlLoader.load(inputStream);

            JasperPrint empReport =
                    JasperFillManager.fillReport
                            (
                                    JasperCompileManager.compileReport(jasperDesign)
                                    , parameters
                                    , dataSource
                            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "acknowledgedInvoiceReport.pdf");
            return new ResponseEntity<byte[]>
                    (JasperExportManager.exportReportToPdf(empReport), headers, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<byte[]> getStockValuationReport(
            Long companyId, List<Long> categoryIds, List<Long> depotIds,
            List<Long> productIds, Long fiscalYearId, String fromDateStr, String endDateStr,
            String reportType, Boolean isWithSum, String reportFormat,
            String dateType, HttpServletResponse response) {
        try {
            Map<String, Object> parameters = new HashMap<String, Object>();
            Organization organization = organizationService.findById(companyId);
            AccountingYear accountingYear = null;
            LocalDate fromDate = null;
            LocalDate endDate = null;

            if (fiscalYearId != null) {
                accountingYear = accountingYearService.findById(fiscalYearId);
                if (accountingYear != null) {
                    fromDate = accountingYear.getStartDate();
                    endDate = accountingYear.getEndDate();
                }
            } else {
                if (fromDateStr != null && fromDateStr != "") {
                    fromDate = LocalDate.parse(fromDateStr);
                }

                if (endDateStr != null && endDateStr != "") {
                    endDate = LocalDate.parse(endDateStr);
                }
            }
            parameters.put("productOwner", getProductOwner());
            parameters.put("dateHeader", getReportDate(fromDate, endDate, dateType));
            parameters.put("companyLogo", new ByteArrayInputStream(
                    organizationService.getOrganizationLogoByteData(companyId)));
            parameters.put("companyName", organization.getName() + " [" + organization.getShortName() + "] ");
            parameters.put("address", organization.getAddress());
            parameters.put("email", organization.getEmail());
            parameters.put("webAddress", organization.getWebAddress());
            parameters.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            ApplicationUser applicationUser = applicationUserService.getApplicationUserFromLoginUser();
            parameters.put("user", applicationUser.getName());

            JRBeanCollectionDataSource dataSource = new
                    JRBeanCollectionDataSource(stockService.getStockValuation(
                    companyId, categoryIds, depotIds, productIds, fromDate, endDate, reportType));

            String fileName = "";

            if (!"PDF".equals(reportFormat)) {
                if ("DETAILS".equals(reportType))
                    if (isWithSum) {
                        fileName = "stockValuationReportXlsx";
                    } else
                        fileName = "stockValuationReportWithoutSubTotalXlsx";
                else
                    fileName = "stockValuationSummaryReportXlsx";

                reportService.getReportsXLS(parameters, dataSource,
                        "reports/stock/", fileName, response);
                return null;
            } else {
                if ("DETAILS".equals(reportType)) {
                    if (isWithSum) {
                        fileName = "stockValuationReport";
                    } else
                        fileName = "stockValuationReportWithoutSubTotal";
                } else
                    fileName = "stockValuationSummaryReport";

                return reportService.getReportsPDF(parameters, dataSource,
                        "reports/stock/", fileName, response);
            }

        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String getReportDate(
            LocalDate startDate, LocalDate endDate, String dateType) {
        String dateHeader = "";
        if ("Year".equals(dateType)) {
            if (startDate != null && endDate != null) {
                dateHeader += "From: " + startDate.getYear() + "  ";
                dateHeader += "To: " + endDate.getYear() + "  ";
            } else if (startDate == null && endDate != null) {
                dateHeader += "As of: " + endDate.getYear() + "  ";
            } else if (startDate != null && endDate == null) {
                dateHeader += "From: " + startDate.getYear() + "  ";
                dateHeader += "As of: " + LocalDate.now().getYear() + "  ";
            } else {
                dateHeader += "As of: " + LocalDate.now().getYear() + "  ";
            }
        } else if ("Date".equals(dateType)) {
            if (startDate != null && endDate != null) {
                dateHeader += "From: " + startDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) + "  ";
                dateHeader += "To: " + endDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) + "  ";
            } else if (startDate == null && endDate != null) {
                dateHeader += "As of: " + endDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) + "  ";
            } else if (startDate != null && endDate == null) {
                dateHeader += "From: " + startDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) + "  ";
                dateHeader += "As of: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) + "  ";
            } else {
                dateHeader += "As of: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) + "  ";
            }
        } else if ("Month".equals(dateType)) {
            if (startDate != null && endDate != null) {
                dateHeader += "From: " + startDate.format(DateTimeFormatter.ofPattern("MMM-yyyy")) + "  ";
                dateHeader += "To: " + endDate.format(DateTimeFormatter.ofPattern("MMM-yyyy")) + "  ";
            } else if (startDate == null && endDate != null) {
                dateHeader += "As of: " + endDate.format(DateTimeFormatter.ofPattern("MMM-yyyy")) + "  ";
            } else if (startDate != null && endDate == null) {
                dateHeader += "From: " + startDate.format(DateTimeFormatter.ofPattern("MMM-yyyy")) + "  ";
                dateHeader += "As of: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMM-yyyy")) + "  ";
            } else {
                dateHeader += "As of: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMM-yyyy")) + "  ";
            }
        }

        return dateHeader;
    }

    public String getProductOwner() {
        return "Newgen Technology Limited";
    }


    public ResponseEntity<byte[]> getOrderToCashCycleReport(
            Long companyId, Boolean nationalLocationChecked,
            Long locationTypeLevel, List<Long> locationIds,
            List<Long> salesOfficerIds, List<Long> distributorIds,
            String fromDateStr, String endDateStr,
            String reportType, Boolean isWithSum, String reportFormat,
            String dateType, Boolean allChecked, String locationTypeData,
            String allFilterType, HttpServletResponse response) {
        try {
            Map<String, Object> parameters = new HashMap<String, Object>();
            Organization organization = organizationService.findById(companyId);
            parameters.put("companyName", organization.getName()
                    + " [" + organization.getShortName() + "] ");
//            parameters.put("companyLogo",
//                    new ByteArrayInputStream(
//                    organizationService.getOrganizationLogoByteData(companyId))
//            );
            parameters.put("companyAddress", organization.getAddress());
            parameters.put("companyEmail", organization.getEmail());
            parameters.put("companyWebAddress", organization.getWebAddress());

            LocalDate startDate = null;
            LocalDate endDate = null;
            if (fromDateStr != null && fromDateStr != "") {
                startDate = LocalDate.parse(fromDateStr);
            }

            if (endDateStr != null && endDateStr != "") {
                endDate = LocalDate.parse(endDateStr);
            }
            parameters.put("dateHeader", getReportDate(startDate, endDate, dateType));
            parameters.put("productOwner", getProductOwner());

            ApplicationUser applicationUser = applicationUserService.getApplicationUserFromLoginUser();
            parameters.put("printedBy", applicationUser.getName());

            JRBeanCollectionDataSource dataSource = new
                    JRBeanCollectionDataSource(
                    salesInvoiceService.getOrderToCashCycleReport(
                            companyId, locationIds, salesOfficerIds, distributorIds, startDate, endDate, reportType));

            Map reportData = getOrderToCashReportReportFile(reportType,
                    reportFormat, isWithSum, allChecked, nationalLocationChecked,
                    locationTypeData, locationTypeLevel,
                    salesOfficerIds, distributorIds, allFilterType);
            String fileName = reportData.get("fileName").toString();
            String reportHeader = reportData.get("reportHeader").toString();
            parameters.put("reportHeader", reportHeader);

            if (!"PDF".equals(reportFormat) && "" != reportFormat) {
                reportService.getReportsXLS(parameters,
                        dataSource, "reports/orderToCashCycleReport/", fileName, response);
                return null;
            } else {
                return
                        reportService.getReportsPDF(parameters,
                                dataSource, "reports/orderToCashCycleReport/", fileName, response);
            }

        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    public List<Map<String, Object>> getReceivableOverdueData(
            Long companyId, List<Long> locationIds,
            List<Long> salesOfficerIds, List<Long> distributorIds,
            LocalDate startDate, LocalDate endDate, String reportType) {

        List<Map<String, Object>> salesInvoiceList = new ArrayList<>();
        LocalDate asOnDate = LocalDate.now();
        try {
            if ("SUMMARY".equals(reportType)) {
                salesInvoiceList = salesInvoiceRepository.getInvoiceAgingSummary(
                        companyId, locationIds, salesOfficerIds, distributorIds,
                        startDate, endDate, asOnDate);
            } else
                salesInvoiceList =
                        salesInvoiceRepository.getInvoiceAgingData(companyId, locationIds,
                                salesOfficerIds, distributorIds,
                                startDate, endDate, asOnDate);

            return salesInvoiceList;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    public ResponseEntity<byte[]> getFinishedGoodsAgeingData(
            Long companyId, Long storeId, List<Long> categoryIds, List<Long> depotIds,
            List<Long> productIds, Long fiscalYearId, String fromDateStr, String endDateStr,
            String reportType, Boolean isWithSum, String reportFormat,
            String dateType, HttpServletResponse response) {
        try {
            Map<String, Object> parameters = new HashMap<String, Object>();
            Organization organization = organizationService.findById(companyId);
            AccountingYear accountingYear = null;
            LocalDate fromDate = null;
            LocalDate endDate = null;

            if (fiscalYearId != null) {
                accountingYear = accountingYearService.findById(fiscalYearId);
                if (accountingYear != null) {
                    fromDate = accountingYear.getStartDate();
                    endDate = accountingYear.getEndDate();
                }
            } else {
                if (fromDateStr != null && fromDateStr != "") {
                    fromDate = LocalDate.parse(fromDateStr);
                }

                if (endDateStr != null && endDateStr != "") {
                    endDate = LocalDate.parse(endDateStr);
                }
            }
            parameters.put("productOwner", getProductOwner());
            parameters.put("dateHeader", getReportDate(fromDate, endDate, dateType));
            parameters.put("companyLogo", new ByteArrayInputStream(
                    organizationService.getOrganizationLogoByteData(companyId)));
            parameters.put("companyName", organization.getName() + " [" + organization.getShortName() + "] ");
            parameters.put("address", organization.getAddress());
            parameters.put("email", organization.getEmail());
            parameters.put("webAddress", organization.getWebAddress());
            parameters.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            ApplicationUser applicationUser = applicationUserService.getApplicationUserFromLoginUser();
            parameters.put("user", applicationUser.getName());

            JRBeanCollectionDataSource dataSource = new
                    JRBeanCollectionDataSource(stockService.getFinishedGoodsAgeing(
                    companyId, storeId, categoryIds, depotIds, productIds, fromDate, endDate, reportType));

            String fileName = "";
            String reportHeader = "";

            if (!"PDF".equals(reportFormat)) {
                /*if ("DETAILS".equals(reportType))
                    fileName = "finishedGoodsAgeingReportXls";
                else*/

                fileName = "finishedGoodsAgeingReportXls";
                reportHeader = "Finished Goods Ageing Report";
                parameters.put("reportHeader", reportHeader);
                reportService.getReportsXLS(parameters, dataSource,
                        "reports/goodsageing/", fileName, response);
                return null;
            } else {
                /*if ("DETAILS".equals(reportType)) {
                    if (isWithSum) {
                        fileName = "finishedGoodsAgeingReport";
                    } else
                        fileName = "finishedGoodsAgeingReportWithoutSum";
                } else*/

                fileName = "finishedGoodsAgeingReport";
                reportHeader = "Finished Goods Ageing Report";
                parameters.put("reportHeader", reportHeader);
                return reportService.getReportsPDF(parameters, dataSource,
                        "reports/goodsageing/", fileName, response);
            }

        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Map<String, String> getInvoiceAgeingReportFile (
            String reportType, String reportFormat,
            Boolean isWithSum, Boolean allChecked, Boolean nationalLocationChecked,
            String locationTypeData, Long locationTypeLevel,
            Long categoryTypeLevel, List<Long> productIds,
            List<Long> salesOfficerIds, List<Long> distributorIds,
            String allFilterType) {
        Map<String, String> reportData = new HashMap<>();
        String fileName = "";
        String reportHeader = "";

        if (!"PDF".equals(reportFormat) && "" != reportFormat) {
            if ("DETAILS".equals(reportType)) {
                fileName = "invoiceWiseAgingReport";
                reportHeader = "Invoice Wise Ageing Report";
            } else {
                if (nationalLocationChecked.toString().equals("true")) {
                    fileName = "invoiceAgeingSummaryReport";
                    reportHeader = "Invoice Wise Ageing Summary Report";
                }
            }
        } else {
            if ("DETAILS".equals(reportType)) {
                if (isWithSum) {
                    fileName = "invoiceWiseAgingReportPdf";
                    reportHeader = "Invoice Wise Ageing Report (Details)";
                } else {
                    fileName = "invoiceWiseAgingReportWithoutSumPdf";
                    reportHeader = "Invoice Wise Ageing Report (Without Sum)";
                }
            } else {
                if (nationalLocationChecked.toString().equals("true")
                        //&& (categoryTypeLevel == 0)
                        && (salesOfficerIds == null || salesOfficerIds.size() == 0)
                        && (distributorIds == null || distributorIds.size() == 0)) {
                    if (!"".equals(locationTypeData)) {
                        if ("Zone".equals(locationTypeData)) {
                            fileName = "invoiceAgeingSummaryReportZonePdf";
                            reportHeader = "Invoice Wise Ageing Report (All Zone)";
                        } else if ("Area".equals(locationTypeData)) {
                            fileName = "invoiceAgeingSummaryReportAreaPdf";
                            reportHeader = "Invoice Wise Ageing Report (All Area)";
                        } else if ("Territory".equals(locationTypeData)) {
                            fileName = "invoiceAgeingSummaryReportTerritoryPdf";
                            reportHeader = "Invoice Wise Ageing Report (All Territory)";
                        }
                    } else {
                        fileName = "invoiceAgeingSummaryReportNationalPdf";
                        reportHeader = "Invoice Wise Ageing Report (National)";
                    }
                }
                if (locationTypeLevel > 0
                        && (salesOfficerIds == null || salesOfficerIds.size() == 0)
                        && (distributorIds == null || distributorIds.size() == 0)
                        //&& (categoryTypeLevel == 0)
                ) {
                    if (1 == locationTypeLevel) {
                        if (allChecked) {
                            if ("SO".equals(allFilterType)) {
                                fileName = "invoiceAgeingSummaryReportSoPdf";
                                reportHeader = "Invoice Wise Ageing Report (All Sales Officer)";
                            }
                            else if ("DIS".equals(allFilterType)) {
                                fileName = "invoiceAgeingSummaryReportDistributorPdf";
                                reportHeader = "Invoice Wise Ageing Report (All Distributor)";
                            }
                            else if ("TERRITORY".equals(allFilterType)) {
                                fileName = "invoiceAgeingSummaryReportTerritoryPdf";
                                reportHeader = "Invoice Wise Ageing Report (All Territory)";
                            }
                            else {
                                fileName = "invoiceAgeingSummaryReportAreaPdf";
                                reportHeader = "Invoice Wise Ageing Report (All Area)";
                            }
                        }
                        else {
                            fileName = "invoiceAgeingSummaryReportZonePdf";
                            reportHeader = "Invoice Wise Ageing Report (Zone)";
                        }
                    } else if (2 == locationTypeLevel) {
                        if (allChecked) {
                            if ("SO".equals(allFilterType)) {
                                fileName = "invoiceAgeingSummaryReportSoPdf";
                                reportHeader = "Invoice Wise Ageing Report (All Sales Officer)";
                            }
                            else if ("DIS".equals(allFilterType)) {
                                fileName = "invoiceAgeingSummaryReportDistributorPdf";
                                reportHeader = "Invoice Wise Ageing Report (All Distributor)";
                            }
                            else if ("TERRITORY".equals(allFilterType)) {
                                fileName = "invoiceAgeingSummaryReportTerritoryPdf";
                                reportHeader = "Invoice Wise Ageing Report (All Territory)";
                            }
                            else {
                                fileName = "invoiceAgeingSummaryReportTerritoryPdf";
                                reportHeader = "Invoice Wise Ageing Report (All Territory)";
                            }
                        } else {
                            fileName = "invoiceAgeingSummaryReportAreaPdf";
                            reportHeader = "Invoice Wise Ageing Report (Area)";
                        }
                    } else if (3 == locationTypeLevel) {
                        if (allChecked) {
                            if ("SO".equals(allFilterType)) {
                                fileName = "invoiceAgeingSummaryReportSoPdf";
                                reportHeader = "Invoice Wise Ageing Report (All Sales Officer)";
                            }
                            else if ("DIS".equals(allFilterType)) {
                                fileName = "invoiceAgeingSummaryReportDistributorPdf";
                                reportHeader = "Invoice Wise Ageing Report (All Distributor)";
                            }
                            else if ("TERRITORY".equals(allFilterType)) {
                                fileName = "invoiceAgeingSummaryReportTerritoryPdf";
                                reportHeader = "Invoice Wise Ageing Report (Territory)";
                            }
                            else {
                                fileName = "invoiceAgeingSummaryReportSoPdf";
                                reportHeader = "Invoice Wise Ageing Report (All Sales Officer)";
                            }
                        } else {
                            fileName = "invoiceAgeingSummaryReportTerritoryPdf";
                            reportHeader = "Invoice Wise Ageing Report (Territory)";
                        }
                    }
                } else if (salesOfficerIds.size() > 0 && distributorIds.size() > 0) {
                    fileName = "invoiceAgeingSummaryReportSoDistributorPdf";
                    reportHeader = "Invoice Wise Ageing Report (So and Distributor)";
                } else if (salesOfficerIds.size() > 0) {
                    if (allChecked) {
                        fileName = "invoiceAgeingSummaryReportSoDistributorPdf";
                        reportHeader = "Invoice Wise Ageing Report (All Distributor)";
                     }
                    else {
                        fileName = "invoiceAgeingSummaryReportSoPdf";
                        reportHeader = "Invoice Wise Ageing Report (Sales Officer)";
                    }
                } else if (distributorIds.size() > 0) {
                    fileName = "invoiceAgeingSummaryReportDistributorPdf";
                    reportHeader = "Invoice Wise Ageing Report (Distributor)";
                } else if (categoryTypeLevel > 0 && locationTypeLevel == 0) {
                    if (categoryTypeLevel == 1) {
                        fileName = "invoiceAgeingSummaryCategoryLevel1Pdf";
                        reportHeader = "Invoice Wise Ageing Report (Category)";
                    } else if (categoryTypeLevel == 2) {
                        if (productIds.size() > 0) {
                            fileName = "invoiceAgeingSummaryProductPdf";
                            reportHeader = "Invoice Wise Ageing Report (Product)";
                        } else {
                            fileName = "invoiceAgeingSummaryCategoryLevel2Pdf";
                            reportHeader = "Invoice Wise Ageing Report (Sub Category)";
                        }
                    }
                } else if (locationTypeLevel >= 0 && (categoryTypeLevel > 0)) {
                    if (nationalLocationChecked && 1 == categoryTypeLevel) {
                        fileName = "invoiceAgeingSummaryCategoryLevel1Pdf";
                        reportHeader = "Invoice Wise Ageing Report (National & Category)";
                    } else if (1 == locationTypeLevel && 1 == categoryTypeLevel) {
                        fileName = "invoiceAgeingSummaryReportZoneAndCatLevel1Pdf";
                        reportHeader = "Invoice Wise Ageing Report (Zone & Category)";
                    } else if (1 == locationTypeLevel && 2 == categoryTypeLevel) {
                        if (productIds.size() == 0) {
                            fileName = "invoiceAgeingSummaryReportZoneAndSubCatLevel1Pdf";
                            reportHeader = "Invoice Wise Ageing Report (Zone & Sub Category)";
                        } else {
                            //when no condition meet default format set.
                            fileName = "invoiceAgeingSummaryBlank";
                            reportHeader = "Invoice Wise Ageing Report";
                        }
                    } else {
                        //when no condition meet default format set.
                        fileName = "invoiceAgeingSummaryBlank";
                        reportHeader = "Invoice Wise Ageing Report";
                    }
                }
            }
        }
        reportData.put("fileName", fileName);
        reportData.put("reportHeader", reportHeader);

        return reportData;
    }

    private Map<String, String> getOrderToCashReportReportFile (
            String reportType, String reportFormat,
            Boolean isWithSum, Boolean allChecked,
            Boolean nationalLocationChecked,
            String locationTypeData, Long locationTypeLevel,
            List<Long> salesOfficerIds, List<Long> distributorIds,
            String allFilterType) {
        Map<String, String> reportData = new HashMap<>();
        String fileName = "";
        String reportHeader = "";

        if (!"PDF".equals(reportFormat) && !"".equals(reportFormat)) {
            if ("DETAILS".equals(reportType)) {
                fileName = "OrderToCashCycleReportXls";
                reportHeader = "Order To Cash Cycle Details Report";
            } else {
                fileName = "OrderToCashCycleSummaryReport";
                reportHeader = "Order To Cash Cycle Summary Report";
            }
        } else {
            if ("DETAILS".equals(reportType)) {
                if (isWithSum) {
                    fileName = "OrderToCashCycleReportWithSubTotal";
                    reportHeader = "Order To Cash Cycle Details Report [With Sub Total]";
                } else {
                    fileName = "OrderToCashCycleReport";
                    reportHeader = "Order To Cash Cycle Details Report (Without Sub Total)";
                }
            } else {
                 if (nationalLocationChecked.toString().equals("true")
                            && (salesOfficerIds == null || salesOfficerIds.size() == 0)
                            && (distributorIds == null || distributorIds.size() == 0)) {
                        if (!"".equals(locationTypeData)) {
                            if ("Zone".equals(locationTypeData)) {
                                fileName = "OrderToCashCycleSummaryReportByZone";
                                reportHeader = "Order To Cash Cycle Summary Report (All Zone)";
                            } else if ("Area".equals(locationTypeData)) {
                                fileName = "OrderToCashCycleSummaryReportByArea";
                                reportHeader = "Order To Cash Cycle Summary Report (All Area)";
                            } else if ("Territory".equals(locationTypeData)) {
                                fileName = "OrderToCashCycleSummaryReportByTerritory";
                                reportHeader = "Order To Cash Cycle Summary Report (All Territory)";
                            }
                        } else if (allChecked) {
                            fileName = "OrderToCashCycleSummaryReportByZone";
                            reportHeader = "Order To Cash Cycle Summary Report (All Zone)";
                        } else {
                            fileName = "OrderToCashCycleSummaryReportByNational";
                            reportHeader = "Order To Cash Cycle Summary Report (National)";
                        }
                    }
                    if (locationTypeLevel > 0
                            && (salesOfficerIds == null || salesOfficerIds.size() == 0)
                            && (distributorIds == null || distributorIds.size() == 0)) {
                        if (1 == locationTypeLevel) {
                            if (allChecked) {
                                if ("SO".equals(allFilterType)) {
                                    fileName = "OrderToCashCycleSummaryReportBySO";
                                    reportHeader = "Order To Cash Cycle Summary Report (All Sales Officer)";
                                }
                                else if ("DIS".equals(allFilterType)) {
                                    fileName = "OrderToCashCycleSummaryReportByDistributor";
                                    reportHeader = "Order To Cash Cycle Summary Report (All Distributor)";
                                }
                                else if ("TERRITORY".equals(allFilterType)) {
                                    fileName = "OrderToCashCycleSummaryReportByTerritory";
                                    reportHeader = "Order To Cash Cycle Summary Report  (All Territory)";
                                }
                                else {
                                    fileName = "OrderToCashCycleSummaryReportByArea";
                                    reportHeader = "Order To Cash Cycle Summary Report  (All Area)";
                                }
                            } else {
                                fileName = "OrderToCashCycleSummaryReportByZone";
                                reportHeader = "Order To Cash Cycle Summary Report (Zone)";
                            }
                        } else if (2 == locationTypeLevel) {
                            if (allChecked) {
                                if ("SO".equals(allFilterType)) {
                                    fileName = "OrderToCashCycleSummaryReportBySO";
                                    reportHeader = "Order To Cash Cycle Summary Report (All Sales Officer)";
                                }
                                else if ("DIS".equals(allFilterType)) {
                                    fileName = "OrderToCashCycleSummaryReportByDistributor";
                                    reportHeader = "Order To Cash Cycle Summary Report (All Distributor)";
                                }
                                else if ("TERRITORY".equals(allFilterType)) {
                                    fileName = "OrderToCashCycleSummaryReportByTerritory";
                                    reportHeader = "Order To Cash Cycle Summary Report  (Territory)";
                                }
                                else {
                                    fileName = "OrderToCashCycleSummaryReportByTerritory";
                                    reportHeader = "Order To Cash Cycle Summary Report  (All Territory)";
                                }
                            } else {
                                fileName = "OrderToCashCycleSummaryReportByArea";
                                reportHeader = "Order To Cash Cycle Summary Report  (Area)";
                            }
                        } else if (3 == locationTypeLevel) {
                            if (allChecked) {
                                if ("SO".equals(allFilterType)) {
                                    fileName = "OrderToCashCycleSummaryReportBySO";
                                    reportHeader = "Order To Cash Cycle Summary Report (All Sales Officer)";
                                }
                                else if ("DIS".equals(allFilterType)) {
                                    fileName = "OrderToCashCycleSummaryReportByDistributor";
                                    reportHeader = "Order To Cash Cycle Summary Report (All Distributor)";
                                }
                                else if ("TERRITORY".equals(allFilterType)) {
                                    fileName = "OrderToCashCycleSummaryReportByTerritory";
                                    reportHeader = "Order To Cash Cycle Summary Report  (Territory)";
                                }
                                else {
                                    fileName = "OrderToCashCycleSummaryReportBySO";
                                    reportHeader = "Order To Cash Cycle Summary Report (All Sales Officer)";
                                }
                            } else {
                                fileName = "OrderToCashCycleSummaryReportByTerritory";
                                reportHeader = "Order To Cash Cycle Summary Report  (Territory)";
                            }
                        }
                    } else if (salesOfficerIds.size() > 0 && distributorIds.size() > 0) {
                        fileName = "OrderToCashCycleSummaryReportByDistributor";
                        reportHeader = "Order To Cash Cycle Summary Report (So and Distributor)";
                    } else if (salesOfficerIds.size() > 0) {
                        if (allChecked) {
                            fileName = "OrderToCashCycleSummaryReportByDistributor";
                            reportHeader = "Order To Cash Cycle Summary Report (All Distributor)";
                        } else {
                            fileName = "OrderToCashCycleSummaryReportBySO";
                            reportHeader = "Order To Cash Cycle Summary Report (Sales Officer)";
                        }
                    } else if (distributorIds.size() > 0) {
                        fileName = "OrderToCashCycleSummaryReportByDistributor";
                        reportHeader = "Order To Cash Cycle Summary Report (Distributor)";
                    }
            }
        }

        reportData.put("fileName", fileName);
        reportData.put("reportHeader", reportHeader);

        return reportData;
    }

    public ResponseEntity<byte[]> getDistributorLedgerSummary(
            Long companyId, Boolean nationalLocationChecked,
            Long locationTypeLevel, List<Long> locationIds,
            List<Long> salesOfficerIds, List<Long> distributorIds,
            String fromDateStr, String endDateStr,
            String reportType, Boolean isWithSum, String reportFormat,
            String dateType, Boolean allChecked, String locationTypeData,
            String allFilterType, HttpServletResponse response) {
        try {
            Map<String, Object> parameters = new HashMap<String, Object>();
            Organization organization = organizationService.findById(companyId);
            parameters.put("companyName", organization.getName()
                    + " [" + organization.getShortName() + "] ");
            parameters.put("companyLogo",
                    new ByteArrayInputStream(
                    organizationService.getOrganizationLogoByteData(companyId))
            );
            parameters.put("companyAddress", organization.getAddress());
            parameters.put("companyEmail", organization.getEmail());
            parameters.put("companyWebAddress", organization.getWebAddress());

            LocalDate startDate = null;
            LocalDate endDate = null;
            if (fromDateStr != null && fromDateStr != "") {
                startDate = LocalDate.parse(fromDateStr);
            }

            if (endDateStr != null && endDateStr != "") {
                endDate = LocalDate.parse(endDateStr);
            }
            parameters.put("dateHeader", getReportDate(startDate, endDate, dateType));
            parameters.put("productOwner", getProductOwner());
            ApplicationUser applicationUser = applicationUserService.getApplicationUserFromLoginUser();
            parameters.put("printedBy", applicationUser.getName());

            JRBeanCollectionDataSource dataSource = new
                    JRBeanCollectionDataSource(
                    distributorService.findLedgerSummary(
                            companyId, distributorIds, salesOfficerIds,
                            locationIds, startDate, endDate));

            Map reportData = getLedgerSummaryReportFile(reportType,
                    reportFormat, isWithSum, allChecked, nationalLocationChecked,
                    locationTypeData, locationTypeLevel,
                    salesOfficerIds, distributorIds, allFilterType);
            String fileName = reportData.get("fileName").toString();
            String reportHeader = reportData.get("reportHeader").toString();
            parameters.put("reportHeader", reportHeader);

            if (!"PDF".equals(reportFormat) && "" != reportFormat) {
                reportService.getReportsXLS(parameters,
                        dataSource, "reports/ledgersummary/", fileName, response);
                return null;
            } else {
                return
                        reportService.getReportsPDF(parameters,
                                dataSource, "reports/ledgersummary/", fileName, response);
            }

        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Map<String, String> getLedgerSummaryReportFile (
            String reportType, String reportFormat,
            Boolean isWithSum, Boolean allChecked,
            Boolean nationalLocationChecked,
            String locationTypeData, Long locationTypeLevel,
            List<Long> salesOfficerIds, List<Long> distributorIds,
            String allFilterType) {
        Map<String, String> reportData = new HashMap<>();
        String fileName = "";
        String reportHeader = "";

        if (!"PDF".equals(reportFormat) && !"".equals(reportFormat)) {
            fileName = "DistributorLedgerSummaryReportXlsx";
            reportHeader = "Distributor Ledger Summary Report";
        }
        else {
            fileName = "DistributorLedgerSummaryReport";
            reportHeader = "Distributor Ledger Summary Report";
        }

        reportData.put("fileName", fileName);
        reportData.put("reportHeader", reportHeader);

        return reportData;
    }
}

