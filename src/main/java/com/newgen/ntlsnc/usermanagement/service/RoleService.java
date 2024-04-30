package com.newgen.ntlsnc.usermanagement.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.entity.Bank;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.usermanagement.dto.RoleDto;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUserRoleMap;
import com.newgen.ntlsnc.usermanagement.entity.Role;
import com.newgen.ntlsnc.usermanagement.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.newgen.ntlsnc.common.CommonConstant.ROLE_PREFIX;
import static org.hibernate.type.SerializableToBlobType.CLASS_NAME;

/**
 * @author anika
 * @Date ৫/৬/২২
 */
@Service
public class RoleService implements IService<Role> {

    @Autowired
    RoleRepository roleRepository;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    ApplicationUserRoleMapService applicationUserRoleMapService;
    public static final String DUPLICATE_NAME = "Role Name is Duplicate";
    @Transactional
    @Override
    public Role create(Object object) {
      try{
          RoleDto roleDto = (RoleDto) object;
          Role role = new Role();
          Organization organization =organizationService.getOrganizationFromLoginUser();
          Optional<Role> name = roleRepository.findByOrganizationAndNameIgnoreCaseAndIsDeletedFalse(organization,ROLE_PREFIX + roleDto.getName().toUpperCase());
          if (name.isPresent()) {//check only name in create
              throw new Exception(DUPLICATE_NAME);
          }

          role.setName(ROLE_PREFIX + roleDto.getName().replaceAll(" ", "_").toUpperCase());

          role.setDescription(roleDto.getDescription());
          role.setIsActive(roleDto.getIsActive());
          role.setOrganization(organization);
//          if (!this.validate(role)) {
//              return null;
//          }
          return roleRepository.save(role);
      }
     catch (Exception e) {
        throw new RuntimeException(e.getMessage());
    }

    }

    @Transactional
    @Override
    public Role update(Long id, Object object) {
        try{
            RoleDto roleDto = (RoleDto) object;
            Role role = this.findById(roleDto.getId());

                Organization organization =organizationService.getOrganizationFromLoginUser();
                Optional<Role> roleNameOpt = roleRepository.findByOrganizationAndNameIgnoreCaseAndIsDeletedFalse(organization,ROLE_PREFIX + roleDto.getName().replaceAll(" ", "_").toUpperCase());

                if (roleNameOpt.isPresent() && roleDto.getId() != roleNameOpt.get().getId()) {//check backend id and frontend id
                    throw new Exception(DUPLICATE_NAME);
                }

                String s= roleDto.getName().replaceAll(" ", "_").toUpperCase();
                String[] s1= s.split("_");
                String s2= String.valueOf(s1[0]);
                if(s2.equals("ROLE")){
                    role.setName(s);
                }else{
                    role.setName(ROLE_PREFIX + roleDto.getName().replaceAll(" ", "_").toUpperCase());
                }

                role.setDescription(roleDto.getDescription());
                role.setIsActive(roleDto.getIsActive());
               // roleRepository.save(role);
//                if (!this.validate(role)) {
//                    return null;
//                }
            return roleRepository.save(role);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }

    }

    @Transactional
    @Override
    public boolean delete(Long id) {

        try {
            Optional<Role> optionalRole = roleRepository.findById(id);
            if (!optionalRole.isPresent()) {
                throw new Exception("Role is Not exist");
            }
            List<ApplicationUserRoleMap> applicationUserRoleMaps = applicationUserRoleMapService.findAllByRole(optionalRole.get().getId());
            if(applicationUserRoleMaps.size() > 0){
                throw new RuntimeException("This Role already in used.");
            }

            Role role = optionalRole.get();
            role.setIsDeleted(true);
            roleRepository.save(role);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    @Override
    public Role findById(Long id) {
        try {
            Optional<Role> optionalRole = roleRepository.findByIdAndIsDeletedFalse(id);
            if (!optionalRole.isPresent()) {
                throw new Exception("Role Not exist with id " + id);
            }
            return optionalRole.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Role> findAllByIds(List<Long> ids) {
        try {
            List<Role> roleList = roleRepository.findAllByIdInAndIsActiveTrueAndIsDeletedFalse(ids);
            return roleList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    @Override
    public List<Role> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return roleRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    public List<Role> findAllActiveRole() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return roleRepository.findAllByOrganizationAndIsActiveTrueAndIsDeletedFalse(organization);
    }

    @Transactional
    @Override
    public boolean validate(Object object) {
        Role role=(Role) object;
        Optional<Role> optionalRole = Optional.empty();
        if(role.getId()== null){
            optionalRole= roleRepository.findByOrganizationAndNameIgnoreCaseAndIsDeletedFalse(
                    role.getOrganization(),role.getName());
        }
        else{
            optionalRole=roleRepository.findByOrganizationAndIdIsNotAndNameIgnoreCaseAndIsDeletedFalse(
                    role.getOrganization(),role.getId(),role.getName());
        }
        if(optionalRole.isPresent()){
            throw new RuntimeException("Role name is already exist");
        }
        return true;
    }

    public List<Role> findAllByLoginUserOrganization() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return roleRepository.findAllByOrganizationAndIsActiveTrueAndIsDeletedFalse(organization);
    }

    public Role findByRoleName(String roleName) {
        return roleRepository.findByNameAndIsDeletedFalse(roleName).orElse(null);
    }
}
