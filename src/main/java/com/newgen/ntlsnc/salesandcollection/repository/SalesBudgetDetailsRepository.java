package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.salesandcollection.entity.SalesBudgetDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author anika
 * @Date ১৩/৪/২২
 */
@Repository
public interface SalesBudgetDetailsRepository extends JpaRepository<SalesBudgetDetails, Long> {
    @Modifying
    @Query(value = "UPDATE month_wise_sales_and_collection_budget_details SET is_deleted = true WHERE month_wise_sales_and_collection_budget_id = :masterId", nativeQuery = true)
    void deleteAllByMasterId(@Param("masterId") Long masterId);
    @Transactional
    long deleteBySalesBudgetId(Long salesBudgetId);
    List<SalesBudgetDetails> findAllByOrganizationAndIsDeletedFalse(Organization organization);
}
