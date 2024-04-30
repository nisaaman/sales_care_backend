package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.salesandcollection.entity.Distributor;
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
 * @author sagor
 * Created on 5/4/22 10:29 AM
 */
@Repository
public interface DistributorRepository extends JpaRepository<Distributor, Long> {

    Distributor findByIdAndIsDeletedFalse(Long id);

    List<Distributor> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    @Query(value = "select d.id,d.distributor_name distributorName," +
            "d.contact_no contactNo," +
            "d.ship_to_address ship_to_address," +
            "dsom.company_id companyId\n" +
            "from  distributor d\n" +
            "INNER JOIN distributor_sales_officer_map dsom ON dsom.distributor_id = d.id\n" +
            "where dsom.company_id =:companyId\n" +
            "and dsom.sales_officer_id= :salesOfficerId\n" +
            "and dsom.to_date is null\n" +
            "and d.is_active is true and d.is_deleted is false \n" +
            "and dsom.is_active is true and dsom.is_deleted is false;", nativeQuery = true)
    List<Map<String, Object>> findAllDistributorList(@Param("salesOfficerId") Long salesOfficerId, @Param("companyId") Long companyId);

//    @Query(value = "select  cl.credit_limit, sb.distributor_id, sb.semester_id,sb.booking_date\n" +
//            "from sc.credit_limit cl\n" +
//            "INNER JOIN sc.sales_booking sb ON sc.sb.distributor_id = sc.cl.distributor_id\n" +
//            "where cl.is_active is true and cl.is_deleted is false\n" +
//            "ORDER BY sb.booking_date desc limit 1;",nativeQuery = true)

    @Query(value = "select cl.credit_limit \n" +
            "from sales_booking sb \n" +
            "INNER JOIN credit_limit cl ON sb.distributor_id = cl.distributor_id \n" +
            "AND sb.semester_id = cl.semester_id \n" +
            "where cl.is_active is true and cl.is_deleted is false \n" +
            "AND :booking_id is NULL OR sb.id = :booking_id \n" +
            "ORDER BY cl.created_date desc limit 1;", nativeQuery = true)
    Double findDistributorCreditLimitByBookingNo(@Param("booking_id") Long bookingId);

    @Query(value = "select dis.distributor_name as distributorName,  \n" +
            "dis.contact_no as distributorContactNo, \n" +
            "round(ifnull(sum(lt.debit - lt.credit),0),4) as ledgerBalance from sales_order so\n" +
            "inner join sales_booking sb on so.sales_booking_id = sb.id \n" +
            "and so.id =:salesOrderId \n" +
            "inner join distributor dis on sb.distributor_id = dis.id \n" +
            "left join ledger_transaction lt on dis.id = lt.distributor_id\n" +
            "and lt.transaction_date <= :asOnDate", nativeQuery = true)
    Map<String, Object> getDistributorDetailsInSalesOrder(
            @Param("salesOrderId") Long salesOrderId, LocalDate asOnDate);


    @Query(value = "select round(ifnull(sum(lt.debit - lt.credit),0),4) as ledgerBalance \n" +
            "from distributor dis \n" +
            "inner join ledger_transaction lt on dis.id = lt.distributor_id \n" +
            "and dis.id=:distributorId and dis.is_active is true and dis.is_deleted is false \n" +
            "and lt.company_id = :companyId and (:fromDate is null or lt.transaction_date >= :fromDate) \n" +
            "and lt.transaction_date <= :asOnDate group by dis.id", nativeQuery = true)
    Object getDistributorLedgerBalancePeriodicOrAsOnDate(@Param("companyId") Long companyId,
                                                         @Param("distributorId") Long distributorId,
                                                         @Param("fromDate") LocalDate fromDate,
                                                         @Param("asOnDate") LocalDate asOnDate);


