package com.newgen.ntlsnc.usermanagement.repository;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, Long> {
    List<ApplicationUser> findAllByIsDeletedFalse();

    Optional<ApplicationUser> findByEmailAndIsAccountNonExpiredTrueAndIsAccountNonLockedTrueAndIsCredentialsNonExpiredTrueAndIsEnabledTrueAndIsActiveTrue(String username);
    List<ApplicationUser> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    List<ApplicationUser> findAllByOrganizationAndIsDeletedFalseAndIsActiveTrue(Organization organization);

    @Query(value = "SELECT rm.application_user_id sales_officer " +
            "FROM reporting_manager rm " +
            "WHERE rm.reporting_to_id= :manager_id " +
            "AND rm.to_date is null " +
            "AND rm.is_active is true and rm.is_deleted is false \n", nativeQuery = true)
    List<Long> getSoListByLocationManager(@Param("manager_id") Long managerId);


    @Query(value = "SELECT rm.application_user_id salesOfficerId, concat(au.name,\", \",\n" +
            "    au.email,\", \",d.name,\", \",l.name) salesOfficerName\n" +
            "    FROM reporting_manager rm \n" +
            "    INNER JOIN application_user au ON au.id=rm.application_user_id\n" +
            "    INNER JOIN designation d ON d.id=au.designation_id\n" +
            "    INNER JOIN location_manager_map lmm ON lmm.application_user_id = rm.reporting_to_id\n" +
            "    INNER JOIN location l ON l.id = lmm.location_id\n" +
            "    WHERE l.id= :locationId \n" +
            "    AND lmm.company_id=:companyId\n" +
            "    AND rm.to_date is null \n" +
            "    AND rm.is_active is true and rm.is_deleted is false\n" +
            "    AND lmm.is_active is true and lmm.is_deleted is false\n" +
            "    AND lmm.to_date is null\n" +
            "    AND d.is_active is true and d.is_deleted is false\n" +
            "    AND l.is_active is true and l.is_deleted is false\n" +
            "    AND au.is_active is true and l.is_deleted is false;", nativeQuery = true)
    List<Map<String, Object>> getSoDetailsByLocationManager(@Param("locationId") Long locationId, @Param("companyId") Long companyId);

    List<ApplicationUser> findByDepartmentIdAndIsDeletedFalse(Long departmentId);

    Optional<ApplicationUser> findByOrganizationAndNameIgnoreCaseAndIsDeletedFalse(Organization organization, String name);

    Optional<ApplicationUser> findByOrganizationAndIdIsNotAndNameIgnoreCaseAndIsDeletedFalse(Organization organization, Long id, String name);

    Optional<ApplicationUser> findByOrganizationAndEmailIgnoreCase(Organization organization, String email);

    Optional<ApplicationUser> findByOrganizationAndIdIsNotAndEmailIgnoreCase(Organization organization, Long id, String email);

    Optional<ApplicationUser> findByOrganizationAndIdIsNotAndMobile(
            Organization organization, Long id, String contactNo);

    Optional<ApplicationUser> findByIdAndIsDeletedFalse(Long id);

    @Query(value = "select * from application_user au\n" +
            "inner join application_user_company_mapping aucm\n" +
            "\t\t\t\ton au.id=aucm.user_id\n" +
            "\t\t\t\tand au.is_active is true\n" +
            "\t\t\t\tand au.is_deleted is false\n" +
            "\t\t\t\tand aucm.is_active is true\n" +
            "\t\t\t\tand aucm.is_deleted is false\n" +
            "        and aucm.company_id=:companyId" , nativeQuery = true)
    List<ApplicationUser> getByCompany( Long companyId);

    List<ApplicationUser> findAllByDesignationIdAndIsDeletedFalse(Long designationId);

    List<ApplicationUser> findAllByOrganizationAndDesignationIdAndIsDeletedFalseAndIsActiveTrue(Organization organization, Long designationId);

    @Query(value = "select * from application_user au\n" +
            "       inner join application_user_company_mapping aucm\n" +
            "       on au.id=aucm.user_id \n" +
            "       and au.is_active is true \n" +
            "       and au.is_deleted is false \n" +
            "       and aucm.is_active is true \n" +
            "       and aucm.is_deleted is false \n" +
            "       and aucm.company_id=:companyId \n" +
            "       where au.id not in(SELECT application_user_id \n" +
            "           FROM reporting_manager \n" +
            "           where to_date is null \n" +
            "           and is_active is true \n" +
            "           and is_deleted is false \n" +
            "           and reporting_to_id is null)" , nativeQuery = true)
    List<ApplicationUser> getByCompanyWithoutZonalManager( Long companyId);

    List<ApplicationUser> findAllByOrganizationAndDepartmentIdAndIsDeletedFalseAndIsActiveTrue(Organization organization, Long departmentId);

    @Query(value ="select * from application_user au where au.name LIKE %:searchString% \n" +
            "and au.is_active is true and au.is_deleted is false;" ,nativeQuery = true)
    List<Map<String, Object>>searchUser(@Param("searchString") String searchString);

       @Query(value = "Select id,concat(name,'-[',email,']') as salesOfficerName from application_user  where id in :soIds",nativeQuery = true)
      List<Map<String, Object>>  findAllSO(List<Long> soIds);

    Optional<ApplicationUser> findByOrganizationAndMobile(Organization organization, String trim);

    @Query(value = "SELECT distinct(au.id), au.name, au.email FROM location_manager_map as lmp\n" +
            "inner join reporting_manager as rm on rm.reporting_to_id = lmp.application_user_id\n" +
            "#and rm.to_date is null\n" +
            "#and lmp.to_date is null\n" +
            "inner join application_user as au on au.id = rm.application_user_id\n" +
            "where (0=:len OR lmp.location_id in(:locationIds))\n" +
            "and lmp.company_id = :companyId\n" +
            "and au.is_deleted is false\n" +
            "and rm.is_deleted is false\n" +
            "and lmp.is_active is true and lmp.is_deleted is false group by au.id", nativeQuery = true)
    List<Map<String, Object>> getSalesOfficerByLocationWise( Long companyId, List<Long> locationIds, int len);
}
