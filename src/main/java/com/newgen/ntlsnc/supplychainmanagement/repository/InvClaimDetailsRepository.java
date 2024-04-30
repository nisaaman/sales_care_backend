package com.newgen.ntlsnc.supplychainmanagement.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvClaim;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvClaimDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author sunipa
 * @date ২১/৯/২২
 */
@Repository
public interface InvClaimDetailsRepository extends JpaRepository<InvClaimDetails, Long> {
    List<InvClaimDetails> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<InvClaimDetails> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);
}
