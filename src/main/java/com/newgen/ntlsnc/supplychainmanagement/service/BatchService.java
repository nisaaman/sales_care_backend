package com.newgen.ntlsnc.supplychainmanagement.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.MonthCode;
import com.newgen.ntlsnc.common.enums.StoreType;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.Product;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.globalsettings.service.ProductService;
import com.newgen.ntlsnc.salesandcollection.entity.Distributor;
import com.newgen.ntlsnc.supplychainmanagement.dto.BatchDetailsDto;
import com.newgen.ntlsnc.supplychainmanagement.dto.BatchDto;
import com.newgen.ntlsnc.supplychainmanagement.entity.Batch;
import com.newgen.ntlsnc.supplychainmanagement.entity.BatchDetails;
import com.newgen.ntlsnc.supplychainmanagement.repository.BatchDetailsRepository;
import com.newgen.ntlsnc.supplychainmanagement.repository.BatchRepository;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * @author kamal
 * @Date ১৩/৪/২২
 */

@Service
public class BatchService implements IService<Batch> {
    @Autowired
    OrganizationService organizationService;
    @Autowired
    BatchRepository batchRepository;
    @Autowired
    BatchDetailsRepository batchDetailsRepository;
    @Autowired
    ProductService productService;
    @Autowired
    ApplicationUserService applicationUserService;

    @Transactional
    @Override
    public Batch create(Object object) {
        BatchDto batchDto = (BatchDto) object;
        BatchDetailsDto batchDetailsDto = batchDto.getBatchDetailsDto();
        Batch batch = new Batch();
        String batchNo = generateBatchFormat(batchDto.getConsignmentNo(),
                productService.findById(batchDetailsDto.getProductId()).getProductSku(),
                LocalDate.parse(batchDto.getProductionDate()));

        if(isItDuplicateBatchNo(batchDto.getId(), batchNo)) {
            return null;
        }
        if (batchDto.getId() != null) {
            batch.setId(batchDto.getId());
        }
        batch.setConsignmentNo(batchDto.getConsignmentNo());
        batch.setBatchNo(batchNo);
        batch.setQuantity(batchDto.getBatchQuantity());
        batch.setProductionDate(LocalDate.parse(batchDto.getProductionDate()));    //yyyy-MM-dd
        batch.setOrganization(organizationService.getOrganizationFromLoginUser());
        batch.setRemarks(batchDto.getRemarks());
        if (batchDto.getCompanyId() != null) {
            batch.setCompany(organizationService.findById(batchDto.getCompanyId()));
        }
        if (!this.validate(batch)) {
            return null;
        }
        batch = batchRepository.save(batch);
        if (batchDto.getId() == null) {
            BatchDetails batchDetails = getBatchDetails(batchDto.getBatchDetailsDto(), batch);
            batchDetailsRepository.save(batchDetails);
        }
        return batch;
    }

    @Transactional
    @Override
    public Batch update(Long id, Object object) {
        BatchDto batchDto = (BatchDto) object;
        Optional<Batch> batchOptional = batchRepository.findById(id);
        if (!batchOptional.isPresent()) {
            return null;
        }
        Batch batch = batchOptional.get();
        //batch.setBatchNo(batchDto.getBatchNo());
        batch.setProductionDate(LocalDate.parse(batchDto.getProductionDate()));    //yyyy-MM-dd
        batch.setOrganization(organizationService.getOrganizationFromLoginUser());
        if (batchDto.getCompanyId() != null) {
            batch.setCompany(organizationService.findById(batchDto.getCompanyId()));
        }
        if (!this.validate(batch)) {
            return null;
        }
        batch = batchRepository.save(batch);
        BatchDetails batchDetails = getBatchDetails(batchDto.getBatchDetailsDto(), batch);
        batchDetailsRepository.save(batchDetails);
        return batch;
    }

