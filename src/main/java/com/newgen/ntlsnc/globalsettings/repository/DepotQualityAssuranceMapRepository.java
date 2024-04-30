package com.newgen.ntlsnc.globalsettings.repository;
;
import com.newgen.ntlsnc.globalsettings.entity.Depot;
import com.newgen.ntlsnc.globalsettings.entity.DepotQualityAssuranceMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DepotQualityAssuranceMapRepository extends JpaRepository<DepotQualityAssuranceMap, Long> {
    List<DepotQualityAssuranceMap> findAllByDepotAndToDateIsNullAndIsActiveTrueAndIsDeletedFalse(Depot depot);

    @Query(value = "select dqam.id, dqam.qa_id as depotInchargeId, au.name, au.email\n" +
            "from depot_quality_assurance_map dqam \n" +
            "inner join application_user au on au.id = dqam.qa_id\n" +
            "where depot_id = :depotId\n" +
            "and au.is_active is true and au.is_deleted is false\n" +
            "and dqam.is_active is true and dqam.is_deleted is false and dqam.to_date is null", nativeQuery = true)
    List<Map<String,Object>> getQAByDepot(@Param("depotId") Long depotId);

    DepotQualityAssuranceMap findByQaIdAndIsActiveTrueAndIsDeletedFalse(Long depotInchargeId);

    @Query(value = "select dqam.depot_id id,dpt.depot_name depotName \n" +
            "from depot_quality_assurance_map dqam inner join depot dpt \n" +
            "on dqam.depot_id = dpt.id and dqam.is_active is true \n" +
            "and dqam.is_deleted is false and dqam.to_date is null \n" +
            "and (:qaId is null or dqam.qa_id=:qaId) and dqam.organization_id=:organizationId \n" +
            "group by dqam.depot_id",nativeQuery = true)
    List<Map> getAllQADepotsOrDepotByQA(@Param("qaId") Long qaId, Long organizationId);

}
