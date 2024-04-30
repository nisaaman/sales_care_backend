package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.common.SuperEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author Sunipa
 * @since 27th Sep,23
 */
public interface SalesOverviewRepository extends JpaRepository<SuperEntity, Long> {

    @Query(value = "#get salesofficer sales budget month wise \n" +
            "select d_budget.company_id,\n" +
            "sum(ifnull(d_budget.budgetQuantity, 0)) budgetQuantity,\n" +
            "round(sum(ifnull((d_budget.budgetAmount), 0)), 4) totalBudget \n" +
            "from distributor_budget d_budget\n" +
            "where d_budget.company_id = :companyId\n" +
            "and d_budget.accounting_year_id = :accountingYearId\n" +
            "and (coalesce(:monthList) is null or d_budget.month in (:monthList))\n" +
            "and (coalesce(:distributorIds) is null or d_budget.distributor_id in (:distributorIds))\n" +
            "group by d_budget.id\n" +
            "", nativeQuery = true)
    Map<String, Object> findSalesBudgetSalesOfficer(
            @Param("companyId") Long companyId,
            @Param("distributorIds") List<Long> distributorIds,
            @Param("accountingYearId") Long accountingYearId,
            @Param("monthList") List<Integer> monthList);

    @Query(value = "#get sales officer sales data summary\n" +
            "select\n" +
            "round((sum(ifnull(si.invoice_amount,0))\n" +
            "+ sum(ifnull(db_amount,0)) - sum(ifnull(return_amount,0))\n" +
            "+ sum(ifnull(cr_amount,0))),4) as tolalSales\n" +
            "from distributor dis\n" +
            "inner join distributor_sales_officer_map dsom\n" +
            "on dis.id = dsom.distributor_id \n" +
            "and dsom.company_id = :companyId\n" +
            "and (coalesce(:salesOfficerIds) is null or dsom.sales_officer_id in(:salesOfficerIds))\n" +
            "and dsom.is_active is true and dsom.is_deleted is false \n" +
            "inner join sales_invoice si on dis.id = si.distributor_id \n" +
            "and si.company_id = dsom.company_id\n" +
            "#NGLSC-2261 and si.is_accepted is true \n" +
            "and si.is_active is true and si.is_deleted is false \n" +
            "and (:fromDate is null or si.invoice_date >= :fromDate)\n" +
            "and si.invoice_date <= :toDate\n" +
            "and dis.is_active is true\n" +
            "and dis.is_deleted is false\n" +
            "left join\n" +
            "(select srp.company_id, srp.distributor_id, srp.sales_invoice_id,\n" +
            "(sum(ifnull((invtdet.quantity * ptp.trade_price),0))) return_amount\n" +
            "from sales_return_proposal srp\n" +
            "left join sales_return sr on srp.id = sr.sales_return_proposal_id\n" +
            "left join inv_transaction_details invtdet\n" +
            "on sr.inv_transaction_id = invtdet.inv_transaction_id\n" +
            "left join product_trade_price ptp\n" +
            "on invtdet.product_id = ptp.product_id\n" +
            "and ptp.expiry_date is null\n" +
            "where srp.approval_status='APPROVED' \n" +
            "and (:fromDate is null or sr.return_date >= :fromDate)\n" +
            "and (:toDate is null or sr.return_date <= :toDate)\n" +
            "group by srp.company_id, srp.distributor_id, srp.sales_invoice_id\n" +
            ") return_data\n" +
            "on dsom.company_id = return_data.company_id\n" +
            "and dis.id = return_data.distributor_id\n" +
            "and si.id = return_data.sales_invoice_id\n" +
            "\n" +
            "left join (select cn.company_id, cn.distributor_id, cn.invoice_id,\n" +
            "sum(cn.amount) cr_amount  from credit_debit_note cn\n" +
            "where cn.approval_status='APPROVED' and cn.note_type='CREDIT'\n" +
            "and (:fromDate is null or cn.proposal_date >= :fromDate)\n" +
            "and (:toDate is null or cn.proposal_date <= :toDate)\n" +
            "group by cn.company_id, cn.distributor_id, cn.invoice_id\n" +
            ") cr on dis.id = cr.distributor_id\n" +
            "and si.id = cr.invoice_id\n" +
            "and cr.company_id = dsom.company_id\n" +
            "\n" +
            "left join\n" +
            "(select dn.distributor_id, dn.invoice_id, sum(dn.amount) db_amount\n" +
            "from credit_debit_note dn\n" +
            "where dn.approval_status='APPROVED' and dn.note_type='DEBIT'\n" +
            "and (:fromDate is null or dn.proposal_date >= :fromDate)\n" +
            "and (:toDate is null or dn.proposal_date <= :toDate)\n" +
            "and dn.company_id = :companyId\n" +
            "group by dn.distributor_id, dn.invoice_id) db\n" +
            "on dis.id = db.distributor_id\n" +
            "and si.id = db.invoice_id\n" +
            "group by dsom.company_id\n", nativeQuery = true)
    Double getSalesSummarySalesOfficer(
            @Param("salesOfficerIds") List<Long> salesOfficerIds,
            @Param("companyId") Long companyId, @Param("fromDate")
            LocalDate fromDate, @Param("toDate") LocalDate toDate); // asOnDate = toDate


    @Query(value = "#get sales officer sales data summary\n" +
            "select sb.sales_officer_id,\n" +
            "sum(sbd.free_quantity) as freeQuantity,\n" +
            "round(sum(invtd.quantity * ptp.trade_price),4) as sale_amount,\n" +
            "sum(ifnull(case when calculation_type = 'PERCENTAGE'\n" +
            "then ((ptp.trade_price * td.discount_value)/100)\n" +
            "else td.discount_value end,0)) as tradeDiscount from sales_booking sb\n" +
            "inner join sales_booking_details sbd on sb.id = sbd. sales_booking_id\n" +
            "and sb.company_id = :companyId\n" +
            "and sb.sales_officer_id in(:salesOfficerUserLoginId)\n" +
            "and sb.booking_date >= :startDate and sb.booking_date <= :endDate\n" +
            "inner join sales_order_details sod on sbd.id = sod.sales_booking_details_id\n" +
            "inner join sales_order so on sod.sales_order_id = so.id\n" +
            "inner join inv_transaction_details invtd on sod.id = invtd.sales_order_details_id\n" +
            "inner join inv_delivery_challan idc on invtd.inv_transaction_id = idc.inv_transaction_id\n" +
            "inner join sales_invoice_challan_map sicm on idc.id = sicm.inv_delivery_challan_id\n" +
            "inner join sales_invoice si on sicm.sales_invoice_id = si.id\n" +
            "inner join product_trade_price ptp on sbd.product_trade_price_id = ptp.id\n" +
            "left join trade_discount td on invtd.product_id = td.product_id\n" +
            "and sb.semester_id = td.semester_id\n" +
            "and td.product_id = ptp.product_id and td.approval_status='APPROVED'\n" +
            "and si.is_accepted=true\n" +
            "group by sb.sales_officer_id;", nativeQuery = true)
    Map<String, Object> getSalesSummarySalesOfficerMonthly(
            @Param("salesOfficerUserLoginId") List<Long> salesOfficerUserLoginId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("companyId") Long companyId);

}