    @Transactional
    @Override
    public boolean delete(Long id) {
        Optional<Batch> batchOptional = batchRepository.findById(id);
        try {
            if (!batchOptional.isPresent()) {
                throw new Exception("Batch not exist");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        Batch batch = batchOptional.get();
        batch.setIsDeleted(true);
        batchRepository.save(batch);
        List<BatchDetails> batchDetailsList = batchDetailsRepository.findAllByBatch(batch);
        batchDetailsList.forEach(salesBookingDetails -> salesBookingDetails.setIsDeleted(true));
        batchDetailsRepository.saveAll(batchDetailsList);
        return true;
    }

    @Override
    public Batch findById(Long id) {
        try {
            Optional<Batch> optionalBatch = batchRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalBatch.isPresent()) {
                throw new Exception("Batch Not exist with id " + id);
            }
            return optionalBatch.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<Batch> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return batchRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    private BatchDetails getBatchDetails(BatchDetailsDto batchDetailsDto, Batch batch) {
        List<BatchDetails> batchDetailsList = new ArrayList<>();

            BatchDetails batchDetails = new BatchDetails();
            if (batchDetailsDto.getId() != null) {
                batchDetails = batchDetailsRepository.findById(batchDetailsDto.getId()).get();
            }

            if(batchDetailsDto.getExpiryDate() !=null){
                batchDetails.setExpiryDate(LocalDate.parse(batchDetailsDto.getExpiryDate()));
            }
            batchDetails.setOrganization(batch.getOrganization());
            batchDetails.setBatch(batch);
            if (batchDetailsDto.getSupervisorId() != null) {
                ApplicationUser supervisor = applicationUserService.findById(batchDetailsDto.getSupervisorId());
                batchDetails.setSupervisor(supervisor);
            }else {
                batchDetails.setSupervisor(applicationUserService.getApplicationUserFromLoginUser());
            }
            if (batchDetailsDto.getProductId() != null) {
                Product product = productService.findById(batchDetailsDto.getProductId());
                batchDetails.setProduct(product);
                if(batchDetails.getExpiryDate() ==null){
                    batchDetails.setExpiryDate(LocalDate.now().plusDays(product.getExpiryDays()));
                }
            }


        return batchDetails;
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }

    public boolean validate(Long id, String batchName) {
        Optional<Batch> optionalObj = Optional.empty();

        if (id == null) {
            optionalObj = batchRepository.findByBatchNoIgnoreCaseAndIsDeletedFalse(batchName.trim());
        } else {
            optionalObj = batchRepository.findByIdIsNotAndBatchNoIgnoreCaseAndIsDeletedFalse(id, batchName.trim());
        }
        if (optionalObj.isPresent()) {
            return true;
        }
        return false;
    }

    public List<Map> getAllByDeliveryChallanIdAndProductId(Long deliveryChallanId, Long productId) {
        try {
            return batchRepository.findAllByDeliveryChallanIdAndProductId(deliveryChallanId, productId);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public String generateBatchFormat(
            String consignmentNo, String productSku, LocalDate productionDate) {

        StringJoiner batchFormatJoiner = new StringJoiner("-");
        batchFormatJoiner.add(consignmentNo);
        batchFormatJoiner.add(productSku);
        batchFormatJoiner.add(String.valueOf(productionDate.getDayOfMonth()));
        batchFormatJoiner.add(String.valueOf(MonthCode.valueOf(
                                String.valueOf(productionDate.getMonth())).getName()));
        batchFormatJoiner.add(String.valueOf(productionDate.getYear()));
        return String.valueOf(batchFormatJoiner);

    }

    public List<Map<String, Object>> getBatchNoListFromACompany(Long companyId) {

        return batchRepository.getAllBatchFromACompany(companyId);
    }

    public boolean isItDuplicateBatchNo(Long batchId, String batchNo) {
         return validate(batchId, batchNo);
                //batchRepository.existsByBatchNo(batchNo);
    }

    public Integer getBatchQuantity(Long batchId) {

        return (Integer) batchRepository.getBatchQuantity(batchId);
    }

    public Map<String, Object> getProductLatestBatchInfo(Long productId) {

        return batchRepository.getProductLatestBatchInfo(productId);
    }

    public Map<String, Object> getProductBatchInfoByProductIdAndBatchId(
            Long productId, Long batchId) {

        return batchRepository.getProductBatchInfoByProductIdAndBatchId(productId,batchId);
    }

    public Object getBatchNoAutoCompleteList(Long productId, String searchString) {

        return  batchRepository.
                getBatchNoAutoCompleteList(productId, searchString);
    }

    public List<Map<String, Object>> getAllBatchFromAProduct(Long productId) {

        return batchRepository.getAllBatchFromAProduct(productId);
    }

    public Object getDepotAndStoreWiseBatchAndStockInfo(
            Long companyId, Long depotId, Long productId, String storeType, String searchString) {

        return  batchRepository.
                getDepotAndStoreAndProductWiseBatchAndStockInfo(companyId, depotId,
                        productId, storeType, searchString);
    }

    public Map<String, Object> getProductBatchInfoByBatchNo(
            String batchNo) {

        return batchRepository.getProductBatchInfoByBatchNo(batchNo);
    }
}
