package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.VehicleOwnership;
import com.newgen.ntlsnc.common.enums.VehicleType;
import com.newgen.ntlsnc.globalsettings.dto.VehicleDto;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.Vehicle;
import com.newgen.ntlsnc.globalsettings.repository.VehicleRepository;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvDeliveryChallan;
import com.newgen.ntlsnc.supplychainmanagement.service.InvDeliveryChallanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author marzia
 * Created on 16/4/22
 */

@Service
public class VehicleService implements IService<Vehicle> {

    @Autowired
    VehicleRepository vehicleRepository;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    InvDeliveryChallanService invDeliveryChallanService;

//    @Override
//    public boolean validate(Object object) {
//        return true;
//    }

    @Override
    @Transactional
    public Vehicle create(Object object) {

        VehicleDto vehicleDto = (VehicleDto) object;
        Vehicle vehicle = new Vehicle();
        vehicle.setRegistrationNo(vehicleDto.getRegistrationNo());
        vehicle.setVehicleHeight(vehicleDto.getVehicleHeight());
        vehicle.setVehicleWidth(vehicleDto.getVehicleWidth());
        vehicle.setVehicleDepth(vehicleDto.getVehicleDepth());
        vehicle.setVehicleType(VehicleType.valueOf(vehicleDto.getVehicleType()));
        vehicle.setVehicleOwnership(VehicleOwnership.valueOf(vehicleDto.getVehicleOwnership()));
        vehicle.setOrganization(organizationService.getOrganizationFromLoginUser());
        vehicle.setIsActive(vehicleDto.getIsActive());
        if (!this.validate(vehicle)) {
            return null;
        }
        return vehicleRepository.save(vehicle);
    }

    @Override
    @Transactional
    public Vehicle update(Long id, Object object) {

        VehicleDto vehicleDto = (VehicleDto) object;
        Vehicle vehicle = vehicleRepository.findById(vehicleDto.getId()).get();
        //Organization organization = organizationService.findById(vehicleDto.getOrganizationId());
        vehicle.setRegistrationNo(vehicleDto.getRegistrationNo());
        vehicle.setVehicleHeight(vehicleDto.getVehicleHeight());
        vehicle.setVehicleWidth(vehicleDto.getVehicleWidth());
        vehicle.setVehicleDepth(vehicleDto.getVehicleDepth());
        vehicle.setVehicleType(VehicleType.valueOf(vehicleDto.getVehicleType()));
        vehicle.setVehicleOwnership(VehicleOwnership.valueOf(vehicleDto.getVehicleOwnership()));
        vehicle.setOrganization(organizationService.getOrganizationFromLoginUser());
        vehicle.setIsActive(vehicleDto.getIsActive());
        if (!this.validate(vehicle)) {
            return null;
        }

        return vehicleRepository.save(vehicle);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            Vehicle vehicle = vehicleRepository.findById(id).get();

            List<InvDeliveryChallan> invDeliveryChallanList = invDeliveryChallanService.getAllInvDeliveryChallanByVehicle(vehicle.getId());
                 if(invDeliveryChallanList.size()>0) {
                   throw new RuntimeException("This Vehicle is already in use");
               }

            vehicle.setIsDeleted(true);
            vehicleRepository.save(vehicle);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Vehicle findById(Long id) {
        try {
            Optional<Vehicle> optionalVehicle = vehicleRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalVehicle.isPresent()) {
                throw new Exception("Vehicle Not exist with id " + id);
            }
            return optionalVehicle.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<Vehicle> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return vehicleRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    public List<Vehicle> findAllActive() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return vehicleRepository.findAllByOrganizationAndIsActiveTrueAndIsDeletedFalse(organization);
    }

    @Override
    public boolean validate(Object object) {
        Vehicle vehicle = (Vehicle) object;
        Optional<Vehicle> optionalVehicle = Optional.empty();

        if (vehicle.getId() == null) {
            optionalVehicle = vehicleRepository.findByOrganizationAndRegistrationNoIgnoreCaseAndIsDeletedFalse(
                    vehicle.getOrganization(), vehicle.getRegistrationNo().trim());
        } else {
            optionalVehicle = vehicleRepository.findByOrganizationAndIdIsNotAndRegistrationNoIgnoreCaseAndIsDeletedFalse(
                    vehicle.getOrganization(), vehicle.getId(), vehicle.getRegistrationNo().trim());
        }

        if (optionalVehicle.isPresent()) {
            throw new RuntimeException("Registration No already exist.");
        }

        return true;
    }
}
