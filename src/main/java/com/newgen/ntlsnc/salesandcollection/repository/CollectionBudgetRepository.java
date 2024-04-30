package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.salesandcollection.entity.CollectionBudget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Newaz Sharif
 * @since 27th July, 22
 */
@Repository
public interface CollectionBudgetRepository extends JpaRepository<CollectionBudget,Long> {

    @Query(value = "select pcb.distributorId, pcb.distributorName, \n" +
            "ifnull(round(sum(pcb.collectionBudget), 2), 0) as collectionBudget, \n" +
            "ifnull(round(sum(pcb.collectionAmount), 2), 0) as collectionAmount \n" +
            "from (select \n" +
            "dis.id as distributorId, dis.distributor_name as distributorName, cbd.month,\n" +
            "round(ifnull(pc.collection_amount,0), 4) as collectionAmount,\n" +
            "#round(sum(ifnull(pc.collection_amount,0)),4) as collectionAmount, \n" +
            "round(sum(cbd.collection_budget_amount), 4) as collectionBudget\n" +
            "from distributor dis \n" +
            "left join collection_budget_details cbd on cbd.distributor_id = dis.id\n" +
            "left join collection_budget cb on cb.id = cbd.collection_budget_id\n" +
            "and cb.accounting_year_id = :accountingYearId and cb.company_id = :companyId  \n" +
            "and (:month is null or cbd.month = :month) \n" +
            "and cb.approval_status = \"APPROVED\" and cb.is_active is true and cb.is_deleted is false \n" +
            "and (case when target_type = \"DISTRIBUTOR\" then (COALESCE(:distributorId, null) = '' OR dis.id IN (:distributorId))\n" +
            "      when target_type = \"SALES_OFFICER\" then (COALESCE(:salesOfficerId, null) = '' OR cbd.sales_officer_id IN (:salesOfficerId)) end)\n" +
            "left join distributor_sales_officer_map disSalesMap on \n" +
            "(case when target_type = \"DISTRIBUTOR\" then dis.id \n" +
            "      when target_type = \"SALES_OFFICER\" then cbd.sales_officer_id end) = \n" +
            "(case when target_type = \"DISTRIBUTOR\" then disSalesMap.distributor_id \n" +
            "      when target_type = \"SALES_OFFICER\" then disSalesMap.sales_officer_id end)\n" +
            "and disSalesMap.is_active is true and disSalesMap.is_deleted is false\n" +
            "left join reporting_manager rm on \n" +
            "(case when target_type = \"SALES_OFFICER\" then disSalesMap.sales_officer_id \n" +
            "      when target_type = \"DISTRIBUTOR\" then disSalesMap.sales_officer_id end) = \n" +
            "rm.application_user_id and rm.to_date is null and rm.is_active is true and rm.is_deleted is false\n" +
            "left join location_manager_map lmm on rm.reporting_to_id = lmm.application_user_id \n" +
            "and lmm.to_date is null and lmm.is_active is true and lmm.is_deleted is false\n" +
            "\n" +
            "left join \n" +
            "(select sum(ifnull(collection_amount, 0)) as collection_amount, month(payment_date) as month,\n" +
            "distributor_id, collection_by_id from payment_collection  " +
            "where approval_status=\"APPROVED\" \n" +
            "and is_active is true and is_deleted is false \n" +
            "and (:startDate is null or payment_date >= :startDate) \n" +
            "and (:endDate is null or payment_date <= :endDate) \n" +
            "group by distributor_id,collection_by_id,month(payment_date)) as pc on \n" +
            "dis.id = pc.distributor_id\n" +
            "#(case when target_type = \"DISTRIBUTOR\" then dis.id \n" +
            "#when target_type = \"SALES_OFFICER\" then disSalesMap.sales_officer_id end) = \n" +
            "#(case when target_type = \"DISTRIBUTOR\" then pc.distributor_id \n" +
            "#when target_type = \"SALES_OFFICER\" then pc.collection_by_id end) \n" +
            "#and cbd.month = pc.month\n" +
            "where (coalesce(:distributorId) is null or dis.id in (:distributorId))\n" +
            "and (:month is null or pc.month = :month) \n" +
            "group by dis.id, dis.distributor_name,\n" +
            "cbd.month, pc.collection_amount\n" +
            ") as pcb\n" +
            "group by pcb.distributorId, pcb.distributorName;", nativeQuery = true)
    List<Map<String,Object>> getDistributorWiseCollectionBudgetMonthly(
            @Param("accountingYearId") Long accountingYearId,
            @Param("month") Integer month,
            @Param("companyId") Long companyId,
            @Param("distributorId") List<Long> distributorId,
            @Param("salesOfficerId") List<Long> salesOfficerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);