    @Query(value = "select dis.id, dis.distributor_name as distributorName, dis.contact_no as contactNo, \n" +
            "ifnull(ltd.ledgerBalance,0) ledgerBalance from distributor dis \n" +
            "left join (select lt.distributor_id,round(ifnull(sum(lt.debit - lt.credit),0),4) as ledgerBalance \n" +
            "from ledger_transaction lt where lt.company_id = :companyId \n" +
            "and (:fromDate is null or lt.transaction_date >= :fromDate) \n"+
            "and lt.transaction_date <= :asOnDate group by lt.distributor_id) ltd on dis.id = ltd.distributor_id \n"+
            "where (COALESCE(:distributorList) is null or dis.id in (:distributorList)) \n" +
            "and dis.is_active is true and dis.is_deleted is false\n" +
            "order by dis.distributor_name;", nativeQuery = true)
    List<Map<String, Object>> getDistributorsDetailsWithLedgerBalance(
            @Param("distributorList") List<Long> distributorList,
            @Param("companyId") Long companyId,
            @Param("fromDate") LocalDate fromDate,
            @Param("asOnDate") LocalDate asOnDate); // asOnDate = toDate


    @Query(value = "select  row_id, DATE_FORMAT(transaction_date,\"%Y-%m-%d\") as transactionDate,\n" +
            "DATE_FORMAT(transaction_date,\"%d-%b-%Y\") as date, \n" +
            "MONTH(transaction_date) AS month, \n" +
            "YEAR(transaction_date) AS year, \n" +
            "DATE_FORMAT(transaction_date, '%b %Y') AS year_and_month, \n" +
            "description, \n" +
            "SUBSTRING_INDEX(description,\"(\",1) transactionType, \n" +
            "SUBSTRING_INDEX(SUBSTRING_INDEX(description,')', 2), '(',-1) AS transactionNumber,\n" +
            "debit, credit \n" +
            "from ledger_transaction where company_id= :companyId \n" +
            "and distributor_id=:distributorId and transaction_date >=:fromDate \n" +
            "and transaction_date <= :thruDate", nativeQuery = true)
    List<Map<String, Object>> getDistributorLedgerTransaction(
            @Param("distributorId") Long distributorId,
            @Param("companyId") Long companyId,
            @Param("fromDate") LocalDate fromDate,
            @Param("thruDate") LocalDate thruDate);

    @Query(value = "select (sum(ifnull(si.invoice_amount,0)) + sum(ifnull(dn.amount,0))) - " +
            "(sum(ifnull((invtdet.quantity * ptp.trade_price),0)) + sum(ifnull(pc.collection_amount,0)) " +
            "+ sum(ifnull(cn.amount,0))) as distributorLedgerBalance from distributor dis \n" +
            "inner join distributor_company_map dcm on dis.id = dcm.distributor_id \n" +
            "and dcm.company_id = :companyId \n" +
            "inner join sales_invoice si on dis.id = si.distributor_id \n" +
            "# NGLSC-2261 and si.is_accepted is true \n" +
            "and si.is_active is true and si.is_deleted is false \n" +
            "and si.company_id = :companyId \n" +
            "and si.acceptance_date <= :asOnDate and dis.is_active is true  \n" +
            "and dis.is_deleted is false and dis.id= :distributorId \n" +
            "left join sales_return_proposal srp on dis.id = srp.distributor_id \n" +
            "and srp.approval_status='APPROVED' and srp.company_id = :companyId \n" +
            "left join sales_return sr on srp.id = sr.sales_return_proposal_id \n" +
            "and sr.return_date <= :asOnDate  \n" +
            "left join inv_transaction_details invtdet on sr.inv_transaction_id = invtdet.inv_transaction_id \n" +
            "left join product_trade_price ptp on invtdet.product_id = ptp.product_id \n" +
            "and ptp.expiry_date is null \n" +
            "left join credit_debit_note cn on dis.id = cn.distributor_id \n" +
            "and cn.company_id = :companyId \n" +
            "and cn.approval_status='APPROVED' and cn.note_type='CREDIT' and \n" +
            "cn.proposal_date <= :asOnDate \n" +
            "left join credit_debit_note dn on dis.id = dn.distributor_id \n" +
            "and dn.company_id = :companyId  \n" +
            "and dn.approval_status='APPROVED' and dn.note_type='DEBIT'  \n" +
            "and dn.proposal_date <= :asOnDate \n" +
            "left join payment_collection pc on dis.id = pc.distributor_id \n" +
            "and pc.company_id = :companyId \n" +
            "and pc.approval_status='APPROVED' and pc.action_taken_date <= :asOnDate", nativeQuery = true)
    Object getDistributorLedgerBalance(@Param("companyId") Long companyId,
                                       @Param("distributorId") Long distributorId,
                                       @Param("asOnDate") LocalDate asOnDate);


