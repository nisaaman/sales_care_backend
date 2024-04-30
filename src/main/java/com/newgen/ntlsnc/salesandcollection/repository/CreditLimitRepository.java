package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.salesandcollection.entity.CreditLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface CreditLimitRepository extends JpaRepository<CreditLimit, Long> {
    List<CreditLimit> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<CreditLimit> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    @Query(value = "select cl.id\n" +
            "     , cl.distributor_id  as                  distributor_id\n" +
            "     , d.distributor_name as                  distributor_name\n" +
            "     , d.contact_no       as                  contact_no\n" +
            "     , cl.credit_limit    as                  credit_limit\n" +
            "     , DATE_FORMAT(cl.start_date, '%d %b %Y') start_date\n" +
            "     , DATE_FORMAT(cl.end_date, '%d %b %Y')   end_date\n" +
            "     , case\n" +
            "           when cl.credit_limit_term = 'LT' then 'Long Term'\n" +
            "           when cl.credit_limit_term = 'ST' then 'Short Term'\n" +
            "           when cl.credit_limit_term = 'SB' then 'Sales Booking'\n" +
            "    end                                       credit_limit_term\n" +
            "from credit_limit cl\n" +
            "         inner join distributor d\n" +
            "                    on d.id = cl.distributor_id\n" +
            "                        and cl.company_id = :companyId \n" +
            "                        and d.is_active is true\n" +
            "                        and d.is_deleted is false\n" +
            "                        and cl.is_active is true\n" +
            "                        and cl.is_deleted is false\n" +
            "order by cl.created_date desc", nativeQuery = true)
    List<Map<String, Object>> findCreditLimitList(Long companyId);

    @Modifying
    @Query(value = " update CreditLimit cl \n" +
            " set cl.isActive = false \n" +
            " where cl.company.id = :CompanyId and cl.distributor.id = :distributorId")
    void inactiveAllByCompanyAndDistributor(Long CompanyId, Long distributorId);

    @Query("select cl\n" +
            "from CreditLimit cl\n" +
            "where cl.company.id = :companyId\n" +
            "  and cl.distributor.id = :distributorId\n" +
            "  and cl.creditLimitTerm = 'ST'\n" +
            "  and now() between cl.startDate and cl.endDate\n" +
            "  and cl.isActive is true\n" +
            "  and cl.isDeleted is false ")
    Optional<CreditLimit> findCurrentShortTermByCompanyAndDistributor(Long companyId, Long distributorId);

    @Query("select c \n" +
            "from CreditLimit c\n" +
            "where c.id = (select cl.id\n" +
            "              from CreditLimit cl\n" +
            "              where cl.company.id = :companyId\n" +
            "                and cl.distributor.id = :distributorId\n" +
            "                and cl.creditLimitTerm = 'LT'\n" +
            "                and cl.isActive is true\n" +
            "                and cl.isDeleted is false ) ")
    Optional<CreditLimit> findCurrentLongTermByCompanyAndDistributor(Long companyId, Long distributorId);

    @Query(value = "select cl.id\n" +
            "from credit_limit cl\n" +
            "where cl.is_deleted is false\n" +
            "  and cl.is_active is true\n" +
            "  and cl.company_id = :companyId \n" +
            "  and cl.distributor_id = :distributorId \n" +
            "  and (:startDate between cl.start_date and cl.\n" +
            "    end_date or :endDate between cl.start_date and cl.end_date\n" +
            "    or (:startDate <= cl.start_date and :endDate >= cl.end_date))", nativeQuery = true)
    List<Map> getExistShortTermListListByCompanyAndDistributorAndStartDateOrEndDate(Long companyId, Long distributorId,
                                                                                    String startDate, String endDate);
    @Query(value = "SELECT cl.id, cl.distributor_id,\n" +
            "dis.distributor_name,\n" +
            "cl.credit_limit,\n" +
            "lo.id locationId, au.name salesOfficer,\n" +
            "ifnull (LAG(cl.credit_limit)\n" +
            "OVER (partition by cl.distributor_id\n" +
            "order by cl.distributor_id), 0) as prev_credit_limit,\n" +
            "ifnull (credit_limit - LAG(credit_limit)\n" +
            "OVER (partition by distributor_id\n" +
            "order by distributor_id), 0) AS variance,\n" +
            "cl.credit_limit_term as term,\n" +
            "DATE_FORMAT(cl.created_date, '%d %b %Y') date\n" +
            "FROM credit_limit cl\n" +
            "inner join distributor_sales_officer_map dis_so\n" +
            "on cl.distributor_id = dis_so.distributor_id\n" +
            "#and dis_so.to_date is null\n" +
            "and dis_so.company_id = cl.company_id\n" +
            "and dis_so.is_deleted is false\n" +
            "inner join distributor dis on dis_so.distributor_id = dis.id\n" +
            "inner join application_user au on dis_so.sales_officer_id = au.id\n" +
            "inner join reporting_manager rm on dis_so.sales_officer_id = rm.application_user_id\n" +
            "and rm.to_date is null\n" +
            "inner join location_manager_map lmm\n" +
            "on rm.reporting_to_id = lmm.application_user_id\n" +
            "and lmm.to_date is null and lmm.company_id =:companyId\n" +
            "inner join location lo on lmm.location_id = lo.id\n" +
            "inner join child_location_hierarchy lo_hierarchy\n" +
            "on lo.id = lo_hierarchy.id\n" +
            "and (coalesce(:locationIds) is null or lo_hierarchy.id in (:locationIds))\n"+
            "where\n" +
            "cl.company_id = :companyId\n" +
            "and cl.is_deleted is false\n" +
            "and (:startDate is null or cl.created_date >= :startDate)\n" +
            "and (:endDate is null or cl.created_date <= :endDate)\n"+
            "and (coalesce(:salesOfficerIds) is null or dis_so.sales_officer_id in (:salesOfficerIds))\n"+
            "and (coalesce(:distributorIds) is null or cl.distributor_id in (:distributorIds))\n"
            , nativeQuery = true)
    List<Map<String, Object>> getCreditLimitHistoryList(
            Long companyId, List<Long> locationIds, List<Long> salesOfficerIds,
            List<Long> distributorIds, LocalDate startDate, LocalDate endDate);

    /*@Query(value = "select cl.id\n" +
            "     , @prev as prev_amount\n" +
            "     , (cl.credit_limit -  @prev) as variance\n" +
            "     , @prev \\:= cl.credit_limit as amount\n" +
            "     , cl.credit_limit_term as term\n" +
            "     , DATE_FORMAT(cl.created_date, '%d %b %Y') date\n" +
            "from credit_limit cl,\n" +
            "(select @prev \\:= null) as p\n" +
            "where\n" +
            "  cl.company_id = :companyId \n" +
            "  and cl.is_deleted is false\n" +
            "  and (:startDate is null or cl.created_date >= :startDate) \n" +
            "  and (:endDate is null or cl.created_date <= :endDate) \n"+
            "  and (coalesce(:distributorId) is null or cl.distributor_id in (:distributorId)) \n"
            , nativeQuery = true)
    List<Map<String, Object>> getCreditLimitHistoryList(
            Long companyId, List<Long> locationIds, List<Long> salesOfficerIds,
            List<Long> distributorIds, LocalDate startDate, LocalDate endDate);*/
}
