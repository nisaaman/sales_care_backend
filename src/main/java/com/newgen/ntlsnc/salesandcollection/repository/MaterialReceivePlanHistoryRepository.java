package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.salesandcollection.entity.MaterialReceivePlanHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author sagor
 * @date ১২/৪/২২
 */
public interface MaterialReceivePlanHistoryRepository extends JpaRepository<MaterialReceivePlanHistory, Long> {
    List<MaterialReceivePlanHistory> findAllByOrganizationAndIsDeletedFalse(Organization organization);
}
