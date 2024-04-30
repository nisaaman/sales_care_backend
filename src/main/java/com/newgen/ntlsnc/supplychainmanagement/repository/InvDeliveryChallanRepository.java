package com.newgen.ntlsnc.supplychainmanagement.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvDeliveryChallan;
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
 * @author anika
 * @Date ১৭/৪/২২
 */
@Repository
public interface InvDeliveryChallanRepository extends JpaRepository<InvDeliveryChallan, Long> {
    List<InvDeliveryChallan> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<InvDeliveryChallan> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    @Query(value = "select si.id             invoice_id\n" +
            "     , si.invoice_no     invoice_no\n" +
            "     , si.invoice_amount  invoice_amount\n" +
            "     , si.invoice_date   invoice_date\n" +
            "     , idc.id            delivery_challan_id\n" +
            "     , idc.challan_no    challan_no\n" +
            "     , sum(itd.quantity) total_quantity\n" +
            "     , n.name            invoice_nature_name\n" +
            "from sales_invoice si\n" +
            "inner join sales_invoice_challan_map sicm\n" +
            "on si.id = sicm.sales_invoice_id\n" +
            "and si.company_id = :companyId\n" +
            "and si.distributor_id = :distributorId\n" +
            "and si.invoice_nature_id = :invoiceNatureId \n" +
            "and (coalesce(:fromDate) is null or si.invoice_date >= (:fromDate))\n" +
            "and (coalesce(:toDate) is null or si.invoice_date <= (:toDate))\n" +
            "and si.is_active is true and si.is_deleted is false\n" +
            "and sicm.is_deleted is false and\n" +
            "         sicm.is_active is true\n" +
            "         inner join invoice_nature n on n.id = si.invoice_nature_id and n.is_deleted is false and n.is_active is true\n" +
            "         inner join inv_delivery_challan idc\n" +
            "                    on sicm.inv_delivery_challan_id = idc.id and idc.is_deleted is false and idc.is_active is true\n" +
            "         inner join inv_transaction_details itd\n" +
            "                    on idc.inv_transaction_id = itd.inv_transaction_id and itd.is_deleted is false and\n" +
            "                       itd.is_active is true\n" +
            "group by si.id, idc.id", nativeQuery = true)
    List<Map> findAllInvoiceAndChallanWiseDeliveryQuantityByCompanyAndDistributor(
            Long companyId, Long distributorId, Long invoiceNatureId,
            LocalDate fromDate, LocalDate toDate);

    @Query(value = "select d.id                          distributor_id\n" +
            "     , d.distributor_name            distributor_name\n" +
            "     , d.contact_no                  contact_no\n" +
            "     , ifnull(dlb.ledger_balance, 0) ledger_balance\n" +
            "     , count(c.id)                   number_of_challan\n" +
            "from inv_delivery_challan c\n" +
            "         inner join distributor d\n" +
            "                    on c.distributor_id = d.id\n" +
            "                        and c.has_invoice = false\n" +
            "                        and c.is_active is true\n" +
            "                        and c.is_deleted is false\n" +
            "                        and d.is_active is true\n" +
            "                        and d.is_deleted is false\n" +
            "                        and c.company_id = :companyId\n" +
            "                        and (:fromDate is null or c.delivery_date between :fromDate and :toDate)\n" +
            "         left join (select lt.distributor_id distributor_id, sum(lt.debit - lt.credit) ledger_balance\n" +
            "                    from ledger_transaction lt\n" +
            "                    where lt.company_id = :companyId\n" +
            "                    group by lt.company_id, lt.distributor_id) as dlb on dlb.distributor_id = d.id\n" +
            "         inner join (select dsm.distributor_id\n" +
            "                     from distributor_sales_officer_map dsm\n" +
            "                              inner join reporting_manager rm\n" +
            "                                         on dsm.sales_officer_id = rm.application_user_id\n" +
            "                                             and dsm.company_id = :companyId\n" +
            "                                             and dsm.is_deleted is false\n" +
            "                                             and dsm.is_active is true\n" +
            "                                             and rm.is_active is true\n" +
            "                                             and rm.is_deleted is false\n" +
            "                              inner join location_manager_map lmm\n" +
            "                                         on rm.reporting_to_id = lmm.application_user_id\n" +
            "                                             and lmm.is_deleted is false\n" +
            "                                             and lmm.is_active is true\n" +
            "                                             and lmm.location_id in :locationIdList\n" +
            "                     group by dsm.distributor_id\n" +
            "                     order by dsm.distributor_id) d_table\n" +
            "                    on d_table.distributor_id = d.id\n" +
            "group by d.id", nativeQuery = true)
    List<Map> getDistributorListWithTotalChallanNoByCompanyAndLocationListAndDateRange(Long companyId, List<Long> locationIdList, String fromDate, String toDate);  // fromDate = null = no date range

