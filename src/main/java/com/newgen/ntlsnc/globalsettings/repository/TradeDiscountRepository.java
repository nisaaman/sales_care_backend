package com.newgen.ntlsnc.globalsettings.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.TermsAndConditions;
import com.newgen.ntlsnc.globalsettings.entity.TradeDiscount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author marziah
 */

@Repository
public interface TradeDiscountRepository extends JpaRepository<TradeDiscount, Long> {
    List<TradeDiscount> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    @Query(value = "select p.id,p.name, p.product_sku, p.item_size, " +
            "pc.name productCategoryName, ps.pack_size, uom.abbreviation, \n" +
            "concat(p.name, ' ', p.item_size, ' ', uom.abbreviation, ' * ', ps.pack_size) p_name," +
            "ptp.trade_price, DATE_FORMAT(ptp.created_date, '%d/%m/%Y') created_date, " +
            "group_concat(inv.name, \" \" ,td.discount_value, \" \", td.calculation_type ,  \" \", \n" +
            "CASE calculation_type\n" +
            "when 'PERCENTAGE' then CAST(trade_price - (trade_price * discount_value)/100 AS DECIMAL(7,2))\n" +
            "when 'EQUAL' then CAST(trade_price - discount_value AS DECIMAL(7,2))\n" +
            "END) total, td.id tradeDiscountId, ay.fiscal_year_name, s.semester_name\n" +
            "from trade_discount td\n" +
            "inner join product p on td.product_id = p.id  \n" +
            "           and p.product_category_id in (:categoryIds) \n" +
            "           and p.is_active is true \n" +
            "           and p.is_deleted is false \n" +
            "           and td.is_active is true \n" +
            "           and td.is_deleted is false\n" +
            "           and (coalesce(:semesterIds) is null or td.semester_id in (:semesterIds))\n" +
            "inner join product_category pc on p.product_category_id = pc.id \n" +
            "\t\t   and pc.is_active is true \n" +
            "           and pc.is_deleted is false\n" +
            "         inner join pack_size ps\n" +
            "                    on ps.id = p.pack_size_id\n" +
            "                        and ps.is_deleted is false\n" +
            "         inner join unit_of_measurement uom\n" +
            "                    on ps.uom_id = uom.id\n" +
            "                        and uom.is_deleted is false\n" +
            "inner join product_trade_price ptp on ptp.product_id = p.id \n" +
            "\t\t   and ptp.expiry_date is null \n" +
            "           and ptp.is_active is true \n" +
            "           and ptp.is_deleted is false\n" +
            "inner join semester s on  td.semester_id = s.id \n" +
            "inner join accounting_year ay on  ay.id = s.accounting_year_id \n" +
            "inner join invoice_nature inv on inv.id = td.invoice_nature_id \n" +
            "\t\t   and inv.is_active is true \n" +
            "           and inv.is_deleted is false\n" +
            "group by p.id,  p.name, p.product_sku, p.item_size, pc.name, \n" +
            "ps.pack_size, uom.abbreviation, ptp.trade_price, ptp.created_date, s.id, td.id\n" +
            "order by p.id, td.id desc;",nativeQuery = true)
    List<Map<String, Object>> findAllTradeDicountListByProductCategory(
            @Param("categoryIds") Long[] categoryIds,
            @Param("semesterIds") List<Long> semesterIds);

    @Query(value = "select exists (select id from sales_booking_details sbd where sbd.trade_discount_id = :id);",nativeQuery = true)
    Integer existsByIdInSaleBookingDetailsEntity(@Param("id") Long id);

    Optional<TradeDiscount> findByIdAndIsDeletedFalseAndIsActiveTrue(Long id);

    @Query("select d from TradeDiscount d where d.company.id = :companyId \n" +
            "and d.product.id = :productId and d.semester.id = :semesterId and d.invoiceNature.id= :invoiceNatureId  \n" +
            "and d.isActive is true and d.isDeleted is false")
    Optional<TradeDiscount> getByCompanyIdAndProductIdAndSemesterIdAndInvoiceNatureIdAndIsActiveTrueAndIsDeletedFalse(Long companyId, Long productId, Long semesterId, Long invoiceNatureId);

    @Query(value = "select d.id                                  id\n" +
            "     , d.discount_name                       discount_name\n" +
            "     , d.discount_value                      discount_value\n" +
            "     , case\n" +
            "           when d.calculation_type = 'EQUAL' then '/='\n" +
            "           else '%' end                      calculation_type\n" +
            "     , DATE_FORMAT(s.start_date, '%d %b %Y') start_date\n" +
            "     , DATE_FORMAT(s.end_date, '%d %b %Y')   end_date\n" +
            "     , case\n" +
            "           when now() > s.end_date then 'EXPIRED'\n" +
            "           when now() between s.start_date and s.end_date then 'ACTIVE'\n" +
            "           else 'NOT_START_YET' end          status\n" +
            "from trade_discount d\n" +
            "         inner join semester s\n" +
            "                    on d.semester_id = s.id and d.product_id = :productId and d.invoice_nature_id = :invoiceNatureId \n" +
            "                        and d.is_active is true and d.is_deleted is false\n" +
            "                        and s.is_active is true and s.is_deleted is false\n" +
            "order by s.start_date desc, s.end_date desc", nativeQuery = true)
    List<Map> getAllByProductAndInvoiceNature(Long productId, Long invoiceNatureId);
}
