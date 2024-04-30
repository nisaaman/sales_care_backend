package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.*;
import com.newgen.ntlsnc.common.enums.CalculationType;
import com.newgen.ntlsnc.common.enums.FileType;
import com.newgen.ntlsnc.globalsettings.entity.*;
import com.newgen.ntlsnc.globalsettings.repository.LocationRepository;
import com.newgen.ntlsnc.globalsettings.service.*;
import com.newgen.ntlsnc.reports.service.CommonReportsService;
import com.newgen.ntlsnc.salesandcollection.dto.SalesInvoiceDto;
import com.newgen.ntlsnc.salesandcollection.entity.Distributor;
import com.newgen.ntlsnc.salesandcollection.entity.SalesInvoice;
import com.newgen.ntlsnc.salesandcollection.repository.SalesInvoiceRepository;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvDeliveryChallan;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvTransaction;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvTransactionDetails;
import com.newgen.ntlsnc.supplychainmanagement.repository.InvTransactionDetailsRepository;
import com.newgen.ntlsnc.supplychainmanagement.service.InvDeliveryChallanService;
import com.newgen.ntlsnc.supplychainmanagement.service.SalesReturnService;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import net.sf.jasperreports.engine.JasperPrint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author anika
 * @Date ১৯/৪/২২
 */
