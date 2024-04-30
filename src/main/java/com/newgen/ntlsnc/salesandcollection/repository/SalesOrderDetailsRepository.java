package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.salesandcollection.entity.SalesOrder;
import com.newgen.ntlsnc.salesandcollection.entity.SalesOrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author kamal
 * @Date ১২/৪/২২
 */

@Repository
public interface SalesOrderDetailsRepository extends JpaRepository<SalesOrderDetails, Long> {
    List<SalesOrderDetails> findAllBySalesOrder(SalesOrder salesOrder);

    List<SalesOrderDetails> findAllByIsDeletedFalse();

    List<SalesOrderDetails> findAllByOrganizationAndIsDeletedFalse(Organization organization);
}
