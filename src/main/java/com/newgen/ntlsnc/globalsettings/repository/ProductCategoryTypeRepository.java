package com.newgen.ntlsnc.globalsettings.repository;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.ProductCategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author marzia
 * Created on 6/4/22 09:55 AM
 */

@Repository
public interface ProductCategoryTypeRepository extends JpaRepository<ProductCategoryType, Long> {
    List<ProductCategoryType> findAllByIsDeletedFalse();
    List<ProductCategoryType> findAllByIsDeletedFalseAndCompanyOrderByLevel(Organization organization);

    List<ProductCategoryType> findAllByCompanyAndIsDeletedFalse(Organization organization);
    List<ProductCategoryType> findAllByOrganizationAndIsDeletedFalse(Organization organization);
    List<ProductCategoryType> findByOrganizationAndCompanyAndIsDeletedFalse(Organization organization, Organization company);

    Optional<ProductCategoryType> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);
}
