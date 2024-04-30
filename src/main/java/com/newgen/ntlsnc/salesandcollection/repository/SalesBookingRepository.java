package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.Semester;
import com.newgen.ntlsnc.salesandcollection.entity.SalesBooking;
import com.newgen.ntlsnc.salesandcollection.entity.SalesBookingDetails;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * @author kamal
 * @Date ১১/৪/২২
 */

@Repository
public interface SalesBookingRepository extends JpaRepository<SalesBooking, Long> {
    List<SalesBooking> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    @Query(value = "SELECT d.distributor_name as distributorName,\n" +
            "sb.approval_status as approvalStatus,\n" +
            "sb.booking_date as bookingDate,\n" +
            "Sum(sbd.quantity)   AS quantity,\n" +
            "Sum(sbd.quantity) * Sum(ptp.trade_price) amount\n" +
            "FROM   sales_booking AS sb\n" +
            "INNER JOIN sales_booking_details AS sbd ON sb.id = sbd.sales_booking_id\n" +
            "INNER JOIN distributor AS d ON sb.distributor_id = d.id\n" +
            "INNER JOIN product_trade_price AS ptp  ON ptp.id = sbd.product_trade_price_id\n" +
            "where sb.is_active is true and sb.is_deleted is false \n" +
            "and d.is_active is true and d.is_deleted is false \n" +
            "and ptp.is_active is true and ptp.is_deleted is false \n" +
            "GROUP  BY sb.id,\n" +
            "d.distributor_name,\n" +
            "sb.approval_status,\n" +
            "sb.booking_date;",nativeQuery = true)
    List<Map<String, Object>> findAllSalesBookingList();

    @Query(value = "SELECT " +
            "COUNT(sbd.product_id) product_count, sbd_sts.rejected_count," +
            "COUNT(DISTINCT(sb.id)) booking_count, " +
            "sb.company_id, sb.semester_id, sb.id booking_id, sb.booking_no, sb.notes, \n" +
            "sb.is_booking_stock_confirmed stock_confirmed, \n" +
            "sb.booking_date, MONTH(sb.booking_date) AS month, YEAR(sb.booking_date) AS year, \n" +
            "DATE_FORMAT(sb.booking_date, '%b %Y') AS year_and_month, \n" +
            "sb.approval_date, sb.approval_status, sb.tentative_delivery_date, \n" +
            "DATEDIFF( sb.tentative_delivery_date, sb.booking_date ) as days_left, \n" +
            "d.id distributor_id, d.distributor_name, d.contact_no distributor_contact_no, d.bill_to_address, \n" +
            "au.name sales_officer_name, ds.name designation_name, \n" +
            "rm.reporting_to_id reporting_manager, \n" +
            "lmm.location_id, lo.name location_name, \n" +
            "dp.depot_name, dp.contact_number, dp.address, inv.name invoice_nature, ar.fiscal_year_name, \n" +
            "SUM(ifnull(sbd.quantity,0)) booking_quantity, " +
            "SUM(ifnull(sbd.free_quantity,0)) free_quantity, \n" +

            "ROUND( SUM(CASE " +
            "WHEN td.discount_value IS NULL THEN 0 \n" +
            "when td.discount_value > 0 " +
            "AND td.calculation_type IS NOT NULL " +
            "AND td.calculation_type = 'PERCENTAGE' \n" +
            "THEN (td.discount_value/100) * (ifnull(sbd.quantity,0) * ifnull(tp.trade_price,0)) " +
            "when td.discount_value > 0 " +
            "AND td.calculation_type IS NOT NULL " +
            "AND td.calculation_type = 'EQUAL' \n" +
            "THEN td.discount_value * ifnull(sbd.quantity,0) END) , 4) discounted_amount, " +

            "ROUND(SUM(ifnull(sbd.quantity,0) * ifnull(tp.trade_price,0)), 4) booking_amount, \n" +
            "SUM(ifnull(sbd.quantity,0) * ifnull(tp.trade_price,0)) booking_amount_without_cast, \n" +
            "ROUND(SUM(ordsbd.quantity * ifnull(tp.trade_price,0)), 4) order_converted_amount, \n" +
            "d.ship_to_address ship_to_address \n" +
            "FROM sales_booking sb \n" +

            "left join sales_booking_details sbd " +
            "on sb.id = sbd.sales_booking_id \n" +

            "left join ( select ord_sbd.sales_booking_id, sum(ord_sbd.quantity) quantity\n" +
            "            from sales_booking_details ord_sbd\n" +
            "            where ord_sbd.sales_booking_status='ORDER_CONVERTED'\n" +
            "            group by ord_sbd.sales_booking_id\n" +
            "            ) ordsbd on ordsbd.sales_booking_id = sb.id\n" +
            "            and sbd.sales_booking_status='ORDER_CONVERTED'\n" +

            "left join  (select sbd_r.sales_booking_id, count(sbd_r.product_id) rejected_count\n" +
            "            from sales_booking_details sbd_r where sbd_r.sales_booking_status = 'TICKET_REJECTED'\n" +
            "            group by sbd_r.sales_booking_id\n" +
            "            ) sbd_sts on sbd_sts.sales_booking_id = sb.id\n" +

            "left join product_trade_price tp " +
            "on sbd.product_trade_price_id = tp.id \n" +

            "inner join distributor d " +
            "on sb.distributor_id = d.id \n" +

            "inner join application_user au " +
            "on sb.sales_officer_id = au.id \n" +

            "inner join semester s " +
            "on sb.semester_id = s.id \n" +

            "inner join accounting_year ar " +
            "on s.accounting_year_id = ar.id \n" +

            "inner join invoice_nature inv " +
            "on sb.invoice_nature_id = inv.id \n" +

            "inner join reporting_manager rm \n" +
            "on sb.sales_officer_id = rm.application_user_id \n" +
            "and rm.to_date is null \n" +

            "inner join location_manager_map lmm \n" +
            "on rm.reporting_to_id = lmm.application_user_id \n" +
            "and lmm.to_date is null \n" +
            "and lmm.company_id = :company_id\n" +

            "inner join location lo " +
            "on lmm.location_id = lo.id \n" +

            "left join trade_discount td \n" +
            "on sbd.trade_discount_id = td.id \n" +
            "and sbd.product_id = td.product_id \n" +

            "left join designation ds " +
            "on au.designation_id = ds.id \n" +

            "left join depot dp " +
            "on sb.depot_id = dp.id \n" +

            "WHERE (:company_id is NULL OR sb.company_id = :company_id) \n" +
            "AND (:start_date is NULL OR sb.booking_date >= :start_date) \n" +
            "AND (:end_date is NULL OR sb.booking_date <= :end_date) \n" +
            "AND (:semester_id is NULL OR sb.semester_id = :semester_id) \n" +
            "AND sb.approval_status IN (:approvalStatus) \n" +
            "AND sb.sales_officer_id IN (:soList) \n" +
            "AND sb.is_active is true AND sb.is_deleted is false \n" +

            "GROUP BY sb.company_id, sb.id, \n" +
            "rm.reporting_to_id, lmm.location_id " +
            "ORDER BY sb.booking_date DESC \n", nativeQuery = true)
    List<Map<String, Object>> findSalesBookingList(@Param("company_id") Long companyId,
                                   @Param("start_date") LocalDateTime startDate,
                                   @Param("end_date") LocalDateTime endDate,
                                   @Param("semester_id") Long semesterId,
                                   @Param("soList") List<Long> soList,
                                   @Param("approvalStatus") List<String> approvalStatus);


