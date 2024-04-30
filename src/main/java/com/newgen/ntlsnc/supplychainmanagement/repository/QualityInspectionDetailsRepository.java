package com.newgen.ntlsnc.supplychainmanagement.repository;

import com.newgen.ntlsnc.supplychainmanagement.entity.QualityInspectionDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Newaz Sharif
 * @since 30th Oct, 22
 */
@Repository
public interface QualityInspectionDetailsRepository extends JpaRepository<QualityInspectionDetails, Long> {
}
