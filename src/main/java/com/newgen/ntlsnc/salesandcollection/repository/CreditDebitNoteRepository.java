package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.salesandcollection.entity.CreditDebitNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author anika
 * @date ৩০/৫/২২
 */
@Repository
public interface CreditDebitNoteRepository extends JpaRepository<CreditDebitNote, Long> {
    List<CreditDebitNote> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<CreditDebitNote> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    @Query(value = "select n.id                                                      credit_debit_note_id\n" +
            "     , DATE_FORMAT(n.proposal_date, '%d %b %Y')                  proposal_date\n" +
            "     , n.note_no                                                 note_no\n" +
            "     , n.reason                                                  reason\n" +
            "     , case when n.note_type = 'DEBIT' then n.amount else 0 end  debit\n" +
            "     , case when n.note_type = 'CREDIT' then n.amount else 0 end credit\n" +
            "from credit_debit_note n\n" +
            "where n.company_id = :companyId \n" +
            "  and n.distributor_id = :distributorId \n" +
            "  and n.is_active is true\n" +
            "  and n.is_deleted is false\n" +
            "  and n.proposal_date between :fromDate and :toDate \n" +
            "order by n.proposal_date desc", nativeQuery = true)
    List<Map> getAllByCompanyAndDistributorAndDateRange(Long companyId, Long distributorId, String fromDate, String toDate);

    @Query(value = "select cdn.id                                              credit_debit_note_id\n" +
            "     , d.id                                                distributor_id\n" +
            "     , d.contact_no                                        contact_no\n" +
            "     , cdn.note_no                                         note_no\n" +
            "     , cdn.approval_status                                 approval_status\n" +
            "     , cdn.amount                                          amount\n" +
            "     , cdn.note_type                                       note_type\n" +
            "     , cdn.reason                                          reason\n" +
            "     , cdn.note                                            note\n" +
            "     , cdn.company_id                                      company_id\n" +
            "     , DATE_FORMAT(cdn.proposal_date, '%d-%b-%Y %l:%i %p') proposal_date\n" +
            "     , DATE_FORMAT(cdn.created_date, '%d-%b-%Y %l:%i %p')  note_created_date\n" +
            "     , DATE_FORMAT(cdn.approval_date, '%d-%b-%Y %l:%i %p') approval_date\n" +
            "     , DATEDIFF(now(), cdn.approval_date) as               day_diff\n" +
            "     , d.distributor_name                                  distributor_name\n" +
            "     , au.name                                             entry_by\n" +
            "     , d2.name                                             entry_by_designation\n" +
            "     , o.name                                              company_name\n" +
            "     , s.invoice_no                                        invoice_no\n" +
            "     , dlb.ledger_balance                                  ledger_balance\n" +
            "from credit_debit_note cdn\n" +
            "         inner join distributor d\n" +
            "                    on cdn.distributor_id = d.id\n" +
            "                        and cdn.company_id = :companyId\n" +
            "                        and cdn.is_active is true\n" +
            "                        and cdn.is_deleted is false and d.is_active is true\n" +
            "                        and d.is_deleted is false\n" +
            "                        and (:noteType is null or cdn.note_type = :noteType)\n" +
            "                        and (:status is null or cdn.approval_status = :status)\n" +
            "                        and (:fromDate is null or cdn.proposal_date between :fromDate and :toDate)\n" +
            "         left join sales_invoice s\n" +
            "                   on cdn.invoice_id = s.id\n" +
            "                       and s.is_active is true\n" +
            "                       and s.is_deleted is false\n" +
            "         inner join distributor_sales_officer_map dsm\n" +
            "                    on d.id = dsm.distributor_id\n" +
            "                        and dsm.is_deleted is false\n" +
            "                        and dsm.company_id = :companyId\n" +
            "         inner join reporting_manager rm\n" +
            "                    on dsm.sales_officer_id = rm.application_user_id\n" +
            "                        and rm.is_active is true\n" +
            "                        and rm.is_deleted is false\n" +
            "                        and rm.to_date is null\n" +
            "         inner join location_manager_map lmm\n" +
            "                    on rm.reporting_to_id = lmm.application_user_id\n" +
            "                        and lmm.is_deleted is false\n" +
            "                        and lmm.is_active is true\n" +
            "                        and lmm.to_date is null and lmm.company_id =:companyId\n" +
            "         inner join location l\n" +
            "                    on lmm.location_id = l.id\n" +
            "                        and l.is_active is true\n" +
            "                        and l.is_deleted is false\n" +
            "                        and (:locationId is null or l.id = :locationId)\n" +
            "         left join organization o\n" +
            "                   on o.id = cdn.company_id\n" +
            "         left join application_user au\n" +
            "                   on cdn.created_by = au.id\n" +
            "         inner join designation d2\n" +
            "                    on au.designation_id = d2.id\n" +
            "         left join (select lt.distributor_id distributor_id, sum(lt.debit - lt.credit) ledger_balance\n" +
            "from ledger_transaction lt\n" +
            "where lt.company_id = :companyId\n" +
            "group by lt.company_id, lt.distributor_id) as dlb on dlb.distributor_id = d.id\n" +
            "order by cdn.created_by desc", nativeQuery = true)
    List<Map> getAllWithDistributorBalanceByCompanyAndNoteTypeAndApprovalStatusAndLocationAndDateRange(Long companyId, Long locationId, String noteType, String status, String fromDate, String toDate);

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
            "     , @var\\:=:approvalStatusName approvalStatus \n" +
            "     , @var\\:=:approvalStepName approvalStepName \n" +
            " from credit_debit_note cdn\n" +
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
    List<Map<String, Object>> getPendingListForApproval(@Param("companyId") Long companyId,
                                                        @Param("approvalStatus") String approvalStatus,
                                                        @Param("approvalActor") String approvalActor,
                                                        @Param("level") Integer level,
                                                        @Param("approvalStepId") Long approvalStepId,
                                                        @Param("multiLayerApprovalPathId") Long multiLayerApprovalPathId,
                                                        @Param("approvalActorId") Long approvalActorId,
                                                        @Param("distributorList") List<Long>distributorList,
                                                        @Param("approvalStatusName") String approvalStatusName,
                                                        @Param("approvalStepName") String approvalStepName);

}
