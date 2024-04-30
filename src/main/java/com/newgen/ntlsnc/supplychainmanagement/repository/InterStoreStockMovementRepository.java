package com.newgen.ntlsnc.supplychainmanagement.repository;

import com.newgen.ntlsnc.supplychainmanagement.entity.InterStoreStockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author Newaz Sharif
 * @since 3rd Oct, 22
 */
@Repository
public interface InterStoreStockMovementRepository extends JpaRepository<InterStoreStockMovement, Long> {

    @Query(value = "select invdet.movementBy, invdet.designation, invdet.organization_id, \n" +
            "invdet.company_id, invdet.depot_id, \n" +
            "invtransdet.transaction_type, invdet.movement_ref_no movementRefNo, \n" +
            "invdet.approval_status status, (invtransdet.quantity), invdet.id,\n" +
            "invtransdet.fromStore, invtransdet.toStore,invdet.movementDate from( \n" +
            "select issm.inv_transaction_id, au.name movementBy, desg.name designation,\n" +
            "issm.movement_ref_no, issm.approval_status, issm.movement_date movementDate, \n" +
            "issm.organization_id, issm.company_id, \n" +
            "issm.depot_id,\n" +
            "issm.id\n" +
            "from inter_store_stock_movement issm\n" +
            "left join application_user au on issm.movement_by_id = au.id \n"+
            "left join designation desg on au.designation_id = desg.id\n"+
            "where issm.is_active is true and issm.is_deleted is false \n" +
            "and issm.company_id=:companyId and issm.depot_id=:depotId \n" +
            "and issm.movement_date >= :fromDate " +
            "and issm.movement_date <= :toDate) invdet\n" +

            "                             \tinner join \n" +
            "(select invt.id transactionId, invt.transaction_type, \n" +
            "sum(itd.quantity) quantity, st1.name fromStore, st2.name toStore \n" +
            "from inv_transaction invt inner join inv_transaction_details itd \n" +
            "on invt.id = itd.inv_transaction_id \n" +
            "inner join store st1 on itd.from_store_id = st1.id\n" +
            "inner join store st2 on itd.to_store_id = st2.id\n" +
            "group by invt.id, invt.transaction_type, st1.name, st2.name) invtransdet \n" +
            "on invdet.inv_transaction_id = invtransdet.transactionId;", nativeQuery = true)
    List<Map<String, Object>> getInterStoreStockMovementDetails(
            @Param("companyId") Long companyId,
            @Param("depotId") Long depotId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    @Query(value = "select sum(available_qc_quantity) available_qc_quantity from qc_stock where company_id=:companyId \n" +
            "and depot_id=:depotId and product_id=:productId and store_type=:storeType \n" +
            "and batch_id=:batchId and qa_status=:qa_status group by depot_id, product_id, batch_id;", nativeQuery = true)
    Map getQCStockQCInfo(@Param("companyId") Long companyId,
                         @Param("depotId") Long depotId,
                         @Param("productId") Long productId,
                         @Param("storeType") String storeType,
                         @Param("batchId") Long batchId,
                         @Param("qa_status") String qa_status);
}
