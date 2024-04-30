package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.salesandcollection.entity.CreditLimitProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Marziah
 */
@Repository
public interface CreditLimitProposalRepository extends JpaRepository<CreditLimitProposal, Long> {
    List<CreditLimitProposal> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<CreditLimitProposal> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    @Query(value = "select clp.id as proposal_id, \n" +
            "dsom.distributor_id, \n" +
            "d.distributor_name,\n" +
            "au.id as application_user_id,\n" +
            "au.organization_id, \n" +
            "au.name as sales_officer, \n" +
            "des.name as designation,  \n" +
            "clp.proposed_amount,\n" +
            "clp.business_plan businessPlan,\n" +
            "clp.current_limit currentLimit,\n" +
            "clp.approval_status,\n" +
            "DATE_FORMAT(clp.created_date, '%d/%m/%Y') as date,\n" +
            "DATE_FORMAT(clp.created_date, '%Y-%m-%d') as createdDate,\n" +
            "DATE_FORMAT(clp.created_date, '%b %Y') AS monthAndYear \n" +
            "from credit_limit_proposal as clp\n" +
            "inner join distributor_sales_officer_map as dsom \n" +
            "\ton dsom.distributor_id = clp.distributor_id\n" +
            "    and dsom.sales_officer_id in(:salesOfficerId)\n" +
            "    and dsom.company_id = :companyId\n" +
            "\tand dsom.is_active is true \n" +
            "    and dsom.is_deleted is false\n" +
            "inner join distributor as d \n" +
            "\ton d.id = dsom.distributor_id\n" +
            "\tand d.is_active is true \n" +
            "    and d.is_deleted is false\n" +
            "inner join application_user as au \n" +
            "\ton au.id = dsom.sales_officer_id\n" +
            "    and au.is_active is true \n" +
            "    and au.is_deleted is false\n" +
            "inner join designation as des \n" +
            "\ton des.id = au.designation_id\n" +
            "    and des.is_active is true \n" +
            "    and des.is_deleted is false\n" +
            "where clp.company_id = :companyId \n" +
            "\tand ( :startDate is null or clp.created_date >= :startDate )\n" +
            "    and ( :endDate is null or clp.created_date <= :endDate )-- \n" +
            "\tand clp.approval_status IN (:approvalStatusList)\n" +
            "\tand clp.is_active is true \n" +
            "    and clp.is_deleted is false;", nativeQuery = true)
    List<Map<String, Object>> findList(Long companyId, List<Long> salesOfficerId, LocalDate startDate, LocalDate endDate, List<String> approvalStatusList);
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
            "from credit_limit_proposal as clp\n" +
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

    @Query(value = "select ifnull(max(t.pending),0) as pending,\n" +
            "       ifnull(max(t.approved),0) as approved,\n" +
            "       ifnull(max(t.rejected),0) as rejected\n" +
            "from(select case clp.approval_status when 'PENDING' then count(clp.approval_status) else 0 end as pending,\n" +
            "        case clp.approval_status when 'APPROVED' then count(clp.approval_status) else 0 end as approved,\n" +
            "        case clp.approval_status when 'REJECTED' then count(clp.approval_status) else 0 end rejected\n" +
            "    from credit_limit_proposal clp\n" +
            "    where clp.is_active is true\n" +
            "    and clp.is_deleted is false\n" +
            "    and clp.company_id = :companyId\n" +
            "    and clp.created_by = :userId\n" +
            "    group by approval_status\n" +
            ") as t;", nativeQuery = true)
    Map<String, Object> getProposalStatusWiseCount(
            @Param("companyId") Long companyId,
            @Param("userId") Long userId);
}
