package com.newgen.ntlsnc.globalsettings.repository;

import com.newgen.ntlsnc.globalsettings.entity.BankAccount;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Mou
 * Created on 6/3/22 10:05 AM
 */
@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByIdAndIsDeletedFalse(Long id);

    List<BankAccount> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<BankAccount> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    List<BankAccount> findByBranchIdAndIsDeletedFalse(Long branchId);

    Optional<BankAccount> findByOrganizationAndAccountNumberIgnoreCaseAndIsDeletedFalse(Organization organization, String accountNumber);

    Optional<BankAccount> findByOrganizationAndIdIsNotAndAccountNumberIgnoreCaseAndIsDeletedFalse(Organization organization, Long id, String accountNumber);

    List<BankAccount> findAllByOrganizationAndIsDeletedFalseAndBranchIsActiveIsTrue(Organization organization);

    List<BankAccount> findAllByOrganizationAndBranchIdAndIsDeletedFalseAndIsActiveTrue(Organization organization,Long branchId);
}
