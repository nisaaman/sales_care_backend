package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.dto.LocationManagerMapDto;
import com.newgen.ntlsnc.globalsettings.entity.*;
import com.newgen.ntlsnc.globalsettings.repository.LocationManagerMapRepository;
import com.newgen.ntlsnc.usermanagement.dto.ReportingManagerDto;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import com.newgen.ntlsnc.usermanagement.entity.ReportingManager;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import com.newgen.ntlsnc.usermanagement.service.ReportingManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author anika
 * @Date ২/৬/২২
 */
@Service
public class LocationManagerMapService implements IService<LocationManagerMap> {

    @Autowired
    LocationService locationService;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    LocationManagerMapRepository locationManagerMapRepository;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    LocationTypeService locationTypeService;
    @Autowired
    ReportingManagerService reportingManagerService;
    @Autowired
    DepotLocationMapService depotLocationMapService;

    @Transactional
    @Override
    public LocationManagerMap create(Object object) {

        LocationManagerMapDto locationManagerMapDto = (LocationManagerMapDto) object;
        LocationManagerMap locationManagerMap =new LocationManagerMap();

        locationManagerMap.setFromDate(LocalDate.now());

        locationManagerMap.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (locationManagerMapDto.getCompanyId()!= null) {
            locationManagerMap.setCompany(organizationService.findById(locationManagerMapDto.getCompanyId()));
        }
        if (locationManagerMapDto.getLocationId()!= null) {
          locationManagerMap.setLocation(locationService.findById(locationManagerMapDto.getLocationId()));
        }
        if (locationManagerMapDto.getApplicationUserId()!= null) {
           locationManagerMap.setApplicationUser(applicationUserService.findById(locationManagerMapDto.getApplicationUserId()));
        }

        if (!this.validateLocationManagerMap(locationManagerMap, locationManagerMapDto)) {
            return null;
        }

        LocationType locationType = locationTypeService.findById(locationManagerMapDto.getLocationTypeId());
        if(locationType.getLevel().equals(1)){
            ReportingManagerDto reportingManagerDto = new ReportingManagerDto();
            reportingManagerDto.setApplicationUserId(locationManagerMapDto.getApplicationUserId());
            reportingManagerService.create(reportingManagerDto);
        }

        return locationManagerMapRepository.save(locationManagerMap);

    }

    @Transactional
    @Override
    public LocationManagerMap update(Long id, Object object) {

        LocationManagerMapDto locationManagerMapDto = (LocationManagerMapDto) object;
        LocationManagerMap locationManagerMap= this.findById(locationManagerMapDto.getId());
        LocationManagerMap locationManagerNew = new LocationManagerMap();

        if (locationManagerMapDto.getApplicationUserId().equals(locationManagerMap.getApplicationUser().getId())) {
            throw new RuntimeException("This User is already assign for This Location.");
        }else{
            LocalDate date = LocalDate.now();
            LocalDate yesterday = date.minusDays(1);
            locationManagerMap.setToDate(yesterday);

            locationManagerNew.setOrganization(organizationService.getOrganizationFromLoginUser());

            if (locationManagerMapDto.getLocationId()!= null) {
                locationManagerNew.setLocation(locationService.findById(locationManagerMapDto.getLocationId()));
            }

            if (locationManagerMapDto.getApplicationUserId()!= null) {
                locationManagerNew.setApplicationUser(applicationUserService.findById(locationManagerMapDto.getApplicationUserId()));
            }
            if (locationManagerMapDto.getCompanyId()!= null) {
                locationManagerNew.setCompany(organizationService.findById(locationManagerMapDto.getCompanyId()));
            }

            locationManagerNew.setFromDate(LocalDate.now());
        }
        if (!this.validateLocationManagerMap(locationManagerMap, locationManagerMapDto)) {
            return null;
        }

        locationManagerMapRepository.save(locationManagerMap);

        return locationManagerMapRepository.save(locationManagerNew);

    }

