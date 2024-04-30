package com.newgen.ntlsnc.supplychainmanagement.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvReceive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author kamal
 * @Date ১৭/৪/২২
 */

@Repository
public interface InvReceiveRepository extends JpaRepository<InvReceive, Long> {
    List<InvReceive> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<InvReceive> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    @Query(value = "#production receive data which manufacturing cost  data not yet set NGLSC-2444\n" +
            "select invrcvinfo.batch_no as batchNo, invrcvinfo.quantity as batchQuantity,\n" +
            "invrcvinfo.product_id as productId, prodinfo.productSku,prodinfo.productName,\n" +
            "prodinfo.productCategory,invrcvinfo.receiveStore, invrcvinfo.receiveQuantity,\n" +
            "invrcvinfo.manFacCost, invrcvinfo.id, invrcvinfo.receive_no,\n" +
            "invrcvinfo.receive_date from\n" +
            "(select itd.product_id, b.batch_no, b.quantity, st.name as receiveStore,\n" +
            "itd.quantity as receiveQuantity,itd.rate as manFacCost, itd.id as id,\n" +
            "inr.receive_date, inr.receive_no from inv_receive inr\n" +
            "inner join inv_transaction_details itd on inr.inv_transaction_id = itd.inv_transaction_id\n" +
            "and inr.company_id=:companyId\n" +
            "and itd.rate is null\n" +
            "and inr.receive_date >=:fromDate and inr.receive_date <=:endDate\n" +
            "and inr.is_active is true and inr.is_deleted is false\n" +
            "inner join store st on itd.to_store_id = st.id\n" +
            "inner join batch b on itd.batch_id = b.id) invrcvinfo\n" +
            "                           inner join \n" +
            "(select prod.id, prod.product_sku as productSku, \n" +
            "concat(prod.name, \"  \", prod.item_size, \"  \" , uom.abbreviation,\" * \", ps.pack_size) as productName,\n" +
            "pc.name as productCategory from product prod   \n" +
            "inner join product_category pc on prod.product_category_id = pc.id \n" +
            "and prod.is_active is true and prod.is_deleted is false and pc.is_active is true\n" +
            "and prod.company_id=:companyId\n" +
            "left join unit_of_measurement uom on prod.uom_id = uom.id \n" +
            "and uom.is_active is true and uom.is_deleted is false\n" +
            "left join pack_size ps on prod.pack_size_id = ps.id) prodinfo\n" +
            "on invrcvinfo.product_id = prodinfo.id\n" +
            "order by invrcvinfo.receive_date desc;", nativeQuery = true)
    List<Map<String, Object>>  getInvProductionReceiveDetailsDataListToMfCost(
            @Param("companyId") Long companyId,
            @Param("fromDate") LocalDate fromDate,
            @Param("endDate") LocalDate endDate);

    @Query(value = "#all production receive data get\n" +
            "select invrcvinfo.batch_no as batchNo, invrcvinfo.quantity as batchQuantity,\n" +
            "invrcvinfo.product_id as productId, prodinfo.productSku,prodinfo.productName,\n" +
            "prodinfo.productCategory,invrcvinfo.receiveStore, invrcvinfo.receiveQuantity, " +
            "invrcvinfo.manFacCost, invrcvinfo.id, invrcvinfo.receive_no,\n" +
            "invrcvinfo.receive_date from \n" +
            "(select itd.product_id, b.batch_no, b.quantity, st.name as receiveStore, \n" +
            "itd.quantity as receiveQuantity,itd.rate as manFacCost, itd.id as id, inr.receive_date, inr.receive_no from inv_receive inr \n" +
            "inner join inv_transaction_details itd on inr.inv_transaction_id = itd.inv_transaction_id \n" +
            "and inr.company_id=:companyId and inr.receive_date >=:fromDate and inr.receive_date <=:endDate\n" +
            "and inr.depot_id =:depotId \n" +
            "and inr.is_active is true and inr.is_deleted is false\n" +
            "inner join store st on itd.to_store_id = st.id\n" +
            "inner join batch b on itd.batch_id = b.id) invrcvinfo\n" +
            "                           inner join \n" +
            "(select prod.id, prod.product_sku as productSku, \n" +
            "concat(prod.name, \"  \", prod.item_size, \"  \" , uom.abbreviation,\" * \", ps.pack_size) as productName,\n" +
            "pc.name as productCategory from product prod   \n" +
            "inner join product_category pc on prod.product_category_id = pc.id \n" +
            "and prod.is_active is true and prod.is_deleted is false and pc.is_active is true\n" +
            "and prod.company_id=:companyId\n" +
            "left join unit_of_measurement uom on prod.uom_id = uom.id\n" +
            "and uom.is_active is true and uom.is_deleted is false\n" +
            "left join pack_size ps on prod.pack_size_id = ps.id) prodinfo\n" +
            "on invrcvinfo.product_id = prodinfo.id\n" +
            "order by invrcvinfo.receive_date desc;", nativeQuery = true)
    List<Map<String, Object>> getInvProductionReceiveDetailsDataList(
            @Param("companyId") Long companyId,
            @Param("depotId") Long depotId,
            @Param("fromDate") LocalDate fromDate,
            @Param("endDate") LocalDate endDate);

