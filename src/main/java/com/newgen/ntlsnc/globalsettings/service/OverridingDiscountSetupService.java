package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.common.enums.CalculationType;
import com.newgen.ntlsnc.globalsettings.dto.OverridingDiscountDto;
import com.newgen.ntlsnc.globalsettings.entity.InvoiceNature;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.OverridingDiscount;
import com.newgen.ntlsnc.globalsettings.entity.Semester;
import com.newgen.ntlsnc.globalsettings.repository.OverridingDiscountRepository;
import com.newgen.ntlsnc.salesandcollection.service.DistributorService;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * @author sagor
 * @date ৯/৪/২২
 */
@Service
public class OverridingDiscountSetupService implements IService<OverridingDiscount> {

    @Autowired
    OrganizationService organizationService;
    @Autowired
    OverridingDiscountRepository overridingDiscountRepository;
    @Autowired
    SemesterService semesterService;
    @Autowired
    InvoiceNatureService invoiceNatureService;
    @Autowired
    DistributorService distributorService;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    OverridingDiscountService overridingDiscountService;

    @Override
    @Transactional
    public OverridingDiscount create(Object object) {
        OverridingDiscountDto overridingDiscountDto = (OverridingDiscountDto) object;
        OverridingDiscount overridingDiscount = new OverridingDiscount();
        overridingDiscount.setFromDay(overridingDiscountDto.getFromDay());
        overridingDiscount.setToDay(overridingDiscountDto.getToDay());
        overridingDiscount.setCalculationType(CalculationType.valueOf(overridingDiscountDto.getCalculationType()));
        overridingDiscount.setOrd(overridingDiscountDto.getOrd());
        overridingDiscount.setApprovalStatus(ApprovalStatus.PENDING);
        overridingDiscount.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (overridingDiscountDto.getSemesterId() != null) {
            overridingDiscount.setSemester(semesterService.findById(overridingDiscountDto.getSemesterId()));
        }
        if (overridingDiscountDto.getInvoiceNatureId() != null) {
            overridingDiscount.setInvoiceNature(invoiceNatureService.findById(overridingDiscountDto.getInvoiceNatureId()));
        }
        if (overridingDiscountDto.getCompanyId() != null) {
            overridingDiscount.setCompany(organizationService.findById(overridingDiscountDto.getCompanyId()));
        }

        if (!this.validate(overridingDiscount)) {
            return null;
        }
        return overridingDiscountRepository.save(overridingDiscount);
    }