    @Query(value = "SELECT " +
            "SUM(b.booking_quantity) total_booking_quantity, " +
            "SUM(b.booking_amount) total_booking_amount \n" +
            "FROM \n" +

            "(SELECT " +
            "sb.company_id, sb.semester_id, sb.booking_no, sb.booking_date, d.distributor_name, " +
            "au.name sales_officer_name, ds.name designation_name, " +
            "rm.reporting_to_id reporting_manager, lmm.location_id, lo.name location_name, " +
            "SUM(sbd.quantity) booking_quantity, " +
            "SUM(sbd.free_quantity) free_quantity, " +

            "SUM(CASE " +
            "WHEN td.discount_value IS NULL THEN 0 \n" +
            "WHEN td.discount_value > 0 " +
            "AND td.calculation_type IS NOT NULL " +
            "AND td.calculation_type = 'PERCENTAGE' \n" +
            "THEN (td.discount_value/100) * (sbd.quantity * tp.trade_price) \n" +
            "WHEN td.discount_value > 0 " +
            "AND td.calculation_type IS NOT NULL " +
            "AND td.calculation_type = 'EQUAL' \n" +
            "THEN td.discount_value * sbd.quantity END) discounted_amount, \n" +

            "SUM(sbd.quantity * tp.trade_price) booking_amount " +
            "FROM sales_booking sb \n" +

            "inner join sales_booking_details sbd " +
            "on sb.id = sbd.sales_booking_id \n" +

            "inner join product_trade_price tp " +
            "on sbd.product_trade_price_id = tp.id \n" +

            "inner join distributor d " +
            "on sb.distributor_id = d.id \n" +

            "inner join application_user au " +
            "on sb.sales_officer_id = au.id \n" +

            "inner join reporting_manager rm " +
            "on sb.sales_officer_id = rm.application_user_id " +
            "and rm.to_date is null \n" +

            "inner join location_manager_map lmm " +
            "on rm.reporting_to_id = lmm.application_user_id " +
            "and lmm.to_date is null and lmm.company_id =:company_id\n " +

            "inner join location lo " +
            "on lmm.location_id = lo.id \n" +

            "left join designation ds " +
            "on au.designation_id = ds.id \n" +

            "left join trade_discount td \n" +
            "on sbd.trade_discount_id = td.id \n" +
            "and sbd.product_id = td.product_id \n" +

            "inner join semester s " +
            "on sb.semester_id = s.id \n" +

            "inner join accounting_year ar " +
            "on s.accounting_year_id = ar.id \n" +

            "WHERE \n" +
            "(:company_id is NULL OR sb.company_id = :company_id) \n" +
            "AND (:start_date is NULL OR sb.booking_date >= :start_date) \n" +
            "AND (:end_date is NULL OR sb.booking_date <= :end_date) \n" +
            "AND (:semester_id is NULL OR sb.semester_id = :semester_id) \n" +
            "AND sb.sales_officer_id IN (:soList) \n" +
            "AND sb.is_active is true AND sb.is_deleted is false \n" +

            "GROUP BY sb.company_id, sb.id, \n" +
            "rm.reporting_to_id, lmm.location_id ) b " +
            "GROUP BY b.company_id, b.semester_id ", nativeQuery = true)
    Map findSalesBookingSummary(@Param("company_id") Long companyId,
                                   @Param("start_date") LocalDateTime startDate,
                                   @Param("end_date") LocalDateTime endDate,
                                   @Param("semester_id") Long semesterId,
                                   @Param("soList") List<Long> soList);