    @Query(value = "select b.id                                       sales_booking_id\n" +
            "     , so.id                                      sales_order_id\n" +
            "     , idc.id                                     delivery_challan_id\n" +
            "     , b.booking_no                               booking_no\n" +
            "     , b.notes                                    booking_note\n" +
            "     , DATE_FORMAT(b.booking_date, '%d-%b-%Y')    booking_date\n" +
            "     , so.order_no                                order_no\n" +
            "     , idc.challan_no                             challan_no\n" +
            "     , DATE_FORMAT(idc.delivery_date, '%d-%b-%Y') delivery_date\n" +
            "from sales_booking b\n" +
            "         inner join invoice_nature i\n" +
            "                    on b.invoice_nature_id = i.id\n" +
            "                        and b.company_id = :companyId\n" +
            "                        and b.invoice_nature_id = :invoiceNatureId\n" +
            "                        and b.is_active is true\n" +
            "                        and b.is_deleted is false\n" +
            "         inner join sales_order so\n" +
            "                    on b.id = so.sales_booking_id\n" +
            "                        and so.is_active is true\n" +
            "                        and so.is_deleted is false\n" +
            "         inner join sales_order_details sod\n" +
            "                    on so.id = sod.sales_order_id\n" +
            "                        and sod.is_active is true\n" +
            "                        and sod.is_deleted is false\n" +
            "         inner join inv_transaction_details itd\n" +
            "                    on sod.id = itd.sales_order_details_id\n" +
            "                        and itd.is_active is true\n" +
            "                        and itd.is_deleted is false\n" +
            "         inner join inv_transaction it\n" +
            "                    on itd.inv_transaction_id = it.id\n" +
            "                        and it.is_active is true\n" +
            "                        and it.is_deleted is false\n" +
            "         inner join inv_delivery_challan idc\n" +
            "                    on it.id = idc.inv_transaction_id\n" +
            "                        and idc.has_invoice is false \n" +
            "                        and idc.distributor_id = :distributorId\n" +
            "                        and (:fromDate is null or idc.delivery_date between :fromDate and :toDate)\n" +
            "                        and idc.is_active is true\n" +
            "                        and idc.is_deleted is false\n" +
            "group by idc.id, b.id, so.id\n" +
            "order by idc.id asc ", nativeQuery = true)
    List<Map> getAllDeliveryChallanByCompanyAndInvoiceNatureAndDistributorAndDateRange(Long companyId, Long invoiceNatureId, Long distributorId, String fromDate, String toDate);

    @Query(value = "select count(distinct(del.order_id)) order_count,\n" +
            "sum(ifnull(del.order_quantity,0)) order_quantity, sum(ifnull(inv.inv_chalan_quantity,0)) chalan_quantity,\n" +
            "sum(ifnull(del.order_quantity,0) - ifnull(inv.inv_chalan_quantity,0)) deliverable_quantity\n" +
            "from (select distinct(so.id) order_id, sb.company_id, sb.distributor_id,\n" +
            "sum(ifnull(sod.quantity,0)) order_quantity\n" +
            "from sales_order_details sod\n" +
            "inner join sales_order so on so.id = sod.sales_order_id\n" +
            "and so.approval_status='APPROVED'\n" +
            "inner join sales_booking sb on sb.id = so.sales_booking_id\n" +
            "inner join distributor d on sb.distributor_id = d.id\n" +
            "where sb.company_id = :company_id\n" +
            "and (:start_date is null or so.order_date >= :start_date)\n" +
            "and (:end_date is null or so.order_date <= :end_date)\n" +
            "and sb.distributor_id = :distributor_id\n" +
            "and so.is_active is true and so.is_deleted is false\n" +
            " group by so.id) as del\n" +
            "left join (select sum(ifnull(i.quantity,0)) inv_chalan_quantity, so.id order_id  \n" +
            "from inv_transaction_details i \n" +
            "inner join sales_order_details sod on i.sales_order_details_id = sod.id\n" +
            "inner join sales_order so on so.id = sod.sales_order_id\n" +
            "group by so.id) as inv\n" +
            "on del.order_id = inv.order_id\n" +
            "where (inv.inv_chalan_quantity is null or del.order_quantity > inv.inv_chalan_quantity)\n" +
            "group by del.company_id\n" +
            ";", nativeQuery = true)
    Map getDistributorOrderSummary(@Param("company_id") Long companyId,
                                   @Param("start_date") LocalDateTime startDate,
                                   @Param("end_date") LocalDateTime endDate,
                                   @Param("distributor_id") Long distributorId);

