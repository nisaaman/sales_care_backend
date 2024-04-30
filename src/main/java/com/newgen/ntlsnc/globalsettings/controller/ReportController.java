package com.newgen.ntlsnc.globalsettings.controller;

import com.newgen.ntlsnc.globalsettings.service.ProductTradePriceService;
import com.newgen.ntlsnc.globalsettings.service.ReportService;
import com.newgen.ntlsnc.salesandcollection.service.CreditDebitNoteService;
import com.newgen.ntlsnc.salesandcollection.service.PaymentCollectionAdjustmentService;
import com.newgen.ntlsnc.salesandcollection.service.SalesBudgetReportService;
import com.newgen.ntlsnc.salesandcollection.service.SalesInvoiceService;
import com.newgen.ntlsnc.supplychainmanagement.service.SalesReturnService;
import io.micrometer.core.lang.Nullable;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @author kamal
 * @Date ২১/৯/২২
 */

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    SalesInvoiceService salesInvoiceService;
    @Autowired
    ProductTradePriceService productTradePriceService;
    @Autowired
    SalesBudgetReportService salesBudgetReportService;
    @Autowired
    PaymentCollectionAdjustmentService paymentCollectionAdjustmentService;
    @Autowired
    SalesReturnService salesReturnService;
    @Autowired
    CreditDebitNoteService creditDebitNoteService;
    @Autowired
    ReportService reportService;

    @GetMapping(path = "/invoice/{reportName}")
    public void getReport(@NotNull @PathVariable String reportName,
                          @Nullable @RequestParam Map<String, Object> params,
                          HttpServletResponse response) {
        try {
            JasperPrint print = salesInvoiceService.getInvoiceReport(reportName, params);
            response.setContentType("application/pdf");
            response.addHeader("Content-disposition", "inline; filename=" + reportName + ".pdf");
            OutputStream outputStream = response.getOutputStream();
            JasperExportManager.exportReportToPdfStream(print, outputStream);
            outputStream.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping(path = "/trade-price/history-by-category")
    public void getReport(@RequestParam Map<String, Object> params, HttpServletResponse response) {
        try (OutputStream outputStream = response.getOutputStream()){
            JasperPrint print = productTradePriceService.getPriceHistoryReportByCategory(params);
            JasperExportManager.exportReportToPdfStream(print, outputStream);
            if (params.get("reportFormat").equals("PDF")) {
                response.setContentType("application/pdf");
                response.addHeader("Content-disposition", "inline; filename=" + "trade_price_change_history.pdf");
            } else {
                response.setContentType("application/application/vnd.openxmlformats-officedocument.spreadsheetml.sheet; charset=UTF-8");
                response.addHeader("Content-disposition", "attachment; filename=" + "trade_price_change_history.xlsx");
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/sales-budget-report")
    public void getSalesBudgetReport( @RequestParam(value = "companyId") Long companyId,
                                      @RequestParam(value = "locationId", required = false) List<Long> locationIds,
                                      @RequestParam(value = "salesOfficerId", required = false) List<Long> salesOfficerIds,
                                      @RequestParam(value = "distributorId", required = false) List<Long> distributorIds,
                                      @RequestParam(value = "fiscalYearId") Long accountingYearId,
                                      @RequestParam(value = "userLoginId") Long userLoginId,
                                      @RequestParam(value = "startDate") String startDate,
                                      @RequestParam(value = "endDate") String endDate,
                                      @RequestParam(value = "dateType") String dateType,
                                      @RequestParam(value = "reportFormat") String reportFormat,
                                      HttpServletResponse response) {
        try (OutputStream outputStream = response.getOutputStream()) {
            JasperPrint print = salesBudgetReportService.getSalesBudgetReport(companyId, locationIds, salesOfficerIds,
                    distributorIds, accountingYearId, userLoginId, startDate, endDate, dateType, response, reportFormat);
            if ("EXCEL".equals(reportFormat)) {
                reportService.getExcelExporter("SalesBudget.xlsx", response, print);
            } else {
                JasperExportManager.exportReportToPdfStream(print, outputStream);
                response.setContentType("application/pdf");
                response.addHeader("Content-disposition", "inline; filename=" + "SalesBudget.pdf");
            }

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/sales-return")
    public void getSalesReturnReport(@RequestParam Map<String, Object> params, HttpServletResponse response) {
        try (OutputStream outputStream = response.getOutputStream()) {
            JasperPrint print = salesReturnService.getSalesReturnReport(params);

            if (params.get("reportFormat").equals("PDF")) {
                JasperExportManager.exportReportToPdfStream(print, outputStream);
                response.setContentType("application/pdf");
                response.addHeader("Content-disposition", "inline; filename=" + "SalesReturn.pdf");
            } else {
                reportService.getExcelExporter("SalesReturn.xlsx", response, print);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/performance-report")
    public void getPerformanceReport(@RequestParam Map<String, Object> params, HttpServletResponse response) {
        try (OutputStream outputStream = response.getOutputStream()) {
            JasperPrint print = paymentCollectionAdjustmentService.getPerformanceReport(params);
            if (params.get("reportFormat").equals("PDF")) {
                JasperExportManager.exportReportToPdfStream(print, outputStream);
                response.setContentType("application/pdf");
                response.addHeader("Content-disposition", "inline; filename=" + "PerformanceReport.pdf");
            } else {
                reportService.getExcelExporter("PerformanceReport.xlsx", response, print);
//                response.setContentType("application/application/vnd.openxmlformats-officedocument.spreadsheetml.sheet; charset=UTF-8");
//                response.addHeader("Content-disposition", "attachment; filename=" + "PerformanceReport.xlsx");
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/debit-credit-note")
    public void getDebitCreditNoteReport(@RequestParam Map<String, Object> params, HttpServletResponse response) {
        try (OutputStream outputStream = response.getOutputStream()){
            JasperPrint print = creditDebitNoteService.getDebitCreditNoteReport(params);

            if (params.get("reportFormat").equals("PDF")) {
                JasperExportManager.exportReportToPdfStream(print, outputStream);
                response.setContentType("application/pdf");
                response.addHeader("Content-disposition", "inline; filename=" + "DebitCreditNoteReport.pdf");
            } else {
                reportService.getExcelExporter("DebitCreditNoteReport.xlsx", response, print);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
