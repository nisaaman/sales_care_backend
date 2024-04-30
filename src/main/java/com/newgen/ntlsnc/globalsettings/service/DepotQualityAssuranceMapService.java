package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.globalsettings.dto.DepotQualityAssuranceMapDto;
import com.newgen.ntlsnc.globalsettings.entity.Depot;
import com.newgen.ntlsnc.globalsettings.entity.DepotLocationMap;
import com.newgen.ntlsnc.globalsettings.entity.DepotQualityAssuranceMap;
import com.newgen.ntlsnc.globalsettings.repository.DepotQualityAssuranceMapRepository;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DepotQualityAssuranceMapService {
    @Autowired
    DepotQualityAssuranceMapRepository depotQualityAssuranceMapRepository;

    @Autowired
    OrganizationService organizationService;

    @Autowired
    ApplicationUserService applicationUserService;

    @Transactional
    public List<Map<String,Object>> findQAByDepot(Long id) {
        try {
            List<Map<String,Object>> depotQualityAssuranceMapList = depotQualityAssuranceMapRepository.getQAByDepot(id);

            return depotQualityAssuranceMapList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    @Transactional
    public List<DepotQualityAssuranceMap> saveQAByDepot(List<DepotQualityAssuranceMapDto> depotQualityAssuranceMapDtoList, Depot depot ) {
        try {
            List<DepotQualityAssuranceMap> depotQualityAssuranceMapList = new ArrayList<>();
                for (DepotQualityAssuranceMapDto depotQualityAssuranceMapDto : depotQualityAssuranceMapDtoList) {
                    DepotQualityAssuranceMap depotQualityAssuranceMap = new DepotQualityAssuranceMap();
                    depotQualityAssuranceMap.setFromDate(LocalDate.now());
                    depotQualityAssuranceMap.setDepot(depot);
                    depotQualityAssuranceMap.setOrganization(organizationService.getOrganizationFromLoginUser());
                    depotQualityAssuranceMap.setQa(applicationUserService.getUserById(depotQualityAssuranceMapDto.getDepotInchargeId()));
                    depotQualityAssuranceMapRepository.save(depotQualityAssuranceMap);
                    depotQualityAssuranceMapList.add(depotQualityAssuranceMap);
                }
            return depotQualityAssuranceMapList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public List<DepotQualityAssuranceMap> updateQAByDepot(List<DepotQualityAssuranceMapDto> depotQualityAssuranceMapDtoList, Depot depot ) {
        try {
            List<DepotQualityAssuranceMap> depotQualityAssuranceMapList = new ArrayList<>();
            List<DepotQualityAssuranceMap> depotQualityAssuranceMapUpdatedList = new ArrayList<>();
            for (DepotQualityAssuranceMapDto depotQualityAssuranceMapDto : depotQualityAssuranceMapDtoList) {
                DepotQualityAssuranceMap depotQualityAssuranceMapNew = new DepotQualityAssuranceMap();
                DepotQualityAssuranceMap depotQualityAssuranceMap =
                        depotQualityAssuranceMapRepository.findByQaIdAndIsActiveTrueAndIsDeletedFalse(
                                depotQualityAssuranceMapDto.getDepotInchargeId());
                if (depotQualityAssuranceMap != null) {
                    depotQualityAssuranceMapNew = depotQualityAssuranceMap;
                } else {
                    depotQualityAssuranceMapNew.setFromDate(LocalDate.now());
                }
                depotQualityAssuranceMapNew.setDepot(depot);
                depotQualityAssuranceMapNew.setOrganization(organizationService.getOrganizationFromLoginUser());
                depotQualityAssuranceMapNew.setQa(applicationUserService.getUserById(depotQualityAssuranceMapDto.getDepotInchargeId()));
                depotQualityAssuranceMapRepository.save(depotQualityAssuranceMapNew);
                depotQualityAssuranceMapUpdatedList.add(depotQualityAssuranceMapNew);
            }
            List<DepotQualityAssuranceMap> qualityAssuranceByDepotList = depotQualityAssuranceMapRepository.findAllByDepotAndToDateIsNullAndIsActiveTrueAndIsDeletedFalse(depot);
            List<DepotQualityAssuranceMap> qualityAssuranceToDateUpdateList = qualityAssuranceByDepotList.stream().filter(it -> !depotQualityAssuranceMapUpdatedList.contains(it)).collect(Collectors.toList());
            if (qualityAssuranceToDateUpdateList.size() > 0) {
                for (DepotQualityAssuranceMap depotQAMap : qualityAssuranceToDateUpdateList) {
                    depotQAMap.setToDate(LocalDate.now());
                    depotQualityAssuranceMapRepository.save(depotQAMap);
                }
            }
            return depotQualityAssuranceMapList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map> getAllQADepotOrQADepotByQAId(Long qaId) {
        Long organizationId = organizationService.getOrganizationIdFromLoginUser();
        List<Map> depotQAMap = depotQualityAssuranceMapRepository.getAllQADepotsOrDepotByQA(
                qaId,organizationId);
        return depotQAMap.size() == 0 ? depotQualityAssuranceMapRepository.getAllQADepotsOrDepotByQA(
                null,organizationId) : depotQAMap;
    }

    public Map getQADepotByQAId(Long qaId) {
        Long organizationId = organizationService.getOrganizationIdFromLoginUser();
        List<Map> depotQAMap = depotQualityAssuranceMapRepository.getAllQADepotsOrDepotByQA(
                qaId,organizationId);
        return depotQAMap.size() != 0 ? depotQAMap.get(0) : new HashMap<>();

    }
}
