package com.newgen.ntlsnc.multilayerapproval.repository;

import com.newgen.ntlsnc.common.enums.ApprovalFeature;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.multilayerapproval.entity.ApprovalStep;
import com.newgen.ntlsnc.multilayerapproval.entity.ApprovalStepFeatureMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author nisa
 * @date 9/11/22
 * @time 10:47 AM
 */
@Repository
@EnableJpaRepositories
public interface ApprovalStepFeatureMapRepository extends JpaRepository<ApprovalStepFeatureMap, Long> {
    List<ApprovalStepFeatureMap> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<ApprovalStepFeatureMap> findByIdAndIsDeletedFalse(Long id);

    List<ApprovalStepFeatureMap> findAllByOrganizationAndApprovalStepIdAndIsActiveTrueAndIsDeletedFalse(Organization organization, Long approvalStepId);

    @Query(value = "SELECT approval_feature as approvalFeature " +
            "FROM approval_step_feature_map " +
            "where company_id=:companyId " +
            "and is_active is true and is_deleted is false " +
            "group by approval_feature;", nativeQuery = true)
    List<Map<String,String>> findAllByCompanyIdAndIsDeletedFalseAndIsActiveTrue(@Param("companyId") Long companyId);

    @Query(value = "SELECT asfm.id ,aps.name \n" +
            "FROM approval_step_feature_map asfm\n" +
            "inner join approval_step aps \n" +
            "on asfm.approval_step_id=aps.id\n" +
            "where company_id=:companyId \n" +
            "\tand approval_feature =:approvalStepFeature\n" +
            "\tand asfm.is_active is true and asfm.is_deleted is false\n" +
            "\tand aps.is_active is true and aps.is_deleted is false  \n" +
            "order by level;", nativeQuery = true)
    List<Map<String,String>> findAllApprovalStepFeatureByCompanyIdAndApprovalStepFeature(@Param("companyId") Long companyId,@Param("approvalStepFeature") String approvalStepFeature);

    @Query(value = "SELECT o.id as companyId,\n" +
            "o.name as companyName,\n" +
            "asfm.approval_feature as feature,\n" +
            "group_concat(distinct ast.name order by asfm.level   SEPARATOR '->') as approvalStepName\n" +
            "FROM approval_step_feature_map asfm\n" +
            "inner JOIN approval_step ast \n" +
            "\tON asfm.approval_step_id = ast.id\n" +
            "    and asfm.is_active is true\n" +
            "    and asfm.is_deleted is false\n" +
            "inner join organization o \n" +
            "\ton asfm.company_id=o.id\n" +
            "where asfm.organization_id=:organizationId\n" +
            "GROUP BY asfm.approval_feature,asfm.company_id;", nativeQuery = true)
    List<Map<String, String>> findAllByOrganizationAsPerDesign(@Param("organizationId") Long organizationId);

    List<ApprovalStepFeatureMap> findAllByOrganizationAndCompanyAndApprovalFeatureAndIsDeletedFalseOrderByLevelAsc(Organization organization, Organization company, ApprovalFeature approvalFeature);

    ApprovalStepFeatureMap findByOrganizationAndCompanyAndApprovalFeatureAndApprovalStepIdAndIsDeletedFalse(Organization organization, Organization company, ApprovalFeature feature, Long approvalStepId);
}
