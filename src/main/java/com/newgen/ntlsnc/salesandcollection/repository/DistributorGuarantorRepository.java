package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.salesandcollection.entity.Distributor;
import com.newgen.ntlsnc.salesandcollection.entity.DistributorGuarantor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Marziah
 * Created on 5/4/22 10:55 AM
 */
@Repository
public interface DistributorGuarantorRepository extends JpaRepository<DistributorGuarantor, Long> {
    List<DistributorGuarantor> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<DistributorGuarantor> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    List<DistributorGuarantor> findAllByDistributorAndIsActiveTrueAndIsDeletedFalse(Distributor distributor);
}
