package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.salesandcollection.entity.SalesReturnAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author sunipa
 * @date ৮/৬/২৩
 */
@Repository
public interface SalesReturnAdjustmentRepository extends JpaRepository<SalesReturnAdjustment, Long> {
    List<SalesReturnAdjustment> findAllByIsDeletedFalse();
}
