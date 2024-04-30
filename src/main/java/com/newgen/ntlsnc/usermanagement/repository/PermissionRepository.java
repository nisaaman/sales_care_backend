package com.newgen.ntlsnc.usermanagement.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.usermanagement.entity.Permission;
import com.newgen.ntlsnc.usermanagement.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author nisa
 * @date 6/9/22
 * @time 5:53 PM
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Collection<? extends Permission> findAllByRoleAndIsDeletedFalse(Role role);

//    List<Permission> findAllByIsDeletedFalse();

    List<Permission> findAllByOrganizationAndIsDeletedFalse(Organization organization);


    Optional<Permission> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    @Query(value = "select p.id,\n" +
            "       fi.id as featureId,\n" +
            "       :roleId as roleId,\n" +
            "       fi.feature_name as featureName,\n" +
            "       fi.activity_feature as activityFeature,\n" +
            "       p.is_create as isCreate,\n" +
            "       p.is_update as isUpdate,\n" +
            "       p.is_delete as isDelete,\n" +
            "       p.is_view as isView,\n" +
            "       p.is_all_view as isAllView\n" +
            "from feature_info fi\n" +
            "left join permission p\n" +
            "       on fi.activity_feature = p.activity_feature\n" +
            "      and fi.is_deleted is false\n" +
            "      and fi.is_active is true\n" +
            "      and p.organization_id = :organizationId\n" +
            "      and p.is_deleted is false\n" +
            "      and p.role_id = :roleId\n" +
            "      and p.is_active is true\n" +
            "order by fi.feature_name;", nativeQuery = true)
    List<Map<String, Object>> getAllPermissionListByRole(@Param("organizationId") Long organizationId,
                                                         @Param("roleId") Long roleId);
}
