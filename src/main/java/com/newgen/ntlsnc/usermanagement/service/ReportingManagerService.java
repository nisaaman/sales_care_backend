package com.newgen.ntlsnc.usermanagement.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.entity.Location;
import com.newgen.ntlsnc.globalsettings.entity.LocationManagerMap;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.service.LocationManagerMapService;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.salesandcollection.entity.DistributorSalesOfficerMap;
import com.newgen.ntlsnc.salesandcollection.entity.SalesBooking;
import com.newgen.ntlsnc.salesandcollection.repository.DistributorSalesOfficerMapRepository;
import com.newgen.ntlsnc.salesandcollection.repository.SalesBookingRepository;
import com.newgen.ntlsnc.usermanagement.dto.ReportingManagerDto;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import com.newgen.ntlsnc.usermanagement.entity.ReportingManager;
import com.newgen.ntlsnc.usermanagement.repository.ReportingManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author sunipa
 * @Date 20/06/22
 */

@Service
public class ReportingManagerService implements IService<ReportingManager> {
    @Autowired
    ReportingManagerRepository reportingManagerRepository;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    LocationManagerMapService locationManagerMapService;
    @Autowired
    SalesBookingRepository salesBookingRepository;
    @Autowired
    DistributorSalesOfficerMapRepository distributorSalesOfficerMapRepository;

