package com.newgen.ntlsnc.globalsettings.repository;

import com.newgen.ntlsnc.globalsettings.entity.Location;
import com.newgen.ntlsnc.globalsettings.entity.LocationManagerMap;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author anika
 * @Date ২/৬/২২
 */
@Repository
public interface LocationManagerMapRepository extends JpaRepository<LocationManagerMap, Long> {
    List<LocationManagerMap> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<LocationManagerMap> findByIdAndIsDeletedFalse(Long id);

    @Query(value = "select id, name as locationName from \n" +
            "(select application_user_id, location_id from location_manager_map \n" +
            "where application_user_id = :userLoginId and company_id = :companyId \n" +
            "and to_date is null and is_active is true and is_deleted is false \n"+
            "                           union \n" +
            "select rm.application_user_id, lmm.location_id from reporting_manager rm \n" +
            "left join reporting_manager rmc on rm.application_user_id = rmc.reporting_to_id \n" +
            "inner join location_manager_map lmm on rm.reporting_to_id = lmm.application_user_id \n" +
            "where rmc.application_user_id is null and rm.application_user_id = :userLoginId and \n" +
            "rm.organization_id = :organizationId \n" +
            "and lmm.to_date is null and lmm.company_id =:companyId and lmm.is_active is true and lmm.is_deleted is false \n" +
            "and rm.to_date is null and rm.is_active is true and rm.is_deleted is false) As UL \n" +
            "inner join (select id, name from location \n" +
            "where organization_id = :organizationId and is_active is true \n" +
            "and is_deleted is false) as loc on UL.location_id = loc.id;", nativeQuery = true)
    Map<String, Object> getLoggedInUserLocation(@Param("userLoginId") Long userLoginId,
                                                @Param("organizationId") Long organizationId,
                                                @Param("companyId") Long companyId);


    @Query(value = "CALL SNC_SO_LIST_FROM_MANAGER(:salesOfficerUserId);", nativeQuery = true)
    List<Map<String, Object>> getSalesOfficerListFromSalesManager(@Param("salesOfficerUserId")
                                                                          Long salesOfficerUserId);

    Optional<LocationManagerMap> findByIsActiveTrueAndIsDeletedFalseAndToDateIsNullAndCompanyIdAndLocationId(
            Long companyId, Long LocationId);

    List<LocationManagerMap> findAllByLocationInAndIsActiveTrueAndIsDeletedFalse(List<Location> locationList);


    Optional<LocationManagerMap> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    Optional<LocationManagerMap> findByOrganizationAndCompanyAndApplicationUserAndToDateIsNullAndIsDeletedFalseAndIsActiveTrue(Organization organization, Organization company, ApplicationUser applicationUser);

    Optional<LocationManagerMap> findByOrganizationAndCompanyAndLocationAndToDateIsNullAndIsDeletedFalseAndIsActiveTrue(Organization organization, Organization company, Location location);

    List<LocationManagerMap> findAllByOrganizationAndIsDeletedFalseAndIsActiveTrueAndToDateIsNull(Organization organization);

    List<LocationManagerMap> findAllByOrganizationAndCompanyAndIsDeletedFalseAndIsActiveTrueAndToDateIsNull(Organization organization, Organization company);

    Optional<LocationManagerMap> findByOrganizationAndCompanyAndApplicationUserIdAndToDateIsNullAndIsDeletedFalseAndIsActiveTrue(
            Organization organization, Organization company, Long applicationUserId);


}
