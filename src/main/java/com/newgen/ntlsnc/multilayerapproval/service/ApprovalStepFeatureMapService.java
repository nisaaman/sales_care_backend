package com.newgen.ntlsnc.multilayerapproval.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.ApprovalFeature;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.multilayerapproval.dto.ApprovalStepFeatureMapDto;
import com.newgen.ntlsnc.multilayerapproval.entity.ApprovalStep;
import com.newgen.ntlsnc.multilayerapproval.entity.ApprovalStepFeatureMap;
import com.newgen.ntlsnc.multilayerapproval.entity.MultiLayerApprovalPath;
import com.newgen.ntlsnc.multilayerapproval.repository.ApprovalStepFeatureMapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author nisa
 * @date 9/11/22
 * @time 10:46 AM
 */
@Service
public class ApprovalStepFeatureMapService implements IService<ApprovalStepFeatureMap> {

    @Autowired
    ApprovalStepFeatureMapRepository approvalStepFeatureMapRepository;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    ApprovalStepService approvalStepService;
    @Autowired
    MultiLayerApprovalPathService multiLayerApprovalPathService;

    @Override
    @Transactional
    public ApprovalStepFeatureMap create(Object object) {
        ApprovalStepFeatureMapDto approvalStepFeatureMapDto = (ApprovalStepFeatureMapDto) object;
        ApprovalStepFeatureMap approvalStepFeatureMap = new ApprovalStepFeatureMap();

        approvalStepFeatureMap.setApprovalFeature(ApprovalFeature.valueOf(approvalStepFeatureMapDto.getApprovalFeature()));
        ApprovalStep approvalStep = approvalStepService.findById(approvalStepFeatureMapDto.getApprovalStepId());
        approvalStepFeatureMap.setApprovalStep(approvalStep);
        approvalStepFeatureMap.setLevel(approvalStepFeatureMapDto.getLevel());
        Organization company = organizationService.findByIdAndIsDeletedFalseAndActiveTrue(approvalStepFeatureMapDto.getCompanyId());
        approvalStepFeatureMap.setCompany(company);
        approvalStepFeatureMap.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (!this.validate(approvalStepFeatureMap)) {
            return null;
        }

        return approvalStepFeatureMapRepository.save(approvalStepFeatureMap);
    }

