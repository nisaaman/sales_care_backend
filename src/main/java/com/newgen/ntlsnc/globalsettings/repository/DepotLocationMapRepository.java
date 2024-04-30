package com.newgen.ntlsnc.globalsettings.repository;

import com.newgen.ntlsnc.globalsettings.entity.Depot;
import com.newgen.ntlsnc.globalsettings.entity.DepotLocationMap;
import com.newgen.ntlsnc.globalsettings.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author kamal
 * @Date ২৬/৪/২২
 */

@Repository
public interface DepotLocationMapRepository extends JpaRepository<DepotLocationMap, Long> {

    @Modifying
    @Query(value = "update depot_location_map dlm set dlm.is_deleted = true where dlm.depot_id = :depotId",nativeQuery = true)
    void deleteAllByDepotId(@Param("depotId") Long depotId);

    List<DepotLocationMap> findAllByDepot(Depot depot);

    @Query(value = "select map.company_id companyId, org.name companyName, " +
            "json_arrayagg(JSON_OBJECT(\"id\", l.id, \"name\", l.name)) areaList,\n" +
            "group_concat(l.id) locationIds\n" +
            "from depot_location_map map\n" +
            "         inner join location l on map.location_id = l.id \n" +
            "and l.is_active = true and l.is_deleted = false\n" +
            "         inner join organization org on map.company_id = org.id \n" +
            "and org.is_active = true and org.is_deleted = false\n" +
            "where map.depot_id = :depotId \n" +
            "and map.is_active = true and map.is_deleted = false\n" +
            "group by map.company_id, org.name", nativeQuery = true)
    List<Map<String, Object>> getLocationAndCompanyByDepotId(@Param("depotId") Long depotId);

    @Query(value = "select l.*\n" +
            "from depot_location_map m\n" +
            "         inner join location l\n" +
            "                    on m.location_id = l.id\n" +
            "                        and m.company_id = :companyId\n" +
            "                        and m.depot_id = :depotId \n" +
            "                        and m.is_active is true\n" +
            "                        and m.is_deleted is false;", nativeQuery = true)
    Map getLocationOfDepotByCompanyAndDepot(Long companyId, Long depotId);

    @Query(value = "select location_id from depot_location_map where company_id = :companyId and is_active is true and is_deleted is false", nativeQuery = true)
    List<Long> findAllByCompanyIdAndIsActiveTrueAndIsDeletedFalse(@Param("companyId") Long companyId);

    List<DepotLocationMap> findAllByCompanyId(Long companyTd);

    @Query(value = "select l.*\n" +
            "from depot_location_map m\n" +
            "         inner join location l\n" +
            "                    on m.location_id = l.id\n" +
            "                        and m.company_id = :companyId\n" +
            "                        and m.depot_id = :depotId \n" +
            "                        and m.is_active is true\n" +
            "                        and m.is_deleted is false;", nativeQuery = true)
    List<Map> getLocationListOfDepotByCompanyAndDepot(Long companyId, Long depotId);
}
