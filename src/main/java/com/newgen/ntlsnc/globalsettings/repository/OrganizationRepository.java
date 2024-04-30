package com.newgen.ntlsnc.globalsettings.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author mou
 * Created on 4/3/22 11:00 AM
 */

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByIdAndIsDeletedFalseAndIsActiveTrue(Long organizationId);

    List<Organization> findAllByIsDeletedFalseAndIsActiveTrue();

    List<Organization> findAllByIsDeletedFalseAndIsActiveTrueAndParentId(Long parentId);
    List<Organization> findAllByIsDeletedFalseAndIsActiveTrueAndParentIsNotNull();

    Optional<Organization> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByWebAddressIgnoreCase(String webAddress);
    Optional<Organization> findByEmailAndIdIsNot(String email, Long id);
    Optional<Organization> findByWebAddressAndIdIsNot(String webAddress, Long organizationId);

    @Query(value = "SELECT o.id,\n" +
            "       o.name,\n" +
            "       o.short_name,\n" +
            "       o.address,\n" +
            "       o.contact_number,\n" +
            "       o.contact_person,\n" +
            "       o.email,\n" +
            "       o.remarks,\n" +
            "       o.web_address,\n" +
            "       o.parent_id,\n" +
            "       o.subscription_package_id,\n" +
            "       o.is_active,\n" +
            "       o.is_deleted,\n" +
            "       lt.name AS location_name,\n" +
            "       d.file_path\n" +
            "\n" +
            "FROM organization AS o\n" +
            "LEFT JOIN location_tree lt\n" +
            "       ON lt.id = o.location_tree_id\n" +
            "      AND o.is_deleted IS FALSE\n" +
            "      -- AND o.is_active IS TRUE\n" +
            "      AND lt.is_deleted IS FALSE\n" +
            "      AND lt.is_active IS TRUE\n" +
            "LEFT JOIN document AS d\n" +
            "       ON o.id = d.ref_id\n" +
            "      AND d.ref_table = 'organization'\n" +
            "      AND d.is_deleted IS FALSE\n" +
            "      AND d.is_active IS TRUE\n" +
            "WHERE o.is_deleted IS FALSE\n" +
            "AND (o.parent_id = :organizationId or o.id = :organizationId)\n" +
            "ORDER BY o.parent_id,o.id" ,nativeQuery = true)
    List<Map<String, Object>> findAllWithLogo(@Param("organizationId") long organizationId);

    @Query(value = "select d.id as documentId,d.file_path,o.id,o.is_active,o.is_deleted,o.address,\n" +
            "\to.contact_number,o.contact_person,o.email,o.name,o.location_tree_id,\n" +
            "\to.remarks,o.short_name,o.web_address,\n" +
            "\to.parent_id,o.subscription_package_id \n" +
            "from organization as o\n" +
            "left join document as d \n" +
            "on o.id=d.ref_id \n" +
            "  and d.ref_table = 'organization' \n" +
            "AND d.is_deleted IS FALSE \n" +
            "where o.id=:id",nativeQuery = true)
    Map<String, Object> findByIdWithLogo(Long id);

    Optional<Organization> findByShortNameIgnoreCaseAndIsDeletedFalse(String shortName);

    Optional<Organization> findByNameIgnoreCaseAndIsDeletedFalse(String name);

    List<Organization> findAllByParentAndIsDeletedFalseAndIsActiveTrue(Organization parentOrganization);

    Optional<Organization> findById(Long id);

    Optional<Organization> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);
    @Query(value = "select *\n" +
            "from Organization as o\n" +
            "left join ApplicationUserCompanyMapping as au\n" +
            "on o.id=au.organization_id \n" +
            "and au.is_deleted is false\n" +
            "where o.parent = :parentId;",
    nativeQuery = true)
    List<Organization> findAllUserCompanyMappingByOrganizationAndUser(Long parentId);

    List<Organization> findByAddress(String address);

    List<Organization> findByLocationTreeIdAndIsActiveTrueAndIsDeletedFalse(Long id);

}
