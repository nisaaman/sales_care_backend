package com.newgen.ntlsnc.supplychainmanagement.repository;

import com.newgen.ntlsnc.supplychainmanagement.entity.QualityInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author Newaz Sharif
 * @since 30th Oct, 22
 */
@Repository
public interface QualityInspectionRepository extends JpaRepository<QualityInspection, Long> {

    @Query(value = "select qid.id, prod.id product_id, prod.product_sku as productSku,  \n" +
            "concat(prod.name, \" \", prod.item_size, \" \", uom.abbreviation, ' * ', ps.pack_size) as productName,\n" +
            "pc.name as productCategory, b.batch_no batchNo, qid.quantity, \n" +
            "qid.qa_status qaStatus, au.name salesOfficer,desg.name designation from quality_inspection qi \n" +
            "inner join quality_inspection_details qid on qi.id = qid.quality_inspection_id \n" +
            "and qi.is_active is true and qi.is_deleted is false \n" +
            "and qi.depot_id =:depotId and qi.company_id =:companyId\n" +
            "and qi.qa_date >=:startDate and qi.qa_date <= :endDate \n" +
            "and (:qaStatus is null or qid.qa_status =:qaStatus) \n" +
            "inner join product prod on qid.product_id = prod.id\n" +
            "inner join product_category pc on prod.product_category_id = pc.id \n" +
            "       and prod.is_active is true and prod.is_deleted is false and pc.is_active is true\n" +
            "       and (:productCategoryId is null or pc.id =:productCategoryId) \n" +
            "       and prod.company_id =:companyId\n" +
            "left join pack_size ps on prod.pack_size_id = ps.id and ps.is_active is true \n" +
            "            and ps.is_deleted is false\n" +
            "left join unit_of_measurement uom on prod.uom_id = uom.id \n" +
            "            and uom.is_active is true and uom.is_deleted is false\n" +
            "inner join batch b on qid.batch_id = b.id\n" +
            "inner join application_user au on qi.qa_by_id = au.id \n" +
            "left join designation desg on au.designation_id = desg.id and desg.is_active is true \n" +
            "            and desg.is_deleted is false;",nativeQuery = true)
    List<Map> getQualityInspectionInfo(
            @Param("companyId") Long companyId,
            @Param("depotId") Long depotId,
            @Param("productCategoryId") Long productCategoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("qaStatus") String qaStatus);

    @Query(value = "select qd.batch_id, q.depot_id,\n" +
            "sum(case when qd.qa_status='PASS' \n" +
            "then qd.quantity else 0 end) pass_quantity,\n" +
            "sum(case when qd.qa_status='FAILED' \n" +
            "then qd.quantity else 0 end) fail_quantity,\n" +
            "sum(ifnull(qd.quantity, 0)) qc_quantity\n" +
            "from quality_inspection q \n" +
            "inner join quality_inspection_details qd on q.id = qd.quality_inspection_id\n" +
            "and qd.batch_id = :batch_id\n" +
            "where q.company_id = :company_id\n" +
            "and qd.product_id = :product_id\n" +
            "and q.depot_id = :depot_id\n" +
            "and q.store_id = :store_id\n" +
            "and q.is_active is true AND q.is_deleted is false\n" +
            "group by q.company_id, qd.product_id, qd.batch_id " +
            "", nativeQuery = true)
    Map getProductBatchAvailableQcStock(@Param("company_id") Long companyId,
                                           @Param("product_id") Long productId,
                                              @Param("batch_id") Long batchId,
                                           @Param("depot_id") Long depotId,
                                           @Param("store_id") Long storeId);
}
