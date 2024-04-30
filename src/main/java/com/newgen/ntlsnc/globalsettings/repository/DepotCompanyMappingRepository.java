package com.newgen.ntlsnc.globalsettings.repository;

import com.newgen.ntlsnc.globalsettings.entity.DepotCompanyMapping;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author anika
 * @Date ২০/৬/২২
 */
@Repository
public interface DepotCompanyMappingRepository extends JpaRepository<DepotCompanyMapping, Long> {
    List<DepotCompanyMapping> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<DepotCompanyMapping> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);
}
