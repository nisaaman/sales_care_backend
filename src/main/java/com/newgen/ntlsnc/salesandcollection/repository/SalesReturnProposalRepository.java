package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.salesandcollection.entity.SalesReturnProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author marziah
 * @date 20/04/22
 */

@Repository
public interface SalesReturnProposalRepository extends JpaRepository<SalesReturnProposal, Long> {
    List<SalesReturnProposal> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<SalesReturnProposal> findBySalesInvoiceIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    Optional<SalesReturnProposal> findByIdAndIsDeletedFalseAndIsActiveTrue(Long salesReturnProposalId);
    @Query(value = "select srp.id, srp.proposal_no as proposalNo, srp.proposal_date as proposalDate, " +
            "DATE_FORMAT(srp.proposal_date, \"%d-%b-%Y\") as forMattedProposalDate, \n"+
            "dis.distributor_name as distributorName, dpt.depot_name as depotName, \n" +
            "au.name as salesOfficer, desg.name as designation, lo.name location_name,\n" +
            "@invoice_discount \\:= round(si.invoice_discount / (si.invoice_discount + si.invoice_amount), 4),\n" +
            "sum(round((case \n" +
            "                when intact_type = \"MC\" then (srpd.quantity * ps.pack_size)\n" +
            "                when intact_type = \"IP\" then (srpd.quantity)\n" +
            "                else (srpd.quantity/prod.item_size)\n" +
            "            end) * (ptp.trade_price - (ifnull(case \n" +
            "                   when calculation_type = 'PERCENTAGE' then ((ptp.trade_price * td.discount_value)/100) \n" +
            "                   else td.discount_value end,0))),4)-\n" +
            "round((case \n" +
            "                when intact_type = \"MC\" then (srpd.quantity * ps.pack_size)\n" +
            "                when intact_type = \"IP\" then (srpd.quantity)\n" +
            "                else (srpd.quantity/prod.item_size)\n" +
            "            end) * (ptp.trade_price - (ifnull(case \n" +
            "                   when calculation_type = 'PERCENTAGE' then ((ptp.trade_price * td.discount_value)/100) \n" +
            "                   else td.discount_value end,0))),4) * @invoice_discount ) as returnProposalAmount, \n" +
            "                   \n" +
            "sum(round((ifnull(invtdet.quantity, 0)) * (ptp.trade_price - (ifnull(case \n" +
            "                   when calculation_type = 'PERCENTAGE' then ((ptp.trade_price * td.discount_value)/100) \n" +
            "                   else td.discount_value end,0))),4)-\n" +
            "round((ifnull(invtdet.quantity, 0)) * (ptp.trade_price - (ifnull(case \n" +
            "                   when calculation_type = 'PERCENTAGE' then ((ptp.trade_price * td.discount_value)/100) \n" +
            "                   else td.discount_value end,0))),4) * @invoice_discount ) as returnAmount, \n" +
            "srp.approval_status as approvalStatus from sales_return_proposal srp \n" +
            "inner join sales_return_proposal_details srpd on srp.id = srpd.sales_return_proposal_id \n" +
            "and srp.company_id = :companyId  and ( :approvalStatus is  null or srp.approval_status = :approvalStatus ) and srp.is_active is true and srp.is_deleted is false \n" +
            "and srpd.is_active is true and srpd.is_deleted is false \n" +
            "and srp.proposal_date >= :startDate and srp.proposal_date <= :endDate \n" +
            "and srp.sales_officer_id IN (:salesOfficerUserLoginId)\n" +
            "inner join product prod on srpd.product_id = prod.id\n" +
            "left join pack_size ps on prod.pack_size_id = ps.id\n" +
            "inner join sales_invoice si on srp.sales_invoice_id = si.id \n" +
            "inner join sales_invoice_challan_map sicm on si.id = sicm.sales_invoice_id " +
            "and sicm.is_active is true and sicm.is_deleted is false\n" +
            "inner join inv_delivery_challan idc on sicm.inv_delivery_challan_id = idc.id " +
            "and idc.is_active is true and idc.is_deleted is false\n" +
            "inner join inv_transaction_details invtd on idc.inv_transaction_id = invtd.inv_transaction_id \n" +
            "inner join sales_order_details sod on invtd.sales_order_details_id = sod.id \n" +
            "inner join sales_booking_details sbd on sod.sales_booking_details_id = sbd.id \n" +
            "inner join sales_booking sb on sbd.sales_booking_id = sb.id \n" +
            "inner join depot dpt  on sb.depot_id = dpt.id \n" +
            "left join trade_discount td on sbd.trade_discount_id = td.id and td.is_active is true \n" +
            "and td.is_deleted is false \n" +
            "inner join distributor dis on srp.distributor_id = dis.id \n" +
            "inner join application_user au on srp.sales_officer_id = au.id \n" +
            "left join designation desg on au.designation_id = desg.id and desg.is_active is true " +
            "and desg.is_deleted is false\n" +
            "inner join product_trade_price ptp on sbd.product_trade_price_id = ptp.id \n" +
            "and ptp.is_active is true and ptp.is_deleted is false \n"    +

            "inner join reporting_manager rm \n" +
            "on srp.sales_officer_id = rm.application_user_id \n" +
            "and rm.to_date is null \n" +
            "inner join location_manager_map lmm \n" +
            "on rm.reporting_to_id = lmm.application_user_id \n" +
            "and lmm.to_date is null and lmm.company_id =:companyId\n" +
            "inner join location lo " +
            "on lmm.location_id = lo.id \n" +
            "left join (select sr.id, sr.sales_return_proposal_id, invtd.product_id, invtd.quantity\n" +
            "from sales_return sr\n" +
            "inner join inv_transaction_details invtd\n" +
            " on invtd.inv_transaction_id = sr.inv_transaction_id) invtdet\n" +
            " on srp.id = invtdet.sales_return_proposal_id\n" +
            " and invtdet.product_id = srpd.product_id\n" +

            "group by srp.id, srp.proposal_date,srp.proposal_no, \n" +
            "dis.distributor_name,au.name,desg.name,dpt.depot_name,srp.approval_status,lo.name \n" +
            "order by srp.proposal_date desc;",nativeQuery = true)
    List<Map<String, Object>> getSalesReturnProposalOverView(
            @Param("salesOfficerUserLoginId") List<Long> salesOfficerUserLoginId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("companyId") Long companyId,
            @Param("approvalStatus") String approvalStatus);

