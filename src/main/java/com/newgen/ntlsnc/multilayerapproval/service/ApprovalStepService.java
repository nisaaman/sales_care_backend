package com.newgen.ntlsnc.multilayerapproval.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.multilayerapproval.dto.ApprovalStepDto;
import com.newgen.ntlsnc.multilayerapproval.entity.ApprovalStep;
import com.newgen.ntlsnc.multilayerapproval.entity.ApprovalStepFeatureMap;
import com.newgen.ntlsnc.multilayerapproval.repository.ApprovalStepRepository;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author nisa
 * @date 9/11/22
 * @time 10:38 AM
 */
@Service
public class ApprovalStepService implements IService<ApprovalStep> {

    @Autowired
    ApprovalStepRepository approvalStepRepository;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    ApprovalStepFeatureMapService approvalStepFeatureMapService;

    @Override
    @Transactional
    public ApprovalStep create(Object object) {
        ApprovalStepDto approvalStepDto = (ApprovalStepDto) object;
        ApprovalStep approvalStep = new ApprovalStep();
        approvalStep.setName(approvalStepDto.getName());
        approvalStep.setDescription(approvalStepDto.getDescription());
        approvalStep.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (!this.validate(approvalStep)) {
            return null;
        }

        return approvalStepRepository.save(approvalStep);
    }

    @Override
    @Transactional
    public ApprovalStep update(Long id, Object object) {
        ApprovalStepDto approvalStepDto = (ApprovalStepDto) object;
        ApprovalStep approvalStep = this.findById(id);
        approvalStep.setName(approvalStepDto.getName());
        approvalStep.setDescription(approvalStepDto.getDescription());
        approvalStep.setOrganization(organizationService.getOrganizationFromLoginUser());
        if (!this.validate(approvalStep)) {
            return null;
        }
        return approvalStepRepository.save(approvalStep);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            Optional<ApprovalStep> approvalStep = approvalStepRepository.findById(id);
            if (!approvalStep.isPresent()) {
                throw new Exception("Approval Step not exist.");
            }

            List<ApprovalStepFeatureMap> approvalStepBranchList = approvalStepFeatureMapService.findAllApprovalStepFeatureByApprovalStep(approvalStep.get().getOrganization(),approvalStep.get().getId());
            if(approvalStepBranchList.size() > 0){
                throw new RuntimeException("This Approval Step already in use.");
            }

            approvalStep.get().setIsDeleted(true);
            approvalStepRepository.save(approvalStep.get());
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public ApprovalStep findById(Long id) {
        try {
            Optional<ApprovalStep> optionalApprovalStep = approvalStepRepository.findByIdAndIsDeletedFalse(id);
            if (!optionalApprovalStep.isPresent()) {
                throw new Exception("Approval Step Not exist with id " + id);
            }
            return optionalApprovalStep.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<ApprovalStep> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return approvalStepRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Override
    public boolean validate(Object object) {
        ApprovalStep approvalStep = (ApprovalStep) object;
        Optional<ApprovalStep> optionalApprovalStep = Optional.empty();

        if(approvalStep.getId() == null) {
            optionalApprovalStep = approvalStepRepository.findByOrganizationAndNameIgnoreCaseAndIsDeletedFalse(
                    approvalStep.getOrganization(), approvalStep.getName().trim());

        } else {
            optionalApprovalStep = approvalStepRepository.findByOrganizationAndIdIsNotAndNameIgnoreCaseAndIsDeletedFalse(
                    approvalStep.getOrganization(), approvalStep.getId(), approvalStep.getName().trim());

        }

        if(optionalApprovalStep.isPresent()){
            throw new RuntimeException("Approval Step name already exist.");
        }

        return true;
    }
}
