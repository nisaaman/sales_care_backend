package com.newgen.ntlsnc.reports.service;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.service.*;
import com.newgen.ntlsnc.salesandcollection.service.PaymentCollectionService;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sunipa
 * @date 03/20/24
 * @time 10:27 PM
 */
@Service
public class CollectionReportsService {

    @Autowired
    CommonReportsService commonReportsService;
    @Autowired
    PaymentCollectionService paymentCollectionService;
    @Autowired
    ReportService reportService;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    OrganizationService organizationService;

    public ResponseEntity<byte[]> getCollectionData(
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
            parameters.put("dateHeader", commonReportsService.getReportDate(startDate, endDate, dateType));
            parameters.put("productOwner", commonReportsService.getProductOwner());
            parameters.put("companyLogo", new ByteArrayInputStream(
                    organizationService.getOrganizationLogoByteData(companyId)));

            Map<String, Object> reportMap = paymentCollectionService.getCollectionReportData(
                    companyId, locationIds, salesOfficerIds, distributorIds,
                    accountingYearId, startDate, endDate, dateType, parameters);

            parameters = (Map<String, Object>) reportMap.get("parameters");

            List<Map<String, Object>> collectionList =
                    (List<Map<String, Object>>) reportMap.get("collectionList");

            JRBeanCollectionDataSource dataSource = new
                    JRBeanCollectionDataSource(collectionList);

            Map reportData = getCollectionFileName(reportType,
                    reportFormat, isWithSum, allChecked, nationalLocationChecked,
                    locationTypeData, locationTypeLevel,
                    salesOfficerIds, distributorIds, allFilterType);
            String fileName = reportData.get("fileName").toString();
            String reportHeader = reportData.get("reportHeader").toString();
            parameters.put("reportHeader", reportHeader);

            if (!"PDF".equals(reportFormat) && "" != reportFormat) {
                reportService.getReportsXLS(parameters,
                        dataSource, "reports/collection/", fileName, response);
                return null;
            } else {
                return
                        reportService.getReportsPDF(parameters,
                                dataSource, "reports/collection/", fileName, response);
            }

        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Map<String, String> getCollectionFileName(
            String reportType, String reportFormat,
            Boolean isWithSum, Boolean allChecked, Boolean nationalLocationChecked,
            String locationTypeData, Long locationTypeLevel,
            List<Long> salesOfficerIds, List<Long> distributorIds,
            String allFilterType) {
        Map<String, String> reportData = new HashMap<>();
        String fileName = "";
        String reportHeader = "";
        if ("DETAILS".equals(reportType)) {
            if (!"PDF".equals(reportFormat) && "" != reportFormat) {
                fileName = "CollectionReportXls";
                reportHeader = "Collection Details Report";
            } else {
                if (isWithSum) {
                    fileName = "CollectionReport";
                    reportHeader = "Collection Details Report";
                } else {
                    fileName = "CollectionWithoutSubtotal";
                    reportHeader = "Collection Details Report";
                }
            }
        } else {
            if (nationalLocationChecked.toString().equals("true")) {
                if (!"".equals(locationTypeData)) {
                    if ("Zone".equals(locationTypeData)) {
                        fileName = "CollectionSummaryZone";
                        reportHeader = "Collection Summary (All Zone)";
                    } else if ("Area".equals(locationTypeData)) {
                        fileName = "CollectionSummaryArea";
                        reportHeader = "Collection Summary (All Area)";
                    } else if ("Territory".equals(locationTypeData)) {
                        fileName = "CollectionSummaryTerritory";
                        reportHeader = "Collection Summary (All Territory)";
                    }
                }  else {
                    fileName = "CollectionSummaryNational";
                    reportHeader = "Collection Summary (National)";
                }
            }

            if (locationTypeLevel > 0 &&
                    salesOfficerIds.size() == 0 && distributorIds.size() == 0) {
                if (1 == locationTypeLevel) {
                    if (allChecked) {
                        if ("SO".equals(allFilterType)) {
                            fileName = "CollectionSummarySo";
                            reportHeader = "Collection Summary (All Sales Officer)";
                        }
                        else if ("DIS".equals(allFilterType)) {
                            fileName = "CollectionSummarySoAndDistributor";
                            reportHeader = "Collection Summary (All Distributor)";
                        }
                        else if ("TERRITORY".equals(allFilterType)) {
                            fileName = "CollectionSummaryTerritory";
                            reportHeader = "Collection Summary (All Territory)";
                        }
                        else {
                            fileName = "CollectionSummaryArea";
                            reportHeader = "Collection Summary (All Area)";
                        }
                    }
                    else {
                        fileName = "CollectionSummaryZone";
                        reportHeader = "Collection Summary (Zone)";
                    }
                } else if (2 == locationTypeLevel) {
                    if (allChecked) {
                        if ("SO".equals(allFilterType)) {
                            fileName = "CollectionSummarySo";
                            reportHeader = "Collection Summary (All Sales Officer)";
                        }
                        else if ("DIS".equals(allFilterType)) {
                            fileName = "CollectionSummarySoAndDistributor";
                            reportHeader = "Collection Summary (All Distributor)";
                        }
                        else if ("TERRITORY".equals(allFilterType)) {
                            fileName = "CollectionSummaryTerritory";
                            reportHeader = "Collection Summary (All Territory)";
                        }
                        else {
                            fileName = "CollectionSummaryTerritory";
                            reportHeader = "Collection Summary (All Territory)";
                        }
                    } else {
                        fileName = "CollectionSummaryArea";
                        reportHeader = "Collection Summary (Area)";
                    }
                } else if (3 == locationTypeLevel) {
                    if (allChecked) {
                        if ("SO".equals(allFilterType)) {
                            fileName = "CollectionSummarySo";
                            reportHeader = "Collection Summary (All Sales Officer)";
                        }
                        else if ("DIS".equals(allFilterType)) {
                            fileName = "CollectionSummarySoAndDistributor";
                            reportHeader = "Collection Summary (All Distributor)";
                        }
                        else if ("TERRITORY".equals(allFilterType)) {
                            fileName = "CollectionSummaryTerritory";
                            reportHeader = "Collection Summary (All Territory)";
                        }
                        else {
                            fileName = "CollectionSummarySo";
                            reportHeader = "Collection Summary (All Sales Officer)";
                        }
                    } else {
                        fileName = "CollectionSummaryTerritory";
                        reportHeader = "Collection Summary (Territory)";
                    }
                }
            } else if (salesOfficerIds.size() > 0 && distributorIds.size() > 0) {
                fileName = "CollectionSummarySoAndDistributor";
                reportHeader = "Collection Summary (So and Distributor)";
            } else if (salesOfficerIds.size() > 0) {
                if (allChecked) {
                    fileName = "CollectionSummarySoAndDistributor";
                    reportHeader = "Collection Summary (All Distributor)";
                } else {
                    fileName = "CollectionSummarySo";
                    reportHeader = "Collection Summary (Sales Officer)";
                }
            } else if (distributorIds.size() > 0) {
                fileName = "CollectionSummaryDistributor";
                reportHeader = "Collection Summary (Distributor)";
            }
        }

        reportData.put("fileName", fileName);
        reportData.put("reportHeader", reportHeader);
        return reportData;
    }

}

