package com.newgen.ntlsnc.supplychainmanagement.repository;

import com.newgen.ntlsnc.salesandcollection.entity.SalesOrderDetails;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvDeliveryChallan;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvTransaction;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvTransactionDetails;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author kamal
 * @Date ১৭/৪/২২
 */

@Repository
public interface InvTransactionDetailsRepository extends JpaRepository<InvTransactionDetails, Long> {
    List<InvTransactionDetails> findAllByInvTransaction(InvTransaction invTransaction);
    List<InvTransactionDetails> findAllByInvTransactionIn(Set<InvTransaction> invTransactions);

    List<InvTransactionDetails> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    InvTransactionDetails findInvTransactionDetailsById(Long id);

    List<InvTransactionDetails> findAllInvTransactionDetailsByInvTransactionId(Long id);

    Optional<InvTransactionDetails> findByPickingDetailsIdAndIsActiveTrueAndIsDeletedFalse(Long id);


    @Query(value = "SELECT * " +
            "FROM inv_transaction_details td \n" +
            "WHERE td.sales_order_details_id = :sales_order_details_id ", nativeQuery = true)
    Map findOrderItemDelivery(@Param("sales_order_details_id") Long sales_order_details_id);

    @Query(value = "SELECT td.id, td.inv_transaction_id, \n" +
            "td.product_id, td.quantity, sod.sales_order_id, ch.id challan_id \n" +
            "FROM inv_transaction_details td \n" +

            "INNER JOIN inv_delivery_challan ch \n" +
            "ON td.inv_transaction_id = ch.inv_transaction_id \n" +

            "INNER JOIN sales_order_details sod \n" +
            "ON td.sales_order_details_id = sod.id \n" +

            "WHERE td.sales_order_details_id in :salesOrderDetailsList ", nativeQuery = true)
    List<Map> findOrderItemsDelivery(@Param("salesOrderDetailsList") List<SalesOrderDetails> salesOrderDetailsList);

    /*@Query(value = "SELECT td.inv_transaction_id, \n" +
            "td.product_id, td.quantity, sod.sales_order_id, " +
            "ch.id challan_id, invoice.id sales_invoice_id, invoice.is_accepted \n" +
            "FROM sales_booking_details sbd " +

            "INNER JOIN sales_order_details sod \n" +
            "ON sbd.id = sod.sales_booking_details_id \n" +

            "INNER JOIN inv_transaction_details td \n" +
            "ON sod.id = td.sales_order_details_id \n" +

            "INNER JOIN inv_delivery_challan ch \n" +
            "ON td.inv_transaction_id = ch.inv_transaction_id \n" +

            "INNER JOIN sales_invoice_challan_map invoice_map \n" +
            "ON ch.id = invoice_map.inv_delivery_challan_id \n" +

            "INNER JOIN sales_invoice invoice \n" +
            "ON invoice_map.sales_invoice_id = invoice.id \n" +

            "WHERE sbd.sales_booking_id = :booking_id \n" +
            "AND sbd.sales_booking_status IN ('ORDER_CONVERTED', \n" +
            " 'TICKET_CONFIRMED', 'TICKET_REQUESTED' ) ", nativeQuery = true)
    List<Map> findInvoiceOfBooking(@Param("booking_id") Long bookingId);*/

    @Query(value = "SELECT  invoice.id sales_invoice_id, invoice.is_accepted \n" +
            "FROM sales_booking_details sbd " +

            "INNER JOIN sales_order_details sod \n" +
            "ON sbd.id = sod.sales_booking_details_id \n" +

            "INNER JOIN inv_transaction_details td \n" +
            "ON sod.id = td.sales_order_details_id \n" +

            "INNER JOIN inv_delivery_challan ch \n" +
            "ON td.inv_transaction_id = ch.inv_transaction_id \n" +

            "INNER JOIN sales_invoice_challan_map invoice_map \n" +
            "ON ch.id = invoice_map.inv_delivery_challan_id \n" +

            "INNER JOIN sales_invoice invoice \n" +
            "ON invoice_map.sales_invoice_id = invoice.id \n" +

            "WHERE sbd.sales_booking_id = :booking_id \n" +
            "AND sbd.sales_booking_status IN ('ORDER_CONVERTED', \n" +
            " 'TICKET_CONFIRMED', 'TICKET_REQUESTED' ) \n" +
            "group by invoice.id", nativeQuery = true)
    List<Map> findInvoiceOfBooking(@Param("booking_id") Long bookingId);

