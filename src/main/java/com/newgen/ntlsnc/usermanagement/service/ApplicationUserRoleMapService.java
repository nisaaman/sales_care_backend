package com.newgen.ntlsnc.usermanagement.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.usermanagement.dto.ApplicationUserRoleMapDto;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUserCompanyMapping;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUserRoleMap;
import com.newgen.ntlsnc.usermanagement.entity.Role;
import com.newgen.ntlsnc.usermanagement.repository.ApplicationUserRoleMapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author anika
 * @Date ৫/৬/২২
 */
@Service
public class ApplicationUserRoleMapService implements IService<ApplicationUserRoleMap> {

    @Autowired
    ApplicationUserRoleMapRepository applicationUserRoleMapRepository;
    @Autowired
    RoleService roleService;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    ApplicationUserService applicationUserService;

    @Override
    @Transactional
    public ApplicationUserRoleMap create(Object object) {
        ApplicationUserRoleMapDto applicationUserRoleMapDto = (ApplicationUserRoleMapDto) object;

        if(applicationUserRoleMapDto.getUserId() == null){
            throw new RuntimeException("User is required.");
        }
        if(applicationUserRoleMapDto.getIsSave() == true){
            if(applicationUserRoleMapDto.getRoleList().size() == 0){
                throw new RuntimeException("Role is required.");
            }
        }
        Organization organization = organizationService.getOrganizationFromLoginUser();
        ApplicationUser applicationUser = applicationUserService.findById(applicationUserRoleMapDto.getUserId());

        applicationUserRoleMapRepository.deleteAllByUserAndOrganization(applicationUser.getId(), organization.getId());

        List<Role> roleList = roleService.findAllByIds(applicationUserRoleMapDto.getRoleList());

        List<ApplicationUserRoleMap> applicationUserRoleMapList = new ArrayList<>();
        roleList.forEach(r -> {
            ApplicationUserRoleMap applicationUserRoleMap = new ApplicationUserRoleMap();
            applicationUserRoleMap.setOrganization(organization);
            applicationUserRoleMap.setApplicationUser(applicationUser);
            applicationUserRoleMap.setRole(r);
            applicationUserRoleMapList.add(applicationUserRoleMap);
        });

        applicationUserRoleMapRepository.saveAll(applicationUserRoleMapList);
        return null;
    }

    @Override
    public ApplicationUserRoleMap update(Long id, Object object) {

        ApplicationUserRoleMapDto applicationUserRoleMapDto = (ApplicationUserRoleMapDto) object;
        ApplicationUserRoleMap applicationUserRoleMap = this.findById(applicationUserRoleMapDto.getId());
        applicationUserRoleMap.setOrganization(organizationService.getOrganizationFromLoginUser());
        applicationUserRoleMap.setIsActive(applicationUserRoleMapDto.getIsActive());

        return applicationUserRoleMapRepository.save(applicationUserRoleMap);

    }

    @Override
    public boolean delete(Long id) {
        try {
            Optional<ApplicationUserRoleMap> optionalApplicationUserRoleMap = applicationUserRoleMapRepository.findById(id);
            if (!optionalApplicationUserRoleMap.isPresent()) {
                throw new Exception("Application User Role Map is Not exist");
            }

            ApplicationUserRoleMap applicationUserRoleMap = optionalApplicationUserRoleMap.get();
            applicationUserRoleMap.setIsDeleted(true);
            applicationUserRoleMapRepository.save(applicationUserRoleMap);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public ApplicationUserRoleMap findById(Long id) {
        try {
            Optional<ApplicationUserRoleMap> optionalApplicationUserRoleMap = applicationUserRoleMapRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalApplicationUserRoleMap.isPresent()) {
                throw new Exception("Application User Role Map Not exist with id " + id);
            }
            return optionalApplicationUserRoleMap.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<ApplicationUserRoleMap> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return applicationUserRoleMapRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }


    public List<ApplicationUserRoleMap> findAllByApplicationUser(ApplicationUser applicationUser) {
        return applicationUserRoleMapRepository.findAllByApplicationUserAndIsDeletedFalse(applicationUser);
    }


    @Transactional(readOnly = true)
    public Map<String, List<Role>> getAllRoleByUser(Long userId) {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        ApplicationUser applicationUser = applicationUserService.findById(userId);
        List<Role> allRoles = roleService.findAllByLoginUserOrganization();

        List<Role> mappedRoles = applicationUserRoleMapRepository
                .findAllByOrganizationAndApplicationUserAndIsActiveTrueAndIsDeletedFalse(organization, applicationUser)
                .stream()
                .map(ApplicationUserRoleMap::getRole).collect(Collectors.toList());
        List<Role> unmappedRoles = allRoles.stream().filter(it -> !mappedRoles.contains(it))
                .collect(Collectors.toList());

        Map<String, List<Role>> map = new HashMap<>();
        map.put("mappedRoles", mappedRoles);
        map.put("unmappedRoles", unmappedRoles);

        return map;
    }

    public List<ApplicationUserRoleMap> findAllByRole(Long roleId) {
        return applicationUserRoleMapRepository.findAllByRoleIdAndIsDeletedFalse(roleId);
    }
}
