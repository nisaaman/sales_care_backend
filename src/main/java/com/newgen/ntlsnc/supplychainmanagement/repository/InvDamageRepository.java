package com.newgen.ntlsnc.supplychainmanagement.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvDamage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author anika
 * @Date ৩১/৫/২২
 */
@Repository
public interface InvDamageRepository extends JpaRepository<InvDamage, Long> {
    List<InvDamage> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<InvDamage> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    @Query(value = "select id.id, id.damage_no, s.store_type,\n" +
            "       au.name as created_by, d.name as designation,\n" +
            "       o.name as company_name,\n" +
            "       UCASE(date_format(id.declaration_date, '%d %b %Y')) as declaration_date,\n" +
            "       sum(idd.quantity) damage_quantity,\n" +
            "       id.reason\n" +
            "\n" +
            "from inv_damage id\n" +
            "inner join inv_damage_details idd\n" +
            "        on id.id = idd.inv_damage_id\n" +
            "       and id.is_deleted is false\n" +
            "inner join store s on id.store_id = s.id\n" +
            "inner join application_user au on id.created_by = au.id\n" +
            "inner join designation d on d.id = au.designation_id\n" +
            "inner join organization o on o.id = id.company_id\n" +
            "\n" +
            "where id.organization_id = :organizationId\n" +
            "  and id.company_id = :companyId\n" +
            "  and id.depot_id = :depotId\n" +
            "  and (:fromDate = '' or :fromDate is null or :toDate = '' or :toDate is null\n" +
            "   or (id.declaration_date between :fromDate and :toDate))\n" +
            "\n" +
            "group by id.id order by id desc;", nativeQuery = true)
    List<Map<String, String>> getDamageDeclarationList(
            @Param("organizationId") Long organizationId,
            @Param("companyId") Long companyId,
            @Param("depotId") Long distributorId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
}
