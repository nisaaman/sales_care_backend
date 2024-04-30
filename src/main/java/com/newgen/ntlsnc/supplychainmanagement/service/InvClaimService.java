package com.newgen.ntlsnc.supplychainmanagement.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.common.enums.InvReturnType;
import com.newgen.ntlsnc.common.enums.QaStatus;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.Product;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.globalsettings.service.ProductService;
import com.newgen.ntlsnc.supplychainmanagement.dto.InvClaimDetailsDto;
import com.newgen.ntlsnc.supplychainmanagement.dto.InvClaimDto;
import com.newgen.ntlsnc.supplychainmanagement.dto.InvDamageDetailsDto;
import com.newgen.ntlsnc.supplychainmanagement.dto.InvTransactionDetailsDto;
import com.newgen.ntlsnc.supplychainmanagement.entity.*;
import com.newgen.ntlsnc.supplychainmanagement.repository.InvClaimDetailsRepository;
import com.newgen.ntlsnc.supplychainmanagement.repository.InvClaimRepository;
import com.newgen.ntlsnc.supplychainmanagement.repository.InvTransactionDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author sunipa
 * @date ২১/৯/২২
 */
@Service
public class InvClaimService implements IService<InvClaim> {
    @Autowired
    OrganizationService organizationService;
    @Autowired
    InvClaimRepository invClaimRepository;
    @Autowired
    InvClaimDetailsRepository invClaimDetailsRepository;
    @Autowired
    InvTransactionService invTransactionService;
    @Autowired
    InvTransactionDetailsRepository invTransactionDetailsRepository;
    @Autowired
    BatchService batchService;
    @Autowired
    ProductService productService;
    @Autowired
    InvTransferService invTransferService;

    @Transactional
    @Override
    public InvClaim create(Object object) {

        InvClaimDto invClaimDto = (InvClaimDto) object;
        InvClaim invClaim = new InvClaim();

        if (null == invClaimDto.getClaimDate()) {
            invClaim.setClaimDate( LocalDate.now());
        } else
            invClaim.setClaimDate(LocalDate.parse(invClaimDto.getClaimDate()));

        invClaim.setNotes(invClaimDto.getNotes());
        invClaim.setApprovalStatus(ApprovalStatus.PENDING);

        invClaim.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (invClaimDto.getCompanyId() != null) {
            invClaim.setCompany(organizationService.findById(invClaimDto.getCompanyId()));
        }
        invClaim.setInvTransaction(invTransactionService.findById(invClaimDto.getInvTransactionId()));
        invClaim.setInvTransfer(invTransferService.findById(invClaimDto.getInvTransferId()));

        invClaim = invClaimRepository.save(invClaim);

        List<InvClaimDetails> invClaimDetailsList =
               getInvClaimDetailsList(invClaimDto.getInvClaimDetailsDto(), invClaim);

        invClaimDetailsRepository.saveAll(invClaimDetailsList);

        return invClaim;
    }

    @Override
    public InvClaim update(Long id, Object object) {
        return null;
    }

    @Override
    public boolean delete(Long id) {
        return false;
    }

    @Override
    public InvClaim findById(Long id) {
        try {
            Optional<InvClaim> optionalInvDamage =
                    invClaimRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalInvDamage.isPresent()) {
                throw new Exception("Inv Damage Not exist with id " + id);
            }
            return optionalInvDamage.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<InvClaim> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return invClaimRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    public Optional<InvClaim> findClaimByInvTransaction(Long invTransactionId) {
        return invClaimRepository.findByInvTransactionIdAndIsActiveTrueAndIsDeletedFalse(invTransactionId);
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }
    @Transactional
    private List<InvClaimDetails> getInvClaimDetailsList(
            List<InvClaimDetailsDto> invClaimDetailsDtoList, InvClaim invClaim) {

        List<InvClaimDetails> invClaimDetailsList = new ArrayList<>();

        for (InvClaimDetailsDto detailsDto : invClaimDetailsDtoList) {
            InvClaimDetails invClaimDetails = new InvClaimDetails();
            InvTransactionDetails invTransactionDetails = new InvTransactionDetails();
            Product product = productService.findById(detailsDto.getProductId());

            if (detailsDto.getId() != null) {
                invTransactionDetails =
                        invTransactionDetailsRepository.findById(detailsDto.getId()).get();
            }
            if (detailsDto.getClaimQuantity() != null) {
                invClaimDetails.setQuantity(detailsDto.getClaimQuantity());
                invClaimDetails.setQuantityInUom((float)
                        (product.getItemSize() * detailsDto.getClaimQuantity()));
            }

           if (detailsDto.getProductId() != null) {
                invClaimDetails.setProduct(product);
            }

           if (detailsDto.getBatchId() != null) {
                invClaimDetails.setBatch(batchService.findById(detailsDto.getBatchId()));
            }

            if (detailsDto.getClaimTypeId() != null) {
                invClaimDetails.setClaimType(InvReturnType.valueOf(detailsDto.getClaimTypeId()));
            }

            invClaimDetails.setInvClaim(invClaim);
            invClaimDetails.setInvTransactionDetails(invTransactionDetails);
            invClaimDetails.setOrganization(organizationService.getOrganizationFromLoginUser());
            invClaimDetailsList.add(invClaimDetails);

        }
        return invClaimDetailsList;
    }

}
