package com.newgen.ntlsnc.globalsettings.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author marzia
 * Created on 16/4/22
 */

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    Optional<Vehicle> findByOrganizationAndRegistrationNoIgnoreCaseAndIsDeletedFalse(Organization organization, String trim);

    Optional<Vehicle> findByOrganizationAndIdIsNotAndRegistrationNoIgnoreCaseAndIsDeletedFalse(Organization organization, Long id, String trim);

    Vehicle findByIdAndIsDeletedFalse(Long id);

    List<Vehicle> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    List<Vehicle> findAllByOrganizationAndIsActiveTrueAndIsDeletedFalse(Organization organization);
}
