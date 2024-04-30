package com.newgen.ntlsnc.supplychainmanagement.service;

import com.newgen.ntlsnc.common.CommonConstant;
import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.InvReturnType;
import com.newgen.ntlsnc.common.enums.InvTransactionType;
import com.newgen.ntlsnc.common.enums.StoreType;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.Product;
import com.newgen.ntlsnc.globalsettings.service.*;
import com.newgen.ntlsnc.supplychainmanagement.dto.InvTransactionDetailsDto;
import com.newgen.ntlsnc.supplychainmanagement.dto.InvTransactionDto;
import com.newgen.ntlsnc.supplychainmanagement.dto.InvTransferDto;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvTransaction;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvTransactionDetails;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvTransfer;
import com.newgen.ntlsnc.supplychainmanagement.repository.InvTransactionDetailsRepository;
import com.newgen.ntlsnc.supplychainmanagement.repository.InvTransferRepository;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * @author marziah
 * @Date 17/04/22
 */

@Service
public class InvTransferService implements IService<InvTransfer> {
    @Autowired
    InvTransactionDetailsRepository invTransactionDetailsRepository;
    @Autowired
    InvTransactionService invTransactionService;
    @Autowired
    InvTransferRepository invTransferRepository;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    BatchService batchService;
    @Autowired
    StoreService storeService;
    @Autowired
    ProductService productService;
    @Autowired
    DepotService depotService;
    @Autowired
    VehicleService vehicleService;
    @Autowired
    DocumentSequenceService documentSequenceService;
    @Autowired
    AccountingYearService accountingYearService;
    @Autowired
    ApplicationUserService applicationUserService;

    @Transactional
    @Override
    public InvTransfer create(Object object) {
        InvTransferDto invTransferDto = (InvTransferDto) object;
        //====================================== InvTransaction part start =================
        InvTransactionDto invTransactionDto = invTransferDto.getInvTransactionDto();
        InvTransaction invTransaction = new InvTransaction();
        invTransaction.setTransactionDate(LocalDate.now());
        invTransaction.setTransactionType(InvTransactionType.valueOf(
                invTransferDto.getInvTransactionDto().getTransactionType()));
        invTransaction.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (invTransferDto.getCompanyId() != null) {
            invTransaction.setCompany(organizationService.findById(invTransferDto.getCompanyId()));
        }
        if (!this.validate(invTransaction)) {
            return null;
        }
        invTransaction = invTransactionService.save(invTransaction);
//====================================== InvTransaction part end =================

        InvTransfer invTransfer = new InvTransfer();
        invTransfer.setTransferNo(documentSequenceService.getSequenceByDocumentId(
                                CommonConstant.DOCUMENT_ID_FOR_INV_TRANSFER));
        invTransfer.setTransferDate(LocalDate.now()); ////yyyy-MM-dd
        invTransfer.setDriverName(invTransferDto.getDriverName());
        invTransfer.setDriverContactNo(invTransferDto.getDriverContactNo());
        invTransfer.setRemarks(invTransferDto.getRemarks());
        invTransfer.setReturnReason(invTransferDto.getReturnReason());
        if (invTransferDto.getInvReturnType() != null) {
            invTransfer.setInvReturnType(InvReturnType.valueOf(invTransferDto.getInvReturnType()));
        }
        if (invTransferDto.getInvTransferId() != null) {
            invTransfer.setInvTransfer(invTransferRepository.findById(invTransferDto.getInvTransferId()).orElse(null));
        }
        invTransfer.setInvTransaction(invTransaction);
        //invTransfer.setOrganization(invTransaction.getOrganization());
        invTransfer.setOrganization(organizationService.getOrganizationFromLoginUser());


        if (invTransferDto.getCompanyId() != null) {
            invTransfer.setCompany(organizationService.findById(invTransferDto.getCompanyId()));
        }
        if (invTransferDto.getFromDepotId() != null) {
            invTransfer.setFromDepot(depotService.findById(invTransferDto.getFromDepotId()));
        }
        if (invTransferDto.getToDepotId() != null) {
            invTransfer.setToDepot(depotService.findById(invTransferDto.getToDepotId()));
        }
        if (invTransferDto.getVehicleId() != null) {
            invTransfer.setVehicle(vehicleService.findById(invTransferDto.getVehicleId()));
        }
        if (invTransferDto.getStoreId() != null) {
            //invTransfer.setStore(storeService.findById(invTransferDto.getStoreId()));
        }
        if (!this.validate(invTransfer)) {
            return null;
        }
        invTransfer = invTransferRepository.save(invTransfer);
        List<InvTransactionDetails> invTransactionDetailsList = getInvTransactionDetails(invTransferDto.getInvTransactionDetailsDtoList(), invTransaction);
        invTransactionService.saveAllTransactionDetails(invTransactionDetailsList);

        return invTransfer;
    }

