package com.newgen.ntlsnc.multilayerapproval.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.multilayerapproval.entity.ApprovalStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author nisa
 * @date 9/11/22
 * @time 10:39 AM
 */
@Repository
@EnableJpaRepositories
public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Long> {
    Optional<ApprovalStep> findByIdAndIsDeletedFalse(Long id);

    List<ApprovalStep> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<ApprovalStep> findByOrganizationAndNameIgnoreCaseAndIsDeletedFalse(Organization organization, String trim);

    Optional<ApprovalStep> findByOrganizationAndIdIsNotAndNameIgnoreCaseAndIsDeletedFalse(Organization organization, Long id, String trim);
}
