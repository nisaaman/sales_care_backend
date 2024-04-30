package com.newgen.ntlsnc.supplychainmanagement.service;

import com.newgen.ntlsnc.common.FileUploadService;
import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.common.enums.FileType;
import com.newgen.ntlsnc.common.enums.StoreType;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.Store;
import com.newgen.ntlsnc.globalsettings.service.*;
import com.newgen.ntlsnc.supplychainmanagement.dto.QualityInspectionDto;
import com.newgen.ntlsnc.supplychainmanagement.entity.QualityInspection;
import com.newgen.ntlsnc.supplychainmanagement.repository.QualityInspectionRepository;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Newaz Sharif
 * @since 30th Oct, 22
 */

@Service
public class QualityInspectionService implements IService<QualityInspection> {

    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    DepotService depotService;
    @Autowired
    StoreService storeService;
    @Autowired
    QualityInspectionRepository qualityInspectionRepository;
    @Autowired
    FileUploadService fileUploadService;
    @Autowired
    DocumentService documentService;
    @Autowired
    QualityInspectionDetailsService qualityInspectionDetailsService;
    @Autowired
    AccountingYearService accountingYearService;

    @Transactional
    @Override
    public QualityInspection create(Object object) {

        QualityInspectionDto qualityInspectionDto = (QualityInspectionDto) object;
        QualityInspection qualityInspection = new QualityInspection();
        qualityInspection.setQaDate(LocalDate.now());
        qualityInspection.setQaBy(applicationUserService.findById(
                qualityInspectionDto.getQaBy()));

        Organization organization = organizationService.getOrganizationFromLoginUser();
        qualityInspection.setOrganization(organization);
        qualityInspection.setCompany(organizationService.findById(qualityInspectionDto.getCompanyId()));
        qualityInspection.setDepot(depotService.findById(qualityInspectionDto.getDepotId()));
        qualityInspection.setStore(storeService.findById(qualityInspectionDto.getStoreId()));
        qualityInspection.setApprovalStatus(ApprovalStatus.APPROVED);

        qualityInspectionRepository.save(qualityInspection);

        if (qualityInspectionDto.getFile() != null) {
            String filePath = fileUploadService.fileUpload(qualityInspectionDto.getFile(),
                    FileType.DOCUMENT.getCode(), "QualityInspection",
                    organizationService.getOrganizationIdFromLoginUser(),
                    qualityInspectionDto.getCompanyId());

            documentService.save("QualityInspection", filePath, qualityInspection.getId(),
                    fileUploadService.getFileNameFromFilePath(filePath), FileType.DOCUMENT.getCode()
                    , organization, qualityInspectionDto.getCompanyId(),
                    qualityInspectionDto.getFile().getSize());
        }

        qualityInspectionDetailsService.create(
                qualityInspectionDto.getQualityInspectionDetails(), qualityInspection);

        return qualityInspection;
    }

    @Override
    public QualityInspection update(Long id, Object object) {
        return null;
    }

    @Override
    public boolean delete(Long id) {
        return false;
    }

    @Override
    public QualityInspection findById(Long id) {
        return null;
    }

    @Override
    public List<QualityInspection> findAll() {
        return null;
    }

    @Override
    public boolean validate(Object object) {
        return false;
    }

    public List<Map> getQualityInspectionInfo(
            Long companyId, Long depotId, Long productCategoryId, Long accountingYearId,
            String qaStatus) {

        if(accountingYearId == null){
            accountingYearId = accountingYearService.getCurrentAccountingYearId(
                    companyId, LocalDate.now());

            if(accountingYearId == null)
                    return new ArrayList<>();
        }
        Map<String, LocalDate> accountingYear = accountingYearService
                .getAccountingYearDate(accountingYearId);

        return qualityInspectionRepository.getQualityInspectionInfo(
                companyId, depotId, productCategoryId, accountingYear.get("startDate"),
                accountingYear.get("endDate"),qaStatus);
    }

    public Map  getProductBatchAvailableQcStock (Long companyId,
                                                    Long productId, Long batchId, Long depotId) {
        Store store = storeService.getStoreByOrganizationAndStoreType(
                organizationService.getOrganizationFromLoginUser(), StoreType.QUARANTINE);

        Map qcStockMap = qualityInspectionRepository.getProductBatchAvailableQcStock(
                        companyId, productId, batchId, depotId, store.getId());

        return qcStockMap;
    }
}
