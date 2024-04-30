package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.salesandcollection.entity.Distributor;
import com.newgen.ntlsnc.salesandcollection.entity.DistributorBalance;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Sunipa
 * @since 2nd Aug, 23
 */

@Repository
public interface DistributorBalanceRepository extends CrudRepository<DistributorBalance, Long> {

    List<DistributorBalance> findAllByOrganizationAndIsDeletedFalse(Organization organization);
    Optional<DistributorBalance> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    @Query(value = "SELECT db.id FROM distributor_balance db\n" +
            " WHERE NOT EXISTS (select * from payment_collection_adjustment pca where pca.distributor_balance_id = db.id \n" +
            " and pca.is_active is true and pca.is_deleted is false) \n" +
            "and db.distributor_id = :distributorId and db.company_id = :companyId ",nativeQuery = true)
    List<Long> getNotUsedListByDistributorIdAndCompanyId(
            @Param("distributorId") Long distributorId, @Param("companyId") Long companyId);

    List<DistributorBalance> findByIdIn(List<Long> distributorBalanceIds);

    boolean existsByDistributorAndCompany(Distributor distributor, Organization company);
}