@Service
public class SalesInvoiceService implements IService<SalesInvoice> {
    @Autowired
    InvoiceNatureService invoiceNatureService;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    TermsAndConditionsService termsAndConditionsService;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    SalesInvoiceRepository salesInvoiceRepository;
    @Autowired
    DistributorService distributorService;
    @Autowired
    InvDeliveryChallanService invDeliveryChallanService;
    @Autowired
    EnumeService enumeService;
    @Autowired
    AccountingYearService accountingYearService;
    @Autowired
    LocationService locationService;
    @Autowired
    SalesReturnService salesReturnService;
    @Autowired
    SalesInvoiceChallanMapService salesInvoiceChallanMapService;
    @Autowired
    ReportService reportService;
    @Autowired
    FileUploadService fileUploadService;
    @Autowired
    DocumentService documentService;
    @Autowired
    BarcodeGenerateService barcodeGenerateService;
    @Autowired
    LocationRepository locationRepository;
    @Autowired
    MonthWiseSalesAndCollectionBudgetService monthWiseSalesAndCollectionBudgetService;
    @Autowired
    InvTransactionDetailsRepository invTransactionDetailsRepository;
    @Autowired
    InvoiceSequenceService invoiceSequenceService;
    @Autowired
    CommonReportsService commonReportsService;
    private static final Logger logger = LoggerFactory.getLogger(SalesInvoiceService.class);
    @Transactional
    @Override
    public SalesInvoice create(Object object) {
        try {
            SalesInvoiceDto salesInvoiceDto = (SalesInvoiceDto) object;
            SalesInvoice salesInvoice = new SalesInvoice();

            salesInvoice.setInvoiceDate(LocalDate.now());
            salesInvoice.setInvoiceAmount(salesInvoiceDto.getInvoiceAmount() - salesInvoiceDto.getInvoiceDiscount());
            salesInvoice.setVatAmount(salesInvoiceDto.getVatAmount());
            salesInvoice.setDiscountAmount(salesInvoiceDto.getDiscountAmount());
            salesInvoice.setInvoiceDiscount(salesInvoiceDto.getInvoiceDiscount());
            salesInvoice.setRemainingAmount(salesInvoice.getInvoiceAmount());
            salesInvoice.setRemarks(salesInvoiceDto.getRemarks());

            if (!salesInvoiceDto.getInvoiceDiscountType().equals("")){
                salesInvoice.setInvoiceDiscountType(CalculationType.valueOf(salesInvoiceDto.getInvoiceDiscountType()));
            }

            salesInvoice.setOrganization(organizationService.getOrganizationFromLoginUser());
            salesInvoice.setInvoiceNature(invoiceNatureService.findById(salesInvoiceDto.getInvoiceNatureId()));
            salesInvoice.setCompany(organizationService.findById(salesInvoiceDto.getCompanyId()));
            salesInvoice.setDistributor(distributorService.findById(salesInvoiceDto.getDistributorId()));

            if (salesInvoiceDto.getTermsAndConditionsId() != null) {
                salesInvoice.setTermsAndConditions(termsAndConditionsService.findById(salesInvoiceDto.getTermsAndConditionsId()));
            }

            List<InvDeliveryChallan> invDeliveryChallanList = invDeliveryChallanService.getAllInvDeliveryChallanByIds(salesInvoiceDto.getDeliveryChallanIds());
            // Validate duplicate portfolio and get category
            ProductCategory productCategory = validateCategory(invDeliveryChallanList);
            //Generate and set invoice number
            salesInvoice.setInvoiceNo(
                    invoiceSequenceService.getInvoiceSequence(CommonConstant.DOCUMENT_ID_FOR_SALES_INVOICE,
                            salesInvoice, productCategory));

            if (!this.validate(salesInvoice)) {
                return null;
            }
            salesInvoice = salesInvoiceRepository.save(salesInvoice);

            salesInvoiceChallanMapService.createMapFromInvoice(invDeliveryChallanList, salesInvoice);
            return salesInvoice;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public SalesInvoice update(Long id, Object object) {
        SalesInvoiceDto salesInvoiceDto = (SalesInvoiceDto) object;
        Optional<SalesInvoice> salesInvoiceOptional = salesInvoiceRepository.findById(salesInvoiceDto.getId());
        SalesInvoice salesInvoice = salesInvoiceOptional.get();
        salesInvoice.setInvoiceNo(salesInvoiceDto.getInvoiceNo());
        salesInvoice.setRemarks(salesInvoiceDto.getRemarks());
        salesInvoice.setInvoiceDate(LocalDate.parse(salesInvoiceDto.getInvoiceDate()));
        salesInvoice.setIsAccepted(salesInvoiceDto.getIsAccepted());
        salesInvoice.setAcceptanceDate(LocalDate.parse(salesInvoiceDto.getAcceptanceDate()));
        salesInvoice.setInvoiceAmount(salesInvoiceDto.getInvoiceAmount());
        salesInvoice.setRemainingAmount(salesInvoiceDto.getRemainingAmount());
        salesInvoice.setOrganization(organizationService.getOrganizationFromLoginUser());
        if (salesInvoiceDto.getInvoiceNatureId() != null) {
            salesInvoice.setInvoiceNature(invoiceNatureService.findById(salesInvoiceDto.getInvoiceNatureId()));
        }
        if (salesInvoiceDto.getCompanyId() != null) {
            salesInvoice.setCompany(organizationService.findById(salesInvoiceDto.getCompanyId()));
        }
        if (salesInvoiceDto.getTermsAndConditionsId() != null) {
            salesInvoice.setTermsAndConditions(termsAndConditionsService.findById(salesInvoiceDto.getTermsAndConditionsId()));
        }
        if (salesInvoiceDto.getAcceptedById() != null) {
            salesInvoice.setAcceptedBy(applicationUserService.findById(salesInvoiceDto.getAcceptedById()));
        }
        if (salesInvoiceDto.getDistributorId() != null) {
            Distributor distributor = distributorService.findById(salesInvoiceDto.getDistributorId());
            salesInvoice.setDistributor(distributor);
        }
        return salesInvoiceRepository.save(salesInvoice);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            Optional<SalesInvoice> salesInvoiceOptional = salesInvoiceRepository.findById(id);
            if (!salesInvoiceOptional.isPresent()) {
                throw new Exception("Sales Invoice Not exist");
            }
            SalesInvoice salesInvoice = salesInvoiceOptional.get();
            salesInvoice.setIsDeleted(true);
            salesInvoiceRepository.save(salesInvoice);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public SalesInvoice findById(Long id) {
        try {
            Optional<SalesInvoice> optionalSalesInvoice = salesInvoiceRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalSalesInvoice.isPresent()) {
                throw new Exception("Sales Invoice Not exist with id " + id);
            }
            return optionalSalesInvoice.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<SalesInvoice> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return salesInvoiceRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Transactional
    public SalesInvoice save(SalesInvoice salesInvoice) {
        try {
            salesInvoiceRepository.save(salesInvoice);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return salesInvoice;
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }

    public List<SalesInvoice> getOrdCalculatorEligibleInvoiceListByDistributorIdAndCompanyId(Long companyId, Long distributorId) {
        List<SalesInvoice> salesInvoiceList = salesInvoiceRepository.findAllForOrdCalculableInvoiceByCompanyIdAndDistributorId(companyId, distributorId);
        return salesInvoiceList;
    }

    public List<Map> getAllInvoiceWiseAdjustedAmountByCompanyAndDistributorAndInvoiceNature(Long companyId, Long distributorId, Long invoiceNatureId) {
        List<Map> adjustmentAmountList = salesInvoiceRepository.findAllInvoiceWiseAdjustedAmountByCompanyAndDistributorAndInvoiceNature(companyId, distributorId, invoiceNatureId);
        return adjustmentAmountList;
    }

    public List<Map> getAllInvoiceWiseOrdAmountByCompanyAndDistributor(Long companyId, Long distributorId, Long invoiceNatureId) {
        List<Map> ordAmountList = salesInvoiceRepository.findAllInvoiceWiseOrdAmountByCompanyAndDistributor(companyId, distributorId, invoiceNatureId);
        return ordAmountList;
    }

    public List<Map> getAllInvoiceWiseDeliveryQuantityByCompanyAndDistributor(
            Long companyId, Long distributorId, Long invoiceNatureId,
        LocalDate fromDate, LocalDate toDate) {
        List<Map> quantityList =
                invDeliveryChallanService.getAllInvoiceAndChallanWiseDeliveryQuantityByCompanyAndDistributor(
                        companyId, distributorId, invoiceNatureId, fromDate, toDate);
        return quantityList;
    }

    public List<Map> getAllInvoiceOrdAndAdjustedAmountAndChallanByDistributorAndInvoiceNature(
            Long companyId, Long distributorId, Long invoiceNatureId,
            String invoiceFromDate, String invoiceToDate) {
        LocalDate fromDate = null;
        LocalDate toDate = null;
        if (invoiceFromDate != null) {
            fromDate = LocalDate.parse(invoiceFromDate);
        }
        if (invoiceToDate != null) {
            toDate = LocalDate.parse(invoiceToDate);
        }

        List<Map> invoiceList = new ArrayList<>();
        List<Map> adjustmentAmountList = getAllInvoiceWiseAdjustedAmountByCompanyAndDistributorAndInvoiceNature(companyId, distributorId, invoiceNatureId);
        List<Map> ordAmountList = getAllInvoiceWiseOrdAmountByCompanyAndDistributor(companyId, distributorId, invoiceNatureId);
        List<Map> quantityList =
                getAllInvoiceWiseDeliveryQuantityByCompanyAndDistributor(
                        companyId, distributorId, invoiceNatureId, fromDate, toDate);

        invoiceList = setAdjustmentAmountToInvoiceList(adjustmentAmountList, invoiceList);
        invoiceList = setOrdToInvoiceList(ordAmountList, invoiceList);
        invoiceList = setChallanListToInvoiceList(quantityList, invoiceList);

        return invoiceList;
    }

    public List<SalesInvoice> findInvoiceListByDistributorId(Long distributorId, Long companyId) {
        try {

            return salesInvoiceRepository.getAllByDistributorIdAndCompanyIdAndIsDeletedIsFalseAndIsActiveIsTrueAndIsAcceptedIsTrueAndRemainingAmountGreaterThanOrderByInvoiceDateDesc(
                    distributorId, companyId, (float) 0);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map<String, Object>> getUnadjustedInvoiceListByDistributorAndPaymentDate(
            Long organizationId, Long companyId, Long distributorId, LocalDate paymentDate, LocalDate fromDate, LocalDate toDate) {
        try {
            List<Map<String, Object>> data =
                    salesInvoiceRepository.getUnadjustedInvoiceListByDistributorAndPaymentDate(
                    organizationId, companyId, distributorId, paymentDate, fromDate, toDate);

            return data;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map<String, Object>> getUnadjustedOpeningInvoiceListByDistributor(
            Long companyId, Long distributorId, LocalDate fromDate, LocalDate toDate) {
        try {
            List<Map<String, Object>> data =
                    salesInvoiceRepository.getUnadjustedOpeningInvoiceListByDistributor(
                            companyId, distributorId, fromDate, toDate);

            return data;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    //=============================================start private methods section====================================
    private List<Map> setAdjustmentAmountToInvoiceList(List<Map> adjustmentAmountList, List<Map> invoiceList) {
        for (int i = 0; i < adjustmentAmountList.size(); i++) {
            Map invoice = new HashMap();
            invoice.put("invoiceId", adjustmentAmountList.get(i).get("invoice_id"));
            invoice.put("invoiceNo", adjustmentAmountList.get(i).get("invoice_no"));
            invoice.put("invoiceAmount", adjustmentAmountList.get(i).get("invoice_amount"));
            invoice.put("invoiceDate", adjustmentAmountList.get(i).get("invoice_date"));
            invoice.put("invoiceNatureName", adjustmentAmountList.get(i).get("invoice_nature_name"));
            invoice.put("totalAdjustedAmount", adjustmentAmountList.get(i).get("total_adjusted_amount"));
            invoice.put("ordAmount", 0); // it will fill in ord portion
            invoice.put("challanList", new ArrayList<>()); // it will fill in challan portion
            invoiceList.add(invoice);
        }
        return invoiceList;
    }

    private List<Map> setOrdToInvoiceList(List<Map> ordAmountList, List<Map> invoiceList) {
        ordAmountList.forEach(i -> {
            boolean isExist = false;
            for (int j = 0; j < invoiceList.size(); j++) {
//                if (i.get("invoice_id") == invoiceList.get(j).get("invoiceId")) {
                if (i.get("invoice_id").toString().equals( invoiceList.get(j).get("invoiceId").toString())) {
                    invoiceList.get(j).put("ordAmount", i.get("ord_amount"));
                    isExist = true;
                    return;
                }
            }
            if (!isExist) {
                Map invoice = new HashMap();
                invoice.put("invoiceId", i.get("invoice_id"));
                invoice.put("invoiceNo", i.get("invoice_no"));
                invoice.put("invoiceAmount", i.get("invoice_amount"));
                invoice.put("invoiceDate", i.get("invoice_date"));
                invoice.put("invoiceNatureName", i.get("invoice_nature_name"));
                invoice.put("totalAdjustedAmount", 0);
                invoice.put("ordAmount", i.get("ord_amount"));
                invoice.put("challanList", new ArrayList<>());
                invoiceList.add(invoice);
            }
        });
        return invoiceList;
    }

    public List getAllByCompanyAndDistributor(Long companyId, Long distributorId) {
        try {
            List<Map<String, Object>> salesInvoices =
                    salesInvoiceRepository.getInvoiceListForDebitCreditNote(companyId, distributorId);
            return salesInvoices;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Map getInvoiceListPageAllFilterList(Long companyId) {
        Map response = new HashMap();
        List<Map> approvalStatusList = enumeService.findAllApprovalStatus();
        List<Map<String, Object>> accountingYearList = accountingYearService.getAllByCompanyId(companyId);
        List<Location> locationList = locationService.findAllTerritoryByCompanyId(companyId);

        response.put("approvalStatusList", approvalStatusList);
        response.put("accountingYearList", accountingYearList);
        response.put("locationList", locationList);
        return response;
    }

    public List<Map> getAllInvoiceableProductsWithTotalAmountByDeliveryChallanId(Long deliveryChallanId) {
        try {
            List<Map> products = salesInvoiceRepository.getAllInvoiceableProductsWithTotalAmountByDeliveryChallanId(deliveryChallanId);
            return products;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private List<Map> setChallanListToInvoiceList(List<Map> quantityList, List<Map> invoiceList) {
//        List<Map> invoiceList = new ArrayList<>();
        quantityList.forEach(i -> {
            boolean isExist = false;
            for (int j = 0; j < invoiceList.size(); j++) {
//                if (i.get("invoice_id") == invoiceList.get(j).get("invoiceId")) {
                if (i.get("invoice_id").toString().equals(invoiceList.get(j).get("invoiceId").toString())) {
                    Map challan = new HashMap();
                    challan.put("deliveryChallanId", i.get("delivery_challan_id"));
                    challan.put("challanNo", i.get("challan_no"));
                    challan.put("totalQuantity", i.get("total_quantity"));

                    ((List<Map>) invoiceList.get(j).get("challanList")).add(challan);
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                Map invoice = new HashMap();
                invoice.put("invoiceId", i.get("invoice_id"));
                invoice.put("invoiceNo", i.get("invoice_no"));
                invoice.put("invoiceAmount", i.get("invoice_amount"));
                invoice.put("invoiceDate", i.get("invoice_date"));
                invoice.put("invoiceNatureName", i.get("invoice_nature_name"));
                invoice.put("totalAdjustedAmount", 0); // when no adjusted amount
                invoice.put("ordAmount", 0);  //// when no ord

                List<Map> challanList = new ArrayList<>();
                Map challan = new HashMap();
                challan.put("deliveryChallanId", i.get("delivery_challan_id"));
                challan.put("challanNo", i.get("challan_no"));
                challan.put("totalQuantity", i.get("total_quantity"));

                challanList.add(challan);
                invoice.put("challanList", challanList);

                invoiceList.add(invoice);
            }

        });
        return invoiceList;
    }

    //=============================================end private methods section====================================


    //    ============= start  report service===========================================================================================
    public JasperPrint getInvoiceReport(String reportName, Map<String, Object> params) throws IOException {
        Long invoiceId = Long.parseLong(params.get("id").toString());
        SalesInvoice salesInvoice = findById(invoiceId);

        List<Map> lastPayment = salesInvoiceRepository.getLastPaymentRecord(salesInvoice.getCompany().getId(),
                salesInvoice.getDistributor().getId(), salesInvoice.getCreatedDate());
        for (int i = 0; i < lastPayment.size(); i++) {
            params.put("lastInvoiceNo", lastPayment.get(i).get("invoice_no"));
            params.put("lastInvoiceDate", lastPayment.get(i).get("invoice_date"));
            DecimalFormat df = new DecimalFormat("0.00");
            if (lastPayment.get(i).get("collection_amount") != null) {
                params.put("lastPaymentAmount",
                        Float.parseFloat(df.format(Float.parseFloat(lastPayment.get(i).get("collection_amount").toString()))));
            } else {
                params.put("lastPaymentAmount", 0F);
            }
            if (lastPayment.get(i).get("invoice_amount") != null) {
                params.put("lastInvoiceAmount",
                        Float.parseFloat(df.format(Float.parseFloat(lastPayment.get(i).get("invoice_amount").toString()))));
            } else {
                params.put("lastInvoiceAmount", 0F);
            }
            params.put("lastPaymentDate", lastPayment.get(i).get("payment_date"));
        }

        params.put("id", invoiceId);
        params.put("invoiceNo", salesInvoice.getInvoiceNo());
        params.put("invoiceAmountInWord", NumberToBanglaTaka.convert(salesInvoice.getInvoiceAmount()));
        params.put("invoiceDate", salesInvoice.getInvoiceDate().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
        params.put("invoiceNature", salesInvoice.getInvoiceNature().getName());
        params.put("productDiscount", salesInvoice.getDiscountAmount());
        params.put("invoiceDiscount", salesInvoice.getInvoiceDiscount());
        params.put("invoiceVat", salesInvoice.getVatAmount());
        params.put("companyName", salesInvoice.getCompany().getName());
        params.put("companyLogo", new ByteArrayInputStream(organizationService.getOrganizationLogoByteData(
                salesInvoice.getCompany().getId())));
        params.put("companyAddress", salesInvoice.getCompany().getAddress());
        params.put("companyEmail", salesInvoice.getCompany().getEmail());
        params.put("distributorName", salesInvoice.getDistributor().getDistributorName());
        params.put("distributorContractNo", salesInvoice.getDistributor().getContactNo());
        params.put("distributorAddress", salesInvoice.getDistributor().getShipToAddress());
        params.put("termsAndCondition", salesInvoice.getTermsAndConditions() != null ? salesInvoice.getTermsAndConditions().getTermsAndConditions() : "");

        if (salesInvoice.getInvoiceDiscountType() != null
                && CalculationType.PERCENTAGE.equals(salesInvoice.getInvoiceDiscountType())) {
            Float percent =
                    Float.valueOf(Math.round((salesInvoice.getInvoiceDiscount() * 100) /
                            (salesInvoice.getInvoiceAmount() +
                                    salesInvoice.getInvoiceDiscount())));
                    //salesInvoice.getDiscountAmount() -
                    params.put("invoiceDiscountPercentage", "@" + Math.round(percent) + "%");
        }

        List<Map> previousInvoiceList =
                findLastThreeDueInvoiceList(salesInvoice.getCompany().getId(),
                        salesInvoice.getDistributor().getId(), invoiceId, salesInvoice.getCreatedDate());
        for (int i = 0; i < previousInvoiceList.size(); i++) {
            params.put("preInvoiceNo_" + i, previousInvoiceList.get(i).get("invoice_no").toString());
            params.put("preInvoiceDate_" + i, previousInvoiceList.get(i).get("invoice_date").toString());
            params.put("preInvoiceAmount_" + i, Float.parseFloat(previousInvoiceList.get(i).get("invoice_amount").toString()));
            params.put("preInvoiceDueDays_" + i, previousInvoiceList.get(i).get("due_days").toString());
        }

        Float previousTotalInvoiceAmount = getTotalPreviousInvoiceAmountByCompanyAndDistributorAndCurrentInvoice(salesInvoice.getCompany().getId(), salesInvoice.getDistributor().getId(), invoiceId);
        Float previousTotalInvoicePaymentAmount = getTotalPreviousInvoicePaymentAmountByCompanyAndDistributorAndCurrentInvoice(salesInvoice.getCompany().getId(), salesInvoice.getDistributor().getId(), invoiceId);
        Float previousTotalInvoiceAdjustedAmount = getTotalPreviousInvoiceAdjustedAmountByCompanyAndDistributorAndCurrentInvoice(salesInvoice.getCompany().getId(), salesInvoice.getDistributor().getId(), invoiceId);
        Float totalAdvanceAmount = distributorService.getDistributorAdvanceBalance(salesInvoice.getCompany().getId(), salesInvoice.getDistributor().getId(), salesInvoice.getCreatedDate());
        Float totalReturnAmount = salesReturnService.getSalesReturnAmount(salesInvoice.getCompany().getId(), salesInvoice.getDistributor().getId(), salesInvoice.getCreatedDate());
        /*Float previousTotalInvoiceOrdAmount = getTotalPreviousInvoiceOrdAmountByCompanyAndDistributorAndCurrentInvoice(
                salesInvoice.getCompany().getId(), salesInvoice.getDistributor().getId(), invoiceId);*/
        if (previousTotalInvoicePaymentAmount >0 &&
                previousTotalInvoicePaymentAmount >= totalReturnAmount) {
            totalAdvanceAmount += totalReturnAmount;
        }
        else if (previousTotalInvoicePaymentAmount >0 &&
                totalReturnAmount > previousTotalInvoicePaymentAmount) {

        }
        Float totalOutstandingAmount = previousTotalInvoiceAmount
                - previousTotalInvoicePaymentAmount
                //- previousTotalInvoiceOrdAmount
                + previousTotalInvoiceAdjustedAmount
                + salesInvoice.getInvoiceAmount();
        DecimalFormat df = new DecimalFormat("0.00");

        params.put("previousTotalInvoiceAmount", previousTotalInvoiceAmount);
        params.put("previousTotalInvoicePaymentAmount", previousTotalInvoicePaymentAmount);
        params.put("previousTotalInvoiceAdjustedAmount", previousTotalInvoiceAdjustedAmount);
        params.put("totalAdvanceAmount", Float.parseFloat(df.format(totalAdvanceAmount)));
        params.put("currentInvoiceAmount", salesInvoice.getInvoiceAmount());
        params.put("totalOutstandingAmount", Float.parseFloat(df.format(totalOutstandingAmount)));

        BufferedImage barCodeImage = barcodeGenerateService.getBarCodeImage(salesInvoice.getInvoiceNo());
        params.put("barCode", new ByteArrayInputStream(
                CommonUtilityService.getBufferedImageToByteArray(barCodeImage)));

        Map<String, Object> loginUser = applicationUserService.getMe();
        params.put("printedBy", loginUser.get("userName"));
        Object o = loginUser.get("designation");
        params.put("printedByDesignation", ((Designation) o).getName());

        return reportService.getReports(reportName, params);
    }
//    ==============end  report service==========================================================================================

    public List<SalesInvoice> getAllByCompanyAndDistributorAndIsAcceptedFalse(Long companyId, Long distributorId) {
        try {
            List<SalesInvoice> salesInvoices = salesInvoiceRepository.findAllByCompanyIdAndDistributorIdAndIsAcceptedFalseAndIsActiveTrueAndIsDeletedFalse(companyId, distributorId);
            return salesInvoices;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public SalesInvoice saveDocument(Object object) {
        try {
            SalesInvoiceDto salesInvoiceDto = (SalesInvoiceDto) object;
            SalesInvoice salesInvoice = this.findById(salesInvoiceDto.getId());

            Organization organization = organizationService.getOrganizationFromLoginUser();

            salesInvoice.setIsAccepted(true);
            salesInvoice.setAcceptedBy(applicationUserService.findById(salesInvoiceDto.getAcceptedById()));
            salesInvoice.setAcceptanceDate(LocalDate.parse(salesInvoiceDto.getAcceptanceDate()));

            salesInvoiceRepository.save(salesInvoice);

            if (salesInvoiceDto.getFile() != null) {
                String filePath = fileUploadService.fileUpload(salesInvoiceDto.getFile(), FileType.DOCUMENT.getCode(),
                        "SalesInvoice",
                        organization.getId(), salesInvoice.getCompany().getId());

                documentService.save("SalesInvoice", filePath, salesInvoice.getId(),
                        fileUploadService.getFileNameFromFilePath(filePath), FileType.DOCUMENT.getCode()
                        , organization, salesInvoice.getCompany().getId(), salesInvoiceDto.getFile().getSize());
            } else {
                throw new Exception("Please Select file first!!");
            }

            return salesInvoice;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map> findLastThreeDueInvoiceList(Long companyId, Long distributorId,
                                                 Long currentInvoiceId, LocalDateTime invoiceDate) {
        try {
            List<Map> list = salesInvoiceRepository.findLastThreeDueInvoiceList(companyId,
                    distributorId, currentInvoiceId, invoiceDate);
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Float getTotalPreviousInvoiceAmountByCompanyAndDistributorAndCurrentInvoice(Long companyId, Long distributorId, Long currentInvoiceId) {
        Float totalPreviousInvoiceAmount = 0.0f;
        Map map = salesInvoiceRepository.getTotalPreviousInvoiceAmountByCompanyAndDistributorAndCurrentInvoice(companyId, distributorId, currentInvoiceId);
        totalPreviousInvoiceAmount = Float.parseFloat(map.get("total_previous_invoice_amount").toString());
        return totalPreviousInvoiceAmount;
    }

    public Float getTotalPreviousInvoicePaymentAmountByCompanyAndDistributorAndCurrentInvoice(Long companyId, Long distributorId, Long currentInvoiceId) {
        Float totalPreviousInvoicePaymentAmount = 0.0f;
        Map map = salesInvoiceRepository.getTotalPreviousInvoicePaymentAmountByCompanyAndDistributorAndCurrentInvoice(companyId, distributorId, currentInvoiceId);
        totalPreviousInvoicePaymentAmount = Float.parseFloat(map.get("total_previous_payment_amount").toString());
        return totalPreviousInvoicePaymentAmount;
    }

    public Float getTotalPreviousInvoiceOrdAmountByCompanyAndDistributorAndCurrentInvoice(
            Long companyId, Long distributorId, Long currentInvoiceId) {
        Float totalPreviousInvoiceOrdAmount = 0.0f;
        Map map = salesInvoiceRepository.getTotalPreviousInvoicePaymentAmountByCompanyAndDistributorAndCurrentInvoice(companyId, distributorId, currentInvoiceId);
        totalPreviousInvoiceOrdAmount = Float.parseFloat(map.get("total_previous_ord_amount").toString());
        return totalPreviousInvoiceOrdAmount;
    }

    public Float getTotalPreviousInvoiceAdjustedAmountByCompanyAndDistributorAndCurrentInvoice(Long companyId, Long distributorId, Long currentInvoiceId) {
        Float totalPreviousInvoiceAdjustedAmount = 0.0f;
        Map map = salesInvoiceRepository.getTotalPreviousInvoiceAdjustedAmountByCompanyAndDistributorAndCurrentInvoice(companyId, distributorId, currentInvoiceId);
        totalPreviousInvoiceAdjustedAmount = Float.parseFloat(map.get("previous_adjusted_amount").toString());
        return totalPreviousInvoiceAdjustedAmount;
    }

    public Map getSalesInvoiceByInvoiceNo(String invoiceNo) {

        return salesInvoiceRepository.getSalesInvoiceByInvoiceNo(invoiceNo);
    }

    public Float getTotalInvoiceAdvanceAmountByCompanyAndDistributor(Long companyId, Long distributorId) {
        Float totalInvoiceAdvanceAmount = 0.0f;
        Map map = salesInvoiceRepository.getTotalInvoiceAdvanceAmountByCompanyAndDistributor(companyId, distributorId);
        totalInvoiceAdvanceAmount = Float.parseFloat(map.get("total_advance_amount").toString());
        return totalInvoiceAdvanceAmount;
    }

    @Transactional
    public SalesInvoice deductInvoiceRemainingAmount(SalesInvoice salesInvoice, Float deductedRemainingAmount) {
        try {
            salesInvoice.setRemainingAmount(salesInvoice.getRemainingAmount() - deductedRemainingAmount);
            return salesInvoiceRepository.save(salesInvoice);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /*Total revenue growth =
    [(current period revenue - previous same period revenue)qty
    / previous same period revenue] x 100
    Contribution margin = Net Sales – Variable Costs or Adjustments
    Contribution Margin Ratio = (Net Sales – Variable Costs or Adjustments)
    / Variable Costs or Adjustments x 100*/
    public Map<String, Object> getSalesAndCollectionData(
            Long companyId, List<Long> locationIds,
            List<Long> salesOfficerIds, List<Long> distributorIds,
            Long accountingYearId, LocalDate startDate, LocalDate endDate,
            String reportType, String dateType, Map<String, Object> parameters) {

        Map<String, Object> reportMap = new HashMap<>();

        try {
            locationRepository.SNC_CHILD_LOCATION_HIERARCHY(
                    Long.parseLong(String.valueOf(companyId)));

            if (startDate == null || endDate == null) {
                Map<String, LocalDate> dateMap =
                        accountingYearService.getAccountingYearDate(accountingYearId);
                if (dateMap != null) {
                    startDate = dateMap.get("startDate");
                    endDate = dateMap.get("endDate");
                }
            }

            List<Integer> monthList = DateUtil.monthListBetweenDates(startDate, endDate);
            LocalDate startDateLastYear = startDate.minusYears(1);
            LocalDate endDateLastYear = endDate.minusYears(1);
            List<Map<String, Object>> salesOverViewList = new ArrayList<>();
            /*if ("byProduct".equals(reportType)) {
                salesOverViewList =
                        salesInvoiceRepository.getSalesOverViewProduct(salesOfficerIds,
                                startDate, endDate, startDateLastYear, endDateLastYear,
                                accountingYearId, monthList, companyId, locationIds);
            } else*/
                salesOverViewList =
                        salesInvoiceRepository.getSalesOverViewSalesOficer(salesOfficerIds,
                                distributorIds, startDate, endDate, startDateLastYear, endDateLastYear,
                                accountingYearId, monthList, companyId, locationIds);

            //parameters.put("startDateLastYear", startDateLastYear);
            //parameters.put("endDateLastYear", endDateLastYear);
            parameters.put("accountingYearId", accountingYearId);
            parameters.put("companyId", companyId);
            parameters.put("lastDateHeader",
                    commonReportsService.getReportDate(startDateLastYear,
                            endDateLastYear, dateType));

            reportMap.put("parameters", parameters);
            reportMap.put("salesOverViewList", salesOverViewList);

            return reportMap;

        } catch (Exception e) {
            logger.error("An error occurred: ", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    private ProductCategory validateCategory(List<InvDeliveryChallan> deliveryChallanList) throws Exception {
        Set<InvTransaction> invTransactionSet = deliveryChallanList.stream().map(InvDeliveryChallan::getInvTransaction).collect(Collectors.toSet());
        List<InvTransactionDetails> invTransactionDetailsList = invTransactionDetailsRepository.findAllByInvTransactionIn(invTransactionSet);
        Set<ProductCategory> productCategorySet = invTransactionDetailsList.stream().map(it ->
                it.getProduct().getProductCategory().getParent() != null ?
                        getProductCategory(it.getProduct().getProductCategory().getParent()) :
                        it.getProduct().getProductCategory()
        ).collect(Collectors.toSet());

        if (productCategorySet.size() > 1) {
            throw new Exception("Multiple product portfolio not allowed in a single invoice.");
        } else {
            return productCategorySet.iterator().next();
        }

    }

    private ProductCategory getProductCategory(ProductCategory productCategory) {
        while (productCategory.getParent() != null) {
            productCategory = productCategory.getParent();
        }

        return productCategory;
    }

    public Document getAcknowledgementDocumentInfo(Long invoiceId) {
        Document document = documentService.getDocumentInfoByRefIdAndRefTable(invoiceId, CommonConstant.SALES_INVOICE_ACKNOWLEDGEMENT);
        return document;
    }

    public List<Long> getInvoiceListByDistributorId(
            Long companyId, Long distributorId,
            LocalDate startDate, LocalDate endDate){
        List<Long> salesInvoiceList =
                salesInvoiceRepository.getInvoiceIdListByCompanyIdAndDistributorId(
                        companyId, distributorId, startDate, endDate);
        return  salesInvoiceList;
    }

    public List<Map<String, Object>> getOrderToCashCycleReport(Long companyId, List<Long> locationIds, List<Long> salesOfficerIds, List<Long> distributorIds, LocalDate startDate, LocalDate endDate, String reportType) {
        List<Map<String, Object>> orderToCashCycleList = new ArrayList<>();
        LocalDate asOnDate = LocalDate.now();

        try {
            if ("SUMMARY".equals(reportType)) {
                orderToCashCycleList = salesInvoiceRepository.getOrderToCashCycleSummaryReport(
                            companyId, locationIds, salesOfficerIds, distributorIds,
                            startDate, endDate);
            } else {
                orderToCashCycleList = salesInvoiceRepository.getOrderToCashCycleDetailsReport(
                                companyId, locationIds, salesOfficerIds, distributorIds,
                                startDate, endDate);
            }



            return orderToCashCycleList;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}


