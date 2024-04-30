package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.dto.InvoiceNatureDto;
import com.newgen.ntlsnc.globalsettings.entity.InvoiceNature;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.repository.InvoiceNatureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author kamal
 */

@Service
public class InvoiceNatureService implements IService<InvoiceNature> {
    @Autowired
    InvoiceNatureRepository invoiceNatureRepository;
    @Autowired
    OrganizationService organizationService;


    @Transactional
    @Override
    public InvoiceNature create(Object object) {

        InvoiceNatureDto invoiceNatureDto = (InvoiceNatureDto) object;
        InvoiceNature invoiceNature = new InvoiceNature();
        invoiceNature.setName(invoiceNatureDto.getName());
        invoiceNature.setDescription(invoiceNatureDto.getDescription());
        invoiceNature.setOrganization(organizationService.getOrganizationFromLoginUser());
        if (!this.validate(invoiceNature)) {
            return null;
        }
        return invoiceNatureRepository.save(invoiceNature);
    }

    @Override
    @Transactional
    public InvoiceNature update(Long id, Object object) {
        InvoiceNatureDto invoiceNatureDto = (InvoiceNatureDto) object;
        Optional<InvoiceNature> optionalInvoiceNature = invoiceNatureRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(invoiceNatureDto.getId());

        try {
            if (!optionalInvoiceNature.isPresent()) {
                throw new Exception("Invoice Nature Not exist");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        InvoiceNature invoiceNature = optionalInvoiceNature.get();
        invoiceNature.setName(invoiceNatureDto.getName());
        invoiceNature.setDescription(invoiceNatureDto.getDescription());
        invoiceNature.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (!this.validate(invoiceNature)) {
            return null;
        }
        return invoiceNatureRepository.save(invoiceNature);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {

        try {
            Optional<InvoiceNature> optionalInvoiceNature = invoiceNatureRepository.findById(id);
            if (!optionalInvoiceNature.isPresent()) {
                throw new Exception("Invoice Nature Not exist");
            }
            InvoiceNature invoiceNature = optionalInvoiceNature.get();
            invoiceNature.setIsDeleted(true);
            invoiceNatureRepository.save(invoiceNature);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public InvoiceNature findById(Long id) {
        try {
            Optional<InvoiceNature> optionalInvoiceNature = invoiceNatureRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalInvoiceNature.isPresent()) {
                throw new Exception("Invoice Nature Not exist with id " + id);
            }
            return optionalInvoiceNature.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<InvoiceNature> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return invoiceNatureRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }
}
