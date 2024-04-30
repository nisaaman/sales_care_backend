package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.CommonConstant;
import com.newgen.ntlsnc.common.FileUploadService;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.common.enums.BudgetType;
import com.newgen.ntlsnc.globalsettings.service.AccountingYearService;
import com.newgen.ntlsnc.globalsettings.service.FileStorageService;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.salesandcollection.dto.CollectionBudgetUploadDto;
import com.newgen.ntlsnc.salesandcollection.entity.CollectionBudget;
import com.newgen.ntlsnc.salesandcollection.repository.CollectionBudgetDetailsRepository;
import com.newgen.ntlsnc.salesandcollection.repository.CollectionBudgetRepository;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.extensions.excel.ExcelFileParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;

/**
 * @author Newaz Sharif
 * @since 20th August,22
 */

@Service
public class CollectionBudgetService {

    @Autowired
    CollectionBudgetOverViewService collectionBudgetOverViewService;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    FileUploadService fileUploadService;
    @Autowired
    FileStorageService fileStorageService;
    @Autowired
    CollectionBudgetRepository collectionBudgetRepository;
    @Autowired
    AccountingYearService accountingYearService;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    CollectionBudgetDetailsRepository collectionBudgetDetailsRepository;
    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    @Qualifier("collectionBudget")
    private Job job;

    public String distributorWiseCollectionBudgetDataUpload(
            CollectionBudgetUploadDto collectionBudgetUploadDto) {

        try {

            Long uploadedCollectionBudgetId = isCollectionBudgetAlreadyUploadedForThisAccountingYear(
                    collectionBudgetUploadDto.getCompanyId(),
                                collectionBudgetUploadDto.getAccountingYearId());

            if(uploadedCollectionBudgetId != null) {
                deletePreviouslyUploadedData(uploadedCollectionBudgetId,
                        collectionBudgetUploadDto.getCompanyId()
                        ,collectionBudgetUploadDto.getAccountingYearId());
            }

            MultipartFile collectionBudgetFile = collectionBudgetUploadDto.getCollectionBudgetFile();
            String rootPath = CommonConstant.COLLECTION_BUDGET_FILE_UPLOAD_PATH;
            String filePath = new ClassPathResource(rootPath).getPath();
            String fileName = fileUploadService.getFileNameWithoutExtension(
                    collectionBudgetFile.getOriginalFilename())
                    + "." +fileUploadService.getFileExtension(collectionBudgetFile.getOriginalFilename());
            fileStorageService.uploadInResourceDirectory(collectionBudgetUploadDto.getCollectionBudgetFile(),
                    filePath, fileName);

            CollectionBudget collectionBudget = create(collectionBudgetUploadDto);

            JobParametersBuilder jobParameters = new JobParametersBuilder();
            jobParameters.addLong("collectionBudgetId", collectionBudget.getId());
            jobParameters.addString("fileName", fileName);

            try {
                JobExecution jobExecution = jobLauncher.run(job, jobParameters.toJobParameters());

                if(String.valueOf(BatchStatus.FAILED).equalsIgnoreCase(String.valueOf(jobExecution.getStatus()))) {
                    collectionBudgetRepository.delete(collectionBudget);
                    return "Invalid Excel File. Please Download File then Upload";
                }

            } catch (JobExecutionAlreadyRunningException | JobRestartException
                    | JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
                e.printStackTrace();
                return "Job Already Running or Invalid Job Parameter";
            } catch (ExcelFileParseException efpe) {
                return "Invalid Excel File. Please Download File then Upload";
            } finally {
                fileStorageService.deleteFileFromDirectory(filePath,fileName);
            }

        } catch (RuntimeException re) {
            throw new RuntimeException(re.getMessage());
        }
        return "success";
    }

