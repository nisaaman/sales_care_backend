package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.dto.ProductCategoryDto;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.Product;
import com.newgen.ntlsnc.globalsettings.entity.ProductCategory;
import com.newgen.ntlsnc.globalsettings.entity.ProductCategoryType;
import com.newgen.ntlsnc.globalsettings.repository.ProductCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author sagor
 * @date ৬/৪/২২
 */
@Service
public class ProductCategoryService implements IService<ProductCategory> {
    @Autowired
    ProductCategoryRepository productCategoryRepository;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    ProductCategoryTypeService categoryTypeService;

    @Autowired
    ProductCategoryTypeService productCategoryTypeService;

    @Autowired
    ProductService productService;

    @Override
    @Transactional
    public ProductCategory create(Object object) {

        ProductCategoryDto productCategoryDto = (ProductCategoryDto) object;
        ProductCategory productCategory = new ProductCategory();
        productCategory.setName(productCategoryDto.getName());
        productCategory.setPrefix(productCategoryDto.getPrefix());
        productCategory.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (productCategoryDto.getProductCategoryTypeId() != null) {
            productCategory.setProductCategoryType(categoryTypeService.findById(productCategoryDto.getProductCategoryTypeId()));
        }
        if (productCategoryDto.getParentId() != null) {
            productCategory.setParent(this.findById(productCategoryDto.getParentId()));
        }
        if (!this.validate(productCategory)) {
            return null;
        }
        return productCategoryRepository.save(productCategory);
    }

