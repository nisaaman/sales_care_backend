package com.newgen.ntlsnc.globalsettings.repository;

import com.newgen.ntlsnc.globalsettings.entity.Depot;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface DepotRepository extends JpaRepository<Depot, Long> {
    List<Depot> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    @Query(value = "select d.id                      as id,\n" +
            "       d.code                    as code,\n" +
            "       d.depot_name              as depot_name,\n" +
            "       d.contact_number          as contact_number,\n" +
            "       d.address                 as address,\n" +
            "       d.is_central_warehouse    as isCentralWarehouse,\n" +
            "       d.is_active               as is_active,\n" +
            "       d.is_deleted              as is_deleted,\n" +
            "       concat(au.name,' [',au.email,']')   as depotManager,\n" +
            "       case when is_central_warehouse is true \n" +
            "       then 'Central Warehouse' \n" +
            "       else ''\n" +
            "       END centralWarehouse,\n" +
            "       (select GROUP_CONCAT(l.name,' [',org.short_name,']')\n" +
            "        from depot_location_map m\n" +
            "         inner join location l on m.location_id = l.id\n" +
            "         inner join organization org on m.company_id = org.id and org.is_deleted = false\n" +
            "        where m.is_deleted = false\n" +
            "          and l.is_deleted = false\n" +
            "          and m.depot_id = d.id) as areas\n" +
            "from depot d\n" +
            "inner join application_user au on au.id = d.depot_manager_id\n" +
            "where d.is_deleted = false\n" +
            "  and case\n" +
            "          when '' = :searchText then true\n" +
            "          else (d.depot_name like :searchText\n" +
            "              or d.code like :searchText\n" +
            "              or d.contact_number like :searchText\n" +
            "              or d.address like :searchText\n" +
            "              or d.id in (select m.depot_id\n" +
            "                          from depot_location_map m\n" +
            "                                   left join location l on m.location_id = l.id\n" +
            "                          where m.is_deleted = false\n" +
            "                            and l.is_deleted = false\n" +
            "                            and (l.name like :searchText))\n" +
            "              ) end\n" +
            "  and case\n" +
            "          when '' = :searchAreaId then true\n" +
            "          else d.id in (select m.depot_id\n" +
            "                        from depot_location_map m\n" +
            "                                 left join location l on m.location_id = l.id\n" +
            "                        where m.is_deleted = false\n" +
            "                          and l.is_deleted = false\n" +
            "                          and  l.id = :searchAreaId) end\n" +
            "\n" +
            "order by d.id desc", nativeQuery = true)
    List<Map> findAllDepotList(@Param("searchText") String searchText, @Param("searchAreaId") String searchAreaId);

    @Query(value = "select d.id                 distributor_id\n" +
            "     , d.distributor_name   distributor_name\n" +
            "     , d.ship_to_address    ship_to_address\n" +
            "     , d.contact_no         dis_contact_no\n" +
            "     , l.name               location_name\n" +
            "     , l.id                 location_id\n" +
            "     , lt.name              location_type_name\n" +
            "     , lt.id                location_type_id\n" +
            "     , dsm.sales_officer_id sales_officer_id\n" +
            "from distributor d\n" +
            "         inner join distributor_sales_officer_map dsm\n" +
            "                    on d.id = dsm.distributor_id\n" +
            "                        and d.is_deleted is false\n" +
            "                        and dsm.is_deleted is false\n" +
            "                        and dsm.is_active is true\n" +
            "                        and dsm.to_date is null\n" +
            "                        and d.id = :distributorId\n" +
            "                        and dsm.company_id = :companyId \n" +
            "         inner join reporting_manager rm\n" +
            "                    on dsm.sales_officer_id = rm.application_user_id\n" +
            "                        and rm.to_date is null\n" +
            "                        and rm.is_active is true\n" +
            "                        and rm.is_deleted is false\n" +
            "         inner join location_manager_map lmm\n" +
            "               on rm.reporting_to_id = lmm.application_user_id\n" +
            "               and lmm.to_date is null and lmm.company_id =:companyId" +
            "                        and lmm.is_deleted is false\n" +
            "                        and lmm.is_active is true\n" +
            "         inner join location l\n" +
            "                    on lmm.location_id = l.id\n" +
            "                        and l.is_active is true\n" +
            "                        and l.is_deleted is false\n" +
            "         inner join location_type lt\n" +
            "                    on lt.id = l.location_type_id\n" +
            "                        and lt.is_deleted is false\n" +
            "                        and lt.is_active is true\n", nativeQuery = true)
    Optional<Map> findDepotAndTerritoryLocationByDistributorId(Long companyId, Long distributorId);

    Optional<Depot> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    @Query(value = "SELECT d.id, d.code, d.depot_name, d.depot_manager_id, d.contact_number, d.address \n" +
            "FROM depot d\n" +
            "WHERE  d.depot_manager_id = :loginUserId\n" +
            "AND d.is_deleted = false \n" +
            "AND d.is_active = true \n" +
            "AND exists(select * from depot_location_map \n" +
            "where company_id = :companyId and depot_id = d.id \n" +
            "and is_deleted = false and is_active = true);", nativeQuery = true)
    Optional<Map> getDepotByLoginUserId(@Param("companyId") Long companyId,
                                 @Param("loginUserId") Long userLoginId);


    @Query(value = "select d.id, d.depot_name as depotName, d.address as depotAddress\n" +
            "from depot d\n" +
            "inner join depot_location_map dlm\n" +
            "        on d.id = dlm.depot_id\n" +
            "       and d.is_central_warehouse is true\n" +
            "       and d.is_deleted is false\n" +
            "       and d.is_active is true\n" +
            "       and dlm.company_id = :companyId", nativeQuery = true)
    List<Map<String, Object>> getCompanyCentralWareHouseInfo(@Param("companyId") Long companyId);

    @Query(value = "SELECT d.id, d.code, d.depot_name, d.depot_manager_id, d.contact_number, d.address \n" +
            "FROM depot d \n" +
            "WHERE \n" +
            "d.is_deleted = false \n" +
            "AND d.is_active = true \n" +
            "AND exists(select * from depot_location_map where company_id = :companyId and depot_id = d.id);",nativeQuery = true)
    List<Map<String, Object>> getCompanyAllDepots(@Param("companyId") Long companyId);

    @Query(value = "select d.id, d.depot_name as depotName, d.address as depotAddress\n" +
            "from depot d\n" +
            "inner join depot_location_map dlm\n" +
            "        on d.id = dlm.depot_id\n" +
            "       and d.is_central_warehouse is true\n" +
            "       and d.depot_manager_id = :userLoginId \n" +
            "       and d.is_deleted is false\n" +
            "       and d.is_active is true\n" +
            "       and dlm.company_id = :companyId \n" +
            "       and dlm.is_active is true and dlm.is_deleted is false limit 1 \n"
            , nativeQuery = true)
    List<Map<String, Object>> getCompanyUserCentralWareHouseInfo(
            @Param("companyId") Long companyId,
            @Param("userLoginId") Long userLoginId);

    @Query(value = "select lmm.company_id, loc.id location_id, dpt.id,dpt.depot_name, \n" +
            "lmm.application_user_id sales_manager_id, rm.application_user_id sales_officer_id \n" +
            "from location loc \n" +
            "inner join depot_location_map dlm on loc.id = dlm.location_id \n" +
            "and dlm.is_active is true and dlm.is_deleted is false and company_id=:companyId\n" +
            "inner join location_manager_map lmm on dlm.location_id = lmm.location_id \n" +
            "and lmm.is_deleted is false and lmm.is_active is true and lmm.company_id=dlm.company_id\n" +
            "inner join reporting_manager rm on lmm.application_user_id = rm.reporting_to_id \n" +
            "and rm.is_active is true and rm.is_deleted is false and rm.to_date is null\n" +
            "and rm.application_user_id=:salesOfficerId\n" +
            "inner join depot dpt on dlm.depot_id = dpt.id;", nativeQuery = true)
    Optional<Map> getSalesOfficerDepotInfo(@Param("companyId") Long companyId,
                                           @Param("salesOfficerId") Long salesOfficerId);

    @Query(value = "select * from  child_product_category_hierarchy pch\n"+
            "inner join (\n" +
            "select i.transaction_type, p.id product_id,\n"+
            "concat(p.name, ' ', p.item_size, ' ', uom.abbreviation, ' * ', ps.pack_size) as product_name,\n" +
            "p.name productName,\n"+
            "p.product_category_id, pc.name product_category,\n"+
            "tf.to_depot_id, dpt.depot_name receive_depot,\n"+
            "tf.from_depot_id, t_dpt.depot_name sent_depot,\n"+
            "sum(itd.quantity) quantity,\n"+
            "sum(itd.rate * itd.quantity) amount, b.batch_no\n"+
            "from inv_transaction_details itd\n"+
            "inner join inv_transaction i on i.id = itd.inv_transaction_id\n"+
            "and itd.is_active is true  and i.company_id= :companyId\n"+
            "and (:startDate is null or i.transaction_date >= :startDate)\n"+
            "and (:endDate is null or i.transaction_date <= :endDate)\n"+
            "and i.transaction_type in :transactionTypes\n"+
            "and (coalesce(:productIds) is null or itd.product_id in (:productIds))\n"+
            "inner join inv_transfer tf on tf.inv_transaction_id = i.id\n"+
            "and (coalesce(:depotIds) is null or tf.from_depot_id in (:depotIds))\n"+
            "and tf.is_active is true\n"+
            "inner join batch b on b.id = itd.batch_id\n"+
            "inner join product p on p.id = itd.product_id\n"+
            "and (coalesce(:categoryIds) is null or p.product_category_id in (:categoryIds))\n"+
            "left join pack_size ps on p.pack_size_id = ps.id\n"+
            "left join unit_of_measurement uom on p.uom_id = uom.id \n"+
            "inner join product_category pc on pc.id = p.product_category_id\n"+
            "inner join depot dpt on tf.to_depot_id = dpt.id\n"+
            "left join depot t_dpt on tf.from_depot_id = t_dpt.id\n"+
            "group by itd.batch_id, itd.product_id,\n"+
            "tf.to_depot_id, tf.from_depot_id) moveinfo \n"+
            "on moveinfo.product_category_id = pch.id"
            , nativeQuery = true)
    List<Map<String, Object>> depotToDepotMovementHistory(
            @Param("companyId") Long companyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("depotIds") List<Long> depotIds,
            @Param("productIds") List<Long> productIds,
            @Param("transactionTypes")List<String> transactionTypes);

    /*
    "left join (select sum(itd1.quantity) receive_quantity,\n" +
            "tf_r.from_depot_id from inv_transfer tf_r\n" +
            "inner join inv_transaction i1 on i1.id = tf_r.inv_transaction_id\n" +
            "and i1.transaction_type in ('TRANSFER_RECEIVE')\n" +
            "inner join inv_transaction_details itd1 on itd1.inv_transaction_id = tf_r.inv_transaction_id\n" +
            "group by tf_r.from_depot_id) as receive\n" +
            "on receive.from_depot_id = tf.to_depot_id\n" +*/

    @Query(value = "select distinct(dpt.id), dpt.depot_name, loc.id location_id \n" +
            "#lmm.application_user_id sales_manager_id, rm.application_user_id sales_officer_id \n" +
            "from location loc \n" +
            "inner join depot_location_map dlm on loc.id = dlm.location_id \n" +
            "and dlm.is_active is true and dlm.is_deleted is false and company_id=:companyId\n" +
            "inner join location_manager_map lmm on dlm.location_id = lmm.location_id \n" +
            "and lmm.company_id=:companyId and lmm.is_deleted is false and lmm.is_active is true\n" +
            "inner join reporting_manager rm on lmm.application_user_id = rm.reporting_to_id \n" +
            "and rm.is_active is true and rm.is_deleted is false \n" +
            "and (COALESCE(:salesOfficerIds, null) is null or rm.application_user_id in (:salesOfficerIds)) \n" +
            "inner join depot dpt on dlm.depot_id = dpt.id group by dpt.id, loc.id;", nativeQuery = true)
    List<Map> getDepotListFiltered(@Param("companyId") Long companyId,
                                       @Param("salesOfficerIds") List salesOfficerIds);

    @Query(value = "select qam.qa_id, qam.depot_id\n" +
            "from depot_quality_assurance_map qam \n" +
            "inner join depot d on d.id = qam.depot_id \n" +
            "and qam.is_active is true and qam.is_deleted is false\n" +
            "and qam.qa_id=:userId\n" +
            "and (:depotId is null or qam.depot_id != :depotId)\n"
            , nativeQuery = true)
    List<Map> findUserQaAssignedStatus(@Param("userId") Long userId, @Param("depotId") Long depotId);

    @Query(value = "select d.depot_manager_id, d.id\n" +
            "from depot d \n" +
            "where d.is_active is true and d.is_deleted is false\n" +
            "and d.depot_manager_id=:userId\n" +
            "and (:depotId is null or d.id != :depotId)\n"
            , nativeQuery = true)
    List<Map> findUserDepotInchargeAssignedStatus(@Param("userId") Long userId,
                                                  @Param("depotId") Long depotId);

    boolean existsByIdAndIsCentralWarehouseTrueAndIsDeletedFalse(Long depotId);
}