    @Query(value = "select dis.id, dis.distributor_name as distributorName, dis.contact_no as contactNo, " +
            "round((sum(ifnull(si.invoice_amount,0)) + " +
            "sum(ifnull(dn.amount,0))) - (sum(ifnull((invtdet.quantity * ptp.trade_price),0)) + " +
            "sum(ifnull(pc.collection_amount,0)) + sum(ifnull(cn.amount,0))),4) as ledgerBalance \n" +
            "from distributor dis inner join distributor_company_map dcm on dis.id = dcm.distributor_id \n" +
            "and dcm.company_id = :companyId \n"+
            "inner join sales_invoice si on dis.id = si.distributor_id \n" +
            "and si.company_id = :companyId \n"+
            "# NGLSC-2261 and si.is_accepted is true \n"+
            "and si.is_active is true and si.is_deleted is false \n" +
            "and (:fromDate is null or si.acceptance_date >= :fromDate) \n" +
            "and si.acceptance_date <= :asOnDate and dis.is_active is true \n" +
            "and dis.is_deleted is false and dis.id in(:distributorList) \n" +
            "left join sales_return_proposal srp on dis.id = srp.distributor_id \n" +
            "and srp.approval_status='APPROVED' and srp.company_id = :companyId \n" +
            "left join sales_return sr on srp.id = sr.sales_return_proposal_id \n" +
            "and (:fromDate is null or sr.return_date >= :fromDate )\n" +
            "and sr.return_date <= :asOnDate \n" +
            "left join inv_transaction_details invtdet on sr.inv_transaction_id = invtdet.inv_transaction_id \n"+
            "left join product_trade_price ptp on invtdet.product_id = ptp.product_id and ptp.expiry_date is null\n" +
            "left join credit_debit_note cn on dis.id = cn.distributor_id \n" +
            "and cn.approval_status='APPROVED' and cn.note_type='CREDIT' \n" +
            "and (:fromDate is null or cn.proposal_date >= :fromDate) \n" +
            "and cn.proposal_date <= :asOnDate and cn.company_id = :companyId \n" +
            "left join credit_debit_note dn on dis.id = dn.distributor_id \n" +
            "and dn.approval_status='APPROVED' and dn.note_type='DEBIT'  \n" +
            "and (:fromDate is null or dn.proposal_date >= :fromDate) \n" +
            "and dn.proposal_date <= :asOnDate and dn.company_id = :companyId \n" +
            "left join payment_collection pc on dis.id = pc.distributor_id \n" +
            "and pc.company_id = :companyId \n"+
            "and ( :fromDate is null or pc.action_taken_date >= :fromDate ) \n" +
            "and pc.approval_status='APPROVED'  and pc.action_taken_date <= :asOnDate \n" +
            "group by dis.id, dis.distributor_name;\n", nativeQuery = true)
    List<Map<String, Object>> getDistributorDetailsWithLedgerBalance(
            @Param("distributorList") List<Long> distributorList,
            @Param("companyId") Long companyId, @Param("fromDate") LocalDate fromDate, @Param("asOnDate") LocalDate asOnDate); // asOnDate = toDate