    @Override
    @Transactional
    public OverridingDiscount update(Long id, Object object) {
        OverridingDiscountDto overridingDiscountDto = (OverridingDiscountDto) object;
        OverridingDiscount overridingDiscount = overridingDiscountRepository.findById(overridingDiscountDto.getId()).get();
        try {
            if (overridingDiscount == null) {
                throw new Exception("Overriding Discount does not exist");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        overridingDiscount.setFromDay(overridingDiscountDto.getFromDay());
        overridingDiscount.setToDay(overridingDiscountDto.getToDay());
        overridingDiscount.setCalculationType(CalculationType.valueOf(overridingDiscountDto.getCalculationType()));
        overridingDiscount.setOrd(overridingDiscountDto.getOrd());
        overridingDiscount.setApprovalStatus(overridingDiscountDto.getApprovalStatus());
        overridingDiscount.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (overridingDiscountDto.getSemesterId() != null) {
            Semester semester = semesterService.findById(overridingDiscountDto.getSemesterId());
            overridingDiscount.setSemester(semester);
        }
        if (overridingDiscountDto.getInvoiceNatureId() != null) {
            overridingDiscount.setInvoiceNature(invoiceNatureService.findById(overridingDiscountDto.getInvoiceNatureId()));
        }
        if (overridingDiscountDto.getCompanyId() != null) {
            overridingDiscount.setCompany(organizationService.findById(overridingDiscountDto.getCompanyId()));
        }

        if (!this.validate(overridingDiscount)) {
            return null;
        }
        return overridingDiscountRepository.save(overridingDiscount);
    }

    @Override
    public boolean delete(Long id) {
        try {
            OverridingDiscount overridingDiscount = findById(id);
            overridingDiscount.setIsDeleted(true);
            overridingDiscountRepository.save(overridingDiscount);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public OverridingDiscount findById(Long id) {
        try {
            Optional<OverridingDiscount> optionalOverridingDiscount = overridingDiscountRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalOverridingDiscount.isPresent()) {
                throw new Exception("Overriding Discount Not exist with id " + id);
            }
            return optionalOverridingDiscount.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<OverridingDiscount> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return overridingDiscountRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    public OverridingDiscount findByIdIgnoreAllStatus(Long id) {
        try {
            Optional<OverridingDiscount> optionalOverridingDiscount = overridingDiscountRepository.findById(id);
            if (!optionalOverridingDiscount.isPresent()) {
                throw new Exception("Overriding Discount Not exist with id " + id);
            }
            return optionalOverridingDiscount.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    public Boolean createAll(List<OverridingDiscountDto> overridingDiscountDtoList) {
        try {
            List<OverridingDiscount> overridingDiscountList = new ArrayList<>();
            if (getAllByCompanyAndSemesterAndInvoiceNature(overridingDiscountDtoList.get(0).getCompanyId(), overridingDiscountDtoList.get(0).getSemesterId(), overridingDiscountDtoList.get(0).getInvoiceNatureId()).size() > 0) {
                throw new IllegalArgumentException("Already exist with this ORD Setup");
            }

            Organization company = organizationService.findById(overridingDiscountDtoList.get(0).getCompanyId());
            Semester semester = semesterService.findById(overridingDiscountDtoList.get(0).getSemesterId());
            InvoiceNature invoiceNature = invoiceNatureService.findById(overridingDiscountDtoList.get(0).getInvoiceNatureId());
            Organization organization = organizationService.getOrganizationFromLoginUser();

            overridingDiscountDtoList.forEach(dto -> {
                OverridingDiscount overridingDiscount = new OverridingDiscount();
                overridingDiscount.setFromDay(dto.getFromDay());
                overridingDiscount.setToDay(dto.getToDay());
                overridingDiscount.setCalculationType(CalculationType.valueOf(dto.getCalculationType()));
                overridingDiscount.setOrd(dto.getOrd());
                //@Todo will be pending
                overridingDiscount.setApprovalStatus(ApprovalStatus.APPROVED);

                overridingDiscount.setCompany(company);
                overridingDiscount.setSemester(semester);
                overridingDiscount.setInvoiceNature(invoiceNature);
                overridingDiscount.setOrganization(organization);
                overridingDiscountList.add(overridingDiscount);
            });
            overridingDiscountRepository.saveAll(overridingDiscountList);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public Boolean updateAll(List<OverridingDiscountDto> overridingDiscountDtoList) {
        try {
            List<OverridingDiscount> overridingDiscountList = new ArrayList<>();

            String previousCompanyIdSemesterIdInvoiceNatureId = overridingDiscountDtoList.get(0).getPreviousCompanyIdSemesterIdInvoiceNatureId();
            String[] splitIds = previousCompanyIdSemesterIdInvoiceNatureId.split("-");

            // if all master ids are previous ids
            if (Long.parseLong(splitIds[0]) == overridingDiscountDtoList.get(0).getCompanyId() && Long.parseLong(splitIds[1]) == overridingDiscountDtoList.get(0).getSemesterId() && Long.parseLong(splitIds[2]) == overridingDiscountDtoList.get(0).getInvoiceNatureId()) {
                overridingDiscountRepository.deactivateAllByCompanyAndSemesterAndInvoiceNature(overridingDiscountDtoList.get(0).getCompanyId(), overridingDiscountDtoList.get(0).getSemesterId(), overridingDiscountDtoList.get(0).getInvoiceNatureId());
            } else {
                if (getAllByCompanyAndSemesterAndInvoiceNature(overridingDiscountDtoList.get(0).getCompanyId(),
                        overridingDiscountDtoList.get(0).getSemesterId(),
                        overridingDiscountDtoList.get(0).getInvoiceNatureId()).size() > 0) {
                    throw new IllegalArgumentException("Already exist with this ORD Setup");
                }
            }

            Organization company = organizationService.findById(overridingDiscountDtoList.get(0).getCompanyId());
            Semester semester = semesterService.findById(overridingDiscountDtoList.get(0).getSemesterId());
            InvoiceNature invoiceNature = invoiceNatureService.findById(overridingDiscountDtoList.get(0).getInvoiceNatureId());
            Organization organization = organizationService.getOrganizationFromLoginUser();

            overridingDiscountDtoList.forEach(dto -> {
                OverridingDiscount overridingDiscount = new OverridingDiscount();
                if (dto.getId() != null) {
                    overridingDiscount = findByIdIgnoreAllStatus(dto.getId());
                }
                overridingDiscount.setFromDay(dto.getFromDay());
                overridingDiscount.setToDay(dto.getToDay());
                overridingDiscount.setCalculationType(CalculationType.valueOf(dto.getCalculationType()));
                overridingDiscount.setOrd(dto.getOrd());
                overridingDiscount.setApprovalStatus(ApprovalStatus.PENDING);
                overridingDiscount.setIsActive(true);
                overridingDiscount.setIsDeleted(false);
                overridingDiscount.setCompany(company);
                overridingDiscount.setSemester(semester);
                overridingDiscount.setInvoiceNature(invoiceNature);
                overridingDiscount.setOrganization(organization);
                overridingDiscountList.add(overridingDiscount);
            });
            overridingDiscountRepository.saveAll(overridingDiscountList);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<OverridingDiscount> findAllByCompany(Organization company) {
        List<OverridingDiscount> overridingDiscounts = overridingDiscountRepository.findAllByCompanyAndIsDeletedFalse(company);
        return overridingDiscounts;
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }

    public OverridingDiscount findByCompanyIdAndInvoiceNatureIdAndSemesterDateAndDay(Long companyId, Long invoiceNatureId, LocalDate invoiceDate, Integer day) {
        return overridingDiscountRepository.findByCompanyIdAndInvoiceNatureIdAndSemesterDateAndDay(companyId, invoiceNatureId, invoiceDate, day);
    }

    public List<Map<String, Object>> getInvoiceWiseOverridingDiscountList(Long companyId, Long salesOfficerId) {
        if (salesOfficerId == null) {
            salesOfficerId = applicationUserService.getApplicationUserIdFromLoginUser();
        }
        return overridingDiscountService.getInvoiceWiseOverridingDiscountList(companyId, salesOfficerId);
    }

    public List<Map<String, Object>> getOverridingDiscountDetailsOfASalesInvoice(Long companyId, Long salesInvoiceId) {
        return overridingDiscountService.getOverridingDiscountDetailsOfASalesInvoice(companyId, salesInvoiceId);
    }

    public List<Map> getAllByCompanyAndSemesterAndInvoiceNature(Long companyId, Long semesterId, Long invoiceNature) {
        try {
            List<Map> mapList = overridingDiscountRepository.getAllByCompanyAndSemesterAndInvoiceNature(companyId, semesterId, invoiceNature);
            return mapList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map> getAllByCompanyAndSemesterAndInvoiceNatureExceptIds(Long companyId, Long semesterId, Long invoiceNature, List<Long> ids) {
        try {
            List<Map> mapList = overridingDiscountRepository.getAllByCompanyAndSemesterAndInvoiceNatureExceptIds(companyId, semesterId, invoiceNature, ids);
            return mapList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map> getOrdListWithRelatedInfoByCompanyAndSemesterAndInvoiceNature(Long companyId, Long semesterId, Long invoiceNatureId) {
        try {
            List<Map> ordList = overridingDiscountRepository.getOrdListWithRelatedInfoByCompanyAndSemesterAndInvoiceNature(companyId, semesterId, invoiceNatureId);
            return ordList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map> getUniqueSemesterListOfOrdByCompany(Long companyId) {
        try {
            List<Map> semesterList = overridingDiscountRepository.getUniqueSemesterListOfOrdByCompany(companyId);
            return semesterList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map> getUniqueInvoiceNatureListOfOrdByCompanyAndSemester(Long companyId, Long semesterId) {
        try {
            List<Map> invoiceList = overridingDiscountRepository.getUniqueInvoiceNatureListOfOrdByCompanyAndSemester(companyId, semesterId);
            return invoiceList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map> getOrdListForListPageByCompany(Long companyId) {
        try {
            List<Map> semesterList = getUniqueSemesterListOfOrdByCompany(companyId);
            List<Map> finalSemesterList = new ArrayList<>();
            for (int i = 0; i < semesterList.size(); i++) {
                Map semester = new HashMap();
                semesterList.get(i).forEach((key, tab) -> semester.put(key, tab));
                List<Map> invoiceNatureList = getUniqueInvoiceNatureListOfOrdByCompanyAndSemester(companyId,
                        Long.parseLong(semester.get("semester_id").toString()));
                List<Map> finalInvoiceNatureList = new ArrayList<>();
                for (int j = 0; j < invoiceNatureList.size(); j++) {
                    Map invoiceNature = new HashMap();
                    invoiceNatureList.get(j).forEach((key, tab) -> invoiceNature.put(key, tab));
                    List<Map> ordList = getOrdListWithRelatedInfoByCompanyAndSemesterAndInvoiceNature(companyId,
                            Long.parseLong(semester.get("semester_id").toString()),
                            Long.parseLong(invoiceNature.get("invoice_nature_id").toString()));
                    invoiceNature.put("ordList", ordList);
                    finalInvoiceNatureList.add(invoiceNature);
                }
                semester.put("invoice_list", finalInvoiceNatureList);
                finalSemesterList.add(semester);
            }
            return finalSemesterList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
