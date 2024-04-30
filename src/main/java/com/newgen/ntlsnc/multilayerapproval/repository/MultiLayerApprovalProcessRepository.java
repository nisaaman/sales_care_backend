package com.newgen.ntlsnc.multilayerapproval.repository;

import com.newgen.ntlsnc.multilayerapproval.entity.MultiLayerApprovalProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author nisa
 * @date 9/16/22
 * @time 8:29 PM
 */
@Repository
@EnableJpaRepositories
public interface MultiLayerApprovalProcessRepository extends JpaRepository<MultiLayerApprovalProcess, Long> {

//    List<Object> findByRefTableAndRefIdAndCompanyIdAndIsDeletedFalse(Object refTable, Long refId,
//                                                                     Long companyId);
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
            "FROM multi_layer_approval_process mlap\n" +
            "inner join sales_booking sb on sb.id = mlap.ref_id and mlap.level = :prevLevel and mlap.ref_table = 'SalesBooking' and mlap.is_next_layer_done = false\n" +
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
            "AND sb.is_active is true AND sb.is_deleted is false\n" +
            "GROUP BY sb.company_id, sb.id order by sb.booking_date desc", nativeQuery = true)
    List<Map<String, Object>> getPendingSalesBookingListForAuthorizationAfterFirstLevel(@Param("companyId") Long companyId,
                                                        @Param("soList") List<Long> soList,
                                                        @Param("approvalStatus") String approvalStatus,
                                                        @Param("approvalActor") String approvalActor,
                                                        @Param("level") Integer level,
                                                        @Param("approvalStepId") Long approvalStepId,
                                                        @Param("multiLayerApprovalPathId") Long multiLayerApprovalPathId,
                                                        @Param("approvalActorId") Long approvalActorId,
                                                        @Param("approvalStatusName") String approvalStatusName,
                                                        @Param("prevLevel") Integer prevLevel,
                                                        @Param("approvalStepName") String approvalStepName);

    @Query(value = "select cdn.id                                       creditDebitNoteId\t\n" +
            "     , d.contact_no                                        contactNo\n" +
            "     , cdn.note_no                                         noteNo\n" +
            "     , cdn.approval_status                                 approval_status\n" +
            "     , cdn.amount                                          amount\n" +
            "     , cdn.note_type                                       noteType\n" +
            "     , cdn.reason                                          reason\n" +
            "     , cdn.note                                            note\n" +
            "     , cdn.proposal_date                                   proposalDate\n" +
            "     , DATE_FORMAT(cdn.created_date, \"%M %Y\")            year_and_month\n" +
            "     , DATEDIFF(now(), cdn.created_date) as                dayDiff\n" +
            "     , d.distributor_name                                  distributorName\n" +
            "     , s.invoice_no                                        invoiceNo" +
            "     , au.name                                             createdBy\n" +
            "     , cdn.transaction_type                                transactionType\n" +
            "     , @var\\:=:approvalActor approvalActor\n" +
            "     , @var\\:=:level level\n" +
            "     , @var\\:=:approvalStepId approvalStepId\n" +
            "     , @var\\:=:multiLayerApprovalPathId multiLayerApprovalPathId\n" +
            "     , @var\\:=:approvalActorId approvalActorId\n" +
            "     , @var\\:=:approvalStatusName approvalStatus, \n" +
            "     , @var\\:=:approvalStepName approvalStepName \n" +
            " from multi_layer_approval_process mlap \n" +
            "         inner join credit_debit_note cdn on cdn.id = mlap.ref_id and mlap.level = :prevLevel and mlap.ref_table = 'CreditDebitNote' and mlap.is_next_layer_done = false\n" +
            "         inner join distributor d\n" +
            "                    on cdn.distributor_id = d.id\n" +
            "                        and cdn.company_id = :companyId\n" +
            "                        and cdn.approval_status = :approvalStatus\n" +
            "                        and cdn.is_active is true\n" +
            "                        and cdn.is_deleted is false and d.is_active is true\n" +
            "                        and d.is_deleted is false\n" +
            "         inner join application_user au\n" +
            "                   on cdn.created_by = au.id\n" +
            "         left join sales_invoice s\n" +
            "                   on cdn.invoice_id = s.id\n" +
            "                       and s.is_active is true\n" +
            "                       and s.is_deleted is false\n" +
            "where COALESCE(:distributorList) is null or cdn.distributor_id IN(:distributorList)\n" +
            "order by cdn.created_by desc",nativeQuery = true)
    List<Map<String, Object>> getPendingCreditDebitNoteListForAuthorizationAfterFirstLevel(@Param("companyId") Long companyId,
                                                        @Param("approvalStatus") String approvalStatus,
                                                        @Param("approvalActor") String approvalActor,
                                                        @Param("level") Integer level,
                                                        @Param("approvalStepId") Long approvalStepId,
                                                        @Param("multiLayerApprovalPathId") Long multiLayerApprovalPathId,
                                                        @Param("approvalActorId") Long approvalActorId,
                                                        @Param("distributorList") List<Long>distributorList,
                                                        @Param("approvalStatusName") String approvalStatusName,
                                                        @Param("prevLevel") Integer prevLevel,
                                                        @Param("approvalStepName") String approvalStepName);

    @Query(value = "select \n" +
            "clp.id as proposalId, \n" +
            "d.distributor_name as distributorName,\n" +
            "au.name as sales_officer, \n" +
            "des.name as designation,  \n" +
            "clp.proposed_amount as proposedAmount,\n" +
            "clp.business_plan businessPlan,\n" +
            "clp.current_limit currentLimit,\n" +
            "clp.approval_status,\n" +
            "DATE_FORMAT(clp.created_date, '%d/%m/%Y') as date,\n" +
            "DATE_FORMAT(clp.created_date, '%b %Y') as year_and_month,\n" +
            "DATE_FORMAT(clp.created_date, '%Y-%m-%d') as createdDate,\n" +
            "@var\\:=:approvalActor approvalActor,\n" +
            "@var\\:=:level level,\n" +
            "@var\\:=:approvalStepId approvalStepId,\n" +
            "@var\\:=:multiLayerApprovalPathId multiLayerApprovalPathId,\n" +
            "@var\\:=:approvalActorId approvalActorId,\n" +
            "@var\\:=:approvalStatusName approvalStatus, \n" +
            "@var\\:=:approvalStepName approvalStepName \n" +
            "from multi_layer_approval_process mlap \n" +
            "inner join credit_limit_proposal as clp on clp.id = mlap.ref_id and mlap.level = :prevLevel and mlap.ref_table = 'CreditLimitProposal' and mlap.is_next_layer_done = false\n" +
            "inner join distributor as d \n" +
            "\ton d.id = clp.distributor_id\n" +
            "\tand d.is_active is true \n" +
            "    and d.is_deleted is false\n" +
            "    and clp.company_id = :companyId\n" +
            "\tand clp.approval_status = :approvalStatus\n" +
            "    AND (COALESCE(:soList) is NULL OR clp.created_by IN (:soList))\n" +
            "    and clp.is_active is true \n" +
            "    and clp.is_deleted is false\n" +
            "inner join application_user as au \n" +
            "\ton au.id = clp.created_by\n" +
            "    and au.is_active is true \n" +
            "    and au.is_deleted is false\n" +
            "inner join designation as des \n" +
            "\ton des.id = au.designation_id\n" +
            "    and des.is_active is true \n" +
            "    and des.is_deleted is false\n" +
            "order by clp.created_date desc\n" , nativeQuery = true)
    List<Map<String, Object>> getPendingCreditLimitListForAuthorizationAfterFirstLevel(@Param("companyId") Long companyId,
                                                        @Param("soList") List<Long> soList,
                                                        @Param("approvalStatus") String approvalStatus,
                                                        @Param("approvalActor") String approvalActor,
                                                        @Param("level") Integer level,
                                                        @Param("approvalStepId") Long approvalStepId,
                                                        @Param("multiLayerApprovalPathId") Long multiLayerApprovalPathId,
                                                        @Param("approvalActorId") Long approvalActorId,
                                                        @Param("approvalStatusName") String approvalStatusName,
                                                        @Param("prevLevel") Integer prevLevel,
                                                        @Param("approvalStepName") String approvalStepName);

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
            "from multi_layer_approval_process mlap \n" +
            "inner join sales_return_proposal srp on srp.id = mlap.ref_id and mlap.level = :prevLevel and mlap.ref_table = 'SalesReturnProposal' and mlap.is_next_layer_done = false\n" +
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
    List<Map<String, Object>> getPendingSalesReturnListForAuthorizationAfterFirstLevel(@Param("companyId") Long companyId,
                                                        @Param("soList") List<Long> soList,
                                                        @Param("approvalStatus") String approvalStatus,
                                                        @Param("approvalActor") String approvalActor,
                                                        @Param("level") Integer level,
                                                        @Param("approvalStepId") Long approvalStepId,
                                                        @Param("multiLayerApprovalPathId") Long multiLayerApprovalPathId,
                                                        @Param("approvalActorId") Long approvalActorId,
                                                        @Param("approvalStatusName") String approvalStatusName,
                                                        @Param("prevLevel") Integer prevLevel,
                                                        @Param("approvalStepName") String approvalStepName);

    @Query(value = "select sb.id as salesBudgetId,\n" +
            "ay.fiscal_year_name as fiscalYearName, \n" +
            "sb.target_type as targetType,\n" +
            "round(SUM(sbd.quantity*sbd.product_trade_price),4) as salesBudget,\n" +
            "sb.budget_date as budgetDate,\n" +
            "DATE_FORMAT(sb.budget_date, \"%M %Y\") as year_and_month,\n"+
            "sb.approval_status as approval_status," +
            "@var\\:=:approvalActor approvalActor,\n" +
            "@var\\:=:level level,\n" +
            "@var\\:=:approvalStepId approvalStepId,\n" +
            "@var\\:=:multiLayerApprovalPathId multiLayerApprovalPathId,\n" +
            "@var\\:=:approvalActorId approvalActorId,\n" +
            "@var\\:=:approvalStatusName approvalStatus, \n" +
            "@var\\:=:approvalStepName approvalStepName \n" +
            "from multi_layer_approval_process mlap \n" +
            "inner join sales_budget as sb on sb.id = mlap.ref_id and mlap.level = :prevLevel and mlap.ref_table = 'SalesBudget' and mlap.is_next_layer_done = false\n" +
            "INNER JOIN sales_budget_details as sbd ON sb.id=sbd.sales_budget_id\n" +
            "INNER JOIN accounting_year as ay ON ay.id=sb.accounting_year_id\n" +
            "where approval_status = :approvalStatus and sb.company_id=:companyId\n" +
            "and sb.is_active is true and sb.is_deleted is false\n" +
            "and ay.is_active is true and ay.is_deleted is false\n" +
            "and sbd.is_active is true and sbd.is_deleted is false\n" +
            "group by sb.id ,ay.fiscal_year_name,sb.target_type,sb.approval_status ",nativeQuery = true)
    List<Map<String, Object>> getPendingSalesBudgetListForAuthorizationAfterFirstLevel(@Param("companyId") Long companyId,
                                                        @Param("approvalStatus") String approvalStatus,
                                                        @Param("approvalActor") String approvalActor,
                                                        @Param("level") Integer level,
                                                        @Param("approvalStepId") Long approvalStepId,
                                                        @Param("multiLayerApprovalPathId") Long multiLayerApprovalPathId,
                                                        @Param("approvalActorId") Long approvalActorId,
                                                        @Param("approvalStatusName") String approvalStatusName,
                                                        @Param("prevLevel") Integer prevLevel,
                                                        @Param("approvalStepName") String approvalStepName);

    @Query(value = "select cb.id as collectionBudgetId,ay.fiscal_year_name as fiscalYearName, \n" +
            "cb.target_type as targetType,SUM(cbd.collection_budget_amount) as collectionBudgetAmount,\n" +
            "cb.budget_date as budgetDate,\n" +
            "DATE_FORMAT(cb.budget_date, \"%M %Y\") as year_and_month,\n"+
            "cb.approval_status as approval_status, " +
            "@var\\:=:approvalActor approvalActor,\n" +
            "@var\\:=:level level,\n" +
            "@var\\:=:approvalStepId approvalStepId,\n" +
            "@var\\:=:multiLayerApprovalPathId multiLayerApprovalPathId,\n" +
            "@var\\:=:approvalActorId approvalActorId,\n" +
            "@var\\:=:approvalStatusName approvalStatus, \n" +
            "@var\\:=:approvalStepName approvalStepName \n" +
            "from multi_layer_approval_process mlap \n" +
            "INNER JOIN collection_budget as cb on cb.id = mlap.ref_id and mlap.level = :prevLevel and mlap.ref_table = 'CollectionBudget' and mlap.is_next_layer_done = false\n" +
            "INNER JOIN collection_budget_details as cbd ON cb.id=cbd.collection_budget_id\n" +
            "INNER JOIN accounting_year as ay ON ay.id=cb.accounting_year_id\n" +
            "where approval_status = :approvalStatus and cb.company_id=:companyId\n" +
            "group by cb.id,ay.fiscal_year_name,cb.target_type,cb.approval_status",nativeQuery = true)
    List<Map<String, Object>> getPendingCollectionBudgetListForAuthorizationAfterFirstLevel(@Param("companyId") Long companyId,
                                                        @Param("approvalStatus") String approvalStatus,
                                                        @Param("approvalActor") String approvalActor,
                                                        @Param("level") Integer level,
                                                        @Param("approvalStepId") Long approvalStepId,
                                                        @Param("multiLayerApprovalPathId") Long multiLayerApprovalPathId,
                                                        @Param("approvalActorId") Long approvalActorId,
                                                        @Param("approvalStatusName") String approvalStatusName,
                                                        @Param("prevLevel") Integer prevLevel,
                                                        @Param("approvalStepName") String approvalStepName);


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
            "from multi_layer_approval_process mlap \n" +
            "INNER JOIN sales_order so on so.id = mlap.ref_id and mlap.level = :prevLevel and mlap.ref_table = 'SalesOrder'  and mlap.is_next_layer_done = false\n" +
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
            "AND sb.is_active is true AND sb.is_deleted is false \n" +
            "GROUP BY sb.company_id, so.id order by so.order_date desc", nativeQuery = true)
    List<Map<String, Object>> getPendingSalesOrderListForAuthorizationAfterFirstLevel(@Param("companyId") Long companyId,
                                                        @Param("soList") List<Long> soList,
                                                        @Param("approvalStatus") String approvalStatus,
                                                        @Param("approvalActor") String approvalActor,
                                                        @Param("level") Integer level,
                                                        @Param("approvalStepId") Long approvalStepId,
                                                        @Param("multiLayerApprovalPathId") Long multiLayerApprovalPathId,
                                                        @Param("approvalActorId") Long approvalActorId,
                                                        @Param("approvalStatusName") String approvalStatusName,
                                                        @Param("prevLevel") Integer prevLevel,
                                                        @Param("approvalStepName") String approvalStepName);

    @Query(value = "select pc.id, pc.payment_no,\n" +
            "       pc.organization_id, pc.company_id,\n" +
            "       date_format(pc.payment_date, '%d-%b-%Y') as payment_date,\n" +
            "       date_format(pc.payment_date, '%b %Y') as year_and_month, \n" +
            "       datediff(now(), pc.payment_date) as days_ago,\n" +
            "       d.distributor_name,\n" +
            "       round(ifnull(t_lb.ledger_balance,0),2) as distributor_balance,\n" +
            "       cau.name as collected_by,\n" +
            "       des.name as designation,\n" +
            "       l.name as location_name,\n" +
            "       pc.collection_by_id,\n" +
            "       round(pc.collection_amount,2) as collection_amount,\n" +
            "       pc.payment_nature,\n" +
            "       pc.payment_type,\n" +
            "       pc.reference_no,\n" +
            "       pb.book_number,\n" +
            "       pc.money_receipt_no,\n" +
            "       bb.name as bank_branch_name,\n" +
            "       b.name as bank_name,\n" +
            "@var\\:=:approvalActor approvalActor,\n" +
            "@var\\:=:level level,\n" +
            "@var\\:=:approvalStepId approvalStepId,\n" +
            "@var\\:=:multiLayerApprovalPathId multiLayerApprovalPathId,\n" +
            "@var\\:=:approvalActorId approvalActorId, \n" +
            "@var\\:=:approvalStatusName approvalStatus, \n" +
            "@var\\:=:approvalStepName approvalStepName \n" +
            "from multi_layer_approval_process mlap \n" +
            "inner join  payment_collection pc on pc.id = mlap.ref_id and mlap.level = :prevLevel and mlap.ref_table = 'PaymentCollection'  and mlap.is_next_layer_done = false\n" +
            "inner join payment_book pb\n" +
            "        on pb.id = pc.payment_book_id\n" +
            " left join bank_branch bb\n" +
            "        on bb.id = pc.bank_branch_id\n" +
            " left join bank b\n" +
            "        on b.id = bb.bank_id\n" +
            "inner join distributor d\n" +
            "        on pc.distributor_id = d.id\n" +
            "       and pc.is_deleted is false\n" +
            "       and pc.company_id = :companyId\n" +
            "left join distributor_balance db " +
            "        on d.id = db.distributor_id\n" +
            "inner join application_user cau " +
            "        on pc.collection_by_id = cau.id\n" +
            "inner join designation des " +
            "        on cau.designation_id = des.id\n" +
            "inner join reporting_manager rm\n" +
            "        on cau.id = rm.application_user_id\n" +
            "       and rm.to_date is null\n" +
            "inner join location_manager_map lmm\n" +
            "        on rm.reporting_to_id = lmm.application_user_id\n" +
            "       and lmm.to_date is null\n" +
            "       and lmm.company_id = pc.company_id\n" +
            "inner join location l\n" +
            "        on lmm.location_id = l.id\n" +
            "inner join location_type lt\n" +
            "        on l.location_type_id = lt.id\n"+
            " left join (" +
            "    select lt.distributor_id distributor_id, sum(lt.debit - lt.credit) ledger_balance\n" +
            "    from ledger_transaction lt\n" +
            "    where lt.company_id = :companyId\n" +
            "    group by lt.company_id, lt.distributor_id" +
            ") as t_lb on d.id = t_lb.distributor_id\n"+
            "\n" +
            "where (:paymentNature = '' or :paymentNature is null or pc.payment_nature = :paymentNature)\n" +
            "  AND (COALESCE(:soList) is NULL OR pc.collection_by_id IN (:soList))\n" +
            "  and (:approvalStatus = '' or :approvalStatus is null or pc.approval_status_for_authorization = :approvalStatus)\n" +
            "order by pc.payment_date desc, pc.id desc;", nativeQuery = true)
    List<Map<String, Object>> getPendingPaymentCollectionListForAdvanceAfterFirstLevel(
            @Param("companyId") Long companyId,
            @Param("paymentNature") String paymentNature,
            @Param("soList") List<Long> soList,
            @Param("approvalStatus") String approvalStatus,
            @Param("approvalActor") String approvalActor,
            @Param("level") Integer level,
            @Param("approvalStepId") Long approvalStepId,
            @Param("multiLayerApprovalPathId") Long multiLayerApprovalPathId,
            @Param("approvalActorId") Long approvalActorId,
            @Param("approvalStatusName") String approvalStatusName,
            @Param("prevLevel") Integer prevLevel,
            @Param("approvalStepName") String approvalStepName
    );

    Optional<MultiLayerApprovalProcess> findByRefIdAndRefTableAndLevel(Long refId, String refTable, Integer level);
}
