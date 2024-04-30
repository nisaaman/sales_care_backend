package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.AmountUtil;
import com.newgen.ntlsnc.common.CommonConstant;
import com.newgen.ntlsnc.common.FileUploadService;
import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.common.enums.CreditDebitTransactionType;
import com.newgen.ntlsnc.common.enums.FileType;
import com.newgen.ntlsnc.common.enums.NoteType;
import com.newgen.ntlsnc.globalsettings.entity.*;
import com.newgen.ntlsnc.globalsettings.service.*;
import com.newgen.ntlsnc.reports.service.CommonReportsService;
import com.newgen.ntlsnc.salesandcollection.dto.CreditDebitNoteDto;
import com.newgen.ntlsnc.salesandcollection.entity.CreditDebitNote;
import com.newgen.ntlsnc.salesandcollection.entity.SalesInvoice;
import com.newgen.ntlsnc.salesandcollection.repository.CreditDebitNoteRepository;
import com.newgen.ntlsnc.supplychainmanagement.entity.SalesReturn;
import com.newgen.ntlsnc.supplychainmanagement.service.SalesReturnService;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author anika
 * @Date ৩০/৫/২২
 */
@Service
public class CreditDebitNoteService implements IService<CreditDebitNote> {

    @Autowired
    OrganizationService organizationService;
    @Autowired
    DistributorService distributorService;
    @Autowired
    SalesInvoiceService salesInvoiceService;
    @Autowired
    CreditDebitNoteRepository creditDebitNoteRepository;
    @Autowired
    FileUploadService fileUploadService;
    @Autowired
    DocumentService documentService;
    @Autowired
    DocumentSequenceService documentSequenceService;
    @Autowired
    EnumeService enumeService;
    @Autowired
    AccountingYearService accountingYearService;
    @Autowired
    LocationService locationService;
    @Autowired
    ReportService reportService;
    @Autowired
    SalesReturnService salesReturnService;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    CommonReportsService commonReportsService;
    @Autowired
    DistributorBalanceService distributorBalanceService;
    @Override
    public CreditDebitNote create(Object object) {
        return null;
    }

