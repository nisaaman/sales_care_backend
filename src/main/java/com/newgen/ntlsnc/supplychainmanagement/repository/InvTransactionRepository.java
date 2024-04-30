package com.newgen.ntlsnc.supplychainmanagement.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author kamal
 * @Date ১৭/৪/২২
 */

@Repository
public interface InvTransactionRepository extends JpaRepository<InvTransaction, Long> {
    List<InvTransaction> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<InvTransaction> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);
}
