package com.newgen.ntlsnc.supplychainmanagement.service;

import com.newgen.ntlsnc.common.CommonConstant;
import com.newgen.ntlsnc.common.FileUploadService;
import com.newgen.ntlsnc.common.enums.ProductStockOpeningTemplate;
import com.newgen.ntlsnc.common.enums.StoreType;
import com.newgen.ntlsnc.globalsettings.entity.AccountingYear;
import com.newgen.ntlsnc.globalsettings.repository.LocationRepository;
import com.newgen.ntlsnc.globalsettings.repository.ProductRepository;
import com.newgen.ntlsnc.globalsettings.service.*;
import com.newgen.ntlsnc.salesandcollection.dto.StockOpeningDataUploadDto;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @author Newaz Sharif
 * @since 21st Sep, 2022
 */
@Service
public class StockService {

    @Autowired
    ProductRepository productRepository;
    @Autowired
    DepotService depotService;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    StoreService storeService;
    @Autowired
    FileUploadService fileUploadService;
    @Autowired
    FileStorageService fileStorageService;

    @Autowired
    @Qualifier("stockOpeningData")
    private Job job;

    @Autowired
    private JobLauncher jobLauncher;

    public List<Map<String, Object>> getDepotWiseStockDetailsInfo(
            Long companyId, Long depotId, Long productCategoryId) {

        return productRepository.
                getStockDetailsWithWeightedAverageRate(companyId, depotId, productCategoryId);

    }

    public List<Map<String, Object>> getDepotStockInfo(
            Long companyId, Long userLoginId) {

        List<Long> depotIdList = new ArrayList<>();
        Map<String, Object> depotMap = depotService.getDepotByLoginUserId(companyId, userLoginId);

        if (depotMap.size() == 0)
            depotIdList = null;
        else {
            Long depotId = Long.parseLong(String.valueOf(depotMap.get("id")));
            depotIdList.add(depotId);
        }

        return productRepository.getDepotStockInfo(companyId, depotIdList);

    }


