package com.newgen.ntlsnc.supplychainmanagement.service;

import com.newgen.ntlsnc.common.CommonConstant;
import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.common.enums.QaStatus;
import com.newgen.ntlsnc.common.enums.StoreType;
import com.newgen.ntlsnc.globalsettings.entity.AccountingYear;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.service.*;
import com.newgen.ntlsnc.supplychainmanagement.dto.InvDamageDetailsDto;
import com.newgen.ntlsnc.supplychainmanagement.dto.InvDamageDto;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvDamage;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvDamageDetails;
import com.newgen.ntlsnc.supplychainmanagement.repository.InvDamageDetailsRepository;
import com.newgen.ntlsnc.supplychainmanagement.repository.InvDamageRepository;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author anika
 * @Date ৩১/৫/২২
 */
@Service
public class InvDamageService implements IService<InvDamage> {
    @Autowired
    OrganizationService organizationService;
    @Autowired
    StoreService storeService;
    @Autowired
    DepotService depotService;
    @Autowired
    InvDamageRepository invDamageRepository;
    @Autowired
    InvDamageDetailsRepository invDamageDetailsRepository;
    @Autowired
    ProductService productService;
    @Autowired
    BatchService batchService;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    DocumentSequenceService documentSequenceService;
    @Autowired
    AccountingYearService accountingYearService;