    @Query(value = "select cl.credit_limit \n" +
            "from credit_limit cl \n" +
            "where cl.is_active is true and cl.is_deleted is false \n" +
            "and cl.approval_status = 'APPROVED' \n" +
            "and :semester_id is NULL OR cl.semester_id = :semester_id \n" +
            "and :distributor_id is NULL OR cl.distributor_id = :distributor_id \n" +
            "order by cl.created_date desc limit 1 ", nativeQuery = true)
    Double getDistributorCreditLimit(@Param("distributor_id") Long distributorId,
                                     @Param("semester_id") Long semesterId);

    @Query(value = "select d.id                                            distributor_id\n" +
            "     , si.id                                           invoice_id\n" +
            "     , pca.id                                          payment_collection_adjustment_id\n" +
            "     , pc.id                                           payment_collection_id\n" +
            "     , d.distributor_name\n" +
            "     , si.invoice_amount                               invoice_amount\n" +
            "     , si.remaining_amount                             remaining_amount\n" +
            "     , pca.adjusted_amount                             adjusted_amount\n" +
            "     , date_format(pc.action_taken_date, '%Y-%m-%d')                           payment_date\n" +
            "     , datediff(pc.action_taken_date, si.invoice_date) collection_duration\n" +
            "from distributor d\n" +
            "         inner join sales_invoice si on si.distributor_id = d.id and d.is_deleted is false and d.is_active is true\n" +
            "    and d.id in :distributorIdList \n" +
            "    and si.company_id = :companyId \n" +
            "    and si.is_deleted is false and si.is_active is true and si.remaining_amount != 0\n" +
            "         inner join payment_collection_adjustment pca\n" +
            "                    on pca.sales_invoice_id = si.id and pca.is_active is true and pca.is_deleted is false\n" +
            "                        and pca.company_id = :companyId \n" +
            "         inner join payment_collection pc\n" +
            "                    on pc.id = pca.payment_collection_id and pc.is_active is true and pc.is_deleted is false\n" +
            "                        and pc.company_id = :companyId ;", nativeQuery = true)
    List<Map> getPaymentCollectionAdjustmentDistributorListByCompanyIdAndLocationList(@Param("companyId") Long companyId, @Param("distributorIdList") List<Long> distributorIdList);

    List<Distributor> findAllByIdIn(List<Long> ids);

    Optional<Distributor> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    @Query(value = "select round((sum(ifnull(si.invoice_amount,0)) + " +
            "sum(ifnull(dn.amount,0))) - (sum(ifnull((invtdet.quantity * ptp.trade_price),0)) + " +
            "sum(ifnull(pc.collection_amount,0)) + sum(ifnull(cn.amount,0))),4) as tolalLedgerBalance \n" +
            "from distributor dis inner join distributor_sales_officer_map dsom on dis.id = dsom.distributor_id \n" +
            "and dsom.company_id = :companyId and dsom.sales_officer_id = :salesOfficerId \n" +
            "and dsom.is_active is true and dsom.is_deleted is false \n" +
            "inner join sales_invoice si on dis.id = si.distributor_id \n" +
            "and si.company_id = :companyId \n" +
            "# NGLSC-2261 and si.is_accepted is true \n" +
            "and si.is_active is true and si.is_deleted is false \n" +
            "and (:fromDate is null or si.acceptance_date >= :fromDate) \n" +
            "and si.acceptance_date <= :toDate and dis.is_active is true \n" +
            "and dis.is_deleted is false\n" +
            "left join sales_return_proposal srp on dis.id = srp.distributor_id \n" +
            "and srp.approval_status='APPROVED' and srp.company_id = :companyId \n" +
            "left join sales_return sr on srp.id = sr.sales_return_proposal_id \n" +
            "and (:fromDate is null or sr.return_date >= :fromDate )\n" +
            "and sr.return_date <= :toDate \n" +
            "left join inv_transaction_details invtdet on sr.inv_transaction_id = invtdet.inv_transaction_id \n" +
            "left join product_trade_price ptp on invtdet.product_id = ptp.product_id and ptp.expiry_date is null\n" +
            "left join credit_debit_note cn on dis.id = cn.distributor_id \n" +
            "and cn.approval_status='APPROVED' and cn.note_type='CREDIT' \n" +
            "and (:fromDate is null or cn.proposal_date >= :fromDate) \n" +
            "and cn.proposal_date <= :toDate and cn.company_id = :companyId \n" +
            "left join credit_debit_note dn on dis.id = dn.distributor_id \n" +
            "and dn.approval_status='APPROVED' and dn.note_type='DEBIT'  \n" +
            "and (:fromDate is null or dn.proposal_date >= :fromDate) \n" +
            "and dn.proposal_date <= :toDate and dn.company_id = :companyId \n" +
            "left join payment_collection pc on dis.id = pc.distributor_id \n" +
            "and pc.company_id = :companyId \n"+
            "and ( :fromDate is null or pc.action_taken_date >= :fromDate ) \n" +
            "and pc.approval_status='APPROVED' and pc.action_taken_date <= :toDate \n" +
            "group by dsom.company_id\n", nativeQuery = true)
    Double getPeriodicLedgerBalanceBySO(
            @Param("salesOfficerId") Long salesOfficerId,
            @Param("companyId") Long companyId, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate); // asOnDate = toDate

