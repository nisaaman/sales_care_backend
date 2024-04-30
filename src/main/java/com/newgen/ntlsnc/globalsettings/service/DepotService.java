package com.newgen.ntlsnc.globalsettings.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newgen.ntlsnc.common.CommonConstant;
import com.newgen.ntlsnc.common.DateUtil;
import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.common.enums.InvTransactionType;
import com.newgen.ntlsnc.globalsettings.dto.DepotDto;
import com.newgen.ntlsnc.globalsettings.dto.DepotQualityAssuranceMapDto;
import com.newgen.ntlsnc.globalsettings.entity.*;
import com.newgen.ntlsnc.globalsettings.repository.DepotQualityAssuranceMapRepository;
import com.newgen.ntlsnc.globalsettings.repository.DepotRepository;
import com.newgen.ntlsnc.salesandcollection.entity.DistributorGuarantor;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import com.newgen.ntlsnc.usermanagement.service.ReportingManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DepotService implements IService<Depot> {
    @Autowired
    DepotRepository depotRepository;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    DocumentSequenceService documentSequenceService;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    LocationService locationService;
    @Autowired
    DepotLocationMapService depotLocationMapService;

    @Autowired
    DepotQualityAssuranceMapRepository depotQualityAssuranceMapRepository;

    @Autowired
    DepotQualityAssuranceMapService depotQualityAssuranceMapService;
    @Autowired
    ReportingManagerService reportingManagerService;

    @Override
    @Transactional
    public Depot create(Object object) {
        DepotDto depotDto = (DepotDto) object;
        Depot depot = new Depot();
        List<DepotLocationMap> depotLocationMaps = new ArrayList<>();

        depot.setDepotName(depotDto.getDepotName());
        depot.setAddress(depotDto.getAddress());
        depot.setCode(documentSequenceService.getSequenceByDocumentId(CommonConstant.DOCUMENT_ID_FOR_DEPOT_ID));
        depot.setContactNumber(depotDto.getContactNumber());
        depot.setIsCentralWarehouse(depotDto.getIsCentralWarehouse());
        depot.setIsActive(depotDto.getIsActive());
        depot.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (depotDto.getDepotManagerId() != null) {
            depot.setDepotManager(applicationUserService.getUserById(depotDto.getDepotManagerId()));
        }
        if (!this.validate(depot)) {
            return null;
        }

        depot = depotRepository.save(depot);

        if (depotDto.getDepotCompanyLocationList() != null) {
            List<Map<String, Object>> depotCompanyLocationList = depotDto.getDepotCompanyLocationList();
            for (Map<String, Object> depotCompanyLocation : depotCompanyLocationList) {
                List<Map<String, Object>> locationList = (List<Map<String, Object>>) depotCompanyLocation.get("areaList");
                List<Long> locationIds = new ArrayList<>();
                for (Map<String, Object> location : locationList) {
                    locationIds.add(Long.parseLong(location.get("id").toString()));
                }
                List<Location> locations = locationService.getAllLocationsByIdList(locationIds);
                Depot finalDepot = depot;
                locations.forEach(location -> {
                    DepotLocationMap depotLocationMap = new DepotLocationMap();
                    depotLocationMap.setDepot(finalDepot);
                    depotLocationMap.setLocation(location);
                    depotLocationMap.setCompany(organizationService.findById(Long.parseLong(depotCompanyLocation.get("companyId").toString())));
                    depotLocationMap.setOrganization(finalDepot.getOrganization());
                    depotLocationMaps.add(depotLocationMap);
                });
            }
        }
        depotLocationMapService.saveAllDepotLocationMap(depotLocationMaps);

        if (depotDto.getDepotQualityAssuranceMapDtoList() != null) {
            List<DepotQualityAssuranceMap> depotQualityAssuranceMapList = depotQualityAssuranceMapService.saveQAByDepot(depotDto.getDepotQualityAssuranceMapDtoList(), depot);
        }
        return depot;
    }

    @Override
    @Transactional
    public Depot update(Long id, Object object) {
        DepotDto depotDto = (DepotDto) object;
        List<DepotLocationMap> depotLocationMaps = new ArrayList<>();
        Depot depot = findById(depotDto.getId());

        depot.setDepotName(depotDto.getDepotName());
        depot.setAddress(depotDto.getAddress());
        depot.setContactNumber(depotDto.getContactNumber());
        depot.setIsCentralWarehouse(depotDto.getIsCentralWarehouse());
        depot.setOrganization(organizationService.getOrganizationFromLoginUser());
        if (depotDto.getDepotManagerId() != null) {
            ApplicationUser depotManager = applicationUserService.getUserById(depotDto.getDepotManagerId());
            depot.setDepotManager(depotManager);
        }
        if (!this.validate(depot)) {
            return null;
        }
        depotLocationMapService.deleteAllDepotLocationMapByDepotId(depot.getId());
        if (depotDto.getDepotCompanyLocationList() != null) {
            List<Map<String, Object>> depotCompanyLocationList = depotDto.getDepotCompanyLocationList();
            for (Map<String, Object> depotCompanyLocation : depotCompanyLocationList) {
                List<Map<String, Object>> locationList = (List<Map<String, Object>>) depotCompanyLocation.get("areaList");
                List<Long> locationIds = new ArrayList<>();
                for (Map<String, Object> location : locationList) {
                    locationIds.add(Long.parseLong(location.get("id").toString()));
                }
                List<Location> locations = locationService.getAllLocationsByIdList(locationIds);
                locations.forEach(location -> {
                    DepotLocationMap depotLocationMap = new DepotLocationMap();
                    depotLocationMap.setDepot(depot);
                    depotLocationMap.setLocation(location);
                    depotLocationMap.setCompany(organizationService.findById(Long.parseLong(depotCompanyLocation.get("companyId").toString())));
                    depotLocationMap.setOrganization(depot.getOrganization());
                    depotLocationMaps.add(depotLocationMap);
                });
            }
        }
        depotLocationMapService.saveAllDepotLocationMap(depotLocationMaps);

        if (depotDto.getDepotQualityAssuranceMapDtoList() != null) {
            List<DepotQualityAssuranceMap> depotQualityAssuranceMapUpdate =
                    depotQualityAssuranceMapService.updateQAByDepot(
                            depotDto.getDepotQualityAssuranceMapDtoList(), depot);
        }

        return depot;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        Depot depot = findById(id);
        try {
            if (depot == null) {
                throw new Exception("Depot Not exist");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        //depot.setIsDeleted(true);
        depotRepository.save(depot);
        depotLocationMapService.deleteAllDepotLocationMapByDepotId(depot.getId());

        return true;
    }

    @Override
    public Depot findById(Long id) {
        try {
            Optional<Depot> optionalDepot = depotRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalDepot.isPresent()) {
                throw new Exception("Depot not exist");
            }
            return optionalDepot.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<Depot> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return depotRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    public List<Map> list(Map searchParams) {
        String searchText = "%" + searchParams.get("searchText").toString() + "%";
        String searchAreaId = searchParams.get("searchAreaId").toString();

        List<Map> depotList = depotRepository.findAllDepotList(searchText, searchAreaId);
        return depotList;
    }

    @Transactional
    public Depot updateActiveStatus(Long id) {
        Optional<Depot> optionalDepot = depotRepository.findById(id);
        try {
            if (!optionalDepot.isPresent()) {
                throw new Exception("Depot does not exist!");
            }
            Depot depot = optionalDepot.get();
            depot.setIsActive(!depot.getIsActive());
            return depotRepository.save(depot);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
//        Depot depot = optionalDepot.get();
//        depot.setIsActive(!depot.getIsActive());
//        return depotRepository.save(depot);
    }

    public Map<String, Object> findDepotInfoById(Long id) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> companyAndLocationList = depotLocationMapService.getAllLocationAndCompanyByDepotId(id);
        List<Map<String, Object>> locationList = new ArrayList<>();
        for (Map<String, Object> companyAndLocation : companyAndLocationList) {
            Map<String, Object> companyAndLocationMap = new HashMap<>(companyAndLocation);
            try {
                companyAndLocationMap.put("areaList", new ObjectMapper().readValue(companyAndLocation.get("areaList").toString(), new TypeReference<List<HashMap<String, Object>>>() {
                }));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            List<Long> locationIds = Arrays
                    .stream(companyAndLocation.get("locationIds").toString().split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            companyAndLocationMap.put("areaIdArray", locationService.getDepotLocationByCompany(Long.parseLong(companyAndLocation.get("companyId").toString()), locationIds));
            locationList.add(companyAndLocationMap);
        }
        response.put("qaList", depotQualityAssuranceMapService.findQAByDepot(id));
        response.put("depot", findById(id));
        response.put("locations", companyAndLocationList);
        response.put("dropdownLocations", locationList);
        return response;
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }

    public Map getDepotAndTerritoryLocationByCompanyIdAndDistributorId(Long companyId, Long distributorId) {
        Optional<Map> optionalMap = depotRepository.findDepotAndTerritoryLocationByDistributorId(companyId, distributorId);
        return optionalMap.get();
    }

    public Map getDepotByCompanyIdAndDistributorId(Long companyId, Long distributorId) {
        return getDepotAndTerritoryLocationByCompanyIdAndDistributorId(companyId, distributorId);
    }

    public Map getDepotByLoginUserId(Long companyId, Long loginUserId) {
        Optional<Map> optionalMap = depotRepository.getDepotByLoginUserId(companyId, loginUserId);
        return optionalMap.get();
    }

    public List<Map> findDepotAreaList(Long companyId, Long loginUserId) {
        List<Map> areaList = new ArrayList<>();
        Map depot = getDepotByLoginUserId(companyId, loginUserId);
        if (depot.get("id") != null) {
            Long depotId = Long.parseLong(depot.get("id").toString());

            areaList = locationService.getAllLocationObjByDepotId(depotId);
        }
        return areaList;
    }

    public List<Map<String, Object>> getCompanyCentralWareHouseInfo(Long companyId) {

        return depotRepository.getCompanyCentralWareHouseInfo(companyId);
    }

    public List<Map<String, Object>> getCompanyUserCentralWareHouseInfo(Long companyId, Long userLoginId) {
        return depotRepository.getCompanyUserCentralWareHouseInfo(companyId, userLoginId);
    }

    public List<Map<String, Object>> getCompanyAllDepots(Long companyId) {
        return depotRepository.getCompanyAllDepots(companyId);
    }

    public Map getSalesOfficerDepotInfo(Long companyId, Long salesOfficerId) {
        if (null == salesOfficerId) {
            return new HashMap();
        }
        Optional<Map> depotInfo = depotRepository.getSalesOfficerDepotInfo(
                companyId, salesOfficerId);
        if (depotInfo.get().get("id") == null) {
            Long managerId = reportingManagerService.getReportingTo(companyId, salesOfficerId);
            return getSalesOfficerDepotInfo(companyId, managerId);

        }
        return depotInfo.get();

    }

    public List<Map<String, Object>> depotToDepotMovementHistoryReport(
            Long companyId, List<Long> categoryIds,
            List<Long> depotIds, List<Long> productIds,
            LocalDate startDate, LocalDate endDate) {

        try {

            List<String> transactionTypes = Stream.of(
                    InvTransactionType.TRANSFER_SENT.toString()
            ).collect(Collectors.toList());

            List<Map<String, Object>> movementList =
                    depotRepository.depotToDepotMovementHistory(companyId,
                    startDate, endDate, categoryIds, depotIds, productIds, transactionTypes);

            return movementList;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map> getDepotListFiltered(Long companyId, List salesOfficerList) {

        List<Map> depotList = depotRepository.getDepotListFiltered(
                companyId, salesOfficerList);

        return depotList;

    }

    public Map getUserQaAssignedStatus(Long companyId, Long userId, Long depotId) {
        Map assigenedMap = new HashMap<>();
        List<Map> qaMap = depotRepository.findUserQaAssignedStatus(
                userId, depotId);

        if (qaMap.size() !=0) {
            assigenedMap.put("qaAssigned", true);
        }
        else {
            assigenedMap.put("qaAssigned", false);
        }
        return assigenedMap;
    }

    public Map getUserDepotInchargeAssignedStatus(Long companyId, Long userId, Long depotId) {
        Map assigenedMap = new HashMap<>();
        List<Map> depotInchargeMap = depotRepository.findUserDepotInchargeAssignedStatus(
                userId, depotId);

        if (depotInchargeMap.size() !=0) {
            assigenedMap.put("depotInchargeAssigned", true);
        }
        else {
            assigenedMap.put("depotInchargeAssigned", false);
        }
        return assigenedMap;
    }

    public boolean exitsByIdAndIsCentralWarehouse(Long depotId){
        return depotRepository.existsByIdAndIsCentralWarehouseTrueAndIsDeletedFalse(depotId);
    }
}
