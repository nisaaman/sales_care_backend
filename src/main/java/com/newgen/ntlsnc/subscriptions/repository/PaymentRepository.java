package com.newgen.ntlsnc.subscriptions.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.subscriptions.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Mou
 * Created on 5/3/22 10:52 AM
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByIdAndIsDeletedFalse(Long id);

    List<Payment> findAllByOrganizationAndIsDeletedFalse(Organization organization);


    Optional<Payment> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);
}