    @Override
    @Transactional
    public InvDamage create(Object object) {
        try {
            InvDamageDto invDamageDto = (InvDamageDto) object;
            InvDamage invDamage = new InvDamage();

            invDamage.setDamageNo(documentSequenceService.getSequenceByDocumentId(
                    CommonConstant.DOCUMENT_ID_FOR_DAMAGE_DECLARATION));
            invDamage.setDeclarationDate(LocalDate.now());
            invDamage.setReason(invDamageDto.getReason());
            invDamage.setNotes(invDamageDto.getNotes());
//            invDamage.setApprovalStatus(ApprovalStatus.APPROVED); //TODO need to change after approval dev done.
            invDamage.setOrganization(organizationService.getOrganizationFromLoginUser());

            Map depotMap = depotService.getDepotByLoginUserId(invDamageDto.getCompanyId(),
                    applicationUserService.getApplicationUserIdFromLoginUser());

            if(depotMap.size() > 0){
                Long depotId = Long.parseLong(String.valueOf(depotMap.get("id")));
                invDamage.setDepot(depotService.findById(depotId));
            } else {
                throw new RuntimeException("Deport not found.");
            }

            invDamage.setCompany(organizationService.findById(invDamageDto.getCompanyId()));
            invDamage.setStore(storeService.getStoreByOrganizationAndStoreType(
                    organizationService.getOrganizationFromLoginUser(), StoreType.valueOf(invDamageDto.getStoreType())));

            invDamage = invDamageRepository.save(invDamage);

            List<InvDamageDetails> invDamageDetailsList = getInvDamageDetailsList(invDamageDto.getDamageDetailsList(), invDamage);
            invDamageDetailsRepository.saveAll(invDamageDetailsList);

            return invDamage;
        }catch (Exception ex){
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public InvDamage update(Long id, Object object) {

        InvDamageDto invDamageDto = (InvDamageDto) object;
        InvDamage invDamage = this.findById(invDamageDto.getId());

        invDamage.setDeclarationDate(LocalDate.parse(invDamageDto.getDeclarationDate()));
        invDamage.setReason(invDamageDto.getReason());
        invDamage.setNotes(invDamageDto.getNotes());
//        invDamage.setApprovalStatus(ApprovalStatus.PENDING);
        invDamage.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (invDamageDto.getCompanyId() != null) {
            invDamage.setCompany(organizationService.findById(invDamageDto.getCompanyId()));
        }
        if (invDamageDto.getStoreId() != null) {
            invDamage.setStore(storeService.findById(invDamageDto.getCompanyId()));
        }
        if (invDamageDto.getDepotId() != null) {
            invDamage.setDepot(depotService.findById(invDamageDto.getDepotId()));
        }

        invDamage = invDamageRepository.save(invDamage);
        List<InvDamageDetails> invDamageDetailsList = getInvDamageDetailsList(invDamageDto.getInvDamageDetailsDtoList(), invDamage);
        invDamageDetailsRepository.saveAll(invDamageDetailsList);

        return invDamage;
    }

    @Override
    public boolean delete(Long id) {
        Optional<InvDamage> optionalInvDamage = invDamageRepository.findById(id);
        try {
            if (!optionalInvDamage.isPresent()) {
                throw new Exception("Credit Limit Not exist");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        InvDamage invDamage = optionalInvDamage.get();
        invDamage.setIsDeleted(true);
        invDamageRepository.save(invDamage);

        List<InvDamageDetails> invDamageDetailsList = invDamageDetailsRepository.findAllByInvDamage(invDamage);
        invDamageDetailsList.forEach(invDamageDetails -> invDamageDetails.setIsDeleted(true));
        invDamageDetailsRepository.saveAll(invDamageDetailsList);

        return true;
    }

//    @Override
//    public InvDamage findById(Long id) {
//        return invDamageRepository.findById(id).orElse(null);
//    }

    @Override
    public InvDamage findById(Long id) {
        try {
            Optional<InvDamage> optionalInvDamage = invDamageRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalInvDamage.isPresent()) {
                throw new Exception("Damage Not exist with id " + id);
            }
            return optionalInvDamage.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<InvDamage> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return invDamageRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }

    private List<InvDamageDetails> getInvDamageDetailsList(List<InvDamageDetailsDto> invDamageDetailsDtoList, InvDamage invDamage) {
        List<InvDamageDetails> invDamageDetailsList = new ArrayList<>();

        for (InvDamageDetailsDto damageDetailsDto : invDamageDetailsDtoList) {
            InvDamageDetails invDamageDetails = new InvDamageDetails();

            if (damageDetailsDto.getId() != null) {
                invDamageDetails = invDamageDetailsRepository.findById(damageDetailsDto.getId()).get();
            }

            invDamageDetails.setQuantity(damageDetailsDto.getDamageQty());
            invDamageDetails.setQuantityInUom(damageDetailsDto.getQuantityInUom());
            invDamageDetails.setRate(damageDetailsDto.getWar());
//            invDamageDetails.setQaDate(LocalDateTime.parse(damageDetailsDto.getQaDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//            invDamageDetails.setQaStatus(QaStatus.IN_PROGRESS);

            if (damageDetailsDto.getProductId() != null) {
                invDamageDetails.setProduct(productService.findById(damageDetailsDto.getProductId()));
            }

            if (damageDetailsDto.getBatchId() != null) {
                invDamageDetails.setBatch(batchService.findById(damageDetailsDto.getBatchId()));
            }

            /*if (damageDetailsDto.getQaById() != null) {
                invDamageDetails.setQaBy(applicationUserService.findById(damageDetailsDto.getQaById()));
            }*/

            invDamageDetails.setOrganization(organizationService.getOrganizationFromLoginUser());
            invDamageDetails.setInvDamage(invDamage);

            invDamageDetailsList.add(invDamageDetails);

        }
        return invDamageDetailsList;
    }

    public List<Map<String, String>> getDamageDeclarationList(
            Long companyId, String fiscalYearId) {
        try {
            LocalDate fromDate = null;
            LocalDate toDate = null;
            Long depotId = null;

            if(!fiscalYearId.equals("")){
                long id =  Long.parseLong(fiscalYearId);
                AccountingYear accountingYear = accountingYearService.findById(id);
                if(accountingYear != null){
                    fromDate = accountingYear.getStartDate();
                    toDate = accountingYear.getEndDate();
                }
            }

            Map depotMap = depotService.getDepotByLoginUserId(companyId,
                    applicationUserService.getApplicationUserIdFromLoginUser());

            if(depotMap.size() > 0){
                depotId = Long.parseLong(String.valueOf(depotMap.get("id")));
            } else {
                return new ArrayList<>();
            }

            return invDamageRepository.getDamageDeclarationList(applicationUserService.getOrganizationIdFromLoginUser(),
                    companyId, depotId, fromDate, toDate);
        } catch(Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

}
