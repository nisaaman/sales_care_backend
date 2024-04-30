package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.salesandcollection.entity.CollectionBudgetDetails;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Newaz Sharif
 * @since 25th Aug,22
 */
@Repository
public interface CollectionBudgetDetailsCrudRepository extends CrudRepository<CollectionBudgetDetails, Long> {
}
