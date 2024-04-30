package com.newgen.ntlsnc.supplychainmanagement.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.supplychainmanagement.entity.Batch;
import com.newgen.ntlsnc.supplychainmanagement.entity.BatchDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author kamal
 * @Date ১৩/৪/২২
 */

@Repository
public interface BatchDetailsRepository extends JpaRepository<BatchDetails, Long> {
    List<BatchDetails> findAllByBatch(Batch batch);

    List<BatchDetails> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    @Query(value = "select b.id, prod.name as prodName, ps.pack_size as noOfIqr,\n" +
            "concat(prod.name, \" \", prod.item_size, \" \", uom.abbreviation, ' * ', ps.pack_size)\n" +
            "as productName,\n" +
            "b.batch_no as batchNo,b.production_date as productionDate,\n" +
            "bd.expiry_date as expiryDate, au.name as supervisorName from batch b\n" +
            "inner join batch_details bd on b.id = bd.batch_id and b.is_active is true\n" +
            "and b.is_deleted is false and b.id=:batchId\n" +
            "inner join product prod on bd.product_id = prod.id\n" +
            "left join application_user au on bd.supervisor_id = au.id\n" +
            "left join pack_size ps on prod.pack_size_id = ps.id\n" +
            "left join unit_of_measurement uom on prod.uom_id = uom.id\n" +
            "and uom.is_deleted is false;", nativeQuery = true)
    Map<String, Object> getBatchDetails(@Param("batchId") Long batchId);

    boolean existsByProductId(Long productId);

}
