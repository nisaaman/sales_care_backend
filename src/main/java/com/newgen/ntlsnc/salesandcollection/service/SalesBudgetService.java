package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.CommonConstant;
import com.newgen.ntlsnc.common.FileUploadService;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.common.enums.BudgetType;
import com.newgen.ntlsnc.globalsettings.service.AccountingYearService;
import com.newgen.ntlsnc.globalsettings.service.FileStorageService;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.salesandcollection.dto.SalesBudgetUploadDto;
import com.newgen.ntlsnc.salesandcollection.entity.SalesBudget;
import com.newgen.ntlsnc.salesandcollection.repository.SalesBudgetDetailsRepository;
import com.newgen.ntlsnc.salesandcollection.repository.SalesBudgetRepository;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Newaz Sharif
 * @since 20th Aug, 22
 */
@Service
public class SalesBudgetService {

    @Autowired
    FileStorageService fileStorageService;
    @Autowired
    FileUploadService fileUploadService;
    @Autowired
    AccountingYearService accountingYearService;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    SalesBudgetRepository salesBudgetRepository;
    @Autowired
    SalesBudgetDetailsRepository salesBudgetDetailsRepository;
    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    @Qualifier("salesBudget")
    private Job job;


    public String distributorWiseSalesBudgetDataUpload(SalesBudgetUploadDto salesBudgetUploadDto) {

        try {

            Long uploadedSalesBudgetId = isSalesBudgetAlreadyUploadedForThisAccountingYear(
                    salesBudgetUploadDto.getCompanyId(),salesBudgetUploadDto.getAccountingYearId());

            if(uploadedSalesBudgetId != null) {
                deletePreviouslyUploadedData(uploadedSalesBudgetId,salesBudgetUploadDto.getCompanyId()
                        ,salesBudgetUploadDto.getAccountingYearId());
            }

            MultipartFile salesBudgetFile = salesBudgetUploadDto.getSalesBudgetFile();
            String rootPath = CommonConstant.SALES_BUDGET_FILE_UPLOAD_PATH;
            String filePath = new ClassPathResource(rootPath).getPath();
            String fileName = fileUploadService.getFileNameWithoutExtension(
                    salesBudgetFile.getOriginalFilename())
                    + "." +fileUploadService.getFileExtension(salesBudgetFile.getOriginalFilename());
            fileStorageService.uploadInResourceDirectory(salesBudgetUploadDto.getSalesBudgetFile(),
                                    filePath, fileName);

            SalesBudget salesBudget = create(salesBudgetUploadDto);

            JobParametersBuilder jobParameters = new JobParametersBuilder();
            jobParameters.addLong("salesBudgetId", salesBudget.getId());
            jobParameters.addString("fileName", fileName);

            try {
                JobExecution jobExecution = jobLauncher.run(job, jobParameters.toJobParameters());

                if(String.valueOf(BatchStatus.FAILED).equalsIgnoreCase(String.valueOf(jobExecution.getStatus()))) {
                    salesBudgetRepository.delete(salesBudget);
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

    private SalesBudget create(SalesBudgetUploadDto salesBudgetUploadDto) {
        SalesBudget salesBudget = new SalesBudget();
        salesBudget.setBudgetDate(accountingYearService.getAccountingYearDate(
                salesBudgetUploadDto.getAccountingYearId()
        ).get("startDate"));
        salesBudget.setCompany(organizationService.findById(salesBudgetUploadDto.getCompanyId()));
        salesBudget.setTargetType(BudgetType.DISTRIBUTOR);
        salesBudget.setOrganization(organizationService.findById(salesBudgetUploadDto.getCompanyId()));
        salesBudget.setAccountingYear(accountingYearService.findById(
                salesBudgetUploadDto.getAccountingYearId()));
        salesBudget.setApprovalStatus(ApprovalStatus.APPROVED);//TODO for the time being
        salesBudgetRepository.save(salesBudget);
        return salesBudget;
    }

    public Long isSalesBudgetAlreadyUploadedForThisAccountingYear(
            Long companyId, Long accountingYearId) {

        Optional<SalesBudget> salesBudget = salesBudgetRepository.findByCompanyIdAndAccountingYearIdAndIsDeletedFalseAndIsActiveTrue(
                companyId, accountingYearId);

        return salesBudget.isPresent() ? salesBudget.get().getId(): null;
    }


    private void deletePreviouslyUploadedData(
            Long uploadedSalesBudgetId, Long companyId, Long accountingYearId){

        salesBudgetDetailsRepository.deleteBySalesBudgetId(uploadedSalesBudgetId);
        salesBudgetRepository.deleteByCompanyIdAndAccountingYearId(
                                    companyId,accountingYearId);

    }

    public List<Map<String, Object>> getPendingListForApproval(
            Long companyId,  String approvalActor, String approvalStatus, Integer level,
            Long approvalStepId, Long multiLayerApprovalPathId, Long approvalActorId, String approvalStepName) {
        return salesBudgetRepository.getPendingListForApproval(
                companyId, approvalStatus, approvalActor, level, approvalStepId, multiLayerApprovalPathId, approvalActorId, ApprovalStatus.PENDING.getName(), approvalStepName);
    }

    public void updateApprovalStatus(Long id, ApprovalStatus approvalStatus) {
        try {
            Optional<SalesBudget> salesBudgetOptional = salesBudgetRepository.findById(id);
            if (!salesBudgetOptional.isPresent()) {
                return;
            }
            SalesBudget salesBudget = salesBudgetOptional.get();
            salesBudget.setApprovalStatus(approvalStatus);
            salesBudget.setApprovalDate(LocalDate.now());
            salesBudgetRepository.save(salesBudget);
        }catch (Exception e){
            throw new RuntimeException("Sales Budget process can't be Executed. Something went wrong!");
        }
    }


}