    @Query(value = "select stock.company_id,  \n" +
            "stock.product_id, stock.name, stock.product_sku, \n" +
            "stock.receive_quantity, stock.return_quantity,   \n" +
            "stock.challan_quantity, stock.transfer_rcv_quantity, stock.inter_move_quantity, stock.trans_sent_quantity, \n" +
            "(stock.transfer_rcv_quantity + stock.receive_quantity + stock.return_quantity + stock.inter_move_quantity) \n" +
            "-(stock.challan_quantity - stock.trans_sent_quantity) as stock_quantity, \n" +
            "(stock.transfer_rcv_quantity_uom + stock.receive_quantity_uom + stock.return_quantity_uom + stock.inter_move_quantity_uom) \n" +
            "-(stock.challan_quantity_uom - stock.trans_sent_quantity_uom) as stock_quantity_uom \n" +

                "from (select it.company_id, p.id product_id, p.name, p.product_sku, \n" +
                "SUM(CASE WHEN it.transaction_type='PRODUCTION_RECEIVE' " +
                    "and :depotId is null or r.depot_id=:depotId \n" +
                    "and itd.to_store_id=:storeId \n" +
                "THEN quantity ELSE 0 END) AS receive_quantity, \n" +
                "SUM(CASE WHEN it.transaction_type='RETURN' " +
                    "and :depotId is null or sr.depot_id=:depotId \n " +
                    "and itd.to_store_id=:storeId \n" +
                "THEN quantity ELSE 0 END) AS return_quantity, \n" +
                "SUM(CASE WHEN it.transaction_type='DELIVERY_CHALLAN' " +
                    "and :depotId is null or c.depot_id=:depotId \n" +
                    "and itd.from_store_id=:storeId \n" +
                "THEN quantity ELSE 0 END) AS challan_quantity, \n" +
                "SUM(CASE WHEN it.transaction_type='TRANSFER_RECEIVE' " +
                    "and :depotId is null or t.to_depot_id=:depotId \n" +
                    "and itd.to_store_id=:storeId \n" +
                "THEN quantity ELSE 0 END) AS transfer_rcv_quantity, \n" +
                "SUM(CASE WHEN it.transaction_type='INTER_STORE_MOVEMENT' " +
                    "and :depotId is null or sv.depot_id=:depotId \n" +
                    "and itd.to_store_id=:storeId \n" +
                "THEN quantity ELSE 0 END) AS inter_move_quantity, \n" +
                "SUM(CASE WHEN it.transaction_type='TRANSFER_SENT' " +
                    "and :depotId is null or ts.from_depot_id=:depotId \n" +
                    "and itd.from_store_id=:storeId \n" +
                "THEN itd.quantity ELSE 0 END) AS trans_sent_quantity, \n" +

                "SUM(CASE WHEN it.transaction_type='PRODUCTION_RECEIVE' " +
                "and :depotId is null or r.depot_id=:depotId \n" +
                "and itd.to_store_id=:storeId \n" +
                "THEN quantity_in_uom ELSE 0 END) AS receive_quantity_uom, \n" +
                "SUM(CASE WHEN it.transaction_type='RETURN' " +
                "and :depotId is null or sr.depot_id=:depotId \n " +
                "and itd.to_store_id=:storeId \n" +
                "THEN quantity_in_uom ELSE 0 END) AS return_quantity_uom, \n" +
                "SUM(CASE WHEN it.transaction_type='DELIVERY_CHALLAN' " +
                "and :depotId is null or c.depot_id=:depotId \n" +
                "and itd.from_store_id=:storeId \n" +
                "THEN quantity_in_uom ELSE 0 END) AS challan_quantity_uom, \n" +
                "SUM(CASE WHEN it.transaction_type='TRANSFER_RECEIVE' " +
                "and :depotId is null or t.to_depot_id=:depotId \n" +
                "and itd.to_store_id=:storeId \n" +
                "THEN quantity_in_uom ELSE 0 END) AS transfer_rcv_quantity_uom, \n" +
                "SUM(CASE WHEN it.transaction_type='INTER_STORE_MOVEMENT' " +
                "and :depotId is null or sv.depot_id=:depotId \n" +
                "and itd.to_store_id=:storeId \n" +
                "THEN quantity_in_uom ELSE 0 END) AS inter_move_quantity_uom, \n" +
                "SUM(CASE WHEN it.transaction_type='TRANSFER_SENT' " +
                "and :depotId is null or ts.from_depot_id=:depotId \n" +
                "and itd.from_store_id=:storeId \n" +
                "THEN quantity_in_uom ELSE 0 END) AS trans_sent_quantity_uom \n" +

            "from inv_transaction it \n" +
                "inner join inv_transaction_details itd on it.id=itd.inv_transaction_id \n" +
                "and (itd.qa_status is null or itd.qa_status = 'PASS') \n" +
                "left join inv_transfer t on it.id=t.inv_transaction_id \n" +
                "and (:depotId is null or t.to_depot_id = :depotId) \n" +
                "left join inv_receive r on it.id=r.inv_transaction_id \n" +
                "and (:depotId is null or r.depot_id = :depotId) \n" +
                "left join inv_delivery_challan c on it.id=c.inv_transaction_id \n" +
                "and (:depotId is null or c.depot_id = :depotId) \n" +
                "left join sales_return sr on it.id=sr.inv_transaction_id \n" +
                "and (:depotId is null or sr.depot_id = :depotId) \n" +
                "left join inter_store_stock_movement sv on it.id=sv.inv_transaction_id \n" +
                "and (:depotId is null or sv.depot_id = :depotId) \n" +
                "left join inv_transfer ts on it.id=ts.inv_transaction_id \n" +
                "and (:depotId is null or ts.from_depot_id = :depotId) \n" +
                "inner join product p on p.id=itd.product_id \n" +
                "where itd.product_id = :productId \n" +
                "and it.company_id = :companyId \n" +
                "and itd.is_active is true and itd.is_deleted is false \n" +

                "group by itd.product_id) as stock \n"+
            "group by stock.product_id \n"

            , nativeQuery = true)
    Map getProductStock(@Param("companyId") Long companyId,
                           @Param("productId") Long productId,
                           @Param("depotId") Long depotId,
                            @Param("storeId") Long storeId);

