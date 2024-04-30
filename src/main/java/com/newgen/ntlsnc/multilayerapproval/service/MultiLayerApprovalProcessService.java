package com.newgen.ntlsnc.multilayerapproval.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.ApprovalActor;
import com.newgen.ntlsnc.common.enums.ApprovalFeature;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.common.enums.PaymentNature;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.service.LocationManagerMapService;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.multilayerapproval.dto.MultiLayerApprovalProcessDto;
import com.newgen.ntlsnc.multilayerapproval.entity.MultiLayerApprovalPath;
import com.newgen.ntlsnc.multilayerapproval.entity.MultiLayerApprovalProcess;
import com.newgen.ntlsnc.multilayerapproval.repository.MultiLayerApprovalProcessRepository;
import com.newgen.ntlsnc.salesandcollection.service.*;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author nisa
 * @date 9/14/22
 * @time 6:43 PM
 */
@Service
public class MultiLayerApprovalProcessService implements IService<MultiLayerApprovalProcess> {

    final MultiLayerApprovalProcessRepository multiLayerApprovalProcessRepository;
    final SalesBookingService salesBookingService;

    final LocationManagerMapService locationManagerMapService;

    final OrganizationService organizationService;

    final MultiLayerApprovalPathService multiLayerApprovalPathService;

    final ApplicationUserService applicationUserService;

    final ApprovalStepService approvalStepService;

    final CreditLimitProposalService creditLimitProposalService;

    final SalesReturnProposalService salesReturnProposalService;
    final SalesBudgetService salesBudgetService;

    final CreditDebitNoteService creditDebitNoteService;
    final CollectionBudgetService collectionBudgetService;
    final DistributorService distributorService;

    final SalesOrderService salesOrderService;

    final PaymentCollectionService paymentCollectionService;


    public MultiLayerApprovalProcessService(MultiLayerApprovalProcessRepository multiLayerApprovalProcessRepository, SalesBookingService salesBookingService, LocationManagerMapService locationManagerMapService, OrganizationService organizationService, MultiLayerApprovalPathService multiLayerApprovalPathService, ApplicationUserService applicationUserService, ApprovalStepService approvalStepService, CreditLimitProposalService creditLimitProposalService, SalesReturnProposalService salesReturnProposalService, SalesBudgetService salesBudgetService, CreditDebitNoteService creditDebitNoteService, CollectionBudgetService collectionBudgetService, DistributorService distributorService, SalesOrderService salesOrderService, PaymentCollectionService paymentCollectionService) {
        this.multiLayerApprovalProcessRepository = multiLayerApprovalProcessRepository;
        this.salesBookingService = salesBookingService;
        this.locationManagerMapService = locationManagerMapService;
        this.organizationService = organizationService;
        this.multiLayerApprovalPathService = multiLayerApprovalPathService;
        this.applicationUserService = applicationUserService;
        this.approvalStepService = approvalStepService;
        this.creditLimitProposalService = creditLimitProposalService;
        this.salesReturnProposalService = salesReturnProposalService;
        this.salesBudgetService = salesBudgetService;
        this.creditDebitNoteService = creditDebitNoteService;
        this.collectionBudgetService = collectionBudgetService;
        this.distributorService = distributorService;
        this.salesOrderService = salesOrderService;
        this.paymentCollectionService = paymentCollectionService;
    }