    @Query(value = "select DISTINCT(it.id), it.company_id, \n" +
            "t.id transfer_sent_id, tr.id transfer_rcv_id, \n" +
            "rt.id transaction_receive_id, \n" +
            "it.transaction_type, it.transaction_date, \n" +
            "s.store_type, t.from_depot_id, t.to_depot_id, t.transfer_no, \n" +
            "t.driver_name, t.driver_contact_no, t.remarks, \n" +
            "d.depot_manager_id, d.depot_name, d.address, " +
            "tod.depot_name to_depot_name, u.name user_name, \n" +
            "u.designation_id, ds.name designation_name, \n" +
            "SUM(itd.quantity) quantity \n" +
            "from inv_transaction it \n" +
            "inner join inv_transfer t on it.id=t.inv_transaction_id \n" +
            "and (:depotId is NULL OR t.from_depot_id = :depotId) \n" +
            "and (:userDepotId is NULL OR t.to_depot_id = :userDepotId) \n" +
            "left join inv_transfer tr on t.id=tr.inv_transfer_id \n" +
            "left join inv_transaction rt on tr.inv_transaction_id=rt.id \n" +
            "inner join inv_transaction_details itd on it.id=itd.inv_transaction_id \n" +
            "and (itd.qa_status IS NULL OR itd.qa_status = 'PASS') \n" +
            "inner join store s on s.id=itd.to_store_id \n" +
            "inner join depot d on d.id=t.from_depot_id \n" +
            "inner join depot tod on tod.id=t.to_depot_id \n" +
            "inner join application_user u on u.id=d.depot_manager_id \n" +
            "inner join designation ds on ds.id=u.designation_id \n" +
            "where it.is_active is true and it.is_deleted is false \n" +
            "and (it.transaction_type in (:transactionTypeList)) \n" +
            "and (s.store_type in (:storeTypeList)) \n" +
            "and it.company_id= :companyId \n" +
            "and (:fromDate is null or it.transaction_date >= :fromDate) \n" +
            "and (:endDate is null or it.transaction_date <= :endDate) \n" +
            "group by it.id, s.store_type, t.inv_transaction_id, " +
            "t.from_depot_id, t.to_depot_id,t.transfer_no, \n" +
            "t.driver_name, t.driver_contact_no, t.remarks, tod.depot_name, \n" +
            "rt.id, t.id, tr.id \n" +
            "order by it.id desc \n" +
            "", nativeQuery = true)
    List<Map<String, Object>> findTransferTransactionListToReceive(
            @Param("companyId") Long companyId,
            @Param("fromDate") LocalDate fromDate,
            @Param("endDate") LocalDate endDate,
            @Param("depotId") Long depotId,
            @Param("userDepotId") Long userDepotId,
            @Param("transactionTypeList") List<String> transactionTypeList,
            @Param("storeTypeList") List<String> storeTypeList);

    @Query(value = "select " +
            "itd.product_id id, sum(itd.quantity) quantity, \n" +
            "p.name p_name, p.product_sku, p.uom_id, u.abbreviation uom, \n" +
            "concat(p.name, ' ', p.item_size, ' ', u.abbreviation, ' * ', pk.pack_size) product_name,\n"+
            "pc.name category_name, i.company_id\n" +
            "from inv_transaction_details itd \n" +
            "inner join inv_transaction i on i.id = itd.inv_transaction_id \n" +
            "inner join product p on itd.product_id = p.id \n" +
            "inner join product_category pc on p.product_category_id = pc.id \n" +
            "inner join unit_of_measurement u on p.uom_id = u.id \n" +
            "inner join pack_size pk on p.pack_size_id = pk.id\n" +
            "where itd.is_active is true and itd.is_deleted is false \n" +
            "and itd.inv_transaction_id= :invTransactionId \n" +
            "group by itd.product_id \n" +
            "order by itd.product_id desc \n" +
            "", nativeQuery = true)
    List<Map> findProductListByTransactionId(
            @Param("invTransactionId") Long invTransactionId);

    @Query(value = "select " +
            "itd.id id , itd.batch_id , b.batch_no, itd.quantity quantity, itd.product_id, \n" +
            "pc.name category_name, p.product_sku, concat(p.name, ' ', p.item_size, ' ', u.abbreviation, ' * ', pk.pack_size) product_name\n"+
            "from inv_transaction_details itd \n" +
            "inner join batch b " +
            "on itd.batch_id = b.id \n" +
            "inner join product p on itd.product_id = p.id \n" +
            "inner join product_category pc on p.product_category_id = pc.id \n" +
            "inner join unit_of_measurement u on p.uom_id = u.id \n" +
            "inner join pack_size pk on p.pack_size_id = pk.id\n" +
            "where itd.is_active is true and itd.is_deleted is false \n" +
            "and itd.inv_transaction_id= :invTransactionId \n" +
            "and itd.product_id= :productId \n" +
            "order by itd.batch_id asc \n" +
            "", nativeQuery = true)
    List<Map> getTransferReceiveBatchQtyMap(
            @Param("productId") Long productId,
            @Param("invTransactionId") Long invTransactionId);

    @Query(value = "select " +
            "itd.product_id id, sum(itd.quantity) quantity, \n" +
            "p.name p_name, p.product_sku, p.uom_id, u.abbreviation uom, \n" +
            "concat(p.name, ' ', p.item_size, ' ', u.abbreviation, ' * ', pk.pack_size) product_name,\n"+
            "pc.name category_name,  i.company_id\n" +
            "from inv_transaction_details itd \n" +
            "inner join inv_transaction i on i.id = itd.inv_transaction_id \n" +
            "inner join product p on itd.product_id = p.id \n" +
            "inner join product_category pc on p.product_category_id = pc.id \n" +
            "inner join unit_of_measurement u on p.uom_id = u.id \n" +
            "inner join pack_size pk on p.pack_size_id = pk.id\n" +
            "where itd.is_active is true and itd.is_deleted is false \n" +
            "and itd.inv_transaction_id= :transactionReceiveId \n" +
            "group by itd.product_id \n" +
            "order by itd.product_id desc \n" +
            "", nativeQuery = true)
    List<Map> findProductListToClaim(
            @Param("transactionReceiveId") Long transactionReceiveId);

}
