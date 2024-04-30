package com.newgen.ntlsnc.multilayerapproval.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.multilayerapproval.entity.MultiLayerApprovalPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author sagor
 * @date 9/12/22
 * @time 12:40 AM
 */
@Repository
@EnableJpaRepositories
public interface MultiLayerApprovalPathRepository extends JpaRepository<MultiLayerApprovalPath, Long> {

    Optional<MultiLayerApprovalPath> findByIdAndIsDeletedFalse(Long id);

    List<MultiLayerApprovalPath> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<MultiLayerApprovalPath> findByOrganizationAndCompanyIdAndApprovalStepFeatureMapIdAndIsDeletedFalse(Organization organization, Long id, Long id1);

    Optional<MultiLayerApprovalPath> findByOrganizationAndIdIsNotAndCompanyIdAndApprovalStepFeatureMapIdAndIsDeletedFalse(Organization organization, Long id, Long id1, Long id2);

    @Query(value = "select mlap.approval_actor approvalActor, mlap.approval_actor_id approvalActorId,\n" +
            "mlap.id multiLayerApprovalPathId, asfm.level\n" +
            "from multi_layer_approval_path mlap\n" +
            "inner join approval_step_feature_map asfm on asfm.id = mlap.approval_step_feature_map_id\n" +
            "and asfm.approval_feature = :approvalFeature and mlap.company_id = :companyId\n" +
            "and mlap.is_active = true and mlap.is_deleted =  false\n" +
            "and asfm.is_active = true and asfm.is_deleted =  false " +
            "and asfm.level = :level", nativeQuery = true)
    List<Map<String, Object>> getApprovalListByFeatureAndLevel(
            @Param("companyId") Long companyId,
            @Param("approvalFeature") String approvalFeature,
            @Param("level") Integer level);

    @Query(value = "select mlap.approval_actor approvalActor, mlap.approval_actor_id approvalActorId,\n" +
            "mlap.id multiLayerApprovalPathId, asfm.level\n" +
            "from multi_layer_approval_path mlap\n" +
            "inner join approval_step_feature_map asfm on asfm.id = mlap.approval_step_feature_map_id\n" +
            "and asfm.approval_feature = :approvalFeature and mlap.company_id = :companyId\n" +
            "and mlap.is_active = true and mlap.is_deleted =  false\n" +
            "and asfm.is_active = true and asfm.is_deleted =  false " +
            "order by asfm.level", nativeQuery = true)
    List<Map<String, Object>> getApprovalListByFeature(@Param("companyId") Long companyId, @Param("approvalFeature") String approvalFeature);


    @Query(value = "select aurm.user_id applicationUserId, asfm.approval_step_id approvalStepId, asfm.level, asp.name approvalStepName, mlap.approval_actor approvalActor, mlap.id multiLayerApprovalPathId, r.name roleName from \n" +
            "multi_layer_approval_path mlap\n" +
            "inner join approval_step_feature_map asfm on asfm.id = mlap.approval_step_feature_map_id\n" +
            "and asfm.approval_feature = :approvalFeature and mlap.company_id = :companyId and mlap.approval_actor = :approvalActor and mlap.id = :multiLayerApprovalPathId\n" +
            "and mlap.is_active = true and mlap.is_deleted =  false\n" +
            "and asfm.is_active = true and asfm.is_deleted =  false\n" +
            "inner join approval_step asp on asp.id = asfm.approval_step_id and asp.is_active = true and asp.is_deleted =  false \n" +
            "inner join role r on r.id = mlap.approval_actor_id and r.is_active = true and r.is_deleted =  false \n" +
            "inner join application_user_role_map aurm on aurm.role_id = r.id and aurm.is_active = true and aurm.is_deleted =  false \n" +
            "and (coalesce(:applicationUserId) is null or aurm.user_id = :applicationUserId)\n" +
            "inner join application_user_company_mapping aucm on aucm.user_id = aurm.user_id and aurm.is_deleted =  false and aucm.company_id = :companyId ", nativeQuery = true)
    List<Map<String, Object>> getApprovalListByRole(@Param("companyId") Long companyId,
                                                    @Param("approvalFeature") String approvalFeature,
                                                    @Param("approvalActor") String approvalActor,
                                                    @Param("applicationUserId") Long applicationUserId,
                                                    @Param("multiLayerApprovalPathId") Long multiLayerApprovalPathId);