    @Query(value = "select prod.product_sku                                                                   as productSku,\n" +
            "       concat(prod.name, ' ', prod.item_size, ' ', uom.abbreviation, ' * ', ps.pack_size) as productName,\n" +
            "       pc.name                                                                            as productCategory,\n" +
            "       @invoice_discount \\:= round(si.invoice_discount / (si.invoice_discount + si.invoice_amount), 4),\n" +
            "       @tradePrice \\:= round((ptp.trade_price -\n" +
            "                             (ifnull(case\n" +
            "                                         when calculation_type = 'PERCENTAGE'\n" +
            "                                             then ((ptp.trade_price * td.discount_value) / 100)\n" +
            "                                         else td.discount_value end, 0))), 4)             as tradePrice,\n" +
            "       intact_type,\n" +
            "       @proposeQuantity \\:= (case\n" +
            "                                when intact_type = \"MC\" then (srpd.quantity * ps.pack_size)\n" +
            "                                when intact_type = \"IP\" then (srpd.quantity)\n" +
            "                                else (srpd.quantity / prod.item_size)\n" +
            "           end)                                                                           as proposeQuantity,\n" +
            "       ifnull(invtdet.quantity, 0)                                                        as returnQuantity,\n" +
            "       (case\n" +
            "            when intact_type = \"MC\" then ('pcs')\n" +
            "            when intact_type = \"IP\" then ('pcs')\n" +
            "            when intact_type = \"BU\" then (uom.abbreviation)\n" +
            "           end)                                                                           as uom,\n" +
            "       round((ifnull(invtdet.quantity, 0) * @tradePrice) -\n" +
            "             (ifnull(invtdet.quantity, 0) * @tradePrice * @invoice_discount), 4)          as returnAmount,\n" +
            "       round((@proposeQuantity * @tradePrice) -\n" +
            "             (@proposeQuantity * @tradePrice * @invoice_discount), 4) as returnProposalAmount,\n" +
            "       case when invtdet.quantity > 0 then round((ifnull(invtdet.quantity, 0) * @tradePrice) -\n" +
            "            (ifnull(invtdet.quantity, 0) * @tradePrice * @invoice_discount), 4)  /  ifnull(invtdet.quantity, 0)  else null end as returnPrice\n"+
            "from sales_return_proposal srp\n" +
            "         inner join sales_return_proposal_details srpd\n" +
            "                    on srp.id = srpd.sales_return_proposal_id\n" +
            "                        and srp.is_active is true\n" +
            "                        and srp.is_deleted is false\n" +
            "                        and srpd.is_active is true\n" +
            "                        and srpd.is_deleted is false\n" +
            "                        and srp.id = :salesReturnProposalId\n" +
            "         inner join sales_invoice si\n" +
            "                    on srp.sales_invoice_id = si.id\n" +
            "         left join sales_return sr\n" +
            "                   on srp.id = sr.sales_return_proposal_id\n" +
            "         left join (select sr.id sales_return_id\n" +
            "                         , sr.inv_transaction_id\n" +
            "                         , invtd.product_id\n" +
            "                         , invtd.quantity\n" +
            "                    from sales_return sr\n" +
            "                             inner join inv_transaction_details invtd\n" +
            "                                        on invtd.inv_transaction_id = sr.inv_transaction_id) invtdet\n" +
            "                   on sr.id = invtdet.sales_return_id\n" +
            "                       and invtdet.product_id = srpd.product_id\n" +
            "         inner join product prod\n" +
            "                    on srpd.product_id = prod.id\n" +
            "         left join unit_of_measurement uom\n" +
            "                   on prod.uom_id = uom.id\n" +
            "         left join pack_size ps\n" +
            "                   on prod.pack_size_id = ps.id\n" +
            "         inner join product_category pc\n" +
            "                    on prod.product_category_id = pc.id\n" +
            "         left join trade_discount td\n" +
            "                   on srpd.trade_discount_id = td.id\n" +
            "                       and td.is_active is true\n" +
            "                       and td.is_deleted is false\n" +
            "         inner join product_trade_price ptp\n" +
            "                    on srpd.product_trade_price_id = ptp.id\n" +
            "                        and ptp.is_active is true\n" +
            "                        and ptp.is_deleted is false" , nativeQuery = true)
    List<Map<String, Object>> getSalesReturnProposalDetails(
            @Param("salesReturnProposalId") Long salesReturnProposalId);

    @Query(value = "select srp.proposal_no as proposalNo, srp.proposal_date as proposalDate, idc.challan_no challanNo,\n" +
            "sr.return_no as salesReturnNo, date_format(sr.return_date, \"%Y-%m-%d\") as salesReturnDate,\n"+
            "round(sum((case\n" +
            "    when intact_type = \"MC\" then (srpd.quantity * ps.pack_size)\n" +
            "    when intact_type = \"IP\" then (srpd.quantity)\n" +
            "    else (srpd.quantity/prod.item_size)\n" +
            "end)),4) as totalQuantity, inv.invoice_no as invoiceNo, invnat.name as invoiceNature," +
            "inv.invoice_date as invoiceDate, inv.invoice_amount as invoiceAmount,invnat.name from sales_return_proposal srp\n" +
            "inner join sales_return_proposal_details srpd on srp.id = srpd.sales_return_proposal_id \n" +
            "and srp.company_id= :companyId and srp.is_active is true and srp.is_deleted is false \n" +
            "and srpd.is_active is true and srpd.is_deleted is false and srp.id = :salesReturnProposalId \n"+
            "inner join sales_invoice inv on srp.sales_invoice_id = inv.id\n" +
            "inner join invoice_nature invnat on inv.invoice_nature_id = invnat.id\n" +
            "inner join product prod on srpd.product_id = prod.id\n" +
            "inner join inv_delivery_challan idc on srp.delivery_challan_id = idc.id\n" +
            "left join sales_return sr on srp.id = sr.sales_return_proposal_id\n"+
            "and sr.is_active is true and sr.is_deleted is false\n"+
            "left join pack_size ps on prod.pack_size_id = ps.id group by srp.id, sr.id\n", nativeQuery = true)
    Map<String, Object> getSalesReturnProposalSummary(
            @Param("salesReturnProposalId") Long salesReturnProposalId,
            @Param("companyId") Long companyId);

