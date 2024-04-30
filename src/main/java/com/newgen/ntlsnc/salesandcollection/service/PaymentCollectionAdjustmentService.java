package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.AmountUtil;
import com.newgen.ntlsnc.common.CommonConstant;
import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.common.enums.CreditDebitTransactionType;
import com.newgen.ntlsnc.common.enums.NoteType;
import com.newgen.ntlsnc.globalsettings.entity.AccountingYear;
import com.newgen.ntlsnc.globalsettings.entity.Designation;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.OverridingDiscount;
import com.newgen.ntlsnc.globalsettings.service.*;
import com.newgen.ntlsnc.reports.service.CommonReportsService;
import com.newgen.ntlsnc.salesandcollection.dto.paymentAdjustment.AdjustPaymentCollectionDto;
import com.newgen.ntlsnc.salesandcollection.dto.paymentAdjustment.OrdSettlementDto;
import com.newgen.ntlsnc.salesandcollection.dto.paymentAdjustment.SalesInvoiceAdjustedDto;
import com.newgen.ntlsnc.salesandcollection.entity.*;
import com.newgen.ntlsnc.salesandcollection.repository.*;
import com.newgen.ntlsnc.supplychainmanagement.entity.SalesReturn;
import com.newgen.ntlsnc.supplychainmanagement.service.SalesReturnService;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import net.sf.jasperreports.engine.JasperPrint;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author kamal
 * @Date ২১/৪/২২
 */

@Service
public class PaymentCollectionAdjustmentService implements IService<PaymentCollectionAdjustment> {
    @Autowired
    OrganizationService organizationService;
    @Autowired
    PaymentCollectionAdjustmentRepository paymentCollectionAdjustmentRepository;
    @Autowired
    SalesInvoiceService salesInvoiceService;
    @Autowired
    PaymentCollectionService paymentCollectionService;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    OverridingDiscountSetupService overridingDiscountService;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    AccountingYearService accountingYearService;
    @Autowired
    PaymentCollectionRepository paymentCollectionRepository;
    @Autowired
    SalesInvoiceRepository salesInvoiceRepository;
    @Autowired
    DocumentSequenceService documentSequenceService;
    @Autowired
    CreditDebitNoteRepository creditDebitNoteRepository;
    @Autowired
    ReportService reportService;
    @Autowired
    SalesReturnService salesReturnService;
    @Autowired
    SalesReturnProposalService salesReturnProposalService;
    @Autowired
    SalesReturnAdjustmentRepository salesReturnAdjustmentRepository;
    @Autowired
    CommonReportsService commonReportsService;
    @Autowired
    DistributorBalanceRepository distributorBalanceRepository;
    @Autowired
    DistributorBalanceService distributorBalanceService;

