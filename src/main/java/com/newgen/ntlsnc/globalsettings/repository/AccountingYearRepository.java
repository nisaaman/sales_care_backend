package com.newgen.ntlsnc.globalsettings.repository;

import com.newgen.ntlsnc.globalsettings.entity.AccountingYear;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Mou
 * Created on 4/3/22 12:40 AM
 */
@Repository
@EnableJpaRepositories
public interface AccountingYearRepository extends JpaRepository<AccountingYear, Long> {
    List<AccountingYear> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<AccountingYear> findByIdAndIsDeletedFalse(Long id);

    @Query(value = "select *\n" +
            "from accounting_year\n" +
            "where (company_id is null or company_id = :companyId\n" +
            "and organization_id=:organizationId )\n" +
            "and accounting_year.is_active is true\n" +
            "and accounting_year.is_deleted is false\n" +
            "order by accounting_year.id DESC;", nativeQuery = true)
    List<AccountingYear> findOrganizationORCompany(@Param("organizationId") Long organizationId, @Param("companyId") Long companyId);


    @Lazy
    Optional<AccountingYear> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    @Query(value = "select * from accounting_year where start_date<=:toDate \n" +
            "and end_date>=:toDate and company_id=:companyId and is_active is true " +
            "and is_deleted is false;", nativeQuery = true)
    Optional<AccountingYear> findCurrentAccountingYear(
            @Param("companyId") Long companyId, @Param("toDate") LocalDate toDate);

    @Query(value = "select ay.id                                id\n" +
            "     , ay.fiscal_year_name                  fiscal_year_name\n" +
            "     , ay.start_date                        fiscal_year_start_date\n" +
            "     , ay.end_date                          fiscal_year_end_date\n" +
            "  , DATE_FORMAT(ay.start_date, '%d %b %Y') start_date_formated\n" +
            "     , DATE_FORMAT(ay.end_date, '%d %b %Y')   end_date_formated " +
            "     , DATEDIFF(ay.end_date, ay.start_date)+1 fiscal_year_days\n" +
            "     , case\n" +
            "           when current_date() > ay.end_date then 'expired'\n" +
            "           when current_date() >= ay.start_date and current_date() <= ay.end_date then 'current'\n" +
            "           when current_date() < ay.start_date then 'not_started'\n" +
            "           else 'expired' end as            status\n" +
            "from accounting_year ay\n" +
            "where ay.company_id = :companyId \n" +
            "  and ay.is_active is true\n" +
            "  and ay.is_deleted is false\n" +
            "order by ay.start_date asc", nativeQuery = true)
    List<Map<String, Object>> getAllByCompanyId(Long companyId);

    @Query(value = "select a.id\n" +
            "from accounting_year a\n" +
            "where a.is_deleted is false\n" +
            "  and a.is_active is true\n" +
            "  and a.company_id = :companyId \n" +
            "  and (:startDate between a.start_date and a.\n" +
            "    end_date or :endDate between a.start_date and a.end_date\n" +
            "    or (:startDate <= a.start_date and :endDate >= a.end_date))", nativeQuery = true)
    List<Map> getExistListByCompanyAndStartDateOrEndDate(@Param("companyId") Long companyId,
                                                         @Param("startDate") String startDate,
                                                         @Param("endDate") String endDate);

    @Query(value = "select a.id\n" +
            "from accounting_year a\n" +
            "where a.is_deleted is false\n" +
            "  and a.is_active is true\n" +
            "  and a.id != :id \n" +
            "  and a.company_id = :companyId \n" +
            "  and (:startDate between a.start_date and a.\n" +
            "    end_date or :endDate between a.start_date and a.end_date\n" +
            "    or (:startDate <= a.start_date and :endDate >= a.end_date))", nativeQuery = true)
    List<Map> getExistListByCompanyAndStartDateOrEndDateExceptId(@Param("companyId") Long companyId,
                                                                 @Param("startDate") String startDate,
                                                                 @Param("endDate") String endDate,
                                                                 @Param("id") Long id);

    @Query(value = "select a.id\n" +
            "from accounting_year a\n" +
            "where a.is_deleted is false\n" +
            "  and a.is_active is true\n" +
            "  and a.id != :id \n" +
            "  and a.company_id = :companyId \n" +
            "  and a.fiscal_year_name = :name ", nativeQuery = true)
    List<Map> getExistListByCompanyAndNameExceptId(@Param("companyId") Long companyId,
                                                                 @Param("name") String name,
                                                                 @Param("id") Long id);
    @Query(value = "select a.id\n" +
            "from accounting_year a\n" +
            "where a.is_deleted is false\n" +
            "  and a.is_active is true\n" +
            "  and a.company_id = :companyId \n" +
            "  and a.fiscal_year_name = :name ", nativeQuery = true)
    List<Map> getExistListByCompanyAndName(@Param("companyId") Long companyId,
                                                   @Param("name") String name);

    @Query(value = "select id from accounting_year \n" +
            "where id = (select max(id) from accounting_year where id < :accountingYearId) \n" +
            "and company_id=:companyId \n" +
            "and is_active is true and is_deleted is false;", nativeQuery = true)
    Map<String, Object> getPreviousAccountingYear(
            @Param("accountingYearId") Long accountingYearId,
            @Param("companyId") Long companyId);

    AccountingYear findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndCompanyIdAndIsDeletedFalse(LocalDate startDate, LocalDate endDate, Long companyId);
}
