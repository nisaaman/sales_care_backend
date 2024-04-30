package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.globalsettings.entity.Location;
import com.newgen.ntlsnc.globalsettings.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Newaz Sharif
 * @since 21st May, 22
 */
@Service
public class UserLocationTree {

    @Autowired
    LocationRepository locationRepository;

    public List<LocationTreeData> getCompanyLocationTree(Long companyId) throws Exception{

        List<Location> allLocationMapList = locationRepository.
                getCompanyLocationTree(companyId);

        List<LocationTreeData> allLocationList = new ArrayList<>();
        for (Location location : allLocationMapList) {

            allLocationList.add(new LocationTreeData(location.getId(),
                    location.getParent() != null ? Long.parseLong(
                            String.valueOf(location.getParent().getId())) : null, location.getName(), location.getLocationType().getLevel()));
        }
        List<LocationTreeData> parent = allLocationList.stream()
                                             .filter(location -> location.getParentId() == null)
                                             .collect(Collectors.toList());

        parent.forEach( eachParent -> {
            eachParent.setChildren(getChild(eachParent.getId(), allLocationList));
        });
        return parent;

    }

    @Transactional
    public List<LocationTreeData> getSalesOfficerLocationTree(Long companyId, Long salesOfficerUserId) throws Exception{

        List<Map<String, Object>> allLocationMapList = locationRepository.getSalesOfficerLocationTree(
                companyId, salesOfficerUserId);

        List<LocationTreeData> allLocationList = new ArrayList<>();
        List<LocationTreeData> salesOfficerTree = new ArrayList<>();

        for (Map location : allLocationMapList) {

            allLocationList.add(new LocationTreeData(Long.parseLong(String.valueOf(location.get("id"))),
                    location.get("parentId") != null ? Long.parseLong(
                            String.valueOf(location.get("parentId"))) : null,
                            String.valueOf(location.get("locationName")), Integer.parseInt(location.get("locationTypeLevel").toString())));
        }
        if (allLocationList.size()>0) {
            LocationTreeData parent = allLocationList.stream().findFirst().get();

            parent.setChildren(getChild(parent.getId(), allLocationList));
            salesOfficerTree.add(parent);
        }

        return salesOfficerTree;

    }

    @Transactional
    public List<LocationTreeData> getSalesOfficerLocationTree(Long companyId, List<Long> salesOfficerUserIds) throws Exception{

        List<LocationTreeData> salesOfficerTree = new ArrayList<>();

        for (Long salesOfficerUserId : salesOfficerUserIds) {
            List<LocationTreeData> allLocationList = new ArrayList<>();
            List<Map<String, Object>> allLocationMapList = locationRepository.getSalesOfficerLocationTree(
                    companyId, salesOfficerUserId);

            for (Map location : allLocationMapList) {
                allLocationList.add(new LocationTreeData(Long.parseLong(String.valueOf(location.get("id"))),
                        location.get("parentId") != null ? Long.parseLong(
                                String.valueOf(location.get("parentId"))) : null,
                        String.valueOf(location.get("locationName")), Integer.parseInt(location.get("locationTypeLevel").toString())));
            }
            if (allLocationList.size() > 0) {
                LocationTreeData parent = allLocationList.stream().findFirst().get();

                parent.setChildren(getChild(parent.getId(), allLocationList));
                salesOfficerTree.add(parent);
            }
        }
        return salesOfficerTree;

    }

    public static List<LocationTreeData> getChild(Long id, List<LocationTreeData> allLocationList) {

        List<LocationTreeData> childList = new ArrayList<>();
        for (LocationTreeData location : allLocationList) {
            if (id.equals(location.getParentId())) {
                childList.add(location);
            }
        }

        for (LocationTreeData subChild : childList) {
            subChild.setChildren(getChild(subChild.getId(), allLocationList));
        }

        childList = childList.stream()
                             .sorted(Comparator.comparing(LocationTreeData::getId))
                             .collect(Collectors.toList());

        if (childList.size() == 0) {
            return new ArrayList<>();
        }

        return childList;
    }

    public List<LocationTreeData> getCompanyLocationTreeForReport(Long companyId) throws Exception{

        List<Location> allLocationMapList = locationRepository.
                getCompanyLocationTree(companyId);

        List<LocationTreeData> allLocationList = new ArrayList<>();
        for (Location location : allLocationMapList) {

            allLocationList.add(new LocationTreeData(location.getId(),
                    location.getParent() != null ? Long.parseLong(
                            String.valueOf(location.getParent().getId())) : null, location.getName(), location.getLocationType().getLevel()));
        }
        return allLocationList;
    }

    @Transactional
    public List<LocationTreeData> getSalesOfficerLocationTreeForReport(Long companyId, List<Long> salesOfficerUserIds) throws Exception{

        List<LocationTreeData> salesOfficerTree = new ArrayList<>();

        for (Long salesOfficerUserId : salesOfficerUserIds) {
            List<LocationTreeData> allLocationList = new ArrayList<>();
            List<Map<String, Object>> allLocationMapList = locationRepository.getSalesOfficerLocationTree(
                    companyId, salesOfficerUserId);

            for (Map location : allLocationMapList) {
                allLocationList.add(new LocationTreeData(Long.parseLong(String.valueOf(location.get("id"))),
                        location.get("parentId") != null ? Long.parseLong(
                                String.valueOf(location.get("parentId"))) : null,
                        String.valueOf(location.get("locationName")), Integer.parseInt(location.get("locationTypeLevel").toString())));
            }
            if (allLocationList.size() > 0) {
                salesOfficerTree.addAll(allLocationList);
            }
        }
        return salesOfficerTree;

    }


    @Transactional
    public List<LocationTreeData> getSalesOfficerLocationTreeForReport(Long companyId, Long salesOfficerUserId) throws Exception{

        List<Map<String, Object>> allLocationMapList = locationRepository.getSalesOfficerLocationTree(
                companyId, salesOfficerUserId);

        List<LocationTreeData> allLocationList = new ArrayList<>();
        List<LocationTreeData> salesOfficerTree = new ArrayList<>();

        for (Map location : allLocationMapList) {

            allLocationList.add(new LocationTreeData(Long.parseLong(String.valueOf(location.get("id"))),
                    location.get("parentId") != null ? Long.parseLong(
                            String.valueOf(location.get("parentId"))) : null,
                    String.valueOf(location.get("locationName")), Integer.parseInt(location.get("locationTypeLevel").toString())));
        }
        if (allLocationList.size()>0) {
            salesOfficerTree.addAll(allLocationList);
        }

        return salesOfficerTree;

    }
}
