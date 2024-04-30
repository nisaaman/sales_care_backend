package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.dto.PackSizeDto;
import com.newgen.ntlsnc.globalsettings.entity.*;
import com.newgen.ntlsnc.globalsettings.repository.PackSizeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author marzia
 * Created on 5/4/22 03:00 PM
 */

@Service
public class PackSizeService implements IService<PackSize> {

    @Autowired
    PackSizeRepository packSizeRepository;
    @Autowired
    UnitOfMeasurementService unitOfMeasurementService;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    ProductService productService;

    @Override
    @Transactional
    public PackSize create(Object object) {

        PackSizeDto packSizeDto = (PackSizeDto) object;
        PackSize packSize = new PackSize();

        packSize.setPackSize(packSizeDto.getPackSize());
        packSize.setDescription(packSizeDto.getDescription());
        packSize.setHeight(packSizeDto.getHeight());
        packSize.setWidth(packSizeDto.getWidth());
        packSize.setLength(packSizeDto.getLength());

        UnitOfMeasurement unitOfMeasurement = unitOfMeasurementService.findById(packSizeDto.getUomId());
        packSize.setUom(unitOfMeasurement);
        packSize.setIsActive(packSizeDto.getIsActive());
        packSize.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (!this.validate(packSize)) {
            return null;
        }
        return packSizeRepository.save(packSize);
    }

    @Override
    @Transactional
    public PackSize update(Long id, Object object) {

        PackSizeDto packSizeDto = (PackSizeDto) object;
        PackSize packSize = this.findById(packSizeDto.getId());

        packSize.setPackSize(packSizeDto.getPackSize());
        packSize.setDescription(packSizeDto.getDescription());
        packSize.setHeight(packSizeDto.getHeight());
        packSize.setWidth(packSizeDto.getWidth());
        packSize.setLength(packSizeDto.getLength());

        UnitOfMeasurement unitOfMeasurement = unitOfMeasurementService.findById(packSizeDto.getUomId());
        packSize.setUom(unitOfMeasurement);
        packSize.setIsActive(packSizeDto.getIsActive());
        packSize.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (!this.validate(packSize)) {
            return null;
        }
        return packSizeRepository.save(packSize);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            Optional<PackSize> optionalPackSize = packSizeRepository.findById(id);
            if (!optionalPackSize.isPresent()) {
                throw new Exception("Pack Size Not exist");
            }

            List<Product> productList = productService.findAllProductByPackSizeId(optionalPackSize.get().getId());
            if(productList.size() > 0){
                throw new RuntimeException("This Pack Size already in use.");
            }

            optionalPackSize.get().setIsDeleted(true);
            packSizeRepository.save(optionalPackSize.get());
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public PackSize findById(Long id) {
        try {
            Optional<PackSize> optionalPackSize = packSizeRepository.findByIdAndIsDeletedFalse(id);
            if (!optionalPackSize.isPresent()) {
                throw new Exception("Pack Size Not exist with id " + id);
            }
            return optionalPackSize.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<PackSize> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return packSizeRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Override
    public boolean validate(Object object) {
        PackSize packSize = (PackSize) object;
        Optional<PackSize> optionalPackSize = Optional.empty();
        Optional<PackSize> optionalPackSize1 = Optional.empty();

        if(packSize.getId() == null) {
//            optionalPackSize = packSizeRepository.findByOrganizationAndPackSizeAndIsDeletedFalse(
//                    packSize.getOrganization(), packSize.getPackSize());
            optionalPackSize=packSizeRepository.findByOrganizationAndPackSizeAndHeightAndWidthAndLengthAndIsDeletedFalse(packSize.getOrganization().getId(),
                    packSize.getPackSize(),packSize.getHeight(),packSize.getWidth(),packSize.getLength(), packSize.getUom().getId());
        } else {
//            optionalPackSize = packSizeRepository.findByOrganizationAndIdIsNotAndPackSizeAndIsDeletedFalse(
//                    packSize.getOrganization(), packSize.getId(), packSize.getPackSize());

            optionalPackSize = packSizeRepository.findByOrganizationAndIdIsNotAndPackSizeAndHeightAndWidthAndLengthAndIsDeletedFalse(
                    packSize.getOrganization().getId(), packSize.getId(), packSize.getPackSize(),packSize.getHeight(),packSize.getWidth(),packSize.getLength(), packSize.getUom().getId());

            if(packSize.getIsActive()==false){
                List<Product> productList = productService.findAllActiveProduct(packSize.getId()) ;
                if (productList.size()>0) {
                    throw new RuntimeException("This Pack Size is already in use");
                }
            }
        }
        if(optionalPackSize.isPresent()){
            throw new RuntimeException("Pack Size already exist.");
        }
        return true;
    }

    public List<PackSize> findAllPackSizeByUnitOfMeasurementId(Long storeId) {
        return packSizeRepository.findAllPackSizeByUomIdAndIsDeletedFalse(storeId);
    }

}
