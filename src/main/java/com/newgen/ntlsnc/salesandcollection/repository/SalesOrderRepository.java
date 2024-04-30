package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.salesandcollection.entity.SalesOrder;
import com.newgen.ntlsnc.salesandcollection.entity.SalesOrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author kamal
 * @Date ১২/৪/২২
 */

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    List<SalesOrder> findAllByIsDeletedFalse();

    @Query(value = "\n" +
            "select salord.salesOrderId, salord.salesOrderNo, salord.approvalStatus,\n" +
            "salord.salesOrderDate,salbookdet.salesBookingId,\n" +
            "salbookdet.distributorId, salbookdet.distributorName, salbookdet.salesOfficer,\n" +
            "salbookdet.quantity,salbookdet.freeQuantity, salbookdet.orderAmount, \n" +
            "salbookdet.tradeDiscount  from \n" +
            "(select so.sales_booking_id as salesBookingId, so.id as salesOrderId, so.order_no as salesOrderNo, \n" +
            "so.approval_status as approvalStatus, so.order_date as salesOrderDate from sales_order so where \n" +
            "so.order_date >= :startDate and so.order_date <= :endDate) as salord\n" +
            "                               inner join \n" +
            "(select sod.sales_order_id as salesOrderId, sb.id as salesBookingId, dis.id as distributorId, \n" +
            "dis.distributor_name as distributorName, au.name as salesOfficer, \n" +
            "sum(sod.quantity) as quantity,  sum(sbd.free_quantity) as freeQuantity, \n" +
            "sum(sod.quantity * ptp.trade_price) as orderAmount, \n" +
            "sum(ifnull(case when calculation_type = 'PERCENTAGE' \n" +
            "then ((ptp.trade_price * td.discount_value)/100)\n" +
            "else td.discount_value end,0)) as tradeDiscount from sales_booking sb \n" +
            "inner join sales_booking_details sbd on sb.id = sbd. sales_booking_id \n" +
            "and sb.company_id = :companyId and sbd.is_active is true and sbd.is_deleted is false and \n" +
            "sb.sales_officer_id IN (:salesOfficerUserLoginId)\n" +
            "inner join distributor dis on sb.distributor_id = dis.id\n" +
            "inner join application_user au on sb.sales_officer_id = au.id\n" +
            "inner join sales_order_details sod on sbd.id = sod.sales_booking_details_id \n" +
            "and sod.is_active is true and sod.is_deleted is false \n" +
            "inner join product_trade_price ptp on sbd.product_trade_price_id = ptp.id \n" +
            "and ptp.is_active is true and ptp.is_deleted is false \n" +
            "left join trade_discount td on sbd.trade_discount_id = td.id \n" +
            " and sb.semester_id = td.semester_id  \n" +
            "and td.is_active is true and td.is_deleted is false \n" +
            "and td.approval_status='APPROVED' group by sod.sales_order_id,sb.id,dis.id) as salbookdet \n" +
            "on salord.salesOrderId = salbookdet.salesOrderId \n" +
            "order by salord.salesOrderId, salord.salesOrderNo;",nativeQuery = true)
    List<Map<String, Object>> getSalesOrderOverView(
            @Param("salesOfficerUserLoginId") List<Long> salesOfficerUserLoginId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("companyId") Long companyId);


    @Query(value = "select so.id, so.order_no, prod.product_sku as productSku, \n" +
            "concat(prod.name, ' ', prod.item_size, ' ', uom.abbreviation, ' * ', ps.pack_size) as productName, \n" +
            "ps.description, pc.name as productCategory, \n" +
            "ptp.trade_price as actualPrice, \n" +
            "sod.quantity as quantity, sbd.free_quantity as freeQuantity, \n" +
            "@tradeDiscount \\:=round(ifnull((case when calculation_type = 'PERCENTAGE' \n" +
            "then ((ptp.trade_price * td.discount_value)/100) \n" +
            "else td.discount_value end), 0),4) as tradeDiscount, \n" +
            "@priceAfterDiscount \\:=round((ptp.trade_price - @tradeDiscount),2) as price, \n" +
            "round((sod.quantity * @priceAfterDiscount),4) as orderAmount from sales_booking sb \n" +
            "inner join sales_booking_details sbd on sb.id = sbd. sales_booking_id \n" +
            "and sbd.sales_booking_status in ('ORDER_CONVERTED','PARTIAL_ORDER_CONVERTED') \n" +
            "inner join sales_order_details sod on sbd.id = sod.sales_booking_details_id \n" +
            "inner join sales_order so on sod.sales_order_id = so.id and so.id = :salesOrderId \n" +
            "inner join product prod on sbd.product_id = prod.id \n" +
            "left join pack_size ps on prod.pack_size_id = ps.id \n"+
            "left join unit_of_measurement uom on prod.uom_id = uom.id \n" +
            "inner join product_category pc on prod.product_category_id = pc.id \n" +
            "inner join product_trade_price ptp on sbd.product_trade_price_id = ptp.id \n" +
            "left join trade_discount td on sbd.trade_discount_id = td.id\n" +
            "and td.product_id = ptp.product_id and td.approval_status='APPROVED' \n" +
            "order by so.id, prod.id,prod.product_sku, prod.name, pc.name;", nativeQuery = true)
    List<Map<String, Object>> getSalesOrderDetails(@Param("salesOrderId") Long salesOrderId);

    @Query(value = "select so.order_no as orderNo, dep.depot_name as depotName,\n" +
            "invn.name as invoiceNature, sb.booking_no as bookingNo, \n" +
            "sum(sod.quantity) as quantity, sum(sbd.free_quantity) as freeQuantity, \n" +
            "round(sum(sod.quantity * ptp.trade_price),4) as totalOrderAmount, \n" +
            "sum(round(ifnull((case when calculation_type = 'PERCENTAGE' \n" +
            "then (((ptp.trade_price * td.discount_value)/100)) \n" +
            "else td.discount_value end), 0),2) * sod.quantity) as tradeDiscount from sales_booking sb \n" +
            "inner join sales_booking_details sbd on sb.id = sbd. sales_booking_id and \n" +
            "sbd.sales_booking_status in ('ORDER_CONVERTED','PARTIAL_ORDER_CONVERTED')  \n" +
            "left join depot dep on sb.depot_id = dep.id \n" +
            "left join invoice_nature invn on sb.invoice_nature_id = invn.id \n" +
            "inner join sales_order_details sod on sbd.id = sod.sales_booking_details_id \n" +
            "inner join sales_order so on sod.sales_order_id = so.id and so.id = :salesOrderId \n" +
            "inner join product prod on sbd.product_id = prod.id \n" +
            "inner join product_trade_price ptp on sbd.product_trade_price_id = ptp.id \n" +
            "left join trade_discount td on sbd.trade_discount_id = td.id\n" +
            "and td.approval_status='APPROVED'  \n" +
            "group by so.order_no, sb.booking_no, invn.name, dep.depot_name;", nativeQuery = true)
    Map<String, Object> getSalesOrderSummary(@Param("salesOrderId") Long salesOrderId);


    @Query(value = "select sb.created_date as salesBookingAppliedDate, invn.name as paymentMethod, \n" +
            "sb.approval_date as approvalDate, \n" +
            "@invoiceStatus \\:= (case when (select 1 FROM inv_transaction_details where sales_order_details_id \n" +
            "in(select id from sales_order_details where sales_order_id = :salesOrderId) \n" +
            "group by sales_order_details_id having count(distinct sales_order_details_id) =  \n" +
            "(select count(id) from sales_order_details where sales_order_id = :salesOrderId)) = 1 then 'Done' \n" +
            "else 'Pending' end) as deliveryStatus, @invoiceStatus as invoiceStatus, \n" +
            "(case when (select id from sales_invoice where id in(select sales_invoice_id from \n" +
            "sales_invoice_challan_map where inv_delivery_challan_id \n" +
            "in(select id from inv_delivery_challan where inv_transaction_id \n" +
            "in(select inv_transaction_id from inv_transaction_details where sales_order_details_id \n" +
            "in(select id from sales_order_details where sales_order_id = :salesOrderId)))) \n" +
            "and is_accepted = 1 \n" +
            "group by id having count(distinct id)  = (select count(id) from \n" +
            "sales_order_details where sales_order_id = :salesOrderId)) then 'Done' else 'Pending' end) as \n" +
            "invoiceAcknowledgementStatus from sales_booking sb \n" +
            "inner join invoice_nature invn on sb.invoice_nature_id = invn.id \n" +
            "inner join sales_order so on sb.id = so.sales_booking_id and so.id = :salesOrderId", nativeQuery = true)
    Map<String, Object> getSalesOrderLifeCycle(@Param("salesOrderId") Long salesOrderId);

    @Query(value = "SELECT  so " + //java.lang.Integer(so.id)
            "FROM SalesOrder so \n" +
            "WHERE (so.salesBooking.id = :booking_id) \n" )
    List<SalesOrder> getSalesOrderListByBookingId(@Param("booking_id") Long bookingId);

    @Query(value = "SELECT  sod " +
            "FROM SalesOrderDetails sod \n" +
            "WHERE (sod.salesOrder.id = :order_id) \n" )
    List<SalesOrderDetails> getSalesOrderProducts(@Param("order_id") Long salesOrderId);

    List<SalesOrder> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<SalesOrder> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    @Query(value = "select d.distributor_name distributor_name, \n" +
            "d.id distributor_id,\n" +
            "lo.name location_name,\n" +
            "count(sod.id) product_count,\n" +
            "sum(((sod.quantity * ptp.trade_price) -\n" +
            "ifnull(case when td.calculation_type = 'PERCENTAGE'\n" +
            "then ((ptp.trade_price * td.discount_value)/100)\n" +
            "else td.discount_value end,0))) booking_amount,\n" +
            "DATE_FORMAT(sb.booking_date, '%b %Y') AS year_and_month,\n" +
            "sb.booking_no, ar.fiscal_year_name, sb.booking_date, sb.approval_status,\n" +
            "SUM(sbd.quantity) booking_quantity, sb.approval_date, sb.notes,\n" +
            "sb.semester_id, dp.depot_name,sb.id booking_id,\n" +
            "inv.name invoice_nature, d.contact_no distributor_contact_no,\n" +
            "SUM(sbd.free_quantity) free_quantity,\n" +
            "SUM(CASE " +
            "WHEN td.discount_value IS NULL THEN 0\n" +
            "when td.discount_value > 0 " +
            "AND td.calculation_type IS NOT NULL " +
            "AND td.calculation_type = 'PERCENTAGE' \n" +
            "THEN (td.discount_value/100) * (sbd.quantity * ptp.trade_price) " +
            "when td.discount_value > 0 " +
            "AND td.calculation_type IS NOT NULL " +
            "AND td.calculation_type = 'EQUAL' \n" +
            "THEN td.discount_value * sbd.quantity END) discounted_amount " +

            "from sales_order so \n" +
            "inner join sales_order_details sod on sod.sales_order_id = so.id  \n" +
            "and so.is_active = true and so.is_deleted = false\n" +
            "and sod.is_active = true and sod.is_deleted = false\n" +
            "inner join sales_booking sb on sb.id = so.sales_booking_id\n" +
            "and sb.is_active = true and sb.is_deleted = false\n" +
            "and sb.approval_status='APPROVED' and sb.sales_officer_id IN (:soList) and sb.company_id = :companyId \t\n" +
            "and (:startDate is NULL or sb.booking_date >= :startDate) \n" +
            "and (:endDate is NULL or sb.booking_date <= :endDate) \n" +
            "and (:semesterId is NULL or sb.semester_id = :semesterId) \n" +
            "inner join distributor d on d.id=sb.distributor_id\n" +
            "and d.is_active = true and d.is_deleted = false\n" +
            "inner join sales_booking_details sbd on sbd.sales_booking_id = sb.id\n" +
            "and sbd.is_active = true and sbd.is_deleted = false\n" +
            "inner join product_trade_price ptp on ptp.id = sbd.product_trade_price_id\n" +
            "and ptp.is_active = true and ptp.is_deleted = false\n" +
            "inner join depot dp " +
            "on sb.depot_id = dp.id \n" +
            "inner join invoice_nature inv " +
            "on sb.invoice_nature_id = inv.id \n" +
            "inner join semester s " +
            "on sb.semester_id = s.id \n" +
            "inner join accounting_year ar " +
            "on s.accounting_year_id = ar.id \n" +

            "inner join reporting_manager rm \n" +
            "on sb.sales_officer_id = rm.application_user_id \n" +
            "and rm.to_date is null \n" +

            "inner join location_manager_map lmm \n" +
            "on rm.reporting_to_id = lmm.application_user_id \n" +
            "and lmm.to_date is null and lmm.company_id =:companyId\n" +

            "inner join location lo " +
            "on lmm.location_id = lo.id \n" +

            "left join trade_discount td on td.id = sbd.trade_discount_id \n" +
            "and td.is_active = true and td.is_deleted = false\n" +
            "group by so.id, lo.id", nativeQuery = true)
    List<Map<String, Object>> findSaleOrderListBySO(@Param("companyId") Long companyId,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   @Param("semesterId") Long semesterId,
                                   @Param("soList") List<Long> soList);

    @Query(value = "select salord.salesBookingId, salord.salesOrderId, salord.salesOrderNo, \n" +
            "salord.salesOrderDate, salbookdet.quantity,salbookdet.orderAmount from \n" +
            "(select so.sales_booking_id as salesBookingId, so.id as salesOrderId, \n" +
            "so.order_no as salesOrderNo, \n" +
            "so.order_date as salesOrderDate from sales_order so where \n" +
            "so.order_date >= :startDate and so.order_date <= :endDate) as salord \n" +
            "                               inner join \n" +
            "(select sod.sales_order_id as salesOrderId, sb.id as salesBookingId,\n" +
            "sum(sod.quantity) as quantity,\n" +
            "round(sum(sod.quantity * ptp.trade_price),4) as orderAmount \n" +
            "from sales_booking sb \n" +
            "inner join sales_booking_details sbd on sb.id = sbd. sales_booking_id \n" +
            "and sb.company_id = :companyId and sbd.is_active is true and sbd.is_deleted is false \n" +
            "and (:semesterId is null or sb.semester_id = :semesterId) \n" +
            "inner join (select * from sales_order_details  where not exists( \n" +
            "select itd.sales_order_details_id from inv_delivery_challan idc \n" +
            "inner join inv_transaction it on idc.inv_transaction_id = it.id \n" +
            "and it.transaction_type = 'DELIVERY_CHALLAN' and idc.is_active is true \n" +
            "and idc.is_deleted is false\n" +
            "inner join inv_transaction_details itd on it.id = itd.inv_transaction_id)) sod \n" +
            "on sbd.id = sod.sales_booking_details_id \n" +
            "and sod.is_active is true and sod.is_deleted is false \n" +
            "inner join product_trade_price ptp on sbd.product_trade_price_id = ptp.id \n" +
            "and ptp.is_active is true and ptp.is_deleted is false  group by sod.sales_order_id,sb.id) as salbookdet \n" +
            "on salord.salesOrderId = salbookdet.salesOrderId \n" +
            "order by salord.salesOrderId desc, salord.salesOrderNo desc \n", nativeQuery = true)
    List<Map<String, Object>> undeliveredSalesOrderList(@Param("companyId") Long companyId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate,
                                                        @Param("semesterId") Long semesterId);

    @Query(value = "select ocsbd.salesOrderDetailsId, scsbd.bookingDetailsId, scsbd.productId, scsbd.productSku, \n" +
            "scsbd.productTradePriceId, scsbd.tradeDiscountId,\n"+
            "scsbd.productName, scsbd.productCategory, scsbd.sales_booking_status,scsbd.bookingQuantity, \n" +
            "scsbd.freeQuantity, ifnull(ocsbd.orderQuantity,0) as orderConvertedQuantity,\n" +
            "(scsbd.bookingQuantity - ifnull(pocsbd.orderConvertedQuantity,0)) as remainingBookingQuantity, \n" +
            "scsbd.tradePrice,scsbd.tradeDiscount   from \n" +
            "(SELECT sbd.id as bookingDetailsId, prod.id as productId,ptp.id as productTradePriceId, \n" +
            "td.id as tradeDiscountId, prod.product_sku as productSku, \n" +
            "   concat(prod.name,\" \", ps.description) as productName, pc.name as productCategory, \n" +
            "   sbd.quantity as bookingQuantity, sbd.free_quantity as freeQuantity, sbd.sales_booking_status, \n" +
            "   ptp.trade_price as tradePrice,ifnull(case when td.calculation_type = \"PERCENTAGE\" \n" +
            "            then ((ptp.trade_price * td.discount_value)/100)\n" +
            "            else td.discount_value end,0) as tradeDiscount\n" +
            "from sales_booking_details sbd \n" +
            "inner join product prod on sbd.product_id = prod.id and sbd.sales_booking_id =:salesBookingId \n" +
            "and sbd.sales_booking_status in(\"ORDER_CONVERTED\", \"PARTIAL_ORDER_CONVERTED\") \n" +
            "and sbd.is_active is true and sbd.is_deleted is false\n" +
            "inner join pack_size ps on prod.pack_size_id = ps.id\n" +
            "inner join product_category pc on prod.product_category_id = pc.id\n" +
            "inner join product_trade_price ptp on sbd.product_trade_price_id = ptp.id\n" +
            "left join  trade_discount td on sbd.trade_discount_id = td.id and td.approval_status = \"APPROVED\"\n" +
            ") as scsbd\n" +
            "                                 inner join \n" +
            "(select sod.id as salesOrderDetailsId, sbdco.id as bookingDetailsId, sum(sod.quantity) as orderQuantity \n" +
            "from sales_booking_details sbdco\n" +
            "inner join sales_order_details sod on sbdco.id = sod.sales_booking_details_id \n" +
            "and sbdco.sales_booking_id =:salesBookingId and sod.sales_order_id = :salesOrderId \n" +
            "and sbdco.sales_booking_status in(\"ORDER_CONVERTED\", \"PARTIAL_ORDER_CONVERTED\") \n" +
            "and sbdco.is_active is true and sbdco.is_deleted is false group by bookingDetailsId, sod.id\n" +
            " ) as ocsbd\n" +
            "on scsbd.bookingDetailsId = ocsbd.bookingDetailsId \n"+
            "                                  left join \n" +
            "(select sbdco.id as bookingDetailsId, sum(sod.quantity) as orderConvertedQuantity \n" +
            "from sales_booking_details sbdco\n" +
            "inner join sales_order_details sod on sbdco.id = sod.sales_booking_details_id \n" +
            "and sbdco.sales_booking_id =:salesBookingId\n" +
            "and sbdco.sales_booking_status in(\"ORDER_CONVERTED\", \"PARTIAL_ORDER_CONVERTED\") \n" +
            "and sbdco.is_active is true and sbdco.is_deleted is false group by bookingDetailsId\n" +
            " ) as pocsbd\n" +
            " \n" +
            "on ocsbd.bookingDetailsId = pocsbd.bookingDetailsId; \n", nativeQuery = true)
    List<Map<String, Object>> getSalesBookingDetailsInSalesOrder(
            @Param("salesBookingId") Long salesBookingId,
            @Param("salesOrderId") Long salesOrderId);


    @Query(value = "SELECT  \n" +
            "COUNT(sbd.product_id) product_count,\n" +
            "so.id orderId, \n" +
            "so.order_no orderNo,\n" +
            "so.delivery_date, MONTH(so.delivery_date) AS month, YEAR(so.delivery_date) AS year, \n" +
            "DATE_FORMAT(so.delivery_date, '%b %Y') AS year_and_month, \n" +
            "round(SUM(CASE WHEN td.discount_value IS NULL THEN round((sod.quantity * tp.trade_price),4)\n" +
            "when td.discount_value > 0 AND td.calculation_type IS NOT NULL AND td.calculation_type = 'PERCENTAGE' \n" +
            "THEN (round((sod.quantity * tp.trade_price),4) - round(((td.discount_value/100) * (sod.quantity * tp.trade_price)),4))\n" +
            "when td.discount_value > 0 AND td.calculation_type IS NOT NULL AND td.calculation_type = 'EQUAL' \n" +
            "THEN (round((sod.quantity * tp.trade_price),4) - round((td.discount_value * sod.quantity),4)) END),2) orderAmount,\n" +
            "ROUND( SUM(CASE WHEN td.discount_value IS NULL THEN 0 \n" +
            "when td.discount_value > 0 AND td.calculation_type IS NOT NULL AND td.calculation_type = 'PERCENTAGE' \n" +
            "THEN (td.discount_value/100) * (sod.quantity * tp.trade_price) when td.discount_value > 0 AND td.calculation_type IS NOT NULL AND td.calculation_type = 'EQUAL' \n" +
            "THEN td.discount_value * sod.quantity END) , 4) discounted_amount,\n" +
            "dp.depot_name, inv.name invoice_nature, ar.fiscal_year_name, \n" +
            "so.approval_status, so.approval_date,\n" +
            "SUM(sod.quantity) order_quantity,\n" +
            "d.distributor_name  distributorName,\n" +
            "@var\\:=:approvalActor approvalActor,\n" +
            "@var\\:=:level level,\n" +
            "@var\\:=:approvalStepId approvalStepId,\n" +
            "@var\\:=:multiLayerApprovalPathId multiLayerApprovalPathId,\n" +
            "@var\\:=:approvalActorId approvalActorId, \n" +
            "@var\\:=:approvalStatusName approvalStatus, \n" +
            "@var\\:=:approvalStepName approvalStepName \n" +
            "from sales_order so \n" +
            "inner join sales_order_details sod on sod.sales_order_id = so.id  \n" +
            "and so.is_active = true and so.is_deleted = false\n" +
            "and sod.is_active = true and sod.is_deleted = false\n" +
            "inner join sales_booking sb on sb.id = so.sales_booking_id\n" +
            "and sb.is_active = true and sb.is_deleted = false\n" +
            "inner join sales_booking_details sbd on sbd.id = sod.sales_booking_details_id \n" +
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
            "AND so.approval_status = :approvalStatus \n" +
            "AND (COALESCE(:soList) is NULL OR sb.sales_officer_id IN (:soList))\n" +
            "AND so.is_active is true AND so.is_deleted is false \n" +
            "GROUP BY sb.company_id, so.id order by so.order_date desc", nativeQuery = true)
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

    @Query(value = "select sb.sales_officer_id,\n" +
            "au.email as sales_officer_email, ds.contact_no dis_contact_no, sb.booking_no,\n" +
            "so.order_no, date_format(so.order_date, '%d-%b-%Y')\n" +
            "from sales_order_details sod\n" +
            "inner join sales_booking_details sbd on sbd.id = sod.sales_booking_details_id\n" +
            "inner join sales_booking sb on sb.id = sbd.sales_booking_id\n" +
            "and sb.is_active = true and sb.is_deleted = false\n" +
            "inner join sales_order so on so.id = sod.sales_order_id\n" +
            "inner join application_user au on au.id = sb.sales_officer_id\n" +
            "inner join distributor ds on ds.id = sb.distributor_id\n" +
            "where (so.id = :order_id)\n" +
            "limit 1", nativeQuery = true)
    Map findSalesOrderWithSo(@Param("order_id") Long salesOrderId);
}