    @Transactional
    @Override
    public InvTransfer update(Long id, Object object) {
        InvTransferDto invTransferDto = (InvTransferDto) object;
        InvTransaction invTransaction = invTransactionService.findById(invTransferDto.getInvTransactionId());
        if (invTransaction == null) {
            return null;
        }
//====================================== InvTransaction part start =================
//        InvTransaction invTransaction = optionalInvTransaction;
//====================================== InvTransaction part end =================
        Optional<InvTransfer> optionalInvTransfer = invTransferRepository.findById(invTransferDto.getId());
        if (!optionalInvTransfer.isPresent()) {
            return null;
        }
        InvTransfer invTransfer = optionalInvTransfer.get();
        invTransfer.setTransferDate(LocalDate.parse(invTransferDto.getTransferDate())); ////yyyy-MM-dd
        invTransfer.setDriverName(invTransferDto.getDriverName());
        invTransfer.setDriverContactNo(invTransferDto.getDriverContactNo());
        invTransfer.setRemarks(invTransferDto.getRemarks());
       // invTransfer.setOrganization(invTransaction.getOrganization());
        invTransfer.setOrganization(organizationService.getOrganizationFromLoginUser());
        invTransfer.setReturnReason(invTransferDto.getReturnReason());

        if (invTransferDto.getInvReturnType() != null) {
            invTransfer.setInvReturnType(InvReturnType.valueOf(invTransferDto.getInvReturnType()));
        }
        if (invTransferDto.getInvTransferId() != null) {
            invTransfer.setInvTransfer(invTransferRepository.findById(invTransferDto.getInvTransferId()).orElse(null));
        }
        if (invTransferDto.getCompanyId() != null) {
            invTransfer.setCompany(organizationService.findById(invTransferDto.getCompanyId()));
        }
        if (invTransferDto.getFromDepotId() != null) {
            invTransfer.setFromDepot(depotService.findById(invTransferDto.getFromDepotId()));
        }
        if (invTransferDto.getToDepotId() != null) {
            invTransfer.setToDepot(depotService.findById(invTransferDto.getToDepotId()));
        }
        if (invTransferDto.getVehicleId() != null) {
            invTransfer.setVehicle(vehicleService.findById(invTransferDto.getVehicleId()));
        }
        if (invTransferDto.getStoreId() != null) {
            //invTransfer.setStore(storeService.findById(invTransferDto.getStoreId()));
        }
        if (!this.validate(invTransfer)) {
            return null;
        }
        invTransfer = invTransferRepository.save(invTransfer);
        List<InvTransactionDetails> invTransactionDetailsList = getInvTransactionDetails(invTransferDto.getInvTransactionDetailsDtoList(), invTransaction);
        invTransactionService.saveAllTransactionDetails(invTransactionDetailsList);
        return invTransfer;
    }

