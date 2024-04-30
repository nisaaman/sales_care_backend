package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.salesandcollection.dto.DistributorCompanyMapDto;
import com.newgen.ntlsnc.salesandcollection.entity.DistributorCompanyMap;
import com.newgen.ntlsnc.salesandcollection.repository.DistributorCompanyMapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author anika
 * @Date ২২/৬/২২
 */
@Service
public class DistributorCompanyMapService implements IService<DistributorCompanyMap> {

    @Autowired
    DistributorService distributorService;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    DistributorCompanyMapRepository distributorCompanyMapRepository;

    @Override
    @Transactional
    public DistributorCompanyMap create(Object object) {
        DistributorCompanyMap distributorCompanyMap = new DistributorCompanyMap();
        DistributorCompanyMapDto distributorCompanyMapDto = (DistributorCompanyMapDto) object;

        if (distributorCompanyMapDto.getDistributorId() != null) {
            distributorCompanyMap.setDistributor(distributorService.findById(distributorCompanyMapDto.getDistributorId()));
        }

        distributorCompanyMap.setCompany(organizationService.findById(distributorCompanyMapDto.getCompanyId()));

        distributorCompanyMap.setOrganization(organizationService.findById(distributorCompanyMapDto.getOrganizationId()));


        return distributorCompanyMapRepository.save(distributorCompanyMap);
    }

    @Override
    @Transactional
    public DistributorCompanyMap update(Long id, Object object) {
        DistributorCompanyMapDto distributorCompanyMapDto = (DistributorCompanyMapDto) object;
        DistributorCompanyMap distributorCompanyMap = this.findById(distributorCompanyMapDto.getId());

        if (distributorCompanyMapDto.getDistributorId() != null) {
            distributorCompanyMap.setDistributor(distributorService.findById(distributorCompanyMapDto.getDistributorId()));
        }

        distributorCompanyMap.setCompany(organizationService.findById(distributorCompanyMapDto.getCompanyId()));

        distributorCompanyMap.setOrganization(organizationService.findById(distributorCompanyMapDto.getOrganizationId()));


        return distributorCompanyMapRepository.save(distributorCompanyMap);

    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            DistributorCompanyMap distributorCompanyMap = findById(id);
            distributorCompanyMap.setIsDeleted(true);
            distributorCompanyMapRepository.save(distributorCompanyMap);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public DistributorCompanyMap findById(Long id) {
        try {
            Optional<DistributorCompanyMap> optionalDistributorCompanyMap = distributorCompanyMapRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalDistributorCompanyMap.isPresent()) {
                throw new Exception("Distributor Company Map Not exist with id " + id);
            }
            return optionalDistributorCompanyMap.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    public List<DistributorCompanyMap> findAll() {
        Organization organization = new Organization();
        return distributorCompanyMapRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }

    public List<Long> getDistributorListFromCompany(Long companyId) {

        return distributorCompanyMapRepository.getAllDistributorByCompany(companyId);
    }
}
