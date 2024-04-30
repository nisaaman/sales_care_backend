package com.newgen.ntlsnc.supplychainmanagement.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.supplychainmanagement.entity.Picking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author kamal
 * @Date ১৮/৪/২২
 */

@Repository
public interface PickingRepository extends JpaRepository<Picking, Long> {
    List<Picking> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<Picking> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    @Query(value = "select p.id picking_id, so.id, so.order_no, sb.booking_no,\n" +
            "so.order_date, sb.booking_date,\n" +
            "sum(ifnull(pd.good_qty, 0))\n" +
            "from picking p \n" +
            "inner join picking_details pd on pd.picking_id = p.id\n" +
            "inner join sales_order so on so.id = pd.sales_order_id\n" +
            "inner join sales_booking sb on sb.id = so.sales_booking_id\n" +
            "where p.id = :pickingId\n" +
            "and sb.is_active is true and sb.is_deleted is false\n" +
            "and so.is_active is true and so.is_deleted is false\n" +
            "and pd.is_active is true and pd.is_deleted is false\n" +
            "and p.is_active is true and p.is_deleted is false\n" +
            "group by so.id\n", nativeQuery = true)
    List<Map<String, Object>> getOrderListByPickingId(@Param("pickingId") Long pickingId);

    @Query(value = "SELECT pp.company_id, pp.picking_no, pp.id picking_id, pd.product_id,\n" +
            "concat(p.name,' ',p.item_size,uom.abbreviation,'*',ps.pack_size) as p_name,\n" +
            "p.product_sku, pd.quantity picking_quantity,\n" +
            "pc.name category_name, so.id sales_order_id, so.order_no\n" +
            "FROM picking_details pd\n" +
            "inner join sales_order_details sod on sod.id = pd.sales_order_details_id\n" +
            "inner join sales_order so on so.id = sod.sales_order_id\n" +
            "inner join picking pp on pp.id = pd.picking_id\n" +
            "inner join product p on pd.product_id = p.id\n" +
            "inner join product_category pc on p.product_category_id = pc.id \n" +
            "INNER JOIN pack_size as ps ON p.pack_size_id = ps.id\n" +
            "INNER JOIN unit_of_measurement as uom ON p.uom_id = uom.id\n" +
            "where pp.id=:pickingId and pp.company_id=:companyId", nativeQuery = true)
    List<Map<String, Object>> findByPickingList(@Param("pickingId") Long pickingId, @Param("companyId") Long companyId);
}
