package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.entity.Location;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.service.LocationManagerMapService;
import com.newgen.ntlsnc.globalsettings.service.LocationService;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.salesandcollection.dto.DistributorSalesOfficerMapDto;
import com.newgen.ntlsnc.salesandcollection.entity.Distributor;
import com.newgen.ntlsnc.salesandcollection.entity.DistributorSalesOfficerMap;
import com.newgen.ntlsnc.salesandcollection.repository.DistributorSalesOfficerMapRepository;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author anika
 * @Date ২১/৬/২২
 */
@Service
public class DistributorSalesOfficerMapService implements IService<DistributorSalesOfficerMap> {
    @Autowired
    DistributorService distributorService;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    DistributorSalesOfficerMapRepository distributorSalesOfficerMapRepository;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    LocationManagerMapService locationManagerMapService;

    @Autowired
    LocationService locationService;

    @Override
    public DistributorSalesOfficerMap create(Object object) {

        DistributorSalesOfficerMapDto distributorSalesOfficerMapDto = (DistributorSalesOfficerMapDto) object;
        DistributorSalesOfficerMap distributorSalesOfficerMap = new DistributorSalesOfficerMap();

        distributorSalesOfficerMap.setFromDate(LocalDate.parse(distributorSalesOfficerMapDto.getFromDate()));//yyyy-MM-dd
        distributorSalesOfficerMap.setToDate(LocalDate.parse(distributorSalesOfficerMapDto.getToDate()));//yyyy-MM-dd
//        distributorSalesOfficerMap.setOrganization(organizationService.findById(distributorSalesOfficerMapDto.getOrganizationId()));
//           if(distributorSalesOfficerMapDto.getDistributorId()!= null){
//                Distributor distributor = distributorService.findById(distributorSalesOfficerMapDto.getDistributorId());
//                distributorSalesOfficerMap.setDistributor(distributor);
//            }

        if (distributorSalesOfficerMapDto.getSalesOfficerId() != null) {
            ApplicationUser applicationUser = applicationUserService.findById(distributorSalesOfficerMapDto.getSalesOfficerId());
            distributorSalesOfficerMap.setSalesOfficer(applicationUser);
        }

        return distributorSalesOfficerMapRepository.save(distributorSalesOfficerMap);
    }

    @Override
    public DistributorSalesOfficerMap update(Long id, Object object) {

        DistributorSalesOfficerMapDto distributorSalesOfficerMapDto = (DistributorSalesOfficerMapDto) object;
        DistributorSalesOfficerMap distributorSalesOfficerMap = this.findById(distributorSalesOfficerMapDto.getId());

        distributorSalesOfficerMap.setFromDate(LocalDate.parse(distributorSalesOfficerMapDto.getFromDate()));//yyyy-MM-dd
        distributorSalesOfficerMap.setToDate(LocalDate.parse(distributorSalesOfficerMapDto.getToDate()));//yyyy-MM-dd
//        distributorSalesOfficerMap.setOrganization(organizationService.findById(distributorSalesOfficerMapDto.getOrganizationId()));
//
//        if(distributorSalesOfficerMapDto.getDistributorId()!= null){
//            Distributor distributor = distributorService.findById(distributorSalesOfficerMapDto.getDistributorId());
//            distributorSalesOfficerMap.setDistributor(distributor);
//        }

        if (distributorSalesOfficerMapDto.getSalesOfficerId() != null) {
            ApplicationUser applicationUser = applicationUserService.findById(distributorSalesOfficerMapDto.getSalesOfficerId());
            distributorSalesOfficerMap.setSalesOfficer(applicationUser);
        }

        return distributorSalesOfficerMapRepository.save(distributorSalesOfficerMap);

    }