    @Query(value = "select srp.proposal_date as proposalDate, srp.return_reason as returnReason,\n" +
            "case when srp.approval_status = \"PENDING\" then \"Pending\" \n" +
            "     when srp.approval_status = \"APPROVED\" then \"Approved\"\n" +
            "end as approvalStatus, date(sr.return_date) receiveDate \n" +

            "from sales_return_proposal srp\n" +
            "left join sales_return sr on srp.id = sr.sales_return_proposal_id \n"+
            "where srp.company_id = :companyId and srp.is_active is true and srp.is_deleted is false \n" +
            "and srp.id= :salesReturnProposalId", nativeQuery = true)
    Map<String, Object> getSalesReturnProposalLiftCycle(
            @Param("salesReturnProposalId") Long salesReturnProposalId,
            @Param("companyId") Long companyId);

    Optional<SalesReturnProposal> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    Optional<SalesReturnProposal> findById(Long id);

    Optional<SalesReturnProposal> findByCompanyIdAndSalesInvoiceIdAndDeliveryChallanIdAndDistributorIdAndApprovalStatusAndIsActiveTrueAndIsDeletedFalse(Long companyId, Long invoiceId, Long challanId, Long distributorId, ApprovalStatus approvalStatus);

    Optional<SalesReturnProposal> findByDeliveryChallanIdAndApprovalStatusAndIsActiveTrueAndIsDeletedFalse(Long challanId, ApprovalStatus approvalStatus);

    @Query(value = "select b.batch_no                                                                batch_no\n" +
            "     , p.name                                                                    product_name\n" +
            "     , p.product_sku                                                             product_sku\n" +
            "     , p.item_size                                                               item_size\n" +
            "     , uom.abbreviation                                                          abbreviation\n" +
            "     , ps.pack_size                                                              pack_size\n" +
            "     , ptp.trade_price                                                           trade_price\n" +
            "     , t_re.quantity                                                             return_quantity\n" +
            "     , t_re.intact_type                                                          intact_type\n" +
            "     , t_re.quantity * p.item_size                                               return_base_quantity\n" +
            "     , td.quantity_in_uom                                                        quantity_in_uom\n" +
            "     , @sell_price \\:= case\n" +
            "                          when (t.calculation_type is not null and t.calculation_type = 'PERCENTAGE')\n" +
            "                              then ptp.trade_price - (ptp.trade_price * t.discount_value / 100)\n" +
            "                          else ptp.trade_price - IFNULL(t.discount_value, 0) end sell_price\n" +
            "     , @sell_price * t_re.quantity as                                            return_quantity_price\n" +
            "from inv_delivery_challan dc\n" +
            "         inner join inv_transaction_details td\n" +
            "                    on td.inv_transaction_id = dc.inv_transaction_id\n" +
            "                        and dc.id =  :deliveryChallanId  \n" +
            "                        and dc.is_active is true\n" +
            "                        and dc.is_deleted is false\n" +
            "                        and td.is_active is true\n" +
            "                        and td.is_deleted is false\n" +
            "         inner join batch b\n" +
            "                    on b.id = td.batch_id\n" +
            "         inner join product p\n" +
            "                    on td.product_id = p.id\n" +
            "                        and p.is_active is true\n" +
            "                        and p.is_deleted is false\n" +
            "         inner join unit_of_measurement uom\n" +
            "                    on p.uom_id = uom.id\n" +
            "                        and uom.is_active is true\n" +
            "                        and uom.is_deleted is false\n" +
            "         inner join pack_size ps\n" +
            "                    on p.pack_size_id = ps.id\n" +
            "                        and ps.is_active is true\n" +
            "                        and ps.is_deleted is false\n" +
            "         inner join sales_order_details sod\n" +
            "                    on td.sales_order_details_id = sod.id\n" +
            "                        and sod.is_active is true\n" +
            "                        and sod.is_deleted is false\n" +
            "         inner join sales_booking_details sbd\n" +
            "                    on sod.sales_booking_details_id = sbd.id\n" +
            "                        and sbd.is_active is true\n" +
            "                        and sbd.is_deleted is false\n" +
            "         inner join product_trade_price ptp\n" +
            "                    on ptp.id = sbd.product_trade_price_id\n" +
            "                        and ptp.is_active is true\n" +
            "                        and ptp.is_deleted is false\n" +
            "         left join trade_discount t\n" +
            "                   on t.id = sbd.trade_discount_id\n" +
            "                       and t.is_active is true\n" +
            "                       and t.is_deleted is false\n" +
            "         inner join (select srp.delivery_challan_id\n" +
            "                          , srpd.product_id\n" +
            "                          , sum(srpd.quantity) quantity\n" +
            "                          , srpd.batch_id\n" +
            "                          , srpd.intact_type\n" +
            "                     from sales_return_proposal srp\n" +
            "                              inner join sales_return_proposal_details srpd\n" +
            "                                         on srp.id = srpd.sales_return_proposal_id\n" +
            "                                             and srp.delivery_challan_id =  :deliveryChallanId  \n" +
            "                                             and srp.approval_status = 'DRAFT'\n" +
            "                                             and srp.is_active is true\n" +
            "                                             and srp.is_deleted is false\n" +
            "                                             and srpd.is_active is true\n" +
            "                                             and srpd.is_deleted is false\n" +
            "                     group by srp.delivery_challan_id, srpd.product_id, srpd.batch_id, srpd.intact_type) as t_re\n" +
            "                    on t_re.delivery_challan_id = dc.id\n" +
            "                        and t_re.product_id = td.product_id\n" +
            "                        and t_re.batch_id = td.batch_id\n" +
            "         left join (select srp.delivery_challan_id\n" +
            "                         , srpd.product_id\n" +
            "                         , sum(srpd.quantity) quantity\n" +
            "                         , srpd.batch_id\n" +
            "                    from sales_return_proposal srp\n" +
            "                             inner join sales_return_proposal_details srpd\n" +
            "                                        on srp.id = srpd.sales_return_proposal_id\n" +
            "                                            and srp.delivery_challan_id = :deliveryChallanId \n" +
            "                                            and srp.approval_status in ('PENDING', 'APPROVED')\n" +
            "                                            and srp.is_active is true\n" +
            "                                            and srp.is_deleted is false\n" +
            "                                            and srpd.is_active is true\n" +
            "                                            and srpd.is_deleted is false\n" +
            "                    group by srp.delivery_challan_id, srpd.product_id, srpd.batch_id) as pre_return\n" +
            "                   on pre_return.delivery_challan_id = dc.id\n" +
            "                       and pre_return.product_id = td.product_id\n" +
            "                       and pre_return.batch_id = td.batch_id", nativeQuery = true)
    List<Map> getProductWiseSalesReturnProposalDetailsListByDeliveryChallanId(@Param("deliveryChallanId") Long deliveryChallanId);

