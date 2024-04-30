package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.salesandcollection.entity.SalesBudget;
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
 * @author anika
 * @Date ১৩/৪/২২
 */
@Repository
public interface SalesBudgetRepository extends JpaRepository<SalesBudget, Long> {

    List<SalesBudget> findAllByOrganizationAndIsDeletedFalse(Organization organization);
    Optional<SalesBudget> findByCompanyIdAndAccountingYearIdAndIsDeletedFalseAndIsActiveTrue(
            Long companyId, Long accountingYearId);
    Optional<SalesBudget> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);
    @Transactional
    long deleteByCompanyIdAndAccountingYearId(Long companyId, Long accountingYearId);

    @Query(value = "select prod.id, prod.product_sku as productSku, prod.name as productName," +
            "pc.name as productCategory, sum(sbd.quantity) as budgetQuantity, sbd.product_trade_price as tradePrice, " +
            "round(sum(sbd.quantity*sbd.product_trade_price),4) as salesBudget from sales_budget sb \n" +
            "inner join sales_budget_details sbd on sb.id = sbd.sales_budget_id " +
            "and sb.accounting_year_id = :accountingYearId and sb.company_id = :companyId and sb.approval_status = \"APPROVED\" \n" +
            "and (:month is null or sbd.month = :month) and \n" +
            "(case when target_type = \"DISTRIBUTOR\" " +
            "           then (COALESCE(:distributorId, NULL) = '' OR sbd.distributor_id IN (:distributorId)) \n" +
            "      when target_type = \"SALES_OFFICER\" " +
            "           then (COALESCE(:salesOfficerId, NULL) = '' OR sbd.sales_officer_id IN (:salesOfficerId)) \n" +
            "      else (COALESCE(:locationId, NULL) = '' OR sbd.location_id IN (:locationId)) \n" +
            "end )    \n" +
            "inner join product prod on sbd.product_id = prod.id\n" +
            "inner join product_category pc on prod.product_category_id = pc.id " +
            "and (:productCategoryId is null or pc.id= :productCategoryId)  \n" +
            "group by prod.id,prod.product_sku, pc.name, prod.name,sbd.product_trade_price;", nativeQuery = true)
    List<Map<String, Object>> getProductWiseSalesBudget(
            @Param("productCategoryId") Long productCategoryId,
            @Param("accountingYearId") Long accountingYearId,
            @Param("month") Integer month,
            @Param("companyId") Long companyId,
            @Param("distributorId") List<Long> distributorId,
            @Param("salesOfficerId") List<Long> salesOfficerId,
            @Param("locationId") List<Long> locationId);

    @Query(value = "select loc.id as locationId, loc.name as locationName, \n" +
            "disSalesMap.distributor_id as distributorId,\n" +
            "dis.distributor_name as distributorName, \n" +
            "round(sum(sbd.quantity*sbd.product_trade_price),4) as salesBudget \n" +
            "from sales_budget sb \n" +
            "inner join sales_budget_details sbd on sb.id = sbd.sales_budget_id and " +
            "sb.accounting_year_id = :accountingYearId and sb.company_id = :companyId and sb.approval_status = \"APPROVED\" " +
            "and (:month is null or sbd.month = :month) \n" +
            "and sb.is_active is true and sb.is_deleted is false and \n" +
            "(case when target_type = \"DISTRIBUTOR\" \n" +
            "           then sbd.distributor_id IN (:distributorId)\n" +
            "     when target_type = \"SALES_OFFICER\" \n" +
            "           then sbd.sales_officer_id IN (:salesOfficerId)\n" +
            "     else (COALESCE(:locationId) is null OR sbd.location_id IN (:locationId))\n" +
            "end )    \n" +
            "left join distributor_sales_officer_map disSalesMap on " +
            "(case when target_type = \"DISTRIBUTOR\" then sbd.distributor_id \n" +
            "      when target_type = \"SALES_OFFICER\" then sbd.sales_officer_id \n" +
            "end ) = (case when target_type = \"DISTRIBUTOR\" then disSalesMap.distributor_id \n" +
            "              when target_type = \"SALES_OFFICER\" then disSalesMap.sales_officer_id \n" +
            "end ) and disSalesMap.is_active is true and disSalesMap.is_deleted is false\n" +
            "left join location loc on (case when target_type = \"LOCATION\" then sbd.location_id end) = loc.id \n" +
            "left join reporting_manager rm on \n" +
            "(case when target_type = \"SALES_OFFICER\" then disSalesMap.sales_officer_id \n" +
            "      when target_type = \"DISTRIBUTOR\" then disSalesMap.sales_officer_id end) \n" +
            " = rm.application_user_id and rm.to_date is null and rm.is_active is true \n" +
            "and rm.is_deleted is false \n" +
            "left join distributor dis on disSalesMap.distributor_id = dis.id \n" +
            "inner join product prod on sbd.product_id = prod.id \n" +
            "group by disSalesMap.distributor_id,loc.id;", nativeQuery = true)
    List<Map<String, Object>> getDistributorOrLocationWiseBudget(
            @Param("accountingYearId") Long accountingYearId,
            @Param("month") Integer month,
            @Param("companyId") Long companyId,
            @Param("distributorId") List<Long> distributorId,
            @Param("salesOfficerId") List<Long> salesOfficerId,
            @Param("locationId") List<Long> locationId);


    @Query(value = "select loc.id as locationId, loc.name as locationName, \n" +
            "(case when target_type = \"SALES_OFFICER\" then sbd.sales_officer_id \n" +
            "      when target_type = \"DISTRIBUTOR\" then disSalesMap.sales_officer_id end) as salesOfficerId, \n" +
            "au.name as salesOfficer, \n" +
            "desg.name as salesOfficerDesignation, mloc.name as salesOfficerLocation, \n" +
            "round(sum(sbd.quantity*sbd.product_trade_price),4) as salesBudget \n" +
            "from sales_budget sb \n" +
            "inner join sales_budget_details sbd on sb.id = sbd.sales_budget_id and " +
            "sb.accounting_year_id = :accountingYearId and sb.company_id = :companyId and sb.approval_status = \"APPROVED\" " +
            "and (:month is null or sbd.month = :month) \n" +
            "and sb.is_active is true and sb.is_deleted is false and \n" +
            "(case when target_type = \"DISTRIBUTOR\" \n" +
            "           then sbd.distributor_id IN (:distributorId)\n" +
            "     when target_type = \"SALES_OFFICER\" \n" +
            "           then sbd.sales_officer_id IN (:salesOfficerId)\n" +
            "     else (COALESCE(:locationId) is null OR sbd.location_id IN (:locationId))\n" +
            "end )    \n" +
            "left join distributor_sales_officer_map disSalesMap on " +
            "(case when target_type = \"DISTRIBUTOR\" then sbd.distributor_id end) =\n" +
            "(case when target_type = \"DISTRIBUTOR\" then disSalesMap.distributor_id end) \n" +
            "and disSalesMap.is_active is true and disSalesMap.is_deleted is false\n" +
            "left join location loc on (case when target_type = \"LOCATION\" then sbd.location_id end) = loc.id \n" +
            "left join reporting_manager rm on \n" +
            "(case when target_type = \"SALES_OFFICER\" then sbd.sales_officer_id \n" +
            "      when target_type = \"DISTRIBUTOR\" then disSalesMap.sales_officer_id end) \n" +
            " = rm.application_user_id and rm.to_date is null and rm.is_active is true \n" +
            "and rm.is_deleted is false \n" +
            "left join location_manager_map lmm on rm.reporting_to_id = lmm.application_user_id \n" +
            "and lmm.to_date is null and lmm.is_active is true and lmm.is_deleted is false \n" +
            "left join location mloc on lmm.location_id = mloc.id   \n"+
            "left join application_user au on \n" +
            "(case when target_type = \"SALES_OFFICER\" then sbd.sales_officer_id \n" +
            "      when target_type = \"DISTRIBUTOR\" then disSalesMap.sales_officer_id end) = au.id\n" +
            "left join designation desg on au.designation_id = desg.id and desg.is_active is true\n" +
            "inner join product prod on sbd.product_id = prod.id \n" +
            "group by (case when target_type = \"SALES_OFFICER\" then sbd.sales_officer_id \n" +
            "               when target_type = \"DISTRIBUTOR\" then disSalesMap.sales_officer_id end),\n" +
            "au.name, desg.name, mloc.name,loc.id;", nativeQuery = true)
    List<Map<String, Object>> getSalesOfficerOrLocationWiseBudget(
            @Param("accountingYearId") Long accountingYearId,
            @Param("month") Integer month,
            @Param("companyId") Long companyId,
            @Param("distributorId") List<Long> distributorId,
            @Param("salesOfficerId") List<Long> salesOfficerId,
            @Param("locationId") List<Long> locationId);

    @Query(value = "select round(sum(sbd.quantity*sbd.product_trade_price),4) as salesBudget  from sales_budget sb \n" +
            "inner join sales_budget_details sbd on sbd.sales_budget_id = sb.id \n" +
            "and sb.company_id = :companyId\n" +
            "and sb.approval_status = :approvalStatus\n" +
            "and (:accountingYearId is null or sb.accounting_year_id = :accountingYearId) \n" +
            "and sb.is_active = true and sb.is_deleted = false\n" +
            "and sbd.is_active = true and sbd.is_deleted = false\n" +
            "and sbd.month IN (:monthList)\n" +
            "group by sb.accounting_year_id", nativeQuery = true)
    Double getTotalSalesTarget(@Param("accountingYearId") Long accountingYearId,
                               @Param("companyId") Long companyId,
                               @Param("monthList") List<Integer> monthList,
                               @Param("approvalStatus") String approvalStatus);

    @Query(value = "select prodct.id as Product_Id, concat(prodct.productName, \"  \" ,prodct.description) as Product_Name, \n" +
            "dis.id as Distributor_Id, dis.distributor_name as Distributor_Name,\n" +
            "prodct.manufacturingCost as Manufacturing_Cost , round(prodct.productTradePrice,2) as Product_Trade_price\n" +
            "from (select  proddet.*, prodmanfac.manufacturingCost from\n" +
            "     (select prod.id, prod.name as productName, concat(prod.item_size, \n" +
            "           uom.abbreviation) as description,ptp.trade_price as productTradePrice \n" +
            "               from product prod inner join product_trade_price ptp on prod.id = ptp.product_id \n" +
            "and prod.is_active is true and prod.is_deleted is false and prod.company_id=:companyId and ptp.is_active is true \n" +
            "and ptp.is_deleted is false and ptp.expiry_date is null\n" +
            "inner join unit_of_measurement uom on prod.uom_id = uom.id) proddet\n" +
            "                               left join \n" +
            "(select trans1.productId, trans1.manufacturingCost from\n" +
            "    (select invdet.id,invdet.product_id as productId,invdet.rate as manufacturingCost \n" +
            "from inv_transaction invt inner join inv_transaction_details invdet \n" +
            "on invt.id = invdet.inv_transaction_id and invt.transaction_type=\"RECEIVE\" \n" +
            "and invt.is_active is true and invt.is_deleted is false and invt.company_id=:companyId \n" +
            "and invt.transaction_date >=:startDate and invt.transaction_date <=:endDate \n"+
            "and invdet.is_active is true and invdet.is_deleted is false \n" +
            "and invdet.from_store_id is null order by transaction_date desc) trans1 \n" +
            "left join (select invdet.id,invdet.product_id as productId,invdet.rate as manufacturingCost \n" +
            "from inv_transaction invt inner join inv_transaction_details invdet \n" +
            "on invt.id = invdet.inv_transaction_id and invt.transaction_type=\"RECEIVE\" \n" +
            "and invt.is_active is true and invt.is_deleted is false and invt.company_id=:companyId \n" +
            "and invt.transaction_date >=:startDate and invt.transaction_date <=:endDate \n"+
            "and invdet.is_active is true and invdet.is_deleted is false and invdet.from_store_id is null\n" +
            "order by transaction_date desc) trans2 on trans1.productId = trans2.productId \n" +
            "and trans1.id < trans2.id where trans2.id is null) prodmanfac\n" +
            "on proddet.id = prodmanfac.productId) prodct \n" +
            "cross join (select dist.id, dist.distributor_name from distributor dist \n" +
            "inner join distributor_sales_officer_map dsom on dist.id = dsom.distributor_id \n" +
            "and dsom.company_id=:companyId and dsom.is_active is true and dsom.is_deleted is false \n" +
            "and dist.is_active is true and dist.is_deleted is false) dis; \n", nativeQuery = true)
    List<Map<String, Object>> getDistributorWiseSalesBudgetExcelDownloadData(
            @Param("companyId") Long companyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);


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
            " from sales_budget as sb\n" +
            "INNER JOIN sales_budget_details as sbd ON sb.id=sbd.sales_budget_id\n" +
            "INNER JOIN accounting_year as ay ON ay.id=sb.accounting_year_id\n" +
            "where approval_status = :approvalStatus and sb.company_id=:companyId\n" +
            "and sb.is_active is true and sb.is_deleted is false\n" +
            "and ay.is_active is true and ay.is_deleted is false\n" +
            "and sbd.is_active is true and sbd.is_deleted is false\n" +
            "group by sb.id ,ay.fiscal_year_name,sb.target_type,sb.approval_status ",nativeQuery = true)
    List<Map<String, Object>> getPendingListForApproval(@Param("companyId") Long companyId,
                                                        @Param("approvalStatus") String approvalStatus,
                                                        @Param("approvalActor") String approvalActor,
                                                        @Param("level") Integer level,
                                                        @Param("approvalStepId") Long approvalStepId,
                                                        @Param("multiLayerApprovalPathId") Long multiLayerApprovalPathId,
                                                        @Param("approvalActorId") Long approvalActorId,
                                                        @Param("approvalStatusName") String approvalStatusName,
                                                        @Param("approvalStepName") String approvalStepName);


    @Query(value = "CALL SNC_CHILD_LOCATION_HIERARCHY();", nativeQuery = true)
    List<Map<String, Object>> childLocationHierarchyProcCall();
}
