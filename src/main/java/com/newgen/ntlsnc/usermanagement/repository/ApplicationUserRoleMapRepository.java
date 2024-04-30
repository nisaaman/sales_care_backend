package com.newgen.ntlsnc.usermanagement.repository;

import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUserRoleMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author anika
 * @Date ৫/৬/২২
 */
@Repository
public interface ApplicationUserRoleMapRepository extends JpaRepository<ApplicationUserRoleMap, Long> {
    List<ApplicationUserRoleMap> findAllByOrganizationAndIsDeletedFalse(Organization organization);
    List<ApplicationUserRoleMap> findAllByIsDeletedFalse();

    List<ApplicationUserRoleMap> findAllByApplicationUserAndIsDeletedFalse(ApplicationUser user);

    Optional<ApplicationUserRoleMap> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    Collection<ApplicationUserRoleMap> findAllByOrganizationAndApplicationUserAndIsActiveTrueAndIsDeletedFalse(Organization organization, ApplicationUser applicationUser);

    @Modifying
    @Query("update ApplicationUserRoleMap a set a.isDeleted = true where a.applicationUser.id =:userId and a.organization.id=:organizationId")
    void deleteAllByUserAndOrganization(Long userId, Long organizationId);

    List<ApplicationUserRoleMap> findAllByRoleIdAndIsDeletedFalse(Long roleId);
}
