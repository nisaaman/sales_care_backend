package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.CommonConstant;
import com.newgen.ntlsnc.common.FileUploadService;
import com.newgen.ntlsnc.common.enums.DistributorOpeningBalanceTemplate;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.service.FileStorageService;
import com.newgen.ntlsnc.salesandcollection.dto.DistributorOpeningBalanceDto;
import com.newgen.ntlsnc.salesandcollection.entity.Distributor;
import com.newgen.ntlsnc.salesandcollection.entity.DistributorBalance;
import com.newgen.ntlsnc.salesandcollection.repository.DistributorBalanceRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author nisa
 * @Date 7/8/23, 10:00 AM
 */

@Service
public class DistributorBalanceService {
    @Autowired
    FileUploadService fileUploadService;
    @Autowired
    FileStorageService fileStorageService;

    @Autowired
    DistributorBalanceRepository distributorBalanceRepository;

    @Autowired
    @Qualifier("distributorOpeningBalance")
    private Job job;

    @Autowired
    private JobLauncher jobLauncher;

    public List<String> getDistributorOpeneingBalanceColumnNameList() {

        return Stream.of(DistributorOpeningBalanceTemplate.values())
                .map(DistributorOpeningBalanceTemplate::getName)
                .collect(Collectors.toList());
    }

    public String openingBalanceUpload(DistributorOpeningBalanceDto distributorOpeningBalanceDto) {
        try {
            MultipartFile salesBudgetFile = distributorOpeningBalanceDto.getOpeningBalanceFile();
            String rootPath = CommonConstant.DISTRIBUTOR_OPENING_BALANCE_FILE_UPLOAD_PATH;
            String filePath = new ClassPathResource(rootPath).getPath();
            String fileName = fileUploadService.getFileNameWithoutExtension(
                    salesBudgetFile.getOriginalFilename())
                    + "." +fileUploadService.getFileExtension(salesBudgetFile.getOriginalFilename());
            fileStorageService.uploadInResourceDirectory(distributorOpeningBalanceDto.getOpeningBalanceFile(),
                    filePath, fileName);

            JobParameters jobParameters = new JobParametersBuilder().
                    addLong("companyId", distributorOpeningBalanceDto.getCompanyId()).
                    addString("fileName", fileName).
                    addLong("time", System.currentTimeMillis()).
                    toJobParameters();

            try {
                JobExecution jobExecution = jobLauncher.run(job, jobParameters);

                if(String.valueOf(BatchStatus.FAILED).equalsIgnoreCase(String.valueOf(jobExecution.getStatus()))) {
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

    public DistributorBalance findById(Long id) {
        try {
            Optional<DistributorBalance> optionalDistributorBalance = distributorBalanceRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalDistributorBalance.isPresent()) {
                throw new Exception("Distributor Balance Not exist with id " + id);
            }
            return optionalDistributorBalance.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


   public List<Long> getNotUsedListByDistributorIdAndCompanyId(Long distributorId, Long companyId){
        return distributorBalanceRepository.getNotUsedListByDistributorIdAndCompanyId(distributorId, companyId);
    }


    @Transactional
    public void deleteAll(List<Long> distributorBalanceIds) {
        try {
            List<DistributorBalance> distributorBalanceList = distributorBalanceRepository.findByIdIn(distributorBalanceIds);
            distributorBalanceRepository.deleteAll(distributorBalanceList);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public boolean existsByDistributorAndCompany(Distributor distributor, Organization company){
        return distributorBalanceRepository.existsByDistributorAndCompany(distributor, company);
    }



}

