package com.newgen.ntlsnc.globalsettings.repository;
import com.newgen.ntlsnc.globalsettings.entity.Department;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author liton
 * Created on 4/3/22 11:56 AM
 */

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findAllByOrganizationAndIsDeletedFalse(Organization organization);


    Optional<Department> findByIdAndIsDeletedFalse(Long id);

    Optional<Department> findByOrganizationAndNameIgnoreCaseAndIsDeletedFalse(Organization organization, String trim);

    Optional<Department> findByOrganizationAndIdIsNotAndNameIgnoreCaseAndIsDeletedFalse(Organization organization, Long id, String trim);

    List<Department> findAllByOrganizationAndIsDeletedFalseAndIsActiveTrue(Organization organization);
}
