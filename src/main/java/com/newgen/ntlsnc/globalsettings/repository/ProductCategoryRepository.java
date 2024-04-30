package com.newgen.ntlsnc.globalsettings.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.ProductCategory;
import com.newgen.ntlsnc.globalsettings.entity.ProductCategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author mou
 * Created on 4/3/22 11:00 AM
 */

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    ProductCategory findByIdAndIsDeletedFalse(Long id);

    List<ProductCategory> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    @Query(value = "select pc.id, pc.name, count(pcc.id) as childCount\n" +
            "from product_category pc\n" +
            "inner join product_category_type pct\n" +
            "        on pc.product_category_type_id = pct.id\n" +
            "       and pc.company_id = :companyId\n" +
            "       and pc.is_active is true\n" +
            "       and pc.is_deleted is false\n" +
            "       and pct.company_id = :companyId\n" +
            "       and pct.is_active is true\n" +
            "       and pct.is_deleted is false\n" +
            " left join product_category pcc\n" +
            "        on pcc.parent_id = pc.id\n" +
            "       and pcc.company_id = :companyId\n" +
            "       and pcc.is_active is true\n" +
            "       and pcc.is_deleted is false\n" +
            "where pct.level = 0\n" +
            "group by pc.id, pc.name;", nativeQuery = true)
    List<Map<String, Object>> findAllCategoryList(@Param("companyId") Long companyID);

    @Query(value = "select pc.id productCategoryId,pc.name categoryName,count(pcc.id) childCount, count(ptp.id) totalCount\n" +
            "from product_category pc\n" +
            "LEFT JOIN product_category pcc ON pcc.parent_id = pc.id \n" +
            "LEFT JOIN (select p.id, p.product_category_id  from product p\n" +
            "INNER JOIN product_trade_price as ptp ON ptp.product_id=p.id and ptp.expiry_date is null and p.is_active is true and p.is_deleted is false) ptp ON ptp.product_category_id = pc.id\n" +
            "Where pc.parent_id = :parentCategoryId \n" +
            "and pc.is_active is true and pc.is_deleted is false\n"+
            "group by pc.id,pc.name;", nativeQuery = true)
    List<Map<String, Object>> findAllSubCategoryList(@Param("parentCategoryId") Long parentCategoryId);

    List<ProductCategory> findAllByIsDeletedFalseAndProductCategoryTypeInOrderByParent(List<ProductCategoryType> productCategoryTypeList);

    List<ProductCategory> findAllByIsDeletedFalseAndProductCategoryType(ProductCategoryType productCategoryType);

    boolean existsByParentAndIsDeletedFalseAndIsActiveTrue(ProductCategory productCategory);

    boolean existsByNameAndIsDeletedFalseAndIsActiveTrue(String name);

    @Query(value = "select p.*\n" +
            "     , uom.abbreviation                        uom_name\n" +
            "     , ps.pack_size                            pack_size\n" +
            "     , FORMAT(COALESCE(itd.rate, 0), 2)        mfgRate\n" +
            "     , FORMAT(COALESCE(ptp.trade_price, 0), 2) trade_price\n" +
            "     , vs.vat                                  vat \n" +
            "     , vs.vat_included                         vat_included \n" +
            "from product p\n" +
            "         inner join unit_of_measurement uom on p.uom_id = uom.id\n" +
            "         inner join pack_size ps on p.pack_size_id = ps.id\n" +
            "    and p.product_category_id in (:ids)\n" +
            "    and p.is_deleted is false\n" +
            "         left join product_trade_price ptp\n" +
            "                    on p.id = ptp.product_id and ptp.expiry_date is null and ptp.is_deleted is false and ptp.is_active = true\n" +
            "left join vat_setup vs\n" +
            "                   on vs.product_id = p.id and vs.to_date is null and vs.is_active is true and vs.is_deleted is false \n" +
            "         left join (select c.product_id, max(c.id) as details_id\n" +
            "                    from inv_transaction_details c\n" +
            "                             inner join inv_transaction p on c.inv_transaction_id = p.id\n" +
            "                    where p.transaction_type = 'PRODUCTION_RECEIVE'\n" +
            "                    group by c.product_id) t\n" +
            "                   on t.product_id = p.id\n" +
            "         left join inv_transaction_details itd on itd.id = t.details_id ", nativeQuery = true)
    List<Map<String, Object>> findAllProductListByProductCategory(@Param("ids") Long[] ids);

    Optional<ProductCategory> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    @Query(value = "select pc.id as productCategoryId,pc.name as childCategory from product_category pc \n" +
            "left join product_category pcc on pc.id = pcc.parent_id \n" +
            "where pcc.id is null and pc.company_id =:companyId and pc.is_active is true \n" +
            "and pc.is_deleted is false", nativeQuery = true)
    List<Map<String, Object>> getChildCategoryOfACompany(@Param("companyId") Long companyId);

    @Query(value = "CALL SNC_PRODUCT_CATEGORY_LIST_FROM_HIERARCHY(:productCategoryId);", nativeQuery = true)
    List<Long> getProductCategoryListFromCategoryHierarchy(@Param("productCategoryId") Long productCategoryId);

    @Procedure("SNC_PARENT_CATEGORY_PATH")
    String getTopParentByChild(Long productCategoryId);

}
