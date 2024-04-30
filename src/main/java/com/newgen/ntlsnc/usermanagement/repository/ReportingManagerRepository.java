package com.newgen.ntlsnc.usermanagement.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import com.newgen.ntlsnc.usermanagement.entity.ReportingManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * @author Newaz Sharif
 * @since 17th April, 2022
 */
public interface ReportingManagerRepository extends JpaRepository<ReportingManager, Long> {

    @Procedure("SNC_SALES_TEAM")
    List<ReportingManager> getSalesTeam(long salesOfficerUserId);

    @Procedure("SNC_SALES_RESPONSIBLE_TEAM")
    List<ReportingManager> getSalesResponsibleTeam(String bookingNo);

    @Procedure("SNC_SALES_OFFICER_HIERARCHY")
    List<ReportingManager> getSalesTeamHierarchy(long salesOfficerUserId);

    @Procedure("SNC_SO_WISE_SALES_TARGET")
    List<ReportingManager> getSalesOfficerWiseSalesTargetByMonthAndYear(
            long salesOfficerUserId, int month, String year);

    @Procedure("SNC_DISTRIBUTOR_WISE_SO_SALES_TARGET")
    List<ReportingManager> getDistributorWiseSalesOfficerSalesTargetByMonthAndYear(
            long salesOfficerUserId, int month, String year);

    @Procedure("SNC_LOCATION_WISE_SO_SALES_TARGET")
    List<ReportingManager> getLocationWiseSalesOfficerSalesTargetByMonthAndYear(
            long salesOfficerUserId, int month, String year);

    @Query(value = "select rm.reporting_to_id " +
            "from reporting_manager rm " +
            "where rm.application_user_id= :user_login_id \n" +
            "and rm.to_date is null \n" +
            "and rm.is_active is true and rm.is_deleted is false \n"
            , nativeQuery = true)
    Long getReportingTo(@Param("user_login_id") Long userLoginId);

    @Query(value = "select rm.application_user_id " +
            "from reporting_manager rm " +
            "where rm.reporting_to_id= :reporting_to_id \n" +
            "and rm.is_active is true and rm.is_deleted is false \n" +
            "and rm.to_date is null", nativeQuery = true)
    List<Long> getManagerHierarchyDown(@Param("reporting_to_id") Long userLoginId);

    @Query(value = "select lmm.application_user_id " +
            "from location_manager_map lmm " +
            "where lmm.application_user_id= :user_login_id \n" +
            "and lmm.to_date is null and lmm.company_id =:company_id\n" +
            "and lmm.is_active is true and lmm.is_deleted is false \n" +
            "and lmm.company_id = :company_id", nativeQuery = true)
    Long checkLoginUserIsManager(@Param("company_id") Long companyId,
                                 @Param("user_login_id") Long userLoginId);

    List<ReportingManager> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    List<ReportingManager> findAllByReportingToInAndIsActiveTrueAndIsDeletedFalse(List<ApplicationUser> reportingToList);

    Optional<ReportingManager> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    @Query(value = "select lmm.application_user_id as salesOfficer from\n" +
            "(select user_id from application_user_company_mapping where company_id= :companyId) as aucm \n" +
            "                             inner join\n" +
            "(select rm.application_user_id from reporting_manager rm\n" +
            "left join reporting_manager rmc on rm.application_user_id = rmc.reporting_to_id\n" +
            "where rmc.application_user_id is null and rm.to_date is null and rm.is_active is true and rm.is_deleted is false) as lmm\n" +
            "on aucm.user_id = lmm.application_user_id;", nativeQuery = true)
    List<Long> getAllSalesOfficerFromCompany(Long companyId);

    List<ReportingManager> findAllByOrganizationAndToDateIsNullAndIsDeletedFalse(Organization organization);

    @Query(value = "select * from reporting_manager rm \n" +
            "              inner join application_user_company_mapping aucm \n" +
            "\t\t\t\ton rm.application_user_id=aucm.user_id \n" +
            "\t\t\t\tand rm.reporting_to_id is not null \n" +
            "\t\t\t\tand rm.is_active is true \n" +
            "\t\t\t\tand rm.is_deleted is false \n" +
            "                and rm.to_date is null \n" +
            "\t\t\t\tand aucm.is_active is true \n" +
            "\t\t\t\tand aucm.is_deleted is false \n" +
            "              where aucm.company_id=:companyId", nativeQuery = true)
    List<ReportingManager> getAllByCompany(Long companyId);

    Optional<ReportingManager> findByOrganizationAndApplicationUserIdAndIsDeletedFalseAndToDateIsNull(Organization organization, Long id);

    List<ReportingManager> findByOrganizationAndReportingToIdAndToDateIsNullAndIsDeletedFalse(Organization organization, Long id);
}