    @Override
    @Transactional
    public ApprovalStepFeatureMap update(Long id, Object object) {
        ApprovalStepFeatureMapDto approvalStepFeatureMapDto = (ApprovalStepFeatureMapDto) object;
        ApprovalStepFeatureMap approvalStepFeatureMap = this.findById(id);
        approvalStepFeatureMap.setOrganization(organizationService.getOrganizationFromLoginUser());
        if (!this.validate(approvalStepFeatureMap)) {
            return null;
        }
        return approvalStepFeatureMapRepository.save(approvalStepFeatureMap);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            Optional<ApprovalStepFeatureMap> approvalStepFeatureMap = approvalStepFeatureMapRepository.findById(id);
            if (!approvalStepFeatureMap.isPresent()) {
                throw new Exception("Approval Step Feature Map not exist.");
            }

//            List<MultiLayerApprovalPath> approvalStepFeatureMapBranchList = approvalStepFeatureMapBranchService.findAllBranchByApprovalStepFeatureMap(approvalStepFeatureMap.get().getId());
//            if(approvalStepFeatureMapBranchList.size() > 0){
//                throw new RuntimeException("This approvalStepFeatureMap already in use.");
//            }

            approvalStepFeatureMap.get().setIsDeleted(true);
            approvalStepFeatureMapRepository.save(approvalStepFeatureMap.get());
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public ApprovalStepFeatureMap findById(Long id) {
        try {
            Optional<ApprovalStepFeatureMap> optionalApprovalStepFeatureMap = approvalStepFeatureMapRepository.findByIdAndIsDeletedFalse(id);
            if (!optionalApprovalStepFeatureMap.isPresent()) {
                throw new Exception("Approval Step Feature Map Not exist with id " + id);
            }
            return optionalApprovalStepFeatureMap.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<ApprovalStepFeatureMap> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return approvalStepFeatureMapRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }

    public List<ApprovalStepFeatureMap> findAllApprovalStepFeatureByApprovalStep(Organization organization, Long approvalStepId) {
        return approvalStepFeatureMapRepository.findAllByOrganizationAndApprovalStepIdAndIsActiveTrueAndIsDeletedFalse(organization, approvalStepId);
    }

    public List<Map<String, String>> getAllApprovalStepFeatureByCompanyId(Long companyId) {

        List<Map<String, String>> finalList = new ArrayList<>();
        List<Map<String, String>> enumList = approvalStepFeatureMapRepository.findAllByCompanyIdAndIsDeletedFalseAndIsActiveTrue(companyId);
        for (Map map : enumList) {
            Map<String, String> newList = new HashMap<>();
            newList.put("code", map.get("approvalFeature").toString());
            newList.put("name", ApprovalFeature.valueOf(ApprovalFeature.class, map.get("approvalFeature").toString()).getName());
            finalList.add(newList);
        }

        return finalList;

    }

    public List<Map<String, String>> getAllApprovalStepFeatureByCompanyIdAndApprovalStepFeature(Long companyId, String approvalStepFeature) {

        return approvalStepFeatureMapRepository.findAllApprovalStepFeatureByCompanyIdAndApprovalStepFeature(companyId, approvalStepFeature);

    }

    @Transactional
    public List<ApprovalStepFeatureMap> createAll(ApprovalStepFeatureMapDto approvalStepFeatureMapDtoWithList) {
        try {
            List<ApprovalStepFeatureMapDto> approvalStepFeatureMapDtoList = approvalStepFeatureMapDtoWithList.getApprovalStepFeatureMapDtoList();
            if (approvalStepFeatureMapDtoList.size() == 0) {
                throw new RuntimeException("Invalid request! No data submitted.");
            }

            Organization organization = organizationService.getOrganizationFromLoginUser();
            Organization company = organizationService.findById(approvalStepFeatureMapDtoWithList.getCompanyId());
            String feature = approvalStepFeatureMapDtoWithList.getApprovalFeature();

            List<ApprovalStepFeatureMap> existingApprovalStepFeatureList = approvalStepFeatureMapRepository.findAllByOrganizationAndCompanyAndApprovalFeatureAndIsDeletedFalseOrderByLevelAsc(organization, company, ApprovalFeature.valueOf(feature));
            if (existingApprovalStepFeatureList.size() > 0) {
                throw new RuntimeException("You can not create another Authorization Feature for " + feature);
            }
            List<ApprovalStepFeatureMap> approvalStepFeatureMapList = new ArrayList<>();

            for (ApprovalStepFeatureMapDto approvalStepFeatureMapDto : approvalStepFeatureMapDtoList) {
                ApprovalStepFeatureMap approvalStepFeatureMap;

                if (approvalStepFeatureMapDto.getId() == null) {

                    approvalStepFeatureMap = new ApprovalStepFeatureMap();

                    approvalStepFeatureMap.setOrganization(organization);
                    approvalStepFeatureMap.setCompany(company);
                    approvalStepFeatureMap.setLevel(approvalStepFeatureMapDto.getLevel());
                    ApprovalStep approvalStep = approvalStepService.findById(approvalStepFeatureMapDto.getApprovalStepId());
                    approvalStepFeatureMap.setApprovalStep(approvalStep);
                    approvalStepFeatureMap.setApprovalFeature(ApprovalFeature.valueOf(feature));
                } else {
                    Optional<ApprovalStepFeatureMap> approvalStepFeatureMapOptional = approvalStepFeatureMapRepository.findById(approvalStepFeatureMapDto.getId());

                    if (approvalStepFeatureMapOptional.isPresent()) {
                        approvalStepFeatureMap = approvalStepFeatureMapOptional.get();
                        approvalStepFeatureMap.setOrganization(organization);
                        approvalStepFeatureMap.setCompany(company);
                        approvalStepFeatureMap.setLevel(approvalStepFeatureMapDto.getLevel());
                        ApprovalStep approvalStep = approvalStepService.findById(approvalStepFeatureMapDto.getApprovalStepId());
                        approvalStepFeatureMap.setApprovalStep(approvalStep);
                        approvalStepFeatureMap.setApprovalFeature(ApprovalFeature.valueOf(feature));
                    } else {
                        throw new RuntimeException("Approval Step Feature Map not found");
                    }
                }

                approvalStepFeatureMapList.add(approvalStepFeatureMap);
            }

            return approvalStepFeatureMapRepository.saveAll(approvalStepFeatureMapList);

        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public List<Map<String, String>> getAllByOrganizationAsPerDesign() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return approvalStepFeatureMapRepository.findAllByOrganizationAsPerDesign(organization.getId());
    }

    public List<ApprovalStepFeatureMap> getAllByCompanyIdAndApprovalFeature(Long companyId, String feature) {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        Organization company = organizationService.findByIdAndIsDeletedFalseAndActiveTrue(companyId);

        return approvalStepFeatureMapRepository.findAllByOrganizationAndCompanyAndApprovalFeatureAndIsDeletedFalseOrderByLevelAsc(organization, company, ApprovalFeature.valueOf(feature));
    }

    @Transactional
    public boolean updateAll(ApprovalStepFeatureMapDto approvalStepFeatureMapDtoWithList) {
        try {
            List<ApprovalStepFeatureMapDto> approvalStepFeatureMapDtoList = approvalStepFeatureMapDtoWithList.getApprovalStepFeatureMapDtoList();

            if (approvalStepFeatureMapDtoList.size() == 0) {
                throw new RuntimeException("Invalid request! No data submitted.");
            }

            Organization organization = organizationService.getOrganizationFromLoginUser();
            Organization company = organizationService.findById(approvalStepFeatureMapDtoWithList.getCompanyId());
            String feature = approvalStepFeatureMapDtoWithList.getApprovalFeature();

            List<ApprovalStepFeatureMap> ApprovalStepFeatureMapUpdatedList = new ArrayList<>();

            for (ApprovalStepFeatureMapDto approvalStepFeatureMapDto : approvalStepFeatureMapDtoList) {
                ApprovalStepFeatureMap approvalStepFeatureMap = new ApprovalStepFeatureMap();

                ApprovalStepFeatureMap approvalStepFeatureMap1= approvalStepFeatureMapRepository.findByOrganizationAndCompanyAndApprovalFeatureAndApprovalStepIdAndIsDeletedFalse(organization,company,ApprovalFeature.valueOf(feature),approvalStepFeatureMapDto.getApprovalStepId());
                if(approvalStepFeatureMap1 != null){
                    approvalStepFeatureMap=approvalStepFeatureMap1;
                }
                approvalStepFeatureMap.setLevel(approvalStepFeatureMapDto.getLevel());
                ApprovalStep approvalStep = approvalStepService.findById(approvalStepFeatureMapDto.getApprovalStepId());
                approvalStepFeatureMap.setApprovalStep(approvalStep);
                approvalStepFeatureMap.setApprovalFeature(ApprovalFeature.valueOf(feature));
                approvalStepFeatureMap.setOrganization(organization);
                approvalStepFeatureMap.setCompany(company);

                approvalStepFeatureMapRepository.save(approvalStepFeatureMap);
                ApprovalStepFeatureMapUpdatedList.add(approvalStepFeatureMap);
            }

            List<ApprovalStepFeatureMap> existingApprovalStepFeatureList = approvalStepFeatureMapRepository.findAllByOrganizationAndCompanyAndApprovalFeatureAndIsDeletedFalseOrderByLevelAsc(organization, company, ApprovalFeature.valueOf(feature));
            List<ApprovalStepFeatureMap> approvalStepFeatureMapDeletedList = existingApprovalStepFeatureList.stream().filter(it -> !ApprovalStepFeatureMapUpdatedList.contains(it)).collect(Collectors.toList());

            if (approvalStepFeatureMapDeletedList.size() > 0) {
                for (ApprovalStepFeatureMap asfm : approvalStepFeatureMapDeletedList) {
                    MultiLayerApprovalPath  multiLayerApprovalPath=multiLayerApprovalPathService.getByApprovalStepFeatureMapId(asfm.getId());
                    if(multiLayerApprovalPath != null){
                        throw new RuntimeException("Approval Step "+ asfm.getApprovalStep().getName()+ " already used !!!");
                    }
                    asfm.setIsDeleted(true);
                    approvalStepFeatureMapRepository.save(asfm);
                }
            }

            return true;

        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
}
