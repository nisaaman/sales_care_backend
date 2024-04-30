package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.salesandcollection.entity.SalesBudgetDetails;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Newaz Sharif
 * @since 23th Aug, 22
 */

@Repository
public interface SalesBudgetDetailsCrudRepository extends CrudRepository<SalesBudgetDetails, Long> {
}
