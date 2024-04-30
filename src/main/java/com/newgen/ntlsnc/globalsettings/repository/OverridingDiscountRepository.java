package com.newgen.ntlsnc.globalsettings.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.OverridingDiscount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author sagor
 * @date ৯/৪/২২
 */
@Repository
public interface OverridingDiscountRepository extends JpaRepository<OverridingDiscount, Long> {
    OverridingDiscount findByIdAndIsDeletedFalse(Long id);

    List<OverridingDiscount> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    List<OverridingDiscount> findAllByCompanyAndIsDeletedFalse(Organization company);

    @Query(value = "SELECT SUM(cd.invoice_id) total_invoice, \n" +
            "SUM(p.collection_amount) collection_amount, \n" +
            "SUM(pj.adjusted_amount) adjusted_amount, \n" +
            "SUM(cd.amount) ord_amount, \n" +
            "p.company_id, d.id, d.distributor_name , d.contact_no\n" +
            "FROM payment_collection p \n" +
            "INNER JOIN payment_collection_adjustment pj on p.id = pj.payment_collection_id \n" +
            "INNER JOIN credit_debit_note cd on pj.sales_invoice_id = cd.invoice_id \n" +
            "INNER JOIN application_user au on p.collection_by_id = au.id \n" +
            "INNER JOIN distributor d on p.distributor_id = d.id \n" +

            "WHERE (:company_id is NULL OR p.company_id = :company_id) \n" +
            "AND (:start_date is NULL OR p.action_taken_date >= :start_date) \n" +
            "AND (:end_date is NULL OR p.action_taken_date <= :end_date) \n" +
            "AND p.collection_by_id IN (:soList) \n" +
            "AND pj.is_active is true AND pj.is_deleted is false \n" +
            "AND p.approval_status='APPROVED' \n" +
            "AND p.is_active is true and p.is_deleted is false \n" +
            "GROUP BY p.company_id, p.distributor_id "
            ,nativeQuery = true)
    List<Map<String, Object>>  getOrdList(@Param("company_id") Long companyId
                    , @Param("start_date") LocalDateTime startDate
                    , @Param("end_date") LocalDateTime endDate
                    , @Param("soList") List<Long> soList);

    @Query("select od " +
            "from OverridingDiscount od " +
            "         inner join Semester s on od.semester.id = s.id and od.isDeleted is false " +
            "    and od.company.id = :companyId " +
            "    and od.invoiceNature.id = :invoiceNatureId " +
            "    and od.approvalStatus = 'APPROVED' " +
            "    and :day between od.fromDay and od.toDay and :invoiceDate between s.startDate and s.endDate " +
            "    and s.isDeleted is false")
    OverridingDiscount findByCompanyIdAndInvoiceNatureIdAndSemesterDateAndDay(@Param("companyId") Long companyId,
                                                                                               @Param("invoiceNatureId") Long invoiceNatureId,
                                                                                               @Param("invoiceDate") LocalDate invoiceDate,
                                                                                               @Param("day") Integer day);