    @Query(value = "select del.distributor_id, del.order_id id, \n" +
            "del.order_no, del.order_date, del.delivery_date, \n" +
            "del.booking_no, del.booking_date, \n" +
            "sum(ifnull(del.order_quantity,0)) order_quantity, \n" +
            "sum(ifnull(inv.inv_chalan_quantity,0)) chalan_quantity, \n" +
            "sum(ifnull(del.order_quantity,0)) - sum(ifnull(inv.inv_chalan_quantity,0)) deliverable_quantity\n" +
            "from (select distinct(so.id) order_id, sb.company_id, sb.distributor_id, \n" +
            "so.order_no, so.order_date, so.delivery_date, \n" +
            "sb.booking_no, sb.booking_date, \n" +
            "sum(ifnull(sod.quantity,0)) order_quantity\n" +
            "from sales_order_details sod\n" +
            "inner join sales_order so on so.id = sod.sales_order_id\n" +
            "and so.approval_status='APPROVED'\n" +
            "inner join sales_booking sb on sb.id = so.sales_booking_id\n" +
            "inner join distributor d on sb.distributor_id = d.id\n" +
            "where sb.company_id = :company_id\n" +
            "and (:start_date is null or so.order_date >= :start_date)\n" +
            "and (:end_date is null or so.order_date <= :end_date)\n" +
            "and sb.distributor_id = :distributor_id\n" +
            "and so.is_active is true and so.is_deleted is false\n" +
            "group by so.id ) as del\n" +
            "left join (select sum(ifnull(i.quantity,0)) inv_chalan_quantity, so.id order_id  \n" +
            "from inv_transaction_details i \n" +
            "inner join sales_order_details sod on i.sales_order_details_id = sod.id\n" +
            "inner join sales_order so on so.id = sod.sales_order_id\n" +
            "group by so.id) as inv\n" +
            "on del.order_id = inv.order_id\n" +
            "where (inv.inv_chalan_quantity is null or del.order_quantity > inv.inv_chalan_quantity)\n" +
            "group by del.order_id\n" +
            ";", nativeQuery = true)
    List<Map> getDistributorOrderList(@Param("company_id") Long companyId,
                                   @Param("start_date") LocalDateTime startDate,
                                   @Param("end_date") LocalDateTime endDate,
                                   @Param("distributor_id") Long distributorId);


    @Query(value = "select distinct(p.id) id, \n" +
            "p.name p_name, " +
            "concat(p.name, ' ', p.item_size, ' ', u.abbreviation, ' * ', pk.pack_size) product_name,\n"+
            "p.product_sku, pc.name category_name, \n" +
            "so.id order_id, so.order_no, sbd.id sales_booking_details_id, \n" +
            "sod.id sales_order_details_id, p.company_id, sbd.free_quantity, \n" +
            "sod.quantity * tp.trade_price order_amount , \n" +
            "sum(case when i.quantity is not null then i.quantity else 0 end) challan_quantity, \n" +
            "sum(case when i.quantity is not null and i.picking_details_id is null\n " +
            "then i.quantity else 0 end) order_challan_quantity, \n" +
            "sum(case when i.quantity is not null and i.picking_details_id is not null\n" +
            "then i.quantity else 0 end) picking_challan_quantity, \n" +
            "sod.quantity order_quantity, sum(ifnull(pd.good_qty, 0)) pick_quantity \n" +
//            "sum(case when i.quantity is null and pd.good_qty is not null then pd.good_qty else 0 end) pick_quantity\n " +
            "from inv_transaction_details i \n" +
            "right join sales_order_details sod on sod.id = i.sales_order_details_id \n" +
            "left join picking_details pd on pd.id = i.picking_details_id \n" +
            "left join picking pick on pick.id = pd.picking_id and pick.status= \"CONFIRMED\" \n" +
            "right join sales_booking_details sbd on sbd.id = sod.sales_booking_details_id \n" +
            "inner join product_trade_price tp\n" +
            "on sbd.product_trade_price_id = tp.id\n" +
            "inner join product p on p.id = sbd.product_id\n" +
            "inner join pack_size pk on p.pack_size_id = pk.id\n" +
            "inner join unit_of_measurement u on p.uom_id = u.id\n" +
            "inner join product_category pc\n" +
            "on p.product_category_id = pc.id\n" +
            "inner join sales_order so on so.id = sod.sales_order_id\n" +
            "where so.id = :order_id\n" +
            "and sod.is_active is true and sod.is_deleted is false\n" +
            "group by p.company_id, p.id, sod.id, so.id\n" +
            "", nativeQuery = true)
    List<Map> getProductList(@Param("order_id") Long orderId);

