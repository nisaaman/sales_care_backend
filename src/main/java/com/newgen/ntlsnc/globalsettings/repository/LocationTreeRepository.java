package com.newgen.ntlsnc.globalsettings.repository;

import com.newgen.ntlsnc.globalsettings.entity.LocationTree;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author kamal
 * @Date ২৫/৫/২২
 */

@Repository
public interface LocationTreeRepository extends JpaRepository<LocationTree, Long> {

    List<LocationTree> findAllByIsDeletedFalse();

    @Query(value = "select lt.id                                                                  as id,\n" +
            "       lt.name                                                                as name,\n" +
            "       lt.code                                                                as code,\n" +
            "       lt.is_active                                                           as is_active ,\n" +
            "       DATE_FORMAT(lt.created_date, '%d-%M-%Y')                               as created_date,\n" +
            "       DATE_FORMAT(lt.last_modified_date, '%d-%M-%Y')                         as updated_date,\n" +
            "       (select GROUP_CONCAT(o.name) as organization_name from organization o where o.is_deleted = false and o.location_tree_id = lt.id) as companies\n" +
            "from location_tree lt\n" +
            "where lt.is_deleted = false\n" +
            "  and case\n" +
            "          when '' = :searchText then true\n" +
            "          else (lt.name like :searchText )\n" +
            "    end\n" +
            "  and case\n" +
            "          when '' = :childCompanyId then true\n" +
            "          else lt.id in (select c.location_tree_id from organization c where c.id = :childCompanyId)\n" +
            "    end\n" +
            "order by lt.id desc", nativeQuery = true)
    List<Map> findAllLocationTreeList(@Param("searchText") String searchText, @Param("childCompanyId") String childCompanyId);

//    List<LocationTree> findAllByOrganizationIdAndIsDeletedFalse(Long organizationId);

    List<LocationTree> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    List<LocationTree> findAllByOrganizationIdAndIsActiveTrueAndIsDeletedFalse(Long organizationId);

    Optional<LocationTree> findByNameIgnoreCaseAndIsDeletedFalse(String name);

    Optional<LocationTree> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    @Query("select t \n" +
            "from DepotLocationMap m\n" +
            "         inner join Location l\n" +
            "                    on m.location.id = l.id\n" +
            "                        and m.organization.id = :organizationId\n" +
            "                        and m.isActive is true\n" +
            "                        and m.isDeleted is false\n" +
            "         inner join LocationType lt\n" +
            "                    on l.locationType.id = lt.id\n" +
            "         inner join LocationTree t\n" +
            "                    on lt.locationTree.id = t.id\n" +
            "group by t.id")
    List<LocationTree> findDepotConfiguredLocationTreeListByOrganizationId(Long organizationId);


    Optional<LocationTree> findByIdAndIsDeletedFalse(Long id);
}