    private CollectionBudget create(CollectionBudgetUploadDto collectionBudgetUploadDto) {
        CollectionBudget collectionBudget = new CollectionBudget();
        collectionBudget.setBudgetDate(accountingYearService.getAccountingYearDate(
                collectionBudgetUploadDto.getAccountingYearId()
        ).get("startDate"));
        collectionBudget.setCompany(organizationService.findById(collectionBudgetUploadDto.getCompanyId()));
        collectionBudget.setTargetType(BudgetType.DISTRIBUTOR);
        collectionBudget.setOrganization(organizationService.findById(collectionBudgetUploadDto.getCompanyId()));
        collectionBudget.setAccountingYear(accountingYearService.findById(
                collectionBudgetUploadDto.getAccountingYearId()));
        collectionBudget.setApprovalStatus(ApprovalStatus.APPROVED);//TODO for the time being
        collectionBudgetRepository.save(collectionBudget);
        return collectionBudget;
    }

    public Long isCollectionBudgetAlreadyUploadedForThisAccountingYear(
            Long companyId, Long accountingYearId) {

        Optional<CollectionBudget> collectionBudget = collectionBudgetRepository.
                findByCompanyIdAndAccountingYearIdAndIsDeletedFalseAndIsActiveTrue(
                            companyId, accountingYearId);

        return collectionBudget.isPresent() ? collectionBudget.get().getId(): null;
    }


    private void deletePreviouslyUploadedData(
            Long uploadedSalesBudgetId, Long companyId, Long accountingYearId){

        collectionBudgetDetailsRepository.deleteByCollectionBudgetId(uploadedSalesBudgetId);
        collectionBudgetRepository.deleteByCompanyIdAndAccountingYearId(
                companyId,accountingYearId);

    }

    public Map<String, Double> getSalesOfficerOrManagerCollectionTargetVSAchievment(Long salesOfficerId, Long companyId, Long accountingYearId, Integer month) {
        if (salesOfficerId == null) {
            salesOfficerId = applicationUserService.getApplicationUserIdFromLoginUser();
        }
        Long finalSalesOfficerId = salesOfficerId;
        HashMap<String, Double> collectionData = new HashMap<>();
        collectionData.put("collectionAmount", getSalesOfficerOrManagerBudgetOrCollectionAmount(
                finalSalesOfficerId, companyId, accountingYearId, month,
                CommonConstant.COLLECTION_AMOUNT_FIELD));

        Double collectionBudget = collectionBudgetOverViewService.getSalesOfficerCollectionBudgetOrTarget(
                companyId, accountingYearId, month, salesOfficerId);

        collectionData.put("collectionBudget", collectionBudget);
        /*getSalesOfficerOrManagerBudgetOrCollectionAmount(
                finalSalesOfficerId, companyId, accountingYearId, month,
                CommonConstant.COLLECTION_BUDGET_FIELD)*/
        return collectionData;
    }

    public double getSalesOfficerOrManagerBudgetOrCollectionAmount(
            Long salesOfficerId, Long companyId, Long accountingYearId,
            Integer month, String field) {

        return collectionBudgetOverViewService.getCollectionBudgetByTargetType(
                        BudgetType.DISTRIBUTOR.getCode(), accountingYearId, month,
                        companyId, null, salesOfficerId)
                .stream()
                .filter(cb -> Objects.nonNull(cb.get(field)))
                .mapToDouble(cb -> (Double) cb.get(field))
                .sum();
    }
    public List<Map<String, Object>> getPendingListForApproval(
            Long companyId, String approvalActor, String approvalStatus,
            Integer level, Long approvalStepId, Long multiLayerApprovalPathId, Long approvalActorId, String approvalStepName) {
        return collectionBudgetRepository.getPendingListForApproval(
                companyId, approvalStatus, approvalActor, level, approvalStepId, multiLayerApprovalPathId, approvalActorId, ApprovalStatus.PENDING.getName(), approvalStepName);
    }

    public void updateApprovalStatus(Long id, ApprovalStatus approvalStatus) {
        try {
            Optional<CollectionBudget> collectionBudgetOptional = collectionBudgetRepository.findById(id);
            if (!collectionBudgetOptional.isPresent()) {
                return;
            }
            CollectionBudget collectionBudget = collectionBudgetOptional.get();
            collectionBudget.setApprovalStatus(approvalStatus);
            collectionBudget.setApprovalDate(LocalDate.now());
            collectionBudgetRepository.save(collectionBudget);
        }catch (Exception e){
            throw new RuntimeException("Collection Budget process can't be Executed. Something went wrong!");
        }
    }

}
