package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.service.InvoiceNatureService;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.salesandcollection.dto.InvoiceOverdueDto;
import com.newgen.ntlsnc.salesandcollection.entity.InvoiceOverdue;
import com.newgen.ntlsnc.salesandcollection.repository.InvoiceOverdueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author kamal
 * @Date ২৪/৮/২২
 */

@Service
public class InvoiceOverdueService implements IService<InvoiceOverdue> {
    @Autowired
    OrganizationService organizationService;
    @Autowired
    InvoiceOverdueRepository invoiceOverdueRepository;
    @Autowired
    InvoiceNatureService invoiceNatureService;


    @Override
    public InvoiceOverdue create(Object object) {
        return null;
    }

    @Override
    public InvoiceOverdue update(Long id, Object object) {
        return null;
    }

    @Override
    public boolean delete(Long id) {
        return false;
    }

    @Override
    public InvoiceOverdue findById(Long id) {
        try {
            Optional<InvoiceOverdue> optionalInvoiceOverdue = invoiceOverdueRepository.findById(id);
            if (!optionalInvoiceOverdue.isPresent()) {
                throw new Exception("Invoice Overdue Not exist with id " + id);
            }
            return optionalInvoiceOverdue.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<InvoiceOverdue> findAll() {
        return null;
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }

    @Transactional
    public Boolean createAll(List<InvoiceOverdueDto> invoiceOverdueDtoList) {
        try {
            List<InvoiceOverdue> invoiceOverdueList = new ArrayList<>();

            if (getAllByCompany(invoiceOverdueDtoList.get(0).getCompanyId()).size() > 0) {
                throw new IllegalArgumentException("Already exist with this Invoice Overdue");
            }

            Organization company = organizationService.findById(invoiceOverdueDtoList.get(0).getCompanyId());
            Organization organization = organizationService.getOrganizationFromLoginUser();

            invoiceOverdueDtoList.forEach(dto -> {
                InvoiceOverdue invoiceOverdue = new InvoiceOverdue();
                invoiceOverdue.setStartDay(dto.getStartDay());
                invoiceOverdue.setEndDay(dto.getEndDay());
                invoiceOverdue.setNotDueDays(dto.getNotDueDays());

                invoiceOverdue.setCompany(company);
                invoiceOverdue.setInvoiceNature(invoiceNatureService.findById(dto.getInvoiceNatureId()));
                invoiceOverdue.setOrganization(organization);
                invoiceOverdueList.add(invoiceOverdue);
            });
            invoiceOverdueRepository.saveAll(invoiceOverdueList);

            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public Boolean updateAll(List<InvoiceOverdueDto> invoiceOverdueDtoList) {
        try {
            List<InvoiceOverdue> invoiceOverdueList = new ArrayList<>();

            if (invoiceOverdueDtoList.get(0).getCompanyId() == invoiceOverdueDtoList.get(0).getPreviousCompanyId()) {
                deactivateAllByCompanyId(invoiceOverdueDtoList.get(0).getCompanyId());
            } else {
                if (getAllByCompany(invoiceOverdueDtoList.get(0).getCompanyId()).size() > 0) {
                    throw new IllegalArgumentException("Already exist with this Invoice Overdue");
                }
            }

            Organization company = organizationService.findById(invoiceOverdueDtoList.get(0).getCompanyId());
            Organization organization = organizationService.getOrganizationFromLoginUser();

            invoiceOverdueDtoList.forEach(dto -> {
                InvoiceOverdue invoiceOverdue = new InvoiceOverdue();
                if (dto.getId() != null) {
                    invoiceOverdue = findById(dto.getId());
                }
                invoiceOverdue.setStartDay(dto.getStartDay());
                invoiceOverdue.setEndDay(dto.getEndDay());
                invoiceOverdue.setNotDueDays(dto.getNotDueDays());
                invoiceOverdue.setIsActive(true);
                invoiceOverdue.setIsDeleted(false);

                invoiceOverdue.setCompany(company);
                invoiceOverdue.setInvoiceNature(invoiceNatureService.findById(dto.getInvoiceNatureId()));
                invoiceOverdue.setOrganization(organization);
                invoiceOverdueList.add(invoiceOverdue);
            });
            invoiceOverdueRepository.saveAll(invoiceOverdueList);

            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<InvoiceOverdue> getAllByCompany(Long companyId) {
        try {
            List<InvoiceOverdue> invoiceOverdueList = invoiceOverdueRepository.findAllByCompanyIdAndIsActiveTrueAndIsDeletedFalse(companyId);
            return invoiceOverdueList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void deactivateAllByCompanyId(Long companyId) {
        try {
            invoiceOverdueRepository.deactivateAllByCompanyId(companyId);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
