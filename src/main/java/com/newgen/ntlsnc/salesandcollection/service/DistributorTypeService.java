package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.salesandcollection.dto.DistributorTypeDto;
import com.newgen.ntlsnc.salesandcollection.entity.DistributorType;
import com.newgen.ntlsnc.salesandcollection.repository.DistributorTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author anika
 * @Date ২৫/৫/২২
 */

@Service
public class DistributorTypeService implements IService<DistributorType> {

    @Autowired
    DistributorTypeRepository distributorTypeRepository;
    @Autowired
    OrganizationService organizationService;

    @Override
    @Transactional
    public DistributorType create(Object object) {
        DistributorTypeDto distributorTypeDto = (DistributorTypeDto) object;
        DistributorType distributorType = new DistributorType();
        distributorType.setName(distributorTypeDto.getName());
        distributorType.setOrganization(organizationService.getOrganizationFromLoginUser());
        return distributorTypeRepository.save(distributorType);

    }

    @Override
    public DistributorType update(Long id, Object object) {
        DistributorTypeDto distributorTypeDto = (DistributorTypeDto) object;
        DistributorType distributorType = this.findById(distributorTypeDto.getId());
        distributorType.setName(distributorTypeDto.getName());
        distributorType.setOrganization(organizationService.getOrganizationFromLoginUser());
        return distributorTypeRepository.save(distributorType);
    }

    @Override
    public boolean delete(Long id) {
        try {
            Optional<DistributorType> optionalDistributorType = distributorTypeRepository.findById(id);
            if (!optionalDistributorType.isPresent()) {
                throw new Exception("Distributor Type Not exist");
            }
            DistributorType distributorType = optionalDistributorType.get();
            distributorType.setIsDeleted(true);
            distributorTypeRepository.save(distributorType);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public DistributorType findById(Long id) {
        try {
            Optional<DistributorType> optionalDistributorType = distributorTypeRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalDistributorType.isPresent()) {
                throw new Exception("Distributor Type Not exist with id " + id);
            }
            return optionalDistributorType.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<DistributorType> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return distributorTypeRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }

    public DistributorType findByName(String name) {
        try {
            Optional<DistributorType> optionalDistributorType =
                    distributorTypeRepository.findByNameAndIsActiveTrueAndIsDeletedFalse(name);
            if (!optionalDistributorType.isPresent()) {
                throw new Exception("Distributor Type Not exist with id " + name);
            }
            return optionalDistributorType.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
