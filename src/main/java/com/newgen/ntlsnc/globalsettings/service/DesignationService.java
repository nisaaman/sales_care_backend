package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.dto.DesignationDto;
import com.newgen.ntlsnc.globalsettings.entity.Designation;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.repository.DesignationRepository;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
public class DesignationService implements IService<Designation> {
    @Autowired
    DesignationRepository designationRepository;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    ApplicationUserService applicationUserService;

    @Override
    @Transactional
    public Designation create(Object object) {
        DesignationDto designationDto = (DesignationDto) object;
        Designation designation = new Designation();
        designation.setName(designationDto.getName());
        designation.setDescription(designationDto.getDescription());
        designation.setIsActive(designationDto.getIsActive());
        designation.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (!this.validate(designation)) {
            return null;
        }
        return designationRepository.save(designation);
    }

    @Override
    @Transactional
    public Designation update(Long id, Object object) {
        DesignationDto designationDto = (DesignationDto) object;
        Designation designation = new Designation();

        Optional<Designation> optionalDesignation = designationRepository.findById(designationDto.getId());
        if (optionalDesignation.get() != null) {
            designation = optionalDesignation.get();
        }

        designation.setName(designationDto.getName());
        designation.setDescription(designationDto.getDescription());
        designation.setIsActive(designationDto.getIsActive());
        designation.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (!this.validate(designation)) {
            return null;
        }
        return designationRepository.save(designation);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        Designation designation = new Designation();
        Optional<Designation> optionalDesignation = designationRepository.findById(id);
        if (optionalDesignation.get() != null) {
            designation = optionalDesignation.get();
        }

//        Optional<UnitOfMeasurement> optionalUnitOfMeasurement = unitOfMeasurementRepository.findById(id);
//        if (!optionalUnitOfMeasurement.isPresent()) {
//            throw new Exception("Unit Of Measurement Not exist");
//        }




        List<ApplicationUser> applicationUserList = applicationUserService.findAllApplicationUserByDesignation(optionalDesignation.get().getId());
        if (applicationUserList.size() > 0) {
            throw new RuntimeException("This Designation is already in use.");
        }




        if (designation == null)
            return false;

        designation.setIsDeleted(true);
        designationRepository.save(designation);
        return true;
    }

    @Override
    public Designation findById(Long id) {
        try {
            Optional<Designation> optionalDesignation = designationRepository.findByIdAndIsDeletedFalse(id);
            if (!optionalDesignation.isPresent()) {
                throw new Exception("Account Year Not exist with id " + id);
            }
            return optionalDesignation.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<Designation> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return designationRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    public  List<Designation> findAllActiveDesignation(){
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return  designationRepository.findAllByOrganizationAndIsDeletedFalseAndIsActiveTrue(organization);
    }

    @Override
    public boolean validate(Object object) {

        Designation designation= (Designation) object;
        Optional<Designation> optionalDesignation= Optional.empty();
        if(designation.getId()==null){
            optionalDesignation=designationRepository.findByOrganizationAndNameIgnoreCaseAndIsDeletedFalse(
                    designation.getOrganization(),designation.getName());

            if(optionalDesignation.isPresent()){
                throw new RuntimeException("Designation Name is already exist");
            }
        }
        else{
            optionalDesignation=designationRepository.findByOrganizationAndIdIsNotAndNameIgnoreCaseAndIsDeletedFalse(
                    designation.getOrganization(),designation.getId(),designation.getName()
            );

            if(designation.getIsActive()==false){

                List<ApplicationUser> applicationUserList = applicationUserService.findAllActiveApplicationUser(designation.getId()) ;
                if (applicationUserList.size()>0) {
                    throw new RuntimeException("This Designation is already in use");
                }
            }
        }
        if(optionalDesignation.isPresent()){
            throw new RuntimeException("Designation Name already exist");

        }
        return true;
    }
}
