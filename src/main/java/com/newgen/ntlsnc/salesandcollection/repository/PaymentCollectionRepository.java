package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.salesandcollection.entity.Distributor;
import com.newgen.ntlsnc.salesandcollection.entity.PaymentCollection;
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
 * @Date ২০/৪/২২
 */

@Repository
public interface PaymentCollectionRepository extends JpaRepository<PaymentCollection, Long> {
    List<PaymentCollection> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    @Query(value = "SELECT * \n" +
            "FROM payment_collection pc \n" +
            "WHERE (:company_id is NULL OR pc.company_id = :company_id) \n" +
            "AND (:start_date is NULL OR pc.payment_date >= :start_date) \n" +
            "AND (:end_date is NULL OR pc.payment_date <= :end_date) \n" +
            "AND (:paymentCollectionStatus is null or pc.approval_status = :paymentCollectionStatus) \n" +
            "AND pc.collection_by_id IN :soList", nativeQuery = true)
    List<PaymentCollection> getPaymentList(@Param("company_id") Long companyId
                                         , @Param("start_date") LocalDate startDate
                                         , @Param("end_date") LocalDate endDate
                                         , @Param("soList") List<Long> soList
                                         , @Param("paymentCollectionStatus") ApprovalStatus paymentCollectionStatus);



     @Query(value = "SELECT pj \n" +
                "FROM PaymentCollectionAdjustment pj \n" +
                "WHERE (:payment_id is NULL OR pj.paymentCollection.id = :payment_id)" )
     List<PaymentCollectionAdjustment> getPaymentAdjustmentList(
                @Param("payment_id") Long paymentId);

    @Query(value = "SELECT pj.mapping_date, pj.created_by, au.name, \n" +
            "d.name designation_name " +
            "FROM payment_collection_adjustment pj \n" +
            "INNER JOIN application_user au on pj.created_by = au.id \n" +
            "LEFT JOIN designation d on au.designation_id = d.id \n" +
            "WHERE (:payment_id is NULL OR pj.payment_collection_id = :payment_id) \n" +
            "LIMIT 1" , nativeQuery = true)
    Map getPaymentAdjustment(
            @Param("payment_id") Long paymentId);


    Optional<PaymentCollection> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    @Query(value = "#Payment Collection List for Verify\n" +
            "select pc.id, pc.payment_no, pc.remarks,\n" +
            "       pc.organization_id, pc.company_id,\n" +
            "       date_format(pc.payment_date, '%d-%b-%Y') as payment_date,\n" +
            "       datediff(now(), pc.payment_date) as days_ago,\n" +
            "       d.distributor_name,\n" +
//          "       db.advance_payment + db.commission_balance as distributor_balance,\n" +
            "       round(ifnull(t_lb.ledger_balance,0),2) as distributor_balance,\n" +
            "       cau.name as collected_by,\n" +
            "       des.name as designation,\n" +
            "       l.name as location_name,\n" +
            "       pc.collection_by_id,\n" +
            "       round(pc.collection_amount,2) as collection_amount,\n" +
            "       pc.payment_nature,\n" +
            "       pc.payment_type,\n" +
            "       pc.approval_status,\n" +
            "       pc.reference_no,\n" +
            "       pb.book_number,\n" +
            "       pc.money_receipt_no,\n" +
            "       bb.name as bank_branch_name,\n" +
            "       b.name as bank_name,\n" +
            "       doc.file_name,\n" +
            "       doc.file_size" +
            "\n" +
            "from payment_collection pc\n" +
            "inner join payment_book pb\n" +
            "        on pb.id = pc.payment_book_id\n" +
            " left join bank_branch bb\n" +
            "        on bb.id = pc.bank_branch_id\n" +
            " left join bank b\n" +
            "        on b.id = bb.bank_id\n" +
            "inner join distributor d\n" +
            "        on pc.distributor_id = d.id\n" +
            "       and pc.organization_id = :organizationId\n" +
            "       and pc.is_deleted is false\n" +
            "       and pc.company_id = :companyId\n" +
            "# left join distributor_balance db " +
            "#        on d.id = db.distributor_id\n" +
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
            "    #Get distributor ledger balance\n" +
            "    select lt.distributor_id distributor_id, sum(lt.debit - lt.credit) ledger_balance\n" +
            "    from ledger_transaction lt\n" +
            "    where lt.company_id = :companyId\n" +
            "    group by lt.company_id, lt.distributor_id" +
            ") as t_lb on d.id = t_lb.distributor_id\n"+
            "left join document doc\n" +
            "       on pc.id = doc.ref_id\n" +
            "      and ref_table = 'PaymentCollection'\n" +
            "      and doc.organization_id = pc.organization_id\n" +
            "      and doc.company_id = pc.company_id\n" +
            "      and doc.is_deleted is false\n" +
            "      and doc.is_active is true"+
            "\n" +
            "where (:paymentNature = '' or :paymentNature is null or pc.payment_nature = :paymentNature)\n" +
            "  and (:paymentType = '' or :paymentType is null or pc.payment_type = :paymentType)\n" +
            "  and (:location = '' or :location is null or l.id = :location)\n" +
            "  and (:status = '' or :status is null or pc.approval_status = :status)\n" +
            "  and (:fromDate = '' or :fromDate is null or :toDate = '' or :toDate is null\n" +
            "       or (pc.payment_date between :fromDate and :toDate))" +
            "  and (pc.approval_status_for_authorization = 'APPROVED')\n" +
            "order by pc.payment_date desc, pc.id desc;", nativeQuery = true)
    List<Map<String, Object>> getPaymentCollectionListToVerify(
            @Param("organizationId") Long organizationId,
            @Param("companyId") Long companyId,
            @Param("paymentNature") String paymentNature,
            @Param("paymentType") String paymentType,
            @Param("location") Long location,
            @Param("status") String status,
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate
    );

