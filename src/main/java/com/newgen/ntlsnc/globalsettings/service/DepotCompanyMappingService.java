package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.dto.DepotCompanyMappingDto;
import com.newgen.ntlsnc.globalsettings.entity.DepotCompanyMapping;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.repository.DepotCompanyMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author anika
 * @Date ২০/৬/২২
 */
@Service
public class DepotCompanyMappingService implements IService<DepotCompanyMapping> {

    @Autowired
    OrganizationService organizationService;
    @Autowired
    DepotService depotService;
    @Autowired
    DepotCompanyMappingRepository depotCompanyMappingRepository;

    @Override
    @Transactional
    public DepotCompanyMapping create(Object object) {
        DepotCompanyMappingDto depotCompanyMappingDto = (DepotCompanyMappingDto) object;
        DepotCompanyMapping depotCompanyMapping = new DepotCompanyMapping();

        if (depotCompanyMappingDto.getCompanyId() != null) {
            depotCompanyMapping.setCompany(organizationService.findById(depotCompanyMappingDto.getCompanyId()));
        }
        if (depotCompanyMappingDto.getDepotId() != null) {
            depotCompanyMapping.setDepot(depotService.findById(depotCompanyMappingDto.getDepotId()));
        }

        return depotCompanyMappingRepository.save(depotCompanyMapping);
    }

    @Override
    @Transactional
    public DepotCompanyMapping update(Long id, Object object) {
        DepotCompanyMappingDto depotCompanyMappingDto = (DepotCompanyMappingDto) object;
        DepotCompanyMapping depotCompanyMapping = this.findById(depotCompanyMappingDto.getId());

        if (depotCompanyMappingDto.getCompanyId() != null) {
            depotCompanyMapping.setCompany(organizationService.findById(depotCompanyMappingDto.getCompanyId()));
        }
        if (depotCompanyMappingDto.getDepotId() != null) {
            depotCompanyMapping.setDepot(depotService.findById(depotCompanyMappingDto.getDepotId()));
        }

        return depotCompanyMappingRepository.save(depotCompanyMapping);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            Optional<DepotCompanyMapping> optionalDepotCompanyMapping = depotCompanyMappingRepository.findById(id);
            if (!optionalDepotCompanyMapping.isPresent()) {
                throw new Exception("Depot Company Mapping Limit Not exist");
            }
            DepotCompanyMapping depotCompanyMapping = optionalDepotCompanyMapping.get();
            depotCompanyMapping.setIsDeleted(true);
            depotCompanyMappingRepository.save(depotCompanyMapping);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public DepotCompanyMapping findById(Long id) {
        try {
            Optional<DepotCompanyMapping> optionalDepotCompanyMapping = depotCompanyMappingRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalDepotCompanyMapping.isPresent()) {
                throw new Exception("Depot Company Mapping exist with id " + id);
            }
            return optionalDepotCompanyMapping.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public List<DepotCompanyMapping> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return depotCompanyMappingRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Override
    @Transactional
    public boolean validate(Object object) {
        return true;
    }
}
