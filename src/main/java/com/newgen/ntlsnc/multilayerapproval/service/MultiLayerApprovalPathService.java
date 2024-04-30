package com.newgen.ntlsnc.multilayerapproval.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.ApprovalActor;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.multilayerapproval.dto.ApprovalStepDto;
import com.newgen.ntlsnc.multilayerapproval.dto.MultiLayerApprovalPathDto;
import com.newgen.ntlsnc.multilayerapproval.entity.ApprovalStep;
import com.newgen.ntlsnc.multilayerapproval.entity.ApprovalStepFeatureMap;
import com.newgen.ntlsnc.multilayerapproval.entity.MultiLayerApprovalPath;
import com.newgen.ntlsnc.multilayerapproval.repository.MultiLayerApprovalPathRepository;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

/**
 * @author sagor
 * @date 9/12/22
 * @time 12:40 AM
 */
@Service
public class MultiLayerApprovalPathService implements IService<MultiLayerApprovalPath> {

    @Autowired
    MultiLayerApprovalPathRepository multiLayerApprovalPathRepository;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    ApprovalStepFeatureMapService approvalStepFeatureMapService;

    @Autowired
    ApplicationUserService applicationUserService;

    @Override
    @Transactional
    public MultiLayerApprovalPath create(Object object) {

        MultiLayerApprovalPathDto multiLayerApprovalPathDto = (MultiLayerApprovalPathDto) object;
        MultiLayerApprovalPath multiLayerApprovalPath = new MultiLayerApprovalPath();

        Organization company= organizationService.findByIdAndIsDeletedFalseAndActiveTrue(multiLayerApprovalPathDto.getCompanyId());
        multiLayerApprovalPath.setCompany(company);

        ApprovalStepFeatureMap approvalStepFeatureMap = approvalStepFeatureMapService.findById(multiLayerApprovalPathDto.getApprovalStepFeatureMapId());
        multiLayerApprovalPath.setApprovalStepFeatureMap(approvalStepFeatureMap);

        multiLayerApprovalPath.setApprovalActor(ApprovalActor.valueOf(multiLayerApprovalPathDto.getApprovalActor()));

        if(multiLayerApprovalPathDto.getApprovalActorId() != null){
            multiLayerApprovalPath.setApprovalActorId(multiLayerApprovalPathDto.getApprovalActorId());
        }


        multiLayerApprovalPath.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (!this.validate(multiLayerApprovalPath)) {
            return null;
        }

        return multiLayerApprovalPathRepository.save(multiLayerApprovalPath);
    }