    @Query(value = "select distributor.distributor_name as distributorName,distributor.id as distributorId,\t\t\n" +
            "distributor_type.name as distributorType,distributor.is_active as Active\n" +
            "from distributor\n" +
            "INNER JOIN distributor_sales_officer_map ON \n" +
            "distributor.id = distributor_sales_officer_map.distributor_id\n" +
            "and distributor_sales_officer_map.sales_officer_id in(:salesOfficerId)\n" +
            "and distributor_sales_officer_map.company_id = :companyId\n" +
            "INNER JOIN distributor_type ON \n" +
            "distributor_type.id=distributor.distributor_type_id \n" +
            "where distributor_sales_officer_map.to_date is null\n" +
            "AND distributor.is_deleted is false \n" +
            "AND distributor_sales_officer_map.is_active is true and distributor_sales_officer_map.is_deleted is false\n" +
            "AND distributor_type.is_active is true and distributor_type.is_deleted is false order by distributor.id asc;\n", nativeQuery = true)
    List<Map<String, Object>> findList(Long companyId, List<Long> salesOfficerId);

    Optional<Distributor> findByEmail(String email);

    @Query(value = "select distinct(d.id)                      distributor_id\n" +
            "     , d.distributor_name            distributor_name\n" +
            "     , d.contact_no                  contact_no\n" +
            "     , d.ship_to_address                  ship_to_address\n" +
            "     , round(ifnull(dlb.ledger_balance, 0),2) ledger_balance\n" +
            "from distributor d\n" +
            "         inner join distributor_sales_officer_map dsom\n" +
            "                    on d.id = dsom.distributor_id\n" +
            "                        and dsom.company_id = :companyId\n" +
            "                        and dsom.sales_officer_id in :salesOfficerList\n" +
            "                        and d.is_active is true\n" +
            "                        and d.is_deleted is false\n" +
            "                        and dsom.is_active is true\n" +
            "                        and dsom.is_deleted is false\n" +
            "         left join (select lt.distributor_id distributor_id, sum(lt.debit - lt.credit) ledger_balance\n" +
            "from ledger_transaction lt\n" +
            "where lt.company_id = :companyId\n" +
            "group by lt.company_id, lt.distributor_id) as dlb on dlb.distributor_id = d.id", nativeQuery = true)
    List<Map> getDistributorListWithAsOnDateBalanceAndSemesterWiseCreditLimit(@Param("companyId") Long companyId,
                                                                              @Param("salesOfficerList") List<Long> salesOfficerList);

