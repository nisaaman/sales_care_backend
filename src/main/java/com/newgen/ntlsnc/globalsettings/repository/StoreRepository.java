package com.newgen.ntlsnc.globalsettings.repository;

import com.newgen.ntlsnc.common.enums.StoreType;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author kamal
 * @Date ১৬/৪/২২
 */


@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    List<Store> findAllByOrganizationAndIsDeletedFalseAndIsActiveTrue(Organization organization);
    Optional<Store> findByIdAndIsDeletedFalse(Long id);


    Optional<Store> findByOrganizationAndNameIgnoreCaseAndIsDeletedFalse(Organization organization, String trim);

    Optional<Store> findByOrganizationAndIdIsNotAndNameIgnoreCaseAndIsDeletedFalse(Organization organization, Long id, String trim);

    Optional<Store> findByOrganizationAndStoreTypeAndIsDeletedFalse(Organization organization, StoreType storeType);

    @Query(value = "SELECT id, store_type, name, short_name \n" +
            "FROM store s \n" +
            "WHERE s.store_type = :store_type and organization_id = :organization_id " +
            "and is_active is true and is_deleted is false "
            , nativeQuery = true)
    Map getStore(@Param("organization_id") Long organization_id,
                 @Param("store_type") String store_type);

    @Query(value = "select id, name, store_type from store \n" +
            "where organization_id=:organizationId \n" +
            "and (COALESCE(:storeTypes) is null or store_type in(:storeTypes)) \n" +
            "and is_active is true and is_deleted is false;", nativeQuery = true)
    List<Map<String, Object>> findAllStoreOfAOrganization(
            @Param("organizationId") Long organizationId,
            @Param("storeTypes") List<String> storeTypes);

    Optional<Store> findByOrganizationAndIdIsNotAndStoreTypeAndIsDeletedFalse(Organization organization, Long id, StoreType storeType);
}
