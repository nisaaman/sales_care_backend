package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.dto.DepartmentDto;
import com.newgen.ntlsnc.globalsettings.entity.*;
import com.newgen.ntlsnc.globalsettings.repository.DepartmentRepository;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author liton
 * Created on 4/3/22 11:57 AM
 */

@Service
public class DepartmentService implements IService<Department> {

    @Autowired
    DepartmentRepository departmentRepository;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    ApplicationUserService applicationUserService;

    @Override
    @Transactional
    public Department create(Object object) {

        DepartmentDto departmentDto = (DepartmentDto) object;
        Department department = new Department();

        department.setName(departmentDto.getName());
        department.setDescription(departmentDto.getDescription());
        department.setIsActive(departmentDto.getIsActive());
        department.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (!this.validate(department)) {
            return null;
        }
        return departmentRepository.save(department);
    }

    @Override
    @Transactional
    public Department update(Long id, Object object) {
        DepartmentDto departmentDto = (DepartmentDto) object;
        Department department = this.findById(id);

        department.setName(departmentDto.getName());
        department.setDescription(departmentDto.getDescription());
        department.setIsActive(departmentDto.getIsActive());
        department.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (!this.validate(department)) {
            return null;
        }
        return departmentRepository.save(department);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {

        try {

            Optional<Department> department = departmentRepository.findById(id);
            if (!department.isPresent()) {
                throw new Exception("Department not exist.");
            }

            List<ApplicationUser> applicationUserList = applicationUserService.findAllApplicationUserByDepartment(department.get().getId());
            if(applicationUserList.size() > 0){
                throw new RuntimeException("This department already in use.");
            }

            department.get().setIsDeleted(true);
            departmentRepository.save(department.get());
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Department findById(Long id) {
        try {
            Optional<Department> optionalDepartment = departmentRepository.findByIdAndIsDeletedFalse(id);
            if (!optionalDepartment.isPresent()) {
                throw new Exception("Department Not exist with id " + id);
            }
            return optionalDepartment.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<Department> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return departmentRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    public List<Department> findAllActiveDepartment() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return departmentRepository.findAllByOrganizationAndIsDeletedFalseAndIsActiveTrue(organization);
    }
    @Override
    public boolean validate(Object object) {
        Department department = (Department) object;
        Optional<Department> optionalDepartment = Optional.empty();

        if(department.getId() == null) {
            optionalDepartment = departmentRepository.findByOrganizationAndNameIgnoreCaseAndIsDeletedFalse(
                    department.getOrganization(), department.getName().trim());
        } else {
            optionalDepartment = departmentRepository.findByOrganizationAndIdIsNotAndNameIgnoreCaseAndIsDeletedFalse(
                    department.getOrganization(), department.getId(), department.getName().trim());
        }

        if(department.getIsActive()==false){

            List<ApplicationUser> applicationUserList = applicationUserService.findAllActiveApplicationUserByDepartment(department.getId()) ;
            if (applicationUserList.size()>0) {
                throw new RuntimeException("This Department is already in use");
            }
        }

        if(optionalDepartment.isPresent()){
            throw new RuntimeException("Department name already exist.");
        }

        return true;
    }
}