    @Query(value = "select \n" +
            "(case when target_type = \"SALES_OFFICER\" then cbd.sales_officer_id \n" +
            "      when target_type = \"DISTRIBUTOR\" then disSalesMap.sales_officer_id end) as salesOfficerId, \n" +
            "au.name as salesOfficer, desg.name as salesOfficerDesignation, \n" +
            "mloc.name as salesOfficerLocation, round(sum(ifnull(pc.collection_amount,0)),4) as collectionAmount, \n" +
            "round(sum(cbd.collection_budget_amount),4) as collectionBudget  from collection_budget cb \n" +
            "inner join collection_budget_details cbd on cb.id = cbd.collection_budget_id \n" +
            "and cb.accounting_year_id = :accountingYearId and cb.company_id = :companyId  \n" +
            "and (:month is null or cbd.month = :month) \n" +
            "and cb.approval_status = \"APPROVED\" and cb.is_active is true and cb.is_deleted is false and \n" +
            "(case when target_type = \"DISTRIBUTOR\" then cbd.distributor_id IN (:distributorId)\n" +
            "      when target_type = \"SALES_OFFICER\" then cbd.sales_officer_id IN (:salesOfficerId) end)\n" +
            "left join distributor_sales_officer_map disSalesMap on \n" +
            "(case when target_type = \"DISTRIBUTOR\" then cbd.distributor_id end) =  \n" +
            "(case when target_type = \"DISTRIBUTOR\" then disSalesMap.distributor_id end) \n" +
            "and disSalesMap.is_active is true and disSalesMap.is_deleted is false\n" +
            "left join (select sum(collection_amount) as collection_amount, month(payment_date) as month,\n" +
            "distributor_id, collection_by_id from payment_collection  where approval_status=\"APPROVED\" \n" +
            "and is_active is true and is_deleted is false \n" +
            "group by distributor_id,collection_by_id,month(payment_date)) as pc on \n" +
            "(case when target_type = \"DISTRIBUTOR\" then cbd.distributor_id \n" +
            "      when target_type = \"SALES_OFFICER\" then cbd.sales_officer_id end) = \n" +
            "(case when target_type = \"DISTRIBUTOR\" then pc.distributor_id \n" +
            "      when target_type = \"SALES_OFFICER\" then pc.collection_by_id end) \n" +
            "and cbd.month = pc.month\n" +
            "left join reporting_manager rm on \n" +
            "(case when target_type = \"SALES_OFFICER\" then cbd.sales_officer_id \n" +
            "      when target_type = \"DISTRIBUTOR\" then disSalesMap.sales_officer_id end) = \n" +
            "rm.application_user_id and rm.to_date is null and rm.is_active is true and rm.is_deleted is false\n" +
            "left join location_manager_map lmm on rm.reporting_to_id = lmm.application_user_id \n" +
            "and lmm.to_date is null and lmm.company_id =:companyId and lmm.is_active is true and lmm.is_deleted is false\n" +
            "left join location mloc on lmm.location_id = mloc.id   \n" +
            "left join distributor dis on disSalesMap.distributor_id = dis.id\n" +
            "left join application_user au on \n" +
            "(case when target_type = \"SALES_OFFICER\" then cbd.sales_officer_id \n" +
            "      when target_type = \"DISTRIBUTOR\" then disSalesMap.sales_officer_id end) = au.id\n" +
            "left join designation desg on au.designation_id = desg.id and desg.is_active is true\n" +
            "group by (case when target_type = \"SALES_OFFICER\" then cbd.sales_officer_id \n" +
            "               when target_type = \"DISTRIBUTOR\" then disSalesMap.sales_officer_id end),\n" +
            " au.name,mloc.name,desg.name", nativeQuery = true)
    List<Map<String,Object>> getSalesOfficerWiseCollectionBudgetMonthly(
            @Param("accountingYearId") Long accountingYearId,
            @Param("month") Integer month,
            @Param("companyId") Long companyId,
            @Param("distributorId") List<Long> distributorId,
            @Param("salesOfficerId") List<Long> salesOfficerId);

