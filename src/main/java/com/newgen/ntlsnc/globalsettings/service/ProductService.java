package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.BaseEntity;
import com.newgen.ntlsnc.common.CommonConstant;
import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.dto.ProductDto;
import com.newgen.ntlsnc.globalsettings.dto.UnitOfMeasurementDto;
import com.newgen.ntlsnc.globalsettings.entity.*;
import com.newgen.ntlsnc.globalsettings.repository.ProductRepository;
import com.newgen.ntlsnc.salesandcollection.service.SalesBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author kamal
 */

@Service
public class ProductService implements IService<Product> {

    @Autowired
    ProductRepository productRepository;
    @Autowired
    ProductCategoryService productCategoryService;
    @Autowired
    PackSizeService packSizeService;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    UnitOfMeasurementService unitOfMeasurementService;
    @Autowired
    DocumentSequenceService documentSequenceService;
    @Autowired
    SalesBookingService salesBookingService;
    @Autowired
    SemesterService semesterService;

    @Override
    public boolean validate(Object object) {
        Product product = (Product) object;
        Optional<Product> optionalProduct;

        if (product.getId() == null) {
            optionalProduct =
                    productRepository.findByOrganizationAndNameAndPackSizeIdAndItemSizeAndUomIdAndIsDeletedFalse(
                            product.getOrganization(), product.getName().trim(), product.getPackSize().getId(),
                            product.getItemSize(), product.getUom().getId());
        } else {
            optionalProduct =
                    productRepository.findByOrganizationAndIdIsNotAndNameAndPackSizeIdAndItemSizeAndUomIdAndIsDeletedFalse(
                            product.getOrganization(), product.getId(), product.getName().trim(),
                            product.getPackSize().getId(), product.getItemSize(), product.getUom().getId());
        }
        if (optionalProduct.isPresent()) {
            throw new RuntimeException("Product already exist.");
        }
        return true;
    }

