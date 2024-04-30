package com.newgen.ntlsnc.usermanagement.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.salesandcollection.entity.Distributor;
import com.newgen.ntlsnc.usermanagement.dto.ApplicationUserCompanyMappingDto;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUserCompanyMapping;
import com.newgen.ntlsnc.usermanagement.repository.ApplicationUserCompanyMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author anika
 * @Date ২০/৬/২২
 */
@Service
public class ApplicationUserCompanyMappingService implements IService<ApplicationUserCompanyMapping> {
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    ApplicationUserCompanyMappingRepository applicationUserCompanyMappingRepository;

    @Override
    @Transactional
    public ApplicationUserCompanyMapping create(Object object) {
        ApplicationUserCompanyMappingDto applicationUserCompanyMappingDto = (ApplicationUserCompanyMappingDto) object;

        if(applicationUserCompanyMappingDto.getUserId() == null){
            throw new RuntimeException("User is required.");
        }

        if(applicationUserCompanyMappingDto.getCompanyList().size() == 0){
            throw new RuntimeException("Company is required.");
        }

        ApplicationUserCompanyMapping applicationUserCompanyMapping = null;
        ApplicationUser applicationUser = applicationUserService.findById(applicationUserCompanyMappingDto.getUserId());
        Organization organization = organizationService.getOrganizationFromLoginUser();

        List<ApplicationUserCompanyMapping> applicationUserCompanyMappingList = applicationUserCompanyMappingRepository
            .findAllByOrganizationAndApplicationUserAndIsActiveTrueAndIsDeletedFalse(organization, applicationUser)
            .stream().filter(it-> it.getCompany().getParent() != null).collect(Collectors.toList());

        List<Organization> mappedCompanies = applicationUserCompanyMappingList.stream()
            .filter(it-> it.getCompany().getParent() != null)
            .map(ApplicationUserCompanyMapping::getCompany).collect(Collectors.toList());

        List<ApplicationUserCompanyMapping> applicationUserCompanyMappingUnchangedList = new ArrayList<>();
        List<ApplicationUserCompanyMapping> applicationUserCompanyMappingDeletedList = new ArrayList<>();

        //Find and create newly added companies & make unchanged list.
        if (applicationUserCompanyMappingDto.getCompanyList().size() > 0){
            for(Organization company : applicationUserCompanyMappingDto.getCompanyList()) {
                if(mappedCompanies.contains(company)){
                    applicationUserCompanyMapping = applicationUserCompanyMappingList
                        .stream().filter(it-> it.getCompany().equals(company)).findFirst().orElse(null);
                    applicationUserCompanyMappingUnchangedList.add(applicationUserCompanyMapping);
                } else {
                    applicationUserCompanyMapping = new ApplicationUserCompanyMapping();
                    applicationUserCompanyMapping.setOrganization(organization);
                    applicationUserCompanyMapping.setApplicationUser(applicationUser);
                    applicationUserCompanyMapping.setCompany(company);
                    applicationUserCompanyMappingRepository.save(applicationUserCompanyMapping);
                }
            }
        }

        //Make deleted list from unchanged list.
        applicationUserCompanyMappingDeletedList = applicationUserCompanyMappingList.stream()
            .filter(it-> !applicationUserCompanyMappingUnchangedList.contains(it)).collect(Collectors.toList());

        //Deleting user company map.
        if(applicationUserCompanyMappingDeletedList.size() > 0){
            for(ApplicationUserCompanyMapping userCompanyMapping : applicationUserCompanyMappingDeletedList){
                userCompanyMapping.setIsDeleted(true);
                applicationUserCompanyMappingRepository.save(userCompanyMapping);
            }
        }

        return  applicationUserCompanyMapping;
    }

