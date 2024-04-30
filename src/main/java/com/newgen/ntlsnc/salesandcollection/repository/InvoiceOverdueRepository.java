package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.salesandcollection.entity.InvoiceOverdue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author kamal
 * @Date ২৪/৮/২২
 */

@Repository
public interface InvoiceOverdueRepository extends JpaRepository<InvoiceOverdue, Long> {

    List<InvoiceOverdue> findAllByCompanyIdAndIsActiveTrueAndIsDeletedFalse(Long companyId);

    @Modifying
    @Query("update InvoiceOverdue d set d.isDeleted = true where d.company.id =:companyId ")
    void deactivateAllByCompanyId(Long companyId);
}