    @Query(value = "SELECT " +
            "sb.id booking_id, sb.company_id, sb.depot_id,\n" +
            "sb.distributor_id, sbd.id booking_item_id, sbd.product_id,\n" +
            "concat(p.name, ' ', p.item_size, ' ', u.abbreviation, ' * ', pk.pack_size) p_name,\n"+
            "p.product_sku, p.uom_id, u.abbreviation uom,\n" +
            "sbd.sales_booking_status item_status, pc.name category_name,\n" +
            "sbd.quantity booking_quantity, sbd.free_quantity,\n" +
            "tp.trade_price, sbd.quantity * tp.trade_price booking_amount,\n" +
            "\n" +
            "(CASE WHEN td.discount_value > 0 THEN tp.trade_price - SUM(CASE\n" +
            "WHEN td.discount_value IS NULL THEN 0\n" +
            "WHEN td.discount_value > 0\n" +
            "AND td.calculation_type IS NOT NULL\n" +
            "AND td.calculation_type = 'PERCENTAGE'\n" +
            "THEN (td.discount_value/100) * (tp.trade_price) \n" +
            "WHEN td.discount_value > 0\n" +
            "AND td.calculation_type IS NOT NULL\n" +
            "AND td.calculation_type = 'EQUAL'\n" +
            "THEN td.discount_value END) ELSE 0 END) discounted_price,\n" +
            "\n" +
            "SUM(CASE \n" +
            "WHEN td.discount_value IS NULL THEN 0\n" +
            "WHEN td.discount_value > 0\n" +
            "AND td.calculation_type IS NOT NULL\n" +
            "AND td.calculation_type = 'PERCENTAGE'\n" +
            "THEN (td.discount_value/100) * (sbd.quantity * tp.trade_price)\n" +
            "WHEN td.discount_value > 0\n" +
            "AND td.calculation_type IS NOT NULL\n" +
            "AND td.calculation_type = 'EQUAL' \n" +
            "THEN td.discount_value * sbd.quantity END)  discounted_amount,\n" +
            "ifnull(sum(mrp.quantity),0) ticket_quantity,\n" +
            "ifnull(sum(mrp.confirm_quantity),0) confirm_quantity,\n" +
            "(sbd.quantity-ifnull(sum(mrp.quantity),0))+ifnull(sum(mrp.confirm_quantity),0) total_confirm_quantity,\n" +
            "((sbd.quantity-ifnull(sum(mrp.quantity),0))+ifnull(sum(mrp.confirm_quantity),0)) * tp.trade_price confirm_amount,\n" +
            "\n" +
            "SUM(CASE\n" +
            "WHEN td.discount_value IS NULL THEN 0\n" +
            "            WHEN td.discount_value > 0\n" +
            "            AND td.calculation_type IS NOT NULL\n" +
            "            AND td.calculation_type = 'PERCENTAGE'\n" +
            "            THEN (td.discount_value/100) * ((sbd.quantity-ifnull(mrp.quantity,0))+ifnull(mrp.confirm_quantity,0)) * tp.trade_price\n" +
            "            WHEN td.discount_value > 0\n" +
            "            AND td.calculation_type IS NOT NULL\n" +
            "            AND td.calculation_type = 'EQUAL'\n" +
            "            THEN td.discount_value * ((sbd.quantity-ifnull(mrp.quantity,0))+ifnull(mrp.confirm_quantity,0)) END)  confirm_discounted_amount\n" +

            "FROM sales_booking sb \n" +
            "inner join sales_booking_details sbd \n" +
            "on sb.id = sbd.sales_booking_id \n" +

            "inner join product p \n" +
            "on sbd.product_id = p.id \n" +
            "inner join pack_size pk on p.pack_size_id = pk.id\n" +

            "inner join product_category pc \n" +
            "on p.product_category_id = pc.id \n" +

            "inner join product_trade_price tp \n" +
            "on sbd.product_trade_price_id = tp.id \n" +

            "inner join unit_of_measurement u \n" +
            "on p.uom_id = u.id \n" +

            "left join trade_discount td \n" +
            "on sbd.trade_discount_id = td.id \n" +
            "and sbd.product_id = td.product_id \n" +

            "left join material_receive_plan mrp \n" +
            "on mrp.sales_booking_details_id = sbd.id \n" +

            "where (:booking_id is NULL OR sb.id = :booking_id) \n" +
            "and sb.is_active is true and sb.is_deleted is false \n" +
            "and (sbd.sales_booking_status IN (:salesBookingStatus)) \n" +
            "group by sbd.id ", nativeQuery = true)
    List<Map<String, Object>> getSalesBookingToStockConfirm(@Param("booking_id") Long bookingId,
                        @Param("salesBookingStatus") List<String> salesBookingStatus);

