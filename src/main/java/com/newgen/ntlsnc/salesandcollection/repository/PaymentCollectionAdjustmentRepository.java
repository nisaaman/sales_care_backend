package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.salesandcollection.entity.PaymentCollectionAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author kamal
 * @Date ২১/৪/২২
 */

@Repository
public interface PaymentCollectionAdjustmentRepository extends JpaRepository<PaymentCollectionAdjustment, Long> {

    List<PaymentCollectionAdjustment> findAllByIsDeletedFalse();

    @Query(value = "SELECT inv_adjust.sales_invoice_id, \n" +
            "inv_adjust.payment_collection_id, inv_adjust.adjusted_amount \n" +
            "FROM payment_collection_adjustment inv_adjust \n" +
            "WHERE inv_adjust.sales_invoice_id IN (:sales_invoice_list) \n" +
            "AND inv_adjust.company_id = :company_id",
            nativeQuery = true)
    List<Map> findInvoiceAdjustments (@Param("company_id") Long companyId, @Param("sales_invoice_list")
                                      List<Long> salesInvoiceList);

    List<PaymentCollectionAdjustment> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    @Query(value = "SELECT a.name, ds.name designation_name, \n" +
            "pj.sales_invoice_id, i.invoice_date, \n" +
            "p.action_taken_date, i.remaining_amount, cd.proposal_date, \n" +
            "DATEDIFF( p.action_taken_date,  i.invoice_date) ord_days \n" +
            "FROM payment_collection_adjustment pj \n" +
            "INNER JOIN sales_invoice i on pj.sales_invoice_id = i.id \n" +
            "INNER JOIN payment_collection p on pj.payment_collection_id = p.id \n" +
            "LEFT JOIN credit_debit_note cd on i.id = cd.invoice_id \n" +
            "and cd.approval_status='APPROVED' \n" +
            "LEFT JOIN application_user a on cd.created_by = a.id \n" +
            "LEFT JOIN designation ds on a.designation_id = ds.id \n" +
            "WHERE (:payment_id is NULL OR pj.payment_collection_id = :payment_id) \n" , nativeQuery = true)
    List<Map<String, Object>> getPaymentAndInvoiceOrdMap(
            @Param("payment_id") Long paymentId);


