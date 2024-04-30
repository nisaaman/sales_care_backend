package com.newgen.ntlsnc.supplychainmanagement.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvClaim;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvDamage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author sunipa
 * @date ২১/৯/২২
 */
@Repository
public interface InvClaimRepository extends JpaRepository<InvClaim, Long> {
    List<InvClaim> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<InvClaim> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    Optional<InvClaim> findByInvTransactionIdAndIsActiveTrueAndIsDeletedFalse(Long id);
}
