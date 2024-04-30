package com.newgen.ntlsnc.subscriptions.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.subscriptions.entity.SubscriptionPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Mou
 * Created on 4/3/22 03:40 PM
 */
@Repository
public interface SubscriptionPackageRepository extends JpaRepository<SubscriptionPackage, Long> {
    
    List<SubscriptionPackage> findAllByIsDeletedFalse();

    Optional<SubscriptionPackage> findByIdAndIsDeletedFalse(Long id);


    Optional<SubscriptionPackage> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);
}
