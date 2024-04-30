package com.newgen.ntlsnc.globalsettings.repository;

import com.newgen.ntlsnc.globalsettings.entity.Location;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.PaymentBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author anika
 * @Date ১/৬/২২
 */
@Repository
public interface PaymentBookRepository extends JpaRepository<PaymentBook, Long> {
    List<PaymentBook> findAllByOrganizationAndIsDeletedFalse(Organization organization);


    Optional<PaymentBook> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    @Query(value = "select \n" +
            "\tpb.id,book_number as bookNumber,\n" +
            "\tissue_date as issueDate,\n" +
            "\tto_mr_no as toMrNo,\n" +
            "\tfrom_mr_no as fromMrNo,\n" +
            "\tau.name as userName,\n" +
            "\tpb.is_active as status,\n" +
            "\tpb.company_id as companyId,\n" +
            "\tl.id as locationId,\n" +
            "\td.name as designation,\n" +
            "\to.name as companyName,\n" +
            "\tl.name as Territory,\n" +
            "\tpc.payment_book_id as paymentBookId,\n" +
            "\tpc.bookCount\n" +
            "from \n" +
            "\tpayment_book as pb\n" +
            "inner join \n" +
            "\tapplication_user as au \n" +
            "    on pb.created_by=au.id\n" +
            "    and au.is_deleted is false\n" +
            "inner join \n" +
            "\torganization as o \n" +
            "    on pb.company_id=o.id\n" +
            "    and o.is_deleted is false\n" +
            "inner join \n" +
            "\tlocation as l \n" +
            "    on pb.location_id=l.id\n" +
            "    and l.is_deleted is false\n" +
            "inner join \n" +
            "\tdesignation as d \n" +
            "    on au.designation_id=d.id\n" +
            "    and d.is_deleted is false\n" +
            "left join (\n" +
            "\tselect \n" +
            "\t\tcount(c.id) bookCount, \n" +
            "\t\tb.id payment_book_id \n" +
            "    from \n" +
            "\t\tpayment_collection c \n" +
            "\tinner join  \n" +
            "\t\tpayment_book b  \n" +
            "        on c.payment_book_id = b.id\n" +
            "\t\tand b.company_id=:companyId\n" +
            "\t\tand b.location_id=:paymentBookLocationId\n" +
            "\t\tand b.is_deleted is false\n" +
            "        and c.is_deleted is false\n" +
            "\t\tgroup by b.id\n" +
            "\t) pc \n" +
            "    on pc.payment_book_id = pb.id\n" +
            "\twhere pb.company_id=:companyId\n" +
            "\tand pb.location_id=:paymentBookLocationId\n" +
            "\tand pb.is_deleted is false;", nativeQuery = true)
    List<Map> findAllByCompanyIdAndPaymentBookLocationId(@Param("companyId") Long companyId, @Param("paymentBookLocationId") Long paymentBookLocationId);

    Optional<PaymentBook> findByOrganizationAndCompanyAndBookNumberIgnoreCaseAndIsDeletedFalse(Organization organization, Organization company, String trim);

    Optional<PaymentBook> findByOrganizationAndCompanyAndIdIsNotAndBookNumberIgnoreCaseAndIsDeletedFalse(Organization organization, Organization company, Long id, String trim);

    PaymentBook findByIdAndIsDeletedFalse(Long id);

    Optional<PaymentBook> findByCompanyIdAndPaymentBookLocationIdAndIsActiveTrue(Long companyId, Long paymentBookLocationId);

    @Query(value = "select pc.money_receipt_no\n" +
            "from payment_collection pc\n"+
            "inner join payment_book pb on pc.payment_book_id = pb.id\n" +
            "and pb.is_active is true\n" +
            "and pb.is_deleted is false\n" +
            "where pc.payment_book_id = :payment_book_id\n" +
            "order by pc.money_receipt_no desc\n" +
            "limit 1"
            ,nativeQuery = true)
    Long findLatestMoneyReceiptNo(@Param("payment_book_id") Long paymentBookId);

}
