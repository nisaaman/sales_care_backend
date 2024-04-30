package com.newgen.ntlsnc.globalsettings.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.TermsAndConditions;
import com.newgen.ntlsnc.globalsettings.entity.UnitOfMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnitOfMeasurementRepository extends JpaRepository<UnitOfMeasurement, Long> {
    List<UnitOfMeasurement> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<UnitOfMeasurement> findByIdAndIsDeletedFalse(Long id);


    Optional<UnitOfMeasurement> findByOrganizationAndAbbreviationIgnoreCaseAndIsDeletedFalse(Organization organization, String trim);

    Optional<UnitOfMeasurement> findByOrganizationAndIdIsNotAndAbbreviationIgnoreCaseAndIsDeletedFalse(Organization organization, Long id, String trim);

    List<UnitOfMeasurement> findAllByOrganizationAndIsDeletedFalseAndIsActiveTrue(Organization organization);

    Optional<UnitOfMeasurement> findByAbbreviationIgnoreCaseAndOrganizationAndIsDeletedFalse(String trim,Organization organization);


}
