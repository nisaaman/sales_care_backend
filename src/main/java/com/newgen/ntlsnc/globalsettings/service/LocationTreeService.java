package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.CommonConstant;
import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.dto.LocationTreeDto;
import com.newgen.ntlsnc.globalsettings.entity.Location;
import com.newgen.ntlsnc.globalsettings.entity.LocationTree;
import com.newgen.ntlsnc.globalsettings.entity.LocationType;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.repository.LocationTreeRepository;
import com.newgen.ntlsnc.globalsettings.repository.OrganizationRepository;
import com.newgen.ntlsnc.salesandcollection.service.LocationTreeData;
import com.newgen.ntlsnc.salesandcollection.service.UserLocationTreeService;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author kamal
 * @Date ২৫/৫/২২
 */

@Service
public class LocationTreeService implements IService<LocationTree> {
    @Autowired
    OrganizationService organizationService;
    @Autowired
    LocationTypeService locationTypeService;
    @Autowired
    LocationTreeRepository locationTreeRepository;
    @Autowired
    LocationService locationService;
    @Autowired
    DocumentSequenceService documentSequenceService;
    @Autowired
    OrganizationRepository organizationRepository;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    UserLocationTreeService userLocationTreeService;

    private static final String CLASS_NAME = "Business Location Tree";
    private static final String DUPLICATE_NAME = " name can not be duplicate";
    @Override
    @Transactional
    public LocationTree create(Object object) {
        try {
        LocationTreeDto locationTreeDto = (LocationTreeDto) object;

        // Location Tree master save
        LocationTree locationTree = new LocationTree();

        Optional<LocationTree> locationNameOpt = locationTreeRepository.findByNameIgnoreCaseAndIsDeletedFalse(locationTreeDto.getName());
        if (locationNameOpt.isPresent() && locationNameOpt.get().getId() != locationTree.getId()) {
                throw new Exception(CLASS_NAME + DUPLICATE_NAME);
        }
        locationTree.setName(locationTreeDto.getName());
        locationTree.setDescription(locationTreeDto.getDescription());
        locationTree.setCode(documentSequenceService.getSequenceByDocumentId(CommonConstant.DOCUMENT_ID_FOR_LOCATION_TREE));
        //locationTree.setOrganization(organizationService.findById(locationTreeDto.getOrganizationId()));
        locationTree.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (!this.validate(locationTree)) {
            return null;
        }
        locationTree = locationTreeRepository.save(locationTree);

        //List of LocationType save
        List<LocationType> locationTypeList = locationTypeService.createAll(locationTreeDto.getLocationTypeList(), locationTree);

        //Sort the list of LocationType by level ascending order. This sorted list index is used to get locationType as depth of LocationTree
        locationTypeList = locationTypeService.getSortedLocationTypeListByLevelAscending(locationTypeList);

        //Location list as tree save
        locationService.createAll(locationTreeDto.getLocationTree(), null, locationTypeList, locationTree);
        return locationTree;

        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public LocationTree update(Long id, Object object) {
        try {
            // update LocationTree part
            LocationTreeDto locationTreeDto = (LocationTreeDto) object;
            LocationTree locationTree = findById(locationTreeDto.getId());

            Optional<LocationTree> locationNameOpt = locationTreeRepository.findByNameIgnoreCaseAndIsDeletedFalse(locationTreeDto.getName());
            if (locationNameOpt.isPresent() && locationNameOpt.get().getId() != locationTree.getId()) {
                throw new Exception(CLASS_NAME + DUPLICATE_NAME);
            }
            locationTree.setName(locationTreeDto.getName());
            locationTree.setDescription(locationTreeDto.getDescription());
            locationTree.setOrganization(organizationService.getOrganizationFromLoginUser());
            if (!validate(locationTree)) {
                return null;
            }
            locationTree = locationTreeRepository.save(locationTree);

            //update LocationType part
            locationTypeService.deleteAllByLocationTreeId(locationTree.getId());  // before update, delete existing then update
            List<LocationType> locationTypeList = locationTypeService.createAll(locationTreeDto.getLocationTypeList(), locationTree);

            //Sort the list of LocationType by level ascending order. This sorted list index is used to get locationType as depth of LocationTree
            locationTypeList = locationTypeService.getSortedLocationTypeListByLevelAscending(locationTypeList);

            //Location list as tree save
            locationService.deleteAllByLocationTree(locationTree); // before update, delete existing then update
            locationService.createAll(locationTreeDto.getLocationTree(), null, locationTypeList, locationTree);

            return locationTree;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        LocationTree locationTree = findById(id);
        if (locationTree == null)
            return false;

        locationTree.setIsDeleted(true);
        locationTreeRepository.save(locationTree);

        return true;
    }

    @Override
    public LocationTree findById(Long id) {
        try {
            Optional<LocationTree> optionalLocationTree = locationTreeRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalLocationTree.isPresent()) {
                throw new Exception("Location Tree Not exist with id " + id);
            }
            return optionalLocationTree.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<LocationTree> findAll() {

        Organization organization = organizationService.getOrganizationFromLoginUser();
        return locationTreeRepository.findAllByOrganizationAndIsDeletedFalse(organization);
        //return locationTreeRepository.findAllByIsDeletedFalse();
    }

    public LocationTree findByIdAndStatus(Long id) {
        try {
            Optional<LocationTree> optionalLocationTree = locationTreeRepository.findByIdAndIsDeletedFalse(id);
            if (!optionalLocationTree.isPresent()) {
                throw new Exception("Location Tree Not exist with id " + id);
            }
            return optionalLocationTree.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }


    public List<Map> list(Map searchParams) {
        String searchText = "%" + searchParams.get("searchText").toString() + "%";
        String childCompanyId = searchParams.get("childCompanyId").toString();

        List<Map> locationTreeList = locationTreeRepository.findAllLocationTreeList(searchText, childCompanyId);
        return locationTreeList;
    }

    @Transactional
    public LocationTree updateActiveStatus(Long id, Boolean checked) {
        try {
            List<Organization> companyList = organizationRepository.findByLocationTreeIdAndIsActiveTrueAndIsDeletedFalse(id);
            LocationTree locationTree = new LocationTree();
            if (companyList.size() != 0) {
                throw new Exception("Location Tree Used By Company");
            } else {
                if (!checked){
                    locationTree = findById(id);
                    locationTree.setIsActive(!locationTree.getIsActive());
                }else {
                    locationTree = findByIdAndStatus(id);
                    locationTree.setIsActive(true);
                }

            }

            return locationTreeRepository.save(locationTree);
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    public Map findLocationTreeRelatedInfoByLocationTreeId(Long id) {
        Map<String, Object> response = new HashMap<>();
        LocationTree locationTree = findById(id);
        List<LocationType> locationTypeList = locationTypeService.findAllByLocationTree(locationTree);

        List<Location> locationList = locationService.findAllByLocationTypeList(locationTypeList);
        List<Map> locationAsTree = locationService.getLocationAsTree(locationList, new HashMap<>());
        response.put("locationTreeName", locationTree);
        response.put("locationTypeList", locationTypeList);
        response.put("locationAsTree", locationAsTree);
        return response;
    }

    public Map<String, Object> findLocationTreeRelatedInfoByCompanyId(Long companyId) {
        Organization company = organizationService.findById(companyId);
        Map response = findLocationTreeRelatedInfoByLocationTreeIdForReport(company.getLocationTree().getId(), companyId);
        return response;
    }

    public List<LocationTree> findAllByOrganizationId(Long organizationId) {
        return locationTreeRepository.findAllByOrganizationIdAndIsActiveTrueAndIsDeletedFalse(organizationId);
    }

    public List<LocationTree> findDepotConfiguredLocationTreeListByOrganizationId(Long organizationId) {
        List<LocationTree> locationTrees = locationTreeRepository.findDepotConfiguredLocationTreeListByOrganizationId(organizationId);
        return locationTrees;
    }

    public Map findLocationTreeRelatedInfoByLocationTreeIdForReport(Long id, Long companyId) {
        Map<String, Object> response = new HashMap<>();
        LocationTree locationTree = findById(id);
        List<LocationType> locationTypeList = locationTypeService.findAllByLocationTree(locationTree);
        Map<String, Object> result = userLocationTreeService.getResult(applicationUserService.getApplicationUserFromLoginUser().getId(), companyId);
        Long depotId = (Long) result.get("depotId");
        Long locationId = (Long) result.get("locationId");
        List<LocationTreeData> locationList = userLocationTreeService.getUserWiseLocationTreeForReport(applicationUserService.getApplicationUserFromLoginUser().getId(),
                companyId, depotId, locationId);
        List<Map> locationAsTree = locationService.getLocationAsTreeForReport(locationList, null);
        if(depotId==null && locationId==null){
            response.put("isNationalShow", true);
        }else {
            response.put("isNationalShow", false);
        }
        response.put("locationTreeName", locationTree);
        response.put("locationTypeList", locationTypeList);
        response.put("locationAsTree", locationAsTree);
        return response;
    }

}