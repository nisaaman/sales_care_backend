package com.newgen.ntlsnc.supplychainmanagement.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author marziah
 * @Date 17/04/22
 */

@Repository
public interface InvTransferRepository extends JpaRepository<InvTransfer, Long> {
    List<InvTransfer> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<InvTransfer> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    Optional<InvTransfer> findByInvTransactionIdAndIsActiveTrueAndIsDeletedFalse(Long id);
    Optional<InvTransfer> findByInvTransferIdAndIsActiveTrueAndIsDeletedFalse(Long id);


    @Query(value = "select invtransfdet.depotName, invtransfdet.transferDate,\n" +
            "invtransfdet.depotIncharge, invtransfdet.designation, invtransfdet.transferNo, invtransfdet.transferId,\n" +
            "invtransdet.quantity from (select invtrans.id transactionId, d.depot_name depotName, \n" +
            "invt.transfer_date transferDate, invt.id transferId, au.name depotIncharge, dsg.name designation, \n" +
            "invt.transfer_no transferNo from inv_transfer invt \n" +
            "inner join inv_transaction invtrans on invt.inv_transaction_id = invtrans.id \n" +
            "and invt.company_id =:companyId and invt.is_active is true and invt.is_deleted is false \n" +
            "and invt.transfer_date >= :startDate and invt.transfer_date <= :endDate \n" +
            "and invtrans.transaction_type='TRANSFER_SENT'\n" +
            "and (:depotId is null or invt.from_depot_id =:depotId)\n"+
            "inner join application_user au on invt.created_by = au.id\n" +
            "left join designation dsg on au.designation_id = dsg.id\n" +
            "inner join depot d on invt.to_depot_id = d.id) invtransfdet\n" +
            "inner join \n" +
            "(select it.id transactionId, sum(itd.quantity) quantity \n" +
            "from inv_transaction_details itd \n" +
            "inner join inv_transaction it on itd.inv_transaction_id = it.id \n" +
            "and it.transaction_type='TRANSFER_SENT' and it.company_id=:companyId \n" +
            "and it.transaction_date >= :startDate and it.transaction_date <= :endDate \n" +
            "group by it.id) invtransdet \n" +
            "on invtransfdet.transactionId = invtransdet.transactionId", nativeQuery = true)
    List<Map<String, Object>> getInvTransferDetails(
            @Param("companyId") Long companyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("depotId") Long depotId);

    @Query(value = "SELECT id " +
            "FROM inv_transfer t \n" +
            "WHERE t.inv_transfer_id = :invTransferId " +
            "and is_active is true and is_deleted is false "
            , nativeQuery = true)
    Map getTransferReceived(@Param("invTransferId") Long invTransferId);

}