    @Override
    public MultiLayerApprovalProcess create(Object object) {
        MultiLayerApprovalProcessDto multiLayerApprovalProcessDto = (MultiLayerApprovalProcessDto) object;
        MultiLayerApprovalProcess multiLayerApprovalProcess = new MultiLayerApprovalProcess();

        Organization company = organizationService.findByIdAndIsDeletedFalseAndActiveTrue(multiLayerApprovalProcessDto.getCompanyId());
        multiLayerApprovalProcess.setCompany(company);

        MultiLayerApprovalPath multiLayerApprovalPath = multiLayerApprovalPathService.findById(multiLayerApprovalProcessDto.getMultiLayerApprovalPathId());

        multiLayerApprovalProcess.setMultiLayerApprovalPath(multiLayerApprovalPath);

        multiLayerApprovalProcess.setRefId(multiLayerApprovalProcessDto.getRefId());

        multiLayerApprovalProcess.setComments(multiLayerApprovalProcessDto.getComments());

        multiLayerApprovalProcess.setActionTakenBy(applicationUserService.getApplicationUserFromLoginUser());

        multiLayerApprovalProcess.setApprovalActor(ApprovalActor.valueOf(multiLayerApprovalProcessDto.getApprovalActor()));

        multiLayerApprovalProcess.setApprovalStatus(ApprovalStatus.valueOf(multiLayerApprovalProcessDto.getApprovalStatus()));

        multiLayerApprovalProcess.setApprovalStep(approvalStepService.findById(multiLayerApprovalProcessDto.getApprovalStepId()));

        multiLayerApprovalProcess.setOrganization(organizationService.getOrganizationFromLoginUser());

        multiLayerApprovalProcess.setApprovalFeature(ApprovalFeature.valueOf(multiLayerApprovalProcessDto.getApprovalFeature()));

        multiLayerApprovalProcess.setLevel(multiLayerApprovalProcessDto.getLevel());

        if (multiLayerApprovalProcessDto.getApprovalFeature().equals(ApprovalFeature.SALES_BOOKING.toString())) {
            multiLayerApprovalProcess.setRefTable("SalesBooking");
            Integer lastLevel = multiLayerApprovalPathService.getLastLevelApprovalPath(multiLayerApprovalProcessDto.getCompanyId(), multiLayerApprovalProcessDto.getApprovalFeature());

            if (multiLayerApprovalProcessDto.getLevel().equals(lastLevel)) {
                if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.APPROVED.getCode()))
                    salesBookingService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.APPROVED);
                else if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.REJECTED.getCode()))
                    salesBookingService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.REJECTED);
            } else {
                if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.APPROVED.getCode()))
                    salesBookingService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.AUTHORIZATION_FLOW);
                else if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.REJECTED.getCode()))
                    salesBookingService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.REJECTED);

                /*notify next approver about booking status*/
                salesBookingService.sendPushNotificationToBookingNextApprover(
                        multiLayerApprovalProcessDto.getCompanyId(), multiLayerApprovalProcessDto.getRefId(),
                        multiLayerApprovalProcessDto.getLevel()+1);
            }
        } else if (multiLayerApprovalProcessDto.getApprovalFeature().equals(ApprovalFeature.CREDIT_LIMIT.toString())) {
            multiLayerApprovalProcess.setRefTable("CreditLimitProposal");
            Integer lastLevel = multiLayerApprovalPathService.getLastLevelApprovalPath(multiLayerApprovalProcessDto.getCompanyId(), multiLayerApprovalProcessDto.getApprovalFeature());
            if (multiLayerApprovalProcessDto.getLevel().equals(lastLevel)) {
                if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.APPROVED.getCode())) {
                    creditLimitProposalService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.APPROVED);
                    /** #push notification */
                    creditLimitProposalService.sendPushNotificationToCreditLimitApproval(multiLayerApprovalProcessDto.getRefId());
                }
                else if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.REJECTED.getCode()))
                    creditLimitProposalService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.REJECTED);
            } else {
                if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.APPROVED.getCode()))
                    creditLimitProposalService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.AUTHORIZATION_FLOW);
                else if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.REJECTED.getCode()))
                    creditLimitProposalService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.REJECTED);
            }
        } else if (multiLayerApprovalProcessDto.getApprovalFeature().equals(ApprovalFeature.SALES_RETURN.toString())) {
            multiLayerApprovalProcess.setRefTable("SalesReturnProposal");
            Integer lastLevel = multiLayerApprovalPathService.getLastLevelApprovalPath(multiLayerApprovalProcessDto.getCompanyId(), multiLayerApprovalProcessDto.getApprovalFeature());
            if (multiLayerApprovalProcessDto.getLevel().equals(lastLevel)) {
                if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.APPROVED.getCode()))
                    salesReturnProposalService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.APPROVED);
                else if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.REJECTED.getCode()))
                    salesReturnProposalService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.REJECTED);
            } else {
                if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.APPROVED.getCode()))
                    salesReturnProposalService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.AUTHORIZATION_FLOW);
                else if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.REJECTED.getCode()))
                    salesReturnProposalService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.REJECTED);
            }
        } else if (multiLayerApprovalProcessDto.getApprovalFeature().equals(ApprovalFeature.SALES_BUDGET.toString())) {
            multiLayerApprovalProcess.setRefTable("SalesBudget");
            Integer lastLevel = multiLayerApprovalPathService.getLastLevelApprovalPath(multiLayerApprovalProcessDto.getCompanyId(), multiLayerApprovalProcessDto.getApprovalFeature());
            if (multiLayerApprovalProcessDto.getLevel().equals(lastLevel)) {
                if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.APPROVED.getCode()))
                    salesBudgetService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.APPROVED);
                else if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.REJECTED.getCode()))
                    salesBudgetService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.REJECTED);
            } else {
                if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.APPROVED.getCode()))
                    salesBudgetService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.AUTHORIZATION_FLOW);
                else if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.REJECTED.getCode()))
                    salesBudgetService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.REJECTED);
            }
        } else if (multiLayerApprovalProcessDto.getApprovalFeature().equals(ApprovalFeature.CREDIT_DEBIT_NOTE.toString())) {
            multiLayerApprovalProcess.setRefTable("CreditDebitNote");
            Integer lastLevel = multiLayerApprovalPathService.getLastLevelApprovalPath(multiLayerApprovalProcessDto.getCompanyId(), multiLayerApprovalProcessDto.getApprovalFeature());
            if (multiLayerApprovalProcessDto.getLevel().equals(lastLevel)) {
                if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.APPROVED.getCode()))
                    creditDebitNoteService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.APPROVED);
                else if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.REJECTED.getCode()))
                    creditDebitNoteService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.REJECTED);
            } else {
                if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.APPROVED.getCode()))
                    creditDebitNoteService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.AUTHORIZATION_FLOW);
                else if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.REJECTED.getCode()))
                    creditDebitNoteService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.REJECTED);
            }
        } else if (multiLayerApprovalProcessDto.getApprovalFeature().equals(ApprovalFeature.COLLECTION_BUDGET.toString())) {
            multiLayerApprovalProcess.setRefTable("CollectionBudget");
            Integer lastLevel = multiLayerApprovalPathService.getLastLevelApprovalPath(multiLayerApprovalProcessDto.getCompanyId(), multiLayerApprovalProcessDto.getApprovalFeature());
            if (multiLayerApprovalProcessDto.getLevel().equals(lastLevel)) {
                if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.APPROVED.getCode()))
                    collectionBudgetService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.APPROVED);
                else if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.REJECTED.getCode()))
                    collectionBudgetService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.REJECTED);
            } else {
                if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.APPROVED.getCode()))
                    collectionBudgetService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.AUTHORIZATION_FLOW);
                else if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.REJECTED.getCode()))
                    collectionBudgetService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.REJECTED);
            }
        } else if (multiLayerApprovalProcessDto.getApprovalFeature().equals(ApprovalFeature.SALES_ORDER.toString())) {
            multiLayerApprovalProcess.setRefTable("SalesOrder");
            Integer lastLevel = multiLayerApprovalPathService.getLastLevelApprovalPath(multiLayerApprovalProcessDto.getCompanyId(), multiLayerApprovalProcessDto.getApprovalFeature());
            if (multiLayerApprovalProcessDto.getLevel().equals(lastLevel)) {
                if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.APPROVED.getCode())) {
                    salesOrderService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.APPROVED);
                    /** #push notification */
                    salesOrderService.sendPushNotificationToOrderApproval(multiLayerApprovalProcessDto.getRefId());
                    /** #send mail notification currently pause not availability of valid mail*/
                    //salesOrderService.sendMailToOrderApproval(multiLayerApprovalProcessDto.getRefId());
                    /** #send sms notification currently pause*/
                    //salesOrderService.sendSmsNotificationToOrderApproval(multiLayerApprovalProcessDto.getRefId());
                }
                else if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.REJECTED.getCode()))
                    salesOrderService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.REJECTED);
            } else {
                if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.APPROVED.getCode()))
                    salesOrderService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.AUTHORIZATION_FLOW);
                else if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.REJECTED.getCode()))
                    salesOrderService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.REJECTED);
            }
        } else if (multiLayerApprovalProcessDto.getApprovalFeature().equals(ApprovalFeature.ADVANCE_PAYMENT_COLLECTION.toString())) {
            multiLayerApprovalProcess.setRefTable("PaymentCollection");
            Integer lastLevel = multiLayerApprovalPathService.getLastLevelApprovalPath(multiLayerApprovalProcessDto.getCompanyId(), multiLayerApprovalProcessDto.getApprovalFeature());
            if (multiLayerApprovalProcessDto.getLevel().equals(lastLevel)) {
                if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.APPROVED.getCode()))
                    paymentCollectionService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.APPROVED);
                else if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.REJECTED.getCode()))
                    paymentCollectionService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.REJECTED);
            } else {
                if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.APPROVED.getCode()))
                    paymentCollectionService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.AUTHORIZATION_FLOW);
                else if (multiLayerApprovalProcessDto.getApprovalStatus().equals(ApprovalStatus.REJECTED.getCode()))
                    paymentCollectionService.updateApprovalStatus(multiLayerApprovalProcessDto.getRefId(), ApprovalStatus.REJECTED);
            }
        }


        if (!this.validate(multiLayerApprovalProcess)) {
            return null;
        }
        Optional<MultiLayerApprovalProcess> prevLayerMultiLayerApprovalProcessOptional =  multiLayerApprovalProcessRepository.findByRefIdAndRefTableAndLevel(multiLayerApprovalProcess.getRefId(), multiLayerApprovalProcess.getRefTable(), multiLayerApprovalProcess.getLevel()-1);
        if(prevLayerMultiLayerApprovalProcessOptional.isPresent()){
            MultiLayerApprovalProcess prevLayerMultiLayerApprovalProcess = prevLayerMultiLayerApprovalProcessOptional.get();
            prevLayerMultiLayerApprovalProcess.setIsNextLayerDone(Boolean.TRUE);
            multiLayerApprovalProcessRepository.save(prevLayerMultiLayerApprovalProcess);
        }
        return multiLayerApprovalProcessRepository.save(multiLayerApprovalProcess);
    }

    public List<Map<String, Object>> getApprovalProcessList(Long companyId, String approvalFeature) {
        List<Map<String, Object>> approvalActorList = multiLayerApprovalPathService.getApprovalActorList(companyId, approvalFeature);
        List<Map<String, Object>> approvalProcessList = new ArrayList<>();
        for (Map<String, Object> approvalActorMap : approvalActorList) {
            Integer level = Integer.valueOf(approvalActorMap.get("level").toString());
            String approvalActor = approvalActorMap.get("approvalActor").toString();
            Long approvalStepId = Long.parseLong(approvalActorMap.get("approvalStepId").toString());
            String approvalStepName = approvalActorMap.get("approvalStepName").toString();
            Long multiLayerApprovalPathId = Long.parseLong(approvalActorMap.get("multiLayerApprovalPathId").toString());
            Long approvalActorId = approvalActorMap.get("approvalActorId") != null ? Long.parseLong(approvalActorMap.get("approvalActorId").toString()) : null;
            List<Long> soList = new ArrayList<>();
            List<Long> distributorList = new ArrayList<>();

            if (approvalActor.equals(ApprovalActor.LOCATION_TYPE.toString())) {
                Long locationId = Long.parseLong(approvalActorMap.get("locationId").toString());
                soList = locationManagerMapService.getSalesOfficerListFromUserLoginIdOrLocationId(null, locationId, companyId);
            } else if (approvalActor.equals(ApprovalActor.DEPOT_IN_CHARGE.toString())) {
                if(approvalActorMap.get("locationIds") != null) {
                    List<Long> locationIds = Arrays.stream(approvalActorMap.get("locationIds").toString().split(",")).map(Long::parseLong).collect(Collectors.toList());
                    for (Long locationId : locationIds) {
                        List<Long> salesOfficerList = locationManagerMapService.getSalesOfficerListFromUserLoginIdOrLocationId(null, locationId, companyId);
                        if (salesOfficerList != null)
                            soList.addAll(locationManagerMapService.getSalesOfficerListFromUserLoginIdOrLocationId(null, locationId, companyId));
                    }
                }else if(approvalActorMap.get("locationIds") == null) {
                    continue;
                }
            }
            if (soList.size() > 0) {
                distributorList = distributorService.getDistributorListOfSo(soList, companyId);
            }
            if (level.equals(0)) {
                switch (ApprovalFeature.valueOf(approvalFeature)) {
                    case SALES_BOOKING:
                        approvalProcessList.addAll(salesBookingService.getPendingListForApproval(companyId, soList, approvalActor, level, approvalStepId, multiLayerApprovalPathId, approvalActorId, approvalStepName));
                        break;
                    case CREDIT_DEBIT_NOTE:
                        approvalProcessList.addAll(creditDebitNoteService.getPendingListForApproval(companyId, approvalActor, ApprovalStatus.PENDING.toString(), level, approvalStepId, multiLayerApprovalPathId, approvalActorId, distributorList, approvalStepName));
                        break;
                    case CREDIT_LIMIT:
                        approvalProcessList.addAll(creditLimitProposalService.getPendingListForApproval(companyId, soList, approvalActor, ApprovalStatus.PENDING.toString(), level, approvalStepId, multiLayerApprovalPathId, approvalActorId, approvalStepName));
                        break;
                    case SALES_RETURN:
                        approvalProcessList.addAll(salesReturnProposalService.getPendingListForApproval(companyId, soList, approvalActor, ApprovalStatus.PENDING.toString(), level, approvalStepId, multiLayerApprovalPathId, approvalActorId, approvalStepName));
                        break;
                    case SALES_BUDGET:
                        approvalProcessList.addAll(salesBudgetService.getPendingListForApproval(companyId, approvalActor, ApprovalStatus.PENDING.toString(), level, approvalStepId, multiLayerApprovalPathId, approvalActorId, approvalStepName));
                        break;
                    case COLLECTION_BUDGET:
                        approvalProcessList.addAll(collectionBudgetService.getPendingListForApproval(companyId, approvalActor, ApprovalStatus.PENDING.toString(), level, approvalStepId, multiLayerApprovalPathId, approvalActorId, approvalStepName));
                        break;
                    case SALES_ORDER:
                        approvalProcessList.addAll(salesOrderService.getPendingListForApproval(companyId, soList, approvalActor, level, approvalStepId, multiLayerApprovalPathId, approvalActorId, approvalStepName));
                        break;
                    case ADVANCE_PAYMENT_COLLECTION:
                        approvalProcessList.addAll(paymentCollectionService.getPendingListForApproval(companyId, soList, approvalActor, level, approvalStepId, multiLayerApprovalPathId, approvalActorId, approvalStepName));
                        break;
                }
            } else {
                Integer prevLevel = level - 1;

                switch (ApprovalFeature.valueOf(approvalFeature)) {
                    case SALES_BOOKING:
                        approvalProcessList.addAll(getPendingSalesBookingListAfterFirstLevel(companyId, soList, approvalActor, level, approvalStepId, multiLayerApprovalPathId, approvalActorId, prevLevel, approvalStepName));
                        break;
                    case CREDIT_DEBIT_NOTE:
                        approvalProcessList.addAll(getPendingCreditDebitNoteListAfterFirstLevel(companyId, approvalActor, level, approvalStepId, approvalActorId, multiLayerApprovalPathId, distributorList, prevLevel, approvalStepName));
                        break;
                    case CREDIT_LIMIT:
                        approvalProcessList.addAll(getPendingCreditLimitListAfterFirstLevel(companyId, soList, approvalActor, level, approvalStepId, approvalActorId, multiLayerApprovalPathId, prevLevel, approvalStepName));
                        break;
                    case SALES_RETURN:
                        approvalProcessList.addAll(getPendingSalesReturnListAfterFirstLevel(companyId, soList, approvalActor, level, approvalStepId, approvalActorId, multiLayerApprovalPathId, prevLevel, approvalStepName));
                        break;
                    case SALES_BUDGET:
                        approvalProcessList.addAll(getPendingSalesBudgetListAfterFirstLevel(companyId, approvalActor, level, approvalStepId, approvalActorId, multiLayerApprovalPathId, prevLevel, approvalStepName));
                        break;
                    case COLLECTION_BUDGET:
                        approvalProcessList.addAll(getPendingCollectionBudgetListAfterFirstLevel(companyId, approvalActor, level, approvalStepId, approvalActorId, multiLayerApprovalPathId, prevLevel, approvalStepName));
                        break;
                    case SALES_ORDER:
                        approvalProcessList.addAll(getPendingSalesOrderListAfterFirstLevel(companyId, soList, approvalActor, level, approvalStepId, multiLayerApprovalPathId, approvalActorId, prevLevel, approvalStepName));
                        break;
                    case ADVANCE_PAYMENT_COLLECTION:
                        approvalProcessList.addAll(getPendingPaymentCollectionListForAdvanceAfterFirstLevel(companyId, soList, approvalActor, level, approvalStepId, multiLayerApprovalPathId, approvalActorId, prevLevel, approvalStepName));
                        break;
                }
            }
        }
        return approvalProcessList;
    }

    public List<Map<String, Object>> getPendingSalesBookingListAfterFirstLevel(Long companyId, List<Long> soList, String approvalActor, Integer level, Long approvalStepId, Long multiLayerApprovalPathId, Long approvalActorId, Integer prevLevel, String approvalStepName) {
        return multiLayerApprovalProcessRepository.getPendingSalesBookingListForAuthorizationAfterFirstLevel(companyId, soList, ApprovalStatus.AUTHORIZATION_FLOW.toString(), approvalActor, level, approvalStepId, multiLayerApprovalPathId, approvalActorId, ApprovalStatus.AUTHORIZATION_FLOW.getName(), prevLevel, approvalStepName);
    }

    private List<Map<String, Object>> getPendingCreditDebitNoteListAfterFirstLevel(Long companyId, String approvalActor, Integer level, Long approvalStepId, Long approvalActorId, Long multiLayerApprovalPathId, List<Long> distributorList, Integer prevLevel, String approvalStepName) {
        return multiLayerApprovalProcessRepository.getPendingCreditDebitNoteListForAuthorizationAfterFirstLevel(companyId, ApprovalStatus.AUTHORIZATION_FLOW.toString(), approvalActor, level, approvalStepId, multiLayerApprovalPathId, approvalActorId, distributorList, ApprovalStatus.AUTHORIZATION_FLOW.getName(), prevLevel, approvalStepName);
    }

    private List<Map<String, Object>> getPendingCreditLimitListAfterFirstLevel(Long companyId, List<Long> soList, String approvalActor, Integer level, Long approvalStepId, Long approvalActorId, Long multiLayerApprovalPathId, Integer prevLevel, String approvalStepName) {
        return multiLayerApprovalProcessRepository.getPendingCreditLimitListForAuthorizationAfterFirstLevel(companyId, soList, ApprovalStatus.AUTHORIZATION_FLOW.toString(), approvalActor, level, approvalStepId, multiLayerApprovalPathId, approvalActorId, ApprovalStatus.AUTHORIZATION_FLOW.getName(), prevLevel, approvalStepName);
    }

    private List<Map<String, Object>> getPendingSalesReturnListAfterFirstLevel(Long companyId, List<Long> soList, String approvalActor, Integer level, Long approvalStepId, Long approvalActorId, Long multiLayerApprovalPathId, Integer prevLevel, String approvalStepName) {
        return multiLayerApprovalProcessRepository.getPendingSalesReturnListForAuthorizationAfterFirstLevel(companyId, soList, ApprovalStatus.AUTHORIZATION_FLOW.toString(), approvalActor, level, approvalStepId, multiLayerApprovalPathId, approvalActorId, ApprovalStatus.AUTHORIZATION_FLOW.getName(), prevLevel, approvalStepName);
    }

    private List<Map<String, Object>> getPendingSalesBudgetListAfterFirstLevel(Long companyId, String approvalActor, Integer level, Long approvalStepId, Long approvalActorId, Long multiLayerApprovalPathId, Integer prevLevel, String approvalStepName) {
        return multiLayerApprovalProcessRepository.getPendingSalesBudgetListForAuthorizationAfterFirstLevel(companyId, ApprovalStatus.AUTHORIZATION_FLOW.toString(), approvalActor, level, approvalStepId, multiLayerApprovalPathId, approvalActorId, ApprovalStatus.AUTHORIZATION_FLOW.getName(), prevLevel, approvalStepName);
    }

    private List<Map<String, Object>> getPendingCollectionBudgetListAfterFirstLevel(Long companyId, String approvalActor, Integer level, Long approvalStepId, Long approvalActorId, Long multiLayerApprovalPathId, Integer prevLevel, String approvalStepName) {
        return multiLayerApprovalProcessRepository.getPendingCollectionBudgetListForAuthorizationAfterFirstLevel(companyId, ApprovalStatus.AUTHORIZATION_FLOW.toString(), approvalActor, level, approvalStepId, multiLayerApprovalPathId, approvalActorId, ApprovalStatus.AUTHORIZATION_FLOW.getName(), prevLevel, approvalStepName);
    }


    private List<Map<String, Object>> getPendingSalesOrderListAfterFirstLevel(Long companyId, List<Long> soList, String approvalActor, Integer level, Long approvalStepId, Long multiLayerApprovalPathId, Long approvalActorId, Integer prevLevel, String approvalStepName) {
        return multiLayerApprovalProcessRepository.getPendingSalesOrderListForAuthorizationAfterFirstLevel(companyId, soList, ApprovalStatus.AUTHORIZATION_FLOW.toString(), approvalActor, level, approvalStepId, multiLayerApprovalPathId, approvalActorId, ApprovalStatus.AUTHORIZATION_FLOW.getName(), prevLevel, approvalStepName);
    }


    private List<Map<String, Object>> getPendingPaymentCollectionListForAdvanceAfterFirstLevel(Long companyId, List<Long> soList, String approvalActor, Integer level, Long approvalStepId, Long multiLayerApprovalPathId, Long approvalActorId, Integer prevLevel, String approvalStepName) {
        return multiLayerApprovalProcessRepository.getPendingPaymentCollectionListForAdvanceAfterFirstLevel(companyId, PaymentNature.ADVANCE.getCode(), soList, ApprovalStatus.AUTHORIZATION_FLOW.toString(), approvalActor, level, approvalStepId, multiLayerApprovalPathId, approvalActorId, ApprovalStatus.AUTHORIZATION_FLOW.getName(), prevLevel, approvalStepName);
    }



    @Override
    public MultiLayerApprovalProcess update(Long id, Object object) {
        return null;
    }

    @Override
    public boolean delete(Long id) {
        return false;
    }

    @Override
    public MultiLayerApprovalProcess findById(Long id) {
        return null;
    }

    @Override
    public List<MultiLayerApprovalProcess> findAll() {
        return null;
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }

