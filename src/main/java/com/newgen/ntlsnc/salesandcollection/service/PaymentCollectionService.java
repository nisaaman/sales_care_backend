package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.CommonConstant;
import com.newgen.ntlsnc.common.DateUtil;
import com.newgen.ntlsnc.common.FileUploadService;
import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.common.enums.FileType;
import com.newgen.ntlsnc.common.enums.PaymentNature;
import com.newgen.ntlsnc.common.enums.PaymentType;
import com.newgen.ntlsnc.globalsettings.entity.*;
import com.newgen.ntlsnc.globalsettings.repository.LocationRepository;
import com.newgen.ntlsnc.globalsettings.service.*;
import com.newgen.ntlsnc.notification.pushNotification.dto.PushNotificationRequest;
import com.newgen.ntlsnc.notification.pushNotification.service.PushNotificationService;
import com.newgen.ntlsnc.reports.service.CommonReportsService;
import com.newgen.ntlsnc.salesandcollection.dto.PaymentCollectionDto;
import com.newgen.ntlsnc.salesandcollection.entity.Distributor;
import com.newgen.ntlsnc.salesandcollection.entity.PaymentCollection;
import com.newgen.ntlsnc.salesandcollection.entity.PaymentCollectionAdjustment;
import com.newgen.ntlsnc.salesandcollection.repository.PaymentCollectionRepository;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author kamal
 * @Date ২০/৪/২২
 */

@Service
public class PaymentCollectionService implements IService<PaymentCollection> {
    @Autowired
    OrganizationService organizationService;
    @Autowired
    PaymentCollectionRepository paymentCollectionRepository;
    @Autowired
    DistributorService distributorService;
    @Autowired
    PaymentBookService paymentBookService;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    AccountingYearService accountingYearService;
    @Autowired
    SemesterService semesterService;
    @Autowired
    LocationService locationService;
    @Autowired
    FileUploadService fileUploadService;
    @Autowired
    SalesBookingService salesBookingService;
    @Autowired
    BankBranchService bankBranchService;
    @Autowired
    DocumentService documentService;
    @Autowired
    DocumentSequenceService documentSequenceService;
    @Autowired
    PushNotificationService pushNotificationService;
    @Autowired
    CommonReportsService commonReportsService;
    @Autowired
    LocationRepository locationRepository;
    private static final Logger logger = LoggerFactory.getLogger(SalesInvoiceService.class);

