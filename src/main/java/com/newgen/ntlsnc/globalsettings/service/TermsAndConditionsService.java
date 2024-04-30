package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.dto.TermsAndConditionsDto;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.TermsAndConditions;
import com.newgen.ntlsnc.globalsettings.entity.Vehicle;
import com.newgen.ntlsnc.globalsettings.repository.TermsAndConditionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author kamal
 * @Date ১৯/৪/২২
 */

@Service
public class TermsAndConditionsService implements IService<TermsAndConditions> {
    @Autowired
    TermsAndConditionsRepository termsAndConditionsRepository;
    @Autowired
    OrganizationService organizationService;

    @Transactional
    @Override
    public TermsAndConditions create(Object object) {
        TermsAndConditionsDto termsAndConditionsDto = (TermsAndConditionsDto) object;
        TermsAndConditions termsAndConditions = new TermsAndConditions();
        termsAndConditions.setTermsAndConditions(termsAndConditionsDto.getTermsAndConditions());
        termsAndConditions.setCompany(organizationService.findById(termsAndConditionsDto.getCompanyId()));

        termsAndConditions.setOrganization(organizationService.getOrganizationFromLoginUser());
        if (!this.validate(termsAndConditions)) {
            return null;
        }
        return termsAndConditionsRepository.save(termsAndConditions);
    }

    @Transactional
    @Override
    public TermsAndConditions update(Long id, Object object) {
        TermsAndConditionsDto termsAndConditionsDto = (TermsAndConditionsDto) object;
        Optional<TermsAndConditions> optionalTermsAndConditions = termsAndConditionsRepository.findById(termsAndConditionsDto.getId());
        if (!optionalTermsAndConditions.isPresent()) {
            return null;
        }
        TermsAndConditions termsAndConditions = optionalTermsAndConditions.get();
        termsAndConditions.setTermsAndConditions(termsAndConditionsDto.getTermsAndConditions());
        termsAndConditions.setCompany(organizationService.findById(termsAndConditionsDto.getCompanyId()));
        termsAndConditions.setOrganization(organizationService.getOrganizationFromLoginUser());
        if (!this.validate(termsAndConditions)) {
            return null;
        }
        return termsAndConditionsRepository.save(termsAndConditions);
    }

    @Transactional
    @Override
    public boolean delete(Long id) {

        try {
            Optional<TermsAndConditions> optionalTermsAndConditions = termsAndConditionsRepository.findById(id);
            if (!optionalTermsAndConditions.isPresent()) {
                throw new Exception("Terms And Conditions Not exist");
            }
            TermsAndConditions termsAndConditions = optionalTermsAndConditions.get();
            termsAndConditions.setIsDeleted(true);
            termsAndConditionsRepository.save(termsAndConditions);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public TermsAndConditions findById(Long id) {
        try {
            Optional<TermsAndConditions> optionalTermsAndConditions = termsAndConditionsRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalTermsAndConditions.isPresent()) {
                throw new Exception("Terms and Condition Not exist with id " + id);
            }
            return optionalTermsAndConditions.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<TermsAndConditions> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return termsAndConditionsRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }



    public TermsAndConditions findByCompanyId(Long companyId) {
        try {
            Optional<TermsAndConditions> optionalTermsAndConditions = termsAndConditionsRepository.findByCompanyIdAndIsActiveTrueAndIsDeletedFalse(companyId);
            return optionalTermsAndConditions.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }



    @Override
    public boolean validate(Object object) {
        TermsAndConditions termsAndConditions = (TermsAndConditions) object;
        Optional<TermsAndConditions> optionalTermsAndConditions = Optional.empty();

        if (termsAndConditions.getId() == null) {
            optionalTermsAndConditions = termsAndConditionsRepository.findByCompanyIdAndIsDeletedFalse(
                    termsAndConditions.getCompany().getId());
        }

        else {
            optionalTermsAndConditions = termsAndConditionsRepository.findByCompanyIdAndIdIsNotAndIsDeletedFalse(
                    termsAndConditions.getCompany().getId(), termsAndConditions.getId());
        }

        if (optionalTermsAndConditions.isPresent()) {
            throw new RuntimeException("Company name already exist.");
        }

        return true;
    }
}