    @Transactional
    public CreditDebitNote createWithFile(CreditDebitNoteDto creditDebitNoteDto, MultipartFile[] creditDebitNoteFileList) {
        try {
            CreditDebitNote creditDebitNote = new CreditDebitNote();
            creditDebitNote.setProposalDate(LocalDate.parse(creditDebitNoteDto.getProposalDate()));  //yyyy-MM-dd
            creditDebitNote.setAmount(creditDebitNoteDto.getAmount());
            creditDebitNote.setNoteType(NoteType.valueOf(creditDebitNoteDto.getNoteType()));
            creditDebitNote.setTransactionType(CreditDebitTransactionType.valueOf(creditDebitNoteDto.getTransactionType()));
            creditDebitNote.setApprovalStatus(ApprovalStatus.PENDING);
            creditDebitNote.setReason(creditDebitNoteDto.getReason().trim());
            creditDebitNote.setNote(creditDebitNoteDto.getNote());
            creditDebitNote.setApprovalDate(LocalDateTime.now());
            creditDebitNote.setNoteNo(documentSequenceService.getSequenceByDocumentId(CommonConstant.DOCUMENT_ID_FOR_CREDIT_DEBIT_NOTE));

            // For debit nature invoice is optional
            if (creditDebitNoteDto.getIsOpeningBalance().equals("N")
                    && creditDebitNoteDto.getInvoiceId() != null) {
                creditDebitNote.setInvoice(salesInvoiceService.findById(creditDebitNoteDto.getInvoiceId()));
            }
            else {
                creditDebitNote.setDistributorBalance(distributorBalanceService.
                        findById(creditDebitNoteDto.getInvoiceId()));
            }

            creditDebitNote.setDistributor(distributorService.findById(creditDebitNoteDto.getDistributorId()));
            creditDebitNote.setCompany(organizationService.findById(creditDebitNoteDto.getCompanyId()));
            creditDebitNote.setOrganization(organizationService.getOrganizationFromLoginUser());
            creditDebitNote = creditDebitNoteRepository.save(creditDebitNote);

            if (creditDebitNoteFileList != null) {
                for (MultipartFile creditDebitNoteFile : creditDebitNoteFileList) {
                    String filePath = fileUploadService.fileUpload(creditDebitNoteFile, FileType.DOCUMENT.getCode(),
                            CreditDebitNote.class.getSimpleName(), creditDebitNoteDto.getCompanyId(),
                            creditDebitNoteDto.getCompanyId());
                    documentService.save(CreditDebitNote.class.getSimpleName(), filePath, creditDebitNote.getId(),
                            fileUploadService.getFileNameFromFilePath(filePath), FileType.DOCUMENT.getCode(),
                            creditDebitNote.getOrganization(), creditDebitNoteDto.getCompanyId(),
                            creditDebitNoteFile.getSize());
                }
            }

            return creditDebitNote;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    //this method is used when return ord amount from sales return create operation
    @Transactional
    public CreditDebitNote createForOrdFromReturn(SalesReturn salesReturn, SalesInvoice salesInvoice, Float returnOrdAmount) {
        try {
            CreditDebitNote creditDebitNote = new CreditDebitNote();
            creditDebitNote.setProposalDate(LocalDate.now());
            creditDebitNote.setAmount(AmountUtil.round(returnOrdAmount.doubleValue(), 4));
            creditDebitNote.setNoteType(NoteType.DEBIT);
            creditDebitNote.setTransactionType(CreditDebitTransactionType.SALES_RETURN);
            creditDebitNote.setApprovalStatus(ApprovalStatus.APPROVED);
            creditDebitNote.setReason("Ord deducted for Sales Return:" + salesReturn.getReturnNo() + " of Invoice:" + salesInvoice.getInvoiceNo());
            creditDebitNote.setNote("Ord deducted for Sales Return:" + salesReturn.getReturnNo() + " of Invoice:" + salesInvoice.getInvoiceNo());
            creditDebitNote.setApprovalDate(LocalDateTime.now());
            creditDebitNote.setNoteNo(documentSequenceService.getSequenceByDocumentId(CommonConstant.DOCUMENT_ID_FOR_CREDIT_DEBIT_NOTE));
            creditDebitNote.setInvoice(salesInvoice);
            creditDebitNote.setDistributor(salesInvoice.getDistributor());
            creditDebitNote.setCompany(salesInvoice.getCompany());
            creditDebitNote.setOrganization(salesInvoice.getOrganization());
            creditDebitNote = creditDebitNoteRepository.save(creditDebitNote);

            return creditDebitNote;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    //this method is used when return ord amount from sales return create operation
    /*@Transactional
    public CreditDebitNote createForSpecialDiscountOfInvoiceFromReturn(SalesReturn salesReturn, SalesInvoice salesInvoice) {
        try {
            CreditDebitNote creditDebitNote = new CreditDebitNote();
            creditDebitNote.setProposalDate(LocalDate.now());
            creditDebitNote.setAmount(AmountUtil.round(salesInvoice.getInvoiceDiscount().doubleValue(), 4));
            creditDebitNote.setNoteType(NoteType.DEBIT);
            creditDebitNote.setTransactionType(CreditDebitTransactionType.SALES_RETURN);
            creditDebitNote.setApprovalStatus(ApprovalStatus.APPROVED);
            creditDebitNote.setReason("Special Discount deducted for Sales Return:" + salesReturn.getReturnNo() + " of Invoice:" + salesInvoice.getInvoiceNo());
            creditDebitNote.setNote("Special Discount deducted for Sales Return:" + salesReturn.getReturnNo() + " of Invoice:" + salesInvoice.getInvoiceNo());
            creditDebitNote.setApprovalDate(LocalDateTime.now());
            creditDebitNote.setNoteNo(documentSequenceService.getSequenceByDocumentId(CommonConstant.DOCUMENT_ID_FOR_CREDIT_DEBIT_NOTE));
            creditDebitNote.setInvoice(salesInvoice);
            creditDebitNote.setDistributor(salesInvoice.getDistributor());
            creditDebitNote.setCompany(salesInvoice.getCompany());
            creditDebitNote.setOrganization(salesInvoice.getOrganization());
            creditDebitNote = creditDebitNoteRepository.save(creditDebitNote);

            return creditDebitNote;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }*/

    public CreditDebitNote createForSpecialDiscountOfInvoiceFromReturn(
            Float returnAmount, SalesReturn salesReturn, SalesInvoice salesInvoice) {
        try {
            CreditDebitNote creditDebitNote = new CreditDebitNote();
            creditDebitNote.setProposalDate(LocalDate.now());
            Float invoiceDiscount = salesInvoice.getInvoiceDiscount();
            Map returnQuantityMap = salesReturnService.getSalesQuantityAndReturnQuantity(salesInvoice.getId());
            Double returnQuantity = Double.parseDouble(returnQuantityMap.get("return_quantity").toString());
            Double saleQuantity = Double.parseDouble(returnQuantityMap.get("sales_quantity").toString());

            Double returnAmounts = (invoiceDiscount/saleQuantity) * returnQuantity;
            //Double returnAmounts = (double) (discountAmount * returnAmount / salesInvoice.getInvoiceAmount());
            creditDebitNote.setAmount(AmountUtil.round(returnAmounts, 4));
            //creditDebitNote.setAmount(AmountUtil.round(debitAmount.doubleValue(), 4));
            creditDebitNote.setNoteType(NoteType.DEBIT);
            creditDebitNote.setTransactionType(CreditDebitTransactionType.SALES_RETURN);
            creditDebitNote.setApprovalStatus(ApprovalStatus.APPROVED);
            creditDebitNote.setReason("Special Discount deducted for Sales Return:" + salesReturn.getReturnNo() + " of Invoice:" + salesInvoice.getInvoiceNo());
            creditDebitNote.setNote("Special Discount deducted for Sales Return:" + salesReturn.getReturnNo() + " of Invoice:" + salesInvoice.getInvoiceNo());
            creditDebitNote.setApprovalDate(LocalDateTime.now());
            creditDebitNote.setNoteNo(documentSequenceService.getSequenceByDocumentId(CommonConstant.DOCUMENT_ID_FOR_CREDIT_DEBIT_NOTE));
            creditDebitNote.setInvoice(salesInvoice);
            creditDebitNote.setDistributor(salesInvoice.getDistributor());
            creditDebitNote.setCompany(salesInvoice.getCompany());
            creditDebitNote.setOrganization(salesInvoice.getOrganization());
            creditDebitNote = creditDebitNoteRepository.save(creditDebitNote);

            return creditDebitNote;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    public CreditDebitNote update(Long id, Object object) {
        return null;
    }

    @Override
    public boolean delete(Long id) {
        return true;
    }

    @Override
    public CreditDebitNote findById(Long id) {
        try {
            Optional<CreditDebitNote> optionalCreditDebitNote = creditDebitNoteRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalCreditDebitNote.isPresent()) {
                throw new Exception("Credit Debit Note Not exist with id " + id);
            }
            return optionalCreditDebitNote.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<CreditDebitNote> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return creditDebitNoteRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }

    public List<Map> getAllByCompanyAndDistributorAndDateRange(Long companyId, Long distributorId, String fromDate, String toDate) {
        List<Map> mapList = creditDebitNoteRepository.getAllByCompanyAndDistributorAndDateRange(companyId, distributorId, fromDate, toDate);
        return mapList;
    }

    public Map getDebitCreditNoteApprovalPageAllFilterList(Long companyId) {
        Map response = new HashMap();
        List<Map> noteTypeList = enumeService.getAllNoteType();
        List<Map> approvalStatusList = enumeService.findAllApprovalStatus();
        List<Map<String, Object>> accountingYearList = accountingYearService.getAllByCompanyId(companyId);
        List<Location> locationList = locationService.findAllTerritoryByCompanyId(companyId);

        response.put("noteTypeList", noteTypeList);
        response.put("approvalStatusList", approvalStatusList);
        response.put("accountingYearList", accountingYearList);
        response.put("locationList", locationList);
        return response;
    }

    public List<Map> getAllWithDistributorBalanceByCompanyAndNoteTypeAndApprovalStatusAndLocationAndDateRange(Long companyId, Long locationId, String noteType, String status, String fromDate, String toDate) {
        List<Map> mapList = creditDebitNoteRepository.getAllWithDistributorBalanceByCompanyAndNoteTypeAndApprovalStatusAndLocationAndDateRange(companyId, locationId, noteType, status, fromDate, toDate);
        return mapList;
    }

    // if any parameter is null is mean that all. but company is mandatory
    public List<Map> getAllWithDistributorBalanceByCompanyAndNoteTypeAndApprovalStatusAndLocationAndAccountingYear(Long companyId, Long locationId, String noteType, String status, Long accountingYearId) {
        if (status.equals("")) {  // ""= All
            status = null;
        }
        if (noteType.equals("")) {    // ""= All
            noteType = null;
        }
        String fromDate = null;
        String toDate = null;
        List<Map> mapList;
        if (accountingYearId != null) { // null = All
            AccountingYear accountingYear = accountingYearService.findById(accountingYearId);
            fromDate = accountingYear.getStartDate().toString();
            toDate = accountingYear.getEndDate().toString();
        }

        mapList = getAllWithDistributorBalanceByCompanyAndNoteTypeAndApprovalStatusAndLocationAndDateRange(companyId, locationId, noteType, status, fromDate, toDate);
        return mapList;
    }

    public Document getCreditDebitDocumentInfoByRefId(Long creditDebitNoteId) {
        Document document = documentService.getDocumentInfoByRefIdAndRefTable(creditDebitNoteId, CreditDebitNote.class.getSimpleName());
        return document;
    }

    public List<Map<String, Object>> getPendingListForApproval(
            Long companyId, String approvalActor, String approvalStatus,
            Integer level, Long approvalStepId, Long multiLayerApprovalPathId, Long approvalActorId,
            List<Long> distributorList, String approvalStepName) {
        List<Map<String, Object>> pendingList = new ArrayList<Map<String, Object>>();
        creditDebitNoteRepository.getPendingListForApproval(companyId, approvalStatus,
                approvalActor, level, approvalStepId, multiLayerApprovalPathId,
                approvalActorId, distributorList, ApprovalStatus.PENDING.getName(), approvalStepName).forEach(map -> {
            Map<String, Object> pendingApproval = new HashMap<String, Object>(map);
            pendingApproval.put("noteType", NoteType.valueOf(map.get("noteType").toString()).getName());
            pendingApproval.put("transactionType", CreditDebitTransactionType.valueOf(map.get("transactionType").toString()).getName());
            pendingList.add(pendingApproval);
        });
        return pendingList;
    }

    @Transactional
    public void updateApprovalStatus(Long id, ApprovalStatus approvalStatus) {
        try {
            Optional<CreditDebitNote> creditDebitNoteOptional = creditDebitNoteRepository.findById(id);
            if (!creditDebitNoteOptional.isPresent()) {
                return;
            }
            CreditDebitNote creditDebitNote = creditDebitNoteOptional.get();

            if (creditDebitNote.getTransactionType() == CreditDebitTransactionType.DISCOUNT && approvalStatus == ApprovalStatus.APPROVED) {
                SalesInvoice salesInvoice = salesInvoiceService.deductInvoiceRemainingAmount(creditDebitNote.getInvoice(), creditDebitNote.getAmount().floatValue());
                creditDebitNote.setInvoice(salesInvoice);
            }

            creditDebitNote.setApprovalStatus(approvalStatus);
            creditDebitNote.setApprovalDate(LocalDateTime.now());
            creditDebitNoteRepository.save(creditDebitNote);
        } catch (Exception e) {
            throw new RuntimeException("Credit/Debit note approval process can't be Executed. Something went wrong!");
        }
    }

    public JasperPrint getDebitCreditNoteReport(Map<String, Object> params) throws IOException {

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
        params.put("distributorIds", Arrays.asList(params.get("disIds").toString().split(",")));
        params.put("salesOfficerIds", Arrays.asList(params.get("soIds").toString().split(",")));
        params.put("isNational", params.get("nationalLocationChecked").toString().equals("true") ? Boolean.TRUE : Boolean.FALSE);
        params.put("isDistributorExist", !params.get("disIds").toString().equals(""));
        params.put("isSoExist", !params.get("soIds").toString().equals(""));


        // jasper file selection
        String jasperFileName = "";
        if (params.get("reportType").toString().equals("DETAILS")) {
            if (params.get("isWithSum").toString().equals("true")) {
                if (params.get("reportFormat").equals("PDF")){
                    jasperFileName = "DebitCreditNoteDistributorWiseReport"; // details with sum report
                } else {
                    jasperFileName = "DebitCreditNoteDistributorWiseXmlReport"; // details with sum report excel
                }

            } else {
                if (params.get("reportFormat").equals("PDF")){
                    jasperFileName = "debitCreditNote"; // details without sum report
                } else {
                    jasperFileName = "debitCreditNoteXml"; // details without sum report excel
                }

            }
        } else {
            if (params.get("isDistributorExist").toString().equals("true") && params.get("isSoExist").toString().equals("true")){
                params.put("reportType", "Sales Officer And Distributor");
                jasperFileName = "debitCreditNoteSummaryReportBySoAndDistributor"; //  Distributor So wise
            } else if (params.get("isDistributorExist").toString().equals("true")) {
                params.put("reportType", "Distributor");
                jasperFileName = "debitCreditNoteSummaryReportByDistributor"; // Distributor wise
            } else if (params.get("isSoExist").toString().equals("true")) {
                params.put("reportType", "Sales Officer");
                jasperFileName = "debitCreditNoteSummaryReportBySo"; // So wise

            }
            //type wise
            else if (params.get("nationalLocationChecked").toString().equals("true") &&
                    params.get("locationTypeData").toString().equals("Zone")) {
                params.put("reportType", "All Zone");// national
                jasperFileName = "debitCreditNoteSummaryReportByZone"; // zone wise
            } else if (params.get("nationalLocationChecked").toString().equals("true") &&
                    params.get("locationTypeData").toString().equals("Area")) {
                params.put("reportType", "All Area");
                jasperFileName = "debitCreditNoteSummaryReportByArea"; // Area wise
            } else if (params.get("nationalLocationChecked").toString().equals("true") &&
                    params.get("locationTypeData").toString().equals("Territory")) {
                params.put("reportType", "All Territory");
                jasperFileName = "debitCreditNoteSummaryReportByTerritory"; // Territory wise
            }
            //
            //all wise
            else if (params.get("nationalLocationChecked").toString().equals("true") &&
                    params.get("allChecked").toString().equals("true")) {
                params.put("reportType", "All Zone");// national
                jasperFileName = "debitCreditNoteSummaryReportByZone"; // zone wise
            } else if (params.get("locationTypeLevel").toString().equals("1") && params.get("allChecked").toString().equals("true")) {
                params.put("reportType", "All Area");
                jasperFileName = "debitCreditNoteSummaryReportByArea"; // Area wise
            } else if (params.get("locationTypeLevel").toString().equals("2") && params.get("allChecked").toString().equals("true")) {
                params.put("reportType", "All Territory");
                jasperFileName = "debitCreditNoteSummaryReportByTerritory"; // Territory wise
            } else if (params.get("locationTypeLevel").toString().equals("3") && params.get("allChecked").toString().equals("true")) {
                params.put("reportType", "All Sales Officer");
                jasperFileName = "debitCreditNoteSummaryReportBySo"; // So wise
            }
            //
            else if (params.get("nationalLocationChecked").toString().equals("true")) {
                params.put("reportType", "National");// national
                jasperFileName = "debitCreditNoteSummaryReportByNational"; // national wise

            } else if ( params.get("locationTypeLevel").toString().equals("1")) {
                params.put("reportType", "Zone");
                jasperFileName = "debitCreditNoteSummaryReportByZone"; // zone wise
            }
            else if (params.get("locationTypeLevel").toString().equals("2")) {
                params.put("reportType", "Area");
                jasperFileName = "debitCreditNoteSummaryReportByArea"; // Area wise
            }
            else if (params.get("locationTypeLevel").toString().equals("3")) {
                params.put("reportType", "Territory");
                jasperFileName = "debitCreditNoteSummaryReportByTerritory"; // Territory wise
            }
        }
        return reportService.getReport(jasperFileName, "/reports/debitcreditnote/", params);
    }

}
