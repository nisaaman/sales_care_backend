package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.salesandcollection.entity.SalesInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author anika
 * @Date ১৯/৪/২২
 */
@Repository
public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, Long> {
    List<SalesInvoice> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    @Query(value = "select prod.id, prod.product_sku as productSku, " +
            "concat(prod.name, ' ', prod.item_size, ' ', uom.abbreviation, ' * ', ps.pack_size) as productName, \n" +
            "pc.name as productCategory, sum(invtd.quantity) as quantity, \n" +
            "sum(sbd.free_quantity) as freeQuantity, \n" +
            "round(sum(invtd.quantity * ptp.trade_price),4) as sale_amount, \n" +
            "sum(ifnull(case when calculation_type = 'PERCENTAGE' \n" +
            "                       then ((ptp.trade_price * td.discount_value)/100)\n" +
            "                else td.discount_value end,0)) as tradeDiscount from sales_booking sb \n" +
            "inner join sales_booking_details sbd on sb.id = sbd. sales_booking_id and sb.company_id = :companyId \n" +

            "and sb.sales_officer_id in(:salesOfficerUserLoginId)\n" +
            "and sb.booking_date >= :startDate and sb.booking_date <= :endDate \n" +
            "inner join sales_order_details sod on sbd.id = sod.sales_booking_details_id \n" +
            "inner join sales_order so on sod.sales_order_id = so.id  \n" +
            "inner join inv_transaction_details invtd on sod.id = invtd.sales_order_details_id \n" +
            "inner join inv_delivery_challan idc on invtd.inv_transaction_id = idc.inv_transaction_id \n" +
            "inner join sales_invoice_challan_map sicm on idc.id = sicm.inv_delivery_challan_id \n" +
            "inner join sales_invoice si on sicm.sales_invoice_id = si.id\n" +
            "inner join product prod on  invtd.product_id = prod.id \n" +
            "left join pack_size ps on prod.pack_size_id = ps.id\n" +
            "left join unit_of_measurement uom on prod.uom_id = uom.id \n" +
            "inner join product_category pc on prod.product_category_id = pc.id \n" +
            "inner join product_trade_price ptp on sbd.product_trade_price_id = ptp.id and \n" +
            "(:productCategoryId is null or pc.id = :productCategoryId) \n" +
            "left join trade_discount td on prod.id = td.product_id and sb.semester_id = td.semester_id\n" +
            "and td.product_id = ptp.product_id and td.approval_status='APPROVED' " +
            "and si.is_accepted=true group by prod.id,prod.product_sku, prod.name, pc.name;", nativeQuery = true)
    List<Map<String, Object>> getSalesOverView(
            @Param("salesOfficerUserLoginId") List<Long> salesOfficerUserLoginId,
            @Param("productCategoryId") Long productCategoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("companyId") Long companyId);

    /*and si.isAccepted is true remove from everywhre as per reuirement*/
    @Query("select si from SalesInvoice si " +
            "where si.isDeleted is false and si.company.id = :companyId " +
            "and si.distributor.id = :distributorId and si.remainingAmount > 0.0")
    List<SalesInvoice> findAllForOrdCalculableInvoiceByCompanyIdAndDistributorId(Long companyId, Long distributorId);

    Optional<SalesInvoice> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    @Query(value = "select si.id                    invoice_id\n" +
            "     , si.invoice_no            invoice_no\n" +
            "     , si.invoice_amount            invoice_amount\n" +
            "     , si.invoice_date          invoice_date\n" +
            "     , sum(pca.adjusted_amount) total_adjusted_amount\n" +
            "     , n.name                   invoice_nature_name\n" +
            "from payment_collection pc\n" +
            "         inner join payment_collection_adjustment pca on pca.payment_collection_id = pc.id\n" +
            "    and pc.company_id = :companyId \n" +
            "    and pc.distributor_id = :distributorId \n" +
            "    and pc.approval_status = 'APPROVED'\n" +
            "    and pc.is_deleted is false\n" +
            "    and pc.is_active is true\n" +
            "    and pca.is_active is true\n" +
            "    and pca.is_deleted is false\n" +
            "         inner join sales_invoice si on si.id = pca.sales_invoice_id\n" +
            "    and si.invoice_nature_id = :invoiceNatureId \n" +
            "    and si.is_deleted is false and si.is_active is true\n" +
            "         inner join invoice_nature n on n.id = si.invoice_nature_id and n.is_deleted is false and n.is_active is true \n" +
            "group by si.id", nativeQuery = true)
    List<Map> findAllInvoiceWiseAdjustedAmountByCompanyAndDistributorAndInvoiceNature(Long companyId, Long distributorId, Long invoiceNatureId);

    @Query(value = "select si.id           invoice_id\n" +
            "     , si.invoice_no   invoice_no\n" +
            "     , si.invoice_date invoice_date\n" +
            "     , si.invoice_amount   invoice_amount\n" +
            "     , sum(cdn.amount) ord_amount\n" +
            "     , n.name          invoice_nature_name\n" +
            "from sales_invoice si\n" +
            "         inner join credit_debit_note cdn on si.id = cdn.invoice_id and si.company_id = :companyId \n" +
            "    and si.distributor_id = :distributorId and si.invoice_nature_id = :invoiceNatureId and cdn.note_type = 'CREDIT' and cdn.transaction_type = 'ORD'\n" +
            "    and si.is_deleted is false and si.is_active is true and cdn.is_active is true and cdn.is_deleted is false\n" +
            "         inner join invoice_nature n on n.id = si.invoice_nature_id and n.is_deleted is false and n.is_active is true\n" +
            "group by si.id", nativeQuery = true)
    List<Map> findAllInvoiceWiseOrdAmountByCompanyAndDistributor(Long companyId, Long distributorId, Long invoiceNatureId);

    @Query(value = "select disinfo.distributor_id as distributorId, disinfo.distributorName, \n" +
            "disldgb.ledgerBalance, sum(ifnull(disdetails.creditInvoice,0)) as creditInvoice, \n" +
            "sum(ifnull(disdetails.cashInvoice,0)) AS cashInvoice, \n" +
            "ifnull(invovd.numOfOverdueInvoice,0) as overdueInvoice  from\n" +
            "(select dis.id as distributor_id, dis.distributor_name as distributorName from sales_invoice sinv  \n" +
            "inner join invoice_nature invn on sinv.invoice_nature_id = invn.id and invn.is_active is true \n" +
            "and invn.is_deleted is false and sinv.is_active is true and sinv.is_deleted is false and sinv.company_id=:companyId\n" +
            "inner join distributor dis on sinv.distributor_id = dis.id and dis.id in(:distributorIds) \n" +
            "and dis.is_active is true and dis.is_deleted is false group by dis.id) as disinfo\n" +
            "                                       inner join \n" +
            "(select dis.id as distributor_id, dis.distributor_name as distributorName,\n" +
            "(case when invn.name = \"CREDIT\" then count(invn.id) end) as creditInvoice,\n" +
            "(case when invn.name = \"CASH\" then count(dis.id) end) as cashInvoice from sales_invoice sinv  \n" +
            "inner join invoice_nature invn on sinv.invoice_nature_id = invn.id \n" +
            "and invn.is_active is true and invn.is_deleted is false and sinv.is_active is true \n" +
            "and sinv.is_deleted is false and sinv.company_id=:companyId \n" +

            "and (case when 0=:dueStatusValue then sinv.id not in(select sinv.id from  sales_invoice sinv \n" +
            "           inner join invoice_nature invn on sinv.invoice_nature_id = invn.id \n" +
            "           and invn.is_active is true and invn.is_deleted is false and sinv.is_active is true \n" +
            "           and sinv.is_deleted is false and sinv.company_id=:companyId \n" +
            "           inner join invoice_overdue inod on invn.id = inod.invoice_nature_id \n" +
            "           and datediff(:asOnDate, date_add(sinv.invoice_date, INTERVAL inod.not_due_days DAY)) >0 \n" +
            "           and inod.is_active is true and inod.is_deleted is false and inod.company_id=:companyId) \n" +
            "      when 1=:dueStatusValue then sinv.id in(select sinv.id from  sales_invoice sinv \n" +
            "           inner join invoice_nature invn on sinv.invoice_nature_id = invn.id \n" +
            "           and invn.is_active is true and invn.is_deleted is false and sinv.is_active is true \n" +
            "           and sinv.is_deleted is false and sinv.company_id=:companyId \n" +
            "           inner join invoice_overdue inod on invn.id = inod.invoice_nature_id " +
            "           and datediff(:asOnDate, date_add(sinv.invoice_date, INTERVAL inod.not_due_days DAY)) >0 \n" +
            "           and inod.is_active is true and inod.is_deleted is false and inod.company_id=:companyId) \n" +
            "       else 1 \n" +
            "end)\n" +
            "inner join distributor dis on sinv.distributor_id = dis.id \n" +
            "and dis.id in(:distributorIds) group by dis.id,invn.name) as disdetails\n" +
            "on disinfo.distributor_id = disdetails.distributor_id \n" +
            "                           left join                   \n"+
            "(select lt.distributor_id, round(ifnull(sum(lt.debit - lt.credit),0),4) as ledgerBalance \n" +
            "from ledger_transaction lt  where lt.company_id = :companyId \n" +
            "and lt.transaction_date <= :asOnDate group by lt.distributor_id) disldgb \n"+
            "on disdetails.distributor_id = disldgb.distributor_id \n"+
            "                           left join                          \n" +
            "(select dis.id as distributorId, dis.distributor_name as distributorName, \n" +
            "count(dis.id) as numOfOverdueInvoice from  sales_invoice sinv \n" +
            "inner join invoice_nature invn on sinv.invoice_nature_id = invn.id \n" +
            "and sinv.remaining_amount > 0 \n" +
            "and invn.is_active is true and invn.is_deleted is false and sinv.is_active is true \n" +
            "and sinv.is_deleted is false and sinv.company_id=:companyId \n" +
            "inner join invoice_overdue inod on invn.id = inod.invoice_nature_id \n" +
            "and datediff(:asOnDate, date_add(sinv.invoice_date, INTERVAL inod.not_due_days DAY)) > 0 \n" +
            "and inod.is_active is true and inod.is_deleted is false and inod.company_id=:companyId\n" +
            "inner join distributor dis on sinv.distributor_id = dis.id and dis.id in(:distributorIds) \n" +
            "and 1=(case when 0 = :dueStatusValue then  0 else 1 end) group by dis.id) as invovd\n" +
            "on disinfo.distributor_id = invovd.distributorId  group by disinfo.distributor_id;", nativeQuery = true)
    List<Map<String, Object>> getDistributorWiseSalesInvoiceOverview(
            @Param("companyId") Long companyId,
            @Param("distributorIds") List<Long> distributorIds,
            @Param("asOnDate") LocalDate asOnDate,
            @Param("dueStatusValue") Integer dueStatusValue);

    @Query(value = "select invd.salesInvoiceId, invd.invoiceNo, invd.invoiceDate, invd.invoiceNature, \n" +
            "invd.isAccepted, \n" +
            "sod.salesOfficer, sod.designation, sod.location_name, invd.overDueDays,invd.invoiceBalance, \n" +
            "ifnull(invd.invoiceAmount,0) as invoiceAmount, \n" +
            "ifnull(aiwo.ordAmount,0) as ordAmount  from\n" +
            "(select sinv.id as salesInvoiceId, sinv.distributor_id as distributorId, \n" +
            "sinv.invoice_no as invoiceNo, sinv.invoice_date as invoiceDate, \n" +
            "sinv.is_accepted as isAccepted, \n" +
            "invn.name as invoiceNature, sinv.invoice_amount as invoiceAmount, \n" +
            "sinv.remaining_amount as invoiceBalance, \n" +
            "(case when sinv.remaining_amount != 0 then \n" +
            "datediff(:asOnDate, date_add(sinv.invoice_date, INTERVAL inod.not_due_days DAY)) else 0 end)\n" +
            "as overDueDays from sales_invoice sinv \n"+
            "inner join invoice_nature invn on sinv.invoice_nature_id = invn.id \n" +
            "and sinv.distributor_id =:distributorId and sinv.company_id =:companyId and sinv.is_active is true \n" +
            "and sinv.is_deleted is false and invn.is_active is true and invn.is_deleted is false\n" +
            "inner join invoice_overdue inod on invn.id = inod.invoice_nature_id \n" +
            "and inod.is_active is true and inod.is_deleted is false and inod.company_id=:companyId \n" +
            "and (case when 1=:notDueStatus then datediff(:asOnDate, date_add(sinv.invoice_date, INTERVAL inod.not_due_days DAY)) < 0 else 1 end)\n"+
            "and datediff(:asOnDate, date_add(sinv.invoice_date, INTERVAL inod.not_due_days DAY)) \n" +
            "BETWEEN :startInterval and :endInterval) as invd\n" +
            "                              left join                       \n" +
            "(select sinv.id as salesInvoiceId,\n" +
            "sum(ifnull(cdn.amount,0)) as ordAmount from payment_collection_adjustment pca \n" +
            "inner join sales_invoice sinv on pca.sales_invoice_id = sinv.id\n" +
            "and pca.is_active is true and pca.is_deleted is false and sinv.is_active is true \n" +
            "and sinv.is_deleted is false and sinv.company_id =:companyId\n"+

            "left join credit_debit_note cdn on  pca.id = cdn.payment_collection_adjustment_id \n" +
            "and cdn.transaction_type=\"ORD\" and cdn.approval_status = \"APPROVED\" \n" +
            "and pca.is_active is true and pca.is_deleted is false\n" +
            "group by sinv.id) as aiwo on invd.salesInvoiceId = aiwo.salesInvoiceId\n" +
            "                               inner join                           \n" +
            "(select au.id, au.name as salesOfficer, desg.name as designation, lo.name location_name,\n" +
            "dsom.distributor_id as distributorId from distributor_sales_officer_map dsom \n" +
            "inner join sales_invoice sinv on sinv.distributor_id = dsom.distributor_id \n" +
            "and dsom.is_active is true and dsom.is_deleted is false and dsom.to_date is null\n" +
            "and dsom.distributor_id =:distributorId\n" +
            "inner join application_user au on dsom.sales_officer_id = au.id \n" +

            "inner join reporting_manager rm \n" +
            "on dsom.sales_officer_id = rm.application_user_id \n" +
            "and rm.to_date is null \n" +
            "inner join location_manager_map lmm \n" +
            "on rm.reporting_to_id = lmm.application_user_id \n" +
            "and lmm.to_date is null and lmm.company_id =:companyId\n" +
            "inner join location lo " +
            "on lmm.location_id = lo.id \n" +

            "left join designation desg on au.designation_id = desg.id and desg.is_active is true \n" +
            "and desg.is_deleted is false and au.is_deleted is false\n" +
            "group by au.id, dsom.distributor_id, lo.name) as sod on invd.distributorId = sod.distributorId\n" +
            "and (:invoiceNature is null or invd.invoiceNature =:invoiceNature)\n" +
            "and (:isAcknowledged = '' or invd.isAccepted is false)\n"+
            ";", nativeQuery = true)
    List<Map<String, Object>> getDistributorInvoicesDetails(
            @Param("distributorId") Long distributorId,
            @Param("companyId") Long companyId,
            @Param("invoiceNature") String invoiceNature,
            @Param("startInterval") Integer startInterval,
            @Param("endInterval") Integer endInterval,
            @Param("asOnDate") LocalDate asOnDate,
            @Param("notDueStatus") Integer notDueStatus,
            @Param("isAcknowledged") String isAcknowledged);

    @Query(value = "select datediff(ifnull(pca.mapping_date, NOW()), sinv.invoice_date) as dayElapsed \n" +
            "from sales_invoice sinv inner join (select sales_invoice_id, mapping_date from payment_collection_adjustment \n" +
            "where is_active is true and is_deleted is false order by id desc limit 1) pca on sinv.id = pca.sales_invoice_id \n" +
            "and sinv.id=:salesInvoiceId and sinv.is_active is true and sinv.is_deleted is false;", nativeQuery = true)
    Object getSalesInvoiceElapsedDays(
            @Param("salesInvoiceId") Long salesInvoiceId);

    List<SalesInvoice> findAllByCompanyIdAndDistributorIdAndIsAcceptedTrueAndIsActiveTrueAndIsDeletedFalse(Long companyId, Long distributorId);

    List<SalesInvoice> getAllByDistributorIdAndCompanyIdAndIsDeletedIsFalseAndIsActiveIsTrueAndIsAcceptedIsTrueAndRemainingAmountGreaterThanOrderByInvoiceDateDesc(
            Long distributorId, Long companyId, Float remainingAmount);

    @Query(value = "select t.product_sku           product_sku\n" +
            "     , t.product_name          product_name\n" +
            "      , t.item_size             item_size\n" +
            "      , t.uom                   uom\n" +
            "      , t.pack_size             pack_size\n" +
            "     , t.product_category_name product_category_name\n" +
            "     , sum(t.price)            price\n" +
            "     , sum(t.product_quantity) product_quantity\n" +
            "     , sum(t.unit_price)       unit_price\n" +
            "     , sum(t.discount_value)   discount_value\n" +
            "     , sum(t.vat_amount)       vat_amount\n" +
            "     , t.vat_included          vat_included\n" +
            "     , sum(t.invoice_amount)   invoice_amount\n" +
            "from (select p.id                                                       product_id\n" +
            "           , p.product_sku                                              product_sku\n" +
            "           , p.name                                                     product_name\n" +
            "            , p.item_size                                                item_size\n" +
            "           , uom.abbreviation                                           uom\n" +
            "           , ps.pack_size                                               pack_size\n" +
            "           , pc.name                                                    product_category_name\n" +
            "           , @price \\:= round(ptp.trade_price * itd.quantity, 2)       price\n" +
            "           , itd.quantity                                               product_quantity\n" +
            "           , ptp.trade_price                                            unit_price\n" +
            "           , @discount_value \\:= round(case\n" +
            "                                          when td.discount_value is null then 0\n" +
            "                                          when td.calculation_type = 'EQUAL' then td.discount_value * itd.quantity\n" +
            "                                          when td.calculation_type = 'PERCENTAGE'\n" +
            "                                              then (@price * td.discount_value) / 100\n" +
            "                                          end, 2)                       discount_value\n" +
            "           , @after_discount_price \\:= @price - @discount_value          after_discount_price\n" +
            "           , @vat_amount \\:= round(case\n" +
            "                                      when v.vat is null then 0\n" +
            "                                      when v.vat_included is true then @after_discount_price -\n" +
            "                                                                       round(@after_discount_price / ((100 + v.vat) / 100), 2)\n" +
            "                                      else @after_discount_price * (v.vat / 100)\n" +
            "                                      end, 2)                           vat_amount\n" +
            "           , v.vat_included                                             vat_included\n" +
            "           , round(case\n" +
            "                       when v.vat is null or v.vat_included is true then @after_discount_price\n" +
            "                       else @after_discount_price + @vat_amount end, 2) invoice_amount\n" +
            "      from inv_delivery_challan c\n" +
            "               inner join inv_transaction it\n" +
            "                          on c.inv_transaction_id = it.id\n" +
            "                              and c.id = :deliveryChallanId\n" +
            "               inner join inv_transaction_details itd\n" +
            "                          on it.id = itd.inv_transaction_id\n" +
            "                              and itd.is_active is true\n" +
            "                              and itd.is_deleted is false\n" +
            "               inner join batch b\n" +
            "                          on itd.batch_id = b.id\n" +
            "               inner join sales_order_details sod\n" +
            "                          on itd.sales_order_details_id = sod.id\n" +
            "                              and sod.is_active is true\n" +
            "                              and sod.is_deleted is false\n" +
            "               inner join sales_booking_details sbd\n" +
            "                          on sod.sales_booking_details_id = sbd.id\n" +
            "                              and sbd.is_active is true\n" +
            "                              and sbd.is_deleted is false\n" +
            "               inner join product p\n" +
            "                          on sbd.product_id = p.id\n" +
            "                inner join unit_of_measurement uom\n" +
            "                          on p.uom_id = uom.id\n" +
            "               inner join pack_size ps\n" +
            "                          on p.pack_size_id = ps.id\n" +
            "               left join vat_setup v\n" +
            "                         on v.product_id = p.id\n" +
            "                             and now() between v.from_date and v.to_date\n" +
            "                             and v.is_active is true\n" +
            "                             and v.is_deleted is false\n" +
            "               inner join product_category pc\n" +
            "                          on p.product_category_id = pc.id\n" +
            "               inner join product_trade_price ptp\n" +
            "                          on ptp.id = sbd.product_trade_price_id\n" +
            "               left join trade_discount td\n" +
            "                         on td.id = sbd.trade_discount_id) t\n" +
            "group by t.product_id, t.vat_included", nativeQuery = true)
    List<Map> getAllInvoiceableProductsWithTotalAmountByDeliveryChallanId(Long deliveryChallanId);


    @Query(value = "#Get Invoice List for Adjustment\n" +
            "select @isOpeningBalance \\:= \"N\" isOpeningBalance, si.id,\n" +
            "#opening invoice id and regular invoice id could be same\n" +
            "concat(si.id) as dataId,\n" +
            "si.distributor_id,\n" +
            "       si.invoice_no as invoiceNo,\n" +
            "       si.invoice_date as invoiceDate,\n" +
            "       round(si.invoice_amount, 4) as invoiceAmount,\n" +
            "       round(si.remaining_amount, 4)- ifnull(credit.credit_amount, 0) as remainingAmount,\n" +
            "       datediff(:paymentDate, si.invoice_date) as elapsedDay,\n" +
            "       #ord.from_invoiceAmountday, ord.to_day, ord.calculation_type, ord.ord, ord.start_date, ord.end_date,\n" +
            "       round(ifnull(case when ord.calculation_type = 'EQUAL' then ord.ord\n" +
            "           when ord.calculation_type = 'PERCENTAGE' then ((si.invoice_amount - si.vat_amount)/100) * ord.ord\n" +
            "       end - si.ord_amount, 0), 4) as ordAmount,\n" +
            "`in`.name as invoiceNature,\n" +
            "ifnull(ord.calculation_type, 'PERCENTAGE') as calculationType,\n" +
            "round(ifnull(ord.ord,0), 4) as ord\n" +
            "\n" +
            "from sales_invoice si\n" +
            "inner join invoice_nature `in` \n" +
            "        on si.invoice_nature_id = `in`.id\n" +
            "left join (\n" +
            "    select od.*, s.start_date, s.end_date\n" +
            "    from overriding_discount od\n" +
            "    left join semester s\n" +
            "           on od.semester_id = s.id\n" +
            ") as ord on ord.organization_id = si.organization_id\n" +
            "    and ord.company_id = si.company_id\n" +
            "    and ord.is_deleted is false\n" +
            "    and ord.is_active is true\n" +
            "    and ord.approval_status = 'APPROVED'\n" +
            "    and ord.invoice_nature_id = si.invoice_nature_id\n" +
            "    and datediff(:paymentDate, si.invoice_date) between ord.from_day and ord.to_day\n" +
            "    and si.invoice_date between ord.start_date and ord.end_date\n" +
            "\n" +
            "#for credit note return\n"+
            "left join\n" +
            "            (select cn.invoice_id, sum(ifnull(cn.amount, 0)) as credit_amount\n" +
            "            from credit_debit_note cn\n" +
            "            where cn.approval_status = 'APPROVED' and cn.note_type = 'CREDIT'\n" +
            "            and cn.transaction_type = 'SALES_RETURN'\n" +
            "            and cn.is_deleted is false\n" +
            "            and cn.is_active is true\n" +
            "            and cn.distributor_id =:distributorId\n" +
            "            group by cn.invoice_id) \n" +
            "            credit on credit.invoice_id = si.id\n" +
            "\n" +
            "where si.distributor_id = :distributorId\n" +
            "  and si.organization_id= :organizationId\n" +
            "  and si.company_id = :companyId\n" +
            "  and si.is_deleted is false\n" +
            "  and si.is_active is true\n" +
            "#NGLSC-2233  and si.is_accepted is true\n" +
            "  and (si.remaining_amount - ( ifnull(credit.credit_amount, 0) + si.ord_amount)) > 0\n" +
            "  and (:fromDate = '' or :fromDate is null or :toDate = '' or :toDate is null\n" +
            "   or (si.invoice_date between :fromDate and :toDate))\n" +
            "#order by si.invoice_date desc;" +
            "\n" +
            "union all\n" +
            "select @isOpeningBalance \\:= \"Y\" isOpeningBalance, disb.id,\n" +
            "#opening invoice id and regular invoice id could be same\n" +
            "concat(disb.id, \"-\", disb.distributor_id) as dataId,\n" +
            "disb.distributor_id, reference_no as invoiceNo,\n" +
            "disb.created_date as invoiceDate,\n" +
            "round(balance, 4) as invoiceAmount,\n" +
            "round(ifnull(remaining_balance, 0) - ifnull(credit_amount, 0), 4) as remainingAmount,\n" +
            "datediff(:paymentDate, disb.created_date) as elapsedDay,\n" +
            "0, `in`.name as invoiceNature,\n" +
            "\"PERCENTAGE\", 0\n" +
            "from distributor_balance disb inner join invoice_nature `in`\n" +
            "on disb.invoice_nature_id = `in`.id\n" +
            "left join\n" +
            "(select cn.distributor_balance_id, sum(ifnull(cn.amount, 0)) as credit_amount\n" +
            "from credit_debit_note cn\n" +
            "where cn.approval_status = 'APPROVED' and cn.note_type = 'CREDIT'\n" +
            "and cn.transaction_type = 'SALES_RETURN'\n" +
            "and cn.is_deleted is false\n" +
            "and cn.is_active is true\n" +
            "and cn.distributor_id =:distributorId\n" +
            "group by cn.distributor_balance_id) as credit\n" +
            "on credit.distributor_balance_id = disb.id\n" +
            "where remaining_balance>0\n" +
            "and disb.company_id = :companyId\n" +
            "and disb.distributor_id = :distributorId"
            ,nativeQuery = true)
    List<Map<String, Object>> getUnadjustedInvoiceListByDistributorAndPaymentDate(
            @Param("organizationId") Long organizationId,
            @Param("companyId") Long companyId,
            @Param("distributorId") Long distributorId,
            @Param("paymentDate") LocalDate paymentDate,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    @Query(value = "#Get Opening Invoice List for Adjustment\n" +
            "select\n" +
            "disb.id, disb.distributor_id, reference_no as invoiceNo,\n" +
            "disb.created_date as invoiceDate,\n" +
            "round(balance, 4) as invoiceAmount,\n" +
            "round(remaining_balance, 4) as remainingAmount,\n" +
            "`in`.name as invoiceNature,\n" +
            "from distributor_balance disb inner join invoice_nature `in`\n" +
            "on disb.invoice_nature_id = `in`.id\n" +
            "where remaining_balance>0\n" +
            "and disb.company_id = :companyId\n" +
            "and disb.distributor_id = :distributorId\n" +
            "and (:fromDate is null or disb.created_date >=:fromDate)\n" +
            "and (:toDate is null or disb.created_date <=:toDate)"
            ,nativeQuery = true)
    List<Map<String, Object>> getUnadjustedOpeningInvoiceListByDistributor(
            @Param("companyId") Long companyId,
            @Param("distributorId") Long distributorId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);


    @Query(value = "select " +
            "sinv.id invoice_id, sinv.invoice_no, sinv.invoice_amount, \n" +
            "sinv.remaining_amount, \n" +
            "sinv.remarks, sinv.invoice_nature_id, \n" +
            "round(sum(pc.adjusted_amount), 4) adjusted_amount, \n" +
            "date_format(sinv.invoice_date, '%d-%b-%Y') as invoice_date, \n" +
            "year(sinv.invoice_date) as year, datediff(:asOnDate, sinv.invoice_date) as asOnDiffDays,\n" +
            "date_add(sinv.invoice_date, INTERVAL inod.not_due_days DAY) maturity_date,\n" +
            "datediff(:asOnDate, date_add(sinv.invoice_date,INTERVAL inod.not_due_days DAY)) as overdueDays,\n" +
            "inod.not_due_days - datediff(:asOnDate, sinv.invoice_date) as notdueDays \n" +
            "from sales_invoice sinv \n" +
            "left join payment_collection_adjustment pc \n" +
            "on sinv.id = pc.sales_invoice_id \n" +

            "inner join invoice_nature invn on sinv.invoice_nature_id = :invoice_nature \n" +
            "and invn.is_active is true and invn.is_deleted is false \n" +
            "inner join invoice_overdue inod on inod.invoice_nature_id = :invoice_nature \n" +
            "and inod.is_active is true and inod.is_deleted is false \n" +
            "and inod.company_id = :company_id\n" +

            "where (:company_id is null OR sinv.company_id = :company_id) \n" +
            "and (:start_date is null OR sinv.invoice_date >= :start_date) \n" +
            "and (:end_date is null OR sinv.invoice_date <= :end_date) \n" +
            "and (:invoice_nature is null or sinv.invoice_nature_id = :invoice_nature) \n" +
            "and sinv.distributor_id =:distributorId\n" +
            "and sinv.remaining_amount !=0 \n" +
            "and sinv.is_active is true and sinv.is_deleted is false\n" +
            "group by sinv.id, inod.not_due_days \n"+
            "order by sinv.invoice_date desc \n"
            , nativeQuery = true)
    List<Map<String, Object>> getDistributorReceivableInvoicesDetails(
                @Param("company_id") Long companyId,
                @Param("start_date") LocalDate startDate,
                @Param("end_date") LocalDate endDate,
                @Param("invoice_nature") Long invoiceNature,
                @Param("distributorId") Long distributorId,
                @Param("asOnDate") LocalDate asOnDate);

    List<SalesInvoice> findAllByCompanyIdAndDistributorIdAndIsAcceptedFalseAndIsActiveTrueAndIsDeletedFalse(Long companyId, Long distributorId);

    @Query(value = "select summary.* \n"
            + " from (select i.id, i.invoice_no , i.distributor_id,\n"
            + "a.mapping_date, a.created_date mapping_date_time,\n"
            + " date_format(i.invoice_date, '%d-%b-%Y') invoice_date,\n"
            + " i.invoice_amount                        invoice_amount,\n"
            + " i.created_date created_date,\n"
            + " DATEDIFF(now(), i.invoice_date)         due_days,\n"
            + " case when a.is_ord_settled is false then (i.remaining_amount- i.ord_amount) \n"
            + " else i.remaining_amount end as remaining_amount\n"
            + " from sales_invoice i\n"
            + " left join payment_collection_adjustment a on i.id = a.sales_invoice_id\n"
            + " where i.company_id = :companyId\n"
            + " and i.distributor_id = :distributorId\n"
            + " and i.is_active is true\n"
            + " and i.is_deleted is false\n"
            + " and i.id < :currentInvoiceId\n"
            + " order by i.id desc\n"
            + " limit 3\n"
            + " ) summary\n"
            + " where\n"
            + " (mapping_date_time >= :invoiceDate || mapping_date is null)\n"
            //+ "and remaining_amount !=0"
            /*"select i.invoice_no                            invoice_no\n" +
            "     , date_format(i.invoice_date, '%d %b %Y') invoice_date\n" +
            "     , i.invoice_amount                        invoice_amount\n" +
            "     , i.created_date created_date\n" +
            "     , DATEDIFF(now(), i.invoice_date)         due_days\n" +
            " from sales_invoice i\n" +
            " where i.company_id = :companyId \n" +
            "  and i.distributor_id = :distributorId \n" +
            "  and i.remaining_amount != 0 \n" +
            "  and i.is_accepted is true \n" +
            "  and i.is_active is true \n" +
            "  and i.is_deleted is false \n" +
            "  and i.id < :currentInvoiceId \n" +
            " order by i.id desc\n" +
            " limit 3 ",*/
            , nativeQuery = true)
    List<Map> findLastThreeDueInvoiceList(Long companyId, Long distributorId,
                                          Long currentInvoiceId, LocalDateTime invoiceDate);

    @Query(value = "select round(ifnull(sum(i.invoice_amount), 0), 2) total_previous_invoice_amount\n" +
            "from sales_invoice i\n" +
            "where i.company_id = :companyId \n" +
            "  and i.distributor_id = :distributorId \n" +
            "# NGLSC-2261  and i.is_accepted is true\n" +
            "  and i.is_active is true\n" +
            "  and i.is_deleted is false\n" +
            "  and i.id < :currentInvoiceId ", nativeQuery = true)
    Map getTotalPreviousInvoiceAmountByCompanyAndDistributorAndCurrentInvoice(Long companyId, Long distributorId, Long currentInvoiceId);

    @Query(value = "select round(ifnull(sum(a.adjusted_amount),0), 2) total_previous_payment_amount, \n" +
            "round(ifnull(sum(a.ord_amount),0), 2) total_previous_ord_amount \n" +
            "from payment_collection c\n" +
            "         inner join payment_collection_adjustment a\n" +
            "                    on c.id = a.payment_collection_id\n" +
            "                        and c.company_id = :companyId \n" +
            "                        and c.distributor_id = :distributorId \n" +
            "                        and a.sales_invoice_id < :currentInvoiceId\n" +
            "                        and c.is_active is true\n" +
            "                        and c.is_deleted is false\n" +
            "                        and a.is_active is true\n" +
            "                        and a.is_deleted is false ", nativeQuery = true)
    Map getTotalPreviousInvoicePaymentAmountByCompanyAndDistributorAndCurrentInvoice(Long companyId, Long distributorId, Long currentInvoiceId);


    @Query(value = "select round(ifnull(sum(amount), 0), 2) previous_adjusted_amount\n" +
            "from (select round(sum(IF(cdn.note_type = 'DEBIT', -cdn.amount, cdn.amount)), 2) amount\n" +
            "      from credit_debit_note cdn\n" +
            "      where cdn.company_id = :companyId \n" +
            "        and cdn.distributor_id = :distributorId \n" +
            "        and cdn.invoice_id is not null \n" +
            "        and cdn.invoice_id < :currentInvoiceId\n" +
            "        and cdn.is_active is true\n" +
            "        and cdn.is_deleted is false\n" +
            "        and cdn.approval_status = 'APPROVED'\n" +
            "      union\n" +
            "      select -sum(return_tbl.actual_return_price) amount\n" +
            "      from (select @actual_price \\:= case\n" +
            "                                        when (t.calculation_type is not null and t.calculation_type = 'PERCENTAGE')\n" +
            "                                            then (ptp.trade_price - (ptp.trade_price * t.discount_value / 100)) *\n" +
            "                                                 srpd.quantity\n" +
            "                                        else (ptp.trade_price - IFNULL(t.discount_value, 0)) * srpd.quantity end return_price\n" +
            "                 , round(@actual_price -\n" +
            "                         @actual_price * round(si.invoice_discount / (si.invoice_discount + si.invoice_amount), 4),\n" +
            "                         2)                                                                                      actual_return_price\n" +
            "            from sales_return sr\n" +
            "                     inner join sales_return_proposal srp\n" +
            "                                on sr.sales_return_proposal_id = srp.id\n" +
            "                                    and srp.company_id = :companyId\n" +
            "                                    and srp.distributor_id = :distributorId\n" +
            "                                    and srp.sales_invoice_id < :currentInvoiceId\n" +
            "                                    and srp.approval_status = 'APPROVED'\n" +
            "                     inner join sales_invoice si\n" +
            "                                on srp.sales_invoice_id = si.id\n" +
            "                     inner join sales_return_proposal_details srpd\n" +
            "                                on srp.id = srpd.sales_return_proposal_id\n" +
            "                                    and srpd.is_active is true\n" +
            "                                    and srpd.is_deleted is false\n" +
            "                     inner join product_trade_price ptp\n" +
            "                                on ptp.id = srpd.product_trade_price_id\n" +
            "                     left join trade_discount t\n" +
            "                               on t.id = srpd.trade_discount_id) return_tbl) tbl", nativeQuery = true)
    Map getTotalPreviousInvoiceAdjustedAmountByCompanyAndDistributorAndCurrentInvoice(Long companyId, Long distributorId, Long currentInvoiceId);

    @Query(value = "select si.id, date_format(si.invoice_date,'%Y-%m-%d') invoiceDate, si.invoice_no invoiceNo, \n" +
            "si.invoice_amount invoiceAmount, si.remaining_amount remainingAmount, inv_nat.name invoiceNature, \n" +
            "si.is_accepted from sales_invoice si\n" +
            "inner join invoice_nature inv_nat on si.invoice_nature_id = inv_nat.id \n" +
            "and si.invoice_no=:invoiceNo", nativeQuery = true)
    Map getSalesInvoiceByInvoiceNo(@Param("invoiceNo") String invoiceNo);

    @Query(value = "select round(ifnull(sum(c.collection_amount), 0), 2) -\n" +
            "       (select round(ifnull(sum(a.adjusted_amount), 0), 2) total_adjusted_amount\n" +
            "        from payment_collection c\n" +
            "                 inner join payment_collection_adjustment a\n" +
            "                            on c.id = a.payment_collection_id\n" +
            "                                and c.company_id = :companyId \n" +
            "                                and c.distributor_id = :distributorId \n" +
            "                                and c.payment_nature = 'ADVANCE'\n" +
            "                                and c.is_active is true\n" +
            "                                and c.is_deleted is false\n" +
            "                                and a.is_active is true\n" +
            "                                and a.is_deleted is false) total_advance_amount\n" +
            "from payment_collection c\n" +
            "where c.company_id = :companyId \n" +
            "  and c.distributor_id = :distributorId \n" +
            "  and c.payment_nature = 'ADVANCE'\n" +
            "  and c.is_active is true\n" +
            "  and c.is_deleted is false", nativeQuery = true)
    Map getTotalInvoiceAdvanceAmountByCompanyAndDistributor(Long companyId, Long distributorId);


    @Query(value = "select * from\n" +
            "(select lmm.location_id, prod.id productId, prod.product_sku as productSku,\n" +
            "concat(prod.name, ' ', prod.item_size, ' ', uom.abbreviation, ' * ', ps.pack_size) as productName,\n" +
            "ps.pack_size packSize, pc.name as productCategory, au.id salesOfficerId,\n" +
            "au.name as salesOfficer, sum(invtd.quantity) as quantity,\n" +
            "sum(sbd.free_quantity) as freeQuantity, \n" +
            "round(sum(invtd.quantity * ptp.trade_price),4) as saleAmount, \n" +
            "sum(ifnull(case when calculation_type = 'PERCENTAGE' \n" +
            "then ((ptp.trade_price * td.discount_value)/100)\n" +
            "else td.discount_value end,0)) as tradeDiscount, budget.budgetQuantity,\n" +
            "budget.budgetAmount, ifnull(lastYearSale.lastYearSaleAmount, 0) lastYearSaleAmount,\n" +
            "ifnull(lastYearSale.lastYearSaleQuantity, 0) lastYearSaleQuantity\n" +
            "from sales_booking sb \n" +
            "inner join sales_booking_details sbd on sb.id = sbd. sales_booking_id \n" +
            "and sb.company_id = :companyId\n" +
            "and (COALESCE(:salesOfficerUserLoginId) IS NULL OR sb.sales_officer_id in (:salesOfficerUserLoginId))\n" +
            "inner join sales_order_details sod on sbd.id = sod.sales_booking_details_id \n" +
            "inner join sales_order so on sod.sales_order_id = so.id  \n" +
            "inner join inv_transaction_details invtd on sod.id = invtd.sales_order_details_id \n" +
            "inner join inv_delivery_challan idc on invtd.inv_transaction_id = idc.inv_transaction_id \n" +
            "inner join sales_invoice_challan_map sicm on idc.id = sicm.inv_delivery_challan_id \n" +
            "inner join sales_invoice si on sicm.sales_invoice_id = si.id\n" +
            "and (:startDate is null or si.invoice_date >= :startDate) \n" +
            "and (:endDate is null or si.invoice_date <= :endDate) \n" +
            "inner join product prod on  invtd.product_id = prod.id\n" +
            "left join pack_size ps on prod.pack_size_id = ps.id\n" +
            "left join unit_of_measurement uom on prod.uom_id = uom.id\n" +
            "inner join product_category pc on prod.product_category_id = pc.id\n" +
            "inner join product_trade_price ptp on sbd.product_trade_price_id = ptp.id\n" +
            "inner join application_user au on sb.sales_officer_id = au.id\n" +
            "inner join reporting_manager rm on sb.sales_officer_id = rm.application_user_id\n" +
            "and rm.to_date is null\n" +
            "inner join location_manager_map lmm\n" +
            "on rm.reporting_to_id = lmm.application_user_id and lmm.to_date is null and lmm.company_id =:companyId\n" +
            "inner join location lo on lmm.location_id = lo.id\n" +
            "left join trade_discount td on prod.id = td.product_id and sb.semester_id = td.semester_id\n" +
            "and td.product_id = ptp.product_id and td.approval_status='APPROVED' and si.is_accepted=true \n" +
            "inner join (\n" +
            "select budget.*, budget_d.product_id, sum(ifnull(budget_d.quantity, 0)) budgetQuantity,\n" +
            "sum(ifnull((budget_d.quantity * budget_d.product_trade_price), 0)) budgetAmount \n" +
            "from sales_budget budget\n" +
            "inner join sales_budget_details budget_d on budget.id = budget_d.sales_budget_id\n" +
            "where budget_d.month in (:monthList)\n" +
            "and (:accountingYearId is null or budget.accounting_year_id = :accountingYearId)\n" +
            "group by budget.id, budget_d.product_id\n" +
            ") as budget on budget.product_id = prod.id\n" +
            "left join (\n" +
            "select sbd.product_id, sum(ifnull(invtd.quantity, 0)) as lastYearSaleQuantity, sum(sbd.free_quantity) as freeQuantity, \n" +
            "round(sum(invtd.quantity * ptp.trade_price),4) as lastYearSaleAmount\n" +
            "from sales_booking_details sbd\n" +
            "inner join sales_order_details sod on sbd.id = sod.sales_booking_details_id \n" +
            "inner join sales_order so on sod.sales_order_id = so.id  \n" +
            "inner join inv_transaction_details invtd on sod.id = invtd.sales_order_details_id \n" +
            "inner join inv_delivery_challan idc on invtd.inv_transaction_id = idc.inv_transaction_id \n" +
            "inner join sales_invoice_challan_map sicm on idc.id = sicm.inv_delivery_challan_id \n" +
            "inner join sales_invoice si on sicm.sales_invoice_id = si.id\n" +
            "inner join product_trade_price ptp on sbd.product_trade_price_id = ptp.id\n" +
            "and (:startDateLastYear is null or si.invoice_date >= :startDateLastYear) \n" +
            "and (:endDateLastYear is null or si.invoice_date <= :endDateLastYear) \n" +
            "group by sbd.product_id\n" +
            ") as lastYearSale on lastYearSale.product_id = prod.id\n" +
            "group by sb.company_id, lmm.location_id, sb.sales_officer_id,\n" +
            "prod.id, pc.name, budget.budgetQuantity,\n" +
            "budget.budgetAmount, lastYearSale.lastYearSaleAmount, lastYearSale.lastYearSaleQuantity\n" +
            ") sales_info\n" +
            "inner join child_location_hierarchy lo_hierarchy\n" +
            "on sales_info.location_id = lo_hierarchy.id\n" +
            "and (COALESCE(:location_id_list) IS NULL OR lo_hierarchy.id in (:location_id_list))\n", nativeQuery = true)
    List<Map<String, Object>> getSalesOverViewProduct(
            @Param("salesOfficerUserLoginId") List<Long> salesOfficerUserLoginId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("startDateLastYear") LocalDate startDateLastYear,
            @Param("endDateLastYear") LocalDate endDateLastYear,
            @Param("accountingYearId") Long accountingYearId,
            @Param("monthList") List<Integer>  monthList,
            @Param("companyId") Long companyId,
            @Param("location_id_list") List<Long> locationIdList);


    @Query(value = "select lo_hierarchy.*, sales_info.*\n" +
            "from child_location_hierarchy lo_hierarchy\n" +
            "left join\n" +
            "(select sb.company_id, lo.id as location_id,\n" +
            "au.id salesOfficerId,\n" +
            "au.name as salesOfficer,\n" +
            "sb.distributor_id, dis.distributor_name distributorName,\n" +
            "sum(invtd.quantity) as quantity,\n" +
            "sum(sbd.free_quantity) as freeQuantity, \n" +
            "round(sum(invtd.quantity * ptp.trade_price),4)-si.invoice_discount-si.discount_amount as saleAmount,\n" +
            "sum(ifnull(case when calculation_type = 'PERCENTAGE'\n" +
            "then ((ptp.trade_price * td.discount_value)/100)\n" +
            "else td.discount_value end,0)) as tradeDiscount, budget.budgetQuantity,\n" +
            "budget.budgetAmount, ifnull(lastYearSale.lastYearSaleAmount, 0) lastYearSaleAmount,\n" +
            "ifnull(lastYearSale.lastYearSaleQuantity, 0) lastYearSaleQuantity\n" +
            "from sales_booking sb\n" +
            "inner join sales_booking_details sbd on sb.id = sbd. sales_booking_id\n" +
            "and sb.company_id = :companyId\n" +
            "and (coalesce(:salesOfficerUserLoginId) is null or sb.sales_officer_id in (:salesOfficerUserLoginId))\n" +
            "and (coalesce(:distributorIds) is null or sb.distributor_id in (:distributorIds))\n" +
            "inner join sales_order_details sod on sbd.id = sod.sales_booking_details_id\n" +
            "inner join sales_order so on sod.sales_order_id = so.id\n" +
            "inner join inv_transaction_details invtd on sod.id = invtd.sales_order_details_id\n" +
            "inner join inv_delivery_challan idc on invtd.inv_transaction_id = idc.inv_transaction_id\n" +
            "inner join sales_invoice_challan_map sicm on idc.id = sicm.inv_delivery_challan_id\n" +
            "inner join sales_invoice si on sicm.sales_invoice_id = si.id\n" +
            "and (:startDate is null or si.invoice_date >= :startDate)\n" +
            "and (:endDate is null or si.invoice_date <= :endDate)\n" +
            "inner join product prod on  invtd.product_id = prod.id\n" +
            "inner join product_category pc on prod.product_category_id = pc.id\n" +
            "inner join product_trade_price ptp on sbd.product_trade_price_id = ptp.id\n" +
            "inner join application_user au on sb.sales_officer_id = au.id\n" +
            "inner join location lo on sb.location_id = lo.id\n" +
            "inner join distributor dis on sb.distributor_id = dis.id\n" +
            "left join trade_discount td on prod.id = td.product_id\n" +
            "and sb.semester_id = td.semester_id\n" +
            "and td.product_id = ptp.product_id and td.approval_status='APPROVED'\n" +
            "#and si.is_accepted=true\n" +
            "\n" +
            "left join (\n" +
            "select company_id, distributor_id,\n" +
            "sum(ifnull(quantity, 0)) budgetQuantity,\n" +
            "sum(ifnull((quantity * product_trade_price), 0)) budgetAmount\n" +
            "from sales_budget s_budget\n" +
            "inner join sales_budget_details budget_d\n" +
            "on s_budget.id = budget_d.sales_budget_id\n" +
            "where s_budget.accounting_year_id = :accountingYearId\n" +
            "and budget_d.month in (:monthList)\n" +
            "group by s_budget.id, budget_d.distributor_id\n" +
            ") as budget\n" +
            "on budget.distributor_id = sb.distributor_id\n" +
            "and budget.company_id = sb.company_id\n" +
            "\n" +
            "left join (\n" +
            "select sb.company_id, lo.id as location_id, sb.sales_officer_id, sb.distributor_id,\n" +
            "sum(ifnull(invtd.quantity, 0)) as lastYearSaleQuantity,\n" +
            "sum(sbd.free_quantity) as freeQuantity,\n" +
            "round(sum(invtd.quantity * ptp.trade_price),4)-si.invoice_discount as lastYearSaleAmount\n" +
            "from sales_booking_details sbd\n" +
            "inner join sales_order_details sod on sbd.id = sod.sales_booking_details_id\n" +
            "inner join sales_order so on sod.sales_order_id = so.id\n" +
            "inner join sales_booking sb on sb.id = sbd.sales_booking_id\n" +
            "inner join inv_transaction_details invtd on sod.id = invtd.sales_order_details_id\n" +
            "inner join inv_delivery_challan idc on invtd.inv_transaction_id = idc.inv_transaction_id\n" +
            "inner join sales_invoice_challan_map sicm on idc.id = sicm.inv_delivery_challan_id\n" +
            "inner join sales_invoice si on sicm.sales_invoice_id = si.id\n" +
            "and (:startDateLastYear is null or si.invoice_date >= :startDateLastYear)\n" +
            "and (:endDateLastYear is null or si.invoice_date <= :endDateLastYear)\n" +
            "inner join product_trade_price ptp on sbd.product_trade_price_id = ptp.id\n" +
            "inner join location lo on sb.location_id = lo.id\n" +
            "group by sb.company_id, lo.id, sb.sales_officer_id, sb.distributor_id, si.invoice_discount\n" +
            "            ) as lastYearSale\n" +
            "            on lastYearSale.sales_officer_id = sb.sales_officer_id\n" +
            "            and lastYearSale.distributor_id = sb.distributor_id\n" +
            "            and lastYearSale.company_id = sb.company_id\n" +
            "            and lastYearSale.location_id = lo.id\n" +
            "            group by sb.company_id, lo.id, sb.sales_officer_id, sb.distributor_id, dis.distributor_name,\n" +
            "            si.id, budget.budgetQuantity, budget.budgetAmount, lastYearSale.lastYearSaleAmount,\n" +
            "            lastYearSale.lastYearSaleQuantity\n" +
            "            ) sales_info\n" +
            "\n" +
            "#inner join child_location_hierarchy lo_hierarchy\n" +
            "on sales_info.location_id = lo_hierarchy.id\n" +
            "where (coalesce(:location_id_list) is null or lo_hierarchy.id in (:location_id_list))\n" +
            "and (coalesce(:salesOfficerUserLoginId) is null or sales_info.salesOfficerId in (:salesOfficerUserLoginId))\n" +
            "and (coalesce(:distributorIds) is null or sales_info.distributor_id in (:distributorIds))\n"
            , nativeQuery = true)
    List<Map<String, Object>> getSalesOverViewSalesOficer(
            @Param("salesOfficerUserLoginId") List<Long> salesOfficerUserLoginId,
            @Param("distributorIds") List<Long> distributorIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("startDateLastYear") LocalDate startDateLastYear,
            @Param("endDateLastYear") LocalDate endDateLastYear,
            @Param("accountingYearId") Long accountingYearId,
            @Param("monthList") List<Integer>  monthList,
            @Param("companyId") Long companyId,
            @Param("location_id_list") List<Long> locationIdList);

    @Query(value =  "select * from (select invoice_info.company_id, invoice_info.salesOfficer, invoice_info.distributor_name, invoice_info.invoice_nature,\n"+
            "invoice_info.location_id, sum(invoice_info.remaining_amount) remaining_amount, sum(invoice_info.invoice_amount) invoice_amount,\n"+
            "SUM(IF(invoice_info.overdueDays <= 0, invoice_info.remaining_amount, 0)) notDues,\n"+
            "SUM(IF(invoice_info.overdueDays BETWEEN 1 AND 30, invoice_info.remaining_amount, 0)) level1,\n"+
            "SUM(IF(invoice_info.overdueDays BETWEEN 31 AND 60, invoice_info.remaining_amount, 0)) level31,\n"+
            "SUM(IF(invoice_info.overdueDays BETWEEN 61 AND 90, invoice_info.remaining_amount, 0)) level61,\n"+
            "SUM(IF(invoice_info.overdueDays BETWEEN 91 AND 120, invoice_info.remaining_amount, 0)) level91,\n"+
            "SUM(IF(invoice_info.overdueDays BETWEEN 121 AND 180, invoice_info.remaining_amount, 0)) level121,\n"+
            "SUM(IF(invoice_info.overdueDays > 180, invoice_info.remaining_amount, 0)) level181\n"+
            "from\n"+
            "(select sinv.company_id, lmm.location_id, sinv.id invoice_id,\n"+
            "sinv.distributor_id, dis.distributor_name, ifnull(sinv.invoice_amount, 0) invoice_amount,\n"+
            "ifnull(sinv.remaining_amount, 0) remaining_amount,\n"+
            "sinv.invoice_nature_id, invn.name invoice_nature,\n"+
            "datediff(:asOnDate, date_add(sinv.invoice_date, interval inod.not_due_days DAY)) as overdueDays,\n"+
            "au.id salesOfficerId, au.name as salesOfficer\n"+
            "from sales_invoice sinv\n"+
            "inner join invoice_nature invn on sinv.invoice_nature_id = invn.id\n"+
            "and invn.is_active is true and invn.is_deleted is false\n"+
            "inner join invoice_overdue inod on inod.invoice_nature_id = invn.id\n"+
            "and inod.is_active is true and inod.is_deleted is false\n"+
            "and inod.company_id =:company_id\n"+
            "inner join distributor_sales_officer_map dis_so\n"+
            "on sinv.distributor_id = dis_so.distributor_id\n"+
            "and dis_so.to_date is null\n"+
            "and dis_so.company_id =:company_id\n"+
            "and dis_so.is_deleted is false\n"+
//            "and (:sales_officer_id is null or dis_so.sales_officer_id = :sales_officer_id)\n" +
            "inner join distributor dis\n"+
            "on sinv.distributor_id = dis.id and dis.is_deleted is false\n"+
            "inner join application_user au on dis_so.sales_officer_id = au.id\n"+
            "inner join reporting_manager rm on dis_so.sales_officer_id = rm.application_user_id\n"+
            "and rm.to_date is null\n"+
            "inner join location_manager_map lmm\n"+
            "on rm.reporting_to_id = lmm.application_user_id\n"+
            "and lmm.to_date is null and lmm.company_id =:company_id\n"+
            "inner join location lo on lmm.location_id = lo.id\n"+
            "where sinv.company_id =:company_id\n"+
            "and (:start_date is null or sinv.invoice_date >= :start_date)\n"+
            "and (:end_date is null or sinv.invoice_date <= :end_date)\n"+
            "and (coalesce(:salesOfficerIds) is null or dis_so.sales_officer_id in (:salesOfficerIds))\n"+
            "and (coalesce(:distributorIds) is null or sinv.distributor_id in (:distributorIds))\n" +
            "#and sinv.remaining_amount >0\n"+
            "and sinv.is_active is true and sinv.is_deleted is false\n"+
            "group by sinv.id, lmm.location_id, au.id, dis.id,\n"+
            "sinv.invoice_nature_id, inod.not_due_days, sinv.invoice_date) invoice_info\n"+
            "inner join invoice_nature ir\n"+
            "on invoice_info.invoice_nature_id = ir.id\n"+
            "group by ir.id, invoice_info.location_id, invoice_info.salesOfficer, invoice_info.distributor_name) summary\n"+
            "inner join child_location_hierarchy lo_hierarchy\n"+
            "on summary.location_id = lo_hierarchy.id\n"+
            "and (coalesce(:locationIds) is null or lo_hierarchy.id in (:locationIds))\n"+
            "", nativeQuery = true)
    List<Map<String, Object>> getInvoiceAgingSummary(
            @Param("company_id") Long companyId,
            @Param("locationIds") List<Long> locationIds,
            @Param("salesOfficerIds") List<Long> salesOfficerIds,
            @Param("distributorIds") List<Long> distributorIds,
            @Param("start_date") LocalDate startDate,
            @Param("end_date") LocalDate endDate,
            @Param("asOnDate") LocalDate asOnDate);

    @Query(value =  "#invoice product wise ageing\n" +
            "select lo_hierarchy.*, invoice_info.*, pc.Category, pc.`Sub Category`\n" +
            "from\n" +
            "(select rtn.sales_return_amount, invtd.quantity, @product_sale_amount \\:= invtd.quantity \n" +
            "* round((ptp.trade_price - (ifnull(case when calculation_type = 'PERCENTAGE'\n" +
            "then ((ptp.trade_price * td.discount_value)/100) else td.discount_value end,0))),4) as saleAmount,\n" +
            "@productActualAmount \\:= (@product_sale_amount - ifnull(rtn.sales_return_amount, 0)) as product_actual_amount,\n" +
            "if(datediff(:asOnDate, date_add(sinv.invoice_date, INTERVAL inod.not_due_days DAY)) <= 0, (@product_sale_amount - ifnull(rtn.sales_return_amount, 0)), 0) notDues,\n" +
            "if(datediff(:asOnDate, date_add(sinv.invoice_date, INTERVAL inod.not_due_days DAY)) BETWEEN 1 AND 30, (@product_sale_amount - ifnull(rtn.sales_return_amount, 0)), 0) level1,\n" +
            "if(datediff(:asOnDate, date_add(sinv.invoice_date, INTERVAL inod.not_due_days DAY)) BETWEEN 31 AND 60, (@product_sale_amount - ifnull(rtn.sales_return_amount, 0)), 0) level31,\n" +
            "if(datediff(:asOnDate, date_add(sinv.invoice_date, INTERVAL inod.not_due_days DAY)) BETWEEN 61 AND 90, (@product_sale_amount - ifnull(rtn.sales_return_amount, 0)), 0) level61,\n" +
            "if(datediff(:asOnDate, date_add(sinv.invoice_date, INTERVAL inod.not_due_days DAY)) BETWEEN 91 AND 120, (@product_sale_amount - ifnull(rtn.sales_return_amount, 0)), 0) level91,\n" +
            "if(datediff(:asOnDate, date_add(sinv.invoice_date, INTERVAL inod.not_due_days DAY)) BETWEEN 121 AND 180, (@product_sale_amount - ifnull(rtn.sales_return_amount, 0)), 0) level121,\n" +
            "if(datediff(:asOnDate, date_add(sinv.invoice_date, INTERVAL inod.not_due_days DAY)) > 180, (@product_sale_amount - ifnull(rtn.sales_return_amount, 0)), 0) level181,\n" +
            "lmm.location_id, sinv.company_id, au.id salesOfficerId, au.name as salesOfficer, \n" +
            "prod.id product_id, prod.product_category_id, concat(prod.name, ' ', prod.item_size, ' ', u.abbreviation, ' * ', pk.pack_size) product_name,\n" +
            "sinv.id invoice_id, sinv.invoice_no, date_format(sinv.invoice_date, '%d-%b-%Y') as invoice_date,\n" +
            "year(sinv.invoice_date) as year, ifnull(sinv.invoice_amount, 0) invoice_amount,\n" +
            "ifnull(sinv.remaining_amount, 0) remaining_amount, sinv.invoice_nature_id, invn.name invoice_nature,\n" +
            "datediff(:asOnDate, date_add(sinv.invoice_date, interval inod.not_due_days day)) as overdueDays,\n" +
            "if(inod.not_due_days - datediff(:asOnDate, sinv.invoice_date) > 0,\n" +
            "inod.not_due_days - datediff(:asOnDate, sinv.invoice_date), 0)  as notdueDays,\n" +
            "sinv.distributor_id, dis.distributor_name\n" +
            "from sales_invoice sinv\n" +
            "\n" +
            "inner join sales_invoice_challan_map sicm on sinv.id = sicm.sales_invoice_id\n" +
            "and sinv.company_id=:company_id and sinv.is_deleted is false \n" +
            "and sinv.remaining_amount >0\n" +
            "and (coalesce(:start_date) is null or sinv.invoice_date >= :start_date)\n" +
            "and (coalesce(:end_date) is null or sinv.invoice_date <= :end_date)\n" +
            "inner join inv_delivery_challan idc on sicm.inv_delivery_challan_id = idc.id\n" +
            "inner join inv_transaction_details invtd\n" +
            "on idc.inv_transaction_id = invtd.inv_transaction_id\n" +
            "inner join sales_order_details sod on invtd.sales_order_details_id = sod.id and sod.is_active is true and sod.is_deleted is false\n" +
            "inner join sales_booking_details sbd on sod.sales_booking_details_id = sbd.id and sbd.is_active is true and sbd.is_deleted is false\n" +
            "and (coalesce(:productIds) is null or invtd.product_id in (:productIds))\n" +
            "inner join product prod on prod.id = invtd.product_id\n" +
            "inner join product_trade_price ptp on sbd.product_trade_price_id = ptp.id\n" +
            "left join trade_discount td on sbd.trade_discount_id = td.id and sbd.product_id = td.product_id\n" +
            "inner join unit_of_measurement u on prod.uom_id = u.id\n" +
            "inner join pack_size pk on prod.pack_size_id = pk.id\n" +
            "inner join invoice_nature invn on sinv.invoice_nature_id = invn.id\n" +
            "and invn.is_active is true and invn.is_deleted is false\n" +
            "inner join invoice_overdue inod on inod.invoice_nature_id = invn.id\n" +
            "and inod.is_active is true and inod.is_deleted is false\n" +
            "and inod.company_id = sinv.company_id\n" +
            "inner join distributor_sales_officer_map dis_so\n" +
            "on sinv.distributor_id = dis_so.distributor_id\n" +
            "#and dis_so.to_date is null\n" +
            "and dis_so.company_id = sinv.company_id\n" +
            "and dis_so.is_deleted is false\n" +
            "inner join distributor dis on sinv.distributor_id = dis.id\n" +
            "inner join application_user au on dis_so.sales_officer_id = au.id\n" +
            "inner join reporting_manager rm on dis_so.sales_officer_id = rm.application_user_id\n" +
            "and rm.to_date is null\n" +
            "inner join location_manager_map lmm\n" +
            "on rm.reporting_to_id = lmm.application_user_id\n" +
            "and lmm.to_date is null and lmm.company_id =sinv.company_id\n" +
            "inner join location lo on lmm.location_id = lo.id\n" +
            "\n" +
            "left join\n" +
            "(select invtdet.product_id, si.id,\n" +
            "sum(invtdet.quantity * round((ptp.trade_price -\n" +
            "(ifnull(case when calculation_type = 'PERCENTAGE'\n" +
            "then ((ptp.trade_price * td.discount_value)/100)\n" +
            "else td.discount_value end,0))),4)) -\n" +
            "(si.invoice_discount* sum(invtdet.quantity * round((ptp.trade_price -\n" +
            "(ifnull(case when calculation_type = 'PERCENTAGE'\n" +
            "then ((ptp.trade_price * td.discount_value)/100)\n" +
            "else td.discount_value end,0))),4)) / (si.invoice_discount+si.invoice_amount)) sales_return_amount  \n" +
            "from sales_return_proposal srp\n" +
            "inner join sales_return_proposal_details srpd on srp.id = srpd.sales_return_proposal_id\n" +
            "inner join sales_return sr on srp.id = sr.sales_return_proposal_id\n" +
            "and srp.approval_status = 'APPROVED' and srp.is_active is true\n" +
            "and srp.is_deleted is false and sr.is_active is true and sr.is_deleted is false\n" +
            "inner join sales_invoice si on si.id = srp.sales_invoice_id\n" +
            "inner join \n" +
            "(select sr.id sales_return_id, sr.inv_transaction_id, invtd.product_id, invtd.quantity\n" +
            "from sales_return sr inner join inv_transaction_details invtd\n" +
            "on invtd.inv_transaction_id = sr.inv_transaction_id) invtdet\n" +
            "on sr.id = invtdet.sales_return_id and invtdet.product_id = srpd.product_id\n" +
            "inner join product_trade_price ptp on srpd.product_trade_price_id = ptp.id\n" +
            "and ptp.expiry_date is null\n" +
            "left join trade_discount td on srpd.trade_discount_id = td.id\n" +
            "and td.is_deleted is false group by srp.company_id, srp.distributor_id,\n" +
            "si.id, invtdet.product_id) rtn\n" +
            "on rtn.id = sinv.id and rtn.product_id = invtd.product_id \n" +
            "\n" +
            "where\n" +
            "sinv.is_active is true and sinv.is_deleted is false \n" +
            "and (coalesce(:salesOfficerIds) is null or dis_so.sales_officer_id in (:salesOfficerIds))\n" +
            "and (coalesce(:distributorIds) is null or sinv.distributor_id in (:distributorIds))\n" +
            ") invoice_info\n" +
            "inner join child_location_hierarchy lo_hierarchy\n" +
            "on invoice_info.location_id = lo_hierarchy.id\n" +
            "and (coalesce(:locationIds) is null or lo_hierarchy.id in (:locationIds))\n" +
            "inner join child_product_category_hierarchy pc on invoice_info.product_category_id = pc.id\n" +
            "and (coalesce(:categoryIds) is null or pc.id in (:categoryIds))\n" +
            "", nativeQuery = true)
    List<Map<String, Object>> getInvoiceAgingDataProduct(
            @Param("company_id") Long companyId,
            @Param("locationIds") List<Long> locationIds,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("productIds") List<Long> productIds,
            @Param("salesOfficerIds") List<Long> salesOfficerIds,
            @Param("distributorIds") List<Long> distributorIds,
            @Param("start_date") LocalDate startDate,
            @Param("end_date") LocalDate endDate,
            @Param("asOnDate") LocalDate asOnDate);

    @Query(value =  "#invoice wise ageing\n" +
            "select * from (select invoice_info.distributor_name,\n" +
            "invoice_info.invoice_no, invoice_info.invoice_date, invoice_info.year,\n" +
            "invoice_info.salesOfficer, invoice_info.invoice_nature,\n" +
            "invoice_info.overdueDays, invoice_info.notdueDays,\n" +
            "invoice_info.location_id, sum(invoice_info.remaining_amount) remaining_amount, sum(invoice_info.invoice_amount) invoice_amount,\n" +
            "SUM(IF(invoice_info.overdueDays <= 0, invoice_info.remaining_amount, 0)) notDues,\n" +
            "SUM(IF(invoice_info.overdueDays BETWEEN 1 AND 30, invoice_info.remaining_amount, 0)) level1,\n" +
            "SUM(IF(invoice_info.overdueDays BETWEEN 31 AND 60, invoice_info.remaining_amount, 0)) level31,\n" +
            "SUM(IF(invoice_info.overdueDays BETWEEN 61 AND 90, invoice_info.remaining_amount, 0)) level61,\n" +
            "SUM(IF(invoice_info.overdueDays BETWEEN 91 AND 120, invoice_info.remaining_amount, 0)) level91,\n" +
            "SUM(IF(invoice_info.overdueDays BETWEEN 121 AND 180, invoice_info.remaining_amount, 0)) level121,\n" +
            "SUM(IF(invoice_info.overdueDays > 180, invoice_info.remaining_amount, 0)) level181\n" +
            "from\n" +
            "(select lmm.location_id, sinv.id invoice_id,\n" +
            "sinv.invoice_no, date_format(sinv.invoice_date, '%d-%b-%Y') as invoice_date,\n" +
            "year(sinv.invoice_date) as year,\n" +
            "ifnull(sinv.invoice_amount, 0) invoice_amount,\n" +
            "ifnull(sinv.remaining_amount, 0) remaining_amount,\n" +
            "sinv.invoice_nature_id, invn.name invoice_nature,\n" +
            "datediff(:asOnDate, date_add(sinv.invoice_date, interval inod.not_due_days day)) as overdueDays,\n" +
            "if(inod.not_due_days - datediff(:asOnDate, sinv.invoice_date) > 0,\n" +
            "inod.not_due_days - datediff(:asOnDate, sinv.invoice_date), 0)  as notdueDays,\n" +
            "au.id salesOfficerId, au.name as salesOfficer,\n" +
            "sinv.distributor_id, dis.distributor_name\n" +
            "from sales_invoice sinv\n" +
            "inner join invoice_nature invn on sinv.invoice_nature_id = invn.id\n" +
            "and invn.is_active is true and invn.is_deleted is false\n" +
            "inner join invoice_overdue inod on inod.invoice_nature_id = invn.id\n" +
            "and inod.is_active is true and inod.is_deleted is false\n" +
            "and inod.company_id = :companyId\n" +
            "inner join distributor_sales_officer_map dis_so\n" +
            "on sinv.distributor_id = dis_so.distributor_id\n" +
            "and dis_so.to_date is null\n" +
            "and dis_so.company_id = :companyId\n" +
            "and (coalesce(:salesOfficerIds) is null\n" +
            "or dis_so.sales_officer_id in (:salesOfficerIds))\n" +
            "and dis_so.is_deleted is false\n" +
            "inner join distributor dis on sinv.distributor_id = dis.id\n" +
            "inner join application_user au on dis_so.sales_officer_id = au.id\n" +
            "inner join reporting_manager rm on dis_so.sales_officer_id = rm.application_user_id\n" +
            "and rm.to_date is null\n" +
            "inner join location_manager_map lmm\n" +
            "on rm.reporting_to_id = lmm.application_user_id\n" +
            "and lmm.to_date is null and lmm.company_id =:companyId\n" +
            "inner join location lo on lmm.location_id = lo.id\n" +
            "where sinv.company_id = :companyId\n" +
            "and sinv.is_deleted is false\n" +
            "and (:start_date is null or sinv.invoice_date >= :start_date)\n" +
            "and (:end_date is null or sinv.invoice_date <= :end_date)\n" +
            "and (coalesce(:distributorIds) is null\n" +
            "or sinv.distributor_id in (:distributorIds))\n" +
            "and sinv.remaining_amount >0\n" +
            "and sinv.is_active is true and sinv.is_deleted is false\n" +
            "group by sinv.id, lmm.location_id, au.id,\n" +
            "sinv.invoice_nature_id, inod.not_due_days, sinv.invoice_date) invoice_info\n" +
            "inner join invoice_nature ir\n" +
            "on invoice_info.invoice_nature_id = ir.id\n" +
            "group by ir.id, invoice_info.location_id, invoice_info.invoice_id,\n" +
            "invoice_info.salesOfficer, invoice_info.distributor_name,\n" +
            "invoice_info.overdueDays, invoice_info.notdueDays) summary\n" +
            "inner join child_location_hierarchy lo_hierarchy\n" +
            "on summary.location_id = lo_hierarchy.id\n" +
            "and (coalesce(:locationIds) is null or lo_hierarchy.id in (:locationIds))\n" +
            "", nativeQuery = true)
    List<Map<String, Object>> getInvoiceAgingData(
            @Param("companyId") Long companyId,
            @Param("locationIds") List<Long> locationIds,
            @Param("salesOfficerIds") List<Long> salesOfficerIds,
            @Param("distributorIds") List<Long> distributorIds,
            @Param("start_date") LocalDate startDate,
            @Param("end_date") LocalDate endDate,
            @Param("asOnDate") LocalDate asOnDate);


@Query(value = "SELECT date_format(pc.payment_date, '%d-%b-%Y') as payment_date, \n" +
        "      pc.collection_amount, i.invoice_no, i.invoice_amount, date_format(i.invoice_date, '%d-%b-%Y') as invoice_date \n" +
        "      FROM payment_collection  pc \n" +
        "      left join payment_collection_adjustment pca\n" +
        "      on pc.id = pca.payment_collection_id\n" +
        "      and pca.created_date <= :invoiceDate\n" +
        "      left join sales_invoice i \n" +
        "      on pca.sales_invoice_id = i.id\n" +
        "      where pc.company_id = :companyId\n" +
        "      and pc.approval_status = 'APPROVED'\n" +
        "      and pc.distributor_id = :distributorId\n" +
        "      and pc.created_date <= :invoiceDate\n" +
        "      ORDER BY pc.id DESC LIMIT 1", nativeQuery = true)
    List<Map> getLastPaymentRecord(
            @Param("companyId") Long companyId,
            @Param("distributorId") Long distributorId,
            @Param("invoiceDate") LocalDateTime invoiceDate);

    @Query(value = "#Get Invoice List for Debit Credit\n" +
            "select @isOpeningBalance \\:= \"N\" as isOpeningBalance, si.id, date_format(si.invoice_date, '%Y-%m-%d') as invoiceDate,\n" +
            "       si.invoice_no as invoiceNo,\n" +
            "       round(si.invoice_amount, 2) invoive_actual_amount,\n" +
            "       # return amount subtract for current actual invoice value\n" +
            "       round(si.invoice_amount, 2) -      \n" +
            "(ifnull(sales_return.sales_return_amount,0) + ifnull(credit.credit_amount,0)) as invoiceAmount,\n" +
            "       round(si.remaining_amount, 2) as remainingAmount, ifnull(sales_return.sales_return_amount, 0) as sales_return_amount\n" +
            "from sales_invoice si\n" +
            "left join\n" +
            "( select srp.sales_invoice_id,\n" +
            "sum(invtdet.quantity * round((ptp.trade_price -\n" +
            "(ifnull(case when calculation_type = 'PERCENTAGE'\n" +
            "then ((ptp.trade_price * td.discount_value)/100)\n" +
            "else td.discount_value end,0))),4)) -\n" +
            "(si.invoice_discount* sum(invtdet.quantity * round((ptp.trade_price -\n" +
            "(ifnull(case when calculation_type = 'PERCENTAGE'\n" +
            "then ((ptp.trade_price * td.discount_value)/100)\n" +
            "else td.discount_value end,0))),4)) / (si.invoice_discount+si.invoice_amount)) sales_return_amount\n" +
            "from sales_return_proposal srp\n" +
            "inner join sales_return_proposal_details srpd on srp.id = srpd.sales_return_proposal_id\n" +
            "inner join sales_return sr on srp.id = sr.sales_return_proposal_id\n" +
            "and srp.approval_status = 'APPROVED' and srp.is_active is true\n" +
            "and srp.is_deleted is false and sr.is_active is true and sr.is_deleted is false\n" +
            "inner join sales_invoice si on si.id = srp.sales_invoice_id\n" +
            "inner join (select sr.id sales_return_id, sr.inv_transaction_id,\n" +
            "invtd.product_id, invtd.quantity\n" +
            "from sales_return sr inner join inv_transaction_details invtd\n" +
            "on invtd.inv_transaction_id = sr.inv_transaction_id) invtdet\n" +
            "on sr.id = invtdet.sales_return_id and invtdet.product_id = srpd.product_id\n" +
            "inner join product_trade_price ptp on srpd.product_trade_price_id = ptp.id\n" +
            "and ptp.expiry_date is null\n" +
            "left join trade_discount td on srpd.trade_discount_id = td.id\n" +
            "and td.is_deleted is false group by srp.company_id, srp.distributor_id,\n" +
            "srp.sales_invoice_id)\n" +
            "sales_return on si.id = sales_return.sales_invoice_id\n" +
            "\n" +
            "left join\n" +
            "(select cn.invoice_id, sum(ifnull(cn.amount, 0)) as credit_amount\n" +
            "from credit_debit_note cn\n" +
            "where cn.approval_status = 'APPROVED' and cn.note_type = 'CREDIT'\n" +
            "and cn.transaction_type = 'SALES_RETURN'\n" +
            "and cn.is_deleted is false\n" +
            "and cn.is_active is true\n" +
            "group by cn.invoice_id) as credit\n" +
            "on credit.invoice_id = si.id\n" +
            "\n" +
            "where si.company_id = :companyId\n" +
            "  and si.distributor_id = :distributorId\n" +
            "  and si.is_deleted is false\n" +
            "  and si.is_active is true\n" +
            "# NGLSC-2261  and si.is_accepted is true\n" +
            "#order by si.invoice_date desc\n" +
            "#Get Opening Invoice List for Debit Credit\n" +
            "union all\n" +
            "select @isOpeningBalance \\:= \"Y\" as isOpeningBalance,\n" +
            "disb.id, date_format(disb.created_date, '%Y-%m-%d') as invoiceDate, disb.reference_no as invoiceNo,\n" +
            "round(disb.balance, 2) invoive_actual_amount,\n" +
            "round (ifnull(disb.balance, 0) - ifnull(credit.credit_amount, 0), 2) as invoiceAmount,\n" +
            "round(remaining_balance, 4) as remainingAmount,\n" +
            "0 as sales_return_amount \n" +
            "from distributor_balance disb inner join invoice_nature `in`\n" +
            "on disb.invoice_nature_id = `in`.id\n" +
            "left join\n" +
            "(select cn.distributor_balance_id, sum(ifnull(cn.amount, 0)) as credit_amount\n" +
            "from credit_debit_note cn\n" +
            "where cn.approval_status = 'APPROVED' and cn.note_type = 'CREDIT'\n" +
            "and cn.transaction_type = 'SALES_RETURN'\n" +
            "and cn.is_deleted is false\n" +
            "and cn.is_active is true\n" +
            "group by cn.distributor_balance_id) as credit\n" +
            "on credit.distributor_balance_id = disb.id\n" +
            "where remaining_balance>0\n" +
            "and disb.company_id = :companyId\n" +
            "and disb.distributor_id = :distributorId\n" +
            "", nativeQuery = true)
    List<Map<String, Object>> getInvoiceListForDebitCreditNote(
            @Param("companyId") Long companyId,
            @Param("distributorId") Long distributorId);

    @Query(value = "select si.id from sales_invoice si \n" +
            "where si.company_id = :companyId \n" +
            "and si.distributor_id = :distributorId \n" +
            "and (:startDate is null or si.invoice_date >= :startDate)  \n" +
            "and (:endDate is null or si.invoice_date <= :endDate) \n" +
            "and si.is_active is true \n" +
            "and si.is_deleted is false", nativeQuery = true)
    List<Long>getInvoiceIdListByCompanyIdAndDistributorId(
            @Param("companyId") Long companyId,
            @Param("distributorId") Long distributorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);


    @Query(value =  "select c.*, coll_table.*\n" +
            "from (select inv_info.*\n" +
            "           , date_format(pc.payment_date, '%d-%m-%Y')             collection_date\n" +
            "           , pc.payment_no                                        slip_no\n" +
            "           , pca.ord_amount                                       incentive_amount\n" +
            "           , pca.adjusted_amount                                  collection_amount\n" +
            "           , DATEDIFF(pc.payment_date, inv_info.invoice_date_raw) collection_days\n" +
            "      from (select l.id                                             location_id\n" +
            "                 , so.name                                          so_name\n" +
            "                 , d.id                                             distributor_id\n" +
            "                 , d.distributor_name                               distributor_name\n" +
            "                 , si.invoice_no                                    invoice_no\n" +
            "                 , round(si.invoice_amount + si.discount_amount +si.invoice_discount, 2) gross_invoice_amount\n" +
            "                 , date_format(si.invoice_date, '%d-%m-%Y')         invoice_date\n" +
            "                 , si.invoice_date                                  invoice_date_raw\n" +
            "                 , si.discount_amount                               trade_discount\n" +
            "                 ,  round(si.invoice_amount, 2)                                net_invoice_amount\n" +
            "                 , si.id                                            invoice_id\n" +
            "                 , round( si.invoice_discount , 2)                         special_discount\n" +
            "            from sales_invoice si\n" +
            "                     inner join sales_invoice_challan_map sicm\n" +
            "                                on si.id = sicm.sales_invoice_id\n" +
            "                                and si.company_id = :companyId \n" +
            "                                and sicm.is_active is true\n" +
            "                                 and sicm.is_deleted is false\n" +
            "                     inner join inv_delivery_challan idc\n" +
            "                                on sicm.inv_delivery_challan_id = idc.id\n" +
            "                     inner join inv_transaction it\n" +
            "                                on idc.inv_transaction_id = it.id\n" +
            "                     inner join inv_transaction_details itd\n" +
            "                                on it.id = itd.inv_transaction_id\n" +
            "                                    and itd.is_active is true\n" +
            "                                    and itd.is_deleted is false\n" +
            "                     inner join sales_order_details sod\n" +
            "                                on itd.sales_order_details_id = sod.id\n" +
            "                                    and sod.is_active is true\n" +
            "                                    and sod.is_deleted is false\n" +
            "                     inner join sales_booking_details sbd\n" +
            "                                on sod.sales_booking_details_id = sbd.id\n" +
            "                                    and sbd.is_active is true\n" +
            "                                    and sbd.is_deleted is false\n" +
            "                     inner join sales_booking sb\n" +
            "                                on sbd.sales_booking_id = sb.id\n" +
            "                                    and sb.is_active is true\n" +
            "                                    and sb.is_deleted is false\n" +
            "                                    and (coalesce(:salesOfficerIds) is null or sb.sales_officer_id in (:salesOfficerIds))\n" +
            "                                    and (coalesce(:distributorIds) is null  or sb.distributor_id in (:distributorIds))\n" +
            "                     inner join application_user so\n" +
            "                                on sb.sales_officer_id = so.id\n" +
            "                     inner join distributor_sales_officer_map dsom\n" +
            "                                on so.id = dsom.sales_officer_id\n" +
            "                                    and dsom.is_active is true\n" +
            "                                    and dsom.is_deleted is false\n" +
            "                     inner join distributor d\n" +
            "                                on dsom.distributor_id = d.id\n" +
            "                                    and sb.distributor_id = d.id\n" +
            "                     inner join reporting_manager rm\n" +
            "                                on so.id = rm.application_user_id\n" +
            "                                    and rm.is_active is true\n" +
            "                                    and rm.is_deleted is false\n" +
            "                     inner join location_manager_map lmm\n" +
            "                                on rm.reporting_to_id = lmm.application_user_id\n" +
            "                                    and lmm.is_active is true\n" +
            "                                    and lmm.is_deleted is false\n" +
            "                     inner join location l\n" +
            "                                on lmm.location_id = l.id\n" +
            "                     group by lmm.location_id, si.id, sb.sales_officer_id, sb.distributor_id\n" +
            "                     order by lmm.location_id, sb.sales_officer_id, sb.distributor_id, si.id) as inv_info\n" +
            "                     inner join payment_collection_adjustment pca\n" +
            "                          on inv_info.invoice_id = pca.sales_invoice_id\n" +
            "                     and (:startDate is null or pca.mapping_date >= :startDate)\n" +
            "                     and (:endDate is null or pca.mapping_date <= :endDate)\n" +
            "                     inner join payment_collection pc\n" +
            "                          on pc.id = pca.payment_collection_id) as coll_table\n" +
            "                     inner join child_location_hierarchy c on c.id = coll_table.location_id " +
            "                    and (coalesce(:locationIds) is null or c.id in (:locationIds))\n", nativeQuery = true)
    List<Map<String, Object>> getOrderToCashCycleDetailsReport(
            @Param("companyId") Long companyId,
            @Param("locationIds") List<Long> locationIds,
            @Param("salesOfficerIds") List<Long> salesOfficerIds,
            @Param("distributorIds") List<Long> distributorIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);


    @Query(value =  "select \n" +
            "final_table.zone, \n" +
            "final_table.area, \n" +
            "final_table.territory, \n" +
            "final_table.so_name, \n" +
            "final_table.distributor_name, \n" +
            "count(final_table.invoice_id)       number_of_invoice,\n" +
            "sum(final_table.net_invoice_amount) sum_of_net_invoice_amount,\n" +
            "sum(final_table.collection_amount)  sum_of_collection_amount,\n" +
            "round(avg(final_table.collection_days),0)    average_collection_days\n" +
            "from (select c.*, coll_table.*\n" +
            "from (select inv_info.*\n" +
            "           , date_format(pc.payment_date, '%d-%m-%Y')             collection_date\n" +
            "           , pc.payment_no                                        slip_no\n" +
            "           , pca.ord_amount                                       incentive_amount\n" +
            "           , pca.adjusted_amount                                  collection_amount\n" +
            "           , DATEDIFF(pc.payment_date, inv_info.invoice_date_raw) collection_days\n" +
            "      from (select l.id                                             location_id\n" +
            "                 , so.name                                          so_name\n" +
            "                 , d.id                                             distributor_id\n" +
            "                 , d.distributor_name                               distributor_name\n" +
            "                 , si.invoice_no                                    invoice_no\n" +
            "                 , round(si.invoice_amount + si.discount_amount +si.invoice_discount, 2) gross_invoice_amount\n" +
            "                 , date_format(si.invoice_date, '%d-%m-%Y')         invoice_date\n" +
            "                 , si.invoice_date                                  invoice_date_raw\n" +
            "                 , si.discount_amount                               trade_discount\n" +
            "                 ,  round(si.invoice_amount, 2)                     net_invoice_amount\n" +
            "                 , si.id                                            invoice_id\n" +
            "                 , round( si.invoice_discount , 2)                  special_discount\n" +
            "            from sales_invoice si\n" +
            "                     inner join sales_invoice_challan_map sicm\n" +
            "                                on si.id = sicm.sales_invoice_id\n" +
            "                                and si.company_id = :companyId \n" +
            "                                and sicm.is_active is true\n" +
            "                                 and sicm.is_deleted is false\n" +
            "                     inner join inv_delivery_challan idc\n" +
            "                                on sicm.inv_delivery_challan_id = idc.id\n" +
            "                     inner join inv_transaction it\n" +
            "                                on idc.inv_transaction_id = it.id\n" +
            "                     inner join inv_transaction_details itd\n" +
            "                                on it.id = itd.inv_transaction_id\n" +
            "                                    and itd.is_active is true\n" +
            "                                    and itd.is_deleted is false\n" +
            "                     inner join sales_order_details sod\n" +
            "                                on itd.sales_order_details_id = sod.id\n" +
            "                                    and sod.is_active is true\n" +
            "                                    and sod.is_deleted is false\n" +
            "                     inner join sales_booking_details sbd\n" +
            "                                on sod.sales_booking_details_id = sbd.id\n" +
            "                                    and sbd.is_active is true\n" +
            "                                    and sbd.is_deleted is false\n" +
            "                     inner join sales_booking sb\n" +
            "                                on sbd.sales_booking_id = sb.id\n" +
            "                                    and sb.is_active is true\n" +
            "                                    and sb.is_deleted is false\n" +
            "                                    and (coalesce(:salesOfficerIds) is null or sb.sales_officer_id in (:salesOfficerIds))\n" +
            "                                    and (coalesce(:distributorIds) is null  or sb.distributor_id in (:distributorIds))\n" +
            "                     inner join application_user so\n" +
            "                                on sb.sales_officer_id = so.id\n" +
            "                     inner join distributor_sales_officer_map dsom\n" +
            "                                on so.id = dsom.sales_officer_id\n" +
            "                                    and dsom.is_active is true\n" +
            "                                    and dsom.is_deleted is false\n" +
            "                     inner join distributor d\n" +
            "                                on dsom.distributor_id = d.id\n" +
            "                                    and sb.distributor_id = d.id\n" +
            "                     inner join reporting_manager rm\n" +
            "                                on so.id = rm.application_user_id\n" +
            "                                    and rm.is_active is true\n" +
            "                                    and rm.is_deleted is false\n" +
            "                     inner join location_manager_map lmm\n" +
            "                                on rm.reporting_to_id = lmm.application_user_id\n" +
            "                                    and lmm.is_active is true\n" +
            "                                    and lmm.is_deleted is false\n" +
            "                     inner join location l\n" +
            "                                on lmm.location_id = l.id\n" +
            "                     group by lmm.location_id, si.id, sb.sales_officer_id, sb.distributor_id\n" +
            "                     order by lmm.location_id, sb.sales_officer_id, sb.distributor_id, si.id) as inv_info\n" +
            "                     inner join payment_collection_adjustment pca\n" +
            "                          on inv_info.invoice_id = pca.sales_invoice_id\n" +
            "                     and (:startDate is null or pca.mapping_date >= :startDate)\n" +
            "                     and (:endDate is null or pca.mapping_date <= :endDate)\n" +
            "                     inner join payment_collection pc\n" +
            "                          on pc.id = pca.payment_collection_id) as coll_table\n" +
            "                     inner join child_location_hierarchy c on c.id = coll_table.location_id " +
            "                    and (coalesce(:locationIds) is null or c.id in (:locationIds))) final_table\n" +
            "                     group by final_table.zone, final_table.area, final_table.territory, final_table.so_name, final_table.distributor_name;\n", nativeQuery = true)
    List<Map<String, Object>> getOrderToCashCycleSummaryReport(
            @Param("companyId") Long companyId,
            @Param("locationIds") List<Long> locationIds,
            @Param("salesOfficerIds") List<Long> salesOfficerIds,
            @Param("distributorIds") List<Long> distributorIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

}