    List<InvTransactionDetails> findByToStoreIdAndIsDeletedFalse(Long storeId);


    @Query(value = "select DISTINCT(stock.batch_id) id, \n" +
            "stock.batch_no, stock.batchQuantity, stock.stock_quantity, \n" +
            "stock.stock_quantity_in_uom, stock.uom_id, stock.product_id, " +
            "stock.product_uom \n" +
            "from \n" +
                "(select  b.id batch_id, b.batch_no, b.quantity as batchQuantity, p.uom_id, p.id product_id,  \n" +
                "uom.abbreviation product_uom, \n" +
                "( SUM(CASE WHEN it.transaction_type='PRODUCTION_RECEIVE' " +
                "and itd.to_store_id=:storeId " +
                "and r.depot_id=:depotId \n" +
                "THEN itd.quantity ELSE 0 END) + \n" +
                "SUM(CASE WHEN it.transaction_type='TRANSFER_RECEIVE' " +
                "and itd.to_store_id=:storeId " +
                "and t.to_depot_id=:depotId \n" +
                "THEN itd.quantity ELSE 0 END) + \n" +
                "SUM(CASE WHEN it.transaction_type='INTER_STORE_MOVEMENT' " +
                "and itd.to_store_id=:storeId \n" +
                "and sv.depot_id=:depotId \n" +
                "THEN itd.quantity ELSE 0 END) + \n" +
                "SUM(CASE WHEN it.transaction_type='RETURN' " +
                "and itd.to_store_id=:storeId " +
                "and sr.depot_id=:depotId \n" +
                "THEN itd.quantity ELSE 0 END)) - \n" +
                "( SUM(CASE WHEN it.transaction_type='DELIVERY_CHALLAN' " +
                "and itd.from_store_id=:storeId " +
                "and c.depot_id=:depotId \n" +
                "THEN itd.quantity ELSE 0 END) + \n" +
                "SUM(CASE WHEN it.transaction_type='TRANSFER_SENT' " +
                "and itd.from_store_id=:storeId " +
                "and ft.from_depot_id=:depotId \n" +
                "THEN itd.quantity ELSE 0 END)) stock_quantity, \n" +

                "( SUM(CASE WHEN it.transaction_type='PRODUCTION_RECEIVE' " +
                "and itd.to_store_id=:storeId " +
                "and r.depot_id=:depotId \n" +
                "THEN itd.quantity_in_uom ELSE 0 END) + \n" +
                "SUM(CASE WHEN it.transaction_type='TRANSFER_RECEIVE' " +
                "and itd.to_store_id=:storeId " +
                "and t.to_depot_id=:depotId \n" +
                "THEN itd.quantity_in_uom ELSE 0 END) + \n" +
                "SUM(CASE WHEN it.transaction_type='INTER_STORE_MOVEMENT' " +
                "and sv.depot_id=:depotId \n" +
                "and itd.to_store_id=:storeId \n" +
                "THEN itd.quantity_in_uom ELSE 0 END) + \n" +
                "SUM(CASE WHEN it.transaction_type='RETURN' " +
                "and itd.to_store_id=:storeId " +
                "and sr.depot_id=:depotId \n" +
                "THEN itd.quantity_in_uom ELSE 0 END)) - \n" +
                "( SUM(CASE WHEN it.transaction_type='DELIVERY_CHALLAN' " +
                "and c.depot_id=:depotId \n" +
                "and itd.to_store_id=:storeId " +
                "THEN itd.quantity_in_uom ELSE 0 END) + \n" +
                "SUM(CASE WHEN it.transaction_type='TRANSFER_SENT' " +
                "and itd.to_store_id=:storeId " +
                "and ft.from_depot_id=:depotId \n" +
                "THEN itd.quantity_in_uom ELSE 0 END)) stock_quantity_in_uom \n" +

                "from batch b \n" +

                "left join inv_transaction_details itd on b.id = itd.batch_id \n" +
                "and (itd.qa_status IS NULL OR itd.qa_status = 'PASS') \n" +

                "left join inv_transaction it on it.id = itd.inv_transaction_id \n" +

                "left join inv_receive r on it.id=r.inv_transaction_id \n" +
                "and (:depotId is NULL OR r.depot_id = :depotId) \n" +

                "left join inv_delivery_challan c on it.id=c.inv_transaction_id \n" +
                "and (:depotId is NULL OR c.depot_id = :depotId) \n" +

                "left join inv_transfer t on it.id=t.inv_transaction_id \n" +
                "and (:depotId is NULL OR t.to_depot_id = :depotId) \n" +

                "left join inv_transfer ft on it.id=ft.inv_transaction_id \n" +
                "and (:depotId is NULL OR ft.from_depot_id = :depotId) \n" +

                "left join sales_return sr on it.id=sr.inv_transaction_id \n" +
                "and (:depotId is NULL OR sr.depot_id = :depotId) \n" +

                "left join inter_store_stock_movement sv on it.id=sv.inv_transaction_id \n" +
                "and (:depotId is null or sv.depot_id = :depotId) \n" +

                "inner join product p on p.id = itd.product_id \n" +
                "inner join unit_of_measurement uom on uom.id = p.uom_id \n" +

                "where it.company_id = :company_id \n" +
                "and itd.product_id = :product_id \n" +
                "and itd.is_active is true AND itd.is_deleted is false \n" +

                "GROUP BY it.company_id, itd.product_id, itd.batch_id) stock " +
            "where stock.stock_quantity > 0 " +
            "", nativeQuery = true)
    List<Map> getProductBatchListWithStock(@Param("company_id") Long companyId,
                                           @Param("product_id") Long productId,
                                           @Param("depotId") Long depotId,
                                           @Param("storeId") Long storeId);

