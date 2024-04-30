package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.salesandcollection.entity.CollectionBudgetDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Newaz Sharif
 * @since 28th July,22
 */
@Repository
public interface CollectionBudgetDetailsRepository extends JpaRepository<CollectionBudgetDetails,Long> {

    @Query(value = "select(case when cbd.month is NULL then false\n" +
            "else true end) as budget from collection_budget cb \n" +
            "inner join collection_budget_details cbd on cb.id = cbd.collection_budget_id \n" +
            "and cb.accounting_year_id = :accountingYearId and cb.company_id = :companyId \n" +
            "and cb.approval_status = \"APPROVED\" \n" +
            "and cb.is_active is true and cb.is_deleted is false and cbd.is_active is true \n" +
            "and cbd.is_deleted is false limit 1; \n",nativeQuery = true)
    Object isItMonthlyCollectionBudget(Long companyId, Long accountingYearId);

    @Transactional
    long deleteByCollectionBudgetId(Long collectionBudgetId);
    List<CollectionBudgetDetails> findAllByOrganizationAndIsDeletedFalse(Organization organization);

}
