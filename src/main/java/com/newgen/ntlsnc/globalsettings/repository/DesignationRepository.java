package com.newgen.ntlsnc.globalsettings.repository;

import com.newgen.ntlsnc.globalsettings.entity.Designation;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, Long> {
    List<Designation> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<Designation> findByIdAndIsDeletedFalse(Long id);
    
    Optional<Designation> findByOrganizationAndNameIgnoreCaseAndIsDeletedFalse(Organization organization, String name);

    Optional<Designation> findByOrganizationAndIdIsNotAndNameIgnoreCaseAndIsDeletedFalse(Organization organization, Long id, String name);

    List<Designation> findAllByOrganizationAndIsDeletedFalseAndIsActiveTrue(Organization organization);
}