    @Transactional
    @Override
    public boolean delete(Long id) {
        try {
            Optional<LocationManagerMap> optionalLocationManagerMap = locationManagerMapRepository.findById(id);
            if (!optionalLocationManagerMap.isPresent()) {
                throw new Exception("Location Manager Map Not exist");
            }
            LocationManagerMap locationManagerMap = optionalLocationManagerMap.get();
            locationManagerMap.setIsDeleted(true);
            locationManagerMapRepository.save(locationManagerMap);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    @Override
    public LocationManagerMap findById(Long id) {
        try {
            Optional<LocationManagerMap> optionalLocationManagerMap = locationManagerMapRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalLocationManagerMap.isPresent()) {
                throw new Exception("Payment Book Not exist with id " + id);
            }
            return optionalLocationManagerMap.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    @Override
    public List<LocationManagerMap> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return locationManagerMapRepository.findAllByOrganizationAndIsDeletedFalseAndIsActiveTrueAndToDateIsNull(organization);
    }

    @Override
    public boolean validate(Object object) {
        return false;
    }


    public List<LocationManagerMap> findAllByCompanyIdAndLocationId() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return locationManagerMapRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Transactional
    //@Override
    public boolean validateLocationManagerMap(Object object, LocationManagerMapDto locationManagerMapDto) {

        LocationManagerMap locationManagerMap = (LocationManagerMap) object;
        Optional<LocationManagerMap> optionalLocationManagerMap = Optional.empty();
        Optional<LocationManagerMap> optionalLocationManagerMap2 = Optional.empty();

        if(locationManagerMap.getId() == null) {
            optionalLocationManagerMap = locationManagerMapRepository.findByOrganizationAndCompanyAndApplicationUserAndToDateIsNullAndIsDeletedFalseAndIsActiveTrue(
                    locationManagerMap.getOrganization(), locationManagerMap.getCompany(),locationManagerMap.getApplicationUser());
            optionalLocationManagerMap2 = locationManagerMapRepository.findByOrganizationAndCompanyAndLocationAndToDateIsNullAndIsDeletedFalseAndIsActiveTrue(
                    locationManagerMap.getOrganization(), locationManagerMap.getCompany(),locationManagerMap.getLocation());

        } else {
            optionalLocationManagerMap = locationManagerMapRepository.findByOrganizationAndCompanyAndApplicationUserAndToDateIsNullAndIsDeletedFalseAndIsActiveTrue(
                    locationManagerMap.getOrganization(), locationManagerMap.getCompany(),locationManagerMap.getApplicationUser());

            optionalLocationManagerMap2 = locationManagerMapRepository.findByOrganizationAndCompanyAndApplicationUserIdAndToDateIsNullAndIsDeletedFalseAndIsActiveTrue(
                    locationManagerMap.getOrganization(), locationManagerMap.getCompany(),
                    locationManagerMapDto.getApplicationUserId());
        }

        if(optionalLocationManagerMap.isPresent()){
            throw new RuntimeException("Location Manager already exist.");
        }
        if(optionalLocationManagerMap2.isPresent()){
            throw new RuntimeException("Location Manager already exist.");
        }
        return true;
    }

    public Long getLoggedInUserLocationId(Long loggedInUserId, Long organizationId, Long companyId) {

        return locationManagerMapRepository.getLoggedInUserLocation(loggedInUserId,
                organizationId, companyId).get("id") != null ? Long.parseLong(String.valueOf(
                            locationManagerMapRepository.getLoggedInUserLocation(loggedInUserId,
                organizationId, companyId).get("id"))) : null;
    }

    public List<Long> getSalesOfficerListFromManager(Long userLoginId) {

        List<Map<String, Object>> salesOfficerList = locationManagerMapRepository.
                    getSalesOfficerListFromSalesManager(userLoginId);

        return salesOfficerList.stream()
                               .filter(Objects::nonNull)
                               .map( e -> Long.parseLong(String.valueOf(e.get("salesOfficer"))))
                               .collect(Collectors.toList());
    }

    public List<LocationManagerMap> findAllByLocationList(List<Location> locationList) {
        return locationManagerMapRepository.findAllByLocationInAndIsActiveTrueAndIsDeletedFalse(locationList);
    }

    public List<Long> getSalesOfficerListFromUserLoginIdOrLocationId(
            Long userLoginId, Long locationId, Long companyId) {
        Long organizationId = null;
        Organization organization = organizationService.findByIdAndIsDeletedFalseAndActiveTrue
                (companyId);

        if(organization.getId() != null) {
            organizationId = organization.getParent() == null ? companyId :
                    organization.getParent().getId();
        }

        Supplier<Optional<LocationManagerMap>> locationManagerMap = () ->
                locationManagerMapRepository
                    .findByIsActiveTrueAndIsDeletedFalseAndToDateIsNullAndCompanyIdAndLocationId
                        (companyId, locationId);

        Long userLocationId = locationId == null ? getLoggedInUserLocationId(userLoginId, organizationId, companyId) :
                locationManagerMap.get().isPresent() == true ?
                        locationManagerMap.get().get().getLocation().getId() : null;

        List<Long> salesOfficerUserLoginId = null;

        if(userLocationId != null) {

            Long managerUserLoginId = getUserLoginIdFromLocationId(companyId,userLocationId);
            if(managerUserLoginId != null) {
                List<Map<String, Object>> salesOfficerList = locationManagerMapRepository.
                        getSalesOfficerListFromSalesManager(managerUserLoginId);

                salesOfficerUserLoginId = salesOfficerList.stream()
                        .filter(Objects::nonNull)
                        .map( e -> Long.parseLong(String.valueOf(e.get("salesOfficer"))))
                        .collect(Collectors.toList());
            }

        }
        return salesOfficerUserLoginId;
    }

    public Map<String, Object> getSalesOfficerLocation(Long loggedInUserId, Long organizationId, Long companyId) {
        return locationManagerMapRepository.getLoggedInUserLocation(loggedInUserId,
                organizationId, companyId);
    }

    @Transactional
    public List<LocationManagerMap> getAllByCompany(Long companyId) {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        Organization company = organizationService.findByIdAndIsDeletedFalseAndActiveTrue(companyId);
        return locationManagerMapRepository.findAllByOrganizationAndCompanyAndIsDeletedFalseAndIsActiveTrueAndToDateIsNull(organization,company);
    }

    public Long getUserLoginIdFromLocationId(Long companyId, Long locationId) {
        Optional<LocationManagerMap> locationManager = locationManagerMapRepository
                .findByIsActiveTrueAndIsDeletedFalseAndToDateIsNullAndCompanyIdAndLocationId
                        (companyId, locationId);

        if (locationManager.isPresent())
               return locationManager.get().getApplicationUser().getId();
        return null;
    }

    public List<Long> getDepotSalesManagerUserLoginId(Long companyId, Long depotId) {
        List<Long> locationManagerId = new ArrayList<>();
        List<Map> locationListMap = depotLocationMapService.getLocationListOfDepot(companyId,depotId);

        for (Map location : locationListMap) {
            Long depotLocation = !location.isEmpty() ? Long.parseLong(
                    String.valueOf(location.get("id"))) : null;

            locationManagerId.add(getUserLoginIdFromLocationId(companyId, depotLocation));
        }
        return locationManagerId;
    }
}
