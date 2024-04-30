package com.newgen.ntlsnc.supplychainmanagement.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.supplychainmanagement.entity.SalesReturn;
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
 * @author marziah
 * @Date 25/04/22
 */

@Repository
public interface SalesReturnRepository extends JpaRepository<SalesReturn, Long> {

    @Query(value = "select sb.booking_no as bookingNo, dis.distributor_name as distributorName, \n" +
            "sb.booking_date as bookingDate, au.name as salesOfficer, srpd.quantity, \n" +
            "srpd.quantity * ptp.trade_price as salesReturnAmount from sales_return sr \n" +
            "inner join sales_return_proposal srp on sr.sales_return_proposal_id = srp.id \n" +
            "and srp.approval_status = 'APPROVED' \n" +
            "inner join sales_return_proposal_details srpd on srp.id \n" +
            "inner join sales_invoice si on srp.sales_invoice_id = si.id \n" +
            "inner join sales_invoice_challan_map sicm on si.id = sicm.sales_invoice_id \n" +
            "inner join inv_delivery_challan idc on sicm.inv_delivery_challan_id = idc.id \n" +
            "inner join inv_transaction_details invtd on idc.inv_transaction_id = invtd.inv_transaction_id \n" +
            "inner join sales_order_details sod on invtd.sales_order_details_id = sod.id \n" +
            "inner join sales_booking_details sbd on sod.sales_booking_details_id = sbd.id \n" +
            "inner join sales_booking sb on sbd.sales_booking_id = sb.id and sb.company_id = :companyId \n" +
            "and sb.sales_officer_id in(:salesOfficerUserLoginId) \n" +
            "and sb.booking_date >= :startDate " +
            "and sb.booking_date <= :endDate \n" +
            "inner join distributor dis on sb.distributor_id = dis.id \n" +
            "inner join application_user au on sb.sales_officer_id = au.id \n" +
            "inner join product_trade_price ptp on sbd.product_trade_price_id = ptp.id \n" +
            "and expiry_date is null;", nativeQuery = true)
    public List<Map<String, Object>> getSalesReturnOverView(
                        @Param("salesOfficerUserLoginId") List<Long> salesOfficerUserLoginId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        @Param("companyId") Long companyId);

