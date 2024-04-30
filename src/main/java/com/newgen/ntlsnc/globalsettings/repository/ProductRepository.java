package com.newgen.ntlsnc.globalsettings.repository;
import com.newgen.ntlsnc.globalsettings.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Temporal;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author kamal
 */

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByIsDeletedFalse();
    @Query(value = "SELECT p.id as productId,\n" +
            "            p.name as productName,\n" +
            "            uom.abbreviation as abbreviation,\n" +
            "            pack_size as packSize,p.item_size as itemSize,\n" +
            "            ptp.trade_price as tradePrice,\n" +
            "            ptp.id as productTradePriceId,\n" +
            "            td.id as tradeDiscountId,\n" +
            "            0 quantity,0 freeQuantity,\n" +
            "            (ptp.trade_price - ifnull(case when td.calculation_type = \"PERCENTAGE\"\n" +
            "            then ((ptp.trade_price * td.discount_value)/100)\n" +
            "            else td.discount_value end,0)) priceWithDiscount,\n" +
            "            0 productWiseTotalAmount,\n" +
            "            '' as salesBookingId,\n" +
            "            '' as salesBookingDetailsId\n" +
            "            FROM product as p\n" +
            "            INNER JOIN unit_of_measurement as uom ON p.uom_id = uom.id\n" +
            "            INNER JOIN  pack_size as ps ON p.pack_size_id = ps.id\n" +
            "            INNER JOIN product_trade_price as ptp ON ptp.product_id=p.id\n" +
            "            LEFT JOIN trade_discount as td ON td.semester_id = :semesterId\n" +
            "            and td.company_id = :companyId\n" +
            "            and td.product_id = p.id\n" +
            "            and td.invoice_nature_id = :invoiceNatureId\n" +
            "            and td.is_active is true and td.is_deleted is false \n" +
            "            where \n" +
            "            expiry_date is null  \n" +
            "                        and p.product_category_id= :productCategoryId\n" +
            "                        and p.is_active is true and p.is_deleted is false\n" +
            "                        and uom.is_active is true and uom.is_deleted is false\n" +
            "                        and ps.is_active is true and ps.is_deleted is false\n" +
            "                        and ptp.is_active is true and ptp.is_deleted is false\n" +
            "                        order by p.name, pack_size ASC", nativeQuery = true)
    List<Map<String, Object>> findAllProductList(@Param("semesterId") Long semesterId, @Param("companyId") Long companyId,@Param("productCategoryId") Long productCategoryId, @Param("invoiceNatureId") Long invoiceNatureId);


    boolean existsByProductCategoryAndIsDeletedFalse(ProductCategory productCategory);

    List<Product> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Product findFirstByOrganizationAndCompanyAndIsDeletedFalse(Organization organization, Organization company);

    List<Product> findAllByProductCategoryAndIsDeletedFalseAndIsActiveTrue(ProductCategory productCategory);

    Optional<Product> findByOrganizationAndNameAndPackSizeIdAndItemSizeAndUomIdAndIsDeletedFalse(
            Organization organization, String name, Long packSizeId,
            Integer itemSize, Long uomId);
    Optional<Product> findByOrganizationAndIdIsNotAndNameAndPackSizeIdAndItemSizeAndUomIdAndIsDeletedFalse(
            Organization organization, Long id, String name, Long packSizeId,
            Integer itemSize, Long uomId);


    Optional<Product> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    @Query(value = "select prod.id, prod.product_sku as productSku, concat(prod.item_size,\" \",uom.description) as itemSize, \n" +
            "concat(prod.name, \" \", prod.item_size, \" \" ,uom.abbreviation, ' * ', ps.pack_size) as productName,\n" +
            "pc.name as productCategory,prod.expiry_days as productExpiryDays,\n" +
            "ps.pack_size as packSize, prod.minimum_stock as minimumStock from product prod   \n" +
            "inner join product_category pc on prod.product_category_id = pc.id \n" +
            "and prod.is_active is true and prod.is_deleted is false and pc.is_active is true\n" +
            "and (:productCategoryId is null or pc.id = :productCategoryId) and prod.company_id=:companyId\n" +
            "left join pack_size ps on prod.pack_size_id = ps.id and ps.is_active is true and ps.is_deleted is false\n"+
            "left join unit_of_measurement uom on prod.uom_id = uom.id \n" +
            "and uom.is_deleted is false\n" +
            "left join batch_receive_stock br on br.product_id = prod.id\n" +
            "and br.receivable_quantity>0\n" +
            "group by prod.id", nativeQuery = true)
    List<Map<String, Object>> findAllProductOfACompany(
            @Param("companyId") Long companyId,
            @Param("productCategoryId") Long productCategoryId);

    List<Product> findAllProductByUomIdAndIsDeletedFalse(Long storeId);

    List<Product> findAllProductByPackSizeIdAndIsDeletedFalse(Long storeId);

    @Query(value = "select sdd.product_id productId, prod.product_sku productSku,\n" +
            "       concat(prod.name, ' ', prod.item_size, ' ', uom.abbreviation, ' * ', ps.pack_size) as productName,\n" +
            "       pc.name as productCategory, sdd.REGULAR regularStock, sdd.QUARANTINE quarantineStock,\n" +
            "       sdd.IN_TRANSIT inTransitStock, sdd.RESTRICTED restrictedStock,\n" +
            "       war.weighted_average_rate weightedAverageRate\n" +
            "\n" +
            "from stock_details_data sdd\n" +
            "inner join product prod\n" +
            "        on sdd.product_id = prod.id \n" +
            "       and sdd.company_id=:companyId\n" +
            "       and sdd.depot_id=:depotId\n" +
            "left join pack_size ps\n" +
            "        on prod.pack_size_id = ps.id\n" +
            "left join unit_of_measurement uom\n" +
            "        on prod.uom_id = uom.id\n" +
            "inner join product_category pc \n" +
            "        on prod.product_category_id = pc.id\n" +
            "       and (:productCategoryId is null or pc.id=:productCategoryId)\n" +
            "inner join weighted_average_rate war\n" +
            "        on sdd.product_id = war.product_id\n" +
            "       and war.company_id=:companyId",nativeQuery = true)
    List<Map<String, Object>> getStockDetailsWithWeightedAverageRate(
            @Param("companyId") Long companyId,
            @Param("depotId") Long depotId,
            @Param("productCategoryId") Long productCategoryId);


    @Query(value = "select inwarprod.product_id, (inwarprod.inwards_quantity - ifnull(outwarprod.outwards_quantity,0)) available_stock,\n" +
            "inwarprod.to_store_id  from\n" +
            "(select sd.product_id, sum(sd.stock_quantity) inwards_quantity, to_store_id  \n" +
            "from stock_data sd where to_store_id=(select id from store \n" +
            "where store_type=:storeType and is_deleted = false) and sd.depot_id=:depotId and sd.company_id=:companyId \n" +
            "and sd.product_id=:productId group by sd.product_id,to_store_id) inwarprod\n" +
            "\t\tleft join \n" +
            "(select product_id,sum(stock_quantity) outwards_quantity, from_store_id  \n" +
            "from stock_data sd where from_store_id=(select id from store \n" +
            "where store_type=:storeType and is_deleted = false) and sd.depot_id=:depotId and sd.company_id=:companyId \n" +
            "and sd.product_id=:productId group by product_id, from_store_id) outwarprod on inwarprod.product_id = outwarprod.product_id \n" +
            "and inwarprod.to_store_id = outwarprod.from_store_id", nativeQuery = true)
    Map<String, Object> getDepotAndProductWiseStoreAvailableQuantity(
            @Param("companyId") Long companyId,
            @Param("depotId") Long depotId,
            @Param("productId") Long productId,
            @Param("storeType") String storeType);

    List<Product> findAllByOrganizationAndPackSizeIdAndIsDeletedFalseAndIsActiveTrue(Organization organization, Long packSizeId);

    @Query(value = "select dpt.id, dpt.depot_name depotName,dpt.address,sdd.regularStock, sdd.quarantineStock, \n" +
            "sdd.inTransitStock, sdd.restrictedStock from \n" +
            "(select depot_id, sum(ifnull(REGULAR,0)) regularStock, \n" +
            "sum(ifnull(QUARANTINE,0)) quarantineStock, sum(ifnull(IN_TRANSIT,0)) inTransitStock, \n" +
            "sum(ifnull(RESTRICTED,0)) restrictedStock from stock_details_data \n" +
            "where company_id=:companyId and (COALESCE(:depotIds) is null or depot_id in (:depotIds)) \n" +
            "group by depot_id) sdd \n" +
            "inner join depot dpt on sdd.depot_id = dpt.id \n" +
            "and (COALESCE(:depotIds) is null OR dpt.id in (:depotIds))", nativeQuery = true)

    List<Map<String, Object>> getDepotStockInfo(
            @Param("companyId") Long companyId,
            @Param("depotIds") List<Long> depotIds);


    @Query(value = "select weighted_average_rate from weighted_average_rate \n" +
            "where company_id=:companyId and product_id=:productId", nativeQuery = true)
    Object getProductWeightedAverageRate(
            @Param("companyId") Long companyId,
            @Param("productId") Long productId);

    @Query(value = "select bwsd.batch_id as batchId, b.batch_no as batchNo,\n" +
            "bwsd.availableQuantity, b.quantity as batchQuantity\n" +
            "from batch_wise_stock_data_damage bwsd\n" +
            "inner join batch b\n" +
            "        on bwsd.batch_id = b.id\n" +
            "and bwsd.organization_id = :organizationId\n" +
            "and bwsd.company_id = :companyId\n" +
            "and bwsd.depot_id = :depotId\n" +
            "and bwsd.product_id = :productId\n" +
            "and bwsd.store_type = :storeType\n" +
            "and bwsd.availableQuantity > 0", nativeQuery = true)
    List<Map<String, Object>> getProductWiseBatchStockInfo(
            @Param("organizationId") Long organizationId,
            @Param("companyId") Long companyId,
            @Param("depotId") Long depotId,
            @Param("productId") Long productId,
            @Param("storeType") String storeType);

    @Query(value = "select \n" +
            "pc.name as productCategory,\n" +
            "p.name productName, \n" +
            "uom.abbreviation as unitOfMeasurement,\n" +
            "@var\\:=:storeName storeName,\n" +
            "(case when @var\\:=:storeType = 'REGULAR' then (sdd.REGULAR)\n" +
            " when  @var\\:=:storeType  = 'QUARANTINE' then (sdd.QUARANTINE)\n" +
            " when  @var\\:=:storeType  = 'IN_TRANSIT' then (sdd.IN_TRANSIT)\n" +
            " when  @var\\:=:storeType  = 'RESTRICTED' then (sdd.RESTRICTED)\n" +
            " else (sdd.REGULAR+sdd.QUARANTINE+sdd.IN_TRANSIT+sdd.RESTRICTED) end)  stockQty,\n" +
            "war.weighted_average_rate weightedAverageRate\n" +
            "from stock_details_data as sdd\n" +
            "INNER JOIN weighted_average_rate as war \n" +
            "ON sdd.company_id=war.company_id and sdd.product_id=war.product_id\n" +
            "and sdd.depot_id = :depotId and sdd.company_id = :companyId\n" +
            "inner join product p on p.id = sdd.product_id\n" +
            "INNER JOIN unit_of_measurement as uom ON p.uom_id = uom.id\n" +
            "inner join product_category pc on p.product_category_id = pc.id", nativeQuery = true)
    List<Map<String, Object>> getStockValuation(
            @Param("depotId") Long depotId,
            @Param("companyId") Long companyId,
            @Param("storeName") String storeName,
            @Param("storeType") String storeType);

    @Query(value = "select REGULAR regularQuantity, BLOCKED blockedQuantity from stock_details_data " +
            "where company_id= :companyId and depot_id= :depotId and product_id=:productId", nativeQuery = true)
    Map<String, Object> getStockBlockedQuantity(
            @Param("companyId") Long companyId,
            @Param("depotId") Long depotId,
            @Param("productId") Long productId);

    @Query(value = "select pc.*, restricted_info.organization_id, restricted_info.company_id,\n" +
            "restricted_info.depot_id, d.depot_name,\n" +
            "date_format(restricted_info.transaction_date, '%Y') year,\n" +
            "concat(prod.name, \" \", prod.item_size, \" \", uom.abbreviation, ' * ', ps.pack_size) productName,\n" +
            "ps.pack_size, b.batch_no batchNo,\n" +
            "restricted_info.transaction_date inclusionDate,\n" +
            "date_add(b.production_date, interval prod.expiry_days day) expiryDate,\n" +
            "ptp.trade_price tradePrice,restricted_info.rate mc, restricted_info.quantity,\n" +
            "uom.abbreviation uom from  \n" +
            "(select invdet.organization_id, invdet.company_id, invdet.depot_id, invtransdet.transaction_date,  \n" +
            "invtransdet.product_id, invtransdet.batch_id,invtransdet.quantity, invtransdet.rate,\n" +
            "invtransdet.from_store_id, invtransdet.to_store_id\n" +
            "from(select issm.inv_transaction_id, issm.organization_id, issm.company_id, issm.depot_id\n" +
            "        from inter_store_stock_movement issm\n" +
            "where issm.company_id=:companyId \n" +
            "and (coalesce(:fromDate) is null or issm.movement_date >=:fromDate)\n" +
            "and (coalesce(:endDate) is null or issm.movement_date <=:endDate)\n" +
            "and issm.is_active is true and issm.is_deleted is false) invdet\n" +
            "                             \tinner join\n" +
            "(select invt.id transactionId, invt.transaction_date, itd.product_id, itd.batch_id,\n" +
            "itd.quantity quantity, itd.rate,\n" +
            "itd.from_store_id, itd.to_store_id\n" +
            "from inv_transaction invt inner join inv_transaction_details itd\n" +
            "on invt.id = itd.inv_transaction_id and invt.company_id=:companyId \n" +
            "and (coalesce(:fromDate) is null or invt.transaction_date >=:fromDate)\n" +
            "and (coalesce(:endDate) is null or invt.transaction_date <=:endDate)\n" +
            ") invtransdet\n" +
            "on invdet.inv_transaction_id = invtransdet.transactionId\n" +
            "and invtransdet.to_store_id=(select id from store where store_type='RESTRICTED'\n" +
            "and is_active is true and is_deleted is false)) restricted_info\n" +
            "inner join product prod on restricted_info.product_id = prod.id\n" +
            "and (coalesce(:productIds) is null or prod.id in (:productIds))\n" +
            "inner join child_product_category_hierarchy pc on prod.product_category_id = pc.id\n" +
            "left join unit_of_measurement uom on prod.uom_id = uom.id and uom.is_active is true\n" +
            "and uom.is_deleted is false\n" +
            "left join pack_size ps on prod.pack_size_id = ps.id and ps.is_active is true\n" +
            "and ps.is_deleted is false\n" +
            "inner join batch b on restricted_info.batch_id = b.id\n" +
            "left join product_trade_price ptp on restricted_info.product_id = ptp.product_id\n" +
            "and ptp.is_active is true\n" +
            "and ptp.is_deleted is false and ptp.expiry_date is null\n" +
            "inner join depot d on d.id = restricted_info.depot_id\n" +
            "            and (coalesce(:depotIds) is null or d.id in(:depotIds))\n" +
            "            inner join (select distinct dlp.depot_id from depot_location_map dlp\n" +
            "            where dlp.company_id=:companyId\n" +
            "            group by dlp.depot_id\n" +
            "            ) depot on d.id = depot.depot_id\n" +
            "where coalesce(:productCategoryId) is null or prod.product_category_id in(:productCategoryId)", nativeQuery = true)
    List<Map<String, Object>> getRestrictedStoreInfo(
            @Param("companyId") Long companyId,
            @Param("productCategoryId") List<Long> productCategoryId,
            @Param("productIds") List<Long> productIds,
            @Param("depotIds") List<Long> depotIds,
            @Param("fromDate") LocalDate fromDate,
            @Param("endDate") LocalDate endDate);


    @Query(value = "select sb.depot_id, pd.product_id, sum(ifnull(pd.quantity, 0)) pickQuantity,\n" +
            "            sum(ifnull(pd.good_qty, 0)) goodQuantity, sum(ifnull(pd.bad_qty, 0)) badQuantity,\n" +
            "            sum(ifnull(itd.quantity, 0)) challanQuantity,\n" +
            "            sum(case when p.status='PENDING' then ifnull(pd.quantity, 0) \n" +
            "            when (p.status='CONFIRMED' and itd.picking_details_id is not null)\n" +
            "            then ifnull(pd.good_qty, 0) - ifnull(itd.quantity, 0)\n" +
            "            when (p.status='CONFIRMED' and itd.picking_details_id is null)\n" +
            "            then ifnull(pd.good_qty, 0)\n" +
            "            else 0 end)  pickedBlockedQuantity\n" +
            "            from picking p\n" +
            "            inner join picking_details pd on p.id = pd.picking_id\n" +
            "            inner join sales_order_details sod on sod.id = pd.sales_order_details_id\n" +
            "            inner join sales_booking_details sbd on sbd.id = sod.sales_booking_details_id\n" +
            "            inner join sales_booking sb on sb.id = sbd.sales_booking_id\n" +
            "            left join inv_transaction_details itd on itd.picking_details_id = pd.id\n" +
            "where p.company_id= :companyId\n" +
            "and sb.depot_id= :depotId\n" +
            "and pd.product_id= :productId\n" +
            "and p.status in (:pickingStatus)\n" +
            "and pd.is_deleted = false and pd.is_active = true\n" +
            "group by pd.product_id, sb.depot_id",
            nativeQuery = true)
    Map<String, Object> getPickedBlockedQuantity(
            @Param("companyId") Long companyId,
            @Param("depotId") Long depotId,
            @Param("productId") Long productId,
            @Param("pickingStatus") List<String> pickingStatus);

    @Query(value = "select * from\n" +
            "(select d.depot_name, prod.id product_id, pc.*, prod.name prodGenericName, \n" +
            "concat(prod.name, \" \", prod.item_size, \" \", uom.abbreviation, ' * ', ps.pack_size) productName,\n" +
            "ps.pack_size packSize, bh.batch_no,\n" +
            "war.weighted_average_rate war, opening_war.weighted_average_rate opening_war,\n" +
            "ifnull(opening_stock_info.opening_quantity,0) openingQuantity,\n" +
            "@stockQuantity \\:= ifnull(periodic_stock_info.prod_quantity,0)\n" +
            "+ifnull(periodic_stock_info.trans_rcv_quantity,0) productionQuantity,\n" +
            "ifnull(periodic_stock_info.ret_quantity,0) returnQuantity,\n" +
            "ifnull(periodic_stock_out_info.chal_quantity,0)\n" +
            "+ifnull(periodic_stock_out_info.trans_sent_quantity,0) challanQuantity,\n" +
            "ifnull(periodic_stock_out_info.damg_quantity,0) damageQuantity,\n" +
            "(ifnull(periodic_stock_info.prod_quantity,0)\n" +
            "+ifnull(periodic_stock_info.trans_rcv_quantity,0)\n" +
            "+ifnull(periodic_stock_info.ret_quantity,0)\n" +
            ")\n" +
            "-(ifnull(periodic_stock_out_info.trans_sent_quantity,0)\n" +
            "+ifnull(periodic_stock_out_info.chal_quantity,0)\n" +
            "+ifnull(periodic_stock_out_info.damg_quantity,0)\n" +
            ") stockQuantity, @stockQuantity+ifnull(opening_stock_info.opening_quantity,0) currentStock\n" +
            "from product prod \n" +
            "inner join batch_details bhd on prod.id = bhd.product_id\n" +
            "and prod.company_id=:companyId and prod.is_active is true\n" +
            "and (coalesce(:productIds) is null or prod.id in (:productIds))\n" +
            "inner join batch bh on bh.id = bhd.batch_id\n" +
            "inner join child_product_category_hierarchy pc on prod.product_category_id = pc.id \n" +
            "#inner join weighted_average_rate war on prod.id = war.product_id\n" +
            "left join unit_of_measurement uom on prod.uom_id = uom.id and uom.is_active is true and uom.is_deleted is false\n" +
            "left join pack_size ps on prod.pack_size_id = ps.id and ps.is_active is true and ps.is_deleted is false\n" +
            "cross join depot d inner join (select distinct dlp.depot_id from depot_location_map dlp\n" +
            "where dlp.company_id=:companyId\n" +
            "and (coalesce(:depotIds) is null or dlp.depot_id in (:depotIds))\n" +
            "group by dlp.depot_id\n" +
            ") depot on d.id = depot.depot_id\n" +
            "\n" +
            "inner join (" +
            "select inwarprod.company_id, inwarprod.product_id, inwarprod.product_category_id,\n" +
            " round((inwarprod.inwards_value - ifnull(outwarprod.outwards_value,0)) \n" +
            " / (inwarprod.inwards_quantity - ifnull(outwarprod.outwards_quantity,0)),4)\n" +
            "weighted_average_rate from\n" +
            "\n" +
            "(select cwsm.company_id, cwsm.product_id, product_category_id,\n" +
            "sum(cwsm.quantity * ifnull(cwsm.rate,0)) inwards_value,\n" +
            "sum(cwsm.quantity) inwards_quantity  from warehouse_stock_movement cwsm\n" +
            "inner join store st on cwsm.to_store_id = st.id\n" +
            "and cwsm.transaction_type in('PRODUCTION_RECEIVE','RETURN')\n" +
            "and (:asOfDate is null or cwsm.transaction_date <=:asOfDate)\n" +
            "group by cwsm.company_id, cwsm.product_id, product_category_id) inwarprod\n" +
            "left join\n" +
            "(select company_id, product_id, product_category_id, sum(quantity * ifnull(rate,0)) outwards_value, sum(quantity) outwards_quantity\n" +
            "from warehouse_stock_movement cwsm \n" +
            "where cwsm.transaction_type='DELIVERY_CHALLAN'\n" +
            "and (:asOfDate is null or cwsm.transaction_date <=:asOfDate)\n" +
            "group by company_id, product_id, product_category_id) outwarprod\n" +
            "on inwarprod.product_id = outwarprod.product_id and inwarprod.company_id = outwarprod.company_id" +
            ") war on war.product_id = prod.id\n" +
            "and (coalesce(:categoryIds) is null or war.product_category_id in (:categoryIds))\n" +
            "and (coalesce(:productIds) is null or war.product_id in (:productIds))\n" +
            "\n" +
            "left join\n" +
            "(select inwarprod.company_id, inwarprod.depot_id, inwarprod.product_id, inwarprod.batch_id,\n" +
            "(inwarprod.inwards_quantity - ifnull(outwarprod.outwards_quantity,0)) opening_quantity from\n" +
            "(select cwsm.company_id, cwsm.depot_id, cwsm.product_id, batch_id, sum(cwsm.quantity * ifnull(cwsm.rate,0)) inwards_value,\n" +
            " sum(cwsm.quantity) inwards_quantity  from warehouse_stock_movement_batch cwsm\n" +
            "inner join store st on cwsm.to_store_id = st.id\n" +
            "and cwsm.transaction_type in('PRODUCTION_RECEIVE','TRANSFER_RECEIVE','RETURN')\n" +
            "and (:fromDate is null or cwsm.transaction_date <:fromDate)\n" +
            "and cwsm.company_id= :companyId\n" +
            "#and product_id=55 and batch_id=363\n" +
            "and (coalesce(:depotIds) is null or cwsm.depot_id in (:depotIds))\n" +
            "and (coalesce(:categoryIds) is null or cwsm.product_category_id in (:categoryIds))\n" +
            "and (coalesce(:productIds) is null or cwsm.product_id in (:productIds))\n" +
            "group by cwsm.company_id, cwsm.depot_id, cwsm.product_id, batch_id) inwarprod\n" +
            "\n" +
            "left join\n" +
            "(select company_id, depot_id, product_id, batch_id, sum(quantity * ifnull(rate,0)) outwards_value,\n" +
            "sum(quantity) outwards_quantity\n" +
            "from warehouse_stock_movement_batch cwsm\n" +
            "where cwsm.transaction_type in ('DELIVERY_CHALLAN','TRANSFER_SENT','DAMAGE')\n" +
            "and (:fromDate is null or cwsm.transaction_date <:fromDate)\n" +
            "#and product_id=55 and batch_id=363\n" +
            "and (coalesce(:depotIds) is null or cwsm.depot_id in (:depotIds))\n" +
            "and (coalesce(:categoryIds) is null or cwsm.product_category_id in (:categoryIds))\n" +
            "and (coalesce(:productIds) is null or cwsm.product_id in (:productIds))\n" +
            "group by company_id, depot_id, product_id, batch_id) outwarprod\n" +
            "on inwarprod.product_id = outwarprod.product_id\n" +
            "and inwarprod.batch_id = outwarprod.batch_id \n" +
            "and inwarprod.depot_id = outwarprod.depot_id \n" +
            "and inwarprod.company_id = outwarprod.company_id) opening_stock_info\n" +
            "on prod.id = opening_stock_info.product_id\n" +
            "and opening_stock_info.batch_id = bh.id\n" +
            "and opening_stock_info.depot_id = d.id\n" +
            "\n" +
            "left join (" +
            "select inwarprod.company_id, inwarprod.product_id, inwarprod.product_category_id,\n" +
            " round((inwarprod.inwards_value - ifnull(outwarprod.outwards_value,0)) \n" +
            " / (inwarprod.inwards_quantity - ifnull(outwarprod.outwards_quantity,0)),4)\n" +
            "weighted_average_rate from\n" +
            "\n" +
            "(select cwsm.company_id, cwsm.product_id, product_category_id,\n" +
            "sum(cwsm.quantity * ifnull(cwsm.rate,0)) inwards_value,\n" +
            "sum(cwsm.quantity) inwards_quantity  from warehouse_stock_movement cwsm\n" +
            "inner join store st on cwsm.to_store_id = st.id\n" +
            "and cwsm.transaction_type in('PRODUCTION_RECEIVE','RETURN')\n" +
            "and (:asOfDate is null or cwsm.transaction_date <:asOfDate)\n" +
            "group by cwsm.company_id, cwsm.product_id, product_category_id) inwarprod\n" +
            "left join\n" +
            "(select company_id, product_id, product_category_id,\n" +
            "sum(quantity * ifnull(rate,0)) outwards_value, sum(quantity) outwards_quantity\n" +
            "from warehouse_stock_movement cwsm \n" +
            "where cwsm.transaction_type='DELIVERY_CHALLAN'\n" +
            "and (:asOfDate is null or cwsm.transaction_date <:asOfDate)\n" +
            "group by company_id, product_id, product_category_id) outwarprod\n" +
            "on inwarprod.product_id = outwarprod.product_id\n" +
            "and inwarprod.company_id = outwarprod.company_id\n" +
            ") opening_war on opening_war.product_id = prod.id\n" +
            "and (coalesce(:categoryIds) is null or opening_war.product_category_id in (:categoryIds))\n" +
            "and (coalesce(:productIds) is null or opening_war.product_id in (:productIds))\n" +
            "\n" +
            "left join\n" +
            "(select wsm.company_id, wsm.depot_id, wsm.product_category_id, wsm.product_id,  wsm.batch_id, wsm.batch_no,\n" +
            "sum(case when wsm.transaction_type = 'PRODUCTION_RECEIVE' then ((wsm.quantity)) end) as prod_quantity,\n" +
            "sum(case when wsm.transaction_type = 'TRANSFER_RECEIVE' then ((wsm.quantity)) end) as trans_rcv_quantity,\n" +
            "sum(case when wsm.transaction_type = 'RETURN' then ((wsm.quantity)) end) as ret_quantity\n" +
            "from warehouse_stock_movement_batch wsm\n" +
            "where wsm.company_id =:companyId\n" +
            "#and product_id=55 and batch_id=363\n" +
            "and (:fromDate is null or wsm.transaction_date >=:fromDate)\n" +
            "and (:endDate is null or wsm.transaction_date <=:endDate)\n" +
            "and (coalesce(:depotIds) is null or wsm.depot_id in (:depotIds))\n" +
            "and (coalesce(:categoryIds) is null or wsm.product_category_id in (:categoryIds))\n" +
            "and (coalesce(:productIds) is null or wsm.product_id in (:productIds))\n" +
            "group by wsm.company_id, wsm.product_id, wsm.depot_id, wsm.product_category_id,\n" +
            "wsm.batch_id, wsm.batch_no) periodic_stock_info\n" +
            "on periodic_stock_info.product_id = prod.id\n" +
            "and periodic_stock_info.batch_id = bh.id\n" +
            "and periodic_stock_info.depot_id = d.id\n" +
            "\n" +
            "left join\n" +
            "(select wsm.company_id, wsm.depot_id, wsm.product_category_id, wsm.product_id,  wsm.batch_id, wsm.batch_no,\n" +
            "sum(case when wsm.transaction_type = 'TRANSFER_SENT' then ((wsm.quantity)) end) as trans_sent_quantity,\n" +
            "sum(case when wsm.transaction_type = 'DELIVERY_CHALLAN' then ((wsm.quantity)) end) as chal_quantity,\n" +
            "sum(case when wsm.transaction_type = 'DAMAGE' then ((wsm.quantity)) end) as damg_quantity\n" +
            "from warehouse_stock_movement_batch wsm\n" +
            "where wsm.company_id =:companyId\n" +
            "#and product_id=55 and batch_id=363\n" +
            "and (:fromDate is null or wsm.transaction_date >=:fromDate)\n" +
            "and (:endDate is null or wsm.transaction_date <=:endDate)\n" +
            "and (coalesce(:depotIds) is null or wsm.depot_id in (:depotIds))\n" +
            "and (coalesce(:categoryIds) is null or wsm.product_category_id in (:categoryIds))\n" +
            "and (coalesce(:productIds) is null or wsm.product_id in (:productIds))\n" +
            "group by wsm.company_id, wsm.product_id, wsm.depot_id, wsm.product_category_id,\n" +
            "wsm.batch_id, wsm.batch_no) periodic_stock_out_info\n" +
            "on periodic_stock_out_info.product_id = prod.id\n" +
            "and periodic_stock_out_info.batch_id = bh.id\n" +
            "and periodic_stock_out_info.depot_id = d.id\n" +
            "\n" +
            "order by d.id, prod.name, prod.id, bhd.batch_id) stock\n" +
            "where currentStock > 0;"
            , nativeQuery = true)
    List<Map<String, Object>> getInventoryStockValuation(
            @Param("companyId") Long companyId,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("depotIds") List<Long> depotIds,
            @Param("productIds") List<Long> productIds,
            @Param("fromDate") LocalDate fromDate,
            @Param("endDate") LocalDate endDate,
            @Param("asOfDate") LocalDate asOfDate
    ); //,@Param("storeId") Long storeId

    @Query(value = "select sdd.company_id, sdd.product_id, sdd.depot_id, d.depot_name, d.code as depot_code,\n" +
            "ifnull(sdd.REGULAR,0) - ifnull(sdd.BLOCKED,0) as stock_qty\n" +
            "from stock_details_data as sdd\n" +
            "inner join depot d on d.id = sdd.depot_id\n" +
            "where sdd.company_id = :companyId \n" +
            "and sdd.product_id = :productId and (ifnull(sdd.REGULAR,0) - ifnull(sdd.BLOCKED,0)) >0;", nativeQuery = true)
    List<Map<String, Object>> getProductWiseDepotList(
            @Param("companyId") Long companyId,
            @Param("productId") Long productId);

    @Query(value = "select p.id, p.name as productName,\n"+
            "concat(p.name, \"  \", p.item_size, \"  \" , uom.abbreviation, ' * ', ps.pack_size) as name\n" +
            "from product as p \n" +
            "left join pack_size ps on p.pack_size_id = ps.id\n"+
            "and ps.is_active is true and ps.is_deleted is false\n"+
            "left join unit_of_measurement uom on p.uom_id = uom.id\n" +
            "and uom.is_deleted is false\n"+
            "where p.product_category_id in (:productCategoryId) and p.company_id = :companyId \n" +
            "and p.is_active is true and p.is_deleted is false\n"
            , nativeQuery = true)
    List<Map<String, Object>>getProductList(
            @Param("companyId") Long companyId,
            @Param("productCategoryId") Long productCategoryId);

    @Query(value = "select p.id, p.name from product as p \n" +
            "inner join product_trade_price tp \n" +
            "on p.id = tp.product_id and tp.expiry_date is null and tp.is_deleted is false\n" +
            "left join trade_discount td \n" +
            "on p.id = td.product_id and td.is_deleted is false\n" +
            "left join sales_booking_details sbd \n" +
            "on p.id = sbd.product_id and sbd.is_deleted is false\n" +
            "left join inv_transaction_details itd \n" +
            "on p.id = itd.product_id and itd.is_deleted is false\n" +
            "where  \n" +
            "p.id = :product_id and p.is_deleted is false limit 1", nativeQuery = true)
    Map<String, Object>findProductUsage(
            @Param("product_id") Long productId);

    @Procedure
    void SNC_PRODUCT_CATEGORY_HIERARCHY(long companyId);

    @Query(value = "select pc.*, prod.id productId, prod.product_sku as `Product Sku`,prod.name 'Product Name',\n" +
            "            ps.pack_size  `Carton Size`, \n" +
            "            prod.item_size `Pack Size`,\n" +
            "            uom.abbreviation as UoM, \n" +
            "            '' `Opening Stock`,\n" +
            "            '' `Rate`,\n" +
            "            '' `Value`\n" +
            "            from product prod   \n" +
            "            inner join child_product_category_hierarchy pc on prod.product_category_id = pc.id \n" +
            "            and prod.is_active is true and prod.is_deleted is false and prod.company_id=:companyId\n" +
            "            inner join pack_size ps on prod.pack_size_id = ps.id and ps.is_active is true and ps.is_deleted is false\n" +
            "            inner join unit_of_measurement uom on prod.uom_id = uom.id and uom.is_deleted is false\n" +
            "            order by pc.id \n", nativeQuery = true)
    List<Map<String, Object>> getProductDetailsListForStockOpeningExcelDownloadData(
            @Param("companyId") Long companyId);

    Optional<Product> findByNameIgnoreCaseAndItemSizeAndCompanyIdAndUomAndIsDeletedFalseAndIsActiveTrue(String productName, Integer itemSize, Long companyId, UnitOfMeasurement unitOfMeasurement);

    @Query(value = "select * from (select d.id depotId, d.depot_name depotName, pc.*,\n" +
            "periodic_stock_info.product_id productId,\n" +
            "concat(prod.name, \" \", prod.item_size, \" \", uom.abbreviation, ' * ', ps.pack_size) productName,\n" +
            "uom.abbreviation, war.weighted_average_rate war,\n" +
            "ifnull(periodic_stock_info.prod_quantity,0) productionQuantity,\n" +
            "ifnull(periodic_stock_info.trans_rcv_quantity,0) transRcvQuantity,\n" +
            "ifnull(periodic_stock_info.ret_quantity,0) returnQuantity,\n" +
            "ifnull(periodic_stock_out_info.trans_sent_quantity,0) transSentQuantity,\n" +
            "ifnull(periodic_stock_out_info.chal_quantity,0) challanQuantity,\n" +
            "ifnull(periodic_stock_out_info.damg_quantity,0) damageQuantity,\n" +
            "(ifnull(periodic_stock_info.prod_quantity,0)\n" +
            "+ifnull(periodic_stock_info.trans_rcv_quantity,0)\n" +
            "+ifnull(periodic_stock_info.ret_quantity,0)\n" +
            ")\n" +
            "-(ifnull(periodic_stock_out_info.trans_sent_quantity,0)\n" +
            "+ifnull(periodic_stock_out_info.chal_quantity,0)\n" +
            "+ifnull(periodic_stock_out_info.damg_quantity,0)\n" +
            ") stockQuantity\n" +
            "from\n" +
            "(select wsm.company_id, wsm.depot_id, wsm.product_category_id, wsm.product_id,\n" +
            "sum(case when wsm.transaction_type = 'PRODUCTION_RECEIVE' then ((wsm.quantity)) end) as prod_quantity,\n" +
            "sum(case when wsm.transaction_type = 'TRANSFER_RECEIVE' then ((wsm.quantity)) end) as trans_rcv_quantity,\n" +
            "sum(case when wsm.transaction_type = 'RETURN' then ((wsm.quantity)) end) as ret_quantity\n" +
            "from warehouse_stock_movement_batch wsm\n" +
            "where wsm.company_id = :companyId\n" +
            "and (:fromDate is null or wsm.transaction_date >= :fromDate)\n" +
            "and (:endDate is null or wsm.transaction_date <= :endDate)\n" +
            "and (:storeId is null or wsm.to_store_id =:storeId)\n" +
            "and (COALESCE(:depotIds) is null or wsm.depot_id in (:depotIds))\n" +
            "and (COALESCE(:categoryIds) is null or wsm.product_category_id in (:categoryIds))\n" +
            "and (COALESCE(:productIds) is null or wsm.product_id in (:productIds))\n" +
            "group by wsm.company_id, wsm.product_id, wsm.depot_id, wsm.product_category_id\n" +
            ") periodic_stock_info\n" +
            "\n" +
            "left join\n" +
            "(select wsm.company_id, wsm.depot_id, wsm.product_category_id, wsm.product_id,\n" +
            "sum(case when wsm.transaction_type = 'TRANSFER_SENT' then ((wsm.quantity)) end) as trans_sent_quantity,\n" +
            "sum(case when wsm.transaction_type = 'DELIVERY_CHALLAN' then ((wsm.quantity)) end) as chal_quantity,\n" +
            "sum(case when wsm.transaction_type = 'DAMAGE' then ((wsm.quantity)) end) as damg_quantity\n" +
            "from warehouse_stock_movement_batch wsm\n" +
            "where wsm.company_id = :companyId\n" +
            "and (:fromDate is null or wsm.transaction_date >= :fromDate)\n" +
            "and (:endDate is null or wsm.transaction_date <= :endDate)\n" +
            "and (:storeId is null or wsm.from_store_id =:storeId)\n" +
            "and (COALESCE(:depotIds) is null or wsm.depot_id in (:depotIds))\n" +
            "and (COALESCE(:categoryIds) is null or wsm.product_category_id in (:categoryIds))\n" +
            "and (COALESCE(:productIds) is null or wsm.product_id in (:productIds))\n" +
            "group by wsm.company_id, wsm.product_id, wsm.depot_id, wsm.product_category_id\n" +
            ") periodic_stock_out_info\n" +
            "on periodic_stock_info.product_id=periodic_stock_out_info.product_id\n" +
            "and periodic_stock_info.depot_id=periodic_stock_out_info.depot_id\n" +
            "and periodic_stock_info.product_category_id=periodic_stock_out_info.product_category_id\n" +
            "\n" +
            "inner join product prod on periodic_stock_info.product_id = prod.id\n" +
            "inner join depot d on periodic_stock_info.depot_id = d.id\n" +
            "inner join child_product_category_hierarchy pc on prod.product_category_id = pc.id \n" +
            "#inner join product_category pc on periodic_stock_info.product_category_id = pc.id\n" +
            "left join unit_of_measurement uom on prod.uom_id = uom.id and uom.is_active is true and uom.is_deleted is false\n" +
            "left join pack_size ps on prod.pack_size_id = ps.id and ps.is_active is true and ps.is_deleted is false\n" +
            "inner join weighted_average_rate war on periodic_stock_info.product_id = war.product_id\n" +
            "order by d.id, prod.id) stock  where stockQuantity >0;"
            , nativeQuery = true)
    List<Map<String, Object>> getInventoryStockValuationSummary(
            @Param("companyId") Long companyId,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("depotIds") List<Long> depotIds,
            @Param("productIds") List<Long> productIds,
            @Param("fromDate") LocalDate fromDate,
            @Param("endDate") LocalDate endDate,
            @Param("storeId") Long storeId);

    @Query(value = "select * from (select d.id depotId, d.depot_name depotName, pc.*,\n" +
            "periodic_stock_info.product_id productId,\n" +
            "concat(prod.name, \" \", prod.item_size, \" \", uom.abbreviation, ' * ', ps.pack_size) productName,\n" +
            "uom.abbreviation, war.weighted_average_rate war,\n" +
            "periodic_stock_info.transaction_date, periodic_stock_info.batch_id,\n" +
            "bch.batch_no, bch.production_date,\n" +
            "ifnull(periodic_stock_info.prod_quantity,0) productionQuantity,\n" +
            "ifnull(periodic_stock_info.trans_rcv_quantity,0) transRcvQuantity,\n" +
            "ifnull(periodic_stock_info.ret_quantity,0) returnQuantity,\n" +
            "ifnull(periodic_stock_out_info.trans_sent_quantity,0) transSentQuantity,\n" +
            "ifnull(periodic_stock_out_info.chal_quantity,0) challanQuantity,\n" +
            "ifnull(periodic_stock_out_info.damg_quantity,0) damageQuantity,\n" +
            "@stock_quantity \\:= (ifnull(periodic_stock_info.prod_quantity,0)\n" +
            "+ifnull(periodic_stock_info.trans_rcv_quantity,0)\n" +
            "+ifnull(periodic_stock_info.ret_quantity,0)\n" +
            "+ifnull(periodic_stock_info.inter_move_in_quantity,0)\n" +
            ")\n" +
            "-(ifnull(periodic_stock_out_info.trans_sent_quantity,0)\n" +
            "+ifnull(periodic_stock_out_info.chal_quantity,0)\n" +
            "+ifnull(periodic_stock_out_info.damg_quantity,0)\n" +
            "+ifnull(periodic_stock_out_info.inter_move_out_quantity,0)\n" +
            ") stockQuantity,\n" +
            "datediff(:asOfDate, periodic_stock_info.transaction_date) days,\n" +
            "if(datediff(:asOfDate, periodic_stock_info.transaction_date) between 1 and 30, @stock_quantity, 0) + 0.0 as level1,\n" +
            "if(datediff(:asOfDate, periodic_stock_info.transaction_date) between 31 and 60, @stock_quantity, 0) + 0.0 as level31,\n" +
            "if(datediff(:asOfDate, periodic_stock_info.transaction_date) between 61 and 90, @stock_quantity, 0) + 0.0 as level61,\n" +
            "if(datediff(:asOfDate, periodic_stock_info.transaction_date) between 91 and 120, @stock_quantity, 0) + 0.0 as level91,\n" +
            "if(datediff(:asOfDate, periodic_stock_info.transaction_date) between 121 and 180, @stock_quantity, 0) + 0.0 as level121,\n" +
            "if(datediff(:asOfDate, periodic_stock_info.transaction_date) > 180, @stock_quantity, 0) + 0.0 as level181\n" +
            "from\n" +
            "(select wsm.company_id, wsm.depot_id, wsm.product_category_id, wsm.product_id,\n" +
            "wsm.batch_id, min(wsm.transaction_date) transaction_date,\n" +
            "sum(case when wsm.transaction_type = 'INTER_STORE_MOVEMENT' then ((wsm.quantity)) end) as inter_move_in_quantity,\n" +
            "sum(case when wsm.transaction_type = 'PRODUCTION_RECEIVE' then ((wsm.quantity)) end) as prod_quantity,\n" +
            "sum(case when wsm.transaction_type = 'TRANSFER_RECEIVE' then ((wsm.quantity)) end) as trans_rcv_quantity,\n" +
            "sum(case when wsm.transaction_type = 'RETURN' then ((wsm.quantity)) end) as ret_quantity\n" +
            "from warehouse_stock_movement_all wsm\n" +
            "where wsm.company_id = :companyId\n" +
            "and (:fromDate is null or wsm.transaction_date >= :fromDate)\n" +
            "and (:endDate is null or wsm.transaction_date <= :endDate)\n" +
            "and (:storeId is null or wsm.to_store_id =:storeId)\n" +
            "and (COALESCE(:depotIds) is null or wsm.depot_id in (:depotIds))\n" +
            "and (COALESCE(:categoryIds) is null or wsm.product_category_id in (:categoryIds))\n" +
            "and (COALESCE(:productIds) is null or wsm.product_id in (:productIds))\n" +
            "group by wsm.company_id, wsm.product_id, wsm.depot_id,\n" +
            "wsm.product_category_id, wsm.batch_id\n" +
            ") periodic_stock_info\n" +
            "\n" +
            "left join\n" +
            "(select wsm.company_id, wsm.depot_id, wsm.product_category_id,\n" +
            "wsm.product_id, wsm.batch_id,\n" +
            "sum(case when wsm.transaction_type = 'INTER_STORE_MOVEMENT' then ((wsm.quantity)) end) as inter_move_out_quantity,\n" +
            "sum(case when wsm.transaction_type = 'TRANSFER_SENT' then ((wsm.quantity)) end) as trans_sent_quantity,\n" +
            "sum(case when wsm.transaction_type = 'DELIVERY_CHALLAN' then ((wsm.quantity)) end) as chal_quantity,\n" +
            "sum(case when wsm.transaction_type = 'DAMAGE' then ((wsm.quantity)) end) as damg_quantity\n" +
            "from warehouse_stock_movement_all wsm\n" +
            "where wsm.company_id = :companyId\n" +
            "and (:fromDate is null or wsm.transaction_date >= :fromDate)\n" +
            "and (:endDate is null or wsm.transaction_date <= :endDate)\n" +
            "and (:storeId is null or wsm.from_store_id =:storeId)\n" +
            "and (COALESCE(:depotIds) is null or wsm.depot_id in (:depotIds))\n" +
            "and (COALESCE(:categoryIds) is null or wsm.product_category_id in (:categoryIds))\n" +
            "and (COALESCE(:productIds) is null or wsm.product_id in (:productIds))\n" +
            "group by wsm.company_id, wsm.product_id, wsm.depot_id,\n" +
            "wsm.product_category_id, wsm.batch_id\n" +
            "#wsm.transaction_date\n" +
            ") periodic_stock_out_info\n" +
            "on periodic_stock_info.product_id=periodic_stock_out_info.product_id\n" +
            "and periodic_stock_info.batch_id=periodic_stock_out_info.batch_id\n" +
            "and periodic_stock_info.depot_id=periodic_stock_out_info.depot_id\n" +
            "and periodic_stock_info.product_category_id=periodic_stock_out_info.product_category_id\n" +
            "\n" +
            "inner join product prod on periodic_stock_info.product_id = prod.id\n" +
            "inner join batch bch on periodic_stock_info.batch_id = bch.id\n" +
            "inner join depot d on periodic_stock_info.depot_id = d.id\n" +
            "inner join child_product_category_hierarchy pc on prod.product_category_id = pc.id \n" +
            "#inner join product_category pc on periodic_stock_info.product_category_id = pc.id\n" +
            "left join unit_of_measurement uom on prod.uom_id = uom.id and uom.is_active is true and uom.is_deleted is false\n" +
            "left join pack_size ps on prod.pack_size_id = ps.id and ps.is_active is true and ps.is_deleted is false\n" +
            "inner join weighted_average_rate war on periodic_stock_info.product_id = war.product_id\n" +
            "order by d.id, prod.id) stock  where stockQuantity >0;"
            , nativeQuery = true)
    List<Map<String, Object>> getFinishedGoodsAgeing(
            @Param("companyId") Long companyId,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("depotIds") List<Long> depotIds,
            @Param("productIds") List<Long> productIds,
            @Param("fromDate") LocalDate fromDate,
            @Param("endDate") LocalDate endDate,
            @Param("storeId") Long storeId,
            @Param("asOfDate") LocalDate asOfDate);


    @Query(value = "select * from (select prod.id, prod.product_sku as productSku, concat(prod.item_size,\" \",uom.description) as itemSize, \n" +
            "concat(prod.name, \" \", prod.item_size, \" \" ,uom.abbreviation, ' * ', ps.pack_size) as productName,\n" +
            "pc.name as productCategory,prod.expiry_days as productExpiryDays,\n" +
            "ifnull(ppr.receivable_quantity, 0) receivable_quantity, ps.pack_size as packSize, prod.minimum_stock as minimumStock from product prod   \n" +
            "inner join product_category pc on prod.product_category_id = pc.id \n" +
            "and prod.is_active is true and prod.is_deleted is false and pc.is_active is true\n" +
            "and (:productCategoryId is null or pc.id = :productCategoryId) and prod.company_id=:companyId\n" +
            "left join pack_size ps on prod.pack_size_id = ps.id and ps.is_active is true and ps.is_deleted is false\n"+
            "left join unit_of_measurement uom on prod.uom_id = uom.id \n" +
            "and uom.is_deleted is false\n" +
            "left join\n" +
            "product_production_receivable ppr on ppr.product_id = prod.id\n" +
            "group by prod.id) product " +
            "where  receivable_quantity > 0 ", nativeQuery = true)
    List<Map<String, Object>> findAllProductionRecievableProduct(
            @Param("companyId") Long companyId,
            @Param("productCategoryId") Long productCategoryId);

}