    @Query(value = "select disSalesMap.sales_officer_id as salesOfficerId, \n" +
            "disSalesMap.distributor_id as distributorId,dis.distributor_name as distributorName,\n" +
            "au.name as salesOfficer, desg.name as salesOfficerDesignation, \n" +
            "mloc.name as salesOfficerLocation, round(sum(pc.collection_amount),4) as collectionAmount,\n" +
            "round(sum(cbd.collection_budget_amount),4) as collectionBudget  from collection_budget cb \n" +
            "inner join collection_budget_details cbd on cb.id = cbd.collection_budget_id \n" +
            "and cb.accounting_year_id = :accountingYearId and cb.company_id = :companyId \n" +
            "and (:month is null or cbd.month = :month) \n" +
            " and cb.approval_status = \"APPROVED\" and cb.is_active is true and cb.is_deleted is false and \n" +
            "(case when target_type = \"DISTRIBUTOR\" then cbd.distributor_id IN (:distributorId) \n" +
            "      when target_type = \"SALES_OFFICER\" then cbd.sales_officer_id IN (:salesOfficerId) end) \n" +
            "left join distributor_sales_officer_map disSalesMap on \n" +
            "(case when target_type = \"DISTRIBUTOR\" then cbd.distributor_id \n" +
            "      when target_type = \"SALES_OFFICER\" then cbd.sales_officer_id end) = \n" +
            "(case when target_type = \"DISTRIBUTOR\" then disSalesMap.distributor_id \n" +
            "      when target_type = \"SALES_OFFICER\" then disSalesMap.sales_officer_id end)  \n" +
            "and disSalesMap.is_active is true and disSalesMap.is_deleted is false\n" +
            "left join (select sum(collection_amount) as collection_amount, distributor_id,\n" +
            "collection_by_id from payment_collection where payment_date >= :startDate \n" +
            "and payment_date <= :endDate and approval_status=\"APPROVED\" and is_active is true \n" +
            "and is_deleted is false group by distributor_id,collection_by_id) as pc on \n" +
            "(case when target_type = \"DISTRIBUTOR\" then cbd.distributor_id \n" +
            "      when target_type = \"SALES_OFFICER\" then cbd.sales_officer_id end) = \n" +
            "(case when target_type = \"DISTRIBUTOR\" then pc.distributor_id \n" +
            "      when target_type = \"SALES_OFFICER\" then pc.collection_by_id end) \n" +
            "left join reporting_manager rm on \n" +
            "(case when target_type = \"SALES_OFFICER\" then disSalesMap.sales_officer_id \n" +
            "      when target_type = \"DISTRIBUTOR\" then disSalesMap.sales_officer_id end) = \n" +
            "rm.application_user_id and rm.to_date is null and rm.is_active is true and rm.is_deleted is false\n" +
            "left join location_manager_map lmm on rm.reporting_to_id = lmm.application_user_id \n" +
            "and lmm.to_date is null and lmm.is_active is true and lmm.is_deleted is false\n" +
            "left join location mloc on lmm.location_id = mloc.id   \n" +
            "left join distributor dis on disSalesMap.distributor_id = dis.id\n" +
            "left join application_user au on disSalesMap.sales_officer_id = au.id\n" +
            "left join designation desg on au.designation_id = desg.id and desg.is_active is true\n" +
            "group by disSalesMap.sales_officer_id, mloc.name, disSalesMap.distributor_id;", nativeQuery = true)
    List<Map<String,Object>> getDistributorOrSalesOfficerWiseCollectionBudgetYearly(
            @Param("accountingYearId") Long accountingYearId,
            @Param("month") Integer month,
            @Param("companyId") Long companyId,
            @Param("distributorId") List<Long> distributorId,
            @Param("salesOfficerId") List<Long> salesOfficerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query(value = "select prodct.id as Product_Id, concat(prodct.productName, \" \", prodct.description) as Product_Name, \n" +
            "            dis.id as Distributor_Id, dis.distributor_name as Distributor_Name,\n" +
            "            round(prodct.productTradePrice,2) as Product_Trade_price\n" +
            "            from (select  proddet.* from\n" +
            "                 (select prod.id, prod.name as productName, concat(prod.item_size, \n" +
            "            uom.abbreviation, \" * \", ps.pack_size) as description,ptp.trade_price as productTradePrice\n" +
            "            from product prod inner join product_trade_price ptp on prod.id = ptp.product_id \n" +
            "            and prod.is_active is true and prod.is_deleted is false and prod.company_id = :companyId and ptp.is_active is true \n" +
            "            and ptp.is_deleted is false and ptp.expiry_date is null\n" +
            "            inner join unit_of_measurement uom on prod.uom_id = uom.id\n" +
            "            inner join pack_size ps on prod.pack_size_id = ps.id) proddet) prodct \n" +
            "            cross join (select dist.id, dist.distributor_name from distributor dist \n" +
            "            inner join distributor_sales_officer_map dsom on dist.id = dsom.distributor_id \n" +
            "            and dsom.company_id = :companyId and dsom.is_active is true and dsom.is_deleted is false \n" +
            "            and dist.is_active is true and dist.is_deleted is false) dis;",nativeQuery = true)
    List<Map<String, Object>> getDistributorWiseCollectionBudgetExcelDownloadData(
            @Param("companyId") Long companyId);

    @Transactional
    long deleteByCompanyIdAndAccountingYearId(Long companyId, Long accountingYearId);

    Optional<CollectionBudget> findByCompanyIdAndAccountingYearIdAndIsDeletedFalseAndIsActiveTrue(
            Long companyId, Long accountingYearId);

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
            "from collection_budget as cb\n" +
            "INNER JOIN collection_budget_details as cbd ON cb.id=cbd.collection_budget_id\n" +
            "INNER JOIN accounting_year as ay ON ay.id=cb.accounting_year_id\n" +
            "where approval_status = :approvalStatus and cb.company_id=:companyId\n" +
            "group by cb.id,ay.fiscal_year_name,cb.target_type,cb.approval_status",nativeQuery = true)
    List<Map<String, Object>> getPendingListForApproval(@Param("companyId") Long companyId,
                                                        @Param("approvalStatus") String approvalStatus,
                                                        @Param("approvalActor") String approvalActor,
                                                        @Param("level") Integer level,
                                                        @Param("approvalStepId") Long approvalStepId,
                                                        @Param("multiLayerApprovalPathId") Long multiLayerApprovalPathId,
                                                        @Param("approvalActorId") Long approvalActorId,
                                                        @Param("approvalStatusName") String approvalStatusName,
                                                        @Param("approvalStepName") String approvalStepName);

