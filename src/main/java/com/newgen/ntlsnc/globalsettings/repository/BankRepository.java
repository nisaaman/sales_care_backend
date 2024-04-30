package com.newgen.ntlsnc.globalsettings.repository;

import com.newgen.ntlsnc.globalsettings.entity.Bank;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author kamal
 */

@Repository
public interface BankRepository extends JpaRepository<Bank, Long> {
    List<Bank> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<Bank> findByIdAndIsDeletedFalse(Long id);

    Optional<Bank> findByBankShortNameAndIsDeletedFalse(String shortName);

    Optional<Bank> findByOrganizationAndNameIgnoreCaseAndIsDeletedFalse(Organization organization, String name);

    Optional<Bank> findByOrganizationAndIdIsNotAndNameIgnoreCaseAndIsDeletedFalse(Organization organization, Long id, String name);

    List<Bank> findAllByOrganizationAndIsActiveTrueAndIsDeletedFalse(Organization organization);

}