    @Override
    @Transactional
    public ApplicationUserCompanyMapping update(Long id, Object object) {
        ApplicationUserCompanyMappingDto applicationUserCompanyMappingDto = (ApplicationUserCompanyMappingDto) object;
        ApplicationUserCompanyMapping applicationUserCompanyMapping = this.findById(applicationUserCompanyMappingDto.getId());

        if(applicationUserCompanyMappingDto.getUserId()!= null){
            applicationUserCompanyMapping.setApplicationUser(applicationUserService.findById(applicationUserCompanyMappingDto.getUserId()));
        }
        if (applicationUserCompanyMappingDto.getCompanyList()!=null){
//            applicationUserCompanyMapping.setCompany(organizationService.findById(applicationUserCompanyMappingDto.getCompanyList()));
        }
        return  applicationUserCompanyMappingRepository.save(applicationUserCompanyMapping);

    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            Optional<ApplicationUserCompanyMapping> optionalApplicationUserCompanyMapping = applicationUserCompanyMappingRepository.findById(id);
            if (!optionalApplicationUserCompanyMapping.isPresent()) {
                throw new Exception("Application User Company Mapping Not exist");
            }
            ApplicationUserCompanyMapping applicationUserCompanyMapping = optionalApplicationUserCompanyMapping.get();
            applicationUserCompanyMapping.setIsDeleted(true);
            applicationUserCompanyMappingRepository.save(applicationUserCompanyMapping);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    @Transactional
    public ApplicationUserCompanyMapping findById(Long id) {

        try {
            Optional<ApplicationUserCompanyMapping> optionalApplicationUserCompanyMapping = applicationUserCompanyMappingRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalApplicationUserCompanyMapping.isPresent()) {
                throw new Exception("Application User Company Mapping exist with id " + id);
            }
            return optionalApplicationUserCompanyMapping.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public List<ApplicationUserCompanyMapping> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return applicationUserCompanyMappingRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Transactional(readOnly = true)
    public Map<String, List<Organization>> getAllCompanyByUser(Long userId) {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        ApplicationUser applicationUser = applicationUserService.findById(userId);
        List<Organization> allCompanies = organizationService.findAllCompanyByLoginUserOrganization();
        List<Organization> mappedCompanies = applicationUserCompanyMappingRepository
                .findAllByOrganizationAndApplicationUserAndIsActiveTrueAndIsDeletedFalse(organization, applicationUser)
                .stream().filter(it-> it.getCompany().getParent() != null)
                .map(ApplicationUserCompanyMapping::getCompany).collect(Collectors.toList());
        List<Organization> unmappedCompanies = allCompanies.stream().filter(it-> !mappedCompanies.contains(it))
                .collect(Collectors.toList());

        Map<String, List<Organization>> map = new HashMap<>();
        map.put("mappedCompanies", mappedCompanies);
        map.put("unmappedCompanies", unmappedCompanies);

        return map;
    }

    @Override
    @Transactional
    public boolean validate(Object object) {
        return true;
    }

    public List<ApplicationUserCompanyMapping> findAllByUser() {
        ApplicationUser applicationUser = applicationUserService.getApplicationUserFromLoginUser();
        return applicationUserCompanyMappingRepository.findAllByOrganizationAndApplicationUserAndIsDeletedFalse(
                applicationUser.getOrganization(),applicationUser);
    }

    public List<Map<String,Object>> getAllCompanyListByUser(Organization organization, ApplicationUser applicationUser){
        List<ApplicationUserCompanyMapping> applicationUserCompanyMappingList = applicationUserCompanyMappingRepository.findAllByOrganizationAndApplicationUserAndIsActiveTrueAndIsDeletedFalse(organization,applicationUser);
        List<Map<String,Object>> companyList = new ArrayList<Map<String,Object>>();
        applicationUserCompanyMappingList.forEach(applicationUserCompanyMapping -> {
                    Map<String,Object> company = new HashMap<String,Object>();
                    company.put("companyId", applicationUserCompanyMapping.getCompany().getId());
                    company.put("companyName", applicationUserCompanyMapping.getCompany().getName());
                    company.put("companyShortName", applicationUserCompanyMapping.getCompany().getShortName());
                    companyList.add(company);
                }
        );
        return companyList;

    }

    public List<ApplicationUserCompanyMapping> findAllApplicationUserCompanyMappingByApplicationUser(Organization organization,ApplicationUser applicationUser) {
        return applicationUserCompanyMappingRepository.findAllByOrganizationAndApplicationUserAndIsActiveTrueAndIsDeletedFalse(organization,applicationUser);
    }

    public  List<Map<String, Object>> getUserListByCompanyWise(List<Long> companyIds){
        return applicationUserCompanyMappingRepository.findAllUserByCompanyWise(companyIds);
    }

    public List<ApplicationUserCompanyMapping> findAllApplicationUserCompanyMappingByCompany(Organization organization,List<Organization> companyList) {
        return applicationUserCompanyMappingRepository.findAllByOrganizationAndCompanyInAndIsActiveTrueAndIsDeletedFalse(organization,companyList);
    }

    public boolean validate(Organization company, ApplicationUser user) {

        Optional<ApplicationUserCompanyMapping> optionalApplicationUserCompanyMapping =
                applicationUserCompanyMappingRepository.findByCompanyAndApplicationUserAndIsActiveTrueAndIsDeletedFalse(company, user);

        if (optionalApplicationUserCompanyMapping.isPresent()) {
            return false;
        }
        return true;
    }


}