    List<SalesReturn> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<SalesReturn> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);
    Optional<SalesReturn> findBySalesReturnProposalIdAndIsActiveTrueAndIsDeletedFalse(Long id);
    Optional<SalesReturn> findById(Long id);

    @Query(value = "select sr.id                                                                             sales_return_id\n" +
            "     , sr.return_date                                                                    return_date\n" +
            "     ,  DATE_FORMAT(sr.return_date,'%b %Y')                                                                  return_date_formated\n" +
            "     , srp.id                                                                            sales_return_proposal_id\n" +
            "     , srp.proposal_no                                                                   proposal_no\n" +
            "     , srp.approval_status                                                               approval_status\n" +
            "     , d.distributor_name                                                                distributor_name\n" +
            "      ,d2.depot_name depot_name\n" +
            "     , sum(case\n" +
            "               when (t.calculation_type is not null and t.calculation_type = '%')\n" +
            "                   then (ptp.trade_price - (ptp.trade_price * t.discount_value / 100)) * srpd.quantity\n" +
            "               else (ptp.trade_price - IFNULL(t.discount_value, 0)) * srpd.quantity end) return_quantity_price\n" +
            "from sales_return sr\n" +
            "         inner join sales_return_proposal srp\n" +
            "                    on sr.sales_return_proposal_id = srp.id and srp.company_id = :companyId AND srp.sales_officer_id = :salesOfficerId  and\n" +
            "                       sr.is_active is true and sr.is_deleted is false and srp.is_active is true and\n" +
            "                       srp.is_deleted is false\n" +
            "         inner join depot d2 on sr.depot_id = d2.id \n" +
            "         inner join sales_return_proposal_details srpd\n" +
            "                    on srp.id = srpd.sales_return_proposal_id and srpd.is_active is true and srpd.is_deleted is false\n" +
            "         inner join distributor d on srp.distributor_id = d.id and d.is_active is true and d.is_deleted is false\n" +
            "         inner join inv_transaction_details itd\n" +
            "                    on itd.inv_transaction_id = sr.inv_transaction_id and itd.is_active is true and\n" +
            "                       itd.is_deleted is false\n" +
            "         inner join sales_order_details sod\n" +
            "                    on itd.sales_order_details_id = sod.id and sod.is_active is true and sod.is_deleted is false\n" +
            "         inner join sales_booking_details sbd\n" +
            "                    on sod.sales_booking_details_id = sbd.id and sbd.is_active is true and sbd.is_deleted is false\n" +
            "         inner join product_trade_price ptp\n" +
            "                    on ptp.id = sbd.product_trade_price_id and ptp.is_active is true and ptp.is_deleted is false\n" +
            "         left join trade_discount t on t.id = sbd.trade_discount_id and t.is_active is true and t.is_deleted is false\n" +
            "group by d.id, sr.id, sr.return_date\n" +
            "order by sr.return_date desc", nativeQuery = true)
    public List<Map> getDistributorWiseSalesReturnListByCompanyAndSalesPerson(@Param("companyId") Long companyId, @Param("salesOfficerId") Long salesOfficerId);

    @Query(value = "select srp.sales_invoice_id, sum(td.quantity) sales_quantity,\n" +
            "            sum(case when srpd.product_id is not null then srpd.quantity else 0 end ) return_quantity\n" +
            "            from inv_transaction_details td\n" +
            "            left join inv_transaction t\n" +
            "            on td.inv_transaction_id = t.id\n" +
            "            left join inv_delivery_challan ch\n" +
            "            on ch.inv_transaction_id = t.id\n" +
            "            left join sales_invoice_challan_map chmap\n" +
            "            on chmap.inv_delivery_challan_id = ch.id\n" +
            "            left join sales_return_proposal srp\n" +
            "            on chmap.sales_invoice_id = srp.sales_invoice_id\n" +
            "            left join sales_return_proposal_details srpd\n" +
            "            on srp.id = srpd.sales_return_proposal_id\n" +
            "            and td.product_id= srpd.product_id\n" +
            "            and srpd.is_active is true\n" +
            "            and srpd.is_deleted is false\n" +
            "            where chmap.sales_invoice_id = :salesInvoiceId\n" +
            "            group by chmap.sales_invoice_id"
            , nativeQuery = true)
    Map getSalesQuantityAndReturnQuantity(Long salesInvoiceId);

    @Query(value = "#Get total sales return amount\n" +
            "select sum(ifnull(return_data.sales_return_amount,0)) + sum(ifnull(return_data.credit_amount, 0)) totalsales_return_amount\n" +
            "    from (select srp.distributor_id, invtdet.inv_transaction_id,\n" +
            "            sr.id, srp.sales_invoice_id,\n" +
            "            sum(invtdet.quantity * round((ptp.trade_price -\n" +
            "            (ifnull(case when calculation_type = 'PERCENTAGE'\n" +
            "            then ((ptp.trade_price * td.discount_value)/100)\n" +
            "            else td.discount_value end,0))),4)) -\n" +
            "            (si.invoice_discount* sum(invtdet.quantity * round((ptp.trade_price -\n" +
            "            (ifnull(case when calculation_type = 'PERCENTAGE'\n" +
            "            then ((ptp.trade_price * td.discount_value)/100)\n" +
            "            else td.discount_value end,0))),4)) / (si.invoice_discount+si.invoice_amount))\n" +
            "            - ifnull(sra.adjusted_amount, 0) sales_return_amount, cdn_note.credit_amount\n" +
            "            from sales_return_proposal srp\n" +
            "            inner join sales_return_proposal_details srpd on srp.id = srpd.sales_return_proposal_id\n" +
            "            inner join sales_return sr on srp.id = sr.sales_return_proposal_id\n" +
            "            and srp.approval_status = 'APPROVED'\n" +
            "            and srp.distributor_id = :distributorId\n" +
            "            and srp.company_id = :companyId\n" +
            "            and sr.created_date <= :invoiceDate\n" +
            "            and srp.is_active is true\n" +
            "            and srp.is_deleted is false and sr.is_active is true and sr.is_deleted is false\n" +
            "            inner join sales_invoice si on si.id = srp.sales_invoice_id\n" +
            "            \n" +
            "            inner join (select sr.id sales_return_id, sr.inv_transaction_id, invtd.product_id,\n" +
            "            invtd.quantity\n" +
            "            from sales_return sr inner join inv_transaction_details invtd\n" +
            "            on invtd.inv_transaction_id = sr.inv_transaction_id and invtd.created_date <= :invoiceDate) invtdet\n" +
            "            on sr.id = invtdet.sales_return_id\n" +
            "            and invtdet.product_id = srpd.product_id\n" +
            "            inner join product_trade_price ptp on srpd.product_trade_price_id = ptp.id\n" +
            "            and ptp.expiry_date is null\n" +
            "            left join trade_discount td on srpd.trade_discount_id = td.id\n" +
            "            and td.is_deleted is false\n" +
            "            \n" +
            "            left join (select cdn.distributor_id, sum(ifnull(cdn.amount, 0)) credit_amount\n" +
            "            from credit_debit_note cdn where cdn.transaction_type='SALES_RETURN' \n" +
            "            and cdn.note_type='CREDIT' and cdn.approval_status='APPROVED' and cdn.is_deleted is false\n" +
            "            and cdn.created_date <= :invoiceDate\n" +
            "            group by cdn.distributor_id) cdn_note\n" +
            "            on srp.distributor_id = cdn_note.distributor_id\n" +
            "            \n" +
            "            inner join (select pc.distributor_id, sales_invoice_id,\n" +
            "            sum(ifnull(pca.adjusted_amount, 0)) payment_adjusted_amount\n" +
            "            from payment_collection_adjustment pca\n" +
            "            inner join payment_collection pc on pc.id = pca.payment_collection_id\n" +
            "            group by pc.distributor_id, pca.sales_invoice_id) pca\n" +
            "            on srp.sales_invoice_id = pca.sales_invoice_id\n" +
            "            and srp.distributor_id = pca.distributor_id\n" +
            "            \n" +
            "            left join (select sa.sales_return_id, sum(ifnull(sa.adjusted_amount, 0)) adjusted_amount\n" +
            "            from sales_return_adjustment sa where sa.is_deleted is false\n" +
            "            and sa.created_date <= :invoiceDate\n" +
            "            group by sa.sales_return_id) sra\n" +
            "            on sr.id = sra.sales_return_id \n" +
            "            group by invtdet.inv_transaction_id, sr.id) return_data\n" +
            "            where return_data.sales_return_amount >0\n" +
            "            group by return_data.distributor_id\n" +
            "", nativeQuery = true)
    Float getSalesReturnAmount(
            @Param("companyId") Long companyId,
            @Param("distributorId") Long distributorId,
            @Param("invoiceDate") LocalDateTime invoiceDate
    );

}