    @Override
    @Transactional
    public ProductCategory update(Long id, Object object) {
        ProductCategoryDto productCategoryDto = (ProductCategoryDto) object;
        ProductCategory productCategory = productCategoryRepository.findByIdAndIsDeletedFalse(id);
        try {
            if (productCategory == null) {
                throw new Exception("Product Category does not exist");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        productCategory.setName(productCategoryDto.getName());
        productCategory.setPrefix(productCategoryDto.getPrefix());
        productCategory.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (productCategoryDto.getProductCategoryTypeId() != null) {
            productCategory.setProductCategoryType(categoryTypeService.findById(productCategoryDto.getProductCategoryTypeId()));
        }
        if (productCategoryDto.getParentId() != null) {
            productCategory.setParent(this.findById(productCategoryDto.getParentId()));
        }
        if (!this.validate(productCategory)) {
            return null;
        }
        productCategory.setName(productCategoryDto.getName());
        productCategory.setPrefix("ae");
        return productCategoryRepository.save(productCategory);

    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            ProductCategory productCategory = findById(id);
            boolean isExits = productCategoryRepository.existsByParentAndIsDeletedFalseAndIsActiveTrue(productCategory);
            if (isExits) {
                throw new Exception(productCategory.getName() + " can not be deleted.It's already used");
            } else {
                boolean isExitsProduct = productService.findProductByProductCategoryId(productCategory);
                if (isExitsProduct) {
                    throw new Exception(productCategory.getName() + " can not be deleted.It's already used");
                } else {
                    productCategory.setIsDeleted(true);
                    productCategoryRepository.save(productCategory);
                    return true;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public ProductCategory findById(Long id) {

        try {
            Optional<ProductCategory> optionalProductCategory = productCategoryRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalProductCategory.isPresent()) {
                throw new Exception("Product Category Not exist with id " + id);
            }
            return optionalProductCategory.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<ProductCategory> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return productCategoryRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    public List<Map<String, Object>> findAllCategoryList(Long companyId) {
        return productCategoryRepository.findAllCategoryList(companyId);
    }

    public List<Map<String, Object>> findAllSubCategoryList(Long parentCategoryId) {
        return productCategoryRepository.findAllSubCategoryList(parentCategoryId);
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }


    @Transactional
    public Map<String, Object> createAll(ProductCategoryDto productCategoryDto) {
        try {
            if (productCategoryDto.getChildProductCategoryDtoList().size() == 0) {
                throw new RuntimeException("Invalid request! No data submitted.");
            }
            Organization company = organizationService.findById(productCategoryDto.getCompanyId());
            List<ProductCategoryType> productCategoryTypeList = productCategoryTypeService.getProductCategoryList(company);

            this.saveAll(productCategoryDto.getChildProductCategoryDtoList(), null, productCategoryTypeList, company);

            return getAllProductCategoryTreeByCompanyWise(productCategoryDto.getCompanyId());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void saveAll(List<ProductCategoryDto> productCategoryDtoList, ProductCategory parentProductCategory,
                         List<ProductCategoryType> productCategoryTypeList, Organization company) {
        Organization parentCompany = organizationService.getOrganizationFromLoginUser();

        productCategoryDtoList.forEach((productCategoryParams) -> {
            ProductCategory productCategory = null;
            if (productCategoryParams.getId() == null) {
                productCategory = new ProductCategory();

                /*try {
                    boolean isExitsName = productCategoryRepository.existsByNameAndIsDeletedFalseAndIsActiveTrue(productCategoryParams.getName());
                    if (isExitsName) {
                        throw new Exception("This Product Category name is already used");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }*/

                productCategory.setName(productCategoryParams.getName());
                productCategory.setPrefix(getProductCategoryPrefix(productCategoryParams.getName()));
                productCategory.setIsDeleted(false);
                productCategory.setIsActive(true);
                productCategory.setCompany(company);
                productCategory.setParent(parentProductCategory);
                productCategory.setOrganization(parentCompany);
                //set locationType dynamically compare with locationListAsTree's treeLevel length seperated by '-'.
                productCategory.setProductCategoryType(productCategoryTypeList.get(productCategoryParams.getTreeLevel().split("-").length - 1));
                productCategory = productCategoryRepository.save(productCategory);

                if ((productCategoryParams.getChildren()).size() > 0) {
                    saveAll(productCategoryParams.getChildren(), productCategory, productCategoryTypeList, company);
                }
            } else {
                productCategory = productCategoryRepository.findByIdAndIsDeletedFalse(productCategoryParams.getId());
                //set product category type dynamically compare with locationListAsTree's treeLevel length seperated by '-'.
                productCategory.setName(productCategoryParams.getName());
                productCategory.setPrefix(getProductCategoryPrefix(productCategoryParams.getName()));

                if ((productCategoryParams.getChildren()).size() > 0) {
                    saveAll(productCategoryParams.getChildren(), productCategory, productCategoryTypeList, company);
                }
            }
        });
    }

    public Map<String, Object> getAllProductCategoryTreeByCompanyWise(Long companyId) {
        try {
            Map<String, Object> response = new HashMap<>();
            List<ProductCategoryType> productCategoryTypeList = productCategoryTypeService.getAllProductCategoryTypeByCompanyWise(companyId);
            List<ProductCategory> productCategoryList =productCategoryRepository.findAllByIsDeletedFalseAndProductCategoryTypeInOrderByParent(productCategoryTypeList);
            List<Map> productCategoryAsTree = getProductCategoryAsTree(productCategoryList, new HashMap<>());
            response.put("childProductCategoryDtoList", productCategoryAsTree);
            if (productCategoryAsTree == null) {
                throw new Exception("Product Category Type Not exist with company id " + companyId);
            }
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map> getProductCategoryAsTree(List<ProductCategory> productCategoryList, Map parentNode) {
        List<Map> productCategoryTree = new ArrayList<>();
        Integer level = 1;
        for (int i = 0; i < productCategoryList.size(); ) {
            if (productCategoryList.get(i).getParent() == null || productCategoryList.get(i).getParent().getId() == parentNode.get("id")) {
                Map productCategoryNode = new HashMap(); // product category tree node

                productCategoryNode.put("id", productCategoryList.get(i).getId());
                productCategoryNode.put("name", productCategoryList.get(i).getName());
                productCategoryNode.put("productList", new ArrayList<>());
                productCategoryNode.put("typeName", productCategoryList.get(i).getProductCategoryType().getName());
                productCategoryNode.put("treeLevel", parentNode.get("treeLevel") == null ? level.toString() : parentNode.get("treeLevel").toString() + "-" + level);
                productCategoryList.remove(i);
                productCategoryTree.add(productCategoryNode);
                level++;
            } else {
                i++;
            }
        }
        productCategoryTree.forEach(node -> {
            node.put("children", getProductCategoryAsTree(productCategoryList, node));
        });
        return productCategoryTree;

    }

    public boolean findProductCategoryByProductCategoryType(ProductCategoryType productCategoryType) {
        List<ProductCategory> productCategoryList = productCategoryRepository.findAllByIsDeletedFalseAndProductCategoryType(productCategoryType);
        if (productCategoryList.size() > 0) {
            return true;
        } else {
            return false;
        }
    }


    @Transactional
    public Map<String, Object> updateSingleProductCategory(Long id, Object object) {
        try {
            ProductCategoryDto productCategoryDto = (ProductCategoryDto) object;
            ProductCategory productCategory = productCategoryRepository.findByIdAndIsDeletedFalse(id);
            boolean isExitsName = productCategoryRepository.existsByNameAndIsDeletedFalseAndIsActiveTrue(productCategoryDto.getName());
            if (productCategory == null) {
                throw new Exception("Product Category does not exist");
            }
            if (isExitsName) {
                throw new Exception("This Product Category name is already used");
            }
            productCategory.setName(productCategoryDto.getName());
            productCategory.setPrefix("ae");
            return getAllProductCategoryTreeByCompanyWise(productCategoryDto.getCompanyId());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    @Transactional
    public Map<String, Object> deleteSingleProductCategory(Long id) {
        try {
            ProductCategory productCategory = findById(id);
            boolean isExits = productCategoryRepository.existsByParentAndIsDeletedFalseAndIsActiveTrue(productCategory);
            if (isExits) {
                throw new Exception(productCategory.getName() + " can not be deleted.It's already used");
            } else {
                boolean isExitsProduct = productService.findProductByProductCategoryId(productCategory);
                if (isExitsProduct) {
                    throw new Exception(productCategory.getName() + " can not be deleted.It's already used");
                } else {
                    productCategory.setIsDeleted(true);
                    productCategoryRepository.save(productCategory);
                    return getAllProductCategoryTreeByCompanyWise(productCategory.getCompany().getId());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map<String, Object>> getAllProductProfileListByProductCategoryWise(Long[] ids) {
        try {
            List<Map<String, Object>> productProfileList = productCategoryRepository.findAllProductListByProductCategory(ids);
            return productProfileList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map<String,Object>> getChildCategoryOfACompany(Long companyId) {
        return productCategoryRepository.getChildCategoryOfACompany(companyId);
    }

    public List<Long> getProductCategoryListFromCategoryHierarchy(Long productCategoryId) {
        return productCategoryRepository.getProductCategoryListFromCategoryHierarchy(productCategoryId);
    }

    private String getProductCategoryPrefix(String categoryName){
        StringBuilder prefixName = new StringBuilder();
        String[] arrOfStr = categoryName.split(" ");
        int arrOfStrLength = arrOfStr.length;

        if(arrOfStrLength == 1){
            prefixName.append(arrOfStr[0].substring(0,3));
        }else if(arrOfStrLength == 2){
            prefixName.append(arrOfStr[0].substring(0,2));
            prefixName.append(arrOfStr[1].charAt(0));
        }else if(arrOfStrLength > 2){
            for (String word : arrOfStr) {
                Character firstLetter = word.charAt(0);
                prefixName.append(firstLetter);
            }
        }else{
            throw new RuntimeException("Product Category name not found!");
        }

        return prefixName.toString().toUpperCase();

    }

    public String getTopParentByChild(Long childId) {
        String  mostParentId = productCategoryRepository.getTopParentByChild(childId);

        return mostParentId;
    }

}
