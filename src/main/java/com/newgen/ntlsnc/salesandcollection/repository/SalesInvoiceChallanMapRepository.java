package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.salesandcollection.entity.SalesInvoice;
import com.newgen.ntlsnc.salesandcollection.entity.SalesInvoiceChallanMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author kamal
 * @Date ১২/৯/২২
 */

@Repository
public interface SalesInvoiceChallanMapRepository extends JpaRepository<SalesInvoiceChallanMap, Long> {
    List<SalesInvoiceChallanMap> findAllBySalesInvoiceAndIsDeletedIsFalseAndIsActiveIsTrue(SalesInvoice salesInvoice);
}