    @Query(value = "select sb.company_id, so.id,\n" +
            "    so.order_no, so.order_date, so.delivery_date, sb.booking_no,\n" +
//          "    #sum(ifnull(pd.good_qty, 0)) product_pick_quantity,\n" +
            "    sum(case when itd.picking_details_id is null " +
            "    then ifnull(pd.good_qty, 0) else 0 end) product_pick_quantity\n" +
            "    from sales_order_details sod\n" +
            "    inner join sales_order so on so.id = sod.sales_order_id\n" +
            "    inner join sales_booking sb\n" +
            "    on sb.id = so.sales_booking_id\n" +
            "    inner join distributor d on sb.distributor_id = d.id\n" +
            "    inner join picking_details pd on sod.id = pd.sales_order_details_id\n" +
            "    inner join picking pick on pick.id = pd.picking_id and pick.status= \"CONFIRMED\" \n" +
            "    left join inv_transaction_details itd on itd.picking_details_id = pd.id\n" +
            "    where so.id = :order_id\n" +
            "    and pd.product_id = :product_id\n" +
            "    and so.is_active is true and so.is_deleted is false\n" +
            "    group by pd.sales_order_details_id" +
            "", nativeQuery = true)
    Map getOrderProductPickingSummary(@Param("order_id") Long orderId,
                                      @Param("product_id") Long productId);



    @Query(value = "select DISTINCT(driver_name) driver_name \n" +
            "from inv_delivery_challan \n" +
            "where organization_id = :organization_id \n"
    , nativeQuery = true)
    List<Map> getDriverList(@Param("organization_id") Long organizationId);


    List<InvDeliveryChallan> findAllByIdInAndIsActiveTrueAndIsDeletedFalse(List<Long> ids);


    List<InvDeliveryChallan> findByVehicleIdAndIsDeletedFalse(Long vehicleId);

    @Query(value = "select \n" +
            "  idc.id, \n" +
            "  idc.challan_no as challanNo, \n" +
            "  date_format(idc.delivery_date, '%d-%b-%Y') as deliveryDate, \n" +
            "  au.name as name, \n" +
            "  SUM(itd.quantity) as quantity \n" +
            "from \n" +
            "  inv_delivery_challan as idc \n" +
            "  INNER JOIN application_user as au ON idc.created_by = au.id \n" +
            "  INNER JOIN inv_transaction_details as itd ON idc.inv_transaction_id = itd.inv_transaction_id \n" +
            "where \n" +
            "  company_id = :companyId \n" +
            "  and distributor_id = :distributorId \n" +
            "GROUP BY \n" +
            "  idc.id, \n" +
            "  name", nativeQuery = true)
    List<Map<String, Object>> findDistributorWiseDeliveryChallanList(Long companyId, Long distributorId);


