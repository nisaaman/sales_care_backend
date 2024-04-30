package com.newgen.ntlsnc.globalsettings.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.Product;
import com.newgen.ntlsnc.globalsettings.entity.ProductTradePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author kamal
 * @Date ১১/৪/২২
 */

@Repository
public interface ProductTradePriceRepository extends JpaRepository<ProductTradePrice, Long> {
    List<ProductTradePrice> findAllByOrganizationAndIsDeletedFalse(Organization organization);


    @Query(value = "select ptp.id                                       id\n" +
            "     , p.id                                         product_id\n" +
            "     , ptp.trade_price                              tradePrice\n" +
            "     , DATE_FORMAT(ptp.created_date, '%d-%b-%Y')    productTradePriceCreatedDate\n" +
            "     , p.name                                       productName\n" +
            "     , p.product_category_id                        product_category_id\n" +
            "     , p.product_sku                                productSku\n" +
            "     , p.item_size                                  itemSize\n" +
            "     , pc.name                                      productCategoryName\n" +
            "     , ps.pack_size                                 packSize\n" +
            "     , uom.abbreviation                             uomAbbreviation\n" +
            "     , vs.vat                                       vat\n" +
            "     , DATE_FORMAT(vs.from_date, '%d-%b-%Y')        vatFromDate\n" +
            "     , vs.vat_included                              vatIncluded\n" +
            "     , itd.rate                                     mfgRate\n" +
            "     , DATE_FORMAT(it.transaction_date, '%d-%b-%Y') mfgDate\n" +
            "from product_trade_price ptp\n" +
            "         inner join product p\n" +
            "                    on p.id = ptp.product_id\n" +
            "                        and p.is_deleted is false\n" +
            "                        and p.is_active is true\n" +
            "                        and ptp.is_deleted is false\n" +
            "                        and ptp.is_active = true\n" +
            "                        and p.company_id = :companyId\n" +
            "                        and ptp.expiry_date is null\n" +
            "      and (:totalProductCategoryIds = 0  or p.product_category_id in :productCategoryIds)\n" +
            "         inner join product_category pc\n" +
            "                    on pc.id = p.product_category_id\n" +
            "                        and pc.is_deleted is false\n" +
            "         inner join pack_size ps\n" +
            "                    on ps.id = p.pack_size_id\n" +
            "                        and ps.is_deleted is false\n" +
            "         inner join unit_of_measurement uom\n" +
            "                    on ps.uom_id = uom.id\n" +
            "                        and uom.is_deleted is false\n" +
            "         left join vat_setup vs\n" +
            "                   on vs.product_id = p.id\n" +
            "                       and vs.is_active is true\n" +
            "                       and vs.is_deleted is false\n" +
            "                       and now() between vs.from_date and vs.to_date\n" +
            "         left join (select dd.product_id, max(dd.id) as details_id\n" +
            "                    from inv_transaction_details dd\n" +
            "                             inner join inv_transaction tt\n" +
            "                                        on dd.inv_transaction_id = tt.id\n" +
            "                                            and tt.is_active is true\n" +
            "                                            and tt.is_deleted is false\n" +
            "                                            and dd.is_active is true\n" +
            "                                            and dd.is_deleted is false\n" +
            "                                            and tt.transaction_type = 'PRODUCTION_RECEIVE'\n" +
            "                                            and tt.company_id = :companyId\n" +
            "                    group by dd.product_id) t\n" +
            "                   on t.product_id = p.id\n" +
            "         left join inv_transaction_details itd\n" +
            "                   on itd.id = t.details_id\n" +
            "         left join inv_transaction it\n" +
            "                   on it.id = itd.inv_transaction_id" +
            " order by p.id ",nativeQuery = true)
    List<Map> findAllListByOrganizationIdAndProductCategoryIdsAndTotalCategoryIdListSize(@Param("companyId") Long companyId,
                                                          @Param("productCategoryIds") List<Long> productCategoryIds,
                                                                                 @Param("totalProductCategoryIds") Integer totalProductCategoryIds);


    Optional<ProductTradePrice> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    @Query(value = "select COALESCE(count(p.id), 0) product_count\n" +
            "from product p\n" +
            "         inner join product_trade_price ptp\n" +
            "                    on p.id = ptp.product_id\n" +
            "                        and ptp.expiry_date is null\n" +
            "                        and p.company_id = :companyId\n" +
            "                        and p.is_active is true\n" +
            "                        and p.is_deleted is false\n" +
            "                        and ptp.is_active is true\n" +
            "                        and ptp.is_deleted is false", nativeQuery = true)
    Map getTradePriceSumWithProductCountByCompany(@Param("companyId") Long companyId);


    @Query(value = "select ptp.id                                    id\n" +
            "     , ptp.product_id                            product_id\n" +
            "     , ROUND(ptp.trade_price, 2)                 amount \n" +
            "     , DATE_FORMAT(ptp.created_date, '%d-%b-%Y') effective_date\n" +
            "     , DATE_FORMAT(ptp.expiry_date, '%d-%b-%Y')  expired_date \n" +
            "from product_trade_price ptp\n" +
            "where ptp.product_id = :productId \n" +
            "  and ptp.is_active is true\n" +
            "  and ptp.is_deleted is false\n" +
            "order by ptp.created_date desc, ptp.expiry_date desc",nativeQuery = true)
    List<Map> getAllByProductIdAndIsActiveTrueAndIsDeletedFalseOrderByExpiryDate(Long productId);

    List<ProductTradePrice> findAllByProductIdAndExpiryDateIsNullAndIsActiveTrueAndIsDeletedFalse(Long productId);
}