//    public List<Map<String, Object>> getApprovalProcessList(Long companyId, String approvalFeature) {
//        List<Map<String, Object>> approvalActorList = multiLayerApprovalPathService.getApprovalActorList(companyId, approvalFeature);
//        List<Map<String, Object>> approvalProcessList = new ArrayList<Map<String, Object>>();
//        List<Map<String, Object>> pendingApprovalListAfterFirstLevel = new ArrayList<Map<String, Object>>();
//        List<Map<String, Object>> pendingApprovalList = new ArrayList<Map<String, Object>>();
//        Map<String, Object> levelMap = new HashMap<>();
//        for (Map<String, Object> approvalActorMap : approvalActorList) {
//            Integer level = Integer.valueOf(approvalActorMap.get("level").toString());
//            String approvalActor = approvalActorMap.get("approvalActor").toString();
//            Long approvalStepId = Long.parseLong(approvalActorMap.get("approvalStepId").toString());
//            Long multiLayerApprovalPathId = Long.parseLong(approvalActorMap.get("multiLayerApprovalPathId").toString());
//            Long approvalActorId = approvalActorMap.get("approvalActorId") != null ? Long.parseLong(approvalActorMap.get("approvalActorId").toString()) : null;
//            List<Long> soList = new ArrayList<>();
//
//            if (!levelMap.containsValue(level)) {
//
//                if (level.equals(0)) {
//                    if (approvalActor.equals(ApprovalActor.LOCATION_TYPE.toString())) {
//                        Long locationId = Long.parseLong(approvalActorMap.get("locationId").toString());
//                        soList = locationManagerMapService.getSalesOfficerListFromUserLoginIdOrLocationId(
//                                null, locationId, companyId);
//                    } else if (approvalActor.equals(ApprovalActor.DEPOT_IN_CHARGE.toString())) {
//                        List<Long> locationIds = Arrays
//                                .stream(approvalActorMap.get("locationIds").toString().split(","))
//                                .map(Long::parseLong)
//                                .collect(Collectors.toList());
//                        for (Long locationId : locationIds) {
//                            List<Long> salesOfficerList = locationManagerMapService.getSalesOfficerListFromUserLoginIdOrLocationId(
//                                    null, locationId, companyId);
//                            if (salesOfficerList != null)
//                                soList.addAll(locationManagerMapService.getSalesOfficerListFromUserLoginIdOrLocationId(
//                                        null, locationId, companyId));
//                        }
//                    }
//                    if (approvalFeature.equals(ApprovalFeature.SALES_BOOKING.toString())) {
//                        approvalProcessList.addAll(salesBookingService.getPendingListForApproval(companyId, soList,
//                                approvalActor, level, approvalStepId, multiLayerApprovalPathId, approvalActorId));
//                    } else if (approvalFeature.equals(ApprovalFeature.CREDIT_DEBIT_NOTE.toString())) {
//                        List<Long> distributorList = distributorService.getDistributorListOfSo(soList, companyId);
//                        approvalProcessList.addAll(creditDebitNoteService.getPendingListForApproval(companyId, approvalActor,
//                                ApprovalStatus.PENDING.toString(), level, approvalStepId,
//                                multiLayerApprovalPathId, approvalActorId, distributorList));
//                    } else if (approvalFeature.equals(ApprovalFeature.CREDIT_LIMIT.toString())) {
//                        approvalProcessList.addAll(creditLimitProposalService.getPendingListForApproval(companyId, soList,
//                                approvalActor, ApprovalStatus.PENDING.toString(), level, approvalStepId,
//                                multiLayerApprovalPathId, approvalActorId));
//                    } else if (approvalFeature.equals(ApprovalFeature.SALES_RETURN.toString())) {
//                        approvalProcessList.addAll(salesReturnProposalService.getPendingListForApproval(companyId, soList,
//                                approvalActor, ApprovalStatus.PENDING.toString(), level, approvalStepId, multiLayerApprovalPathId, approvalActorId));
//                    } else if (approvalFeature.equals(ApprovalFeature.SALES_BUDGET.toString())) {
//                        approvalProcessList.addAll(salesBudgetService.getPendingListForApproval(companyId,
//                                approvalActor, ApprovalStatus.PENDING.toString(), level, approvalStepId, multiLayerApprovalPathId, approvalActorId));
//                    } else if (approvalFeature.equals(ApprovalFeature.COLLECTION_BUDGET.toString())) {
//                        approvalProcessList.addAll(collectionBudgetService.getPendingListForApproval(companyId,
//                                approvalActor, ApprovalStatus.PENDING.toString(), level, approvalStepId, multiLayerApprovalPathId, approvalActorId));
//                    }
//                    for (Map<String, Object> approvalProcess : approvalProcessList) {
//                        Map<String, Object> pendingMap = new HashMap<>();
//                        pendingMap.put("approvalStatus", ApprovalStatus.PENDING.getName());
//                        pendingMap.putAll(approvalProcess);
//                        pendingApprovalList.add(pendingMap);
//                    }
//                    //pendingApprovalList.addAll(approvalProcessList);
//                }
//
//                else if (!level.equals(0)) {
//                    if (approvalActor.equals(ApprovalActor.LOCATION_TYPE.toString())) {
//                        Long locationId = Long.parseLong(approvalActorMap.get("locationId").toString());
//                        soList = locationManagerMapService.getSalesOfficerListFromUserLoginIdOrLocationId(
//                                null, locationId, companyId);
//                    } else if (approvalActor.equals(ApprovalActor.DEPOT_IN_CHARGE.toString())) {
//                        List<Long> locationIds = Arrays
//                                .stream(approvalActorMap.get("locationIds").toString().split(","))
//                                .map(Long::parseLong)
//                                .collect(Collectors.toList());
//                        for (Long locationId : locationIds) {
//                            List<Long> salesOfficerList =
//                                    locationManagerMapService.getSalesOfficerListFromUserLoginIdOrLocationId(
//                                            null, locationId, companyId);
//                            if (salesOfficerList != null)
//                                soList.addAll(locationManagerMapService.getSalesOfficerListFromUserLoginIdOrLocationId(
//                                        null, locationId, companyId));
//                        }
//                    }
//
//                    if (approvalFeature.equals(ApprovalFeature.SALES_BOOKING.toString())) {
//                        pendingApprovalListAfterFirstLevel.addAll(getSalesBookingPendingListForApproval(
//                                companyId, soList, approvalActor, level,
//                                approvalStepId, multiLayerApprovalPathId, approvalActorId));
//
//                        for (Map<String, Object> approvalProcess : pendingApprovalListAfterFirstLevel) {
//                            Map<String, Object> pendingMap = new HashMap<>();
//                            List<Object>
//                                    dataList =
//                                    multiLayerApprovalProcessRepository.
//                                            findByRefTableAndRefIdAndCompanyIdAndIsDeletedFalse
//                                                    ("SalesBooking", Long.parseLong(approvalProcess.get("bookingId").toString()),
//                                                            companyId);
//                            if (Integer.parseInt(approvalProcess.get("level").toString()) == dataList.size()) {
//                                pendingMap.put("approvalStatus", ApprovalStatus.AUTHORIZATION_FLOW.getName());
//                                pendingMap.putAll(approvalProcess);
//                                pendingApprovalList.add(pendingMap);
//                            }
//                        }
//                    }
//
//                    else if (approvalFeature.equals(ApprovalFeature.CREDIT_DEBIT_NOTE.toString())) {
//                        pendingApprovalList = pendingApprovalListCreditDebitNoteAfterFirstLevel(companyId, soList,
//                                approvalActor, level, approvalStepId, approvalActorId, multiLayerApprovalPathId );
//                    }
//
//                    else if (approvalFeature.equals(ApprovalFeature.CREDIT_LIMIT.toString())) {
//                        pendingApprovalList = pendingApprovalListCreditLimitAfterFirstLevel(companyId, soList,
//                                approvalActor, level, approvalStepId, approvalActorId, multiLayerApprovalPathId );
//                    }
//
//                    else if (approvalFeature.equals(ApprovalFeature.SALES_RETURN.toString())) {
//                        pendingApprovalList = pendingApprovalListSalesReturnAfterFirstLevel(companyId, soList,
//                                approvalActor, level, approvalStepId, approvalActorId, multiLayerApprovalPathId );
//                    }
//
//                    else if (approvalFeature.equals(ApprovalFeature.SALES_BUDGET.toString())) {
//                        pendingApprovalList = pendingApprovalListSalesBudgetAfterFirstLevel(companyId,
//                                approvalActor, level, approvalStepId, approvalActorId,
//                                multiLayerApprovalPathId );
//                    }
//
//                    else if (approvalFeature.equals(ApprovalFeature.COLLECTION_BUDGET.toString())) {
//                        pendingApprovalList = pendingApprovalListCollectionBudgetAfterFirstLevel(companyId,
//                                approvalActor, level, approvalStepId, approvalActorId,
//                                multiLayerApprovalPathId );
//                    }
//
//                    levelMap.put("level"+level, level);
//                }
//            }
//        }
//        pendingApprovalList.sort(Comparator.nullsLast(Comparator.comparing(m ->
//                        m.get("approval_status").toString(),
//                Comparator.nullsLast(Comparator.naturalOrder()))));
//
//        return pendingApprovalList;
//    }
//
//    public List<Map<String, Object>> getSalesBookingPendingListForApproval(Long companyId, List<Long> soList, String approvalActor, Integer level, Long approvalStepId, Long multiLayerApprovalPathId, Long approvalActorId) {
//        return multiLayerApprovalProcessRepository.getPendingListForApproval(
//                companyId, soList, ApprovalStatus.AUTHORIZATION_FLOW.toString(), approvalActor, level, approvalStepId, multiLayerApprovalPathId, approvalActorId);
//    }
//
//    private List<Map<String, Object>> pendingApprovalListCreditDebitNoteAfterFirstLevel(
//            Long companyId, List<Long> soList, String approvalActor,
//            Integer level, Long approvalStepId, Long approvalActorId, Long multiLayerApprovalPathId) {
//        List<Map<String, Object>> pendingApprovalListAfterFirstLevel = new ArrayList<Map<String, Object>>();
//        List<Map<String, Object>> pendingApprovalList = new ArrayList<Map<String, Object>>();
//        List<Long> distributorList = distributorService.getDistributorListOfSo(soList, companyId);
//
//        pendingApprovalListAfterFirstLevel.addAll(
//                creditDebitNoteService.getPendingListForApproval(
//                        companyId, approvalActor, ApprovalStatus.AUTHORIZATION_FLOW.toString(), level,
//                        approvalStepId, multiLayerApprovalPathId, approvalActorId, distributorList));
//
//        for (Map<String, Object> approvalProcess : pendingApprovalListAfterFirstLevel) {
//            Map<String, Object> pendingMap = new HashMap<>();
//            List<Object>
//                    dataList =
//                    multiLayerApprovalProcessRepository.
//                            findByRefTableAndRefIdAndCompanyIdAndIsDeletedFalse
//                                    ("CreditDebitNote",
//                                            Long.parseLong(approvalProcess.get("creditDebitNoteId").toString()),
//                                            companyId);
//            if (Integer.parseInt(approvalProcess.get("level").toString()) == dataList.size()) {
//                approvalProcess.put("approvalStatus", ApprovalStatus.AUTHORIZATION_FLOW.getName());
//                pendingMap.putAll(approvalProcess);
//                pendingApprovalList.add(pendingMap);
//            }
//        }
//        return pendingApprovalList;
//    }
//
//    private List<Map<String, Object>> pendingApprovalListCreditLimitAfterFirstLevel(
//            Long companyId, List<Long> soList, String approvalActor,
//            Integer level, Long approvalStepId, Long approvalActorId, Long multiLayerApprovalPathId) {
//        List<Map<String, Object>> pendingApprovalListAfterFirstLevel = new ArrayList<Map<String, Object>>();
//        List<Map<String, Object>> pendingApprovalList = new ArrayList<Map<String, Object>>();
//
//        pendingApprovalListAfterFirstLevel.addAll(
//                creditLimitProposalService.getPendingListForApproval(companyId,
//                        soList, approvalActor, ApprovalStatus.AUTHORIZATION_FLOW.toString(), level, approvalStepId, multiLayerApprovalPathId,
//                        approvalActorId));
//
//        for (Map<String, Object> approvalProcess : pendingApprovalListAfterFirstLevel) {
//            Map<String, Object> pendingMap = new HashMap<>();
//            List<Object>
//                    dataList =
//                    multiLayerApprovalProcessRepository.
//                            findByRefTableAndRefIdAndCompanyIdAndIsDeletedFalse
//                                    ("CreditLimitProposal",
//                                            Long.parseLong(approvalProcess.get("proposalId").toString()),
//                                            companyId);
//            if (Integer.parseInt(approvalProcess.get("level").toString()) == dataList.size()) {
//                pendingMap.put("approvalStatus", ApprovalStatus.AUTHORIZATION_FLOW.getName());
//                pendingMap.putAll(approvalProcess);
//                pendingApprovalList.add(pendingMap);
//            }
//        }
//        return pendingApprovalList;
//    }
//
//    private List<Map<String, Object>> pendingApprovalListSalesReturnAfterFirstLevel(
//            Long companyId, List<Long> soList, String approvalActor,
//            Integer level, Long approvalStepId, Long approvalActorId, Long multiLayerApprovalPathId) {
//        List<Map<String, Object>> pendingApprovalListAfterFirstLevel = new ArrayList<Map<String, Object>>();
//        List<Map<String, Object>> pendingApprovalList = new ArrayList<Map<String, Object>>();
//
//        pendingApprovalListAfterFirstLevel.addAll(salesReturnProposalService.getPendingListForApproval(companyId, soList,
//                approvalActor, ApprovalStatus.AUTHORIZATION_FLOW.toString(), level,
//                approvalStepId, multiLayerApprovalPathId, approvalActorId));
//
//        for (Map<String, Object> approvalProcess : pendingApprovalListAfterFirstLevel) {
//            Map<String, Object> pendingMap = new HashMap<>();
//            List<Object>
//                    dataList =
//                    multiLayerApprovalProcessRepository.
//                            findByRefTableAndRefIdAndCompanyIdAndIsDeletedFalse
//                                    ("SalesReturn",
//                                            Long.parseLong(approvalProcess.get("id").toString()),
//                                            companyId);
//            if (Integer.parseInt(approvalProcess.get("level").toString()) == dataList.size()) {
//                pendingMap.put("approvalStatus", ApprovalStatus.AUTHORIZATION_FLOW.getName());
//                pendingMap.putAll(approvalProcess);
//                pendingApprovalList.add(pendingMap);
//            }
//        }
//        return pendingApprovalList;
//    }
//
//    private List<Map<String, Object>> pendingApprovalListSalesBudgetAfterFirstLevel(
//            Long companyId, String approvalActor, Integer level,
//            Long approvalStepId, Long approvalActorId, Long multiLayerApprovalPathId) {
//        List<Map<String, Object>> pendingApprovalListAfterFirstLevel =
//                new ArrayList<Map<String, Object>>();
//        List<Map<String, Object>> pendingApprovalList =
//                new ArrayList<Map<String, Object>>();
//
//        pendingApprovalListAfterFirstLevel.addAll(salesBudgetService.getPendingListForApproval(companyId,
//                approvalActor, ApprovalStatus.AUTHORIZATION_FLOW.toString(), level,
//                approvalStepId, multiLayerApprovalPathId, approvalActorId));
//
//        for (Map<String, Object> approvalProcess : pendingApprovalListAfterFirstLevel) {
//            Map<String, Object> pendingMap = new HashMap<>();
//            List<Object>
//                    dataList =
//                    multiLayerApprovalProcessRepository.
//                            findByRefTableAndRefIdAndCompanyIdAndIsDeletedFalse
//                                    ("SalesBudget",
//                                            Long.parseLong(approvalProcess.get("salesBudgetId").toString()),
//                                            companyId);
//            if (Integer.parseInt(approvalProcess.get("level").toString()) == dataList.size()) {
//                pendingMap.put("approvalStatus", ApprovalStatus.AUTHORIZATION_FLOW.getName());
//                pendingMap.putAll(approvalProcess);
//                pendingApprovalList.add(pendingMap);
//            }
//        }
//        return pendingApprovalList;
//    }
//
//    private List<Map<String, Object>> pendingApprovalListCollectionBudgetAfterFirstLevel(
//            Long companyId, String approvalActor, Integer level,
//            Long approvalStepId, Long approvalActorId, Long multiLayerApprovalPathId) {
//        List<Map<String, Object>> pendingApprovalListAfterFirstLevel =
//                new ArrayList<Map<String, Object>>();
//        List<Map<String, Object>> pendingApprovalList =
//                new ArrayList<Map<String, Object>>();
//
//        pendingApprovalListAfterFirstLevel.addAll(collectionBudgetService.getPendingListForApproval(companyId,
//                approvalActor, ApprovalStatus.AUTHORIZATION_FLOW.toString(), level, approvalStepId,
//                multiLayerApprovalPathId, approvalActorId));
//
//        for (Map<String, Object> approvalProcess : pendingApprovalListAfterFirstLevel) {
//            Map<String, Object> pendingMap = new HashMap<>();
//            List<Object>
//                    dataList =
//                    multiLayerApprovalProcessRepository.
//                            findByRefTableAndRefIdAndCompanyIdAndIsDeletedFalse
//                                    ("CollectionBudget",
//                                            Long.parseLong(approvalProcess.get("collectionBudgetId").toString()),
//                                            companyId);
//            if (Integer.parseInt(approvalProcess.get("level").toString()) == dataList.size()) {
//                pendingMap.put("approvalStatus", ApprovalStatus.AUTHORIZATION_FLOW.getName());
//                pendingMap.putAll(approvalProcess);
//                pendingApprovalList.add(pendingMap);
//            }
//        }
//        return pendingApprovalList;
//    }


}
