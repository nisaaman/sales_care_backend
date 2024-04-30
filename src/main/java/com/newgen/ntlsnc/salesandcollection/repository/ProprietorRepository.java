package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.salesandcollection.dto.ProprietorDto;
import com.newgen.ntlsnc.salesandcollection.entity.Distributor;
import com.newgen.ntlsnc.salesandcollection.entity.Proprietor;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.metamodel.SingularAttribute;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProprietorRepository extends JpaRepository<Proprietor, Long> {
    List<Proprietor> findAllByOrganizationAndIsDeletedFalse(Organization organization);


    Optional<Proprietor> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    Optional<Proprietor> findAllById(Optional<Distributor> id);


    List<Proprietor> findAllByDistributorAndIsActiveTrueAndIsDeletedFalse(Distributor distributor);

    List<Proprietor> findAllByDistributorIdAndIsActiveTrueAndIsDeletedFalse(Long id);
}