    @Query(value = "SELECT " +
            "sbd.product_id, p.name p_name, p.product_sku, \n" +
            "concat(p.name, ' ', p.item_size, ' ', uom.abbreviation, ' * ', pk.pack_size) product_name,\n" +
            "sbd.sales_booking_status item_status, pc.name category_name, \n" +
            "sbd.quantity booking_quantity, sbd.free_quantity, \n" +
            "tp.trade_price, sbd.quantity * tp.trade_price booking_amount, " +
            "pk.pack_size as packSize, p.item_size as itemSize,\n" +
            "uom.abbreviation as abbreviation,\n" +
            "SUM(CASE " +
            "WHEN td.discount_value IS NULL THEN 0 \n" +
            "WHEN td.discount_value > 0 " +
            "AND td.calculation_type IS NOT NULL " +
            "AND td.calculation_type = 'PERCENTAGE' \n" +
            "THEN (td.discount_value/100) * (tp.trade_price) \n" +
            "WHEN td.discount_value > 0 " +
            "AND td.calculation_type IS NOT NULL " +
            "AND td.calculation_type = 'EQUAL' \n" +
            "THEN td.discount_value END)  discounted_price, \n" +

            "SUM(CASE " +
            "WHEN td.discount_value IS NULL THEN 0 \n" +
            "WHEN td.discount_value > 0 " +
            "AND td.calculation_type IS NOT NULL " +
            "AND td.calculation_type = 'PERCENTAGE' \n" +
            "THEN (td.discount_value/100) * (sbd.quantity * tp.trade_price) \n" +
            "WHEN td.discount_value > 0 " +
            "AND td.calculation_type IS NOT NULL " +
            "AND td.calculation_type = 'EQUAL' \n" +
            "THEN td.discount_value * sbd.quantity END)  discounted_amount " +

            "FROM sales_booking sb \n" +

            "inner join sales_booking_details sbd " +
            "on sb.id = sbd.sales_booking_id \n" +

            "inner join product p " +
            "on sbd.product_id = p.id \n" +
            "inner join pack_size pk on p.pack_size_id = pk.id\n" +

            "inner join product_category pc " +
            "on p.product_category_id = pc.id \n" +

            "inner join product_trade_price tp " +
            "on sbd.product_trade_price_id = tp.id \n" +

            "inner join unit_of_measurement as uom " +
            "ON p.uom_id = uom.id\n" +

            "left join trade_discount td \n" +
            "on sbd.trade_discount_id = td.id \n" +
            "and sbd.product_id = td.product_id \n" +

            "WHERE (:booking_id is NULL OR sb.id = :booking_id) \n" +
            "and sb.is_active is true and sb.is_deleted is false \n" +
            "GROUP BY sbd.id ", nativeQuery = true)
    List<Map> findSalesBookingDetails(@Param("booking_id") Long bookingId);

    @Query(value = "SELECT  sbd " +
            "FROM SalesBookingDetails sbd \n" +
//            "inner join com.newgen.ntlsnc.common.enums.SalesBookingStatus sts \n" +
//            "on sbd.salesBookingStatus.code = sts.code \n" +
            "WHERE (sbd.salesBooking.id = :booking_id) \n" )
    List<SalesBookingDetails> findSalesBookingProducts(@Param("booking_id") Long bookingId);

    @Query(value = "SELECT p.id as productId,\n" +
            "sb.id as salesBookingId,\n" +
            "sbd.id as salesBookingDetailsId,\n" +
            "p.name as productName,\n" +
            "sbd.quantity as quantity,\n" +
            "sbd.free_quantity freeQuantity,\n" +
            "uom.abbreviation as abbreviation,\n" +
            "pack_size as packSize,\n" +
            "p.item_size as itemSize,\n" +
            "ptp.trade_price as tradePrice,\n" +
            "ptp.id as productTradePriceId,\n" +
            "td.id as tradeDiscountId,\n" +
            "(ptp.trade_price - ifnull(case when td.calculation_type = \"PERCENTAGE\"\n" +
            "then ((ptp.trade_price * td.discount_value)/100)\n" +
            "else td.discount_value end,0)) priceWithDiscount,\n" +
            "(sbd.quantity * (ptp.trade_price - ifnull(case when td.calculation_type = \"PERCENTAGE\"\n" +
            "then ((ptp.trade_price * td.discount_value)/100)\n" +
            "else td.discount_value end,0))) productWiseTotalAmount \n" +
            "from sales_booking sb\n" +
            "inner join sales_booking_details sbd on sbd.sales_booking_id=sb.id \n" +
            "and sb.id=:salesBookingId\n" +
            "and sb.is_active is true and sb.is_deleted is false\n" +
            "and sbd.is_active is true and sbd.is_deleted is false\n" +
            "inner join product as p on sbd.product_id=p.id\n" +
            "and p.is_active is true and p.is_deleted is false\n" +
            "INNER JOIN unit_of_measurement as uom ON p.uom_id = uom.id \n" +
            "and uom.is_active is true and uom.is_deleted is false\n" +
            "INNER JOIN pack_size as ps ON p.pack_size_id = ps.id\n" +
            "and ps.is_active is true and ps.is_deleted is false\n" +
            "INNER JOIN product_trade_price as ptp ON ptp.id=sbd.product_trade_price_id\n" +
            "and ptp.is_active is true and ptp.is_deleted is false\n" +
            "LEFT JOIN trade_discount as td ON td.id = sbd.trade_discount_id\n" +
            "and td.is_active is true and td.is_deleted is false",nativeQuery = true)
    List<Map> findSalesBookingDetailsBySalesBookingId(@Param("salesBookingId") Long salesBookingId);