    @Query(value = "select count(case when p.approval_status = 'APPROVED' THEN p.id end) as approved\n" +
            "     , count(case when p.approval_status = 'PENDING' THEN p.id end)  as pending\n" +
            "     , count(case when p.approval_status = 'REJECTED' THEN p.id end) as rejecte\n" +
            "     , count(case when p.approval_status = 'DRAFT' THEN p.id end)    as draft\n" +
            "from sales_return_proposal p\n" +
            "where p.is_active is true\n" +
            "  and p.is_deleted is false\n" +
            "  and p.sales_officer_id = :salesOfficerId \n" +
            "  and p.company_id = :companyId ", nativeQuery = true)
    Map getApprovalStatusSummaryByCompanyAndSalesOfficer(@Param("companyId") Long companyId, @Param("salesOfficerId") Long salesOfficerId);

    @Query(value = "select dc.id                                                      inv_delivery_challan_id\n" +
            "     , b.id                                                       batch_id\n" +
            "     , b.batch_no                                                 batch_no\n" +
            "     , td.rate                                                    rate\n" +
            "     , ptp.id                                                     product_trade_price_id\n" +
            "     , t.id                                                       trade_discount_id\n" +
            "     , td.quantity - ifnull(pre_return.quantity, 0)               quantity\n" +
            "     , p.id                                                       product_id\n" +
            "     , p.name                                                     product_name\n" +
            "     , p.product_sku                                              product_sku\n" +
            "     , p.item_size                                                item_size\n" +
            "     , uom.abbreviation                                           uom_abbreviation\n" +
            "     , ps.pack_size                                               pack_size\n" +
            "     , ptp.trade_price                                            trade_price\n" +
            "     , t_re.quantity                                              return_quantity\n" +
            "     , IFNULL(t.discount_value, 0)                                discount_value\n" +
            "     , case\n" +
            "           when t.calculation_type is null or t.calculation_type = 'EQUAL' then '='\n" +
            "           else '%' end                                           calculation_type\n" +
            "     , case\n" +
            "           when (t.calculation_type is not null and t.calculation_type = '%')\n" +
            "               then ptp.trade_price - (ptp.trade_price * t.discount_value / 100)\n" +
            "           else ptp.trade_price - IFNULL(t.discount_value, 0) end sell_price\n" +
            "from inv_delivery_challan dc\n" +
            "         inner join inv_transaction_details td\n" +
            "                    on td.inv_transaction_id = dc.inv_transaction_id\n" +
            "                        and dc.id = :deliveryChallanId \n" +
            "                        and dc.is_active is true\n" +
            "                        and dc.is_deleted is false\n" +
            "                        and td.is_active is true\n" +
            "                        and td.is_deleted is false\n" +
            "         inner join batch b\n" +
            "                    on b.id = td.batch_id\n" +
            "         inner join product p\n" +
            "                    on td.product_id = p.id\n" +
            "                        and p.is_active is true\n" +
            "                        and p.is_deleted is false\n" +
            "         inner join unit_of_measurement uom\n" +
            "                    on p.uom_id = uom.id\n" +
            "                        and uom.is_active is true\n" +
            "                        and uom.is_deleted is false\n" +
            "         inner join pack_size ps\n" +
            "                    on p.pack_size_id = ps.id\n" +
            "                        and ps.is_active is true\n" +
            "                        and ps.is_deleted is false\n" +
            "         inner join sales_order_details sod\n" +
            "                    on td.sales_order_details_id = sod.id\n" +
            "                        and sod.is_active is true\n" +
            "                        and sod.is_deleted is false\n" +
            "         inner join sales_booking_details sbd\n" +
            "                    on sod.sales_booking_details_id = sbd.id\n" +
            "                        and sbd.is_active is true\n" +
            "                        and sbd.is_deleted is false\n" +
            "         inner join product_trade_price ptp\n" +
            "                    on ptp.id = sbd.product_trade_price_id\n" +
            "                        and ptp.is_active is true\n" +
            "                        and ptp.is_deleted is false\n" +
            "         left join trade_discount t\n" +
            "                   on t.id = sbd.trade_discount_id\n" +
            "                       and t.is_active is true\n" +
            "                       and t.is_deleted is false\n" +
            "         left join (select srp.delivery_challan_id\n" +
            "                         , srpd.product_id\n" +
            "                         , sum(srpd.quantity) quantity\n" +
            "                         , srpd.batch_id\n" +
            "                    from sales_return_proposal srp\n" +
            "                             inner join sales_return_proposal_details srpd\n" +
            "                                        on srp.id = srpd.sales_return_proposal_id\n" +
            "                                            and srp.delivery_challan_id = :deliveryChallanId\n" +
            "                                            and srp.approval_status = 'DRAFT'\n" +
            "                                            and srp.is_active is true\n" +
            "                                            and srp.is_deleted is false\n" +
            "                                            and srpd.is_active is true\n" +
            "                                            and srpd.is_deleted is false\n" +
            "                    group by srp.delivery_challan_id, srpd.product_id, srpd.batch_id) as t_re\n" +
            "                   on t_re.delivery_challan_id = dc.id\n" +
            "                       and t_re.product_id = td.product_id\n" +
            "                       and t_re.batch_id = td.batch_id\n" +
            "         left join (select srp.delivery_challan_id\n" +
            "                         , srpd.product_id\n" +
            "                         , sum(srpd.quantity) quantity\n" +
            "                         , srpd.batch_id\n" +
            "                    from sales_return_proposal srp\n" +
            "                             inner join sales_return_proposal_details srpd\n" +
            "                                        on srp.id = srpd.sales_return_proposal_id\n" +
            "                                            and srp.delivery_challan_id = :deliveryChallanId \n" +
            "                                            and srp.approval_status in ('PENDING', 'APPROVED')\n" +
            "                                            and srp.is_active is true\n" +
            "                                            and srp.is_deleted is false\n" +
            "                                            and srpd.is_active is true\n" +
            "                                            and srpd.is_deleted is false\n" +
            "                    group by srp.delivery_challan_id, srpd.product_id, srpd.batch_id) as pre_return\n" +
            "                   on pre_return.delivery_challan_id = dc.id\n" +
            "                       and pre_return.product_id = td.product_id\n" +
            "                       and pre_return.batch_id = td.batch_id", nativeQuery = true)
    List<Map> findReturnProposalProductDetailsListByDeliveryChallanId(@Param("deliveryChallanId") Long deliveryChallanId);

