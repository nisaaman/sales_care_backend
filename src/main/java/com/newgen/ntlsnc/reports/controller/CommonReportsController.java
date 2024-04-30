package com.newgen.ntlsnc.reports.controller;

import com.mashape.unirest.http.HttpResponse;
import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.globalsettings.service.ReportService;
import com.newgen.ntlsnc.reports.service.CommonReportsService;
import com.newgen.ntlsnc.reports.service.FinanceReportsService;
import com.newgen.ntlsnc.salesandcollection.service.PaymentCollectionAdjustmentService;
import com.newgen.ntlsnc.salesandcollection.service.ProfitabilityAnalysisReportService;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.OutputStream;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;
import java.util.Objects;


/**
 * @author nisa
 * @date 9/20/22
 * @time 1:54 PM
 */
@RestController
@RequestMapping("/api/reports")
public class CommonReportsController {

    final CommonReportsService commonReportsService;
    @Autowired
    ProfitabilityAnalysisReportService profitabilityAnalysisReportService;
    @Autowired
    ReportService reportService;
    @Autowired
    FinanceReportsService financeReportsService;
    @Autowired
    PaymentCollectionAdjustmentService paymentCollectionAdjustmentService;

    public CommonReportsController(CommonReportsService commonReportsService) {
        this.commonReportsService = commonReportsService;
    }

    @GetMapping("/distributor-ledger")
    public ResponseEntity<byte[]> getDistributorLedgerDetailsReport(
            @RequestParam(value = "distributorId") Long distributorId,
            @RequestParam(value = "companyId") Long companyId,
            @RequestParam(value = "startDateStr") String startDateStr,
            @RequestParam(value = "endDateStr") String endDateStr) {
        return commonReportsService.getDistributorLedgerDetailsReport(
                distributorId, companyId, startDateStr, endDateStr);
    }

    @GetMapping("/distributor-ledger-view")
    public ResponseEntity<?> getDistributorLedgerBalanceViewReport(
            @RequestParam Long distributorId,
            @RequestParam String startDateStr,
            @RequestParam String endDateStr,
            @RequestParam Long companyId) {
        ApiResponse response = new ApiResponse(false);

        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);
        response.setSuccess(commonReportsService.getDistributorLedgerBalanceViewDataSource(
                distributorId, startDate, endDate, companyId));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(path = "/receivable-invoice-statement")
    public ResponseEntity<byte[]> getReceivableInvoiceStatementReport(
            @RequestParam(value = "companyId") Long companyId,
            @RequestParam(value = "invoiceNatureId") Long invoiceNatureId,
            @RequestParam(value = "distributorId", required = false) Long distributorId,
            @RequestParam(value = "reportFormat") String reportFormat,
            @RequestParam(value = "startDate") String startDate,
            @RequestParam(value = "endDate") String endDate,

            HttpServletResponse response) {
        return commonReportsService.getReceivableInvoiceStatementCashReport(
                companyId, invoiceNatureId,
                distributorId, reportFormat, startDate, endDate, response);
    }

    @GetMapping("/delivery-challan")
    public ResponseEntity<byte[]> getDeliveryChallanReportTest(@RequestParam(value = "deliveryChallanId") Long deliveryChallanId) {
        return commonReportsService.getDeliveryChallanReport(deliveryChallanId);
    }