    @Transactional
    @Override
    public boolean delete(Long id) {
//        ============================================ InvReceive start=======================================
        Optional<InvTransfer> optionalInvTransfer = invTransferRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
        try {
            if (!optionalInvTransfer.isPresent()) {
                throw new Exception("Inventory Transfer not exist");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        InvTransfer invTransfer = optionalInvTransfer.get();
        invTransfer.setIsDeleted(true);
        invTransferRepository.save(invTransfer);
//        ============================================ InvReceive end =======================================
//        ============================================ InvTransaction start =======================================
        InvTransaction invTransaction = optionalInvTransfer.get().getInvTransaction();
        invTransaction.setIsDeleted(true);
        invTransactionService.save(invTransaction);
//        ============================================ InvTransaction end =======================================
        List<InvTransactionDetails> invTransactionDetailsList = invTransactionService.findAllInvTransactionDetails(id);
        invTransactionDetailsList.forEach(invTransactionDetails -> invTransactionDetails.setIsDeleted(true));
        invTransactionService.saveAllTransactionDetails(invTransactionDetailsList);

        return true;
    }

    @Override
    public InvTransfer findById(Long id) {
        try {
            Optional<InvTransfer> optionalInvTransfer = invTransferRepository.findById(id);
            if (!optionalInvTransfer.isPresent()) {
                throw new Exception("Inventory Transfer Not exist with id " + id);
            }
            return optionalInvTransfer.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<InvTransfer> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return invTransferRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }


    private List<InvTransactionDetails> getInvTransactionDetails(List<InvTransactionDetailsDto> invTransactionDetailsDtoList, InvTransaction invTransaction) {
        List<InvTransactionDetails> invTransactionDetailsList = new ArrayList<>();
        for (InvTransactionDetailsDto itdd : invTransactionDetailsDtoList) {
            InvTransactionDetails invTransactionDetails = new InvTransactionDetails();
            if (itdd.getId() != null) {
                invTransactionDetails = invTransactionDetailsRepository.findById(itdd.getId()).get();
            }
            invTransactionDetails.setQuantity(itdd.getQuantity());
            Product product = productService.findById(itdd.getProductId());
            invTransactionDetails.setQuantityInUom((float) (product.getItemSize() * itdd.getQuantity()));
            invTransactionDetails.setRate(itdd.getRate());
            // invTransactionDetails.setInvItemStatus(InvItemStatus.valueOf(itdd.getInvItemStatus()));
            //invTransactionDetails.setOrganization(invTransaction.getOrganization());
            invTransactionDetails.setOrganization(organizationService.getOrganizationFromLoginUser());
            invTransactionDetails.setInvTransaction(invTransaction);

            if (itdd.getBatchId() != null) {
                invTransactionDetails.setBatch(batchService.findById(itdd.getBatchId()));
            }
            Map regularStore = storeService.getStore(String.valueOf(StoreType.REGULAR));
            invTransactionDetails.setFromStore(storeService.findById(
                    Long.parseLong(String.valueOf(regularStore.get("id")))));

            Map inTransitStore = storeService.getStore(String.valueOf(StoreType.IN_TRANSIT));
            invTransactionDetails.setToStore(storeService.findById(
                    Long.parseLong(String.valueOf(inTransitStore.get("id")))));

            if (itdd.getProductId() != null) {
                invTransactionDetails.setProduct(product);
            }
            invTransactionDetailsList.add(invTransactionDetails);
        }
        return invTransactionDetailsList;
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }

    public List<Map<String, Object>> getInvTransferDetails(
            Long companyId, Long accountingYearId) {

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

        Map depotMap = depotService.getDepotByLoginUserId(companyId,
                applicationUserService.getApplicationUserIdFromLoginUser());
        Long depotId = !depotMap.isEmpty() ? Long.parseLong(String.valueOf(depotMap.get("id"))) : null;
        return invTransferRepository.getInvTransferDetails(companyId,
                accountingYear.get("startDate"), accountingYear.get("endDate"), depotId);
    }
}