    @Query(value ="SELECT \n" +
            "        CASE cdn.transaction_type\n" +
            "            when 'SALES_RETURN' then 'Sales Return'\n" +
            "            when 'ORD' then 'Ord Commission'\n" +
            "\t\tEND title,\n" +
            "        cdn.note_type as type ,\n" +
            "        cdn.proposal_date as date,\n" +
            "        cdn.amount as amount,\n" +
            "        cdn.note_no as NoteNo,\n" +
            "        date_format(cdn.proposal_date , '%b %Y') as formattedDate\n" +
            "FROM credit_debit_note cdn\n" +
            "where \n" +
            "\t\tcdn.approval_status = 'APPROVED'\n" +
            "\t\tand (:status IS NULL OR cdn.note_type=:status)\n" +
            "\t\tand cdn.is_active is true\t\t\t\t\t\t\t\t\t\t\t \n" +
            "\t\tand cdn.is_deleted is false\n" +
            "\t\tand cdn.company_id=:companyId\n" +
            "\t\tand cdn.distributor_id=:distributorId \n" +
            "\t\n" +
            "union all \n" +
            "\n" +
            "SELECT distinct \n" +
            "\t\t'Sales Return' as title,\n" +
            "        'CREDIT' as type,\n" +
            "\t\tdate_format(sr.return_date , '%Y-%m-%d') as date,\n" +
            "\t\titd.quantity * ptp.trade_price as amount,\n" +
            "        sr.return_no as NoteNo,\n" +
            "        date_format(sr.return_date , '%b %Y') as formattedDate\n" +
            "FROM sales_return sr\n" +
            "inner join sales_return_proposal srp \n" +
            "\t\ton srp.id = sr.sales_return_proposal_id\n" +
            "\t\tand srp.approval_status='APPROVED'\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n" +
            "\t\tand srp.is_active is true\t\t\t\t\t\t\t\t\t\t\n" +
            "\t\tand srp.is_deleted is false\t\n" +
            "inner join inv_transaction_details itd \n" +
            "\t\ton itd.inv_transaction_id=sr.inv_transaction_id\n" +
            "inner join product_trade_price ptp \n" +
            "\t\ton ptp.product_id=itd.product_id\n" +
            "\t\tand ptp.expiry_date is null\n" +
            "where\n" +
            "\t\tsr.is_active=true\n" +
            "        and (:status IS NULL OR 'CREDIT'=:status)\n" +
            "\t\tand sr.is_deleted=false\n" +
            "\t\tand sr.company_id=:companyId\n" +
            "\t\tand sr.distributor_id=:distributorId\n" +
            "        \n" +
            "union all \n" +
            "\n" +
            "SELECT \n" +
            "\t\t'Payment Collection' as title,\n" +
            "        'CREDIT' as type,\n" +
            "        pc.payment_date as date,\n" +
            "        pc.collection_amount as amount,\n" +
            "        pc.payment_no as NoteNo,\n" +
            "        date_format(pc.payment_date , '%b %Y') as formattedDate\n" +
            "FROM payment_collection pc\n" +
            "where \n" +
            "\t\tapproval_status= 'APPROVED'\n" +
            "        and (:status IS NULL OR 'CREDIT'=:status)\n" +
            "\t\tand pc.is_active=true\n" +
            "\t\tand pc.is_deleted=false\n" +
            "\t\tand pc.company_id=:companyId \n" +
            "\t\tand pc.distributor_id=:distributorId\n" +
            "    \n" +
            "union all\n" +
            "    \n" +
            "SELECT \n" +
            "\t\t'Sale' as title,\n" +
            "\t\t'DEBIT' as type,\n" +
            "        si.invoice_date as date,\n" +
            "        si.invoice_amount as amount,\n" +
            "        si.invoice_no as NoteNo,\n" +
            "        date_format(si.invoice_date , '%b %Y') as formattedDate\n" +
            "FROM sales_invoice si\t\t\t\t\t\t\t\t\t\t\t\t\t\n" +
            "where \n" +
            "# NGLSC-2261 si.is_accepted is true\n" +
            "        and (:status IS NULL OR 'DEBIT'=:status)\n" +
            "\t\tand si.is_active is true\t\t\t\t\t\t\t\t\t\t\t\t\n" +
            "\t\tand si.is_deleted is false\t\t\t\t\t\t\t\t\t\t\t\t\n" +
            "\t\tand si.company_id = :companyId\n" +
            "\t\tand si.distributor_id = :distributorId", nativeQuery = true)
    List<Map<String, String>> findLedgerList(
            @Param("distributorId") Long distributorId,
            @Param("companyId") Long companyId,
            @Param("status") String status);