    @Query(value = "#get salesofficer collection budget month wise \n" +
            "select d_budget.company_id,\n" +
            "sum(ifnull(d_budget.budgetQuantity, 0)) budgetQuantity,\n" +
            "sum(ifnull((d_budget.budgetAmount), 0)) totalBudget \n" +
            "from collection_distributor_budget d_budget\n" +
            "where d_budget.company_id = :companyId\n" +
            "and d_budget.accounting_year_id = :accountingYearId\n" +
            "and (coalesce(:monthList) is null or d_budget.month in (:monthList))\n" +
            "and (coalesce(:distributorIds) is null or d_budget.distributor_id in (:distributorIds))\n" +
            "group by d_budget.id\n" +
            "", nativeQuery = true)
    Map<String, Object> findCollectionBudgetSalesOfficer(
            @Param("companyId") Long companyId,
            @Param("distributorIds") List<Long> distributorIds,
            @Param("accountingYearId") Long accountingYearId,
            @Param("monthList") List<Integer> monthList);

    @Query(value = "#get salesofficer collection target\n" +
            "select\n" +
            "ifnull(round(SUM(invoice_info.cashTarget\n" +
            "+ invoice_info.creditTarget), 0), 0) collectionTarget\n" +
            "from\n" +
            "(select sinv.company_id, sinv.id, sinv.invoice_nature_id,\n" +
            "ifnull(sinv.invoice_amount, 0) invoice_amount, invoice_date, \n" +
            "datediff(:asOnDate, sinv.invoice_date) overdueDay,\n" +
            "if(sinv.invoice_nature_id=2 && datediff(:asOnDate, sinv.invoice_date) > inod.not_due_days, ifnull(sinv.invoice_amount, 0), 0) cashTarget,\n" +
            "if(sinv.invoice_nature_id=1 && datediff(:asOnDate, sinv.invoice_date) > inod.not_due_days, ifnull(sinv.invoice_amount, 0), 0) creditTarget\n" +
            "from sales_invoice sinv\n" +
            "inner join invoice_nature invn on sinv.invoice_nature_id = invn.id\n" +
            "and sinv.company_id =:companyId\n" +
            "and invn.is_active is true and invn.is_deleted is false\n" +
            "inner join invoice_overdue inod on inod.invoice_nature_id = invn.id\n" +
            "and inod.is_active is true and inod.is_deleted is false\n" +
            "and inod.company_id = sinv.company_id\n" +
            "inner join distributor_sales_officer_map dis_so\n" +
            "on sinv.distributor_id = dis_so.distributor_id\n" +
            "and dis_so.to_date is null\n" +
            "and dis_so.company_id = sinv.company_id\n" +
            "and dis_so.is_deleted is false\n" +
            "inner join distributor dis\n" +
            "on sinv.distributor_id = dis.id and dis.is_deleted is false\n" +
            "inner join application_user au on dis_so.sales_officer_id = au.id\n" +
            "inner join reporting_manager rm\n" +
            "on dis_so.sales_officer_id = rm.application_user_id\n" +
            "and rm.to_date is null\n" +
            "inner join location_manager_map lmm\n" +
            "on rm.reporting_to_id = lmm.application_user_id\n" +
            "and lmm.to_date is null and lmm.company_id = sinv.company_id\n" +
            "inner join location lo on lmm.location_id = lo.id\n" +
            "where sinv.is_active is true and sinv.is_deleted is false\n" +
            "and (:startDate is null or sinv.invoice_date >= :startDate) \n" +
            "and (:endDate is null or sinv.invoice_date <= :endDate) \n" +
            "and (coalesce(:monthList) is null or month(sinv.invoice_date) in (:monthList))\n" +
            "and (coalesce(:distributorIds) is null or sinv.distributor_id in (:distributorIds))\n" +
            ") invoice_info\n"+
            "", nativeQuery = true)
    Double getCollectionTarget(
            @Param("companyId") Long companyId,
            @Param("distributorIds") List<Long> distributorIds,
            @Param("monthList") List<Integer> monthList,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("asOnDate") LocalDate asOnDate);
}