    public Long getReportingTo(Long companyId, Long userLoginId) {
        try {
            Long managerLocation =
                    reportingManagerRepository.getReportingTo(userLoginId);
            return managerLocation;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Long> getManagerHierarchyDown(Long companyId, Long reportingToId) {
        try {
            List<Long> managerHierarchyDownList =
                    reportingManagerRepository.getManagerHierarchyDown(reportingToId);
            return managerHierarchyDownList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Long checkLoginUserIsManager(Long companyId, Long userLoginId) {
        try {
            Long managerLocation =
                    reportingManagerRepository.checkLoginUserIsManager(companyId, userLoginId);
            return managerLocation;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    @Override
    @Transactional
    public ReportingManager create(Object object) {
        ReportingManagerDto reportingManagerDto = (ReportingManagerDto) object;
        ReportingManager reportingManager = new ReportingManager();

        reportingManager.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (reportingManagerDto.getReportingToId() != null) {
            ApplicationUser reportingTo = applicationUserService.findById(reportingManagerDto.getReportingToId());
            reportingManager.setReportingTo(reportingTo);
        }
        if (reportingManagerDto.getApplicationUserId() != null) {
            ApplicationUser applicationUser = applicationUserService.findById(reportingManagerDto.getApplicationUserId());
            reportingManager.setApplicationUser(applicationUser);
        }

        reportingManager.setFromDate(LocalDate.now());

        if (!this.validate(reportingManager)) {
            return null;
        }
        return reportingManagerRepository.save(reportingManager);
    }

    @Override
    @Transactional
    public ReportingManager update(Long id, Object object) {
        ReportingManagerDto reportingManagerDto = (ReportingManagerDto) object;
        ReportingManager reportingManager = this.findById(reportingManagerDto.getId());
        ReportingManager reportingManagerNew = new ReportingManager();

        if (reportingManagerDto.getReportingToId().equals(reportingManager.getReportingTo().getId()) && reportingManagerDto.getApplicationUserId().equals(reportingManager.getApplicationUser().getId())) {
            throw new RuntimeException("This Reporting To is already assign for the user.");
        } else {
            LocalDate date = LocalDate.now();
            LocalDate yesterday = date.minusDays(1);
            reportingManager.setToDate(yesterday);
            reportingManagerRepository.save(reportingManager);

            reportingManagerNew.setOrganization(organizationService.getOrganizationFromLoginUser());

            if (reportingManagerDto.getReportingToId() != null) {
                ApplicationUser reportingTo = applicationUserService.findById(reportingManagerDto.getReportingToId());
                reportingManagerNew.setReportingTo(reportingTo);
            }
            if (reportingManagerDto.getApplicationUserId() != null) {
                ApplicationUser applicationUser = applicationUserService.findById(reportingManagerDto.getApplicationUserId());
                reportingManagerNew.setApplicationUser(applicationUser);
            }

            reportingManagerNew.setFromDate(LocalDate.now());
        }

        return reportingManagerRepository.save(reportingManagerNew);

    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            ReportingManager reportingManager = findById(id);
            reportingManager.setIsDeleted(true);
            reportingManagerRepository.save(reportingManager);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    @Transactional
    public ReportingManager findById(Long id) {
        try {
            Optional<ReportingManager> optionalReportingManager = reportingManagerRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalReportingManager.isPresent()) {
                throw new Exception("Reporting Manager Not exist with id " + id);
            }
            return optionalReportingManager.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public List<ReportingManager> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return reportingManagerRepository.findAllByOrganizationAndToDateIsNullAndIsDeletedFalse(organization);

    }


    public List<ReportingManager> findAllByReportingToList(List<ApplicationUser> reportingToList) {
        return reportingManagerRepository.findAllByReportingToInAndIsActiveTrueAndIsDeletedFalse(reportingToList);
    }

    public List<ApplicationUser> findAllApplicationUserByReportingManagerList(List<ReportingManager> reportingManagerList) {
        List<ApplicationUser> applicationUserList = reportingManagerList.stream().map(rm -> rm.getApplicationUser()).collect(Collectors.toList());
        return applicationUserList;
    }

    public List<ApplicationUser> findAllSalesOfficerByTerritoryLocationList(List<Location> locationList) {
        List<LocationManagerMap> locationManagerMapList = locationManagerMapService.findAllByLocationList(locationList);
        List<ApplicationUser> managerList = locationManagerMapList.stream().map(lm -> lm.getApplicationUser()).collect(Collectors.toList());
        List<ReportingManager> reportingManagerList = findAllByReportingToList(managerList);
        List<ApplicationUser> salesOfficer = findAllApplicationUserByReportingManagerList(reportingManagerList);
        return salesOfficer;
    }

    public List<ApplicationUser> findAllApplicationUserByReportingToList(List<ApplicationUser> reportingToList) {
        List<ReportingManager> reportingManagerList = findAllByReportingToList(reportingToList);
        return findAllApplicationUserByReportingManagerList(reportingManagerList);
    }

    @Override
    @Transactional
    public boolean validate(Object object) {
        try{
            ReportingManager reportingManager = (ReportingManager) object;
            Optional<ReportingManager> optionalReportingManager = Optional.empty();
            Optional<ReportingManager> reportingManager1 =Optional.empty();
            if (reportingManager.getId() == null) {
                optionalReportingManager = reportingManagerRepository.findByOrganizationAndApplicationUserIdAndIsDeletedFalseAndToDateIsNull(
                        reportingManager.getOrganization(), reportingManager.getApplicationUser().getId());
                if (optionalReportingManager.isPresent()) {
                    throw new RuntimeException("Reporting Manager already exist for this User.");
                }

                if(reportingManager.getReportingTo() != null){
                    reportingManager1 = reportingManagerRepository.findByOrganizationAndApplicationUserIdAndIsDeletedFalseAndToDateIsNull(
                            reportingManager.getOrganization(), reportingManager.getReportingTo().getId());
                    if (reportingManager1.isPresent()) {
                        if(reportingManager1.get().getReportingTo() != null){
                            if (reportingManager1.get().getReportingTo().getId() == reportingManager.getApplicationUser().getId()) {
                                throw new RuntimeException(reportingManager.getApplicationUser().getName() + " is reporting manager of " + reportingManager.getReportingTo().getName());
                            }
                        }


                    }
                }

                List<ReportingManager> reportingManagerList = reportingManagerRepository.findByOrganizationAndReportingToIdAndToDateIsNullAndIsDeletedFalse(
                        reportingManager.getOrganization(), reportingManager.getApplicationUser().getId());
                if (reportingManagerList.size() > 0) {
                    for (int i = 0; i < reportingManagerList.size(); i++) {
                        ReportingManager reportingManager2 = reportingManagerList.get(i);
                        if(reportingManager.getReportingTo() != null){
                            if (reportingManager2.getApplicationUser().getId() == reportingManager.getReportingTo().getId()) {
                                throw new RuntimeException(reportingManager.getApplicationUser().getName() + " is reporting manager of " + reportingManager.getReportingTo().getName());
                            }
                        }
                    }
                    for (int i = 0; i < reportingManagerList.size(); i++) {
                        ReportingManager reportingManager2 = reportingManagerList.get(i);
                        if (reportingManager1.isPresent()) {
                            if(reportingManager1.get().getReportingTo() != null){
                                if (reportingManager1.get().getReportingTo().getId() == reportingManager2.getApplicationUser().getId()) {
                                    throw new RuntimeException("Invalid reporting manager due to circulate mapping.");
                                }
                            }
                        }
                    }

                }




            } else {
//            optionalReportingManager = reportingManagerRepository.findByOrganizationAndApplicationUserIdAndReportingToIdAndIsDeletedFalse(
//                    reportingManager.getOrganization(), reportingManager.getApplicationUser().getId(), reportingManager.getReportingTo().getId());
            }
            if (optionalReportingManager.isPresent()) {
                throw new RuntimeException("Reporting Manager already exist for this User.");
            }
            return true;
        }catch (RuntimeException e){
            throw new RuntimeException(e.getMessage());
        }

    }

    public List<Long> getAllSalesOfficeFromCompany(Long companyId) {

        return reportingManagerRepository.getAllSalesOfficerFromCompany(companyId);
    }

    @Transactional
    public List<ReportingManager> getAllByCompany(Long companyId) {
        //Organization organization = organizationService.getOrganizationFromLoginUser();
        return reportingManagerRepository.getAllByCompany(companyId);

    }

    public boolean deleteReportingManager(Long companyId, Long salesOfficerId) {
        try {
            ReportingManager reportingManager = findById(salesOfficerId);
            List<SalesBooking> salesBookingList = salesBookingRepository.findByCompanyIdAndSalesOfficerId(
                    companyId, reportingManager.getApplicationUser().getId());
            List<DistributorSalesOfficerMap> distributorSalesOfficerMapList =
                    distributorSalesOfficerMapRepository.findByCompanyIdAndSalesOfficerIdAndIsActiveTrueAndIsDeletedFalse(
                            companyId, reportingManager.getApplicationUser().getId());
            if (salesBookingList.size() != 0 && distributorSalesOfficerMapList.size() != 0){
                throw new Exception("Reporting Manager cannot be deleted");
            } else {
                reportingManager.setIsDeleted(true);
                reportingManagerRepository.save(reportingManager);
                return true;
            }

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }
    @Transactional
    public List<ReportingManager> getSalesTeam(Long salesOfficerUserId) {
       return reportingManagerRepository.getSalesTeam(salesOfficerUserId);
    }
}
