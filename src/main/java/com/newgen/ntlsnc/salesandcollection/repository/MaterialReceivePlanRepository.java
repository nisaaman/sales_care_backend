package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.salesandcollection.entity.MaterialReceivePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author sagor
 * @date ১২/৪/২২
 */
@Repository
public interface MaterialReceivePlanRepository extends JpaRepository<MaterialReceivePlan, Long> {
    MaterialReceivePlan findByIdAndIsDeletedFalse(Long id);

    List<MaterialReceivePlan> findAllByOrganizationAndIsDeletedFalse(Organization organization);



    Optional<MaterialReceivePlan> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    @Query(value = "SELECT " +
            "mp.id, mp.sales_booking_details_id, \n" +
            "mp.company_id, \n" +
            "sbd.product_id, p.name product_name, p.product_sku, \n" +
            "sbd.sales_booking_status item_status, mp.ticket_status, \n" +
            "DATEDIFF( mp.commitment_date, mp.ticket_date ) as ticket_days_left, \n" +
            "DATE_FORMAT(mp.ticket_date, '%d %b %Y') as ticket_date, mp.quantity,\n" +
            "mp.ticket_date ticket_date_without_format, \n" +
            "DATE_FORMAT(mp.require_date, '%d %b %Y') as require_date, \n" +
            "DATE_FORMAT(mp.commitment_date, '%d %b %Y') as commitment_date, \n" +
            "DATE_FORMAT(mp.ticket_status_date, '%d %b %Y') as ticket_status_date, \n" +
            "mp.ticket_status_date ticket_status_date_without_format \n" +
            "FROM sales_booking_details sbd \n" +
            "inner join material_receive_plan mp \n" +
            "on sbd.id = mp.sales_booking_details_id \n" +
            "inner join product p \n" +
            "on sbd.product_id = p.id \n" +
            "WHERE mp.sales_booking_details_id = :booking_item_id \n" +
            "and mp.is_active is true and mp.is_deleted is false \n" +
            "", nativeQuery = true)
    Map<String, Object> getTicket(@Param("booking_item_id") Long bookingItemId);

    @Query(value ="select mrp.id, mrp.ticket_date, ifnull(mrp.quantity, 0) ticket_quantity, mrp.sales_booking_details_id,mrp.ticket_status,\n" +
            "sbd.quantity as booking_quantity, sbd.sales_booking_id,\n" +
            "sb.booking_no, sb.booking_date, sb.depot_id,\n" +
            "depot.code, depot.depot_name, sb.distributor_id,\n" +
            "d.distributor_name,sbd.product_id,concat(p.name, ' ' , p.item_size,' ',uom.abbreviation,' * ',pk.pack_size) as product_name,\n" +
            "p.product_category_id, pc.name as category_name, p.product_sku,\n" +
            "sb.semester_id, s.semester_name,\n" +
            "mrp.organization_id, mrp.confirm_quantity, mrp.company_id,\n" +
            "ifnull(sdd.REGULAR,0) as stock_quantity\n" +
            "from material_receive_plan mrp \n" +
            "inner join sales_booking_details sbd on sbd.id = mrp.sales_booking_details_id\n" +
            "inner join sales_booking sb on sb.id = sbd.sales_booking_id\n" +
            "inner join semester s on s.id = sb.semester_id\n" +
            "inner join depot on depot.id = sb.depot_id\n" +
            "inner join distributor d on d.id = sb.distributor_id\n" +
            "inner join product p on p.id = sbd.product_id\n" +
            " left join stock_details_data sdd on sdd.product_id = sbd.product_id and sdd.depot_id = sb.depot_id\n" +
            "inner join unit_of_measurement uom  on uom.id = p.uom_id\n" +
            "inner join pack_size pk  on pk.id = p.pack_size_id\n" +
            "inner join product_category pc  on pc.id = p.product_category_id\n" +
            "where mrp.company_id = :companyId\n" +
            "and (:semesterId is null or sb.semester_id = :semesterId)\n" +
            "and (:depotId is null or sb.depot_id = :depotId)\n" +
            "\n" +
            "and s.is_deleted is false\n" +
            "and pc.is_deleted is false\n" +
            "and pk.is_deleted is false\n" +
            "and uom.is_deleted is false\n" +
            "and p.is_deleted is false\n" +
            "and d.is_deleted is false\n" +
            "and depot.is_deleted is false \n" +
            "and sb.is_deleted is false\n" +
            "and sbd.is_deleted is false\n" +
            "and mrp.is_active is true and mrp.is_deleted is false\n" +
            "order by mrp.id desc" ,nativeQuery = true)
    List<Map<String, Object>>findTicketCompanyWise(@Param("companyId") Long companyId, @Param("depotId") Long depotId, @Param("semesterId") Long semesterId);