    @Transactional
    @Override
    public PaymentCollectionAdjustment create(Object object) {
        try {
            AdjustPaymentCollectionDto adjustPaymentCollectionDto = (AdjustPaymentCollectionDto) object;

            if (adjustPaymentCollectionDto.getAdjustedAmount() <= 0) {
                throw new RuntimeException("Adjusted amount must be greater than 0!");
            }

            if (adjustPaymentCollectionDto.getAdjustedPayment() == null) {
                throw new RuntimeException("Payment not selected!");
            }

            if (adjustPaymentCollectionDto.getAdjustedInvoiceList().size() == 0) {
                throw new RuntimeException("Invoice not selected!");
            }

            PaymentCollection paymentCollection = null;
            PaymentCollectionAdjustment paymentCollectionAdjustment = null;
            List<PaymentCollectionAdjustment> paymentCollectionAdjustmentList = new ArrayList<>();
            List<SalesInvoice> salesInvoiceList = new ArrayList<>();
            List<DistributorBalance> distributorBalanceList = new ArrayList<>();
            SalesReturnAdjustment salesReturnAdjustment = null;
            List<SalesReturnAdjustment> salesReturnAdjustmentList = new ArrayList<>();
            Organization company = organizationService.findById(adjustPaymentCollectionDto.getCompanyId());

            if ("PAYMENT".equals(adjustPaymentCollectionDto.getAdjustedPayment().getAdjustType().trim())) {
                paymentCollection = paymentCollectionService.findById(adjustPaymentCollectionDto.getAdjustedPayment().getId());
                paymentCollection.setRemainingAmount(
                        AmountUtil.round(paymentCollection.getRemainingAmount()
                                - adjustPaymentCollectionDto.getAdjustedAmount(), 4));
            }

            for (SalesInvoiceAdjustedDto salesInvoiceAdjustedDto
                    : adjustPaymentCollectionDto.getAdjustedInvoiceList()) {
                SalesInvoice salesInvoice = null;
                DistributorBalance distributorBalance = null;
                paymentCollectionAdjustment = new PaymentCollectionAdjustment();
                if (salesInvoiceAdjustedDto.getIsOpeningBalance().equals("Y")) {
                    distributorBalance =
                            distributorBalanceService.findById(salesInvoiceAdjustedDto.getId());
                } else {
                    salesInvoice =
                            salesInvoiceService.findById(salesInvoiceAdjustedDto.getId());
                }

                if (salesInvoice != null) {
                    Float remainingAmount = salesInvoice.getRemainingAmount() - salesInvoiceAdjustedDto.getAdjustedAmount();
                    Float ordAmount = salesInvoice.getOrdAmount() + salesInvoiceAdjustedDto.getOrdAmount();
                    salesInvoice.setRemainingAmount(AmountUtil.round(remainingAmount, 4));
                    salesInvoice.setOrdAmount(AmountUtil.round(ordAmount, 4));
                    paymentCollectionAdjustment.setSalesInvoice(salesInvoice);
                    salesInvoiceList.add(salesInvoice);
                }
                if (distributorBalance != null) {
                    distributorBalance.setRemainingBalance(
                            AmountUtil.round(distributorBalance.getRemainingBalance()
                                    - (salesInvoiceAdjustedDto.getAdjustedAmount() + salesInvoiceAdjustedDto.getOrdAmount()), 4));
                    paymentCollectionAdjustment.setDistributorBalance(distributorBalance);
                    distributorBalanceList.add(distributorBalance);
                }

                if ("RETURN".equals(adjustPaymentCollectionDto.getAdjustedPayment().getAdjustType().trim())) {
                    SalesReturn salesReturn =
                            salesReturnService.findById(adjustPaymentCollectionDto.getAdjustedPayment().getId());
                    salesReturnAdjustment = new SalesReturnAdjustment();
                    salesReturnAdjustment.setAdjustedAmount(AmountUtil.round(salesInvoiceAdjustedDto.getAdjustedAmount(), 4));
                    salesReturnAdjustment.setOrdAmount(AmountUtil.round(salesInvoiceAdjustedDto.getOrdAmount(), 4));
                    salesReturnAdjustment.setSalesReturn(salesReturn);
                    salesReturnAdjustment.setSalesInvoice(salesInvoice);
                    salesReturnAdjustment.setMappingDate(LocalDate.now());
                    salesReturnAdjustment.setCompany(company);
                    salesReturnAdjustment.setOrganization(organizationService.getOrganizationFromLoginUser());
                    salesReturnAdjustmentList.add(salesReturnAdjustment);
                } else {
                    paymentCollectionAdjustment.setAdjustedAmount(AmountUtil.round(salesInvoiceAdjustedDto.getAdjustedAmount(), 4));
                    paymentCollectionAdjustment.setOrdAmount(AmountUtil.round(salesInvoiceAdjustedDto.getOrdAmount(), 4));
                    paymentCollectionAdjustment.setPaymentCollection(paymentCollection);
                    paymentCollectionAdjustment.setMappingDate(LocalDate.now());
                    paymentCollectionAdjustment.setCompany(company);
                    paymentCollectionAdjustment.setOrganization(organizationService.getOrganizationFromLoginUser());
                    paymentCollectionAdjustmentList.add(paymentCollectionAdjustment);
                }
            }

            salesInvoiceRepository.saveAll(salesInvoiceList);
            distributorBalanceRepository.saveAll(distributorBalanceList);

            if ("RETURN".equals(adjustPaymentCollectionDto.getAdjustedPayment().getAdjustType().trim())) {
                salesReturnAdjustmentRepository.saveAll(salesReturnAdjustmentList);
            } else {
                paymentCollectionRepository.save(paymentCollection);
                paymentCollectionAdjustmentRepository.saveAll(paymentCollectionAdjustmentList);
            }

            return paymentCollectionAdjustment;

        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Transactional
    public PaymentCollectionAdjustment adjustOrd(Object object) {
        try {
            OrdSettlementDto ordSettlementDto = (OrdSettlementDto) object;

            if (ordSettlementDto.getOrdAmount() <= 0) {
                throw new RuntimeException("ORD Amount must be greater than 0!");
            }

            if (ordSettlementDto.getPaymentCollectionId() == null) {
                throw new RuntimeException("Payment is required!");
            }

            if (ordSettlementDto.getCompanyId() == null) {
                throw new RuntimeException("Company is required!");
            }

            PaymentCollectionAdjustment paymentCollectionAdjustment = null;
            CreditDebitNote creditDebitNote = new CreditDebitNote();
            SalesInvoice salesInvoice = null;
            DistributorBalance distributorBalance = null;

            if (ordSettlementDto.getSalesInvoiceId() != null) {
                salesInvoice = salesInvoiceService.findById(ordSettlementDto.getSalesInvoiceId());
                if (ordSettlementDto.getOrdAmount() > salesInvoice.getRemainingAmount()) {
                    throw new RuntimeException("ORD Amount cannot greater than remaining amount!");
                }

                salesInvoice.setRemainingAmount(
                        AmountUtil.round(salesInvoice.getRemainingAmount() - ordSettlementDto.getOrdAmount(), 4));
                creditDebitNote.setCompany(salesInvoice.getCompany());
                creditDebitNote.setInvoice(salesInvoice);
                creditDebitNote.setDistributor(salesInvoice.getDistributor());
                salesInvoiceRepository.save(salesInvoice);
            } else {
                distributorBalance =
                        distributorBalanceService.findById(ordSettlementDto.getDistributorBalanceId());
                creditDebitNote.setCompany(distributorBalance.getCompany());
                creditDebitNote.setDistributorBalance(distributorBalance);
                creditDebitNote.setDistributor(distributorBalance.getDistributor());
            }

            paymentCollectionAdjustment = this.findById(ordSettlementDto.getId());
            paymentCollectionAdjustment.setIsOrdSettled(Boolean.TRUE);
            creditDebitNote.setOrganization(organizationService.getOrganizationFromLoginUser());
            creditDebitNote.setNoteType(NoteType.CREDIT);
            creditDebitNote.setPaymentCollectionAdjustment(paymentCollectionAdjustment);
            creditDebitNote.setTransactionType(CreditDebitTransactionType.ORD);
            creditDebitNote.setAmount(Double.parseDouble(paymentCollectionAdjustment.getOrdAmount().toString()));
            creditDebitNote.setNote("ORD adjustment");
            creditDebitNote.setReason("For ORD adjustment");
            creditDebitNote.setNoteNo(documentSequenceService.getSequenceByDocumentId(CommonConstant.DOCUMENT_ID_FOR_CREDIT_DEBIT_NOTE));
            creditDebitNote.setProposalDate(LocalDate.now());
            creditDebitNote.setApprovalDate(LocalDateTime.now()); //TODO need to change null when create
            creditDebitNote.setApprovalStatus(ApprovalStatus.APPROVED); //TODO need to change pending when create

            paymentCollectionAdjustmentRepository.save(paymentCollectionAdjustment);
            creditDebitNoteRepository.save(creditDebitNote);
            return paymentCollectionAdjustment;
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Transactional
    @Override
    public PaymentCollectionAdjustment update(Long id, Object object) {
        return null;
    }

    @Transactional
    @Override
    public boolean delete(Long id) {
        Optional<PaymentCollectionAdjustment> optionalPaymentCollectionAdjustment = paymentCollectionAdjustmentRepository.findById(id);
        try {
            if (!optionalPaymentCollectionAdjustment.isPresent()) {
                throw new Exception("Payment Collection Adjustment Not exist");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        PaymentCollectionAdjustment paymentCollectionAdjustment = optionalPaymentCollectionAdjustment.get();
        paymentCollectionAdjustment.setIsDeleted(true);
        paymentCollectionAdjustmentRepository.save(paymentCollectionAdjustment);

        return true;
    }

    @Override
    public PaymentCollectionAdjustment findById(Long id) {
        try {
            Optional<PaymentCollectionAdjustment> optionalPaymentCollectionAdjustment = paymentCollectionAdjustmentRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalPaymentCollectionAdjustment.isPresent()) {
                throw new Exception("Payment collection adjustment record not found!");
            }
            return optionalPaymentCollectionAdjustment.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<PaymentCollectionAdjustment> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return paymentCollectionAdjustmentRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }

    public List<Map> findInvoiceAdjustments(Long companyId, List<Long> invoiceList) {
        return paymentCollectionAdjustmentRepository.findInvoiceAdjustments(companyId, invoiceList);
    }

    public Map<String, Object> getOverridingDiscountStatus(Long paymentId) {
        Map<String, Object> returnMap = new HashMap<>();
        List<Boolean> isOrdAdjusted = new ArrayList<>();
        ;
        String ordAdjustedStatus = "";
        List<Map<String, Object>> ordMapList =
                paymentCollectionAdjustmentRepository.getPaymentAndInvoiceOrdMap(paymentId);

        for (Map<String, Object> map : ordMapList) {
            double remainingAmount = 0f;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();

                if ("remaining_amount".equals(key)) {
                    remainingAmount = Double.parseDouble(entry.getValue().toString());
                }
                if ("ord_days".equals(key)) {
                    Integer value = new Integer(entry.getValue().toString());
                    Boolean isInOrdSlot = isInOrdSlot(value);
                    if (isInOrdSlot && remainingAmount >= 0) {
                        isOrdAdjusted.add(false);
                    }
                }
            }

        }
        if (ordMapList.size() == isOrdAdjusted.size()) {
            ordAdjustedStatus = LIFE_CYCLE_STATUS_PENDING;
        } else if (isOrdAdjusted.contains(false)) {
            ordAdjustedStatus = LIFE_CYCLE_STATUS_INPROGRESS;
        } else
            ordAdjustedStatus = LIFE_CYCLE_STATUS_COMPLETED;

        returnMap.put("ordAdjustedInfo", ordMapList.stream().findFirst());
        returnMap.put("ordAdjustedStatus", ordAdjustedStatus);

        return returnMap;
    }

    public Boolean isInOrdSlot(Integer ordDays) {

        AtomicReference<Boolean> isInOrdSlot = new AtomicReference<>(false);
        List<OverridingDiscount> overridingDiscountSetupList =
                overridingDiscountService.findAll();

        final List<OverridingDiscount> userOption = overridingDiscountSetupList.stream().filter(u -> {
            if (ordDays >= u.getFromDay() && ordDays <= u.getToDay()) {
                isInOrdSlot.set(true);
            }

            return false;

        }).collect(Collectors.toList());

        return isInOrdSlot.get();
    }

    public List<Map<String, Object>> getDistributorWisePaymentCollectionInfoList(Map<String, Object> params) {
        try {
            LocalDate fromDate = null;
            LocalDate toDate = null;
            LocalDate startDate = null;
            LocalDate endDate = null;

            if (params.get("fiscalYear") != null) {
                long id = Long.parseLong(params.get("fiscalYear").toString());
                AccountingYear accountingYear = accountingYearService.findById(id);
                if (accountingYear != null) {
                    fromDate = accountingYear.getStartDate();
                    toDate = accountingYear.getEndDate();
                }
            }
            if (params.get("startDate") != null) {
                startDate = LocalDate.parse(params.get("startDate").toString());
            }
            if (params.get("endDate") != null) {
                endDate = LocalDate.parse(params.get("endDate").toString());
            }

            return paymentCollectionAdjustmentRepository.getDistributorWisePaymentCollectionInfoList(
                    applicationUserService.getOrganizationIdFromLoginUser(),
                    Long.parseLong(params.get("companyId").toString()),
                    params.get("location") != null ? Long.parseLong(params.get("location").toString()) : null,
                    fromDate,
                    toDate,
                    startDate,
                    endDate
            );
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public List<Map<String, Object>> getDistributorWisePaymentCollectionAdjustmentHistory(Map<String, Object> params) {
        try {
            LocalDate fromDate = null;
            LocalDate toDate = null;

            if (params.get("fiscalYear") != null) {
                long id = Long.parseLong(params.get("fiscalYear").toString());
                AccountingYear accountingYear = accountingYearService.findById(id);
                if (accountingYear != null) {
                    fromDate = accountingYear.getStartDate();
                    toDate = accountingYear.getEndDate();
                }
            }

            return paymentCollectionAdjustmentRepository.getDistributorWisePaymentCollectionAdjustmentHistory(
                    applicationUserService.getOrganizationIdFromLoginUser(),
                    Long.parseLong(params.get("companyId").toString()),
                    params.get("location") != null ? Long.parseLong(params.get("location").toString()) : null,
                    fromDate,
                    toDate
            );
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public List<Map<String, Object>> getDistributorWisePaymentCollectionAdjustmentListForOrdSettlement(Map<String, Object> params) {
        try {
            LocalDate fromDate = null;
            LocalDate toDate = null;

            if (params.get("fiscalYear") != null) {
                long id = Long.parseLong(params.get("fiscalYear").toString());
                AccountingYear accountingYear = accountingYearService.findById(id);
                if (accountingYear != null) {
                    fromDate = accountingYear.getStartDate();
                    toDate = accountingYear.getEndDate();
                }
            }

            return paymentCollectionAdjustmentRepository.getDistributorWisePaymentCollectionAdjustmentListForOrdSettlement(
                    applicationUserService.getOrganizationIdFromLoginUser(),
                    Long.parseLong(params.get("companyId").toString()),
                    params.get("location") != null ? Long.parseLong(params.get("location").toString()) : null,
                    fromDate,
                    toDate,
                    params.get("status") == null ? null : params.get("status").toString()
            );
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public List<Map<String, String>> getPaymentListByDistributorId(
            Long distributorId, Long companyId, String fiscalYearId) {
        try {
            LocalDate fromDate = null;
            LocalDate toDate = null;

            if (!fiscalYearId.equals("")) {
                long id = Long.parseLong(fiscalYearId);
                AccountingYear accountingYear = accountingYearService.findById(id);
                if (accountingYear != null) {
                    fromDate = accountingYear.getStartDate();
                    toDate = accountingYear.getEndDate();
                }
            }

            return paymentCollectionService.findPaymentListByDistributorId(distributorId, companyId, fromDate, toDate);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public List<Map<String, Object>> getUnadjustedInvoiceListByDistributorAndPaymentDate(
            Long distributorId, Long companyId, Long paymentId, String adjustType, String fiscalYearId) {
        try {

            LocalDate fromDate = null;
            LocalDate toDate = null;

            if (!fiscalYearId.equals("")) {
                long id = Long.parseLong(fiscalYearId);
                AccountingYear accountingYear = accountingYearService.findById(id);
                if (accountingYear != null) {
                    fromDate = accountingYear.getStartDate();
                    toDate = accountingYear.getEndDate();
                }
            }

            if ("PAYMENT".equals(adjustType)) {
                return salesInvoiceService.getUnadjustedInvoiceListByDistributorAndPaymentDate(
                        applicationUserService.getOrganizationIdFromLoginUser(),
                        companyId, distributorId,
                        paymentCollectionService.findById(paymentId).getPaymentDate(),
                        fromDate, toDate
                );
            } else {
                LocalDate returnDate = salesReturnService.findById(paymentId).getReturnDate().toLocalDate();
                return salesInvoiceService.getUnadjustedInvoiceListByDistributorAndPaymentDate(
                        applicationUserService.getOrganizationIdFromLoginUser(),
                        companyId, distributorId,
                        returnDate,
                        fromDate, toDate
                );
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public List<Map<String, Object>> getUnadjustedOpeningInvoiceListByDistributor(
            Long distributorId, Long companyId, String adjustType, String fiscalYearId) {
        try {

            LocalDate fromDate = null;
            LocalDate toDate = null;

            if (!fiscalYearId.equals("")) {
                long id = Long.parseLong(fiscalYearId);
                AccountingYear accountingYear = accountingYearService.findById(id);
                if (accountingYear != null) {
                    fromDate = accountingYear.getStartDate();
                    toDate = accountingYear.getEndDate();
                }
            }

            return salesInvoiceService.getUnadjustedOpeningInvoiceListByDistributor(
                    companyId, distributorId,
                    fromDate, toDate
            );

        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public JasperPrint getOrderToCashCycleReport(Map<String, Object> params) throws IOException {
        Organization company = organizationService.findById(Long.parseLong(params.get("companyId").toString()));
        params.put("companyName", company.getName() + " (" + company.getShortName() + ")");
        params.put("companyAddress", company.getAddress());
        params.put("companyEmail", company.getEmail());
        params.put("companyWeb", company.getWebAddress());
        params.put("companyLogo", new ByteArrayInputStream(
                organizationService.getOrganizationLogoByteData(company.getId())));

        Map<String, Object> loginUser = applicationUserService.getMe();
        params.put("printedBy", loginUser.get("userName"));
        Object o = loginUser.get("designation");
        params.put("printedByDesignation", ((Designation) o).getName());

        params.put("salesOfficerIds", Arrays.asList(params.get("salesOfficerIds").toString().split(",")));
        params.put("distributorIds", Arrays.asList(params.get("distributorIds").toString().split(",")));
        params.put("locationIds", Arrays.asList(params.get("locationIds").toString().split(",")));
        String jasperFileName = "";
        if (params.get("reportType").toString().equals("DETAILS")) {
            jasperFileName = "OrderToCashCycleInvoice";
        }

        return reportService.getReport(jasperFileName, "/reports/", params);
    }

    public JasperPrint getPerformanceReport(Map<String, Object> params) throws IOException {
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
        if (params.get("startDate").toString().equals("") || params.get("startDate").toString().equals("Invalid date")) {
            params.put("startDate", null);
        } else {
            localStartDate = LocalDate.parse(params.get("startDate").toString(), DateTimeFormatter.ofPattern("yyyy-MM-d"));
            params.put("startDate", params.get("startDate").toString());
        }
        if (params.get("endDate").toString().equals("") || params.get("endDate").toString().equals("Invalid date")) {
            params.put("endDate", null);
        } else {
            localEndDate = LocalDate.parse(params.get("endDate").toString(), DateTimeFormatter.ofPattern("yyyy-MM-d"));
            params.put("endDate", params.get("endDate").toString());
        }
        params.put("dateHeader", commonReportsService.getReportDate(localStartDate, localEndDate, params.get("dateType").toString()));

        params.put("locationIds", Arrays.asList(params.get("locationIds").toString().split(",")));
        params.put("distributorIds", Arrays.asList(params.get("disIds").toString().split(",")));
        params.put("salesOfficerIds", Arrays.asList(params.get("soIds").toString().split(",")));
        params.put("isNational", params.get("nationalLocationChecked").toString().equals("true") ? Boolean.TRUE : Boolean.FALSE);
        params.put("isDistributorExist", !params.get("disIds").toString().equals(""));
        params.put("isSoExist", !params.get("soIds").toString().equals(""));


        // jasper file selection
        String jasperFileName = "";
        if (params.get("reportType").toString().equals("DETAILS")) {
            if (params.get("isWithSum").toString().equals("true")) {
                if (params.get("reportFormat").equals("PDF")) {
                    jasperFileName = "PerformanceReport"; // details with sum report
                } else {
                    jasperFileName = "PerformanceReportExcel"; // details with sum report excel
                }

            } else {
                if (params.get("reportFormat").equals("PDF")) {
                    jasperFileName = "PerformanceReportWithoutSub"; // details without sum report
                } else {
                    jasperFileName = "PerformanceReportWithoutSubExcel"; // details without sum report excel
                }
            }

        } else {
            if (params.get("isDistributorExist").toString().equals("true") && params.get("isSoExist").toString().equals("true")) {
                params.put("reportType", "(Sales Officer And Distributor)");
                jasperFileName = "SoAndDistributorWisePerformanceReport"; //  Distributor So wise
            } else if (params.get("isDistributorExist").toString().equals("true")) {
                params.put("reportType", "(Distributor)");
                jasperFileName = "DistributorWisePerformanceReport"; // Distributor wise
            } else if (params.get("isSoExist").toString().equals("true")) {
                params.put("reportType", "(Sales Officer)");
                jasperFileName = "SoWisePerformanceReport"; // So wise

            }//all wise
            else if (params.get("nationalLocationChecked").toString().equals("true") &&
                    params.get("allChecked").toString().equals("true")) {
                params.put("reportType", "(All Zone)");// national
                jasperFileName = "ZoneWisePerformanceReport"; // zone wise
            } else if (params.get("locationTypeLevel").toString().equals("1") && params.get("allChecked").toString().equals("true")) {
                params.put("reportType", "(All Area)");
                jasperFileName = "AreaWisePerformanceReport"; // Area wise
            } else if (params.get("locationTypeLevel").toString().equals("2") && params.get("allChecked").toString().equals("true")) {
                params.put("reportType", "(All Territory)");
                jasperFileName = "TerritoryWisePerformanceReport"; // Territory wise
            } else if (params.get("locationTypeLevel").toString().equals("3") && params.get("allChecked").toString().equals("true")) {
                params.put("reportType", "(All Sales Officer)");
                jasperFileName = "SoWisePerformanceReport"; // So wise
            }
            // national and location type wise
            else if (params.get("nationalLocationChecked").toString().equals("true")) {
                if (!"".equals(params.get("locationTypeData"))) {
                    if ("Zone".equals(params.get("locationTypeData").toString())) {
                        jasperFileName = "ZoneWisePerformanceReport";
                        params.put("reportType", "(All Zone)");
                    } else if ("Area".equals(params.get("locationTypeData").toString())) {
                        jasperFileName = "AreaWisePerformanceReport";
                        params.put("reportType", "(All Area)");
                    }
                    else if ("Territory".equals(params.get("locationTypeData").toString())) {
                        jasperFileName = "TerritoryWisePerformanceReport";
                        params.put("reportType", "(All Territory)");
                    }
                } else {
                    params.put("reportType", "(National)");// national
                    jasperFileName = "CompanyWisePerformanceReport"; // national wise
                }


            } else if (params.get("locationTypeLevel").toString().equals("1")) {
                params.put("reportType", "(Zone)");
                jasperFileName = "ZoneWisePerformanceReport"; // zone wise
            } else if (params.get("locationTypeLevel").toString().equals("2")) {
                params.put("reportType", "(Area)");
                jasperFileName = "AreaWisePerformanceReport"; // Area wise
            } else if (params.get("locationTypeLevel").toString().equals("3")) {
                params.put("reportType", "(Territory)");
                jasperFileName = "TerritoryWisePerformanceReport"; // Territory wise
            }
        }
        return reportService.getReport(jasperFileName, "/reports/performanceReport/", params);
    }

}
