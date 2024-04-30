package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.BaseEntity;
import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.dto.UnitOfMeasurementDto;
import com.newgen.ntlsnc.globalsettings.entity.*;
import com.newgen.ntlsnc.globalsettings.repository.UnitOfMeasurementRepository;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvTransactionDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UnitOfMeasurementService implements IService<UnitOfMeasurement> {

    @Autowired
    UnitOfMeasurementRepository unitOfMeasurementRepository;

    @Autowired
    OrganizationService organizationService;
    @Autowired
    ProductService productService;
    @Autowired
    PackSizeService packSizeService;

    @Override
    @Transactional
    public UnitOfMeasurement create(Object object) {

        UnitOfMeasurementDto unitOfMeasurementDto = (UnitOfMeasurementDto) object;
        UnitOfMeasurement unitOfMeasurement = new UnitOfMeasurement();

        unitOfMeasurement.setAbbreviation(unitOfMeasurementDto.getAbbreviation());
        unitOfMeasurement.setDescription(unitOfMeasurementDto.getDescription());
        unitOfMeasurement.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (!this.validate(unitOfMeasurement)) {
            return null;
        }

        return unitOfMeasurementRepository.save(unitOfMeasurement);
    }

    @Override
    @Transactional
    public UnitOfMeasurement update(Long id, Object object) {

        UnitOfMeasurementDto unitOfMeasurementDto = (UnitOfMeasurementDto) object;
        UnitOfMeasurement unitOfMeasurement = this.findById(id);

        unitOfMeasurement.setAbbreviation(unitOfMeasurementDto.getAbbreviation());
        unitOfMeasurement.setDescription(unitOfMeasurementDto.getDescription());
        unitOfMeasurement.setIsActive(unitOfMeasurementDto.getIsActive());
        unitOfMeasurement.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (!this.validate(unitOfMeasurement)) {
            return null;
        }
        return unitOfMeasurementRepository.save(unitOfMeasurement);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            Optional<UnitOfMeasurement> optionalUnitOfMeasurement = unitOfMeasurementRepository.findById(id);
            if (!optionalUnitOfMeasurement.isPresent()) {
                throw new Exception("Unit Of Measurement Not exist");
            }

            List<Product> productList = productService.findAllProductByUnitOfMeasurementId(optionalUnitOfMeasurement.get().getId());
            if(productList.size() > 0){
                throw new RuntimeException("This Unit Of Measurement already in use.");
            }

            List<PackSize> packSizeList = packSizeService.findAllPackSizeByUnitOfMeasurementId(optionalUnitOfMeasurement.get().getId());
            if(packSizeList.size() > 0){
                throw new RuntimeException("This Unit Of Measurement already in use.");
            }

            UnitOfMeasurement unitOfMeasurement = optionalUnitOfMeasurement.get();
            unitOfMeasurement.setIsDeleted(true);
            unitOfMeasurementRepository.save(unitOfMeasurement);

            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    public UnitOfMeasurement findById(Long id) {
        try {
            Optional<UnitOfMeasurement> optionalUnitOfMeasurement = unitOfMeasurementRepository.findByIdAndIsDeletedFalse(id);
            if (!optionalUnitOfMeasurement.isPresent()) {
                throw new Exception("Unit Of Measurement Not exist with id " + id);
            }
            return optionalUnitOfMeasurement.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<UnitOfMeasurement> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return unitOfMeasurementRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    public List<UnitOfMeasurement> findAllActiveUOM() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return unitOfMeasurementRepository.findAllByOrganizationAndIsDeletedFalseAndIsActiveTrue(organization);
    }

    @Override
    public boolean validate(Object object) {
        UnitOfMeasurement unitOfMeasurement = (UnitOfMeasurement) object;
        Optional<UnitOfMeasurement> optionalUnitOfMeasurement = Optional.empty();

        if(unitOfMeasurement.getId() == null) {
            optionalUnitOfMeasurement = unitOfMeasurementRepository.findByOrganizationAndAbbreviationIgnoreCaseAndIsDeletedFalse(
                    unitOfMeasurement.getOrganization(), unitOfMeasurement.getAbbreviation().trim());
        } else {
            optionalUnitOfMeasurement = unitOfMeasurementRepository.findByOrganizationAndIdIsNotAndAbbreviationIgnoreCaseAndIsDeletedFalse(
                    unitOfMeasurement.getOrganization(), unitOfMeasurement.getId(), unitOfMeasurement.getAbbreviation().trim());
        }

        if(optionalUnitOfMeasurement.isPresent()){
            throw new RuntimeException("Unit Of Measurement Abbreviation already exist.");
        }
        return true;
    }

    public UnitOfMeasurement getUnitOfMeasurementByAbbreviation(Organization organization, String sbbreviation){
        Optional<UnitOfMeasurement> productOptional = unitOfMeasurementRepository.findByAbbreviationIgnoreCaseAndOrganizationAndIsDeletedFalse(sbbreviation,organization);
        return productOptional.orElse(null);
    }
}
