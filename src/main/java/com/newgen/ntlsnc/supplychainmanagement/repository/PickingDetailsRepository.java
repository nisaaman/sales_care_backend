package com.newgen.ntlsnc.supplychainmanagement.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.supplychainmanagement.entity.Picking;
import com.newgen.ntlsnc.supplychainmanagement.entity.PickingDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author kamal
 * @Date ১৮/৪/২২
 */

@Repository
public interface PickingDetailsRepository extends JpaRepository<PickingDetails, Long> {
    List<PickingDetails> findAllByPicking(Picking picking);
    Optional<PickingDetails> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);
    List<PickingDetails> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    List<PickingDetails> findAllByOrganizationAndSalesOrderIdAndSalesOrderDetailsIdAndIsActiveTrueAndIsDeletedFalse(Organization organization, Long orderId, Long orderDetailsId);

    @Query(value = "SELECT * FROM picking_details \n" +
            "where sales_order_details_id = :salesOrderDetailsId \n" +
            "And picking_id = :pickingId \n" +
            "And is_active is true AND is_deleted is false", nativeQuery = true)
    PickingDetails getPickingDetailsByPickingIdAndSalesOrderDetailsId(Long salesOrderDetailsId, Long pickingId);

    @Query(value = "select pd.id picking_details_id, pd.sales_order_id order_id, pic.status,\n" +
            "so.order_no, pd.sales_order_details_id, pd.picking_id,pd.product_id,\n" +
            "sod.quantity order_quantity, pd.quantity picking_quantity, ifnull(pd.good_qty, 0)good_qty, ifnull(pd.bad_qty, 0)bad_qty, \n" +
            "pd.reason, p.name p_name, p.product_sku, p.product_category_id,\n" +
            "concat(p.name, ' ', p.item_size, ' ', uom.abbreviation, ' * ', pk.pack_size) product_name,\n" +
            "pc.name product_category_name,\n" +
            "case when inv.quantity is not null\n" +
            "then inv.quantity else 0 end order_challan_quantity\n" +
            "from picking_details pd \n" +
            "inner join sales_order_details sod on sod.id = pd.sales_order_details_id\n" +
            "inner join product p on p.id = pd.product_id\n" +
            "inner join pack_size as pk on p.pack_size_id = pk.id\n" +
            "inner join unit_of_measurement as uom on p.uom_id = uom.id\n" +
            "inner join product_category pc on pc.id = p.product_category_id\n" +
            "inner join sales_order so on so.id = sod.sales_order_id\n" +
            "inner join picking pic on pic.id = pd.picking_id\n" +
            "left join (select sum(ifnull(i.quantity,0)) quantity,\n" +
            "i.sales_order_details_id\n" +
            "from inv_transaction_details i\n" +
            "group by i.sales_order_details_id) as inv\n" +
            "on pd.sales_order_details_id = inv.sales_order_details_id\n" +
            //"and pd.id = inv.picking_details_id\n" +i.picking_details_id, and inv.picking_details_id is null
            "where pd.picking_id = :pickingId\n" +
            "and pc.is_active is true and pc.is_deleted is false\n" +
            "and p.is_active is true and p.is_deleted is false \n" +
            "and pd.is_active is true and pd.is_deleted is false",nativeQuery = true)
    List<Map> getProductListByPickingId(@Param("pickingId") Long pickingId);

    @Query(value = "select pd.quantity picking_quantity,\n" +
            "ifnull(pd.good_qty, 0) good_qty, ifnull(pd.bad_qty, 0) bad_qty,\n" +
            "ifnull(itd.quantity,0) deliverdQuantity, \n" +
            "case when itd.picking_details_id is null \n" +
            "then (ifnull(pd.good_qty, 0)) - ifnull(itd.quantity, 0) else 0 end undeliveredQuantity \n" +
            "from picking p\n" +
            "inner join picking_details pd on p.id = pd.picking_id\n" +
            "left join inv_transaction_details itd on itd.sales_order_details_id = pd.sales_order_details_id\n" +
            "and itd.picking_details_id = pd.id\n" +
            "where pd.sales_order_details_id = :salesOrderDetailsId \n" +
            "and pd.product_id = :productId\n" +
            "and p.status = 'CONFIRMED'\n" +
            "and pd.is_active is true and pd.is_deleted is false\n"
            , nativeQuery = true)
    List<Map> getPickedConfirmedUndeliverdQuantityBySalesOrderDetailsId(
            Long productId, Long salesOrderDetailsId);

}
