package com.newgen.ntlsnc.supplychainmanagement.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.QaStatus;
import com.newgen.ntlsnc.globalsettings.service.ProductService;
import com.newgen.ntlsnc.supplychainmanagement.dto.QualityInspectionDetailsDto;
import com.newgen.ntlsnc.supplychainmanagement.entity.QualityInspection;
import com.newgen.ntlsnc.supplychainmanagement.entity.QualityInspectionDetails;
import com.newgen.ntlsnc.supplychainmanagement.repository.QualityInspectionDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Newaz Sharif
 * @since 30th Oct, 22
 */
@Service
public class QualityInspectionDetailsService implements IService {

    @Autowired
    BatchService batchService;
    @Autowired
    ProductService productService;
    @Autowired
    QualityInspectionDetailsRepository qualityInspectionDetailsRepository;

    public Object create(List<QualityInspectionDetailsDto> qualityInspectionDetailsDtoList,
                         QualityInspection qualityInspection) {

        List<QualityInspectionDetails> detailsList = new ArrayList<>();
        qualityInspectionDetailsDtoList.forEach(qualityInspectionDetailsDto -> {

            QualityInspectionDetails qualityInspectionDetails = new QualityInspectionDetails();

            qualityInspectionDetails.setQuantity(qualityInspectionDetailsDto.getQuantity());
            qualityInspectionDetails.setQaStatus(QaStatus.valueOf(qualityInspectionDetailsDto.getStatus()));
            qualityInspectionDetails.setBatch(batchService.findById(qualityInspectionDetailsDto.getBatchId()));
            qualityInspectionDetails.setProduct(productService.findById(qualityInspectionDetailsDto.getProductId()));
            qualityInspectionDetails.setQualityInspection(qualityInspection);
            qualityInspectionDetails.setOrganization(qualityInspection.getOrganization());
            detailsList.add(qualityInspectionDetails);

        });
        if(detailsList.size() == 0)
            throw new RuntimeException("Product Details not available...");
        qualityInspectionDetailsRepository.saveAll(detailsList);
        return detailsList;
    }

    @Override
    public Object create(Object object) {
        return null;
    }

    @Override
    public Object update(Long id, Object object) {
        return null;
    }

    @Override
    public boolean delete(Long id) {
        return false;
    }

    @Override
    public Object findById(Long id) {
        return null;
    }

    @Override
    public List findAll() {
        return null;
    }

    @Override
    public boolean validate(Object object) {
        return false;
    }
}
