package com.newgen.ntlsnc.usermanagement.repository;

import com.newgen.ntlsnc.usermanagement.entity.FeatureInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author liton
 * Created on 9/21/22 12:50 PM
 */

@Repository
public interface FeatureInfoRepository extends JpaRepository<FeatureInfo, Long> {

}
