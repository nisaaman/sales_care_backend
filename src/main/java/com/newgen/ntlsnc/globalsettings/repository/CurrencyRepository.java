package com.newgen.ntlsnc.globalsettings.repository;
import com.newgen.ntlsnc.globalsettings.entity.Currency;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author kamal
 */

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    List<Currency> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<Currency> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    Optional<Currency> findByOrganizationAndNameIgnoreCaseAndIsDeletedFalse(Organization organization, String name);

    Optional<Currency> findByOrganizationAndIdIsNotAndNameIgnoreCaseAndIsDeletedFalse(Organization organization, Long id, String name);
}
