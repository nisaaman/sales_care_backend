package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.globalsettings.entity.Location;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.repository.OrganizationRepository;
import com.newgen.ntlsnc.globalsettings.service.DepotLocationMapService;
import com.newgen.ntlsnc.globalsettings.service.DepotService;
import com.newgen.ntlsnc.globalsettings.service.LocationManagerMapService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Newaz Sharif
 * @since 13th June,22
 */

@Service
public class UserLocationTreeService {

    @Autowired
    UserLocationTree userLocationTree;
    @Autowired
    LocationManagerMapService locationManagerMapService;
    @Autowired
    OrganizationRepository organizationRepository;
    @Autowired
    DepotService depotService;
    @Autowired
    DepotLocationMapService depotLocationMapService;

    public List<LocationTreeData> getUserWiseLocationTree(Long userLoginId,
                                                          Long companyId) {
        try {

            Optional<Organization> organization = organizationRepository.findByIdAndIsDeletedFalseAndIsActiveTrue
                    (companyId);

            Long organizationId  = null;
            if(organization.isPresent()) {
                organizationId = organization.get().getParent() == null ? companyId :
                        organization.get().getParent().getId();

            }

            Long locationId = locationManagerMapService
                    .getLoggedInUserLocationId(userLoginId, organizationId, companyId);

            Map depotMap = depotService.getDepotByLoginUserId(companyId,userLoginId);
            Long depotId = !depotMap.isEmpty() ? Long.parseLong(String.valueOf(depotMap.get("id"))) : null;

            if(depotId != null) {
                return userLocationTree.getSalesOfficerLocationTree(
                        companyId, locationManagerMapService.getDepotSalesManagerUserLoginId(companyId, depotId));
            }

            if(locationId != null) {
                return userLocationTree.getSalesOfficerLocationTree(companyId, userLoginId);
            }

            return userLocationTree.getCompanyLocationTree(companyId);

        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public List<LocationTreeData> getUserWiseLocationTreeForReport(Long userLoginId,
                                                           Long companyId, Long depotId, Long locationId) {
        try {
            if(depotId != null) {
                return userLocationTree.getSalesOfficerLocationTreeForReport(
                        companyId, locationManagerMapService.getDepotSalesManagerUserLoginId(companyId, depotId));
            }

            if(locationId != null) {
                return userLocationTree.getSalesOfficerLocationTreeForReport(companyId, userLoginId);
            }

            return userLocationTree.getCompanyLocationTreeForReport(companyId);

        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public Map<String, Object> getResult(Long userLoginId, Long companyId) {
        Optional<Organization> organization = organizationRepository.findByIdAndIsDeletedFalseAndIsActiveTrue
                (companyId);

        Long organizationId  = null;
        if(organization.isPresent()) {
            organizationId = organization.get().getParent() == null ? companyId :
                    organization.get().getParent().getId();

        }

        Long locationId = locationManagerMapService
                .getLoggedInUserLocationId(userLoginId, organizationId, companyId);

        Map depotMap = depotService.getDepotByLoginUserId(companyId, userLoginId);
        Long depotId = !depotMap.isEmpty() ? Long.parseLong(String.valueOf(depotMap.get("id"))) : null;
        return new HashMap<String, Object>() {{
            put("locationId", locationId);
            put("depotId", depotId);
        }};
    }
}