    @Override
    @Transactional
    public Product create(Object object) {
        try {
            ProductDto productDto = (ProductDto) object;
            Product product = new Product();

            if (productDto.getProductCategoryId() == null) {
                throw new Exception("Please select Product Category.");
            }
            if (productDto.getPackSizeId() == null) {
                throw new Exception("Please select Pack Size.");
            }
            if (productDto.getUomId() == null && productDto.getCustomUOMName() == null) {
                throw new Exception("Please select Unit Of Measurement.");
            }
            if (productDto.getCompanyId() == null) {
                throw new Exception("Please select Company.");
            }

            product.setName(productDto.getName().trim());
            product.setProductSku(documentSequenceService.getSequenceByDocumentId(CommonConstant.DOCUMENT_ID_FOR_PRODUCT_SKU));
            product.setExpiryDays(productDto.getExpiryDays());
            product.setItemSize(productDto.getItemSize());
            product.setMinimumStock(productDto.getMinimumStock());
            product.setCompany(organizationService.findById(productDto.getCompanyId()));
            product.setOrganization(organizationService.getOrganizationFromLoginUser());

            ProductCategory productCategory = productCategoryService.findById(productDto.getProductCategoryId());
            if (productCategory == null) {
                throw new Exception("Product Category does not exist.");
            }
            product.setProductCategory(productCategory);

            PackSize packSize = packSizeService.findById(productDto.getPackSizeId());
            if (packSize == null) {
                throw new Exception("Pack Size does not exist.");
            }
            product.setPackSize(packSize);

            UnitOfMeasurement unitOfMeasurement = null;
            if (productDto.getUomId() != null) {
                unitOfMeasurement = unitOfMeasurementService.findById(productDto.getUomId());
            } else {
                UnitOfMeasurementDto unitOfMeasurementDto = new UnitOfMeasurementDto();
                unitOfMeasurementDto.setAbbreviation(productDto.getCustomUOMName());
                if (productDto.getCustomUOMDescription() != null){
                    unitOfMeasurementDto.setDescription(productDto.getCustomUOMDescription());
                }
                unitOfMeasurement = unitOfMeasurementService.create(unitOfMeasurementDto);

            }

            if (unitOfMeasurement == null) {
                throw new Exception("Unit Of Measurement does not exist.");
            }

            product.setUom(unitOfMeasurement);

            if (!this.validate(product)) {
                return null;
            }

            return productRepository.save(product);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Product update(Long id, Object object) {

        try {

            ProductDto productDto = (ProductDto) object;
            Optional<Product> optionalProduct = productRepository.findById(productDto.getId());

            if (!optionalProduct.isPresent()) {
                throw new RuntimeException("Existing product not found!");
            }

            PackSize packSize = packSizeService.findById(productDto.getPackSizeId());
            if (packSize == null) {
                throw new RuntimeException("Pack Size does not exist.");
            }

            Product product = optionalProduct.get();
            product.setName(productDto.getName().trim());
            product.setExpiryDays(productDto.getExpiryDays());
            product.setItemSize(productDto.getItemSize());
            product.setMinimumStock(productDto.getMinimumStock());
            product.setPackSize(packSize);

            UnitOfMeasurement unitOfMeasurement = null;
            if (productDto.getUomId() != null) {
                unitOfMeasurement = unitOfMeasurementService.findById(productDto.getUomId());
            } else {
                UnitOfMeasurementDto unitOfMeasurementDto = new UnitOfMeasurementDto();
                unitOfMeasurementDto.setAbbreviation(productDto.getCustomUOMName());
                unitOfMeasurementDto.setDescription(productDto.getCustomUOMDescription());
                unitOfMeasurement = unitOfMeasurementService.create(unitOfMeasurementDto);
            }

            product.setUom(unitOfMeasurement);

            if (unitOfMeasurement == null) {
                throw new Exception("Unit Of Measurement does not exist.");
            }

            if (!this.validate(product)) {
                return null;
            }

            return productRepository.save(product);

        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            Organization organization = organizationService.getOrganizationFromLoginUser();
            Optional<Product> optionalProduct = productRepository.findById(id);

            if (!optionalProduct.isPresent()) {
                throw new RuntimeException("Product not found to delete.");
            }
            Map<String, Object> productMap = productRepository.findProductUsage(id);
            if (productMap.size()> 0 ) {
                throw new RuntimeException("Product usage found, can't be delete.");
            }
            Product product = optionalProduct.get();

            product.setIsDeleted(true);
            productRepository.save(product);

            return true;
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

//    @Override
//    public Product findById(Long id) {
//        return productRepository.findById(id).orElse(null);
//    }

    @Override
    public Product findById(Long id) {
        try {
            Optional<Product> optionalProduct = productRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalProduct.isPresent()) {
                throw new Exception("Product Not exist with id " + id);
            }
            return optionalProduct.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<Product> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return productRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    public Product findProductByCompany(Organization company) {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return productRepository.findFirstByOrganizationAndCompanyAndIsDeletedFalse(organization, company);
    }

    public List<Map<String, Object>> findAllProductList(Long semesterId, Long companyId, Long productCategoryId, Long invoiceNatureId, Long salesBookingId) {
        if (semesterId == null) {
            Optional<Semester> semesterOptional = semesterService.findSemesterByDate(LocalDate.now(), companyId);
            if (semesterOptional.isPresent()) {
                semesterId = semesterOptional.get().getId();
            }
        }
        if (salesBookingId != null) {
            return salesBookingService.getAllProductListWithSalesBookingId(semesterId, companyId, productCategoryId, invoiceNatureId, salesBookingId);
        }
        return productRepository.findAllProductList(semesterId, companyId, productCategoryId, invoiceNatureId);
    }

    public boolean findProductByProductCategoryId(ProductCategory productCategory) {
        try {
            boolean product = productRepository.existsByProductCategoryAndIsDeletedFalse(productCategory);
            return product;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Product> getProductByProductCategoryId(ProductCategory productCategory) {
        try {
            List<Product> productList = productRepository.findAllByProductCategoryAndIsDeletedFalseAndIsActiveTrue(productCategory);
//            if(productList.isEmpty()){
//                throw new Exception("Product Profile is empty");
//            }
            return productList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map<String, Object>> getAllProductOfACompany(
            Long companyId, Long productCategoryId) {

        return productRepository.findAllProductOfACompany(companyId, productCategoryId);
    }

    public List<Product> findAllProductByUnitOfMeasurementId(Long storeId) {
        return productRepository.findAllProductByUomIdAndIsDeletedFalse(storeId);
    }

    public List<Product> findAllProductByPackSizeId(Long storeId) {
        return productRepository.findAllProductByPackSizeIdAndIsDeletedFalse(storeId);
    }

    public List<Product> findAllActiveProduct(Long packSizeId) {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        List<Product> productList = productRepository.findAllByOrganizationAndPackSizeIdAndIsDeletedFalseAndIsActiveTrue(organization, packSizeId);
        return productList;
    }


    public Double getWeightedAverageRate(Long companyId, Long productId) {

        //Float weightedAverageRate = 0.0F;
        return (Double) productRepository.getProductWeightedAverageRate(companyId, productId);
    }

    public Map<String, Object> getProductWiseStock(Long companyId, Long depotId, Long productId, String storeType) {
        return productRepository.getDepotAndProductWiseStoreAvailableQuantity(companyId, depotId, productId, storeType);
    }

    public Map<String, Object> getStockBlockedQuantity(Long companyId, Long depotId, Long productId) {
        return productRepository.getStockBlockedQuantity(companyId, depotId, productId);
    }

    public Map<String, Object> getPickedBlockedQuantity(
            Long companyId, Long depotId, Long productId, List<String> pickingStatus) {
        return productRepository.getPickedBlockedQuantity(companyId, depotId, productId, pickingStatus);
    }

    public List<Map<String, Object>> getRestrictedList(
            Long companyId, List<Long> productCategoryId, List<Long> productIds,
            List<Long> depotIds, LocalDate fromDate, LocalDate endDate) {
        return productRepository.getRestrictedStoreInfo(companyId, productCategoryId,
                productIds, depotIds, fromDate, endDate);
    }

    public List<Map<String, Object>> getProductWiseDepotList(Long companyId, Long productId) {
        return productRepository.getProductWiseDepotList(companyId, productId);
    }

    public List<Map<String, Object>> getProductCategoryWiseProductList(Long companyId, Long productCategoryId) {
        return productRepository.getProductList(companyId, productCategoryId);
    }

    public Long getProductIdsByNameAndItemSizeAndCompanyAndUom(String productName,Integer itemSize, Long companyId,String uom,Organization organization){
        System.out.println("productName : "+productName + "uom : " +uom);
        UnitOfMeasurement unitOfMeasurement = unitOfMeasurementService.getUnitOfMeasurementByAbbreviation(organization, uom);
        Optional<Product> productOptional = productRepository.findByNameIgnoreCaseAndItemSizeAndCompanyIdAndUomAndIsDeletedFalseAndIsActiveTrue(productName,itemSize,companyId,unitOfMeasurement);
        return productOptional.map(BaseEntity::getId).orElse(null);
    }

    public List<Map<String, Object>> getAllProductionRecievableProduct(
            Long companyId, Long productCategoryId) {

        return productRepository.findAllProductionRecievableProduct(companyId, productCategoryId);
    }

}