    @GetMapping("/stock-valuation")
    public ResponseEntity<byte[]> getStockValuationReport(
            @RequestParam(value = "companyId") Long companyId,
            @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds,
            @RequestParam(value = "depotIds", required = false) List<Long> depotIds,
            @RequestParam(value = "productIds", required = false) List<Long> productIds,
            @RequestParam(value = "fiscalYearId", required = false) Long fiscalYearId,
            @RequestParam(value = "startDate", required = false) String fromDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "reportType") String reportType,
            @RequestParam(value = "isWithSum") Boolean isWithSum,
            @RequestParam(value = "reportFormat") String reportFormat,
            @RequestParam(value = "dateType") String dateType
            , HttpServletResponse response) {
        return commonReportsService.getStockValuationReport(
                companyId, categoryIds, depotIds, productIds, fiscalYearId, fromDate, endDate
                , reportType, isWithSum, reportFormat, dateType, response);
    }

    @GetMapping("/picking-list")
    public ResponseEntity<byte[]> getPickingListReport(@RequestParam(value = "pickingId") Long pickingId,
                                                       @RequestParam(value = "companyId") Long companyId) {
        return commonReportsService.getPickingListReport(pickingId, companyId);
    }

    @GetMapping("/restricted-list")
    public ResponseEntity<byte[]> getRestrictedListReport(
            @RequestParam(value = "companyId") Long companyId,
            @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds,
            @RequestParam(value = "depotIds", required = false) List<Long> depotIds,
            @RequestParam(value = "productIds", required = false) List<Long> productIds,
            @RequestParam(value = "startDate", required = false) String fromDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "reportType") String reportType,
            @RequestParam(value = "isWithSum") Boolean isWithSum,
            @RequestParam(value = "reportFormat") String reportFormat,
            @RequestParam(value = "dateType") String dateType
            , HttpServletResponse response) {
        return commonReportsService.getRestritedListReport(companyId,
                categoryIds, productIds, depotIds,
                !"".equals(fromDate) ? LocalDate.parse(fromDate) : null,
                !"".equals(endDate) ? LocalDate.parse(endDate) : null, dateType, reportType,
                reportFormat, isWithSum, response);
    }

    @GetMapping("/invoice-type-wise-summary-report")
    public ResponseEntity<byte[]> getInvoiceTypeWiseSummaryReport(
            @RequestParam(value = "companyId") Long companyId,
            @RequestParam(value = "salesOfficerId", required = false) Long salesOfficerId,
            @RequestParam(value = "reportFormat") String reportFormat,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "distributorId", required = false) Long distributorId,
            @RequestParam(value = "locationIds", required = false) List<Long> locationIds,
            HttpServletResponse response) {
        return commonReportsService.getInvoiceTypeWiseSummaryReport(companyId,
                salesOfficerId, reportFormat, startDate, endDate, distributorId, locationIds, response);
    }

    @GetMapping("/sales-and-collection")
    public ResponseEntity<byte[]> salesAndCollectionReport(
            @RequestParam(value = "companyId") Long companyId,
            @RequestParam(value = "nationalLocationChecked") Boolean nationalLocationChecked,
            @RequestParam(value = "locationTypeLevel") Long locationTypeLevel,
            @RequestParam(value = "locationIds", required = false) List<Long> locationIds,
            @RequestParam(value = "salesOfficerIds", required = false) List<Long> salesOfficerIds,
            @RequestParam(value = "distributorIds", required = false) List<Long> distributorIds,
            @RequestParam(value = "accountingYearId", required = false) Long accountingYearId,
            @RequestParam(value = "startDate") String startDate,
            @RequestParam(value = "endDate") String endDate,
            @RequestParam(value = "dateType") String dateType,
            @RequestParam(value = "isWithSum") Boolean isWithSum,
            @RequestParam(value = "reportFormat") String reportFormat,
            @RequestParam(value = "reportType") String reportType,
            @RequestParam(value = "allChecked") Boolean allChecked,
            @RequestParam(value = "locationTypeData", required = false) String locationTypeData,
            @RequestParam(value = "allFilterType", required = false) String allFilterType,
            HttpServletResponse response) {
        return commonReportsService.getSalesAndCollectionData(
                companyId, nationalLocationChecked, locationTypeLevel,
                locationTypeData, locationIds, salesOfficerIds, distributorIds,
                accountingYearId, startDate, endDate, dateType, reportFormat, reportType,
                isWithSum, allChecked, allFilterType, response);
    }

    @GetMapping("/material-planner")
    public ResponseEntity<byte[]> materialPlannerReport(
            @RequestParam(value = "nationalLocationChecked") Boolean nationalLocationChecked,
            @RequestParam(value = "locationTypeLevel") Long locationTypeLevel,
            @RequestParam(value = "companyId") Long companyId,
            @RequestParam(value = "locationIds", required = false) List<Long> locationIds,
            @RequestParam(value = "categoryTypeLevel") Long categoryTypeLevel,
            @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds,
            @RequestParam(value = "depotIds", required = false) List<Long> depotIds,
            @RequestParam(value = "startDate") String startDate,
            @RequestParam(value = "endDate") String endDate,
            @RequestParam(value = "reportType") String reportType,
            @RequestParam(value = "dateType") String dateType,
            @RequestParam(value = "isWithSum") Boolean isWithSum,
            @RequestParam(value = "reportFormat") String reportFormat,
            @RequestParam(value = "locationTypeData", required = false) String locationTypeData,
            HttpServletResponse response) {
        return commonReportsService.getMaterialPlannerTicket(nationalLocationChecked,
                companyId, locationIds, categoryIds, depotIds,
                startDate, endDate, reportType, dateType, isWithSum, reportFormat,
                locationTypeLevel, locationTypeData, categoryTypeLevel, response);
    }

    @GetMapping("/credit-limit-history-report")
    public ResponseEntity<byte[]> getCreditLimitHistoryReport(
            @RequestParam(value = "nationalLocationChecked") Boolean nationalLocationChecked,
            @RequestParam(value = "companyId") Long companyId,
            @RequestParam(value = "locationTypeLevel") Long locationTypeLevel,
            @RequestParam(value = "locationIds", required = false) List<Long> locationIds,
            @RequestParam(value = "salesOfficerIds", required = false) List<Long> salesOfficerIds,
            @RequestParam(value = "distributorIds", required = false) List<Long> distributorIds,
            @RequestParam(value = "startDate") String startDate,
            @RequestParam(value = "endDate") String endDate,
            @RequestParam(value = "reportType") String reportType,
            @RequestParam(value = "isWithSum") Boolean isWithSum,
            @RequestParam(value = "reportFormat") String reportFormat,
            @RequestParam(value = "dateType") String dateType,
            @RequestParam(value = "allChecked") Boolean allChecked,
            @RequestParam(value = "locationTypeData", required = false) String locationTypeData,
            HttpServletResponse response) {
        return commonReportsService.getCreditLimitHistory(companyId, nationalLocationChecked,
                locationTypeLevel, locationIds, salesOfficerIds, distributorIds,
                startDate, endDate, reportType, isWithSum, reportFormat, dateType,
                allChecked, locationTypeData,
                response);
    }

    @GetMapping("/profitability-analysis")
    public void getProfitabilityAnalysisReport(@RequestParam Map<String, Object> params,
            HttpServletResponse response) {

        try (OutputStream outputStream = response.getOutputStream()) {
            JasperPrint print = profitabilityAnalysisReportService.getProfitabilityReportParams(
                    params, response);
            System.out.println("outputStream: "+ outputStream);
            if (params.get("reportFormat").equals("PDF")) {
                JasperExportManager.exportReportToPdfStream(print, outputStream);
                System.out.println("report: " + outputStream);
                response.setContentType("application/pdf");
                response.addHeader("Content-disposition", "inline; filename=" + "Profitability_Analysis.pdf");
            } else {
                reportService.getExcelExporter("Profitability_Analysis.xlsx", response, print);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/depot-to-depot-movement-history")
    public ResponseEntity<byte[]> depotToDepotMovementHistoryReport(
            @RequestParam(value = "companyId") Long companyId,
            @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds,
            @RequestParam(value = "depotIds", required = false) List<Long> depotIds,
            @RequestParam(value = "productIds", required = false) List<Long> productIds,
            @RequestParam(value = "startDate") String startDate,
            @RequestParam(value = "endDate") String endDate,
            @RequestParam(value = "reportType", required = false) String reportType,
            @RequestParam(value = "isWithSum", required = false) Boolean isWithSum,
            @RequestParam(value = "dateType") String dateType,
            @RequestParam(value = "reportFormat", required = false) String reportFormat,
            HttpServletResponse response) {
        return commonReportsService.depotToDepotMovementHistoryReport(
                companyId, categoryIds, depotIds, productIds, startDate, endDate,
                reportFormat, reportType, isWithSum, dateType,
                response);

    }

    @GetMapping("/invoice-wise-aging-report")
    public ResponseEntity<byte[]> getInvoiceAgingReport(
            @RequestParam(value = "companyId") Long companyId,
            @RequestParam(value = "nationalLocationChecked") Boolean nationalLocationChecked,
            @RequestParam(value = "locationTypeLevel") Long locationTypeLevel,
            @RequestParam(value = "locationIds", required = false) List<Long> locationIds,
            @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds,
            @RequestParam(value = "categoryTypeLevel", required = false) Long categoryTypeLevel,
            @RequestParam(value = "productIds", required = false) List<Long> productIds,
            @RequestParam(value = "salesOfficerIds", required = false) List<Long> salesOfficerIds,
            @RequestParam(value = "distributorIds", required = false) List<Long> distributorIds,
            @RequestParam(value = "startDate", required = false) String fromDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "reportType") String reportType,
            @RequestParam(value = "isWithSum") Boolean isWithSum,
            @RequestParam(value = "reportFormat") String reportFormat,
            @RequestParam(value = "dateType") String dateType,
            @RequestParam(value = "allChecked") Boolean allChecked,
            @RequestParam(value = "locationTypeData", required = false) String locationTypeData,
            @RequestParam(value = "allFilterType", required = false) String allFilterType,
            HttpServletResponse response) {

        return commonReportsService.getInvoiceAgingReport(
                companyId, nationalLocationChecked, locationTypeLevel, locationIds,
                categoryIds, categoryTypeLevel, productIds,
                salesOfficerIds, distributorIds, fromDate, endDate,
                reportType, isWithSum, reportFormat, dateType, allChecked,
                locationTypeData, allFilterType, response);
    }

    @GetMapping("/receivable-overdue-report")
    public ResponseEntity<byte[]> getReceivableOverdueReport(
            @RequestParam(value = "companyId") Long companyId,
            @RequestParam(value = "nationalLocationChecked") Boolean nationalLocationChecked,
            @RequestParam(value = "locationTypeLevel") Long locationTypeLevel,
            @RequestParam(value = "locationIds", required = false) List<Long> locationIds,
            @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds,
            @RequestParam(value = "categoryTypeLevel", required = false) Long categoryTypeLevel,
            @RequestParam(value = "productIds", required = false) List<Long> productIds,
            @RequestParam(value = "salesOfficerIds", required = false) List<Long> salesOfficerIds,
            @RequestParam(value = "distributorIds", required = false) List<Long> distributorIds,
            @RequestParam(value = "startDate", required = false) String fromDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "reportType") String reportType,
            @RequestParam(value = "isWithSum") Boolean isWithSum,
            @RequestParam(value = "reportFormat") String reportFormat,
            @RequestParam(value = "dateType") String dateType,
            @RequestParam(value = "allChecked") Boolean allChecked,
            @RequestParam(value = "locationTypeData", required = false) String locationTypeData,
            @RequestParam(value = "allFilterType", required = false) String allFilterType,
            HttpServletResponse response) {

        return commonReportsService.getReceivableOverdueReport(
                companyId, nationalLocationChecked, locationTypeLevel, locationIds,
                categoryIds, categoryTypeLevel, productIds,
                salesOfficerIds, distributorIds, fromDate, endDate,
                reportType, isWithSum, reportFormat, dateType, allChecked,
                locationTypeData, allFilterType, response);
    }

    @GetMapping("/acknowledged-invoice-report")
    public ResponseEntity<byte[]> getAcknowledgedInvoiceReportList(
            @RequestParam(name = "companyId") Long companyId,
            @RequestParam(name = "distributorId") Long distributorId,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "dateType") String dateType) {
        return commonReportsService.getAcknowledgedInvoiceReportList(
                companyId, distributorId, startDate, endDate, dateType);
    }


    @GetMapping("/order-to-cash-cycle")
    public ResponseEntity<byte[]> getOrderToCashCycleReport(
            @RequestParam(value = "companyId") Long companyId,
            @RequestParam(value = "nationalLocationChecked") Boolean nationalLocationChecked,
            @RequestParam(value = "locationTypeLevel") Long locationTypeLevel,
            @RequestParam(value = "locationIds", required = false) List<Long> locationIds,
            @RequestParam(value = "salesOfficerIds", required = false) List<Long> salesOfficerIds,
            @RequestParam(value = "distributorIds", required = false) List<Long> distributorIds,
            @RequestParam(value = "startDate", required = false) String fromDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "reportType") String reportType,
            @RequestParam(value = "isWithSum") Boolean isWithSum,
            @RequestParam(value = "reportFormat") String reportFormat,
            @RequestParam(value = "dateType") String dateType,
            @RequestParam(value = "allChecked") Boolean allChecked,
            @RequestParam(value = "locationTypeData", required = false) String locationTypeData,
            @RequestParam(value = "allFilterType", required = false) String allFilterType,
            HttpServletResponse response) {

        return commonReportsService.getOrderToCashCycleReport(
                companyId, nationalLocationChecked, locationTypeLevel, locationIds,
                salesOfficerIds, distributorIds, fromDate, endDate,
                reportType, isWithSum, reportFormat, dateType, allChecked, locationTypeData,
                allFilterType, response);

    }

    @GetMapping("/finished-goods-ageing")
    public ResponseEntity<byte[]> getFinishedGoodsAgeingReport(
            @RequestParam(value = "companyId") Long companyId,
            @RequestParam(value = "storeId", required = false) Long storeId,
            @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds,
            @RequestParam(value = "depotIds", required = false) List<Long> depotIds,
            @RequestParam(value = "productIds", required = false) List<Long> productIds,
            @RequestParam(value = "fiscalYearId", required = false) Long fiscalYearId,
            @RequestParam(value = "startDate", required = false) String fromDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "reportType") String reportType,
            @RequestParam(value = "isWithSum") Boolean isWithSum,
            @RequestParam(value = "reportFormat") String reportFormat,
            @RequestParam(value = "dateType") String dateType
            , HttpServletResponse response) {
        return commonReportsService.getFinishedGoodsAgeingData(
                companyId, storeId, categoryIds, depotIds, productIds, fiscalYearId, fromDate, endDate
                , reportType, isWithSum, reportFormat, dateType, response);
    }

    @GetMapping("/invoice-wise-aging-report-preview")
    public List<Map<String, Object>>  invoiceAgingReportPreview(
            @RequestParam(value = "companyId") Long companyId,
            @RequestParam(value = "nationalLocationChecked") Boolean nationalLocationChecked,
            @RequestParam(value = "locationTypeLevel") Long locationTypeLevel,
            @RequestParam(value = "locationIds", required = false) List<Long> locationIds,
            @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds,
            @RequestParam(value = "categoryTypeLevel", required = false) Long categoryTypeLevel,
            @RequestParam(value = "productIds", required = false) List<Long> productIds,
            @RequestParam(value = "salesOfficerIds", required = false) List<Long> salesOfficerIds,
            @RequestParam(value = "distributorIds", required = false) List<Long> distributorIds,
            @RequestParam(value = "startDate", required = false) String fromDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr,
            @RequestParam(value = "reportType") String reportType,
            @RequestParam(value = "isWithSum") Boolean isWithSum,
            @RequestParam(value = "reportFormat") String reportFormat,
            @RequestParam(value = "dateType") String dateType,
            @RequestParam(value = "allChecked") Boolean allChecked,
            @RequestParam(value = "locationTypeData", required = false) String locationTypeData,
            HttpServletResponse response) {

        LocalDate startDate = null;
        LocalDate endDate = null;
        if (fromDateStr != null && fromDateStr != "") {
            startDate = LocalDate.parse(fromDateStr);
        }

        if (endDateStr != null && endDateStr != "") {
            endDate = LocalDate.parse(endDateStr);
        }
        return null;
        /*return financeReportsService.getInvoiceAgingData(
                companyId,  locationIds, salesOfficerIds,  distributorIds,
                startDate,  endDate,  reportType);*/
    }

    @GetMapping("/distributor-ledger-summary-report")
    public ResponseEntity<byte[]> getDistributorLedgerSummaryReport(
            @RequestParam(value = "companyId") Long companyId,
            @RequestParam(value = "nationalLocationChecked") Boolean nationalLocationChecked,
            @RequestParam(value = "locationTypeLevel") Long locationTypeLevel,
            @RequestParam(value = "locationIds", required = false) List<Long> locationIds,
            @RequestParam(value = "salesOfficerIds", required = false) List<Long> salesOfficerIds,
            @RequestParam(value = "distributorIds", required = false) List<Long> distributorIds,
            @RequestParam(value = "startDate", required = false) String fromDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "reportType") String reportType,
            @RequestParam(value = "isWithSum") Boolean isWithSum,
            @RequestParam(value = "reportFormat") String reportFormat,
            @RequestParam(value = "dateType") String dateType,
            @RequestParam(value = "allChecked") Boolean allChecked,
            @RequestParam(value = "locationTypeData", required = false) String locationTypeData,
            @RequestParam(value = "allFilterType", required = false) String allFilterType,
            HttpServletResponse response) {

        return commonReportsService.getDistributorLedgerSummary(
                companyId, nationalLocationChecked, locationTypeLevel, locationIds,
                salesOfficerIds, distributorIds, fromDate, endDate, reportType,
                isWithSum, reportFormat, dateType, allChecked, locationTypeData,
                allFilterType, response);

    }

}