    @Override
    public boolean delete(Long id) {
        try {
            Optional<DistributorSalesOfficerMap> optionalDistributorSalesOfficerMap = distributorSalesOfficerMapRepository.findById(id);
            if (!optionalDistributorSalesOfficerMap.isPresent()) {
                throw new Exception("Distributor Sales Officer Map is Not exist");
            }
            DistributorSalesOfficerMap distributorSalesOfficerMap = optionalDistributorSalesOfficerMap.get();
            distributorSalesOfficerMap.setIsDeleted(true);
            distributorSalesOfficerMapRepository.save(distributorSalesOfficerMap);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    public DistributorSalesOfficerMap findById(Long id) {

        try {
            Optional<DistributorSalesOfficerMap> optionalDistributorSalesOfficerMap = distributorSalesOfficerMapRepository.findById(id);
            if (!optionalDistributorSalesOfficerMap.isPresent()) {
                throw new Exception("Distributor Sales Officer Map Not exist with id " + id);
            }
            return optionalDistributorSalesOfficerMap.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<DistributorSalesOfficerMap> findAll() {

        Organization organization = new Organization();
        return distributorSalesOfficerMapRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }


    public List<DistributorSalesOfficerMap> findAllBySalesOfficer(List<ApplicationUser> salesOfficerList) {
        return distributorSalesOfficerMapRepository.findAllBySalesOfficerInAndIsActiveTrueAndIsDeletedFalse(salesOfficerList);
    }

    public List<DistributorSalesOfficerMap> findAllByCompanyIdAndSalesOfficer(Long companyId, List<ApplicationUser> salesOfficerList) {
        return distributorSalesOfficerMapRepository.findAllByCompanyIdAndSalesOfficerInAndIsActiveTrueAndIsDeletedFalse(companyId, salesOfficerList);
    }


    public boolean saveAll(List<DistributorSalesOfficerMapDto> distributorSalesOfficerMapDtoList, Distributor distributor) {
        try {

            List<DistributorSalesOfficerMap> distributorSalesOfficerMapList = new ArrayList<>();
            for (DistributorSalesOfficerMapDto dsom : distributorSalesOfficerMapDtoList) {
                DistributorSalesOfficerMap distributorSalesOfficerMap = new DistributorSalesOfficerMap();
                distributorSalesOfficerMap.setFromDate(LocalDate.now());//yyyy-MM-dd
                distributorSalesOfficerMap.setCompany(organizationService.findById(dsom.getCompanyId()));
                distributorSalesOfficerMap.setOrganization(organizationService.getOrganizationFromLoginUser());
                distributorSalesOfficerMap.setDistributor(distributor);

                if (dsom.getSalesOfficerId() != null) {
                    ApplicationUser applicationUser = applicationUserService.findById(dsom.getSalesOfficerId());
                    distributorSalesOfficerMap.setSalesOfficer(applicationUser);
                }
                if (!this.validate(distributorSalesOfficerMap)) {
                    return false;
                }
                distributorSalesOfficerMapList.add(distributorSalesOfficerMap);

                DistributorSalesOfficerMap distributorSalesOfficerMapOld = new DistributorSalesOfficerMap();
                if (dsom.getId() != null) {
                    distributorSalesOfficerMapOld = this.findById(dsom.getId());
                    distributorSalesOfficerMapOld.setToDate(LocalDate.now());//yyyy-MM-dd
                    distributorSalesOfficerMapList.add(distributorSalesOfficerMapOld);
                }
            }

            /*List<DistributorSalesOfficerMap> distributorSalesOfficerMapListByDistributor =
                    distributorSalesOfficerMapRepository.findAllByDistributorAndIsActiveTrueAndIsDeletedFalse(distributor);
            List<DistributorSalesOfficerMap> distributorSalesOfficerMapDeleteList =
                    distributorSalesOfficerMapListByDistributor.stream().filter(it ->
                            !distributorSalesOfficerMapList.contains(it)).collect(Collectors.toList());
                for (DistributorSalesOfficerMap dsom : distributorSalesOfficerMapListByDistributor) {
                    dsom.setIsActive(false);
                    dsom.setIsDeleted(true);
                    distributorSalesOfficerMapRepository.save(dsom);
                }*/

            distributorSalesOfficerMapRepository.saveAll(distributorSalesOfficerMapList);

            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<DistributorSalesOfficerMapDto> getDistributorInfoWithSalesOfficerByDistributor(Distributor distributor) {
        List<DistributorSalesOfficerMap> distributorSalesOfficerMapList =
                distributorSalesOfficerMapRepository.findAllByDistributorAndToDateIsNullAndIsActiveTrueAndIsDeletedFalse(
                        distributor);
        List<DistributorSalesOfficerMapDto> distributorSalesOfficerMapDtoList = new ArrayList<>();

        for (DistributorSalesOfficerMap distributorSalesOfficerMap : distributorSalesOfficerMapList) {

            DistributorSalesOfficerMapDto distributorSalesOfficerMapDto = new DistributorSalesOfficerMapDto();

            distributorSalesOfficerMapDto.setId(distributorSalesOfficerMap.getId());
            distributorSalesOfficerMapDto.setCompanyId(distributorSalesOfficerMap.getCompany().getId());
            distributorSalesOfficerMapDto.setSalesOfficerId(distributorSalesOfficerMap.getSalesOfficer().getId());
            distributorSalesOfficerMapDtoList.add(distributorSalesOfficerMapDto);
            Map location = locationManagerMapService.getSalesOfficerLocation(distributorSalesOfficerMapDto.getSalesOfficerId(), distributor.getOrganization().getId(), distributorSalesOfficerMapDto.getCompanyId());
            distributorSalesOfficerMapDto.setLocationId(Long.parseLong(location.get("id").toString()));
            List<Location> locationArray = locationService.findAllTerritoryByCompanyId(distributorSalesOfficerMap.getCompany().getId());
            distributorSalesOfficerMapDto.setLocationArray(locationArray);
            List<Map<String, Object>> soArray = applicationUserService.getSoDetailsByLocation(Long.parseLong(location.get("id").toString()), distributorSalesOfficerMap.getCompany().getId());
            distributorSalesOfficerMapDto.setSoArray(soArray);
        }
        return distributorSalesOfficerMapDtoList;
    }

    public boolean findBySalesOfficerId(Long id) {
        List<DistributorSalesOfficerMap> distributorSalesOfficerMapList = distributorSalesOfficerMapRepository.findBySalesOfficerIdAndIsActiveTrueAndIsDeletedFalse(id);
        if (distributorSalesOfficerMapList.size() == 0) {
            return false;
        } else
            return true;
    }
}