    @Query(value = "select au.id applicationUserId, asfm.approval_step_id approvalStepId, asfm.level, asp.name approvalStepName, mlap.approval_actor approvalActor, mlap.id multiLayerApprovalPathId from \n" +
            "multi_layer_approval_path mlap\n" +
            "inner join approval_step_feature_map asfm on asfm.id = mlap.approval_step_feature_map_id\n" +
            "and asfm.approval_feature = :approvalFeature and mlap.company_id = :companyId and mlap.approval_actor = :approvalActor and mlap.id = :multiLayerApprovalPathId\n" +
            "and mlap.is_active = true and mlap.is_deleted =  false\n" +
            "and asfm.is_active = true and asfm.is_deleted =  false\n" +
            "inner join approval_step asp on asp.id = asfm.approval_step_id and asp.is_active = true and asp.is_deleted =  false \n" +
            "inner join designation d on d.id = mlap.approval_actor_id \n" +
            "and d.is_active = true and d.is_deleted =  false\n" +
            "inner join application_user au on au.designation_id = d.id and au.is_active = true and au.is_deleted =  false " +
            "and (coalesce(:applicationUserId) is null or au.id = :applicationUserId)\n" +
            " ", nativeQuery = true)
    List<Map<String, Object>> getApprovalListByDesignation(@Param("companyId") Long companyId, @Param("approvalFeature") String approvalFeature, @Param("approvalActor") String approvalActor, @Param("applicationUserId") Long applicationUserId, @Param("multiLayerApprovalPathId") Long multiLayerApprovalPathId);

    @Query(value = "select lmp.application_user_id applicationUserId, asfm.approval_step_id approvalStepId, asfm.level, asp.name approvalStepName, mlap.approval_actor approvalActor, lmp.location_id locationId, mlap.id multiLayerApprovalPathId from \n" +
            "multi_layer_approval_path mlap\n" +
            "inner join approval_step_feature_map asfm on asfm.id = mlap.approval_step_feature_map_id\n" +
            "and asfm.approval_feature = :approvalFeature and mlap.company_id = :companyId and mlap.approval_actor = :approvalActor and mlap.id = :multiLayerApprovalPathId\n" +
            "and mlap.is_active = true and mlap.is_deleted =  false\n" +
            "and asfm.is_active = true and asfm.is_deleted =  false\n" +
            "inner join approval_step asp on asp.id = asfm.approval_step_id and asp.is_active = true and asp.is_deleted =  false \n" +
            "inner join location_type lt on lt.id = mlap.approval_actor_id\n" +
            "and lt.is_active = true and lt.is_deleted =  false\n" +
            "inner join location l on l.location_type_id = lt.id and l.is_active = true and l.is_deleted =  false\n" +
            "inner join location_manager_map lmp on lmp.location_id = l.id and lmp.to_date is null\n" +
            "and lmp.company_id = :companyId and lmp.is_active = true and lmp.is_deleted =  false \n" +
            "and (coalesce(:applicationUserId) is null or lmp.application_user_id = :applicationUserId)\n" +
            "", nativeQuery = true)
    List<Map<String, Object>> getApprovalListByLocationType(@Param("companyId") Long companyId,
                                                            @Param("approvalFeature") String approvalFeature,
                                                            @Param("approvalActor") String approvalActor,
                                                            @Param("applicationUserId") Long applicationUserId,
                                                            @Param("multiLayerApprovalPathId") Long multiLayerApprovalPathId);

    @Query(value = "select (select group_concat(location_id)\n" +
            "from depot d \n" +
            "inner join depot_location_map dlm on dlm.depot_id = d.id and dlm.company_id = :companyId \n" +
            "and d.depot_manager_id = :applicationUserId \n" +
            "and dlm.is_active = true and dlm.is_deleted =  false\n" +
            "group by d.depot_manager_id\n" +
            ") locationIds, \n" +
            "asfm.approval_step_id approvalStepId,\n" +
            "asfm.level, asp.name approvalStepName, mlap.approval_actor approvalActor, mlap.id multiLayerApprovalPathId\n" +
            "from \n" +
            "multi_layer_approval_path mlap\n" +
            "inner join approval_step_feature_map asfm on asfm.id = mlap.approval_step_feature_map_id\n" +
            "and asfm.approval_feature = :approvalFeature \n" +
            "and mlap.company_id = :companyId and mlap.approval_actor = :approvalActor and mlap.id = :multiLayerApprovalPathId\n" +
            "and mlap.is_active = true and mlap.is_deleted =  false\n" +
            "and asfm.is_active = true and asfm.is_deleted =  false\n" +
            "inner join approval_step asp on asp.id = asfm.approval_step_id \n" +
            "and asp.is_active = true and asp.is_deleted =  false \n"
            , nativeQuery = true)
            /* depot_company_mapping entity currently not used "WHERE EXISTS (\n" +
            "select d.depot_manager_id\n" +
            "from depot d \n" +
            "inner join depot_company_mapping dcm on dcm.depot_id = d.id and dcm.company_id = :companyId\n" +
            "and dcm.is_active = true and dcm.is_deleted =  false and d.depot_manager_id = :applicationUserId\n" +
            "inner join depot_location_map dlm on dlm.depot_id = d.id\n" +
            "and dlm.is_active = true and dlm.is_deleted =  false\n" +
            ")"*/
    List<Map<String, Object>> getApprovalListByDepotInCharge(@Param("companyId") Long companyId, @Param("approvalFeature") String approvalFeature, @Param("approvalActor") String approvalActor, @Param("applicationUserId") Long applicationUserId, @Param("multiLayerApprovalPathId") Long multiLayerApprovalPathId);