    @Query(value = "SELECT p.id as productId,\n" +
            "            p.name as productName,\n" +
            "            sbd.sales_booking_id as salesBookingId,\n" +
            "            sbd.id as salesBookingDetailsId,\n" +
            "            uom.abbreviation as abbreviation,\n" +
            "            pack_size as packSize,p.item_size as itemSize,\n" +
            "            ptp.trade_price as tradePrice,\n" +
            "            ptp.id as productTradePriceId,\n" +
            "            td.id as tradeDiscountId,\n" +
            "            sbd.trade_price as draftTradePrice,\n" +
            "            ifnull(sbd.quantity,0) as quantity, \n" +
            "            ifnull(sbd.free_quantity,0) freeQuantity,\n" +
            "            (ptp.trade_price - ifnull(case when td.calculation_type = \"PERCENTAGE\"\n" +
            "            then ((ptp.trade_price * td.discount_value)/100)\n" +
            "            else td.discount_value end,0)) priceWithDiscount,\n" +
            "            (sbd.trade_price - ifnull(case when sbd.calculation_type = \"PERCENTAGE\"\n" +
            "            then ((sbd.trade_price * sbd.discount_value)/100)\n" +
            "            else sbd.discount_value end,0)) draftPriceWithDiscount,\n" +
            "            ifnull((sbd.quantity * (sbd.trade_price - ifnull(case when sbd.calculation_type = \"PERCENTAGE\"\n" +
            "            then ((sbd.trade_price * sbd.discount_value)/100)\n" +
            "            else sbd.discount_value end,0))),0) productWiseTotalAmount\n" +
            "            FROM product as p\n" +
            "            INNER JOIN unit_of_measurement as uom ON p.uom_id = uom.id\n" +
            "            and p.product_category_id= :productCategoryId\n" +
            "            and p.is_active is true and p.is_deleted is false\n" +
            "            and uom.is_active is true and uom.is_deleted is false\n" +
            "            INNER JOIN pack_size as ps ON p.pack_size_id = ps.id\n" +
            "            and ps.is_active is true and ps.is_deleted is false\n" +
            "            INNER JOIN product_trade_price as ptp ON ptp.product_id=p.id \n" +
            "            and ptp.expiry_date is null  \n" +
            "            and ptp.is_active is true and ptp.is_deleted is false\n" +
            "            LEFT JOIN trade_discount as td ON td.semester_id = :semesterId\n" +
            "            and td.company_id = :companyId\n" +
            "            and td.product_id = p.id\n" +
            "            and td.invoice_nature_id = :invoiceNatureId\n" +
            "            and td.is_active is true and td.is_deleted is false\n" +
            "            LEFT JOIN (select sbd.id, sbd.sales_booking_id,sbd.quantity, sbd.product_id, \n" +
            "            sbd.free_quantity, ptps.trade_price, tds.calculation_type, tds.discount_value," +
            "            sbd.product_trade_price_id draftTradePriceId,sbd.trade_discount_id  draftTradeDiscountId" +
            "            from sales_booking_details as sbd \n" +
            "            INNER JOIN sales_booking as sb on sb.id = sbd.sales_booking_id and sb.invoice_nature_id = :invoiceNatureId \n" +
            "            and sbd.sales_booking_id = :salesBookingId \n" +
            "            and sbd.is_active is true and sbd.is_deleted is false\n" +
            "            INNER JOIN product_trade_price as ptps ON ptps.id =sbd.product_trade_price_id\n" +
            "            LEFT JOIN trade_discount as tds ON tds.id = sbd.trade_discount_id\n" +
            "            and tds.invoice_nature_id = :invoiceNatureId) sbd on p.id=sbd.product_id" +
            "            order by p.name, pack_size ASC",nativeQuery = true)
    List<Map<String, Object>> findAllProductListWithSalesBookingId(@Param("semesterId") Long semesterId, @Param("companyId") Long companyId,@Param("productCategoryId") Long productCategoryId, @Param("invoiceNatureId") Long invoiceNatureId, @Param("salesBookingId") Long salesBookingId);

