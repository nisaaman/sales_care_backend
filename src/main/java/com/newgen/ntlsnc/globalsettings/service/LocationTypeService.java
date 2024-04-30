package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.dto.LocationTypeDto;
import com.newgen.ntlsnc.globalsettings.entity.LocationTree;
import com.newgen.ntlsnc.globalsettings.entity.LocationType;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.repository.LocationTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class LocationTypeService implements IService<LocationType> {
    @Autowired
    LocationTypeRepository locationTypeRepository;
    @Autowired
    OrganizationService organizationService;
    @Lazy
    @Autowired
    LocationTreeService locationTreeService;

    @Override
    public boolean validate(Object object) {
        return true;
    }

    @Override
    @Transactional
    public LocationType create(Object object) {
        LocationTypeDto locationTypeDto = (LocationTypeDto) object;
        LocationType locationType = new LocationType();

        locationType.setName(locationTypeDto.getName());
        locationType.setLevel(locationTypeDto.getLevel());
        locationType.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (!this.validate(locationType)) {
            return null;
        }

        return locationTypeRepository.save(locationType);
    }

    @Override
    @Transactional
    public LocationType update(Long id, Object object) {
        LocationTypeDto locationTypeDto = (LocationTypeDto) object;
        LocationType locationType = findById(locationTypeDto.getId());

        locationType.setName(locationTypeDto.getName());
        locationType.setLevel(locationTypeDto.getLevel());
        //locationType.setOrganization(organizationService.findById(locationTypeDto.getOrganizationId()));
        locationType.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (!this.validate(locationType)) {
            return null;
        }

        return locationTypeRepository.save(locationType);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        LocationType locationType = findById(id);
        if (locationType == null)
            return false;

        locationType.setIsDeleted(true);
        locationTypeRepository.save(locationType);

        return true;
    }

    @Override
    public LocationType findById(Long id) {
        try {
            Optional<LocationType> optionalLocationType = locationTypeRepository.findById(id);
            if (!optionalLocationType.isPresent()) {
                throw new Exception("Location Type Not exist with id " + id);
            }
            return optionalLocationType.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<LocationType> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return locationTypeRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Transactional
    public List<LocationType> createAll(List<LocationTypeDto> locationTypeDtoList, LocationTree locationTree) {
        List<LocationType> locationTypeList = new ArrayList<>();
        locationTypeDtoList.forEach(locationTypeDto -> {
            LocationType locationType = new LocationType();
            if (locationTypeDto.getId() != null) {
                locationType = findById(locationTypeDto.getId());
            } else {
                locationType.setOrganization(locationTree.getOrganization());
            }
            locationType.setName(locationTypeDto.getName());
            locationType.setIsDeleted(false);
            locationType.setIsActive(true);
            locationType.setLevel(locationTypeDto.getLevel());
            locationType.setLocationTree(locationTree);

            locationTypeList.add(locationType);
        });
        return locationTypeRepository.saveAll(locationTypeList);
    }

    @Transactional
    public void deleteAllByLocationTreeId(Long locationTreeId) {
        locationTypeRepository.deleteAllLocationTypeByLocationTreeId(locationTreeId);
    }

    public List<LocationType> findAllByLocationTree(LocationTree locationTree) {
        return locationTypeRepository.findAllByIsDeletedFalseAndLocationTreeOrderByLevelAsc(locationTree); // this sort is most important
    }

    public List<LocationType> findAllByLocationTreeId(Long locationTreeId) {
        return locationTypeRepository.findAllByIsDeletedFalseAndLocationTreeIdOrderByLevelAsc(locationTreeId); // this sort is most important
    }

    public List<LocationType> getSortedLocationTypeListByLevelAscending(List<LocationType> locationTypeList) {
        Collections.sort(locationTypeList, new Comparator<LocationType>() {
            @Override
            public int compare(LocationType o1, LocationType o2) {
                return o1.getLevel().compareTo(o2.getLevel());
            }
        });
        return locationTypeList;
    }

    public List<LocationType> findAllByCompanyId(Long companyId) {
        return locationTypeRepository.findAllByCompanyId(companyId);
    }

    public LocationType findDepotLevelLocationType(Long locationTreeId) {
        try {
            Optional<LocationType> optionalLocationType = locationTypeRepository.findByLocationTreeIdAndIsDepotLevelTrueAndIsActiveTrueAndIsDeletedFalse(locationTreeId);
            if (!optionalLocationType.isPresent()) {
                return null;
            }
            return optionalLocationType.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Boolean addDepotLocationLevelMap(Long locationTypeId) {
        try {
            LocationType locationType = findById(locationTypeId);
            List<LocationType> locationTypeList = locationTypeRepository.findAllByIsDeletedFalseAndLocationTreeIdOrderByLevelAsc(locationType.getLocationTree().getId());
            locationTypeList.forEach(l -> {
                if (l.getId() == locationTypeId) {
                    l.setIsDepotLevel(true);
                } else {
                    l.setIsDepotLevel(false);
                }
            });
            locationTypeRepository.saveAll(locationTypeList);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<LocationType> getAllDepotConfiguredLocationLevelByOrganization(Long organizationId) {
        List<LocationType> locationTypeList = locationTypeRepository.findAllByOrganizationIdAndIsDepotLevelIsTrueAndIsDeletedFalse(organizationId);
        return locationTypeList;
    }

    public Map getAllDepotConfiguredLocationLevelListAndLocationTreeListByLoginOrganization() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        Map map = new HashMap();
        map.put("locationTypeList", getAllDepotConfiguredLocationLevelByOrganization(organization.getId()));
        map.put("locationTreeList", locationTreeService.findDepotConfiguredLocationTreeListByOrganizationId(organization.getId()));
        return map;
    }

    @Transactional
    public Boolean deleteLocationLevelOfDepot(Long locationTypeId) {
        try {
            locationTypeRepository.removeLocationLevelOfDepotById(locationTypeId);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public LocationType getLastLayerLocationTypeByCompanyId(Long companyId){ // last layer location type
        List<LocationType> locationTypeList = findAllByCompanyId(companyId);
        LocationType maxLevelLocationType = locationTypeList.stream().max(Comparator.comparing(LocationType::getLevel)).get();
        return maxLevelLocationType;
    }

    public LocationType getTopLayerLocationTypeByCompanyId(Long companyId){ // last layer location type
        List<LocationType> locationTypeList = findAllByCompanyId(companyId);
        LocationType minLevelLocationType = locationTypeList.stream().min(Comparator.comparing(LocationType::getLevel)).get();
        return minLevelLocationType;  // top layer
    }

    public List<Map> getAllLocationType(Long companyId) {
        List<Map> locationTypeList = locationTypeRepository.findAllLocationTypeList(companyId);
        return locationTypeList;
    }
}