    @Query(value = "select sr.id             sales_return_id\n" +
            "     , d.id              depot_id\n" +
            "     , srp.id            sales_return_proposal_id\n" +
            "     , idc.id            inv_delivery_challan_id\n" +
            "     , si.id             sales_invoice_id\n" +
            "     , sr.return_date    return_date\n" +
            "     , sr.return_note    return_note\n" +
            "     , sr.return_no      return_no\n" +
            "     , d.depot_name      depot_name\n" +
            "     , srp.proposal_no   proposal_no\n" +
            "     , srp.proposal_date proposal_date\n" +
            "     , idc.challan_no    challan_no\n" +
            "     , si.invoice_date   invoice_date\n" +
            "     , si.invoice_no     invoice_no\n" +
            "from sales_return_proposal srp\n" +
            "         left join sales_return sr\n" +
            "                   on sr.sales_return_proposal_id = srp.id and srp.is_active is true and srp.is_deleted is false and\n" +
            "                      sr.is_active is true and sr.is_deleted is false\n" +
            "         inner join inv_delivery_challan idc\n" +
            "                    on idc.id = srp.delivery_challan_id and srp.id = :salesReturnProposalId and srp.is_active is true and\n" +
            "                       srp.is_deleted is false\n" +
            "         inner join depot d on idc.depot_id = d.id and d.is_active is true and d.is_deleted is false\n" +
            "         inner join sales_invoice_challan_map m\n" +
            "                    on m.inv_delivery_challan_id = idc.id and m.is_active is true and m.is_deleted is false\n" +
            "         inner join sales_invoice si\n" +
            "                    on m.sales_invoice_id = si.id and si.is_active is true and si.is_deleted is false", nativeQuery = true)
    Map getDetailsInfo(@Param("salesReturnProposalId") Long salesReturnProposalId);

    @Query(value = "select p.name                                                                    product_name\n" +
            "     , p.item_size                                                               item_size\n" +
            "     , uom.abbreviation                                                          abbreviation\n" +
            "     , ps.pack_size                                                              pack_size\n" +
            "     , srpd.quantity                                                             return_quantity\n" +
            "     , srpd.intact_type                                                          intact_type\n" +
            "     , srpd.quantity * p.item_size                                               return_base_quantity\n" +
            "     , itd.quantity_in_uom                                                       quantity_in_uom\n" +
            "     , @sell_price := case\n" +
            "                          when (t.calculation_type is not null and t.calculation_type = '%')\n" +
            "                              then ptp.trade_price - (ptp.trade_price * t.discount_value / 100)\n" +
            "                          else ptp.trade_price - IFNULL(t.discount_value, 0) end sell_price\n" +
            "     , @sell_price * srpd.quantity as                                            return_quantity_price\n" +
            "from sales_return_proposal srp\n" +
            "         inner join sales_return_proposal_details srpd\n" +
            "                    on srp.id = srpd.sales_return_proposal_id and srp.id = :salesReturnProposalId and srpd.is_active is true and\n" +
            "                       srpd.is_deleted is false and srp.is_active is true and srp.is_deleted is false\n" +
            "         left join sales_return sr\n" +
            "                   on sr.sales_return_proposal_id = srp.id and sr.is_active is true and sr.is_deleted is false\n" +
            "         inner join product p on srpd.product_id = p.id and p.is_active is true and p.is_deleted is false\n" +
            "         inner join unit_of_measurement uom on p.uom_id = uom.id and uom.is_active is true and uom.is_deleted is false\n" +
            "         inner join pack_size ps on p.pack_size_id = ps.id and ps.is_active is true and ps.is_deleted is false\n" +
            "         inner join inv_delivery_challan dc\n" +
            "                    on dc.id = srp.delivery_challan_id and dc.is_active is true and dc.is_deleted is false\n" +
            "         inner join inv_transaction_details itd\n" +
            "                    on itd.inv_transaction_id = dc.inv_transaction_id and itd.batch_id = srpd.batch_id and\n" +
            "                       itd.is_active is true and itd.is_deleted is false\n" +
            "         inner join sales_order_details sod\n" +
            "                    on itd.sales_order_details_id = sod.id and sod.is_active is true and sod.is_deleted is false\n" +
            "         inner join sales_booking_details sbd\n" +
            "                    on sod.sales_booking_details_id = sbd.id and sbd.is_active is true and sbd.is_deleted is false\n" +
            "         inner join product_trade_price ptp\n" +
            "                    on ptp.id = sbd.product_trade_price_id and ptp.is_active is true and ptp.is_deleted is false\n" +
            "         left join trade_discount t on t.id = sbd.trade_discount_id and t.is_active is true and t.is_deleted is false", nativeQuery = true)
    List<Map> getReturnProductDetailsListBySalesReturnProposal(@Param("salesReturnProposalId") Long salesReturnProposalId);

