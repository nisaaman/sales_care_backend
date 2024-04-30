package com.newgen.ntlsnc.usermanagement.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUserCompanyMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author anika
 * @Date ২০/৬/২২
 */
@Repository
public interface ApplicationUserCompanyMappingRepository extends JpaRepository<ApplicationUserCompanyMapping, Long> {
//    List<ApplicationUserCompanyMapping> findAllByIsDeletedFalse();

    List<ApplicationUserCompanyMapping> findAllByOrganizationAndIsDeletedFalse(Organization organization);
    List<ApplicationUserCompanyMapping> findAllByOrganizationAndApplicationUserAndIsDeletedFalse(Organization organization, ApplicationUser applicationUser);

    Optional<ApplicationUserCompanyMapping> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    List<ApplicationUserCompanyMapping> findAllByOrganizationAndApplicationUserAndIsActiveTrueAndIsDeletedFalse(Organization organization, ApplicationUser applicationUser);
    List<ApplicationUserCompanyMapping> findAllByOrganizationAndCompanyInAndIsActiveTrueAndIsDeletedFalse(Organization organization, List<Organization> companyList);

    Optional<ApplicationUserCompanyMapping> findByCompanyAndApplicationUserAndIsActiveTrueAndIsDeletedFalse(Organization company, ApplicationUser user);

    @Query(value = "SELECT aucm.id, aucm.company_id, aucm.user_id, au.name, au.email\n" +
            "FROM  application_user_company_mapping aucm\n" +
            "inner join application_user au on au.id = aucm.user_id\n" +
            "where aucm.company_id in(:companyIds) \n" +
            "and au.is_active is true \n" +
            "and au.is_deleted is false\n" +
            "and aucm.is_active is true \n" +
            "and aucm.is_deleted is false", nativeQuery = true)
    List<Map<String, Object>> findAllUserByCompanyWise(@Param("companyIds") List<Long> companyIds);
}