    @Query(value = "select * from\n" +
            "            (select mrp.company_id, lmm.location_id,\n" +
            "            sum(sbd.quantity) as quantity,\n" +
            "            sum(mrp.quantity) as ticket_quantity, sum((mrp.quantity * ptp.trade_price)) as ticketBeforeDiscountValue,\n" +
            "            concat(p.name, ' ', p.item_size, ' ', u.abbreviation, ' * ', pk.pack_size) productName,\n" +
            "            pc.name subCategory, pc_parent.name category, sb.depot_id,\n" +
            "            sum((mrp.quantity * ptp.trade_price)) - SUM(CASE \n" +
            "            WHEN td.discount_value IS NULL THEN 0 \n" +
            "            WHEN td.discount_value > 0\n" +
            "            AND td.calculation_type IS NOT NULL \n" +
            "            AND td.calculation_type = 'PERCENTAGE'\n" +
            "            THEN (td.discount_value/100) * (mrp.quantity * ptp.trade_price)\n" +
            "            WHEN td.discount_value > 0 \n" +
            "            AND td.calculation_type IS NOT NULL \n" +
            "            AND td.calculation_type = 'EQUAL'\n" +
            "            THEN td.discount_value * mrp.quantity END) ticketValue\n" +
            "            from material_receive_plan mrp\n" +
            "            inner join sales_booking_details sbd on sbd.id = mrp.sales_booking_details_id\n" +
            "            inner join sales_booking sb on sb.id = sbd.sales_booking_id\n" +
            "            and sb.company_id = :companyId\n" +
            "            and (COALESCE(:depotIds) is null or sb.depot_id in(:depotIds)) \n" +
            "            inner join product p on sbd.product_id = p.id\n" +
            "            inner join unit_of_measurement u on p.uom_id = u.id\n" +
            "            inner join pack_size pk  on pk.id = p.pack_size_id\n" +
            "            inner join product_category pc on p.product_category_id = pc.id\n" +
            "            inner join product_category pc_parent on pc_parent.id = pc.parent_id\n" +
            "            inner join product_trade_price ptp on ptp.id = sbd.product_trade_price_id\n" +
            "            inner join application_user au on sb.sales_officer_id = au.id\n" +
            "            inner join reporting_manager rm on sb.sales_officer_id = rm.application_user_id\n" +
            "            and rm.to_date is null\n" +
            "            inner join location_manager_map lmm\n" +
            "            on rm.reporting_to_id = lmm.application_user_id\n" +
            "            and lmm.company_id = :companyId and lmm.to_date is null\n" +
            "            inner join location lo on lmm.location_id = lo.id\n" +
            "            left join trade_discount td on sbd.trade_discount_id = td.id\n" +
            "            and sbd.product_id = td.product_id\n" +
            "            where (:startDate is null or mrp.ticket_date >= :startDate)\n" +
            "            and (:endDate is null or mrp.ticket_date <= :endDate)\n" +
            "            and (COALESCE(:categoryIds) is null or p.product_category_id in(:categoryIds))\n" +
            "            and ptp.is_deleted is false\n" +
            "            and sbd.is_deleted is false\n" +
            "            and mrp.is_deleted is false\n" +
            "            and mrp.company_id = :companyId\n" +
            "            group by lmm.location_id, sbd.product_id, sb.depot_id) ticket_info\n" +
            "            inner join child_location_hierarchy lo_hierarchy\n" +
            "            on ticket_info.location_id = lo_hierarchy.id\n" +
            "            and (COALESCE(:locationIds) is null or lo_hierarchy.id in(:locationIds))", nativeQuery = true)
    List<Map<String, Object>> getMaterialPlannerTicket(
            @Param("companyId") Long companyId,
            @Param("locationIds") List<Long> locationIds,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("depotIds") List<Long> depotIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

}