    Optional<SalesBooking> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);
    List<SalesBooking> findByDistributorIdAndSalesOfficerAndCompanyIdAndSemester(Long distributorId, ApplicationUser salesOfficer, Long companyId, Semester semester);


    @Query(value = "select scsbd.booking_id as bookingId, scsbd.booking_no as bookingNo, \n" +
            "scsbd.booking_date as bookingDate, scsbd.tentative_delivery_date as tentativeDeliveryDate, \n" +
            "scsbd.approval_status as approvalStatus, scsbd.days_left as daysLeft, scsbd.depotName,\n" +
            "scsbd.depotContactNo, scsbd.address as depotAddress,\n"+
            "scsbd.distributor_name as distributorName, scsbd.sales_officer_name as salesOfficerName, \n" +
            "scsbd.contact_no as distributorContactNo, scsbd.ship_to_address as distributorAddress,\n"+
            "scsbd.designation_name as designation, scsbd.location_name as soLocationName, \n" +
            //"scsbd.booking_quantity as bookingQuantity, " +
            "ifnull((scsbd.booking_quantity - scsbd.ticket_quantity + scsbd.confirm_quantity), scsbd.booking_quantity) as bookingQuantity,\n" +
            "scsbd.booking_amount as bookingAmount, \n" +
            "ifnull(ocsbd.orderConvertedQuantity,0) as orderConvertedQuantity \n" +
            "from \n" +
            "(SELECT sb.id booking_id, sb.booking_no,\n" +
            "sb.booking_date,sb.approval_status, sb.tentative_delivery_date, \n" +
            "DATEDIFF( sb.tentative_delivery_date, sb.booking_date ) as days_left, d.distributor_name, \n" +
            "d.contact_no, d.ship_to_address,\n"+
            "au.name sales_officer_name, ds.name designation_name, \n" +
            "rm.reporting_to_id reporting_manager, dp.depot_name as depotName,\n" +
            "dp.contact_number as depotContactNo, dp.address,\n"+
            "lmm.location_id, lo.name location_name,  \n" +
            "sum(sbd.quantity) booking_quantity, sum(sbd.quantity * tp.trade_price) booking_amount,\n" +
            "sum(mrp.quantity) ticket_quantity, sum(mrp.confirm_quantity) confirm_quantity\n" +
            "FROM sales_booking sb \n" +
            "inner join sales_booking_details sbd on sb.id = sbd.sales_booking_id \n" +
            "and sbd.sales_booking_status in(\"STOCK_CONFIRMED\", \"PARTIAL_ORDER_CONVERTED\") and sb.approval_status=:approvalStatus \n" +
            "and sb.is_active is true and sb.is_deleted is false and sb.company_id=:companyId \n" +
            "and (:depotId is null or sb.depot_id =:depotId) \n" +
            "and sbd.is_active is true and sbd.is_deleted is false \n"+
            "and sb.booking_date >=:startDate and sb.booking_date <= :endDate \n" +
            "and sb.sales_officer_id in(:salesOfficerList) \n" +
            "inner join product_trade_price tp on sbd.product_trade_price_id = tp.id \n" +
            "inner join distributor d on sb.distributor_id = d.id \n" +
            "inner join application_user au on sb.sales_officer_id = au.id \n" +
            "inner join reporting_manager rm \n" +
            "on sb.sales_officer_id = rm.application_user_id \n" +
            "and rm.to_date is null \n" +
            "inner join location_manager_map lmm \n" +
            "on rm.reporting_to_id = lmm.application_user_id \n" +
            "and lmm.to_date is null and lmm.company_id =:companyId\n" +
            "inner join location lo on lmm.location_id = lo.id \n" +
            "left join material_receive_plan mrp \n" +
            "on mrp.sales_booking_details_id = sbd.id and mrp.ticket_status='CONFIRMED'\n" +
            "left join designation ds on au.designation_id = ds.id \n" +
            "left join depot dp on sb.depot_id = dp.id \n" +
            "group by sb.id, \n" +
            "rm.reporting_to_id, lmm.location_id \n" +
            "order by sb.id desc) as scsbd\n" +
            "                             left join \n" +
            "(select sbco.id as bookingId, sum(sod.quantity) as orderConvertedQuantity \n" +
            "from sales_booking sbco \n" +
            "inner join sales_booking_details sbdco on sbco.id = sbdco.sales_booking_id \n" +
            "inner join sales_order_details sod on sbdco.id = sod.sales_booking_details_id \n"+
            "and sbco.is_active is true and sbco.is_deleted is false and sbco.company_id=:companyId \n" +
            "and sbco.booking_date >=:startDate and sbco.booking_date <= :endDate \n" +
            "and sbco.sales_officer_id in(:salesOfficerList) \n" +
            "and sales_booking_status in(\"STOCK_CONFIRMED\", \"PARTIAL_ORDER_CONVERTED\") group by sbdco.sales_booking_id) as ocsbd\n" +
            "on scsbd.booking_id = ocsbd.bookingId; ", nativeQuery = true)
    List<Map<String, Object>> getSalesBookingListForSalesOrderCreation(
            @Param("companyId") Long companyId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("salesOfficerList") List<Long> salesOfficerList,
            @Param("approvalStatus") List<String> approvalStatus, Long depotId);


        @Query(value = "select scsbd.bookingDetailsId, scsbd.productId, scsbd.productSku, \n" +
                "scsbd.productTradePriceId, scsbd.tradeDiscountId,\n"+
            "scsbd.productName, scsbd.productCategory, scsbd.sales_booking_status,scsbd.bookingQuantity, \n" +
            "scsbd.freeQuantity, ifnull(ocsbd.orderConvertedQuantity,0) as orderConvertedQuantity,\n" +
                "(ifnull((scsbd.bookingQuantity- scsbd.ticketQuantity + scsbd.confirmedQuantity), scsbd.bookingQuantity) - ifnull(ocsbd.orderConvertedQuantity,0)) as remainingBookingQuantity, \n" +
            "scsbd.tradePrice,scsbd.tradeDiscount,scsbd.discountedPrice, scsbd.confirmedQuantity   from \n" +
            "(SELECT sbd.id as bookingDetailsId, prod.id as productId,ptp.id as productTradePriceId, \n" +
                "td.id as tradeDiscountId, prod.product_sku as productSku, \n" +
            "   concat(prod.name,\" \",prod.item_size,\" \",uom.abbreviation,\" * \",ps.pack_size) as productName, pc.name as productCategory, \n" +
            "   sbd.quantity as bookingQuantity, sbd.free_quantity as freeQuantity, sbd.sales_booking_status, \n" +
                "mrp.quantity as ticketQuantity, mrp.confirm_quantity as confirmedQuantity, \n" +
            "   ptp.trade_price as tradePrice,ifnull(case when td.calculation_type = \"PERCENTAGE\" \n" +
            "            then ((ptp.trade_price * td.discount_value)/100)\n" +
            "            else td.discount_value end,0) as tradeDiscount,\n" +
            "ptp.trade_price - (case when td.calculation_type = \"PERCENTAGE\" \n" +
                "            then ((ptp.trade_price * td.discount_value)/100)\n" +
                "            else td.discount_value end) as discountedPrice \n"+
            "from sales_booking_details sbd \n" +
            "inner join product prod on sbd.product_id = prod.id and sbd.sales_booking_id =:salesBookingId \n" +
            "and sbd.sales_booking_status in(\"STOCK_CONFIRMED\", \"PARTIAL_ORDER_CONVERTED\") \n" +
            "and sbd.is_active is true and sbd.is_deleted is false\n" +
            "left join material_receive_plan mrp \n" +
            "on mrp.sales_booking_details_id = sbd.id and mrp.ticket_status='CONFIRMED'\n" +
            "inner join unit_of_measurement uom on uom.id = prod.uom_id\n" +
            "inner join pack_size ps on prod.pack_size_id = ps.id\n" +
            "inner join product_category pc on prod.product_category_id = pc.id\n" +
            "inner join product_trade_price ptp on sbd.product_trade_price_id = ptp.id\n" +
            "left join  trade_discount td on sbd.trade_discount_id = td.id and td.approval_status = \"APPROVED\"\n" +
            ") as scsbd\n" +
            "                                 left join \n" +
            "(select sbdco.id as bookingDetailsId, sum(sod.quantity) as orderConvertedQuantity \n" +
            "from sales_booking_details sbdco\n" +
            "inner join sales_order_details sod on sbdco.id = sod.sales_booking_details_id \n" +
            "and sbdco.sales_booking_id =:salesBookingId \n" +
            "and sbdco.sales_booking_status in(\"STOCK_CONFIRMED\", \"PARTIAL_ORDER_CONVERTED\") \n" +
            "and sbdco.is_active is true and sbdco.is_deleted is false group by bookingDetailsId\n" +
            " ) as ocsbd\n" +
            "on scsbd.bookingDetailsId = ocsbd.bookingDetailsId; ", nativeQuery = true)
    List<Map<String, Object>> getSalesBookingAndSalesOrderDetails(@Param("salesBookingId") Long salesBookingId);

    @Query(value = "SELECT  \n" +
            "COUNT(sbd.product_id) product_count,\n" +
            "sb.id bookingId, \n" +
            "sb.booking_no bookingNo,\n" +
            "sb.booking_date, MONTH(sb.booking_date) AS month, YEAR(sb.booking_date) AS year, \n" +
            "DATE_FORMAT(sb.booking_date, '%b %Y') AS year_and_month, \n" +
            "round(SUM(CASE WHEN td.discount_value IS NULL THEN round((sbd.quantity * tp.trade_price),4)\n" +
            "when td.discount_value > 0 AND td.calculation_type IS NOT NULL AND td.calculation_type = 'PERCENTAGE' \n" +
            "THEN (round((sbd.quantity * tp.trade_price),4) - round(((td.discount_value/100) * (sbd.quantity * tp.trade_price)),4))\n" +
            "when td.discount_value > 0 AND td.calculation_type IS NOT NULL AND td.calculation_type = 'EQUAL' \n" +
            "THEN (round((sbd.quantity * tp.trade_price),4) - round((td.discount_value * sbd.quantity),4)) END),2) bookingAmount,\n" +
            "ROUND( SUM(CASE WHEN td.discount_value IS NULL THEN 0 \n" +
            "when td.discount_value > 0 AND td.calculation_type IS NOT NULL AND td.calculation_type = 'PERCENTAGE' \n" +
            "THEN (td.discount_value/100) * (sbd.quantity * tp.trade_price) when td.discount_value > 0 AND td.calculation_type IS NOT NULL AND td.calculation_type = 'EQUAL' \n" +
            "THEN td.discount_value * sbd.quantity END) , 4) discounted_amount,\n" +
            "dp.depot_name, inv.name invoice_nature, ar.fiscal_year_name, \n" +
            "sb.approval_status, sb.notes, sb.approval_date, SUM(sbd.free_quantity) free_quantity,\n" +
            "SUM(sbd.quantity) booking_quantity,\n" +
            "d.distributor_name  distributorName,\n" +
            "@var\\:=:approvalActor approvalActor,\n" +
            "@var\\:=:level level,\n" +
            "@var\\:=:approvalStepId approvalStepId,\n" +
            "@var\\:=:multiLayerApprovalPathId multiLayerApprovalPathId,\n" +
            "@var\\:=:approvalActorId approvalActorId, \n" +
            "@var\\:=:approvalStatusName approvalStatus, \n" +
            "@var\\:=:approvalStepName approvalStepName \n" +
            "FROM sales_booking sb \n" +
            "inner join sales_booking_details sbd on sb.id = sbd.sales_booking_id \n" +
            "inner join product_trade_price tp on sbd.product_trade_price_id = tp.id \n" +
            "inner join distributor d on sb.distributor_id = d.id \n" +
            "inner join depot dp on sb.depot_id = dp.id \n" +
            "inner join invoice_nature inv on sb.invoice_nature_id = inv.id\n" +
            "inner join semester s on sb.semester_id = s.id  \n" +
            "inner join accounting_year ar on s.accounting_year_id = ar.id \n" +
            "left join trade_discount td \n" +
            "on sbd.trade_discount_id = td.id \n" +
            "and sbd.product_id = td.product_id \n" +
            "WHERE (:companyId is NULL OR sb.company_id = :companyId) \n" +
            "AND sb.approval_status = :approvalStatus \n" +
            "AND (COALESCE(:soList) is NULL OR sb.sales_officer_id IN (:soList))\n" +
            "AND sb.is_active is true AND sb.is_deleted is false \n" +
            "GROUP BY sb.company_id, sb.id order by sb.booking_date desc", nativeQuery = true)
    List<Map<String, Object>> getPendingListForApproval(@Param("companyId") Long companyId,
                                                   @Param("soList") List<Long> soList,
                                                   @Param("approvalStatus") String approvalStatus,
                                                   @Param("approvalActor") String approvalActor,
                                                   @Param("level") Integer level,
                                                   @Param("approvalStepId") Long approvalStepId,
                                                   @Param("multiLayerApprovalPathId") Long multiLayerApprovalPathId,
                                                   @Param("approvalActorId") Long approvalActorId,
                                                   @Param("approvalStatusName") String approvalStatusName,
                                                   @Param("approvalStepName") String approvalStepName);

    @Query(value = "select b.id         id\n" +
            "     , b.booking_no booking_no\n" +
            "from sales_booking b\n" +
            "where b.company_id = :companyId \n" +
            "  and b.distributor_id = :distributorId\n" +
            "  and b.sales_officer_id = :salesOfficerId\n" +
            "  and b.is_active is true\n" +
            "  and b.is_deleted is false\n" +
            "  and b.approval_status = 'PENDING'",nativeQuery = true)
    List<Map> getPendingBookingListByCompanyAndDistributorAndSalesOfficer(Long companyId, Long distributorId, Long salesOfficerId);

    @Query(value = "SELECT sum(sbd.quantity * (ptp.trade_price - ifnull(case\n" +
            "                                                        when td.calculation_type = \"PERCENTAGE\"\n" +
            "                                                            then ((ptp.trade_price * td.discount_value) / 100)\n" +
            "                                                        else td.discount_value end, 0))) total_amount\n" +
            "from sales_booking sb\n" +
            "         inner join sales_booking_details sbd\n" +
            "                    on sbd.sales_booking_id = sb.id\n" +
            "                        and sb.id = 82\n" +
            "                        and sb.is_active is true\n" +
            "                        and sb.is_deleted is false\n" +
            "                        and sbd.is_active is true\n" +
            "                        and sbd.is_deleted is false\n" +
            "         INNER JOIN product_trade_price as ptp\n" +
            "                    ON ptp.id = sbd.product_trade_price_id\n" +
            "                        and ptp.is_active is true\n" +
            "                        and ptp.is_deleted is false\n" +
            "         LEFT JOIN trade_discount as td\n" +
            "                   ON td.id = sbd.trade_discount_id\n" +
            "                       and td.is_active is true\n" +
            "                       and td.is_deleted is false",nativeQuery = true)
    Map getTotalAmountBySalesBookingId(Long salesBookingId);

    @Query(value = "SELECT map.id mapping_id, map.created_date date, au.id user_id, \n" +
            "au.name approver_name, dg.name approver_designation, \n" +
            "map.approval_status, map.level, map.comments \n" +
            "from multi_layer_approval_process map\n"+
            "inner join application_user au on map.action_taken_by_id = au.id\n" +
            "and map.ref_table = 'SalesBooking'\n" +
            "and map.is_active is true\n" +
            "and map.is_deleted is false\n" +
            "inner join designation dg on au.designation_id = dg.id\n" +
            "where map.ref_id = :booking_id \n"
            ,nativeQuery = true)
    List<Map<String, Object>> getSalesBookingActivitiesList(@Param("booking_id") Long bookingId);

    List<SalesBooking> findByCompanyIdAndSalesOfficerId(Long companyId, Long salesOfficerId);
}