    Optional<PaymentCollectionAdjustment> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    @Query(value = "#Payment Adjustment Distributor Wise List\n" +
            "select d.id, d.distributor_name,\n" +
            "       round(ifnull(i.invoice_amount,0) + ifnull(disb.opening_invoice_amount, 0) ,2) as invoice_amount,\n" +
            "       round(ifnull(i.overdus,0),2) as overdu_count,\n" +
            "       round(ifnull(c.collection_amount,0),2) as collection_amount,\n" +
            "       round(ifnull(c.adjustable_amount,0),2) as adjustable_amount,\n" +
            "       round(ifnull(c.advance_count,0),2) as advance_count,\n" +
            "       round(ifnull(lb.ledger_balance,0),2) as ledger_balance\n" +
            "from (\n" +
            "select d.id, d.distributor_name\n" +
            "from distributor d\n" +
            "inner join distributor_sales_officer_map dsom\n" +
            "        on d.id = dsom.distributor_id\n" +
            "       and d.organization_id = :organizationId\n" +
            "       and dsom.company_id = :companyId\n" +
            "       and d.is_deleted is false\n" +
            "group by d.id, dsom.company_id\n" +
            "order by d.id) as d\n" +
            "\n" +
            "left join (\n" +
            "#Get invoice amount and overdues\n" +
            "select si.distributor_id, sum(si.invoice_amount) as invoice_amount,\n" +
            "       sum(case when datediff(now(), si.invoice_date)>io.end_day then 1 end) as overdus\n" +
            "from sales_invoice si\n" +
            "inner join invoice_overdue io\n" +
            "        on si.invoice_nature_id = io.invoice_nature_id\n" +
            "       and si.organization_id = :organizationId\n" +
            "       and si.company_id = :companyId\n" +
            "       and io.organization_id = si.organization_id\n" +
            "       and io.company_id = si.company_id\n" +
            "       and si.is_deleted is false\n" +
            "       and si.is_active is true\n" +
            "       and (:fromDate = '' or :fromDate is null or :toDate = '' or :toDate is null\n" +
            "        or (si.invoice_date between :fromDate and :toDate))" +
            "group by si.distributor_id) as i on d.id = i.distributor_id\n" +
            "\n" +
            "left join (\n" +
            "#Get opening invoice amount\n" +
            "select disb.distributor_id, sum(disb.balance) as opening_invoice_amount,\n" +
            "       sum(case when datediff(now(), disb.created_date)>io.end_day then 1 end) as overdus\n" +
            "from distributor_balance disb\n" +
            "inner join invoice_overdue io\n" +
            "       on disb.invoice_nature_id = io.invoice_nature_id\n" +
            "       and io.company_id = disb.company_id\n" +
            "       and disb.organization_id = :organizationId\n" +
            "       and disb.company_id = :companyId\n" +
            "       and disb.is_deleted is false\n" +
            "       and disb.is_active is true\n" +
            "       and (:fromDate = '' or :fromDate is null or :toDate = '' or :toDate is null\n" +
            "       or (disb.created_date between :fromDate and :toDate))" +
            "group by disb.distributor_id) as disb on d.id = disb.distributor_id\n" +
            "\n" +
            "inner join (\n" +
            "#Get collection amount, adjustable amount & advance count\n" +
            "select distributor_id, sum(collection_amount) as collection_amount,\n" +
            "       sum(remaining_amount) as adjustable_amount,\n" +
            "       count(case when payment_nature='ADVANCE' then 1 end) as advance_count\n" +
            "from payment_collection pc\n" +
            "where pc.is_deleted is false\n" +
            "  and pc.is_active is true\n" +
            "  and pc.organization_id = :organizationId\n" +
            "  and pc.company_id = :companyId\n" +
            "  and pc.approval_status = 'APPROVED'\n" +
            "  and ((:fromDate = '' or :fromDate is null or :toDate = '' or :toDate is null\n" +
            "   or (pc.payment_date between :fromDate and :toDate))\n" +
            "   and (:startDate = '' or :startDate is null or :endDate = '' or :endDate is null\n" +
            "              or (pc.payment_date between :startDate and :endDate)))\n" +
            "group by distributor_id) c on d.id = c.distributor_id\n" +
            "\n" +
            "left join (" +
            "    #Get distributor ledger balance\n" +
            "    select lt.distributor_id distributor_id, sum(lt.debit - lt.credit) ledger_balance\n" +
            "    from ledger_transaction lt\n" +
            "    where lt.company_id = :companyId\n" +
            "    group by lt.company_id, lt.distributor_id" +
            ") as lb on d.id = lb.distributor_id" +
            "\n" +
            "left join (\n" +
            "select dsm.distributor_id, count(dsm.id) as sales_officer\n" +
            "from  distributor_sales_officer_map dsm\n" +
            "inner join reporting_manager rm\n" +
            "        on dsm.sales_officer_id = rm.application_user_id\n" +
            "       and dsm.company_id = :companyId\n" +
            "       and rm.is_active is true\n" +
            "       and rm.is_deleted is false\n" +
            "inner join location_manager_map lmm\n" +
            "        on rm.reporting_to_id = lmm.application_user_id\n" +
            "       and lmm.is_deleted is false\n" +
            "       and lmm.is_active is true\n" +
            "\n" +
            "where (:location = '' or :location is null or lmm.location_id = :location)\n" +
            "group by dsm.distributor_id) as so\n" +
            "    on d.id = so.distributor_id\n" +
            "where so.sales_officer > 0;",nativeQuery = true)
    List<Map<String, Object>> getDistributorWisePaymentCollectionInfoList(
            @Param("organizationId") Long organizationId,
            @Param("companyId") Long companyId,
            @Param("location") Long location,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query(value = "#Payment Collection Adjustment History List for Verify\n" +
            "select pca.id, pca.payment_collection_id, pca.sales_invoice_id, pc.distributor_id,\n" +
            "       pc.payment_no,\n" +
            "       datediff(now(), pc.payment_date) as days_ago,\n" +
            "       date_format(pc.payment_date, '%d-%b-%Y') as payment_date,\n" +
            "       d.distributor_name,\n" +
            "       round(ifnull(lb.ledger_balance,0),2) as ledger_balance,\n" +
            "       cau.name as collected_by,\n" +
            "       des.name as designation,\n" +
            "       o.name as company_name,\n" +
            "       pca.adjusted_amount as payment_amount,\n" +
            "       pca.ord_amount,\n" +
            "       si.invoice_no,\n" +
            "       l.name as location_name,\n" +
            "       pca.mapping_date\n" +
            "\n" +
            "from payment_collection_adjustment pca\n" +
            "inner join payment_collection pc\n" +
            "        on pca.payment_collection_id = pc.id\n" +
            "       and pca.organization_id = :organizationId\n" +
            "       and pca.company_id = :companyId\n" +
            "       and pca.is_deleted is false\n" +
            "       and pca.is_active is true\n" +
            "inner join sales_invoice si\n" +
            "        on pca.sales_invoice_id = si.id\n" +
            "inner join organization o\n" +
            "        on pc.company_id = o.id\n" +
            "inner join distributor d\n" +
            "        on pc.distributor_id = d.id\n" +
            "inner join application_user cau\n" +
            "           on pc.collection_by_id = cau.id\n" +
            "inner join designation des\n" +
            "           on cau.designation_id = des.id\n" +
            "inner join reporting_manager rm\n" +
            "           on cau.id = rm.application_user_id\n" +
            "               and rm.to_date is null\n" +
            "inner join location_manager_map lmm\n" +
            "        on rm.reporting_to_id = lmm.application_user_id\n" +
            "       and lmm.to_date is null\n" +
            "       and lmm.company_id = pc.company_id\n" +
            "inner join location l\n" +
            "        on lmm.location_id = l.id\n" +
            "inner join location_type lt\n" +
            "        on l.location_type_id = lt.id\n" +
            "left join (\n" +
            "    #Get distributor ledger balance\n" +
            "    select lt.distributor_id distributor_id, sum(lt.debit - lt.credit) ledger_balance\n" +
            "    from ledger_transaction lt\n" +
            "    where lt.company_id = :companyId\n" +
            "    group by lt.company_id, lt.distributor_id\n" +
            ") as lb on d.id = lb.distributor_id\n" +
            "\n" +
            "where (:location = '' or :location is null or l.id = :location)\n" +
            "  and (:fromDate = '' or :fromDate is null or :toDate = '' or :toDate is null\n" +
            "    or (pca.mapping_date between :fromDate and :toDate))\n" +
            "order by pca.id desc;", nativeQuery = true)
    List<Map<String, Object>> getDistributorWisePaymentCollectionAdjustmentHistory(
            @Param("organizationId") Long organizationId,
            @Param("companyId") Long companyId,
            @Param("location") Long location,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query(value = "#Payment Collection Adjustment List for ORD Settlement\n" +
            "select pca.id, pca.payment_collection_id, " +
            "       pca.sales_invoice_id, pc.distributor_id, pca.company_id,\n" +
            "       pc.payment_no,\n" +
            "       datediff(now(), pc.payment_date) as days_ago,\n" +
            "       date_format(pc.payment_date, '%d-%b-%Y') as payment_date,\n" +
            "       d.distributor_name,\n" +
            "       round(ifnull(lb.ledger_balance,0),2) as ledger_balance,\n" +
            "       cau.name as collected_by,\n" +
            "       des.name as designation,\n" +
            "       o.name as company_name,\n" +
            "       pca.adjusted_amount as payment_amount,\n" +
            "       pca.ord_amount,\n" +
            "       si.invoice_no,\n" +
            "       l.name as location_name,\n" +
            "       pca.mapping_date,\n" +
            "       pca.is_ord_settled,\n" +
            "       db.id distributor_balance_id, db.reference_no\n" +
            "\n" +
            "from payment_collection_adjustment pca\n" +
            "inner join payment_collection pc\n" +
            "        on pca.payment_collection_id = pc.id\n" +
            "            and pca.organization_id = :organizationId\n" +
            "            and pca.company_id = :companyId\n" +
            "            and pca.is_deleted is false\n" +
            "            and pca.is_active is true\n" +
            "left join sales_invoice si\n" +
            "        on pca.sales_invoice_id = si.id\n" +
            "left join distributor_balance db\n" +
            "        on pca.distributor_balance_id = db.id\n" +
            "inner join organization o\n" +
            "        on pc.company_id = o.id\n" +
            "inner join distributor d\n" +
            "        on pc.distributor_id = d.id\n" +
            "inner join application_user cau\n" +
            "        on pc.collection_by_id = cau.id\n" +
            "inner join designation des\n" +
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
            "        on l.location_type_id = lt.id\n" +
            "left join (\n" +
            "    #Get distributor ledger balance\n" +
            "    select lt.distributor_id distributor_id, sum(lt.debit - lt.credit) ledger_balance\n" +
            "    from ledger_transaction lt\n" +
            "    where lt.company_id = :companyId\n" +
            "    group by lt.company_id, lt.distributor_id\n" +
            ") as lb on d.id = lb.distributor_id\n" +
            "\n" +
            "where pca.ord_amount > 0\n" +
            "#  and si.remaining_amount >= si.ord_amount\n" +
            "   and (:status = '' or :status is null or pca.is_ord_settled = case :status when 'true' then true else false end )\n" +
            "   and (:location = '' or :location is null or l.id = :location)\n" +
            "   and (:fromDate = '' or :fromDate is null or :toDate = '' or :toDate is null\n" +
            "    or (pca.mapping_date between :fromDate and :toDate))\n" +
            "order by pca.id desc;", nativeQuery = true)
    List<Map<String, Object>> getDistributorWisePaymentCollectionAdjustmentListForOrdSettlement(
            @Param("organizationId") Long organizationId,
            @Param("companyId") Long companyId,
            @Param("location") Long location,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") String status
    );
}
