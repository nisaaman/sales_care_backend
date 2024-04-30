package com.newgen.ntlsnc.usermanagement.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.usermanagement.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author anika
 * @Date ৫/৬/২২
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findAllByOrganizationAndIsDeletedFalse(Organization organization);
    List<Role> findAllByOrganizationAndIsActiveTrueAndIsDeletedFalse(Organization organization);

    Optional<Role> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);
    List<Role> findAllByIdInAndIsActiveTrueAndIsDeletedFalse(List<Long> ids);


    Optional<Role> findByOrganizationAndNameIgnoreCaseAndIsDeletedFalse(Organization organization, String name);

    Optional<Role> findByOrganizationAndIdIsNotAndNameIgnoreCaseAndIsDeletedFalse(Organization organization, Long id, String name);

    Optional<Role> findByIdAndIsDeletedFalse(Long id);

    Optional<Role> findByNameAndIsDeletedFalse(String name);
}