    @Override
    @Transactional
    public MultiLayerApprovalPath update(Long id, Object object) {

        MultiLayerApprovalPathDto multiLayerApprovalPathDto = (MultiLayerApprovalPathDto) object;
        MultiLayerApprovalPath multiLayerApprovalPath = this.findById(id);

        Organization company= organizationService.findByIdAndIsDeletedFalseAndActiveTrue(multiLayerApprovalPathDto.getCompanyId());
        multiLayerApprovalPath.setCompany(company);

        ApprovalStepFeatureMap approvalStepFeatureMap = approvalStepFeatureMapService.findById(multiLayerApprovalPathDto.getApprovalStepFeatureMapId());
        multiLayerApprovalPath.setApprovalStepFeatureMap(approvalStepFeatureMap);

        multiLayerApprovalPath.setApprovalActor(ApprovalActor.valueOf(multiLayerApprovalPathDto.getApprovalActor()));

        if(multiLayerApprovalPathDto.getApprovalActorId() != null){
            multiLayerApprovalPath.setApprovalActorId(multiLayerApprovalPathDto.getApprovalActorId());
        }

        multiLayerApprovalPath.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (!this.validate(multiLayerApprovalPath)) {
            return null;
        }

        return multiLayerApprovalPathRepository.save(multiLayerApprovalPath);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            Optional<MultiLayerApprovalPath> multiLayerApprovalPath = multiLayerApprovalPathRepository.findById(id);
            if (!multiLayerApprovalPath.isPresent()) {
                throw new Exception("MultiLayer Approval Path not exist.");
            }

            multiLayerApprovalPath.get().setIsDeleted(true);
            multiLayerApprovalPathRepository.save(multiLayerApprovalPath.get());
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public MultiLayerApprovalPath findById(Long id) {
        try {
            Optional<MultiLayerApprovalPath> optionalMultiLayerApprovalPath = multiLayerApprovalPathRepository.findByIdAndIsDeletedFalse(id);
            if (!optionalMultiLayerApprovalPath.isPresent()) {
                throw new Exception("MultiLayer Approval Path Not exist with id " + id);
            }
            return optionalMultiLayerApprovalPath.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<MultiLayerApprovalPath> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        List<MultiLayerApprovalPath> multiLayerApprovalPathList = multiLayerApprovalPathRepository.findAllByOrganizationAndIsDeletedFalse(organization);
        multiLayerApprovalPathList.forEach(multiLayerApprovalPath -> multiLayerApprovalPath.setApprovalFeatureName(multiLayerApprovalPath.getApprovalStepFeatureMap().getApprovalFeature().getName()));
        return multiLayerApprovalPathList;
    }

    @Override
    public boolean validate(Object object) {
        MultiLayerApprovalPath multiLayerApprovalPath = (MultiLayerApprovalPath) object;
        Optional<MultiLayerApprovalPath> optionalMultiLayerApprovalPath = Optional.empty();

        if(multiLayerApprovalPath.getId() == null) {
            optionalMultiLayerApprovalPath = multiLayerApprovalPathRepository.findByOrganizationAndCompanyIdAndApprovalStepFeatureMapIdAndIsDeletedFalse(
                    multiLayerApprovalPath.getOrganization(), multiLayerApprovalPath.getCompany().getId(),multiLayerApprovalPath.getApprovalStepFeatureMap().getId());

        } else {
            optionalMultiLayerApprovalPath = multiLayerApprovalPathRepository.findByOrganizationAndIdIsNotAndCompanyIdAndApprovalStepFeatureMapIdAndIsDeletedFalse(
                    multiLayerApprovalPath.getOrganization(),multiLayerApprovalPath.getId(), multiLayerApprovalPath.getCompany().getId(),multiLayerApprovalPath.getApprovalStepFeatureMap().getId());
        }

        if(optionalMultiLayerApprovalPath.isPresent()){
            throw new RuntimeException("MultiLayer Approval Path already exist.");
        }

        return true;
    }

    public List<Map<String,Object>> getMultiLayerApprovalPathList(Long companyId, String approvalFeature){
        return multiLayerApprovalPathRepository.getApprovalListByFeature(companyId, approvalFeature);
    }

    public List<Map<String,Object>> getMultiLayerApprovalPathByLevel(Long companyId, String approvalFeature, Integer level) {
        return multiLayerApprovalPathRepository.getApprovalListByFeatureAndLevel(companyId, approvalFeature, level);
    }

    public List<Map<String, Object>> getApprovalActorList(Long companyId, String approvalFeature){
        List<Map<String, Object>> approvalActorList = new ArrayList<>();
        List<Map<String,Object>> approvalPathList = getMultiLayerApprovalPathList(companyId,approvalFeature);
        Long applicationUserId = applicationUserService.getApplicationUserIdFromLoginUser();
        for (Map<String, Object> approvalPath: approvalPathList) {
            String approvalActor = approvalPath.get("approvalActor").toString();
            Long multiLayerApprovalPathId = Long.parseLong(approvalPath.get("multiLayerApprovalPathId").toString());
            List<Map<String, Object>> approvalList = new ArrayList<>();
            switch (ApprovalActor.valueOf(approvalActor)){
                case ROLE:
                    approvalList.addAll(multiLayerApprovalPathRepository.getApprovalListByRole(companyId,approvalFeature,approvalActor,applicationUserId, multiLayerApprovalPathId));
                    break;
                case DESIGNATION:
                    approvalList.addAll(multiLayerApprovalPathRepository.getApprovalListByDesignation(companyId,approvalFeature,approvalActor,applicationUserId, multiLayerApprovalPathId));
                    break;
                case LOCATION_TYPE:
                    approvalList.addAll(multiLayerApprovalPathRepository.getApprovalListByLocationType(companyId,approvalFeature,approvalActor,applicationUserId, multiLayerApprovalPathId));
                    break;
                case DEPOT_IN_CHARGE:
                    approvalList.addAll(multiLayerApprovalPathRepository.getApprovalListByDepotInCharge(companyId,approvalFeature,approvalActor,applicationUserId, multiLayerApprovalPathId));
                    break;
                case APPLICATION_USER:
                    approvalList.addAll(multiLayerApprovalPathRepository.getApprovalListByApplicationUser(companyId,approvalFeature,approvalActor,applicationUserId, multiLayerApprovalPathId));
                    break;
                default:
                    break;
            }
            approvalActorList.addAll(approvalList);
        }
        return approvalActorList;
    }

    public MultiLayerApprovalPath getByApprovalStepFeatureMapId(Long approvalStepFeatureMapId) {
        return multiLayerApprovalPathRepository.findByApprovalStepFeatureMapIdAndIsDeletedFalse(approvalStepFeatureMapId);
    }

    public Integer getLastLevelApprovalPath(Long companyId,String approvalFeature){
        return multiLayerApprovalPathRepository.getLastLevelApprovalPath(companyId,approvalFeature);
    }
}