    @Query(value = "select weighted_average_rate from\n" +
            "weighted_average_rate war\n" +
            "where war.company_id = :company_id\n" +
            "and war.product_id = :product_id\n" +
            /*"select stock_value / stock_quantity weighted_average_rate, \n" +
            "stock.stock_value, stock.stock_quantity \n" +
            "from \n" +
            "(select  \n" +
            "( " +
            "SUM(CASE WHEN it.transaction_type='PRODUCTION_RECEIVE' and itd.rate is not null \n" +
            "THEN itd.quantity * itd.rate ELSE 0 END) + \n" +
            "SUM(CASE WHEN it.transaction_type='TRANSFER_RECEIVE' and itd.rate is not null \n" +
            "THEN itd.quantity * itd.rate ELSE 0 END) + \n" +
            "SUM(CASE WHEN it.transaction_type='RETURN' and itd.rate is not null \n" +
            "THEN itd.quantity * itd.rate ELSE 0 END) " +
            ") - \n" +
            "( " +
            "SUM(CASE WHEN it.transaction_type='DELIVERY_CHALLAN' and itd.rate is not null \n" +
            "THEN itd.quantity * itd.rate ELSE 0 END) + \n" +
            "SUM(CASE WHEN it.transaction_type='TRANSFER_SENT' and itd.rate is not null \n" +
            "THEN itd.quantity * itd.rate ELSE 0 END) " +
            ") stock_value, \n" +

            "( " +
            "SUM(CASE WHEN it.transaction_type='PRODUCTION_RECEIVE' and itd.quantity is not null \n" +
            "THEN itd.quantity ELSE 0 END) + \n" +
            "SUM(CASE WHEN it.transaction_type='TRANSFER_RECEIVE' and itd.quantity is not null \n" +
            "THEN itd.quantity ELSE 0 END) + \n" +
            "SUM(CASE WHEN it.transaction_type='RETURN' and itd.quantity is not null \n" +
            "THEN itd.quantity ELSE 0 END) " +
            ") - \n" +
            "( " +
            "SUM(CASE WHEN it.transaction_type='DELIVERY_CHALLAN' and itd.quantity is not null \n" +
            "THEN itd.quantity ELSE 0 END) + \n" +
            "SUM(CASE WHEN it.transaction_type='TRANSFER_SENT' and itd.quantity is not null \n" +
            "THEN itd.quantity ELSE 0 END) " +
            ") stock_quantity \n" +

            "from inv_transaction_details itd \n" +
            "inner join inv_transaction it on it.id = itd.inv_transaction_id \n" +
            "left join inv_receive r on r.id=itd.inv_transaction_id \n" +
            "left join inv_delivery_challan c on c.id=itd.inv_transaction_id \n" +
            "left join inv_transfer t on t.id=itd.inv_transaction_id \n" +
            "left join inv_transfer ft on t.id=itd.inv_transaction_id \n" +
            "left join sales_return sr on sr.id=itd.inv_transaction_id \n" +

            "where it.organization_id = :organization_id \n" +
            "and itd.product_id = :product_id \n" +
            "and itd.is_active is true and itd.is_deleted is false \n" +

            "group by it.organization_id, itd.product_id) stock " +
            "where stock.stock_quantity > 0 " +*/
            "", nativeQuery = true)
    Float getWeightedAverageRate(
            @Param("product_id") Long productId,
            @Param("company_id") Long companyId);
}

