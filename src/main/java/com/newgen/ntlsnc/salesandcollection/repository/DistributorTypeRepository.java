package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.salesandcollection.entity.DistributorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author anika
 * @Date ২৫/৫/২২
 */

@Repository
public interface DistributorTypeRepository extends JpaRepository<DistributorType, Long> {
    List<DistributorType> findAllByOrganizationAndIsDeletedFalse(Organization organization);


    Optional<DistributorType> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    Optional<DistributorType> findByNameAndIsActiveTrueAndIsDeletedFalse(String name);
}