    @Query(value = "select srp.company_id, srp.id sales_return_proposal_id\n" +
            "     , srp.proposal_no                                                                              proposal_no\n" +
            "     , DATE_FORMAT(srp.proposal_date, '%d-%b-%Y')                                                   proposal_date\n" +
            "     , srp.invoice_from_date                                              invoice_from_date\n" +
            "     , srp.invoice_to_date                                                invoice_to_date\n" +
            "     , d.id                                                                           distributor_id\n" +
            "     , d.distributor_name                                                                           distributor_name\n" +
            "     , au.name                                                                                      sales_officer_name\n" +
            "     , d2.name                                                                                      designation_name\n" +
            "     , l.name                                                                                       sales_officer_location\n" +
            "     , inv.invoice_no                                                                               invoice_no\n" +
            "     , DATE_FORMAT(inv.invoice_date, '%d-%b-%Y')                                                   invoice_date\n" +
            "     , round(inv.invoice_amount, 2)                                            invoice_amount\n" +
            "     , idc.challan_no                                                                               challan_no\n" +
            "     , DATE_FORMAT(idc.delivery_date, '%d-%b-%Y')                                                   challan_date\n" +
            "     , n.name                                                                                       invoice_nature\n" +
            "     , case\n" +
            "       when srp.is_return is false then 'RECEIVE'\n" +
            "       else 'RECEIVED'\n" +
            "       end                                                                                             status\n" +
            "     , sum(srpd.quantity * round(ptp.trade_price\n" +
            "       - (case\n" +
            "       when td.calculation_type is null\n" +
            "       then 0\n" +
            "       when td.calculation_type = 'PERCENTAGE'\n" +
            "       then (ptp.trade_price * td.discount_value) / 100\n" +
            "       else td.discount_value\n" +
            "       end), 2))\n" +
            "-(inv.invoice_discount* sum(srpd.quantity * round((ptp.trade_price -\n" +
            "            (ifnull(case when calculation_type = 'PERCENTAGE'\n" +
            "            then ((ptp.trade_price * td.discount_value)/100)\n" +
            "            else td.discount_value end,0))),4)) / (inv.invoice_discount+inv.invoice_amount)) price\n" +
            "         from sales_return_proposal srp\n" +
            "         inner join sales_return_proposal_details srpd\n" +
            "         on srp.id = srpd.sales_return_proposal_id\n" +
            "         and srp.company_id = :companyId \n" +
            "         and srp.is_active is true\n" +
            "         and srp.is_deleted is false\n" +
            "         and srp.approval_status = 'APPROVED'\n" +
            "         and (:fromDate is null or srp.proposal_date between :fromDate and :toDate )\n" +
            "         inner join sales_invoice inv on inv.id = srp.sales_invoice_id \n" +
            "         inner join product_trade_price ptp on srpd.product_trade_price_id = ptp.id\n" +
            "         left join trade_discount td on td.id = srpd.trade_discount_id \n" +
            "         inner join distributor d\n" +
            "                    on srp.distributor_id = d.id\n" +
            "         inner join application_user au\n" +
            "                    on srp.sales_officer_id = au.id\n" +
            "         inner join designation d2\n" +
            "                    on au.designation_id = d2.id\n" +
            "         inner join reporting_manager rm\n" +
            "                    on srp.sales_officer_id = rm.application_user_id\n" +
            "                        and rm.to_date is null\n" +
            "                        and rm.is_active is true and\n" +
            "                       rm.is_deleted is false\n" +
            "         inner join location_manager_map lmm\n" +
            "                    on rm.reporting_to_id = lmm.application_user_id\n" +
            "                       and lmm.to_date is null\n" +
            "         and lmm.company_id=:companyId\n"+
            "                        and lmm.is_deleted is false\n" +
            "                        and lmm.is_active is true\n" +
            "         inner join location l\n" +
            "                    on lmm.location_id = l.id\n" +
            "                        and (:locationId is null or l.id = :locationId ) \n" +
            "                        and l.is_active is true\n" +
            "                        and l.is_deleted is false\n" +
            "         inner join inv_delivery_challan idc\n" +
            "                    on idc.id = srp.delivery_challan_id\n" +
            "         inner join sales_invoice si\n" +
            "                    on si.id = srp.sales_invoice_id\n" +
            "         inner join invoice_nature n\n" +
            "                    on n.id = si.invoice_nature_id\n" +
            "group by srp.id, l.name, srp.proposal_date \n" +
            "order by srp.proposal_date desc", nativeQuery = true)
    List<Map> getProposalListForReturnByCompanyAndLocationAndDateRange(Long companyId, Long locationId, String fromDate, String toDate);  // locationId = null =all, fromDate = null = no date range

