package com.newgen.ntlsnc.globalsettings.repository;
import com.newgen.ntlsnc.globalsettings.entity.InvoiceNature;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author kamal
 */

@Repository
public interface InvoiceNatureRepository extends JpaRepository<InvoiceNature, Long> {
    List<InvoiceNature> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<InvoiceNature> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);
}
