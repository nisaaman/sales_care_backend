package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.dto.LocationDto;
import com.newgen.ntlsnc.globalsettings.entity.*;
import com.newgen.ntlsnc.globalsettings.repository.LocationRepository;
import com.newgen.ntlsnc.salesandcollection.service.LocationTreeData;
import com.newgen.ntlsnc.salesandcollection.service.UserLocationTree;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class LocationService implements IService<Location> {
    @Autowired
    LocationRepository locationRepository;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    LocationTypeService locationTypeService;
    @Autowired
    UserLocationTree userLocationTree;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    DepotService depotService;

    @Autowired
    DepotLocationMapService depotLocationMapService;

    @Override
    @Transactional
    public Location create(Object object) {
        LocationDto locationDto = (LocationDto) object;
        Location location = new Location();

        location.setName(locationDto.getName());
        location.setRemarks(locationDto.getRemarks());
        location.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (locationDto.getLocationTypeId() != null) {
            location.setLocationType(locationTypeService.findById(locationDto.getLocationTypeId()));
        }
        if (locationDto.getParentId() != null) {
            location.setParent(findById(locationDto.getParentId()));
        }

        if (!this.validate(location)) {
            return null;
        }
        return locationRepository.save(location);
    }

    @Override
    @Transactional
    public Location update(Long id, Object object) {
        LocationDto locationDto = (LocationDto) object;
        Location location = findById(locationDto.getId());

        location.setName(locationDto.getName());
        location.setRemarks(locationDto.getRemarks());
        location.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (locationDto.getOrganizationId() != null) {
            location.setOrganization(organizationService.findById(locationDto.getOrganizationId()));
        }
        if (locationDto.getParentId() != null) {
            location.setParent(findById(locationDto.getParentId()));
        }
        if (!this.validate(location)) {
            return null;
        }
        return locationRepository.save(location);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            Location location = findById(id);
            location.setIsDeleted(true);
            locationRepository.save(location);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public boolean deleteAllByLocationTree(LocationTree locationTree) {
        try {
            List<LocationType> locationTypeList = locationTypeService.findAllByLocationTree(locationTree);
            List<Location> locationList = locationRepository.findAllByIsDeletedFalseAndLocationTypeInOrderByParent(locationTypeList);
            locationList.forEach(location -> {
                location.setIsDeleted(true);
            });

            locationRepository.saveAll(locationList);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Location findById(Long id) {
        try {
            Optional<Location> optionalLocation = locationRepository.findById(id);
            if (!optionalLocation.isPresent()) {
                throw new Exception("Location Not exist with id " + id);
            }
            return optionalLocation.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<Location> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return locationRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    public List<Location> getAllLocationsByIdList(List<Long> ids) {
        try {
            return locationRepository.findAllByIdInAndIsDeletedFalse(ids);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map> getAllLocationObjByDepotId(Long depotId) {
        try {
            List<Map> locations = locationRepository.getLocationsByDepotId(depotId);

            return locations;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Location> getLocationsOfDepotByDepotId(Long depotId) {
        try {
            List<Location> locations = locationRepository.getAllLocationByDepotId(depotId);

            return locations;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Location> getAllLocationByNotIdListIn(List<Long> ids) {
        try {
            List<Location> locations = locationRepository.findAllByIsDeletedFalseAndIdNotIn(ids);
            return locations;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Location> getAllLocationByIdListIn(List<Long> ids) {
        try {
            List<Location> locations = locationRepository.findAllByIsDeletedFalseAndIsActiveTrueAndIdIn(ids);
            return locations;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }

    @Transactional
    public void createAll(List<Map> locationListAsTree, Location parentLocation, List<LocationType> locationTypeList, LocationTree locationTree) {
        //Recursively save all locations that come as tree
        locationListAsTree.forEach((locationParams) -> {
            Location location = new Location();
            if (locationParams.get("id") != null) {
                location = findById(Long.parseLong(locationParams.get("id").toString()));
            } else {
                location.setOrganization(locationTree.getOrganization());
            }
            location.setName(locationParams.get("name").toString());
            location.setIsDeleted(false);
            location.setIsActive(true);
            location.setParent(parentLocation);
            // set locationType dynamically compare with locationListAsTree's treeLevel length seperated by '-' .
            location.setLocationType(locationTypeList.get(locationParams.get("treeLevel").toString().split("-").length - 1));

            location = locationRepository.save(location);

            if (((List) locationParams.get("children")).size() > 0) {
                createAll((List<Map>) locationParams.get("children"), location, locationTypeList, locationTree);
            }
        });

    }

    public List<Location> findAllByLocationTypeList(List<LocationType> locationTypeList) {
        return locationRepository.findAllByIsDeletedFalseAndLocationTypeInOrderByParent(locationTypeList); // this sort is important for efficient search
    }

    public List<Location> findAllByLocationType(LocationType locationType) {
        return locationRepository.findAllByLocationTypeAndIsDeletedFalseAndIsActiveTrueOrderByIdAsc(locationType);
    }

    public List<Location> findAllTerritoryByCompanyId(Long companyId) {   // last layer location
        LocationType maxLevelLocationType =  locationTypeService.getLastLayerLocationTypeByCompanyId(companyId);
        return findAllByLocationType(maxLevelLocationType);
    }

    public List<Location> findAllTerritoryByCompanyIdAndLocationIdList(Long companyId, List<Long> locationIdList) {   // last layer location
        List<LocationType> locationTypeList = locationTypeService.findAllByCompanyId(companyId);
        LocationType maxLevelLocationType = locationTypeList.stream().max(Comparator.comparing(LocationType::getLevel)).get();
        List<Location> territoryList = locationRepository.findAllByLocationTypeAndLocationIdInAndIsDeletedFalseAndIsActiveTrue(maxLevelLocationType, locationIdList);
        return territoryList;
    }


    public List<Map> getLocationAsTree(List<Location> locationList, Map parentNode) {
        List<Map> locationTree = new ArrayList<>();
        Integer level = 1;
        for (int i = 0; i < locationList.size(); ) {
            if (locationList.get(i).getParent() == null || locationList.get(i).getParent().getId() == parentNode.get("id")) {
                Map locationNode = new HashMap(); // location tree node

                locationNode.put("id", locationList.get(i).getId());
                locationNode.put("name", locationList.get(i).getName());
                locationNode.put("locationTypeLevel", locationList.get(i).getLocationType().getLevel());
                locationNode.put("treeLevel", parentNode.get("treeLevel") == null ? level.toString() : parentNode.get("treeLevel").toString() + "-" + level);
                locationList.remove(i);
                locationTree.add(locationNode);
                level++;
            } else {
                i++;
            }
        }
        locationTree.forEach(node -> {
            node.put("children", getLocationAsTree(locationList, node));
        });
        return locationTree;

    }

    public List<Map<String, Object>> getAllChildLocationOfACompany(Long companyId) {

        try {
            return locationRepository.getAllChildLocationOfACompany(companyId);

        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }


    public Long getManagerLocation(Long companyId, Long managerId) {
        try {
            Long managerLocation =
                    locationRepository.getManagerLocation(companyId, managerId);

            return managerLocation;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Long getLocationManager(Long companyId, Long locationId) {
        try {
            Long managerLocation =
                    locationRepository.getLocationManager(companyId, locationId);

            return managerLocation;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    public List<Location> getLocationsByParentId(Location parentLocation) {
        try {
            List<Location> locationList =
                    locationRepository.getLocationsByParentId(parentLocation);

            return locationList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Location> getParentLocationsList(Long companyId) {
        try {
            List<Location> locationList =
                    locationRepository.getParentLocationsList();

            return locationList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Map<Long, Object> getChildLocationsByParent(
            Long companyIdL, Long locationId, Map<Long, Object> childLocationMap) {

        if (null != locationId) {
            Location parentLocation =
                    findById(locationId);

            List<Location> locations =
                    getLocationsByParentId(parentLocation);

            if (locations.isEmpty()) {
                childLocationMap.put(
                        parentLocation.getId(), parentLocation.getName());
                return childLocationMap;
            } else {
                for (Location location : locations) {
                    //childLocationMap.put(location.getId(), location.getName());
                    childLocationMap =
                            getChildLocationsByParent(companyIdL, location.getId(),
                                    childLocationMap);
                }
            }
        }
        return childLocationMap;
    }

    public List<Long> getSoListByLocation(Long companyId, Map<Long, Object> childLocationMap) {
        List<Long> soListAll = new ArrayList<>();
        for (Long key : childLocationMap.keySet()) {
            Long locationManagerId = getLocationManager(companyId, key);
            List<Long> soList =
                    applicationUserService.getSoListByLocationManager(locationManagerId);
            soListAll.addAll(soList);
        }
        return soListAll;
    }

    public Map<String, Object> getSoLocation(Long companyId, Long salesOfficerId) {
        Map<String, Object> returnMap = new HashMap<>();
        Map<String, Object> soLocationMap =
                locationRepository.getSoLocation(companyId, salesOfficerId);
        returnMap.put("soLocationMap", soLocationMap);
        return returnMap;
    }

    public Long getSoLocationId(Long companyId, Long salesOfficerId) {
        return locationRepository.getSoLocationId(companyId, salesOfficerId);
    }

    public Map getTerritoryLocationByCompanyIdAndDistributorId(Long companyId, Long distributorId) {
        Map optionalMap = depotService.getDepotAndTerritoryLocationByCompanyIdAndDistributorId(companyId, distributorId);
        return optionalMap;
    }

    public Location getParentLocationByChildLocation(Long childLocationId) {
        try {
            Optional<Location> optionalLocation = locationRepository.findParentLocationByChildLocation(childLocationId);
            if (!optionalLocation.isPresent()) {
                throw new Exception("Location Not exist with child id " + childLocationId);
            }
            return optionalLocation.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Long> getLocationListFromLocationHierarchy(Long locationId) {
        return locationRepository.getLocationListFromLocationHierarchy(locationId);
    }

    public List<Location> getLocationDownToUpHierarchyByCompanyIdAndDistributorId(Long companyId, Long distributorId) {
        List<Location> locationList = new ArrayList<>();
        Map optionalMap = depotService.getDepotAndTerritoryLocationByCompanyIdAndDistributorId(companyId, distributorId);
        Long lowestLocationId = Long.parseLong(optionalMap.get("location_id").toString());
        Location location = findById(lowestLocationId);
        locationList.add(location);
        while (location.getParent() != null) {
            Location parent = findById(location.getParent().getId());
            locationList.add(parent);
            location = parent;
        }
        return locationList;
    }

    public List<Location> getAllDepotLevelLocation(Long companyId) {
        Organization company = organizationService.findById(companyId);
        LocationType locationType = locationTypeService.findDepotLevelLocationType(company.getLocationTree().getId());
        List<Location> locationList = new ArrayList<>();
        if (locationType != null) {
            locationList = findAllByLocationType(locationType);
        }
        return locationList;
    }

    public List<Location> getDepotLocationByCompany(Long companyId, List<Long> locationIds) {
        Organization company = organizationService.findById(companyId);
        LocationType locationType = locationTypeService.findDepotLevelLocationType(company.getLocationTree().getId());
        List<Location> locationList = new ArrayList<>();
        if (locationType != null) {
            locationList = locationRepository.findAllByLocationTypeAndIdNotInAndIsDeletedFalseAndIsActiveTrueOrderByIdAsc(locationType, locationIds);
        }
        return locationList;
    }

    public List<Location> getTerritoryLocationListByCompanyAndLoginUser(Long companyId) {
        Long userLoginId = applicationUserService.getApplicationUserFromLoginUser().getId();
        return getTerritoryLocationListByCompanyAndApplicationUser(companyId, userLoginId);
    }

    public List<Location> getTerritoryLocationListByCompanyAndApplicationUser(Long companyId, Long applicationUserId) {
        List<Location> territoryLocationList = new ArrayList<>();
        LocationType maxLevelLocationType = locationTypeService.getLastLayerLocationTypeByCompanyId(companyId);

        Boolean isManager = applicationUserService.checkLoginUserIsManager(companyId, applicationUserId);
        Boolean isSo = applicationUserService.checkLoginUserIsSo(companyId, applicationUserId);
        Boolean isDepotManager = applicationUserService.checkLoginUserIsDepotManager(companyId, applicationUserId);

        if (isManager) { //is manager?
            Long managerLocationId = getManagerLocation(companyId, applicationUserId);
            List<Location> locations = new ArrayList<>();
            locations = getAllChildLocationsByAnyParentLocationAndCompany(companyId, managerLocationId, locations);
            territoryLocationList = locations.stream().filter(l -> l.getLocationType() == maxLevelLocationType).collect(Collectors.toList());
        } else if (isSo) {  // is so?
            Optional<Location> optionalLocation = locationRepository.getLocationByCompanyIdAndSalesOfficerId(companyId, applicationUserId);
            if (optionalLocation.isPresent()) {
                territoryLocationList.add(optionalLocation.get());
            }
        } else if (isDepotManager) {  // is Depot Manager?
            List<Location> locationsOfDepot = new ArrayList<>();
            Map depot = depotService.getDepotByLoginUserId(companyId, applicationUserId);
            if (depot.get("id") != null) {
                locationsOfDepot = getLocationsOfDepotByDepotId(Long.parseLong(depot.get("id").toString()));
                List<Location> locations = new ArrayList<>();
                for (int i = 0; i < locationsOfDepot.size(); i++) {
                    locations = getAllChildLocationsByAnyParentLocationAndCompany(companyId, locationsOfDepot.get(i).getId(), locations);
                }
                locations = Stream.concat(locations.stream(), locationsOfDepot.stream()).collect(Collectors.toList()); // concat locationsOfDepot and other child locations
                territoryLocationList = locations.stream().filter(l -> l.getLocationType() == maxLevelLocationType).collect(Collectors.toList());
            }
        } else { // for admin
            territoryLocationList = findAllTerritoryByCompanyId(companyId);
        }

        return territoryLocationList;
    }

    public List<Location> getAllChildLocationsByAnyParentLocationAndCompany(Long companyId, Long parentLocationId, List<Location> childLocationList) {
        if (parentLocationId != null) {
            Location parentLocation = findById(parentLocationId);
            List<Location> locations = getLocationsByParentId(parentLocation);
            if (locations.isEmpty()) {
                childLocationList.add(parentLocation);
                return childLocationList;
            } else {
                for (Location location : locations) {
                    childLocationList = getAllChildLocationsByAnyParentLocationAndCompany(companyId, location.getId(), childLocationList);
                }
            }
        }
        return childLocationList;
    }

    public List<Location> getLoginUserLocationListByCompany(Long companyId) {
        Long loginUserId = applicationUserService.getApplicationUserFromLoginUser().getId();
        List<Location> locationList = new ArrayList<>();

        Boolean isManager = applicationUserService.checkLoginUserIsManager(companyId, loginUserId);
        Boolean isSo = applicationUserService.checkLoginUserIsSo(companyId, loginUserId);
        Boolean isDepotManager = applicationUserService.checkLoginUserIsDepotManager(companyId, loginUserId);

        if (isManager) { //is location manager?
            locationList = getAllManagerLocationByCompanyIdAndLocationManagerId(companyId, loginUserId);
        } else if (isSo) {  // is so?
            locationList = OfficerLocationByCompanyIdAndSalesOfficerId(companyId, loginUserId);
        } else if (isDepotManager) { // depot manager ?
            locationList = getAllDepotManagerLocationByCompanyIdAndDepotManagerId(companyId, loginUserId);
        } else { // is admin ?
            LocationType minLevelLocationType = locationTypeService.getTopLayerLocationTypeByCompanyId(companyId);
            locationList = findAllByLocationType(minLevelLocationType);
        }

        return locationList;
    }

    public List<Location> getAllManagerLocationByCompanyIdAndLocationManagerId(Long companyId, Long locationManagerId) {
        try {
            List<Location> managerLocation = locationRepository.getAllManagerLocationByCompanyIdAndLocationManagerId(companyId, locationManagerId);
            return managerLocation;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Location> OfficerLocationByCompanyIdAndSalesOfficerId(Long companyId, Long salesOfficerId) {
        try {
            List<Location> salesOfficerLocationList = locationRepository.OfficerLocationByCompanyIdAndSalesOfficerId(companyId, salesOfficerId);
            return salesOfficerLocationList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Location> getAllDepotManagerLocationByCompanyIdAndDepotManagerId(Long companyId, Long depotManagerId) {
        try {
            List<Location> depotManagerLocationList = locationRepository.getAllDepotManagerLocationByCompanyIdAndDepotManagerId(companyId, depotManagerId);
            return depotManagerLocationList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public List<Location> getLocationListWithoutDepotLocationMap(Long companyId) {
        try{
            Organization company = organizationService.findById(companyId);
            List<Long> locationIds = depotLocationMapService.getLocationWithoutDepotLocationMap(companyId);
            LocationType locationType = locationTypeService.findDepotLevelLocationType(company.getLocationTree().getId());
            List<Location> locationList = new ArrayList<>();
            if (locationType != null) {
                if(locationIds.size() > 0) {
                    locationList = locationRepository.findAllByLocationTypeAndIdNotInAndIsDeletedFalseAndIsActiveTrueOrderByIdAsc(locationType, locationIds);
                }else {
                    locationList = locationRepository.findAllByLocationTypeAndIsDeletedFalseAndIsActiveTrueOrderByIdAsc(locationType);
                }
            }
            return locationList;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map> getLocationAsTreeForReport(List<LocationTreeData> locationList, Map parentNode) {
        List<Map> locationTree = new ArrayList<>();
        Integer level = 1;
        Integer firstLocationTypeLevel = !locationList.isEmpty() ? locationList.get(0).getLocationTypeLevel() : null;
        for (int i = 0; i < locationList.size(); ) {
            if (((locationList.get(i).getParentId() == null || Objects.equals(firstLocationTypeLevel, locationList.get(i).getLocationTypeLevel())) && parentNode == null) || (parentNode != null && locationList.get(i).getParentId() == parentNode.get("id"))) {
                Map locationNode = new HashMap(); // location tree node
                locationNode.put("id", locationList.get(i).getId());
                locationNode.put("name", locationList.get(i).getLocationName());
                locationNode.put("locationTypeLevel", locationList.get(i).getLocationTypeLevel());
                locationNode.put("treeLevel", parentNode == null ? level.toString() : parentNode.get("treeLevel").toString() + "-" + level);
                locationList.remove(i);
                locationTree.add(locationNode);
                level++;
            } else {
                i++;
            }
        }
        locationTree.forEach(node -> {
            node.put("children", getLocationAsTreeForReport(locationList, node));
        });
        return locationTree;

    }
}
