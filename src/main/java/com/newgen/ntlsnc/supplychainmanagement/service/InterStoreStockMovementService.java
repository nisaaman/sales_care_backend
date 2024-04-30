package com.newgen.ntlsnc.supplychainmanagement.service;

import com.newgen.ntlsnc.common.CommonConstant;
import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.globalsettings.service.AccountingYearService;
import com.newgen.ntlsnc.globalsettings.service.DepotService;
import com.newgen.ntlsnc.globalsettings.service.DocumentSequenceService;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.supplychainmanagement.dto.InterStoreStockMovementDto;
import com.newgen.ntlsnc.supplychainmanagement.entity.InterStoreStockMovement;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvTransaction;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvTransactionDetails;
import com.newgen.ntlsnc.supplychainmanagement.repository.InterStoreStockMovementRepository;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Newaz Sharif
 * @since 3rd Oct, 22
 */

@Service
public class InterStoreStockMovementService implements IService<InterStoreStockMovement> {

    @Autowired
    InvTransactionService invTransactionService;
    @Autowired
    DepotService depotService;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    InterStoreStockMovementRepository interStoreStockMovementRepository;
    @Autowired
    DocumentSequenceService documentSequenceService;
    @Autowired
    AccountingYearService accountingYearService;
    @Autowired
    ApplicationUserService applicationUserService;

    @Override
    @Transactional
    public InterStoreStockMovement create(Object object) {
        InterStoreStockMovementDto interStoreStockMovementDto = (InterStoreStockMovementDto) object;

        InvTransaction invTransaction = invTransactionService.startNewTransaction(
                                    interStoreStockMovementDto.getInvTransactionDto());

        InterStoreStockMovement interStoreStockMovement = new InterStoreStockMovement();

        interStoreStockMovement.setMovementDate(LocalDate.now());
        interStoreStockMovement.setMovementRefNo(documentSequenceService.getSequenceByDocumentId(
                CommonConstant.DOCUMENT_ID_FOR_INTER_STORE_STOCK_MOVEMENT));
        interStoreStockMovement.setNote(interStoreStockMovementDto.getNote().trim());
        interStoreStockMovement.setReason(interStoreStockMovementDto.getReason().trim());

        if (interStoreStockMovementDto.getDepotId() != null) {
            interStoreStockMovement.setDepot(depotService.findById(
                                interStoreStockMovementDto.getDepotId()));
        }

        interStoreStockMovement.setOrganization(organizationService.getOrganizationFromLoginUser());
        if (interStoreStockMovementDto.getCompanyId() != null) {
            interStoreStockMovement.setCompany(organizationService.findById(
                                interStoreStockMovementDto.getCompanyId()));
        }
        interStoreStockMovement.setApprovalStatus(ApprovalStatus.APPROVED);
        interStoreStockMovement.setInvTransaction(invTransaction);
        interStoreStockMovement.setMovementBy(applicationUserService.getApplicationUserFromLoginUser());

        interStoreStockMovement = interStoreStockMovementRepository.save(interStoreStockMovement);
        List<InvTransactionDetails> invTransactionDetailsList =
                    invTransactionService.getInvTransactionDetails(
                            interStoreStockMovementDto.getInvTransactionDetailsDtoList(),
                                                invTransaction);
        invTransactionService.saveAllTransactionDetails(invTransactionDetailsList);

        return interStoreStockMovement;
    }

    @Override
    public InterStoreStockMovement update(Long id, Object object) {
        return null;
    }

    @Override
    public boolean delete(Long id) {
        return false;
    }

    @Override
    public InterStoreStockMovement findById(Long id) {
        return null;
    }

    @Override
    public List<InterStoreStockMovement> findAll() {
        return null;
    }

    @Override
    public boolean validate(Object object) {
        return false;
    }

    public List<Map<String, Object>> getInterStoreStockMovementDetails(
            Long companyId, Long depotId, Long accountingYearId) {

        Map<String, LocalDate> accountingYear = new HashMap<>();
        if(accountingYearId == null) {
            Long accId  = accountingYearService.getCurrentAccountingYearId(
                    companyId, LocalDate.now());

            if(accId != null)
                accountingYear = accountingYearService.getAccountingYearDate(accId);
            else
                return new ArrayList<>();
        }else
            accountingYear = accountingYearService.getAccountingYearDate(
                    accountingYearId);

        return interStoreStockMovementRepository.getInterStoreStockMovementDetails(companyId,
                depotId, accountingYear.get("startDate"), accountingYear.get("endDate"));
    }

    public Map getQCStockQCInfo(Long companyId, Long depotId, Long productId,
                                 String storeType, Long batchId,String qaStatus ) {
        return interStoreStockMovementRepository.getQCStockQCInfo(companyId, depotId, productId,
                storeType, batchId, qaStatus);
    }
}
