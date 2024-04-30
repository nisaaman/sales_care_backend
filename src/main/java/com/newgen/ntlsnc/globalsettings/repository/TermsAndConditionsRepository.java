package com.newgen.ntlsnc.globalsettings.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.TermsAndConditions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author kamal
 * @Date ১৯/৪/২২
 */

@Repository
public interface TermsAndConditionsRepository extends JpaRepository<TermsAndConditions, Long> {
    List<TermsAndConditions> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<TermsAndConditions> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);
    Optional<TermsAndConditions> findByCompanyIdAndIsActiveTrueAndIsDeletedFalse(Long companyId);

    Optional<TermsAndConditions> findByCompanyIdAndIsDeletedFalse(Long companyId);

    Optional<TermsAndConditions> findByCompanyIdAndIdIsNotAndIsDeletedFalse(Long companyId, Long id);
}
