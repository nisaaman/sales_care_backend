package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.salesandcollection.entity.Distributor;
import com.newgen.ntlsnc.salesandcollection.entity.DistributorSalesOfficerMap;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author Newaz Sharif
 * @since 21st June,2022
 */
public interface DistributorSalesOfficerMapRepository extends JpaRepository<DistributorSalesOfficerMap,Long> {

    @Query(value = "select distributor_id from distributor_sales_officer_map \n" +
            "where sales_officer_id in(:salesOfficers) and company_id = :companyId \n" +
            "and to_date is null and is_active is true and is_deleted is false", nativeQuery = true)
    List<Long> findAllDistributorBySalesOfficers(List<Long> salesOfficers, Long companyId);

    List<DistributorSalesOfficerMap> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    List<DistributorSalesOfficerMap> findAllBySalesOfficerInAndIsActiveTrueAndIsDeletedFalse(List<ApplicationUser> salesOfficerList);

    List<DistributorSalesOfficerMap> findAllByCompanyIdAndSalesOfficerInAndIsActiveTrueAndIsDeletedFalse(Long companyId, List<ApplicationUser> salesOfficerList);

    List<DistributorSalesOfficerMap> findByIsActiveTrueAndIsDeletedFalseAndSalesOfficerIdAndCompanyId(Long salesOfficerUserId, Long companyId);

    List<DistributorSalesOfficerMap> findAllByDistributorAndIsActiveTrueAndIsDeletedFalse(Distributor distributor);

    List<DistributorSalesOfficerMap> findAllByDistributorAndToDateIsNullAndIsActiveTrueAndIsDeletedFalse(Distributor distributor);

    List<DistributorSalesOfficerMap> findByCompanyIdAndSalesOfficerIdAndIsActiveTrueAndIsDeletedFalse(Long companyId, Long salesOfficerId);
    List<DistributorSalesOfficerMap> findBySalesOfficerIdAndIsActiveTrueAndIsDeletedFalse(Long salesOfficerId);
}
