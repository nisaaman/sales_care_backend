package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.CommonConstant;
import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.common.enums.CreditLimitTerm;
import com.newgen.ntlsnc.globalsettings.entity.AccountingYear;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.Semester;
import com.newgen.ntlsnc.globalsettings.service.*;
import com.newgen.ntlsnc.notification.pushNotification.dto.PushNotificationRequest;
import com.newgen.ntlsnc.notification.pushNotification.service.PushNotificationService;
import com.newgen.ntlsnc.salesandcollection.dto.CreditLimitProposalDto;
import com.newgen.ntlsnc.salesandcollection.entity.CreditLimitProposal;
import com.newgen.ntlsnc.salesandcollection.entity.Distributor;
import com.newgen.ntlsnc.salesandcollection.entity.SalesReturnProposal;
import com.newgen.ntlsnc.salesandcollection.repository.CreditLimitProposalRepository;
import com.newgen.ntlsnc.supplychainmanagement.entity.SalesReturn;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author marziah
 */
@Service
public class CreditLimitProposalService implements IService<CreditLimitProposal> {
    @Autowired
    CreditLimitProposalRepository creditLimitProposalRepository;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    DistributorService distributorService;
    @Autowired
    LocationManagerMapService locationManagerMapService;
    @Autowired
    SemesterService semesterService;
    @Autowired
    CreditLimitService creditLimitService;
    @Autowired
    AccountingYearService accountingYearService;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    LocationService locationService;
    @Autowired
    DocumentSequenceService documentSequenceService;
    @Autowired
    SalesBookingService salesBookingService;
    @Autowired
    PushNotificationService pushNotificationService;
    @Override
    @Transactional
    public CreditLimitProposal create(Object object) {
        try {
            CreditLimitProposalDto creditLimitProposalDto = (CreditLimitProposalDto) object;
            CreditLimitProposal creditLimitProposal = new CreditLimitProposal();

            CreditLimitTerm creditLimitTerm = CreditLimitTerm.valueOf(creditLimitProposalDto.getCreditLimitTerm());

            if (creditLimitTerm == CreditLimitTerm.ST) {
                if (creditLimitService.isShortTermExistByCompanyAndDistributorAndStartDateOrEndDate(   // exist check
                        creditLimitProposalDto.getCompanyId(), creditLimitProposalDto.getDistributorId(),
                        creditLimitProposalDto.getStartDate(), creditLimitProposalDto.getEndDate())) {
                    throw new IllegalArgumentException("Already exist with this Short Term Credit Limit Start Date and End Date");
                }
                creditLimitProposal.setStartDate(LocalDate.parse(creditLimitProposalDto.getStartDate()));    //yyyy-MM-dd    // it will come from credit limit proposal
                creditLimitProposal.setEndDate(LocalDate.parse(creditLimitProposalDto.getEndDate()));    //yyyy-MM-dd      // it will come from credit limit proposal
            } else if (creditLimitTerm == CreditLimitTerm.SB) {
                creditLimitProposal.setSalesBooking(salesBookingService.findById(creditLimitProposalDto.getSalesBookingId()));
                creditLimitProposal.setProposedAmount(salesBookingService.getTotalAmountBySalesBookingId(creditLimitProposalDto.getSalesBookingId()));
            }

            creditLimitProposal.setProposedAmount(creditLimitProposalDto.getProposedAmount());
            creditLimitProposal.setProposalNo(documentSequenceService.getSequenceByDocumentId(CommonConstant.DOCUMENT_ID_FOR_CREDIT_LIMIT_PROPOSAL));
            creditLimitProposal.setProposalDate(LocalDate.parse(creditLimitProposalDto.getProposalDate()));    //yyyy-MM-dd
            creditLimitProposal.setProposalNotes(creditLimitProposalDto.getProposalNotes());
            creditLimitProposal.setCreditLimitTerm(creditLimitTerm);
            creditLimitProposal.setApprovalStatus(ApprovalStatus.PENDING);
            creditLimitProposal.setApprovalDate(LocalDate.now());
            Distributor distributorId = distributorService.findById(creditLimitProposalDto.getDistributorId());
            creditLimitProposal.setDistributor(distributorId);
            creditLimitProposal.setCompany(organizationService.findById(creditLimitProposalDto.getCompanyId()));
            creditLimitProposal.setOrganization(organizationService.getOrganizationFromLoginUser());
            creditLimitProposal.setCurrentLimit(creditLimitProposalDto.getCurrentLimit());
            creditLimitProposal.setBusinessPlan(creditLimitProposal.getBusinessPlan());

            if (!this.validate(creditLimitProposal)) {
                return null;
            }
            return creditLimitProposalRepository.save(creditLimitProposal);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public CreditLimitProposal update(Long id, Object object) {

        CreditLimitProposalDto creditLimitProposalDto = (CreditLimitProposalDto) object;
        CreditLimitProposal creditLimitProposal = this.findById(creditLimitProposalDto.getId());
        creditLimitProposal.setOrganization(organizationService.getOrganizationFromLoginUser());
        Distributor distributorId = distributorService.findById(creditLimitProposalDto.getDistributorId());
        creditLimitProposal.setProposedAmount(creditLimitProposalDto.getProposedAmount());
        creditLimitProposal.setProposalNotes(creditLimitProposalDto.getProposalNotes());
        creditLimitProposal.setApprovalStatus(ApprovalStatus.PENDING);
        creditLimitProposal.setDistributor(distributorId);
        creditLimitProposal.setProposalDate(LocalDate.parse(creditLimitProposalDto.getProposalDate()));    //yyyy-MM-dd
        creditLimitProposal.setCurrentLimit(creditLimitProposalDto.getCurrentLimit());
        creditLimitProposal.setBusinessPlan(creditLimitProposal.getBusinessPlan());
        if (!this.validate(creditLimitProposal)) {
            return null;
        }
        return creditLimitProposalRepository.save(creditLimitProposal);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            CreditLimitProposal creditLimitProposal = findById(id);
            creditLimitProposal.setIsDeleted(true);
            creditLimitProposalRepository.save(creditLimitProposal);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public CreditLimitProposal findById(Long id) {
        try {
            Optional<CreditLimitProposal> optionalCreditLimitProposal = creditLimitProposalRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalCreditLimitProposal.isPresent()) {
                throw new Exception("Credit Limit Proposal Not exist with id " + id);
            }
            return optionalCreditLimitProposal.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<CreditLimitProposal> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return creditLimitProposalRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }

    public List<Map<String, Object>> getCreditLimitProposalList(Map<String, Object> searchParams) {
        try {
            List<Map<String, Object>> creditProposalList = new ArrayList<>();
            Long companyId;
            Long userId;
            if (searchParams.get("companyId") == null
                    || searchParams.get("companyId").equals("undefined")) {
                return creditProposalList;
            }
            else {
                companyId = Long.parseLong(searchParams.get("companyId").toString());
            }

            if (searchParams.get("userLoginId") != null) {
                userId = Long.parseLong(searchParams.get("userLoginId").toString());
            } else {
                userId = applicationUserService.getApplicationUserIdFromLoginUser();
            }

            Long locationId;

            Map<Long, Object> childLocationMap = new HashMap<>();
            AccountingYear accountingYear;
            LocalDate startDate = null;
            LocalDate endDate = null;

            if (searchParams.get("fiscalYearId") != null) {
                Long fiscalYearId = Long.parseLong(searchParams.get("fiscalYearId").toString());
                accountingYear = accountingYearService.findById(fiscalYearId);
                if (accountingYear != null) {
                    startDate = accountingYear.getStartDate();
                    endDate = accountingYear.getEndDate();
                }
            }

            List<String> approvalStatusList = new ArrayList<>();
            List<Long> saleOfficerList = new ArrayList<>();
            if (searchParams.get("status") == null) {
                approvalStatusList = Stream.of(ApprovalStatus.DRAFT.toString(), ApprovalStatus.PENDING.toString(), ApprovalStatus.REJECTED.toString(), ApprovalStatus.APPROVED.toString()).collect(Collectors.toList());
            } else {
                approvalStatusList.add(searchParams.get("status").toString());
            }
            if (searchParams.get("locationId") != null) {
                locationId = Long.parseLong(searchParams.get("locationId").toString());
                saleOfficerList = locationManagerMapService.getSalesOfficerListFromUserLoginIdOrLocationId(userId, locationId, companyId);
            } else {

                Boolean isManager = applicationUserService.checkLoginUserIsManager(companyId, userId);
                Boolean isSo = applicationUserService.checkLoginUserIsSo(companyId, userId);

                if (isManager) {
                    locationId = locationService.getManagerLocation(companyId, userId);
                    childLocationMap =
                            locationService.getChildLocationsByParent(
                                    companyId, locationId, childLocationMap);
                    saleOfficerList = locationService.getSoListByLocation(companyId, childLocationMap);

                } else if (isSo) {
                    saleOfficerList.add(userId);
                }
            }

            creditLimitProposalRepository.findList(companyId, saleOfficerList, startDate, endDate, approvalStatusList).forEach(map -> {
                Map<String, Object> creditProposal = new HashMap<>(map);
//                if(searchParams.get("semesterId") == null){
//                    creditProposal.put("currentBalance", creditLimitService.getDistributorLimit(Long.parseLong(map.get("distributor_id").toString()), companyId, semester.get().getId()));
//                }else {
//                    creditProposal.put("currentBalance", creditLimitService.getDistributorLimit(Long.parseLong(map.get("distributor_id").toString()), companyId, Long.parseLong(searchParams.get("semesterId").toString())));
//                }
                creditProposal.put("location", locationManagerMapService.getSalesOfficerLocation(Long.parseLong(map.get("application_user_id").toString()), Long.parseLong(map.get("organization_id").toString()), companyId));
                creditProposalList.add(creditProposal);
            });

            return creditProposalList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Map<String, Object> getSalesVsTargetForADistributor(Long companyId, Long distributorId) throws Exception {
        Map<String, Object> salesTarget = new HashMap<String, Object>();
        LocalDate asOnDate = LocalDate.now();
        List<Long> distributorList = new ArrayList<>();
        distributorList.add(distributorId);
        AccountingYear accountingYear = accountingYearService.getAccountingYearByDateRange(asOnDate, asOnDate, companyId);

        if (accountingYear == null) {
            throw new RuntimeException("No Accounting Year set for current date.");
        }
        List<Map<String, Object>> ledgerBalance = distributorService.getDistributorsDetailsWithLedgerBalance(distributorList, companyId,
                accountingYear.getStartDate(), accountingYear.getEndDate());
        salesTarget.put("totalBudget", ledgerBalance);

        return salesTarget;
    }

    public List<Map<String, Object>> getPendingListForApproval(
            Long companyId, List<Long> soList, String approvalActor, String approvalStatus, Integer level,
            Long approvalStepId, Long multiLayerApprovalPathId, Long approvalActorId, String approvalStepName) {
        return creditLimitProposalRepository.getPendingListForApproval(
                companyId, soList, approvalStatus, approvalActor, level, approvalStepId, multiLayerApprovalPathId, approvalActorId, ApprovalStatus.PENDING.getName(), approvalStepName);
    }

    @Transactional
    public void updateApprovalStatus(Long id, ApprovalStatus approvalStatus) {
        try {
            Optional<CreditLimitProposal> creditLimitProposalOptional = creditLimitProposalRepository.findById(id);
            if (!creditLimitProposalOptional.isPresent()) {
                return;
            }
            CreditLimitProposal creditLimitProposal = creditLimitProposalOptional.get();
            creditLimitProposal.setApprovalStatus(approvalStatus);
            creditLimitProposal.setApprovalDate(LocalDate.now());
            creditLimitProposalRepository.save(creditLimitProposal);
            if (creditLimitProposal.getApprovalStatus() == ApprovalStatus.APPROVED) {
                creditLimitService.convertToCreditLimitByCreditLimitProposalId(creditLimitProposal.getId());
            }
        } catch (Exception e) {
            throw new RuntimeException("Credit Limit Proposal approval process can't be Executed. Something went wrong!");
        }
    }

    public Map<String, Object> getProposalStatusWiseCount(Long companyId) {
        return  creditLimitProposalRepository.getProposalStatusWiseCount(companyId, applicationUserService.getApplicationUserIdFromLoginUser());
    }

    public void sendPushNotificationToCreditLimitApproval(Long refId) {
        CreditLimitProposal creditLimitProposal = findById(refId);
        Long salesOfficerId = Long.valueOf(creditLimitProposal.getCreatedBy());
        Distributor distributor = distributorService.findById(creditLimitProposal.getDistributor().getId());
        PushNotificationRequest pushNotificationRequest = new PushNotificationRequest();
        String userFCMId = applicationUserService.getUserFCMId(salesOfficerId);
        pushNotificationRequest.setTitle("Credit Limit Proposal Verify");
        pushNotificationRequest.setToken(userFCMId);
        pushNotificationRequest.setMessage(creditLimitProposal.getProposalNo()+ " of "+
                distributor.getDistributorName()+ " is verified.");
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
}