    @Query(value = "select idc.id,concat(p.name,' ',p.item_size,' ',uom.abbreviation,'*',ps.pack_size ) as productCodeName,\n" +
            "ps.pack_size as packSize, sum(ifnull(itd.quantity, 0)) as quantity, \n" +
            "ROUND(sum((itd.quantity/ps.pack_size)), 2) as ctnQuantity,pc.name as productCategoryName\n" +
            "from inv_delivery_challan as idc \n" +
            "INNER JOIN inv_transaction_details as itd ON itd.inv_transaction_id = idc.inv_transaction_id\n" +
            "INNER JOIN product as p ON p.id = itd.product_id\n" +
            "INNER JOIN pack_size as ps ON p.pack_size_id = ps.id\n" +
            "INNER JOIN product_category as pc ON pc.id=p.product_category_id\n" +
            "INNER JOIN unit_of_measurement as uom ON p.uom_id = uom.id\n" +
            "where idc.id =:deliveryChallanId\n" +
            "group by p.id,p.product_category_id"
            , nativeQuery = true)
    List<Map<String,Object>> findProductList(Long deliveryChallanId);

    @Query(value = "select * from (select distinct(p.picking_no),p.id picking_id, p.picking_date,p.status,\n" +
            "sb.company_id, d.id distributor_id, d.distributor_name, d.contact_no, \n" +
            "count(pd.product_id) as total_product,\n" +
            "count(distinct(so.id)) as total_order, \n" +
            "sum(ifnull(pd.good_qty,0)) goodQty, sum(ifnull(itd.quantity, 0)) challanQuantity \n" +
            "from sales_booking sb \n" +
            "right join sales_order so on so.sales_booking_id = sb.id\n" +
            "inner join picking_details pd on pd.sales_order_id = so.id\n" +
            "left join inv_transaction_details itd on pd.id = itd.picking_details_id\n" +
            "inner join picking p on p.id = pd.picking_id\n" +
            "inner join distributor d on d.id = sb.distributor_id\n" +
            "where sb.company_id = :companyId \n" +
            "and (:status is null or p.status = :status) \n" +
            "and sb.distributor_id = :distributorId \n" +
            "and (:startDate is null or p.picking_date >= :startDate) \n" +
            "and (:endDate is null or p.picking_date <= :endDate) \n" +
            "and d.is_active is true and d.is_deleted is false\n" +
            "and p.is_active is true and p.is_deleted is false\n" +
            "and pd.is_active is true and pd.is_deleted is false\n" +
            "and so.is_active is true and so.is_deleted is false\n" +
            "and sb.is_active is true and sb.is_deleted is false\n" +
            /*"and itd.picking_details_id is null\n" + *//*to remove picking that already challan done*/
            "GROUP BY  p.id) picklist where challanQuantity = 0", nativeQuery = true)
    List<Map> getDistributorPickingList(@Param("companyId") Long companyId,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate,
                                      @Param("distributorId") Long distributorId,
                                        @Param("status") String status);

    @Query(value = "select distinct(p.picking_no),p.id picking_id, p.picking_date,p.status,\n" +
            "sb.company_id, d.id distributor_id, d.distributor_name, d.contact_no, \n" +
            "count(pd.product_id) as total_product,\n" +
            "count(distinct(so.id)) as total_order, \n" +
            "sum(ifnull(pd.good_qty,0)) goodQty, sum(ifnull(itd.quantity, 0)) challanQuantity \n" +
            "from sales_booking sb \n" +
            "right join sales_order so on so.sales_booking_id = sb.id\n" +
            "inner join picking_details pd on pd.sales_order_id = so.id\n" +
            "left join inv_transaction_details itd on pd.id = itd.picking_details_id\n" +
            "inner join picking p on p.id = pd.picking_id\n" +
            "inner join distributor d on d.id = sb.distributor_id\n" +
            "where sb.company_id = :companyId \n" +
            "and (:status is null or p.status = :status) \n" +
            "and sb.distributor_id = :distributorId \n" +
            "and (:startDate is null or p.picking_date >= :startDate) \n" +
            "and (:endDate is null or p.picking_date <= :endDate) \n" +
            "and d.is_active is true and d.is_deleted is false\n" +
            "and p.is_active is true and p.is_deleted is false\n" +
            "and pd.is_active is true and pd.is_deleted is false\n" +
            "and so.is_active is true and so.is_deleted is false\n" +
            "and sb.is_active is true and sb.is_deleted is false\n" +
            "GROUP BY  p.id", nativeQuery = true)
    List<Map> getDistributorPickingListAll(@Param("companyId") Long companyId,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate,
                                        @Param("distributorId") Long distributorId,
                                        @Param("status") String status);