    Optional<OverridingDiscount> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);
    Optional<OverridingDiscount> findById(Long id);

    @Query(value = "select sinv.id,sinv.invoice_no,sinv.invoice_amount as invoiceAmount,\n" +
            "sum(pca.adjusted_amount) as adjustedInvoiceAmount,sum(ifnull(cdn.amount,0)) as ordAmount \n" +
            "from payment_collection_adjustment pca \n" +
            "inner join payment_collection pc on pca.payment_collection_id = pc.id \n" +
            "and pc.approval_status=\"APPROVED\" and pca.company_id = :companyId and pc.company_id= :companyId \n" +
            "and pc.collection_by_id = :salesOfficerId \n" +
            "left join credit_debit_note cdn on  pca.id = cdn.payment_collection_adjustment_id \n" +
            "and cdn.transaction_type=\"ORD\" and cdn.approval_status = \"APPROVED\" " +
            "and cdn.is_active is true \n" +
            "and cdn.is_deleted is false and cdn.company_id= :companyId \n" +
            "left join sales_invoice sinv on pca.sales_invoice_id = sinv.id \n" +
            "# NGLSC-2261 and sinv.is_accepted is true \n" +
            "and sinv.is_active is true and sinv.is_deleted is false\n" +
            "and sinv.company_id= :companyId \n" +
            "group by sinv.id,sinv.invoice_no,sinv.invoice_amount;", nativeQuery = true)
    List<Map<String,Object>> getInvoiceWiseAvailORDList(
            @Param("companyId") Long companyId,
            @Param("salesOfficerId") Long salesOfficerId);

    @Query(value = "select sinv.id,pc.money_receipt_no as moneyReceiptNo, \n" +
            "pca.adjusted_amount as adjustedInvoiceAmount,ifnull(cdn.amount,0) as ordAmount \n" +
            "from payment_collection_adjustment pca \n" +
            "inner join payment_collection pc on pca.payment_collection_id = pc.id \n" +
            "and pc.approval_status=\"APPROVED\" and pca.company_id =:companyId and pc.company_id=:companyId\n" +
            "left join credit_debit_note cdn on  pca.id = cdn.payment_collection_adjustment_id \n" +
            "and cdn.transaction_type=\"ORD\" and cdn.approval_status = \"APPROVED\" \n" +
            "and cdn.is_active is true and cdn.is_deleted is false and cdn.company_id=:companyId \n" +
            "inner join sales_invoice sinv on pca.sales_invoice_id = sinv.id \n" +
            "and sinv.company_id=:salesInvoiceId " +
            "# NGLSC-2261 and sinv.is_accepted is true " +
            "and sinv.is_active is true " +
            "and sinv.is_deleted is false;",nativeQuery = true)
    List<Map<String,Object>> getORDDetailsOfASalesInvoice(
            @Param("companyId") Long companyId,
            @Param("salesInvoiceId") Long salesInvoiceId);

    @Query(value = "select * from overriding_discount d\n" +
            "where d.company_id = :companyId and d.semester_id = :semesterId and d.invoice_nature_id = :invoiceNature \n" +
            "  and d.is_active is true and d.is_deleted is false ", nativeQuery = true)
    List<Map> getAllByCompanyAndSemesterAndInvoiceNature(Long companyId, Long semesterId, Long invoiceNature);


    @Query(value = "select * from overriding_discount d\n" +
            "where d.id not in :ids and d.company_id = :companyId and d.semester_id = :semesterId and d.invoice_nature_id = :invoiceNature \n" +
            "  and d.is_active is true and d.is_deleted is false ", nativeQuery = true)
    List<Map> getAllByCompanyAndSemesterAndInvoiceNatureExceptIds(Long companyId, Long semesterId, Long invoiceNature, List<Long> ids);

    @Query(value = "select od.id                                                                 overriding_discount_id\n" +
            "     , concat(od.company_id, '-', od.semester_id)                            company_semester_ids\n" +
            "     , concat(od.company_id, '-', od.semester_id, '-', od.invoice_nature_id) company_semester_nature_ids\n" +
            "     , od.company_id                                                         company_id\n" +
            "     , od.semester_id                                                        semester_id\n" +
            "     , od.invoice_nature_id                                                  invoice_nature_id\n" +
            "     , od.from_day                                                           from_day\n" +
            "     , od.to_day                                                             to_day\n" +
            "     , od.ord                                                                ord\n" +
            "     , od.calculation_type                      calculation_type_code  \n" +
            "     , case\n" +
            "           when od.calculation_type = 'EQUAL' then '/='\n" +
            "           else '%' end                                                      calculation_type\n" +
            "     , i.name                                                                invoice_nature_name\n" +
            "     , ay.fiscal_year_name                                                   fiscal_year_name\n" +
            "     , s.semester_name                                                       semester_name\n" +
            "     , DATE_FORMAT(s.start_date, '%d %b %Y')                                 semester_start_date\n" +
            "     , DATE_FORMAT(s.end_date, '%d %b %Y')                                   semester_end_date\n" +
            "     , DATEDIFF(s.end_date, s.start_date) + 1                                semester_days\n" +
            "     , case\n" +
            "           when now() > s.start_date then 'EXPIRED'\n" +
            "           when now() between s.start_date and s.end_date then 'CURRENT'\n" +
            "           else 'NOT_START_YET' END                                          status\n" +
            "from overriding_discount od\n" +
            "         inner join semester s\n" +
            "                    on od.semester_id = s.id and od.company_id = :companyId and od.semester_id = :semesterId and od.invoice_nature_id =:invoiceNatureId and od.is_active is true and od.is_deleted is false\n" +
            "                        and s.is_active is true and s.is_deleted is false\n" +
            "         inner join accounting_year ay on s.accounting_year_id = ay.id\n" +
            "         inner join invoice_nature i on od.invoice_nature_id = i.id and i.is_active is true and i.is_deleted is false\n" +
            "order by s.start_date desc, i.id asc, od.from_day asc ", nativeQuery = true)
    List<Map> getOrdListWithRelatedInfoByCompanyAndSemesterAndInvoiceNature(Long companyId, Long semesterId, Long invoiceNatureId);

    @Query(value = "select od.company_id                          company_id\n" +
            "     , od.semester_id                         semester_id\n" +
            "     , ay.fiscal_year_name                    fiscal_year_name\n" +
            "     , s.semester_name                        semester_name\n" +
            "     , DATE_FORMAT(s.start_date, '%d %b %Y')  semester_start_date\n" +
            "     , DATE_FORMAT(s.end_date, '%d %b %Y')    semester_end_date\n" +
            "     , DATEDIFF(s.end_date, s.start_date) + 1 semester_days\n" +
            "     , case\n" +
            "           when now() between s.start_date and s.end_date then 'CURRENT'\n" +
            "           when now() > s.start_date then 'EXPIRED'\n" +
            "           else 'NOT_START_YET' END           status\n" +
            "from overriding_discount od\n" +
            "         inner join semester s\n" +
            "                    on od.semester_id = s.id and company_id = :companyId and od.is_active is true and od.is_deleted is false \n" +
            "                        and s.is_active is true and s.is_deleted is false \n" +
            "         inner join accounting_year ay on s.accounting_year_id = ay.id \n" +
            "group by s.id \n" +
            "order by s.start_date desc", nativeQuery = true)
    List<Map> getUniqueSemesterListOfOrdByCompany(Long companyId);


    @Query(value = "select od.invoice_nature_id invoice_nature_id\n" +
            "     , i.name               invoice_nature_name\n" +
            "     , od.semester_id\n" +
            "     , od.company_id\n" +
            "from overriding_discount od\n" +
            "         inner join invoice_nature i\n" +
            "                    on od.invoice_nature_id = i.id and od.company_id = :companyId and od.semester_id = :semesterId and\n" +
            "                       od.is_active is true and od.is_deleted is false and\n" +
            "                       i.is_active is true and i.is_deleted is false\n" +
            "group by od.company_id, od.semester_id, od.invoice_nature_id", nativeQuery = true)
    List<Map> getUniqueInvoiceNatureListOfOrdByCompanyAndSemester(Long companyId, Long semesterId);

    @Modifying
    @Query("update OverridingDiscount d set d.isDeleted = true where d.company.id =:companyId and d.semester.id = :semesterId and  d.invoiceNature.id = :invoiceNatureId ")
    void deactivateAllByCompanyAndSemesterAndInvoiceNature(Long companyId, Long semesterId, Long invoiceNatureId);

}
