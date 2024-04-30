package com.newgen.ntlsnc.supplychainmanagement.repository;

import com.newgen.ntlsnc.supplychainmanagement.entity.InvDamage;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvDamageDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author anika
 * @Date ৩১/৫/২২
 */
@Repository
public interface InvDamageDetailsRepository extends JpaRepository<InvDamageDetails, Long> {
    List<InvDamageDetails> findAllByInvDamage(InvDamage invDamage);
}
