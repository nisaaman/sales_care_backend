package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.dto.LocationTypeDto;
import com.newgen.ntlsnc.globalsettings.dto.ProductCategoryTypeDto;
import com.newgen.ntlsnc.globalsettings.entity.*;
import com.newgen.ntlsnc.globalsettings.repository.ProductCategoryTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author marzia
 */

@Service
public class ProductCategoryTypeService implements IService<ProductCategoryType> {
    @Autowired
    ProductCategoryTypeRepository productCategoryTypeRepository;
    @Autowired
    OrganizationService organizationService;

    @Autowired
    ProductCategoryService productCategoryService;

    @Autowired
    ProductService productService;

    @Override
    @Transactional
    public ProductCategoryType create(Object object) {
        ProductCategoryTypeDto categoryTypeDto = (ProductCategoryTypeDto) object;
        ProductCategoryType categoryType = new ProductCategoryType();
        // Organization organization = organizationService.findById(categoryTypeDto.getOrganizationId());
        //categoryType.setOrganization(organization);
        categoryType.setName(categoryTypeDto.getName());
        categoryType.setLevel(categoryTypeDto.getLevel());
        categoryType.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (!this.validate(categoryType)) {
            return null;
        }
        return productCategoryTypeRepository.save(categoryType);
    }

    @Override
    @Transactional
    public ProductCategoryType update(Long id, Object object) {
        ProductCategoryTypeDto categoryTypeDto = (ProductCategoryTypeDto) object;
        ProductCategoryType categoryType = productCategoryTypeRepository.findById(categoryTypeDto.getId()).get();
        //Organization organization = organizationService.findById(categoryTypeDto.getOrganizationId());
        //categoryType.setOrganization(organization);
        categoryType.setName(categoryTypeDto.getName());
        categoryType.setLevel(categoryTypeDto.getLevel());
        categoryType.setOrganization(organizationService.getOrganizationFromLoginUser());
        if (!this.validate(categoryType)) {
            return null;
        }
        return productCategoryTypeRepository.save(categoryType);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            ProductCategoryType productCategoryType = findById(id);
            boolean isExits = productCategoryService.findProductCategoryByProductCategoryType(productCategoryType);
            if (isExits) {
                throw new Exception("This Product Category Type already used.Can not be deleted");
            } else {
                productCategoryType.setIsDeleted(true);
                productCategoryTypeRepository.save(productCategoryType);
                return true;
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public ProductCategoryType findById(Long id) {
        try {
            Optional<ProductCategoryType> optionalProductCategoryType = productCategoryTypeRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalProductCategoryType.isPresent()) {
                throw new Exception("Product Category Type Not exist with id " + id);
            }
            return optionalProductCategoryType.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<ProductCategoryType> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return productCategoryTypeRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }


    @Transactional
    public List<ProductCategoryType> createAll(List<ProductCategoryTypeDto> productCategoryTypeDtoList) {
        try {
            if (productCategoryTypeDtoList.size() == 0) {
                throw new RuntimeException("Invalid request! No data submitted.");
            }

            Organization organization = organizationService.getOrganizationFromLoginUser(); // get parent company from logged in user
            Organization company = organizationService.findById(productCategoryTypeDtoList.get(0).getCompanyId()); // get company from first index of data list
//            List<ProductCategoryType> existingProductCategoryType = productCategoryTypeRepository.findByOrganizationAndCompanyAndIsDeletedFalse(organization, company);

            Product product = productService.findProductByCompany(company);

            /*if (existingProductCategoryType.size() > 0) {
                throw new RuntimeException("Product category level setup already exist for the company: [" +
                        company.getShortName() + "] " + company.getName());
            }*/

            List<ProductCategoryType> categoryTypeDtoList = new ArrayList<>();
            productCategoryTypeDtoList.forEach(productCategoryTypeDto -> {
                ProductCategoryType productCategoryType;

                if(productCategoryTypeDto.getId() == null){
                    if(product != null){
                        throw new RuntimeException("Product exists. New product category level create is forbidden.");
                    }

                    productCategoryType = new ProductCategoryType();

                    productCategoryType.setOrganization(organization);
                    productCategoryType.setCompany(company);
                    productCategoryType.setLevel(productCategoryTypeDto.getLevel());
                }else {
                    Optional<ProductCategoryType> productCategoryTypeOptional = productCategoryTypeRepository.findById(productCategoryTypeDto.getId());

                    if(productCategoryTypeOptional.isPresent()){
                        productCategoryType = productCategoryTypeOptional.get();
                    } else {
                        throw new RuntimeException("Product category type not found");
                    }
                }

                productCategoryType.setName(productCategoryTypeDto.getName());

                categoryTypeDtoList.add(productCategoryType);
            });

            return productCategoryTypeRepository.saveAll(categoryTypeDtoList);

        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Transactional
    public List<ProductCategoryType> getProductCategoryList(Organization organization) {
        List<ProductCategoryType> productCategoryTypeList = productCategoryTypeRepository.findAllByIsDeletedFalseAndCompanyOrderByLevel(organization);
        return productCategoryTypeList;
    }

    public List<ProductCategoryType> getAllProductCategoryTypeByCompanyWise(Long companyId) {
        try {
            Organization organization = organizationService.findById(companyId);
            List<ProductCategoryType> productCategoryTypeList = productCategoryTypeRepository.findAllByCompanyAndIsDeletedFalse(organization);
            if (productCategoryTypeList == null) {
                throw new Exception("Product Category Type Not exist with id " + companyId);
            }
            return productCategoryTypeList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<ProductCategoryTypeDto> getAllProductCategoryTypeDtoByCompanyWise(Long companyId) {
        try {
            Organization organization = organizationService.findById(companyId);
            List<ProductCategoryType> productCategoryTypeList = productCategoryTypeRepository.findAllByCompanyAndIsDeletedFalse(organization);
            List<ProductCategoryTypeDto> productCategoryTypeDtoList = new ArrayList<>();

            if (productCategoryTypeList == null) {
                throw new Exception("Product Category Type Not exist with id " + companyId);
            }

            productCategoryTypeList.forEach(it -> productCategoryTypeDtoList.add(new ProductCategoryTypeDto(it)));

            return productCategoryTypeDtoList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