    @Query(value = "select distinct(p.id) id,\n" +
            "p.name p_name, p.product_sku, pc.name category_name,\n" +
            "concat(p.name, ' ', p.item_size, ' ', u.abbreviation, ' * ', pk.pack_size) product_name,\n"+
            "so.id order_id, so.order_no, sbd.id sales_booking_details_id,\n" +
            "sod.id sales_order_details_id, pd.id picking_details_id, p.company_id, sbd.free_quantity,\n" +
            "sod.quantity * tp.trade_price order_amount,\n" +
            "sum(case when i.quantity is not null then i.quantity else 0 end) challan_quantity, \n" +
            "sum(case when i.quantity is not null and i.picking_details_id is null\n " +
            "then i.quantity else 0 end) order_challan_quantity, \n" +
            "sum(case when i.quantity is not null and i.picking_details_id is not null\n" +
            "then i.quantity else 0 end) picking_challan_quantity, \n" +
            "sod.quantity order_quantity, pd.good_qty picking_quantity,pd.quantity,\n" +
            "pd.picking_id\n" +
            "from picking_details pd\n" +
            "inner join sales_order_details sod on sod.id = pd.sales_order_details_id\n" +
            "inner join sales_booking_details sbd on sbd.id = sod.sales_booking_details_id\n" +
            "left join inv_transaction_details i on pd.sales_order_details_id = i.sales_order_details_id\n" +
            "and pd.id = i.picking_details_id and i.is_deleted is false\n" +
            "inner join product_trade_price tp\n" +
            "on sbd.product_trade_price_id = tp.id\n" +
            "inner join product p on p.id = sbd.product_id\n" +
            "inner join pack_size pk on p.pack_size_id = pk.id\n" +
            "inner join unit_of_measurement u on p.uom_id = u.id\n" +
            "inner join product_category pc\n" +
            "on p.product_category_id = pc.id\n" +
            "inner join sales_order so on so.id = sod.sales_order_id\n" +
            "WHERE so.id = :orderId  and pd.picking_id = :pickingId \n" +
            "AND so.is_deleted is false\n" +
            "AND pc.is_deleted is false\n" +
            "AND p.is_deleted is false\n" +
            "AND tp.is_deleted is false\n" +
            "AND sbd.is_deleted is false\n" +
            "AND sod.is_deleted is false\n" +
            "AND pd.is_active is true AND pd.is_deleted is false\n" +
            "GROUP BY p.company_id, p.id, sod.id, so.id, pd.id", nativeQuery = true)
    List<Map> getProductListByPicking(@Param("pickingId") Long pickingId, @Param("orderId") Long orderId);

    @Query(value = "select sb.company_id, sb.depot_id, \n" +
            "sum(case when i.quantity is not null then i.quantity else 0 end) challan_quantity, \n" +
            "sum(case when i.quantity is not null and i.picking_details_id is null\n " +
            "then i.quantity else 0 end) order_challan_quantity, \n" +
            "sum(case when i.quantity is not null and i.picking_details_id is not null\n" +
            "then i.quantity else 0 end) picking_challan_quantity, \n" +
            "sod.quantity order_quantity, sum(ifnull(pd.good_qty, 0)) pick_quantity \n" +
            "from inv_transaction_details i \n" +
            "right join sales_order_details sod on sod.id = i.sales_order_details_id \n" +
            "inner join sales_booking_details sbd on sbd.id = sod.sales_booking_details_id \n" +
            "inner join sales_booking sb on sb.id = sbd.sales_booking_id \n" +
            "left join picking_details pd on pd.id = i.picking_details_id \n" +
            "where sod.id = :sales_order_details_id\n" +
            "and sod.is_active is true and sod.is_deleted is false\n" +
            "group by sod.id\n" +
            "", nativeQuery = true)
    Map getOrderItemChallanInformation(@Param("sales_order_details_id") Long salesOrderDetailsId);

    @Query(value = "select pd.id, \n" +
            "itd.quantity challan_quantity \n" +
            "from picking p \n" +
            "inner join picking_details pd on p.id = pd.picking_id \n" +
            "inner join inv_transaction_details itd on pd.id = itd.picking_details_id \n" +
            "where p.id = :picking_id\n" +
            "and pd.is_active is true and pd.is_deleted is false\n" +
            "", nativeQuery = true)
    List<Map> getPickingDeliveredInfo(@Param("picking_id") Long pickingId);
}
