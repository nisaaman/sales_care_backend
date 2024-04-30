package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.globalsettings.service.ProductService;
import com.newgen.ntlsnc.salesandcollection.dto.VatSetupDto;
import com.newgen.ntlsnc.salesandcollection.entity.VatSetup;
import com.newgen.ntlsnc.salesandcollection.repository.VatSetupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author anika
 * @Date ২৪/৪/২২
 */
@Service
public class VatSetupService implements IService<VatSetup> {
    @Autowired
    ProductService productService;
    @Autowired
    VatSetupRepository vatSetupRepository;
    @Autowired
    OrganizationService organizationService;

    @Override
    @Transactional
    public VatSetup create(Object object) {
        try {
            VatSetupDto vatSetupDto = (VatSetupDto) object;
            VatSetup vatSetup = new VatSetup();

            vatSetup.setVat(vatSetupDto.getVat());
            vatSetup.setVatIncluded(vatSetupDto.getVatIncluded());
            vatSetup.setRemarks(vatSetupDto.getRemarks());

            LocalDate startDate = LocalDate.parse(vatSetupDto.getFromDate());
            vatSetup.setFromDate(startDate.atStartOfDay());

            LocalDate endDate = LocalDate.parse(vatSetupDto.getToDate());
            vatSetup.setToDate(endDate.atTime(23, 59, 59));  //end of the day

            if (getExistListByProductAndStartDateOrEndDate(vatSetupDto.getProductId(), vatSetup.getFromDate(), vatSetup.getToDate()).size() > 0) {
                throw new IllegalArgumentException("Already exist with this VAT Setup's Start Date or End Date in Included or Excluded List");
            }

            vatSetup.setProduct(productService.findById(vatSetupDto.getProductId()));
            vatSetup.setOrganization(organizationService.getOrganizationFromLoginUser());
            return vatSetupRepository.save(vatSetup);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public VatSetup update(Long id, Object object) {
        VatSetupDto vatSetupDto = (VatSetupDto) object;
        Optional<VatSetup> vatSetupOptional = vatSetupRepository.findById(vatSetupDto.getId());
        VatSetup vatSetup = vatSetupOptional.get();
        if (vatSetupDto.getProductId() != null) {
            vatSetup.setProduct(productService.findById(vatSetupDto.getProductId()));
        }
        vatSetup.setOrganization(organizationService.getOrganizationFromLoginUser());
        vatSetup.setRemarks(vatSetupDto.getRemarks());
        vatSetup.setVat(vatSetupDto.getVat());
        vatSetup.setFromDate(LocalDateTime.parse(vatSetupDto.getFromDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        vatSetup.setToDate(LocalDateTime.parse(vatSetupDto.getToDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return vatSetupRepository.save(vatSetup);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            Optional<VatSetup> vatSetupOptional = vatSetupRepository.findById(id);
            if (!vatSetupOptional.isPresent()) {
                throw new Exception("Vat Setup is Not exist");
            }
            VatSetup vatSetup = vatSetupOptional.get();
            vatSetup.setIsDeleted(true);
            vatSetupRepository.save(vatSetup);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public VatSetup findById(Long id) {
        try {
            Optional<VatSetup> optionalVatSetup = vatSetupRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalVatSetup.isPresent()) {
                throw new Exception("Vat setup Not exist with id " + id);
            }
            return optionalVatSetup.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<VatSetup> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return vatSetupRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }

    public List<Map> getExistListByProductAndStartDateOrEndDate(Long productId, LocalDateTime startDate, LocalDateTime endDate) {
        return vatSetupRepository.getExistListByProductAndStartDateOrEndDate(productId, startDate, endDate);
    }

    public List<Map> getAllByProductAndVatIncluded(Long productId, Boolean vatIncluded) {
        try {
            return vatSetupRepository.getAllByProductAndVatIncluded(productId, vatIncluded);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Map getCurrentVatInfoByProductId(Long productId) {
        try {
            return vatSetupRepository.getCurrentVatInfoByProductId(productId);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Map getAllWithCurrentVatByProductAndVatIncluded(Long productId, Boolean vatIncluded) {
        try {
            Map map = new HashMap();
            map.put("currentVatInfo", getCurrentVatInfoByProductId(productId));
            map.put("vatList", getAllByProductAndVatIncluded(productId, vatIncluded));
            return map;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
