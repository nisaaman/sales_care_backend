package com.newgen.ntlsnc.globalsettings.repository;


import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author mou
 * Created on 4/3/22 11:00 AM
 */

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Long> {

    List<Semester> findAllByIsDeletedFalse();

    @Query(value = "SELECT s.*\n" +
            "FROM semester s\n" +
            "inner join accounting_year ay on ay.id =  s.accounting_year_id\n" +
            "and ay.company_id = :companyId and s.start_date <= :currentDateForStart and s.end_date >= :currentDateForEnd \n" +
            "and s.is_active = true and s.is_deleted = false\n" +
            "and ay.is_active = true and ay.is_deleted = false", nativeQuery = true)
    Optional<Semester> findByDateRangeAndCompany(@Param("currentDateForStart") LocalDate currentDateForStart, @Param("currentDateForEnd") LocalDate currentDateForEnd, @Param("companyId") Long companyId);

    List<Semester> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    List<Semester> findAllByAccountingYearIdAndIsDeletedFalseAndIsActiveTrue(Long accountingYearId);

    Optional<Semester> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    @Query(value = "select ay.id                                fiscal_year_name_id\n" +
            "     , s.id                                 semester_id\n" +
            "     , ay.fiscal_year_name                  fiscal_year_name\n" +
            "     , ay.start_date                        fiscal_year_start_date\n" +
            "     , ay.end_date                          fiscal_year_end_date\n" +
            "     , DATEDIFF(ay.end_date, ay.start_date) fiscal_year_days\n" +
            "     , s.semester_name                      semester_name\n" +
            "     , s.start_date                         semester_start_date\n" +
            "     , s.end_date                           semester_end_date\n" +
            "from accounting_year ay\n" +
            "         inner join semester s on ay.id = s.accounting_year_id and ay.company_id = :companyId and ay.is_active is true and\n" +
            "                                  ay.is_deleted is false and s.is_active is true and s.is_deleted is false\n" +
            "order by ay.start_date desc, s.start_date asc", nativeQuery = true)
    List<Map> findAllAccountingYearAndSemesterByCompanyId(@Param("companyId") Long companyId);

    @Query(value = "select s.id            id\n" +
            "     , s.semester_name semester_name\n" +
            "     , DATE_FORMAT(s.start_date, '%d %b %Y')    semester_start_date\n" +
            "     , DATE_FORMAT(s.end_date, '%d %b %Y')       semester_end_date\n" +
            "from semester s\n" +
            "where s.accounting_year_id = :accountingYearId\n" +
            "  and s.is_active is true\n" +
            "  and s.is_deleted is false\n" +
            "order by s.start_date asc", nativeQuery = true)
    List<Map> getAllByAccountingYearId(Long accountingYearId);


    @Modifying
    @Query("update Semester s set s.isDeleted = true WHERE s.accountingYear.id = :accountingYearId")
    void deleteAllByAccountingYearId(@Param("accountingYearId") Long accountingYearId);

    @Query(value = "select s.id                semester_id\n" +
            "     , s.semester_name     semester_name\n" +
            "     , DATE_FORMAT(s.start_date, '%d %b %Y')       semester_start_date\n" +
            "     , s.start_date      start_date \n" +
            "     , DATE_FORMAT(s.end_date, '%d %b %Y')           semester_end_date\n" +
            "     , s.end_date           end_date \n" +
            "     , ay.id               fiscal_year_id\n" +
            "     , ay.fiscal_year_name fiscal_year_name\n" +
            "     , ay.start_date       fiscal_year_start_date\n" +
            "     , ay.end_date         fiscal_year_end_date\n" +
            "from semester s\n" +
            "         inner join accounting_year ay on s.accounting_year_id = ay.id and ay.company_id = :companyId \n" +
            "    and s.is_active is true and s.is_deleted is false and\n" +
            "                                          ay.is_active is true and ay.is_deleted is false\n" +
            "    and (now() between s.start_date and s.end_date or now() <= s.start_date)", nativeQuery = true)
    List<Map> getAllCurrentAndFutureSemesterByCompany(Long companyId);
}