    @Query(value = "#Get Payment Collection List for Adjustment\n" +
            "select * from (select pc.id,\n" +
            "if (pc.reference_no like 'OB_%', \n" +
            " CONCAT(\"Opening Balance\",\" \",pc.payment_no,\" \",pc.payment_nature), pc.payment_no)\n" +
            "as paymentNo,\n" +
            "       pc.money_receipt_no as moneyReceiptNo,\n" +
            "       pc.payment_date as paymentDate,\n" +
            "       round(pc.remaining_amount, 2) as remainingAmount,\n" +
            "       round(pc.collection_amount, 2) as collectionAmount,\n" +
            "       pc.action_taken_date as actionTakenDate,\n" +
            "       pc.reference_no as referenceNo,\n" +
            "@adjustType \\:= 'PAYMENT' as adjustType,\n" +
            "0 as paymentAdjustedAmount\n" +
            "from payment_collection pc\n" +
            "where pc.distributor_id = :distributorId\n" +
            "  and pc.company_id = :companyId\n" +
            "  and pc.is_deleted is false\n" +
            "  and pc.is_active is true\n" +
            "  and pc.approval_status = 'APPROVED'\n" +
            "  and pc.remaining_amount > 0\n" +
            "  and (:fromDate = '' or :fromDate is null or :toDate = '' or :toDate is null\n" +
            "  or (pc.payment_date between :fromDate and :toDate))\n" +
            "order by pc.payment_date desc) as payment\n" +
            "\n" +
            "union all\n" +
            "select * from (select sales_return.id,\n" +
            "sales_return.invoice_no as paymentNo, sales_return.return_no as moneyReceiptNo,\n" +
            "date_format(CURDATE(), '%Y-%m-%d') as paymentDate,\n" +
            "round(sales_return.sales_return_amount, 2) as remainingAmount,\n" +
            "ifnull(sales_return.payment_adjusted_amount, 0) collectionAmount,\n" +
            "null actionTakenDate, null referenceNo, \n" +
            "@adjustType \\:= 'RETURN' as adjustType,\n" +
            "sales_return.payment_adjusted_amount paymentAdjustedAmount\n" +
            "from sales_return sr\n" +
            "inner join\n" +
            "(select sr.return_no, si.invoice_no,\n" +
            "(si.remaining_amount-si.ord_amount) balance_amount,\n" +
            "invtdet.inv_transaction_id, sr.id,\n" +
            "sum(invtdet.quantity * round((ptp.trade_price -\n" +
            "(ifnull(case when calculation_type = 'PERCENTAGE'\n" +
            "then ((ptp.trade_price * td.discount_value)/100)\n" +
            "else td.discount_value end,0))),4)) -\n" +
            "(si.invoice_discount* sum(invtdet.quantity * round((ptp.trade_price -\n" +
            "(ifnull(case when calculation_type = 'PERCENTAGE'\n" +
            "then ((ptp.trade_price * td.discount_value)/100)\n" +
            "else td.discount_value end,0))),4)) / (si.invoice_discount+si.invoice_amount))\n" +
            "- ifnull(sra.adjusted_amount, 0) sales_return_amount, pca.payment_adjusted_amount\n" +
            "from sales_return_proposal srp\n" +
            "inner join sales_return_proposal_details srpd on srp.id = srpd.sales_return_proposal_id\n" +
            "inner join sales_return sr on srp.id = sr.sales_return_proposal_id\n" +
            "and srp.approval_status = 'APPROVED'\n" +
            "and srp.distributor_id = :distributorId\n" +
            "and srp.company_id = :companyId\n" +
            "and srp.is_active is true\n" +
            "and srp.is_deleted is false and sr.is_active is true and sr.is_deleted is false\n" +
            "inner join sales_invoice si on si.id = srp.sales_invoice_id\n" +
            "\n" +
            "inner join (select sr.id sales_return_id, sr.inv_transaction_id, invtd.product_id,\n" +
            "invtd.quantity\n" +
            "from sales_return sr inner join inv_transaction_details invtd\n" +
            "on invtd.inv_transaction_id = sr.inv_transaction_id) invtdet\n" +
            "on sr.id = invtdet.sales_return_id and invtdet.product_id = srpd.product_id\n" +
            "inner join product_trade_price ptp on srpd.product_trade_price_id = ptp.id\n" +
            "and ptp.expiry_date is null\n" +
            "left join trade_discount td on srpd.trade_discount_id = td.id\n" +
            "and td.is_deleted is false\n" +
            "\n" +
            "inner join (select sales_invoice_id, sum(ifnull(pca.adjusted_amount, 0)) payment_adjusted_amount\n" +
            "from payment_collection_adjustment pca \n" +
            "group by pca.sales_invoice_id) pca\n" +
            "on srp.sales_invoice_id = pca.sales_invoice_id\n" +
            "\n" +
            "left join (select sa.sales_return_id, sum(ifnull(sa.adjusted_amount, 0)) adjusted_amount\n" +
            "from sales_return_adjustment sa group by sa.sales_return_id) sra\n" +
            "on sr.id = sra.sales_return_id" +
            "\n" +
            "group by \n" +
            "invtdet.inv_transaction_id, sr.id, si.invoice_no, sr.return_no, pca.payment_adjusted_amount) sales_return\n" +
            "on sr.inv_transaction_id = sales_return.inv_transaction_id\n" +
            "and sales_return.sales_return_amount >0\n" +
            "order by sr.return_date asc) as return_info\n" +
            "", nativeQuery = true)
    List<Map<String, String>> getPaymentCollectionListForAdjustment(
            @Param("distributorId") Long distributorId,
            @Param("companyId") Long companyId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    List<PaymentCollection> getAllByDistributorIdAndCompanyIdAndIsDeletedIsFalseAndIsActiveIsTrueAndApprovalStatusEqualsAndRemainingAmountGreaterThanOrderByPaymentDateDesc(
            Long distributorId, Long companyId, ApprovalStatus approvalStatus, Float remainingAmount);

    List<PaymentCollection> findByOrganizationAndCompanyIdAndPaymentBookIdAndIsDeletedFalse(Organization organization, Long companyId, Long paymentBookId);

    Optional<PaymentCollection> findByOrganizationAndCompanyIdAndPaymentBookIdAndMoneyReceiptNoAndIsDeletedFalse(Organization organization, Long companyId, Long paymentBookId, Long moneyReceiptNo);

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
            "       @var\\:=:approvalActor approvalActor,\n" +
            "       @var\\:=:level level,\n" +
            "       @var\\:=:approvalStepId approvalStepId,\n" +
            "       @var\\:=:multiLayerApprovalPathId multiLayerApprovalPathId,\n" +
            "       @var\\:=:approvalActorId approvalActorId, \n" +
            "       @var\\:=:approvalStatusName approvalStatus, \n" +
            "       @var\\:=:approvalStepName approvalStepName \n" +
            "       from payment_collection pc\n" +
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
    List<Map<String, Object>> getPendingListForApproval(
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
            @Param("approvalStepName") String approvalStepName
    );

    @Query(value = "SELECT pb.from_mr_no fromMrNo,pb.to_mr_no toMrNo,pc.money_receipt_no moneyReceiptNo\n" +
            "FROM payment_collection pc\n" +
            "INNER JOIN payment_book pb on pb.id = pc.payment_book_id and payment_book_id = :paymentBookId\n" +
            "ORDER BY pc.money_receipt_no desc limit 1",nativeQuery = true)
    Map<String,Object> getLastMoneyReceiptNoDetails(@Param("paymentBookId") Long paymentBookId);

    @Query(value = "SELECT pc.id FROM payment_collection pc\n" +
            "where NOT EXISTS (select * from payment_collection_adjustment pca where pca.payment_collection_id = pc.id \n" +
            "and pca.is_active is true and pca.is_deleted is false) \n" +
            "and pc.is_active is true and pc.is_deleted is false \n" +
            "and pc.payment_nature = 'ADVANCE' and pc.action_taken_date is null \n" +
            "and pc.distributor_id = :distributorId and pc.company_id = :companyId ",nativeQuery = true)
    List<Long> getNotUsedListByDistributorIdAndCompanyId(
            @Param("distributorId") Long distributorId, @Param("companyId") Long companyId);
    List<PaymentCollection> findByIdIn(List<Long> paymentCollectionNotUsedIdList);

    boolean existsByDistributorAndCompany(Distributor distributor, Organization company);
}
