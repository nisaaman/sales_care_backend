package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.salesandcollection.entity.DistributorCompanyMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author anika
 * @Date ২২/৬/২২
 */
@Repository
public interface DistributorCompanyMapRepository extends JpaRepository<DistributorCompanyMap, Long> {
    List<DistributorCompanyMap> findAllByOrganizationAndIsDeletedFalse(Organization organization);
    Optional<DistributorCompanyMap> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    @Query(value = "select distributor_id from distributor_sales_officer_map \n" +
            "where company_id = :companyId \n" +
            "and is_active is true and is_deleted is false", nativeQuery = true)
    List<Long> getAllDistributorByCompany(Long companyId);
}
