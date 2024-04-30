package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.globalsettings.entity.Depot;
import com.newgen.ntlsnc.globalsettings.entity.DepotLocationMap;
import com.newgen.ntlsnc.globalsettings.repository.DepotLocationMapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author kamal
 * @Date ১৮/৫/২২
 */

@Service
public class DepotLocationMapService {
    @Autowired
    DepotLocationMapRepository depotLocationMapRepository;

    @Transactional
    public List<DepotLocationMap> saveAllDepotLocationMap(List<DepotLocationMap> depotLocationMaps) {
        try {
            List<DepotLocationMap> depotLocationMapList = depotLocationMapRepository.saveAll(depotLocationMaps);

            return depotLocationMapList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public Boolean deleteAllDepotLocationMapByDepotId(Long depotId) {
        try {
            depotLocationMapRepository.deleteAllByDepotId(depotId);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map<String, Object>> getAllLocationAndCompanyByDepotId(Long depotId) {
        try {
            return depotLocationMapRepository.getLocationAndCompanyByDepotId(depotId);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Map getLocationOfDepot(Long companyId, Long depotId) {
        return  depotLocationMapRepository.getLocationOfDepotByCompanyAndDepot(companyId,depotId);
    }

    public List<Map> getLocationListOfDepot(Long companyId, Long depotId) {
        return  depotLocationMapRepository.getLocationListOfDepotByCompanyAndDepot(companyId,depotId);
    }

    public List<Long> getLocationWithoutDepotLocationMap(Long companyId){
        return depotLocationMapRepository.findAllByCompanyIdAndIsActiveTrueAndIsDeletedFalse(companyId);
    }

    public Depot getCentalDepotByCompnayId(Long companyId){
        List<DepotLocationMap> depotLocationMapList =  depotLocationMapRepository.findAllByCompanyId(companyId);
        List<Depot> depotList = depotLocationMapList.stream().map(DepotLocationMap::getDepot).filter(depot -> depot.getIsCentralWarehouse().equals(true)).collect(Collectors.toList());
        return depotList.size() > 0 ? depotList.get(0) : null;
    }


}
