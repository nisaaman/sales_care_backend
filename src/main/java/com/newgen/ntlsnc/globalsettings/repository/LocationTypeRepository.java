package com.newgen.ntlsnc.globalsettings.repository;

import com.newgen.ntlsnc.globalsettings.entity.LocationTree;
import com.newgen.ntlsnc.globalsettings.entity.LocationType;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
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
public interface LocationTypeRepository extends JpaRepository<LocationType, Long> {
    List<LocationType> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    List<LocationType> findAllByIsDeletedFalseAndLocationTreeOrderByLevelAsc(LocationTree locationTree);

    List<LocationType> findAllByIsDeletedFalseAndLocationTreeIdOrderByLevelAsc(Long locationTreeId);

    @Modifying
    @Query(value = " update location_type lt \n" +
            " set lt.is_active  = false, \n" +
            "    lt.is_deleted = true \n" +
            " where lt.location_tree_id = :locationTreeId",nativeQuery = true)
    void deleteAllLocationTypeByLocationTreeId(@Param("locationTreeId") Long locationTreeId);

    @Modifying
    @Query(value = " select lt\n" +
            "from Organization o\n" +
            "         inner join LocationTree t\n" +
            "                    on t = o.locationTree and o.id = :companyId and t.isActive is true and t.isDeleted is false\n" +
            "         inner join LocationType lt on lt.locationTree = t and lt.isActive is true and lt.isDeleted is false\n")
    List<LocationType> findAllByCompanyId(@Param("companyId") Long companyId);

    Optional<LocationType> findById(Long id);
    Optional<LocationType> findByLocationTreeIdAndIsDepotLevelTrueAndIsActiveTrueAndIsDeletedFalse(Long locationTreeId);

    List<LocationType> findAllByOrganizationIdAndIsDepotLevelIsTrueAndIsDeletedFalse(Long organizationId);

    @Modifying
    @Query("update LocationType t set t.isDepotLevel = false where t.id =:locationTypeId")
    void removeLocationLevelOfDepotById(Long locationTypeId);
    @Modifying
    @Query(value = "SELECT lt.name as locationName FROM location_type lt\n" +
            " inner join organization org\n" +
            " on lt.location_tree_id = org.location_tree_id\n" +
            " where org.id = :companyId \n" +
            " and lt.is_active is true\n" +
            " and lt.is_deleted is false",nativeQuery = true)
    List<Map> findAllLocationTypeList(Long companyId);
}
