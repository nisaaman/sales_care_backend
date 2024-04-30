package com.newgen.ntlsnc.globalsettings.repository;

import com.newgen.ntlsnc.globalsettings.entity.Location;
import com.newgen.ntlsnc.globalsettings.entity.LocationType;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author mou
 * Created on 4/3/22 11:00 AM
 */

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findAllByIdInAndIsDeletedFalse(List<Long> ids);
    List<Location> findAllByOrganizationAndIsDeletedFalse(Organization organization);
    List<Location> findAllByIsDeletedFalseAndIdNotIn(List<Long> ids);
    List<Location> findAllByIsDeletedFalseAndIsActiveTrueAndIdIn(List<Long> ids);

    @Query(value = "select l.id as id, l.name as name, map.company_id companyId, org.name companyName\n" +
            "from depot_location_map map\n" +
            "         inner join location l on map.location_id = l.id \n" +
            "and l.is_active = true and l.is_deleted = false\n" +
            "         inner join organization org on map.company_id = org.id \n" +
            "and org.is_active = true and org.is_deleted = false\n" +
            "where map.depot_id = :depotId \n" +
            "and map.is_active = true and map.is_deleted = false", nativeQuery = true)
    List<Map> getLocationsByDepotId(@Param("depotId") Long depotId);


    @Query(value = "SELECT l " +
            "FROM Location l " +
            "WHERE l.parent= :parent_id \n" +
            "AND l.isActive is true AND l.isDeleted is false \n")
    List<Location> getLocationsByParentId(@Param("parent_id") Location parent);


    @Query(value = "SELECT l " +
            "FROM Location l " +
            "WHERE l.parent IS NULL \n" +
            "AND l.isActive is true AND l.isDeleted is false \n")
    List<Location> getParentLocationsList();

    @Query(value = "SELECT lmm.location_id " +
            "FROM location_manager_map lmm " +
            "WHERE lmm.application_user_id= :application_user_id \n" +
            "AND lmm.company_id = :company_id \n" +
            "AND lmm.to_date is null \n" +
            "AND lmm.is_active is true AND lmm.is_deleted is false \n", nativeQuery = true)
    Long getManagerLocation(@Param("company_id") Long companyId,
                            @Param("application_user_id") Long managerId);

    @Query(value = "SELECT lmm.application_user_id manager_id " +
            "FROM location_manager_map lmm " +
            "WHERE lmm.location_id= :location_id \n" +
            "AND lmm.company_id = :company_id and lmm.to_date is null \n" +
            "AND lmm.is_active is true AND lmm.is_deleted is false \n", nativeQuery = true)
    Long getLocationManager(@Param("company_id") Long companyId,
                            @Param("location_id") Long locationId);

    List<Location> findAllByIsDeletedFalseAndLocationTypeInOrderByParent(List<LocationType> locationTypeList);

    List<Location> findAllByLocationTypeAndIsDeletedFalseAndIsActiveTrueOrderByIdAsc(LocationType locationType);

    @Query(value = "select l from Location l where l.isActive = true and l.isDeleted is false and l.locationType = :locationType and l.id in :idList ")
    List<Location> findAllByLocationTypeAndLocationIdInAndIsDeletedFalseAndIsActiveTrue(LocationType locationType, List<Long> idList);

    @Query(value = "CALL SNC_SO_LOCATION_TREE(:companyId, :salesOfficerUserId);", nativeQuery = true)
    List<Map<String, Object>> getSalesOfficerLocationTree(
            @Param("companyId") Long companyId,
            @Param("salesOfficerUserId") Long salesOfficerUserId);

    List<Location> findByOrganizationIdAndIsDeletedFalseAndIsActiveTrue(Long organizationId);

    @Query(value = "select l from Organization org inner join LocationTree lt \n" +
                   "on org.locationTree.id = lt.id inner join LocationType lty \n" +
                   "on lt.id = lty.locationTree.id inner join Location l \n" +
                   "on lty.id = l.locationType.id where org.id = :companyId \n" +
                   "and org.locationTree.isActive is true and org.locationTree.isDeleted is false \n")
    List<Location> getCompanyLocationTree(@Param("companyId") Long companyId);

    @Query(value = "SELECT lo.id, lo.name location_name, lo.location_type_id, \n" +
            "lt.name location_type, lt.level \n" +
            "FROM reporting_manager rm \n" +

            "inner join location_manager_map lmm \n" +
            "on rm.reporting_to_id = lmm.application_user_id \n" +
            "and lmm.to_date is null \n" +
            "and lmm.company_id = :company_id \n" +

            "inner join location lo " +
            "on lmm.location_id = lo.id \n" +

            "inner join location_type lt " +
            "on lo.location_type_id = lt.id \n" +

            "WHERE rm.application_user_id = :application_user_id \n" +
            "AND rm.to_date is null \n" +
            "AND rm.is_active is true AND rm.is_deleted is false \n" , nativeQuery = true)
    Map getSoLocation(@Param("company_id") Long companyId,
                       @Param("application_user_id") Long salesOfficerId);

    @Query(value = "SELECT lo.id \n" +
            "FROM reporting_manager rm \n" +
            "inner join location_manager_map lmm \n" +
            "on rm.reporting_to_id = lmm.application_user_id \n" +
            "and lmm.to_date is null \n" +
            "and lmm.company_id = :company_id \n" +
            "inner join location lo " +
            "on lmm.location_id = lo.id \n" +
            "inner join location_type lt " +
            "on lo.location_type_id = lt.id \n" +
            "WHERE rm.application_user_id = :application_user_id \n" +
            "AND rm.to_date is null \n" +
            "AND rm.is_active is true AND rm.is_deleted is false \n" , nativeQuery = true)
    Long getSoLocationId(@Param("company_id") Long companyId,
                      @Param("application_user_id") Long salesOfficerId);

    @Query(value = "select parent " +
            "from Location child " +
            "         inner join Location parent on child.parent = parent " +
            "         inner join LocationType lt on lt.id = parent.locationType.id " +
            "where child.id = :locationId")
    Optional<Location> findParentLocationByChildLocation(@Param("locationId") Long locationId);

    Optional<Location> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    @Query(value = "CALL SNC_LOCATION_LIST_FROM_HIERARCHY(:locationId);", nativeQuery = true)
    List<Long> getLocationListFromLocationHierarchy(@Param("locationId") Long locationId);

    @Query(value = "select loc.id, loc.name locationName \n" +
            "from (select loc.id, loc.name, loc.parent_id from location_tree lt \n" +
            "inner join organization org on lt.id = org.location_tree_id \n" +
            "and org.id=:companyId and lt.is_active is true and lt.is_deleted is false \n" +
            "inner join location_type ltyp on lt.id = ltyp.location_tree_id \n" +
            "and ltyp.is_active is true and ltyp.is_deleted is false \n" +
            "inner join location loc on ltyp.id = loc.location_type_id) loc \n" +
            "left join (select loc.id, loc.name,loc.parent_id from location_tree lt \n" +
            "inner join organization org on lt.id = org.location_tree_id and org.id=:companyId \n" +
            "and lt.is_active is true and lt.is_deleted is false \n" +
            "inner join location_type ltyp on lt.id = ltyp.location_tree_id \n" +
            "and ltyp.is_active is true and ltyp.is_deleted is false \n" +
            "inner join location loc on ltyp.id = loc.location_type_id) \n" +
            "locp on loc.id = locp.parent_id where locp.id is null;", nativeQuery = true)
    List<Map<String, Object>> getAllChildLocationOfACompany(@Param("companyId") Long companyId);

    List<Location> findAllByLocationTypeAndIdNotInAndIsDeletedFalseAndIsActiveTrueOrderByIdAsc(LocationType locationType,List<Long> locationIds);

    @Query(value = "SELECT l \n" +
            "FROM ReportingManager rm\n" +
            "         inner join LocationManagerMap lmm\n" +
            "                    on rm.reportingTo.id = lmm.applicationUser.id\n" +
            "                        and lmm.company.id = :companyId \n" +
            "                        and rm.applicationUser.id = :salesOfficerId \n" +
            "                        and lmm.toDate is null\n" +
            "                        AND rm.toDate is null\n" +
            "                        AND rm.isActive is true\n" +
            "                        AND rm.isDeleted is false\n" +
            "         inner join Location l\n" +
            "                    on lmm.location.id = l.id\n")
    Optional<Location> getLocationByCompanyIdAndSalesOfficerId(Long companyId, Long salesOfficerId);

    @Query(value = "SELECT l \n" +
            "FROM ReportingManager rm\n" +
            "         inner join LocationManagerMap lmm\n" +
            "                    on rm.reportingTo.id = lmm.applicationUser.id\n" +
            "                        and lmm.company.id = :companyId \n" +
            "                        and rm.applicationUser.id = :salesOfficerId \n" +
            "                        and lmm.toDate is null\n" +
            "                        AND rm.toDate is null\n" +
            "                        AND rm.isActive is true\n" +
            "                        AND rm.isDeleted is false\n" +
            "         inner join Location l\n" +
            "                    on lmm.location.id = l.id\n")
    List<Location> OfficerLocationByCompanyIdAndSalesOfficerId(Long companyId, Long salesOfficerId);

    @Query(value = "select l \n" +
            "from DepotLocationMap map\n" +
            "         inner join Location l on map.location.id = l.id \n" +
            "and l.isActive is true and l.isDeleted is false\n" +
            "where map.depot.id = :depotId \n" +
            "and map.isActive is true and map.isDeleted is false")
    List<Location> getAllLocationByDepotId(Long depotId); // for Location Object

    @Query(value = "SELECT l\n" +
            "FROM LocationManagerMap lmm\n" +
            "         inner join Location l\n" +
            "                    on lmm.location.id = l.id\n" +
            "                        and lmm.applicationUser.id = :managerId \n" +
            "                        AND lmm.company.id = :companyId \n" +
            "                        AND lmm.toDate is null\n" +
            "                        AND lmm.isActive is true\n" +
            "                        AND lmm.isDeleted is false")
    List<Location> getAllManagerLocationByCompanyIdAndLocationManagerId(Long companyId, Long managerId);

    @Query(value = "select l\n" +
            "from DepotLocationMap m\n" +
            "         inner join Depot d on m.depot.id = d.id\n" +
            "    and d.depotManager.id = :depotManagerId\n" +
            "    and m.company.id = :companyId\n" +
            "    and m.isDeleted is false\n" +
            "    and m.isActive is true\n" +
            "         inner join Location l\n" +
            "                    on m.location.id = l.id")
    List<Location> getAllDepotManagerLocationByCompanyIdAndDepotManagerId(Long companyId, Long depotManagerId);

    @Procedure
    void SNC_CHILD_LOCATION_HIERARCHY(long companyId);
}
