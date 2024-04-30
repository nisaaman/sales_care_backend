package com.newgen.ntlsnc.supplychainmanagement.repository;

import com.newgen.ntlsnc.common.enums.StoreType;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.salesandcollection.entity.Distributor;
import com.newgen.ntlsnc.supplychainmanagement.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author kamal
 * @Date ১৩/৪/২২
 */

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long> {

     String STORE_CASE_WHEN = "\nCASE\n"
            + "    WHEN :storeType = 'REGULAR' THEN REGULAR \n"
            + "    WHEN :storeType = 'QUARANTINE' THEN QUARANTINE \n"
            + "    WHEN :storeType = 'IN_TRANSIT' THEN IN_TRANSIT \n"
            + "    WHEN :storeType = 'RESTRICTED' THEN RESTRICTED \n"
            + "END\n";

    List<Batch> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<Batch> findByBatchNoIgnoreCaseAndIsDeletedFalse(String value);

    Optional<Batch> findByIdIsNotAndBatchNoIgnoreCaseAndIsDeletedFalse(Long id, String value);

    Optional<Batch> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    @Query(value = " select b.id       batch_id\n" +
            "     , b.batch_no batch_no\n" +
            "     , td.rate    rate\n" +
            "     , td.quantity batch_quantity\n" +
            "from inv_delivery_challan dc\n" +
            "         inner join inv_transaction_details td\n" +
            "                    on td.inv_transaction_id = dc.inv_transaction_id\n" +
            "                        and dc.id = :deliveryChallanId\n" +
            "                        and td.product_id = :productId\n" +
            "                        and dc.is_active is true\n" +
            "                        and dc.is_deleted is false\n" +
            "                        and td.is_active is true\n" +
            "                        and td.is_deleted is false\n" +
            "         inner join batch b\n" +
            "                    on b.id = td.batch_id\n" +
            "                        and b.is_active is true\n" +
            "                        and b.is_deleted is false\n" +
            "order by b.batch_no", nativeQuery = true)
    List<Map> findAllByDeliveryChallanIdAndProductId(@Param("deliveryChallanId") Long deliveryChallanId, @Param("productId") Long productId);

    @Query(value = "select id, batch_no as batchNo, quantity as batchQuantity \n" +
            "from batch where is_active is true \n" +
            "and is_deleted is false and company_id=:companyId order by id desc", nativeQuery = true)
    List<Map<String, Object>> getAllBatchFromACompany(@Param("companyId") Long companyId);

    boolean existsByBatchNo(String batchNo);

    @Query(value = "select quantity as batchQuantity \n" +
            "from batch where is_active is true \n" +
            "and is_deleted is false and id=:batchId", nativeQuery = true)
    Object getBatchQuantity(@Param("batchId") Long batchId);

    @Query(value = "select batinfo.batchId, batinfo.batchNo, batinfo.batchQuantity,\n" +
            "ifnull(invtrans.receivedQuantity, 0) receivedQuantity from(select b.id as batchId, b.batch_no as batchNo, \n" +
            "b.quantity as batchQuantity \n" +
            "from (select batch_id from batch_details where product_id=:productId \n" +
            "and is_active is true and is_deleted is false order by id desc) bd \n" +
            "inner join batch b on bd.batch_id = b.id) batinfo \n" +
            "                  left join   \n" +
            "(select batch_id, sum(quantity) as receivedQuantity from inv_transaction_details it\n" +
            "inner join inv_transaction i on it.inv_transaction_id = i.id \n" +
            "and i.transaction_type='PRODUCTION_RECEIVE'\n" +
            "where product_id=:productId group by batch_id) invtrans\n" +
            "on batinfo.batchId = invtrans.batch_id where (batinfo.batchQuantity>receivedQuantity)\n" +
            "order by batinfo.batchId desc limit 1", nativeQuery = true)
    Map<String, Object> getProductLatestBatchInfo(
            @Param("productId") Long productId);

    @Query(value = "select batinfo.batchId, batinfo.batchNo, batinfo.remarks,\n" +
            "batinfo.consignment_no consignmentNo,\n" +
            "date_format(batinfo.created_date, '%Y-%m-%d') as createdDate,\n" +
            "date_format(batinfo.expiry_date, '%Y-%m-%d') as expiryDate,\n" +
            "date_format(batinfo.production_date, '%Y-%m-%d') as productionDate,\n" +
            "batinfo.batchQuantity,\n" +
            "invtrans.receivedQuantity\n" +
            "from(select b.id as batchId, b.batch_no as batchNo, remarks,\n" +
            "consignment_no, created_date, bd.expiry_date, production_date,\n" +
            "b.quantity as batchQuantity\n" +
            "from (select batch_id, expiry_date\n" +
            "from batch_details where product_id=:productId\n" +
            "and batch_id=:batchId and is_active is true and is_deleted is false) bd\n" +
            "inner join batch b on bd.batch_id = b.id) batinfo\n" +
            "                  left join   \n" +
            "(select batch_id, sum(quantity) as receivedQuantity from inv_transaction_details it\n" +
            "inner join inv_transaction i on it.inv_transaction_id = i.id\n" +
            "where product_id=:productId and batch_id=:batchId\n" +
            "and i.transaction_type='PRODUCTION_RECEIVE'\n" +
            "group by batch_id) invtrans\n" +
            "on batinfo.batchId = invtrans.batch_id", nativeQuery = true)
    Map<String, Object> getProductBatchInfoByProductIdAndBatchId(
            @Param("productId") Long productId,
            @Param("batchId") Long batchId);

    @Query(value = "select bd.product_id productId, b.id as batchId,\n" +
            "b.batch_no as batchNo, b.quantity as batchQuantity \n" +
            "from (select batch_id, product_id from batch_details \n" +
            "where (:productId is null or product_id=:productId) \n" +
            "and is_active is true and is_deleted is false) bd \n" +
            "inner join batch b on bd.batch_id = b.id  \n" +
            "and (:searchString is null or b.batch_no LIKE %:searchString%)\n" +
            "inner join batch_receive_stock br on br.product_id = bd.product_id\n" +
            "and br.id = bd.batch_id\n" +
            "and br.receivable_quantity>0", nativeQuery = true)
    List<Map<String, Object>> getBatchNoAutoCompleteList(
            @Param("productId") Long productId,
            @Param("searchString") String searchString);

    @Query(value = "select b.id as batchId,\n" +
            "b.consignment_no consignmentNo, b.batch_no as batchNo,\n" +
            "b.quantity as batchQuantity, b.remarks,\n" +
            "b.production_date as productionDate,\n" +
            "b.created_date as createdDate,\n" +
            "bd.expiry_date as expiryDate, invtrans.receivedQuantity\n" +
            "from (select batch_id, expiry_date from batch_details where product_id=:productId\n" +
            "and is_active is true and is_deleted is false) bd\n" +
            "inner join batch b on bd.batch_id = b.id\n" +
            "                  left join   \n" +
            "(select batch_id, sum(quantity) as receivedQuantity\n" +
            "from inv_transaction_details it\n" +
            "inner join inv_transaction i on it.inv_transaction_id = i.id\n" +
            "where product_id=:productId\n" +
            "and i.transaction_type='PRODUCTION_RECEIVE'\n" +
            "group by batch_id) invtrans\n" +
            "on b.id = invtrans.batch_id\n" +
            "order by b.id desc;", nativeQuery = true)
    List<Map<String, Object>> getAllBatchFromAProduct(@Param("productId") Long productId);

    @Query(value = "select bd.product_id productId, b.id as batchId,b.batch_no as batchNo, \n" +
            "b.quantity as batchQuantity, \n" +
            "GREATEST(ifnull(batch_stock.available_stock_quantity,0) - " +
            "(ifnull(qc_data.qc_quantity, 0) + ifnull(fail_qc_data.fail_qc_quantity, 0)) ,0) remainingQcQuantity,\n" +
            "(case when :storeType='QUARANTINE'\n" +
            "then ifnull(qc_data.qc_quantity,0) \n" +
            "when :storeType='RESTRICTED'\n" +
            "then (ifnull(qc_data.qc_quantity,0) - moved_data.move_quantity)\n" +
            "else ifnull(batch_stock.available_stock_quantity,0) end)\n" +
            "availableStockQuantity, ifnull(batch_stock.available_stock_quantity,0) availableStockQuantityForQC \n" +
            "from (select batch_id, product_id from batch_details \n" +
            "where (:productId is null or product_id=:productId)  \n" +
            "and is_active is true and is_deleted is false) bd  \n" +
            "inner join batch b on bd.batch_id = b.id  \n" +
            "                       inner join \n" +
            "(select batch_id, "+STORE_CASE_WHEN+" as available_stock_quantity \n" +
            "from batch_wise_stock_details_data\n" +
            " where company_id=:companyId and depot_id=:depotId  and "+STORE_CASE_WHEN+" > 0) batch_stock \n" +
            "on b.id = batch_stock.batch_id\n" +
            "and (:searchString is null or b.batch_no LIKE %:searchString%)\n" +
            "                       left join \n" +
            "(select batch_id, store_id, coalesce(sum(available_qc_quantity),0) qc_quantity from qc_stock where \n" +
            "company_id=:companyId and depot_id=:depotId and store_type=:storeType \n" +
            "and product_id=:productId \n" +
            "and (case when :storeType = \"QUARANTINE\" then qa_status=\"PASS\" \n" +
            "          when :storeType = \"RESTRICTED\" then qa_status=\"FAILED\" end) \n" +
            "group by company_id,depot_id,store_id, batch_id, product_id) qc_data \n" +
            "on batch_stock.batch_id = qc_data.batch_id\n" +
            "                       left join \n" +
            "(select batch_id, coalesce(sum(available_qc_quantity),0) fail_qc_quantity from qc_stock where \n" +
            "company_id=:companyId and depot_id=:depotId and store_type=:storeType \n" +
            "and product_id=:productId and product_id=:productId \n" +
            "and (case when :storeType = \"QUARANTINE\" then qa_status=\"FAILED\" \n" +
            "          when :storeType = \"RESTRICTED\" then qa_status=\"PASS\" end) \n" +
            "group by company_id,depot_id,store_id, batch_id, product_id) fail_qc_data \n" +
            "on fail_qc_data.batch_id = qc_data.batch_id\n" +
            "                       left join \n" +
            "(select batch_id, from_store_id, coalesce(sum(quantity), 0) move_quantity\n" +
            "from inv_transaction it \n" +
            "inner join inv_transaction_details itd on it.id = itd.inv_transaction_id \n" +
            "and product_id=:productId \n" +
            "and it.company_id=:companyId and it.transaction_type='INTER_STORE_MOVEMENT' \n" +
            "inner join inter_store_stock_movement im on it.id = im.inv_transaction_id \n" +
            "and im.depot_id=:depotId \n" +
            "group by it.company_id, depot_id, from_store_id, batch_id, product_id) moved_data \n" +
            "on moved_data.batch_id = qc_data.batch_id and qc_data.store_id = moved_data.from_store_id"
            , nativeQuery = true)
    List<Map<String, Object>> getDepotAndStoreAndProductWiseBatchAndStockInfo(
            @Param("companyId") Long companyId,
            @Param("depotId") Long depotId,
            @Param("productId") Long productId,
            @Param("storeType") String storeType,
            @Param("searchString") String searchString);


    @Query(value = "select batinfo.batchId, batinfo.productId , batinfo.batchNo, batinfo.batchQuantity,\n" +
            "invtrans.receivedQuantity from(select b.id as batchId, bd.product_id productId, batch_no as batchNo, \n" +
            "b.quantity as batchQuantity \n" +
            "from (select batch_id,product_id from batch_details where  \n" +
            "is_active is true and is_deleted is false) bd \n" +
            "inner join batch b on bd.batch_id = b.id and b.batch_no=:batchNo) batinfo \n" +
            "                  left join   \n" +
            "(select batch_id, sum(quantity) as receivedQuantity from inv_transaction_details \n" +
            "group by batch_id) invtrans\n" +
            "on batinfo.batchId = invtrans.batch_id", nativeQuery = true)
    Map<String, Object> getProductBatchInfoByBatchNo(
            @Param("batchNo") String batchNo);
}