    @Query(value = "select au.id applicationUserId, asfm.approval_step_id approvalStepId, asfm.level, asp.name approvalStepName, mlap.approval_actor approvalActor, mlap.id multiLayerApprovalPathId  from \n" +
            "multi_layer_approval_path mlap\n" +
            "inner join approval_step_feature_map asfm on asfm.id = mlap.approval_step_feature_map_id\n" +
            "and asfm.approval_feature = :approvalFeature and mlap.company_id = :companyId and mlap.approval_actor = :approvalActor and mlap.id = :multiLayerApprovalPathId\n" +
            "and mlap.is_active = true and mlap.is_deleted =  false\n" +
            "and asfm.is_active = true and asfm.is_deleted =  false\n" +
            "inner join approval_step asp on asp.id = asfm.approval_step_id and asp.is_active = true and asp.is_deleted =  false \n" +
            "inner join application_user au on au.id = mlap.approval_actor_id and au.is_active = true and au.is_deleted =  false " +
            "and (coalesce(:applicationUserId) is null or au.id = :applicationUserId)\n" +
            "", nativeQuery = true)
    List<Map<String, Object>> getApprovalListByApplicationUser(@Param("companyId") Long companyId, @Param("approvalFeature") String approvalFeature, @Param("approvalActor") String approvalActor, @Param("applicationUserId") Long applicationUserId, @Param("multiLayerApprovalPathId") Long multiLayerApprovalPathId);

    MultiLayerApprovalPath findByApprovalStepFeatureMapIdAndIsDeletedFalse(Long approvalStepFeatureMapId);

    @Query(value = "select asfm.level from \n" +
            "multi_layer_approval_path mlap\n" +
            "inner join approval_step_feature_map asfm on asfm.id = mlap.approval_step_feature_map_id\n" +
            "and mlap.company_id = :companyId and asfm.approval_feature = :approvalFeature \n" +
            "and mlap.is_active = true and mlap.is_deleted =  false\n" +
            "and asfm.is_active = true and asfm.is_deleted =  false \n" +
            "order by asfm.level desc limit 1", nativeQuery = true)
    Integer getLastLevelApprovalPath(@Param("companyId") Long companyId, @Param("approvalFeature") String approvalFeature);

    @Query(value = "select lmp.application_user_id applicationUserId, asfm.approval_step_id approvalStepId, asfm.level, asp.name approvalStepName, mlap.approval_actor approvalActor, lmp.location_id locationId, mlap.id multiLayerApprovalPathId from \n" +
            "multi_layer_approval_path mlap\n" +
            "inner join approval_step_feature_map asfm on asfm.id = mlap.approval_step_feature_map_id\n" +
            "and asfm.approval_feature = :approvalFeature and mlap.company_id = :companyId and mlap.approval_actor = :approvalActor and mlap.id = :multiLayerApprovalPathId\n" +
            "and mlap.is_active = true and mlap.is_deleted =  false\n" +
            "and asfm.is_active = true and asfm.is_deleted =  false\n" +
            "inner join approval_step asp on asp.id = asfm.approval_step_id and asp.is_active = true and asp.is_deleted =  false \n" +
            "inner join location_type lt on lt.id = mlap.approval_actor_id\n" +
            "and lt.is_active = true and lt.is_deleted =  false\n" +
            "inner join location l on l.location_type_id = lt.id and l.is_active = true and l.is_deleted =  false\n" +
            "inner join location_manager_map lmp on lmp.location_id = l.id and lmp.to_date is null\n" +
            "and lmp.company_id = :companyId and lmp.is_active = true and lmp.is_deleted =  false \n" +
            "and (coalesce(:teamIds) is null or lmp.application_user_id in (:teamIds))\n" +
            "", nativeQuery = true)
    List<Map<String, Object>> getApprovalListByLocationType(@Param("companyId") Long companyId,
                                                            @Param("approvalFeature") String approvalFeature,
                                                            @Param("approvalActor") String approvalActor,
                                                            @Param("multiLayerApprovalPathId") Long multiLayerApprovalPathId,
                                                            @Param("teamIds") List<Long> teamIds);



}