    public List<Map<String, Object>> getProductWiseBatchStockInfo(
            Long companyId, Long productId, String storeType) {

        Long organizationId = organizationService.getOrganizationIdFromLoginUser();
        Long userLoginId = applicationUserService.getApplicationUserIdFromLoginUser();

        Map<String, Object> depotMap = depotService.getDepotByLoginUserId(companyId, userLoginId);

        if (depotMap.size() > 0) {
            Long depotId = Long.parseLong(String.valueOf(depotMap.get("id")));
            return productRepository.getProductWiseBatchStockInfo(organizationId, companyId,
                    depotId, productId, storeType);
        } else {
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> getStockValuation(
            Long companyId, List<Long> categoryIds, List<Long> depotIds,
            List<Long> productIds, LocalDate fromDate, LocalDate endDate, String reportType) {
        LocalDate asOfDate = null;
        /*Store store = storeService.findById(storeId);
        return productRepository.getStockValuation(depotId, companyId,
                store.getName(), store.getStoreType().getCode());*/
        productRepository.SNC_PRODUCT_CATEGORY_HIERARCHY(
                Long.parseLong(String.valueOf(companyId)));

        Map store = storeService.getStore(String.valueOf(StoreType.REGULAR));
        //,Long.parseLong(store.get("id").toString())

        if ("DETAILS".equals(reportType)) {
            if (fromDate == null && endDate == null)
                asOfDate = LocalDate.now();
            if (fromDate == null && endDate != null)
                asOfDate = endDate;
            if (fromDate != null && endDate == null)
                asOfDate = fromDate;

            if (fromDate == null && endDate == null)
                fromDate = LocalDate.now();
            if (fromDate == null && endDate != null)
                fromDate = endDate;

            return productRepository.getInventoryStockValuation(companyId,
                    categoryIds, depotIds, productIds, fromDate, endDate, asOfDate
            );
        } else
            return productRepository.getInventoryStockValuationSummary(companyId,
                    categoryIds, depotIds, productIds, fromDate, endDate
                    , Long.parseLong(store.get("id").toString())
            );

    }

    public Map<String, Object> getProductBlockedQuantityInStock(
            Long companyId, Long depotId, Long productId) {
        return productRepository.getStockBlockedQuantity(companyId, depotId, productId);
    }


    public List<Map<String, Object>> getProductDetailsListForStockOpening(Long companyId) {
        productRepository.SNC_PRODUCT_CATEGORY_HIERARCHY(companyId);
        return productRepository.getProductDetailsListForStockOpeningExcelDownloadData(companyId);

    }

    public List<String> getProductDetailsColumnNameList(List<Map<String, Object>> dataList) {

        Set<String> columnNameSet = Stream.of(ProductStockOpeningTemplate.values()).map(ProductStockOpeningTemplate::getName).collect(Collectors.toSet());
        columnNameSet
                .addAll(dataList.get(1).keySet());

        return new ArrayList<>(columnNameSet);
    }

    public String stockOpeningDataUpload(StockOpeningDataUploadDto stockOpeningDataUploadDto) {
        try {
            MultipartFile salesBudgetFile = stockOpeningDataUploadDto.getStockOpeningFile();
            String rootPath = CommonConstant.STOCK_OPENING_FILE_UPLOAD_PATH;
            String filePath = new ClassPathResource(rootPath).getPath();
            String fileName = fileUploadService.getFileNameWithoutExtension(
                    salesBudgetFile.getOriginalFilename())
                    + "." + fileUploadService.getFileExtension(salesBudgetFile.getOriginalFilename());
            fileStorageService.uploadInResourceDirectory(stockOpeningDataUploadDto.getStockOpeningFile(),
                    filePath, fileName);

            JobParametersBuilder jobParameters = new JobParametersBuilder();
            jobParameters.addLong("companyId", stockOpeningDataUploadDto.getCompanyId());
            jobParameters.addLong("depotId", stockOpeningDataUploadDto.getDepotId());
            jobParameters.addString("fileName", fileName);

            try {
                JobExecution jobExecution = jobLauncher.run(job, jobParameters.toJobParameters());

                if (String.valueOf(BatchStatus.FAILED).equalsIgnoreCase(String.valueOf(jobExecution.getStatus()))) {
                    return "Invalid Excel File. Please Download File then Upload";
                }

            } catch (JobExecutionAlreadyRunningException | JobRestartException
                     | JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
                e.printStackTrace();
                return "Job Already Running or Invalid Job Parameter";
            } catch (ExcelFileParseException efpe) {
                return "Invalid Excel File. Please Download File then Upload";
            } finally {
                fileStorageService.deleteFileFromDirectory(filePath, fileName);
            }

        } catch (RuntimeException re) {
            throw new RuntimeException(re.getMessage());
        }
        return "success";
    }


//    public Long isSalesBudgetAlreadyUploadedForThisAccountingYear(
//            Long companyId, Long accountingYearId) {
//
//        Optional<SalesBudget> salesBudget = salesBudgetRepository.findByCompanyIdAndAccountingYearIdAndIsDeletedFalseAndIsActiveTrue(
//                companyId, accountingYearId);
//
//        return salesBudget.isPresent() == true ? salesBudget.get().getId(): null;
//    }

    public List<Map<String, Object>> getFinishedGoodsAgeing(
            Long companyId, Long storeId, List<Long> categoryIds, List<Long> depotIds,
            List<Long> productIds, LocalDate fromDate, LocalDate endDate, String reportType) {
        LocalDate asOfDate = LocalDate.now();
        if (endDate == null)
            endDate = LocalDate.now();

        //Map store = storeService.getStore(String.valueOf(StoreType.REGULAR));
        //Long.parseLong(store.get("id").toString());

        return productRepository.getFinishedGoodsAgeing(companyId,
                categoryIds, depotIds, productIds, fromDate, endDate,
                storeId, asOfDate);
    }

}