    @Query(value = "select srpd.id srpd_id, p.id\n" +
            "     , p.product_sku                product_sku\n" +
            "     , p.name                       product_name\n" +
            "     , p.item_size                  item_size\n" +
            "     , ps.pack_size                 pack_size\n" +
            "     , uom.abbreviation             abbreviation\n" +
            "     , pc.name                      product_category_name\n" +
            "     , b.id batch_id, b.batch_no    batch_no\n" +
            "     , b.quantity                   batch_quantity\n" +
            "     , srpd.quantity                propose_quantity\n" +
            "     , ptp.trade_price              trade_price\n" +
            "     , ifnull(td.discount_value, 0) discount_value\n" +
            "     , round(case\n" +
            "                 when td.calculation_type = 'PERCENTAGE'\n" +
            "                     then ptp.trade_price - (ptp.trade_price * td.discount_value / 100)\n" +
            "                 else ptp.trade_price - IFNULL(td.discount_value, 0)\n" +
            "                 end, 2)            prodcut_price\n" +
            "from sales_return_proposal_details srpd\n" +
            "         inner join product p\n" +
            "                    on srpd.product_id = p.id\n" +
            "                        and srpd.sales_return_proposal_id = :salesReturnProposalId \n" +
            "                        and srpd.is_active is true\n" +
            "                        and srpd.is_deleted is false\n" +
            "         inner join pack_size ps\n" +
            "                    on p.pack_size_id = ps.id\n" +
            "         inner join unit_of_measurement uom\n" +
            "                    on p.uom_id = uom.id\n" +
            "         inner join product_category pc\n" +
            "                    on p.product_category_id = pc.id\n" +
            "         inner join batch b\n" +
            "                    on srpd.batch_id = b.id\n" +
            "         inner join product_trade_price ptp\n" +
            "                    on srpd.product_trade_price_id = ptp.id\n" +
            "         left join trade_discount td\n" +
            "                   on srpd.trade_discount_id = td.id", nativeQuery = true)
    List<Map<String, Object>> getSalesReturnProposalDetailsListBySalesReturnProposalId(Long salesReturnProposalId);

    @Query(value = "select  srp.company_id                     company_id" +
            "     , srp.proposal_no                            proposal_no\n" +
            "     , DATE_FORMAT(srp.proposal_date, '%d-%b-%Y') proposal_date\n" +
            "     , dt.id                                      depot_id\n" +
            "     , dt.depot_name                              depot_name\n" +
            "     , dt.contact_number                          depot_contact_number\n" +
            "     , d.id                                       distributor_id\n" +
            "     , d.distributor_name                         distributor_name\n" +
            "     , d.contact_no                               distributor_contact_no\n" +
            "     , si.invoice_no                              invoice_no\n" +
            "     , DATE_FORMAT(si.invoice_date, '%d-%b-%Y')   invoice_date\n" +
            "     , round(si.invoice_amount, 2)                invoice_amount\n" +
            "     , idc.challan_no                             challan_no\n" +
            "     , DATE_FORMAT(idc.delivery_date, '%d-%b-%Y') delivery_date\n" +
            "     , au.name                                    sales_officer_name\n" +
            "     , ds.name                                    sales_officer_designation\n" +
            "     , l.name                                     sales_officer_location\n" +
            "from sales_return_proposal srp\n" +
            "         inner join sales_invoice si\n" +
            "                    on srp.sales_invoice_id = si.id\n" +
            "                        and srp.id = :salesReturnProposalId\n" +
            "         inner join inv_delivery_challan idc\n" +
            "                    on srp.delivery_challan_id = idc.id\n" +
            "         inner join application_user au\n" +
            "                    on srp.sales_officer_id = au.id\n" +
            "         inner join designation ds\n" +
            "                    on au.designation_id = ds.id\n" +
            "         inner join reporting_manager rm\n" +
            "                    on srp.sales_officer_id = rm.application_user_id\n" +
            "                        and rm.to_date is null\n" +
            "                        and rm.is_active is true and\n" +
            "                       rm.is_deleted is false\n" +
            "         inner join location_manager_map lmm\n" +
            "                    on rm.reporting_to_id = lmm.application_user_id\n" +
            "                    and srp.company_id = lmm.company_id\n" +
            "                    and lmm.to_date is null\n" +
            "                        and lmm.is_deleted is false\n" +
            "                        and lmm.is_active is true\n" +
            "         inner join location l\n" +
            "                    on lmm.location_id = l.id\n" +
            "         inner join distributor d\n" +
            "                    on srp.distributor_id = d.id\n" +
            "         inner join depot dt on dt.id = idc.depot_id", nativeQuery = true)
    Map getSalesReturnProposalInfosById(Long salesReturnProposalId);


    @Query(value = "select srp.id, srp.proposal_no as proposalNo, srp.proposal_date as proposalDate, " +
            "DATE_FORMAT(srp.proposal_date, \"%M %Y\") as year_and_month, \n"+
            "dis.distributor_name as distributorName, dpt.depot_name as depotName, \n" +
            "au.name as salesOfficer, desg.name as designation, \n" +
            "sum(round((case \n" +
            "    when intact_type = \"MC\" then (srpd.quantity * ps.pack_size)\n" +
            "    when intact_type = \"IP\" then (srpd.quantity)\n" +
            "    else (srpd.quantity/prod.item_size)\n" +
            "end) * (ptp.trade_price - (ifnull(case \n" +
            "       when calculation_type = 'PERCENTAGE' then ((ptp.trade_price * td.discount_value)/100) \n" +
            "       else td.discount_value end,0))),4)) as returnProposalAmount, \n" +
            "srp.approval_status as approval_status, " +
            "@var\\:=:approvalActor approvalActor,\n" +
            "@var\\:=:level level,\n" +
            "@var\\:=:approvalStepId approvalStepId,\n" +
            "@var\\:=:multiLayerApprovalPathId multiLayerApprovalPathId,\n" +
            "@var\\:=:approvalActorId approvalActorId, \n" +
            "@var\\:=:approvalStatusName approvalStatus, \n" +
            "@var\\:=:approvalStepName approvalStepName \n" +
            "from sales_return_proposal srp \n" +
            "inner join sales_return_proposal_details srpd on srp.id = srpd.sales_return_proposal_id \n" +
            "and srp.company_id = :companyId  and srp.approval_status = :approvalStatus and srp.is_active is true and srp.is_deleted is false \n" +
            "and srpd.is_active is true and srpd.is_deleted is false \n" +
            "AND (COALESCE(:soList) is NULL OR srp.sales_officer_id IN (:soList)) \n" +
            "inner join product prod on srpd.product_id = prod.id\n" +
            "left join pack_size ps on prod.pack_size_id = ps.id\n" +
            "inner join sales_invoice si on srp.sales_invoice_id = si.id \n" +
            "inner join sales_invoice_challan_map sicm on si.id = sicm.sales_invoice_id " +
            "and sicm.is_active is true and sicm.is_deleted is false\n" +
            "inner join inv_delivery_challan idc on sicm.inv_delivery_challan_id = idc.id " +
            "and idc.is_active is true and idc.is_deleted is false\n" +
            "inner join inv_transaction_details invtd on idc.inv_transaction_id = invtd.inv_transaction_id \n" +
            "inner join sales_order_details sod on invtd.sales_order_details_id = sod.id \n" +
            "inner join sales_booking_details sbd on sod.sales_booking_details_id = sbd.id \n" +
            "inner join sales_booking sb on sbd.sales_booking_id = sb.id \n" +
            "inner join depot dpt  on sb.depot_id = dpt.id \n" +
            "left join trade_discount td on sbd.trade_discount_id = td.id and td.is_active is true \n" +
            "and td.is_deleted is false \n" +
            "inner join distributor dis on srp.distributor_id = dis.id \n" +
            "inner join application_user au on srp.sales_officer_id = au.id \n" +
            "inner join designation desg on au.designation_id = desg.id and desg.is_active is true " +
            "and desg.is_deleted is false\n" +
            "inner join product_trade_price ptp on sbd.product_trade_price_id = ptp.id \n" +
            "and ptp.is_active is true and ptp.is_deleted is false and ptp.expiry_date is null\n"    +
            "and expiry_date is null group by srp.id, srp.proposal_date,srp.proposal_no, \n" +
            "dis.distributor_name,au.name,desg.name,dpt.depot_name,srp.approval_status",nativeQuery = true)
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