    @Query(value = "select d.id                          distributor_id\n" +
            "     , d.distributor_name            distributor_name\n" +
            "     , d.contact_no                  contact_no\n" +
            "     , d.ship_to_address                  ship_to_address\n" +
            "     , round(ifnull(dlb.ledger_balance, 0),2) ledger_balance\n" +
            "     , count(so.id) order_count\n" +
            "from distributor d\n" +
            "         inner join distributor_sales_officer_map dsom\n" +
            "                    on d.id = dsom.distributor_id\n" +
            "                        and dsom.company_id = :companyId\n" +
            "                        and dsom.sales_officer_id in :salesOfficerList\n" +
            "                        and d.is_active is true\n" +
            "                        and d.is_deleted is false\n" +
            "                        and dsom.is_active is true\n" +
            "                        and dsom.is_deleted is false\n" +
            "inner join sales_booking sb on sb.distributor_id = d.id\n" +
            "inner join sales_order so on sb.id = so.sales_booking_id\n" +
            "and (:startDate is null or so.order_date >= :startDate)\n" +
            "and (:endDate is null or so.order_date <= :endDate)\n" +
            "and so.approval_status ='APPROVED'\n" +
            "left join (select lt.distributor_id distributor_id, sum(lt.debit - lt.credit) ledger_balance\n" +
            "from ledger_transaction lt\n" +
            "where lt.company_id = :companyId\n" +
            "group by lt.company_id, lt.distributor_id) as dlb on dlb.distributor_id = d.id\n" +
            "group by d.id ", nativeQuery = true)
    List<Map> getDistributorListWithOrderInfo(@Param("companyId") Long companyId,
                                              @Param("salesOfficerList") List<Long> salesOfficerList,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    Optional<Distributor> findByOrganizationAndDistributorNameIgnoreCaseAndIsDeletedFalse(Organization organization, String trim);

    Optional<Distributor> findByOrganizationAndIdIsNotAndDistributorNameIgnoreCaseAndIsDeletedFalse(Organization organization, Long id, String trim);

    @Query(value ="select d.id as distributorId, d.distributor_name as distributorName from distributor d  \n" +
            "inner join distributor_sales_officer_map dsom on dsom.distributor_id = d.id \n" +
            "where dsom.company_id = :companyId \n" +
            "and d.is_active is true \n" +
            "and d.is_deleted is false\n" +
            "and dsom.is_active is true \n" +
            "and dsom.is_deleted is false\n",nativeQuery = true)
    List<Map<String, Object>> findDistributorForCreditLimit(@Param("companyId") Long companyId);

    @Query(value ="select d.id as distributorId, d.distributor_name as " +
            "distributorName from distributor d  \n" +
            "inner join distributor_sales_officer_map dsom on dsom.distributor_id = d.id \n" +
            "where dsom.company_id = :companyId \n" +
            "and d.is_active is true \n" +
            "and d.is_deleted is false\n" +
            "and dsom.is_active is true \n" +
            "and dsom.is_deleted is false\n" +
            "and dsom.to_date is null \n" +
            "and (:searchString is null or d.distributor_name LIKE %:searchString%)\n" +
            "and (:distributorId is null or d.id = :distributorId) \n"  +
            "limit 20 \n" , nativeQuery = true)
    List<Map<String, Object>>findDistributorListOfCompany(
            @Param("companyId") Long companyId,
            @Param("searchString") String searchString,
            @Param("distributorId") Long distributorId);

    Optional<Distributor> findByEmailAndIdIsNot(String trim, Long id);

    @Query(value = "select round( IFNULL( sum(lt.debit - lt.credit),0)  , 2) balance\n" +
            "from ledger_transaction lt\n" +
            "where lt.company_id = :companyId\n" +
            "  and lt.distributor_id = :distributorId", nativeQuery = true)
    Float getDistributorLedgerBalanceByCompanyAndDistributor(Long companyId, Long distributorId);

    @Query(value = "select d.id,d.distributor_name distributorName," +
            "d.contact_no contactNo," +
            "d.ship_to_address ship_to_address," +
            "dsom.company_id companyId\n" +
            "from  distributor d\n" +
            "INNER JOIN distributor_sales_officer_map dsom ON dsom.distributor_id = d.id\n" +
            "where dsom.company_id =:companyId\n" +
            "and dsom.sales_officer_id in :salesOfficerIds\n" +
            "and d.is_active is true and d.is_deleted is false \n" +
            "and dsom.is_active is true and dsom.is_deleted is false;", nativeQuery = true)
    List<Map<String, Object>> findDistributorListOfSo(@Param("salesOfficerIds") List<Long> salesOfficerIds,
                                                      @Param("companyId") Long companyId);

    @Query(value ="SELECT distinct(d.id), d.distributor_name as name, d.email FROM distributor_sales_officer_map as dsom\n" +
            "inner join distributor as d on d.id = dsom.distributor_id\n" +
            "where dsom.sales_officer_id in(:salesOfficerIds)\n" +
            "and dsom.company_id = :companyId \n" +
            "and d.is_deleted is false\n" +
            "and dsom.is_active is true and dsom.is_deleted is false group by d.id" , nativeQuery = true)
    List<Map<String, Object>>getDistributorListByCompanyAndSalesOfficerWise(
            @Param("companyId") Long companyId,
            @Param("salesOfficerIds") List<Long> salesOfficerIds);


    @Query(value ="select dsom.distributor_id, dsom.sales_officer_id\n" +
            "from distributor_sales_officer_map as dsom\n" +
            "where dsom.distributor_id = :distributorId\n" +
            "and dsom.company_id = :companyId \n" +
            "and dsom.is_active is true and dsom.is_deleted is false" , nativeQuery = true)
    Map<String, Object>findSoAssignedStatusDistributor(
            @Param("distributorId") Long distributorId,
            @Param("companyId") Long companyId);

    @Query(value ="select sum(ifnull(pc.collection_amount, 0)) as collection_amount, \n" +
            "sum(ifnull(pc.remaining_amount, 0)) as remaining_amount, sum(adjust.adjusted_amount),\n" +
            "case when sum(ifnull(adjust.adjusted_amount, 0)) > 0\n" +
            "then sum(ifnull(pc.collection_amount, 0)) - sum(ifnull(adjust.adjusted_amount, 0))\n" +
            "else sum(ifnull(pc.collection_amount, 0)) end as remaining_advance_amount,\n" +
            "                pc.distributor_id\n" +
            "                from payment_collection pc\n" +
            "                left join (select pa.payment_collection_id, sum(pa.adjusted_amount) adjusted_amount\n" +
            "                from payment_collection_adjustment pa\n" +
            "                where pa.created_date <= :invoiceDate\n" +
            "                and pa.is_deleted is false\n" +
            "                group by pa.payment_collection_id) as adjust\n" +
            "                on pc.id=adjust.payment_collection_id\n" +
            "                where pc.approval_status='APPROVED'\n" +
            "                and pc.distributor_id = :distributorId\n" +
            "                and pc.company_id = :companyId\n" +
            "                and pc.created_date <= :invoiceDate\n" +
            "                and pc.is_deleted is false\n" +
            "                group by pc.distributor_id\n"
            , nativeQuery = true)
    Map<String, Object>getDistributorAdvanceBalance(
            @Param("companyId") Long companyId,
            @Param("distributorId") Long distributorId,
            @Param("invoiceDate") LocalDateTime invoiceDate);

    @Query(value = "select dis.id as Distributor_Id, dis.distributor_name as Distributor_Name\n" +
            "from distributor dis\n" +
            "inner join distributor_sales_officer_map dissom on dis.id = dissom.distributor_id \n" +
            "and dissom.company_id=:companyId and dis.is_active is true and dis.is_deleted is false \n" +
            "and dissom.is_active is true and dissom.is_deleted is false;",nativeQuery = true)
    List<Map<String, Object>> getDistributorListByCompanyId(
            @Param("companyId") Long companyId);

    @Query(value = "select dis.id distributorId, dis.distributor_name as distributorName, \n" +
            "dis.contact_no as contactNo, au.name as salesOfficer, lo_hierarchy.*,\n" +
            "ifnull(ltd.openingBalance, 0) openingBalance, \n" +
            "ifnull(details.debit, 0) debit, \n" +
            "ifnull(details.credit, 0) credit \n" +
            "from distributor dis \n" +
            "left join (select lt.distributor_id, \n" +
            "round(ifnull(sum(lt.debit - lt.credit),0),4) as openingBalance \n" +
            "from ledger_transaction lt where lt.company_id = :companyId \n" +
            "and lt.transaction_date <= :asOnDate group by lt.distributor_id) ltd \n" +
            "on dis.id = ltd.distributor_id \n" +
            "left join (select \n" +
            "distributor_id, sum(debit) debit, sum(credit) credit \n" +
            "from ledger_transaction lt \n" +
            "where company_id= :companyId \n" +
            "and (:fromDate is null or transaction_date >= :fromDate) \n" +
            "and (:thruDate is null or transaction_date <= :thruDate) \n" +
            "group by lt.distributor_id) details \n" +
            "on dis.id = details.distributor_id \n"+
            "inner join distributor_sales_officer_map dis_so\n"+
            "on dis.id = dis_so.distributor_id\n"+
            "and dis_so.to_date is null\n"+
            "and dis_so.company_id = :companyId\n"+
            "and dis_so.is_deleted is false\n"+
            "inner join application_user au on dis_so.sales_officer_id = au.id\n" +
            "inner join reporting_manager rm on dis_so.sales_officer_id = rm.application_user_id\n"+
            "and rm.to_date is null\n"+
            "inner join location_manager_map lmm\n"+
            "on rm.reporting_to_id = lmm.application_user_id\n"+
            "and lmm.to_date is null and lmm.company_id = :companyId\n"+
            "inner join child_location_hierarchy lo_hierarchy\n"+
            "on lmm.location_id = lo_hierarchy.id\n"+
            "and (coalesce(:locationIds) is null or lo_hierarchy.id in (:locationIds))\n"+
            "where dis.is_active is true and dis.is_deleted is false \n" +
            "and (coalesce(:distributorIds) is null or dis.id in (:distributorIds))\n" +
            "and (coalesce(:salesOfficerIds) is null or dis_so.sales_officer_id in (:salesOfficerIds))\n" +
            "order by dis.distributor_name;", nativeQuery = true)
    List<Map<String, Object>> getDistributorsLedgerSummary(
            @Param("salesOfficerIds") List<Long> salesOfficerIds,
            @Param("distributorIds") List<Long> distributorIds,
            @Param("locationIds") List<Long> locationIds,
            @Param("companyId") Long companyId,
            @Param("fromDate") LocalDate fromDate,
            @Param("thruDate") LocalDate thruDate,
            @Param("asOnDate") LocalDate asOnDate);


    @Query(value ="select d.id\n" +
            "from distributor d \n" +
            "inner join distributor_sales_officer_map as dsom on d.id = dsom.distributor_id\n" +
            "where d.distributor_name = :distributorName\n" +
            "and dsom.company_id = :companyId \n" +
            "and d.is_active is true and d.is_deleted is false \n" +
            "and dsom.is_active is true and dsom.is_deleted is false limit 1" , nativeQuery = true)
    Long findByNameAndCompany(
            @Param("distributorName") String distributorName,
            @Param("companyId") Long companyId);

    @Query(value ="SELECT id FROM distributor\n" +
            "where distributor_name LIKE %:distributorCode% \n" +
            "and is_active is true and is_deleted is false \n" , nativeQuery = true)
    List<Long> findByCode(
            @Param("distributorCode") String distributorCode);

}