    @Override
    @Transactional
    public PaymentCollection create(Object object) {
        try {
            PaymentCollectionDto paymentCollectionDto = (PaymentCollectionDto) object;
            PaymentCollection paymentCollection = new PaymentCollection();

            Organization organization = organizationService.getOrganizationFromLoginUser();
            paymentCollection.setOrganization(organization);

            if (paymentCollectionDto.getCompanyId() == null) {
                throw new RuntimeException("Company not found.");
            }

            paymentCollection.setCompany(organizationService.findById(paymentCollectionDto.getCompanyId()));

            paymentCollection.setPaymentNo(documentSequenceService.getSequenceByDocumentId(CommonConstant.DOCUMENT_ID_FOR_PAYMENT_NO));
            paymentCollection.setPaymentNature(PaymentNature.valueOf(paymentCollectionDto.getPaymentNature()));
            paymentCollection.setPaymentType(PaymentType.valueOf(paymentCollectionDto.getPaymentType()));
            if(paymentCollectionDto.getPaymentDate() == null){
                paymentCollection.setPaymentDate(LocalDate.now());
            }else {
                paymentCollection.setPaymentDate(LocalDate.parse(paymentCollectionDto.getPaymentDate()));
            }
            paymentCollection.setCollectionAmount(paymentCollectionDto.getCollectionAmount());
            paymentCollection.setRemarks(paymentCollectionDto.getRemarks());
            paymentCollection.setRemainingAmount(paymentCollectionDto.getCollectionAmount());
            if(paymentCollectionDto.getApprovalStatus() == null){
                paymentCollection.setApprovalStatus(ApprovalStatus.valueOf("PENDING"));
            }else {
                paymentCollection.setApprovalStatus(ApprovalStatus.valueOf(paymentCollectionDto.getApprovalStatus()));
            }
            paymentCollection.setApprovalStatusForAuthorization(ApprovalStatus.APPROVED);

            if (paymentCollectionDto.getPaymentBookId() == null) {
                throw new RuntimeException("Payment book not found.");
            }

            paymentCollection.setPaymentBook(paymentBookService.findById(paymentCollectionDto.getPaymentBookId()));

            Optional<PaymentCollection> paymentCollectionByMoneyReceiptNo = paymentCollectionRepository.findByOrganizationAndCompanyIdAndPaymentBookIdAndMoneyReceiptNoAndIsDeletedFalse(organization, paymentCollectionDto.getCompanyId(), paymentCollectionDto.getPaymentBookId(), paymentCollectionDto.getMoneyReceiptNo());

            if (paymentCollectionByMoneyReceiptNo.isPresent()) {
                throw new RuntimeException("Duplicate Money Receipt No.");
            }

            paymentCollection.setMoneyReceiptNo(paymentCollectionDto.getMoneyReceiptNo());

            if (paymentCollectionDto.getReferenceNo() != null)
                paymentCollection.setReferenceNo(paymentCollectionDto.getReferenceNo());

            paymentCollection.setCollectionBy(applicationUserService.getApplicationUserFromLoginUser());

            if (paymentCollectionDto.getDistributorId() != null) {
                paymentCollection.setDistributor(distributorService.findById(paymentCollectionDto.getDistributorId()));
            }

            if (paymentCollectionDto.getSalesBookingId() != null) {
                paymentCollection.setSalesBooking(salesBookingService.findById(
                        paymentCollectionDto.getSalesBookingId()));
            }

            if (paymentCollectionDto.getBankBranchId() != null) {
                BankBranch bankBranch = bankBranchService.findById(paymentCollectionDto.getBankBranchId());
                paymentCollection.setBankBranch(bankBranch);
            }

            if(paymentCollection.getPaymentNature().equals(PaymentNature.ADVANCE) && paymentCollectionDto.getApprovalStatusForAuthorization() == null){
                paymentCollection.setApprovalStatusForAuthorization(ApprovalStatus.valueOf("PENDING"));
            }

            paymentCollectionRepository.save(paymentCollection);

            if (paymentCollectionDto.getFile() != null) {
                String filePath = fileUploadService.fileUpload(paymentCollectionDto.getFile(), FileType.DOCUMENT.getCode(),
                        "PaymentCollection",
                        organization.getId(), paymentCollectionDto.getCompanyId());

                documentService.save("PaymentCollection", filePath, paymentCollection.getId(),
                        fileUploadService.getFileNameFromFilePath(filePath), FileType.DOCUMENT.getCode()
                        , organization, paymentCollectionDto.getCompanyId(), paymentCollectionDto.getFile().getSize());
            }

            return paymentCollection;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    @Override
    public PaymentCollection update(Long id, Object object) {
        PaymentCollectionDto paymentCollectionDto = (PaymentCollectionDto) object;
        Optional<PaymentCollection> optionalPaymentCollection = paymentCollectionRepository.findById(paymentCollectionDto.getId());
        if (!optionalPaymentCollection.isPresent()) {
            return null;
        }
        PaymentCollection paymentCollection = optionalPaymentCollection.get();
        paymentCollection.setReferenceNo(paymentCollectionDto.getReferenceNo());
        paymentCollection.setPaymentNature(PaymentNature.valueOf(paymentCollectionDto.getPaymentNature()));
        paymentCollection.setPaymentType(PaymentType.valueOf(paymentCollectionDto.getPaymentType()));
        paymentCollection.setPaymentDate(LocalDate.parse(paymentCollectionDto.getPaymentDate()));
        paymentCollection.setCollectionAmount(paymentCollectionDto.getCollectionAmount());
        paymentCollection.setMoneyReceiptNo(paymentCollectionDto.getMoneyReceiptNo());
        paymentCollection.setRejectReason(paymentCollectionDto.getRejectReason());
        paymentCollection.setActionTakenDate(LocalDateTime.parse(paymentCollectionDto.getActionTakenDate()));
        paymentCollection.setRemarks(paymentCollectionDto.getRemarks());
        paymentCollection.setApprovalStatus(ApprovalStatus.valueOf(paymentCollectionDto.getApprovalStatus()));
        paymentCollection.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (paymentCollectionDto.getPaymentBookId() != null) {
            paymentCollection.setPaymentBook(paymentBookService.findById(paymentCollectionDto.getPaymentBookId()));
        }

        if (paymentCollectionDto.getDistributorId() != null) {
            Distributor distributor = distributorService.findById(paymentCollectionDto.getDistributorId());
            paymentCollection.setDistributor(distributor);
        }
        if (paymentCollectionDto.getCompanyId() != null) {
            paymentCollection.setCompany(organizationService.findById(paymentCollectionDto.getCompanyId()));
        }
        return paymentCollectionRepository.save(paymentCollection);
    }

    @Transactional
    @Override
    public boolean delete(Long id) {
        try {
            Optional<PaymentCollection> optionalPaymentCollection = paymentCollectionRepository.findById(id);
            if (!optionalPaymentCollection.isPresent()) {
                throw new Exception("Payment Collection Not exist");
            }
            PaymentCollection paymentCollection = optionalPaymentCollection.get();
            paymentCollection.setIsDeleted(true);
            paymentCollectionRepository.save(paymentCollection);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public PaymentCollection findById(Long id) {
        try {
            Optional<PaymentCollection> optionalPaymentCollection = paymentCollectionRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalPaymentCollection.isPresent()) {
                throw new Exception("Payment Collection Not exist with id " + id);
            }
            return optionalPaymentCollection.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<PaymentCollection> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return paymentCollectionRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }


    public List<String> getPaymentType() {

        return Stream.of(PaymentType.values())
                .map(PaymentType::getCode)
                .collect(Collectors.toList());

    }

    public List<String> getPaymentNature() {

        return Stream.of(PaymentNature.values())
                .map(PaymentNature::getCode)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getPaymentList(
            Long companyId, Long accountingYearId,
            Long semesterId, Long locationId, String paymentCollectionStatus) {

        Map<String, Object> returnMap = new HashMap<>();
        Map<Long, Object> childLocationMap = new HashMap<>();
        List<Long> soList = new ArrayList<>();
        LocalDate startDateTime = null;
        LocalDate endDateTime = null;
        Long userLoginId = applicationUserService.getApplicationUserIdFromLoginUser();
        if (companyId == null) {
            return null;
        }
        if (accountingYearId != null) {
            Map<String, LocalDate> dateMap =
                    accountingYearService.getAccountingYearDate(accountingYearId);
            if (dateMap != null) {
                LocalDate startDate = dateMap.get("startDate");
                LocalDate endDate = dateMap.get("endDate");
                startDateTime = LocalDate.from(startDate.atStartOfDay());
                endDateTime = LocalDate.from(endDate.atStartOfDay());
            }
        }

        if (semesterId != null) {
            Map<String, LocalDate> dateMap =
                    semesterService.getSemesterDate(semesterId);
            if (dateMap != null) {
                LocalDate startDate = dateMap.get("startDate");
                LocalDate endDate = dateMap.get("endDate");
                startDateTime = LocalDate.from(startDate.atStartOfDay());
                endDateTime = LocalDate.from(endDate.atStartOfDay());
            }
        }

        if (locationId != null) {
            childLocationMap =
                    locationService.getChildLocationsByParent(
                            companyId, locationId, childLocationMap);
            soList = locationService.getSoListByLocation(companyId, childLocationMap);

        } else {
            Boolean isManager =
                    applicationUserService.checkLoginUserIsManager(companyId, userLoginId);
            Boolean isSo =
                    applicationUserService.checkLoginUserIsSo(companyId, userLoginId);
            if (isManager) {
                childLocationMap =
                        locationService.getChildLocationsByParent(
                                companyId, locationId, childLocationMap);
                soList = locationService.getSoListByLocation(companyId, childLocationMap);

            } else if (isSo) {
                soList.add(userLoginId);

            } else {
                List<Location> parentLocations =
                        locationService.getParentLocationsList(companyId);
                for (Location parentLocation : parentLocations) {
                    Map<Long, Object> childLocationMap1 =
                            locationService.getChildLocationsByParent(companyId, parentLocation.getId(),
                                    childLocationMap);
                    childLocationMap.putAll(childLocationMap1);
                }
                soList = locationService.getSoListByLocation(companyId, childLocationMap);
            }
        }

        List<PaymentCollection> paymentList = paymentCollectionRepository.getPaymentList(companyId,
                startDateTime, endDateTime, soList, paymentCollectionStatus == null ? null : ApprovalStatus.valueOf(paymentCollectionStatus));

        double collectionAmount = paymentList.stream()
                .filter(payment -> payment.getApprovalStatus().getCode().equals("APPROVED"))
                .mapToDouble(PaymentCollection::getCollectionAmount)
                .sum();

        returnMap.put("collectionAmount", collectionAmount);
        returnMap.put("paymentList", paymentList);

        return returnMap;
    }


    public Double getCollectionAmount(Long paymentId) {

        PaymentCollection paymentCollection = findById(paymentId);

        if (paymentCollection != null)
            return paymentCollection.getCollectionAmount();

        else
            return null;
    }


    public Map getPaymentAdjustment(Long paymentId) {

        Map adjustment =
                paymentCollectionRepository.getPaymentAdjustment(paymentId);

        return adjustment;
    }

    public List<PaymentCollectionAdjustment> getAdjustedList(Long paymentId) {

        List<PaymentCollectionAdjustment> adjustmentList =
                paymentCollectionRepository.getPaymentAdjustmentList(paymentId);

        return adjustmentList;
    }

    public Double getAdjustedAmount(Long paymentId) {

        List<PaymentCollectionAdjustment> adjustmentList =
                getAdjustedList(paymentId);

        double adjustedAmount = adjustmentList.stream()
                .mapToDouble(PaymentCollectionAdjustment::getAdjustedAmount)
                .sum();

        return adjustedAmount;
    }

    public Map<String, Object> getPaymentAdjustmentStatus(Long paymentId) {

        Map<String, Object> returnMap = new HashMap<>();
        Map firstElement = null;
        ApplicationUser applicationUser = null;
        String paymentAdjustmentStatus = "";
        double adjustedAmount = getAdjustedAmount(paymentId);
        double collectionAmount = getCollectionAmount(paymentId);
        List<PaymentCollectionAdjustment> adjustmentList =
                getAdjustedList(paymentId);

        if (adjustedAmount == 0) {
            paymentAdjustmentStatus = LIFE_CYCLE_STATUS_PENDING;
        } else if (collectionAmount > adjustedAmount) {
            paymentAdjustmentStatus = LIFE_CYCLE_STATUS_INPROGRESS;
        } else {
            paymentAdjustmentStatus = LIFE_CYCLE_STATUS_COMPLETED;
        }

        if (adjustmentList.size() > 0) {
            firstElement = getPaymentAdjustment(paymentId);
        }

        returnMap.put("paymentAdjustmentInfo", firstElement);
        returnMap.put("paymentAdjustmentStatus", paymentAdjustmentStatus);

        return returnMap;
    }

    public Map<String, List<String>> getPaymentTypeAndPaymentNature() {

        Map<String, List<String>> map = new HashMap();
        map.put("paymentType", getPaymentType());
        map.put("paymentNature", getPaymentNature());

        return map;
    }

    /**
     * Newly added to return enum map list;
     */
    public Map<String, List<Map<String, String>>> getPaymentTypeAndNature() {

        Map<String, List<Map<String, String>>> map = new HashMap();

        map.put("paymentType", PaymentType.getAll());
        map.put("paymentNature", PaymentNature.getAll());

        return map;
    }

    public String getPaymentCollectionFileType(Long refId) {
        return documentService.getDocumentFileType(CommonConstant.PAYMENT_COLLECTION_ENTITY, refId);
    }

    public String getPaymentCollectionFileTypeExtension(Long refId) {
        return documentService.getDocumentFileExtension(CommonConstant.PAYMENT_COLLECTION_ENTITY, refId);
    }

    public String getPaymentCollectionFileMimeType(Long refId) {
        return documentService.getDocumentMimeType(CommonConstant.PAYMENT_COLLECTION_ENTITY, refId);
    }

    public String getPaymentCollectionFilePath(Long refId) {

        return documentService.getDocumentFilePath(CommonConstant.PAYMENT_COLLECTION_ENTITY, refId,
                FileType.valueOf(getPaymentCollectionFileType(refId)));
    }

    public String getPaymentCollectionFileName(Long refId) {
        return documentService.getDocumentFileName(CommonConstant.PAYMENT_COLLECTION_ENTITY, refId,
                FileType.valueOf(getPaymentCollectionFileType(refId)));
    }

    public String getPaymentCollectionFileNameForDownload(Long refId) {
        return documentService.getDocumentFileName(CommonConstant.PAYMENT_COLLECTION_ENTITY, refId);
    }

    public List<Map<String, Object>> getPaymentCollectionListToVerify(Map<String, Object> params) {
        try {

            String fromDate = null;
            String toDate = null;

            if (params.get("fiscalYear") != null) {
                long id = Long.parseLong(params.get("fiscalYear").toString());
                AccountingYear accountingYear = accountingYearService.findById(id);
                if (accountingYear != null) {
                    fromDate = accountingYear.getStartDate().toString();
                    toDate = accountingYear.getEndDate().toString();
                }
            }

            return paymentCollectionRepository.getPaymentCollectionListToVerify(
                    applicationUserService.getOrganizationIdFromLoginUser(),
                    Long.parseLong(params.get("companyId").toString()),
                    (String) params.get("paymentNature"),
                    (String) params.get("paymentType"),
                    params.get("location") != null ? Long.parseLong(params.get("location").toString()) : null,
                    (String) params.get("status"),
                    fromDate,
                    toDate
            );
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public PaymentCollection reject(Long id, Map model) {
        try {
            PaymentCollection paymentCollection = this.findById(id);
            paymentCollection.setApprovalStatus(ApprovalStatus.REJECTED);
            paymentCollection.setRejectReason(Objects.requireNonNull(model.get("reason")).toString());
            paymentCollection.setActionTakenBy(applicationUserService.getApplicationUserFromLoginUser());
            paymentCollection.setActionTakenDate(LocalDateTime.now());
            return paymentCollectionRepository.save(paymentCollection);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public PaymentCollection approve(Long id) {
        try {
            PaymentCollection paymentCollection = this.findById(id);
            paymentCollection.setApprovalStatus(ApprovalStatus.APPROVED);
            paymentCollection.setActionTakenBy(applicationUserService.getApplicationUserFromLoginUser());
            paymentCollection.setActionTakenDate(LocalDateTime.now());
            return paymentCollectionRepository.save(paymentCollection);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public void sendPushNotificationWhenPaymentVerifyToSo(Object paymentCollectionObj) {
        PaymentCollection paymentCollection = (PaymentCollection) paymentCollectionObj;
        Long salesOfficerId = Long.valueOf(paymentCollection.getCollectionBy().getId());
        PushNotificationRequest pushNotificationRequest = new PushNotificationRequest();
        String userFCMId = applicationUserService.getUserFCMId(salesOfficerId);
        pushNotificationRequest.setTitle("Collection Verify");
        pushNotificationRequest.setToken(userFCMId);
        pushNotificationRequest.setMessage(paymentCollection.getPaymentNo().toString()
                + " is verified.");
        Resource resource = new ClassPathResource("/images/notification-icon.png");
        try {
            URL url = resource.getURI().toURL();
            if (url!=null)
                pushNotificationRequest.setImage(String.valueOf(ImageIO.read(url)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        pushNotificationService.sendPushNotificationToToken(pushNotificationRequest);
    }

    public List<Map<String, Object>> findLedgerListByDistributorId(Long distributorId, Long companyId, String status, Long accountingYearId) {
        try {
            AccountingYear accountingYear = accountingYearService.findById(accountingYearId);
            LocalDate startDate = accountingYear.getStartDate();
            LocalDate endDate = accountingYear.getEndDate();
            return distributorService.getDistributorLedgerTransaction(distributorId, companyId, startDate, endDate);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map<String, String>> findPaymentListByDistributorId(
            Long distributorId, Long companyId, LocalDate fromDate, LocalDate toDate) {
        try {

            return paymentCollectionRepository.getPaymentCollectionListForAdjustment(
                    distributorId, companyId, fromDate, toDate);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    public Document getAcknowledgementDocumentInfo(Long paymentCollectionId) {
        Document document = documentService.getDocumentInfoByRefIdAndRefTable(paymentCollectionId, PaymentCollection.class.getSimpleName());
        return document;
    }

    public List<Map<String, Object>> getPendingListForApproval(Long companyId, List<Long> soList, String approvalActor, Integer level,Long approvalStepId,Long multiLayerApprovalPathId,Long approvalActorId, String approvalStepName) {
        return paymentCollectionRepository.getPendingListForApproval(
                companyId, PaymentNature.ADVANCE.getCode(), soList,  ApprovalStatus.PENDING.toString(), approvalActor, level, approvalStepId, multiLayerApprovalPathId, approvalActorId, ApprovalStatus.PENDING.getName(),approvalStepName);
    }

    public void updateApprovalStatus(Long id, ApprovalStatus approvalStatus) {
        try {
            Optional<PaymentCollection> paymentCollectionOptional = paymentCollectionRepository.findById(id);
            if (!paymentCollectionOptional.isPresent()) {
                return;
            }
            PaymentCollection paymentCollection = paymentCollectionOptional.get();
            paymentCollection.setApprovalStatusForAuthorization(approvalStatus);
            paymentCollection.setApprovalDateForAuthorization(LocalDate.now());
            paymentCollectionRepository.save(paymentCollection);
        }catch (Exception e){
            throw new RuntimeException("Advance Payment Collection process can't be Executed. Something went wrong!");
        }
    }

    public Map<String,Object> getLastMoneyReceiptNo(Long paymentBookId){
        return paymentCollectionRepository.getLastMoneyReceiptNoDetails(paymentBookId);
    }

    public List<Long> getNotUsedListByDistributorIdAndCompanyId(Long distributorId, Long companyId){
        return paymentCollectionRepository.getNotUsedListByDistributorIdAndCompanyId(distributorId, companyId);
    }

    @Transactional
    public boolean deleteAll(List<Long> paymentCollectionNotUsedIdList) {
        try {
            List<PaymentCollection> paymentCollectionList = paymentCollectionRepository.findByIdIn(paymentCollectionNotUsedIdList);
            paymentCollectionRepository.deleteAll(paymentCollectionList);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return true;
    }

    public boolean existsByDistributorAndCompany(Distributor distributor, Organization company){
        return paymentCollectionRepository.existsByDistributorAndCompany(distributor, company);
    }

    public Map<String, Object> getCollectionReportData(
            Long companyId, List<Long> locationIds,
            List<Long> salesOfficerIds, List<Long> distributorIds,
            Long accountingYearId, LocalDate startDate, LocalDate endDate,
            String dateType, Map<String, Object> parameters) {

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

            parameters.put("accountingYearId", accountingYearId);
            parameters.put("companyId", companyId);
            parameters.put("lastDateHeader",
                    commonReportsService.getReportDate(startDateLastYear,
                            endDateLastYear, dateType));

            reportMap.put("parameters", parameters);
            /*reportMap.put("collectionList",
                    paymentCollectionRepository.getCollectionReportData(salesOfficerIds,
                    distributorIds, startDate, endDate, startDateLastYear, endDateLastYear,
                    accountingYearId, monthList, companyId, locationIds));*/

            return reportMap;

        } catch (Exception e) {
            logger.error("An error occurred: ", e);
            throw new RuntimeException(e.getMessage());
        }
    }

}