    @Query(value = "select round(sum(srpd.quantity * (ptp.trade_price\n" +
            "    - (case\n" +
            "           when td.discount_value is null then 0\n" +
            "           when td.calculation_type = 'EQUAL' then td.discount_value\n" +
            "           when td.calculation_type = 'PERCENTAGE'\n" +
            "               then (ptp.trade_price * td.discount_value) / 100\n" +
            "        end))), 2) return_amount\n" +
            "from sales_return_proposal_details srpd\n" +
            "         inner join product_trade_price ptp\n" +
            "                    on srpd.product_trade_price_id = ptp.id\n" +
            "                        and srpd.sales_return_proposal_id = :salesReturnProposalId\n" +
            "                        and srpd.is_active is true\n" +
            "                        and srpd.is_deleted is false\n" +
            "         left join trade_discount td\n" +
            "                   on srpd.trade_discount_id = td.id", nativeQuery = true)
    Map getSalesReturnProposalAmountById(Long salesReturnProposalId);

    @Query(value = "select batch_id, bh.batch_no, bh.quantity batch_quantity from (\n" +
            "select distinct(batch_id) from sales_invoice si\n" +
            "inner join sales_invoice_challan_map sicm on si.id = sicm.sales_invoice_id\n" +
            "and sicm.is_active is true and sicm.is_deleted is false\n" +
            "inner join inv_delivery_challan idc on sicm.inv_delivery_challan_id = idc.id\n" +
            "and idc.is_active is true and idc.is_deleted is false\n" +
            "inner join inv_transaction_details invtd\n" +
            "on idc.inv_transaction_id = invtd.inv_transaction_id\n" +
            "and si.company_id = :companyId\n" +
            "and si.distributor_id = :distributorId\n" +
            "and (coalesce(:fromDate) is null or si.invoice_date >= (:fromDate))\n" +
            "and (coalesce(:toDate) is null or si.invoice_date <= (:toDate))\n" +
            "and invtd.product_id = :productId\n" +
            "and invtd.is_active is true\n" +
            "and invtd.is_deleted is false\n" +
            "group by batch_id) received_info\n" +
            "inner join batch bh on bh.id = received_info.batch_id"
            , nativeQuery = true)
    List<Map>getDistributorReceivedBatchListProduct(
            Long companyId, Long distributorId, Long productId,
            LocalDate fromDate, LocalDate toDate);

    @Query(value = "select distinct(si.id) invoice_id,\n" +
            "si.invoice_no, invoice_amount, invoice_date\n" +
            "from sales_invoice si\n" +
            "inner join sales_invoice_challan_map sicm on si.id = sicm.sales_invoice_id\n" +
            "and sicm.is_active is true and sicm.is_deleted is false\n" +
            "inner join inv_delivery_challan idc on sicm.inv_delivery_challan_id = idc.id\n" +
            "and idc.is_active is true and idc.is_deleted is false\n" +
            "inner join inv_transaction_details invtd\n" +
            "on idc.inv_transaction_id = invtd.inv_transaction_id\n" +
            "and si.distributor_id = :distributorId\n" +
            "and (coalesce(:fromDate) is null or si.invoice_date >= (:fromDate))\n" +
            "and (coalesce(:toDate) is null or si.invoice_date <= (:toDate))\n" +
            "and invtd.batch_id = :batchId\n" +
            "and invtd.is_active is true\n" +
            "and invtd.is_deleted is false\n" +
            "group by si.id;\n"
            , nativeQuery = true)
    List<Map>getCustomerInvoiceListBatch(
            Long batchId, Long distributorId,
            LocalDate fromDate, LocalDate toDate);

    @Query(value = "select distinct(idc.id) challan_id,\n" +
            "idc.challan_no, delivery_date challan_date\n" +
            "from sales_invoice si\n" +
            "inner join sales_invoice_challan_map sicm on si.id = sicm.sales_invoice_id\n" +
            "and sicm.is_active is true and sicm.is_deleted is false\n" +
            "inner join inv_delivery_challan idc on sicm.inv_delivery_challan_id = idc.id\n" +
            "and idc.is_active is true and idc.is_deleted is false\n" +
            "and si.distributor_id = :distributorId\n" +
            "and si.id = :invoiceId\n" +
            "and (coalesce(:fromDate) is null or si.invoice_date >= (:fromDate))\n" +
            "and (coalesce(:toDate) is null or si.invoice_date <= (:toDate))\n" +
            "and idc.is_active is true\n" +
            "and idc.is_deleted is false\n" +
            "group by idc.id;\n"
            , nativeQuery = true)
    List<Map>getCustomerChallanListInvoice(
        Long invoiceId, Long distributorId,
        LocalDate fromDate, LocalDate toDate);
}
