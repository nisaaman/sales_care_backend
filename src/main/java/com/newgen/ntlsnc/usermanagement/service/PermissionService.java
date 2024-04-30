package com.newgen.ntlsnc.usermanagement.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.usermanagement.dto.PermissionDto;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import com.newgen.ntlsnc.usermanagement.entity.Permission;
import com.newgen.ntlsnc.usermanagement.entity.Role;
import com.newgen.ntlsnc.usermanagement.repository.PermissionRepository;
import com.newgen.ntlsnc.usermanagement.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author nisa
 * @date 6/9/22
 * @time 6:08 PM
 */
@Service
@RequiredArgsConstructor
public class PermissionService implements IService<Permission> {

    private final PermissionRepository permissionRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    RoleService roleService;
    @Autowired
    ApplicationUserService applicationUserService;

    @Override
    @Transactional
    public Permission create(Object object) {

        PermissionDto permissionDto = (PermissionDto) object;
        Permission permission = new Permission();

        if (permissionDto.getRoleId() != null) {
            Optional<Role> optionalRole = roleRepository.findById(permissionDto.getRoleId());
            permission.setRole(optionalRole.get());
        }

        permission.setActivityFeature(permissionDto.getActivityFeature());
        permission.setOrganization(organizationService.getOrganizationFromLoginUser());
        permission.setIsDelete(permissionDto.getIsDelete());
        permission.setIsCreate(permissionDto.getIsCreate());
        permission.setIsUpdate(permissionDto.getIsUpdate());
        permission.setIsView(permissionDto.getIsView());
        permission.setIsAllView(permissionDto.getIsAllView());

        return permissionRepository.save(permission);

    }

    @Transactional
    public List<Permission> createAll(PermissionDto[] permissionDtoList) {
        try {
            List<Permission> permissionList = new ArrayList<>();
            Permission permission = null;

            if (permissionDtoList.length == 0) {
                throw new RuntimeException("No feature permission submitted !");
            }

            for (PermissionDto permissionDto : permissionDtoList) {
                if (permissionDto.getId() != null) {
                    permission = this.findById(permissionDto.getId());
                } else {
                    if (permissionDto.getIsCreate() == null && permissionDto.getIsUpdate() == null
                            && permissionDto.getIsDelete() == null && permissionDto.getIsView() == null) {
                        continue;
                    }

                    permission = new Permission();
                    permission.setRole(roleService.findById(permissionDto.getRoleId()));
                    permission.setActivityFeature(permissionDto.getActivityFeature());
                    permission.setOrganization(organizationService.getOrganizationFromLoginUser());
                }

                permission.setIsCreate(permissionDto.getIsCreate() != null ? permissionDto.getIsCreate() : Boolean.FALSE);
                permission.setIsUpdate(permissionDto.getIsUpdate() != null ? permissionDto.getIsUpdate() : Boolean.FALSE);
                permission.setIsDelete(permissionDto.getIsDelete() != null ? permissionDto.getIsDelete() : Boolean.FALSE);
                permission.setIsView(permissionDto.getIsView() != null ? permissionDto.getIsView() : Boolean.FALSE);

                permissionList.add(permission);
            }

            return permissionRepository.saveAll(permissionList);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    @Transactional
    public Permission update(Long id, Object object) {
        PermissionDto permissionDto = (PermissionDto) object;
        Permission permission = this.findById(permissionDto.getId());

        if (permissionDto.getRoleId() != null) {
            Optional<Role> optionalRole = roleRepository.findById(permissionDto.getRoleId());
            permission.setRole(optionalRole.get());
        }

        permission.setActivityFeature(permissionDto.getActivityFeature());
        permission.setOrganization(organizationService.getOrganizationFromLoginUser());
        permission.setIsDelete(permissionDto.getIsDelete());
        permission.setIsCreate(permissionDto.getIsCreate());
        permission.setIsUpdate(permissionDto.getIsUpdate());
        permission.setIsView(permissionDto.getIsView());
        permission.setIsAllView(permissionDto.getIsAllView());


        return permissionRepository.save(permission);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            Permission permission = findById(id);
            permission.setIsDeleted(true);
            permissionRepository.save(permission);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Permission findById(Long id) {
        try {
            Optional<Permission> optionalPermission = permissionRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalPermission.isPresent()) {
                throw new Exception("Permission Not exist with id " + id);
            }
            return optionalPermission.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public List<Permission> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return permissionRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    public List<Map<String, Object>> getAllPermissionListByRole(Long roleId) {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return permissionRepository.getAllPermissionListByRole(organization.getId(), roleId);
    }

    @Override
    @Transactional
    public boolean validate(Object object) {
        return true;
    }

    public Collection<? extends Permission> findAllByRole(Role role) {
        return permissionRepository.findAllByRoleAndIsDeletedFalse(role);
    }

    public Boolean hasAccess(String activityFeature) {

        Set<Permission> permissions = applicationUserService.getPermissionsFromLoginUser();
        Permission permission = permissions.stream()
                .filter(p -> activityFeature.equals(p.getActivityFeature().getCode()))
                .findAny()
                .orElse(null);

        if (permission.getIsCreate() && permission.getIsDelete()
                && permission.getIsUpdate() && permission.getIsView()) {
            return true;
        } else {
            return false;
        }
    }
}
