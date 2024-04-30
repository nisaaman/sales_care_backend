package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.CreditLimitTerm;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.globalsettings.service.SemesterService;
import com.newgen.ntlsnc.salesandcollection.dto.CreditLimitDto;
import com.newgen.ntlsnc.salesandcollection.entity.CreditLimit;
import com.newgen.ntlsnc.salesandcollection.entity.CreditLimitProposal;
import com.newgen.ntlsnc.salesandcollection.repository.CreditLimitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CreditLimitService implements IService<CreditLimit> {
    @Autowired
    OrganizationService organizationService;
    @Autowired
    DistributorService distributorService;
    @Autowired
    SemesterService semesterService;
    @Autowired
    CreditLimitRepository creditLimitRepository;
    @Autowired
    CreditLimitProposalService creditLimitProposalService;
    @Autowired
    SalesBookingService salesBookingService;

    @Override
    @Transactional
    public CreditLimit create(Object object) {
        try {
            CreditLimitDto creditLimitDto = (CreditLimitDto) object;
            CreditLimit creditLimit = new CreditLimit();

            CreditLimitTerm creditLimitTerm = CreditLimitTerm.valueOf(creditLimitDto.getCreditLimitTerm());
            creditLimit.setDistributor(distributorService.findById(creditLimitDto.getDistributorId()));

            creditLimit.setCreditLimitTerm(creditLimitTerm);
            creditLimit.setCreditLimit(creditLimitDto.getCreditLimit());
            creditLimit.setCompany(organizationService.findById(creditLimitDto.getCompanyId()));
            creditLimit.setOrganization(organizationService.getOrganizationFromLoginUser());

            if (creditLimitTerm == CreditLimitTerm.LT) {
                creditLimitRepository.inactiveAllByCompanyAndDistributor(creditLimitDto.getCompanyId(), creditLimitDto.getDistributorId()); // inactive previous all credit limit
            } else if (creditLimitTerm == CreditLimitTerm.ST) {
                creditLimit.setStartDate(LocalDate.parse(creditLimitDto.getStartDate()));    //yyyy-MM-dd    // it will come from credit limit proposal
                creditLimit.setEndDate(LocalDate.parse(creditLimitDto.getEndDate()));    //yyyy-MM-dd      // it will come from credit limit proposal
                creditLimit.setCreditLimitProposal(creditLimitProposalService.findById(creditLimitDto.getCreditLimitProposalId()));
            } else if (creditLimitTerm == CreditLimitTerm.SB) {
                creditLimit.setSalesBooking(salesBookingService.findById(creditLimitDto.getSalesBookingId()));
            }

            if (creditLimitDto.getCreditLimitProposalId() != null) {
                creditLimit.setCreditLimitProposal(creditLimitProposalService.findById(creditLimitDto.getCreditLimitProposalId()));
            }

            return creditLimitRepository.save(creditLimit);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    @Override
    @Transactional
    public CreditLimit update(Long id, Object object) {
        CreditLimitDto creditLimitDto = (CreditLimitDto) object;
        CreditLimit creditLimit = this.findById(creditLimitDto.getId());
        if (creditLimitDto.getCreditLimit() != null) {
            creditLimit.setDistributor(distributorService.findById(creditLimitDto.getDistributorId()));
        }
        if (creditLimitDto.getCreditLimitProposalId() != null) {
            creditLimit.setCreditLimitProposal(creditLimitProposalService.findById(creditLimitDto.getCreditLimitProposalId()));
        }
        if (creditLimitDto.getCompanyId() != null) {
            creditLimit.setCompany(organizationService.findById(creditLimitDto.getCompanyId()));
        }
        creditLimit.setOrganization(organizationService.getOrganizationFromLoginUser());
        creditLimit.setCreditLimit(creditLimitDto.getCreditLimit());
        return creditLimitRepository.save(creditLimit);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            Optional<CreditLimit> optionalCreditLimit = creditLimitRepository.findById(id);
            if (!optionalCreditLimit.isPresent()) {
                throw new Exception("Credit Limit Not exist");
            }
            CreditLimit creditLimit = optionalCreditLimit.get();
            creditLimit.setIsDeleted(true);
            creditLimitRepository.save(creditLimit);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public CreditLimit findById(Long id) {
        try {
            Optional<CreditLimit> optionalCreditLimit = creditLimitRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalCreditLimit.isPresent()) {
                throw new Exception("Credit Limit Not exist with id " + id);
            }
            return optionalCreditLimit.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<CreditLimit> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return creditLimitRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }

    public Float getDistributorLimit(Long distributorId, Long companyId) {
        Float creditLimit = 0.0f;
        Optional<CreditLimit> optionalShortTermCreditLimit = creditLimitRepository.findCurrentShortTermByCompanyAndDistributor(companyId, distributorId);
        if (optionalShortTermCreditLimit.isPresent()) {  // short term
            CreditLimit creditLimitClass = optionalShortTermCreditLimit.get();
            creditLimit = creditLimitClass.getCreditLimit();
        } else {  // long term
            Optional<CreditLimit> optionalLongTermCreditLimit = creditLimitRepository.findCurrentLongTermByCompanyAndDistributor(companyId, distributorId);
            if (optionalLongTermCreditLimit.isPresent()) {
                CreditLimit creditLimitLongTerm = optionalLongTermCreditLimit.get();
                creditLimit = creditLimitLongTerm.getCreditLimit();
            }
        }
        if (creditLimit > 0) {  // for distributor ledger balance
            List<Map<String, Object>> distributorBalance = distributorService.getDistributorsDetailsWithPeriodicLedgerBalance(Collections.singletonList(distributorId), companyId, LocalDate.now(), LocalDate.now()); // will be change date range

            if (distributorBalance.size() > 0) {
                Float ledgerBalance = Float.parseFloat(distributorBalance.get(0).get("ledgerBalance").toString());
                creditLimit = creditLimit - ledgerBalance;
            }
        }
        return creditLimit;
    }

    public List<Map<String, Object>> getCreditLimitList(Long companyId) {
        try {
            return creditLimitRepository.findCreditLimitList(companyId);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public void convertToCreditLimitByCreditLimitProposalId(Long creditLimitProposalId) {
        try {
            CreditLimitProposal creditLimitProposal = creditLimitProposalService.findById(creditLimitProposalId);
            CreditLimit creditLimit = new CreditLimit();
            CreditLimitTerm creditLimitTerm = creditLimitProposal.getCreditLimitTerm();

            creditLimit.setCreditLimit(creditLimitProposal.getProposedAmount());
            creditLimit.setCreditLimitTerm(creditLimitTerm);
            creditLimit.setStartDate(creditLimitProposal.getStartDate());
            creditLimit.setEndDate(creditLimitProposal.getEndDate());
            creditLimit.setDistributor(creditLimitProposal.getDistributor());
            creditLimit.setCreditLimitProposal(creditLimitProposal);
            creditLimit.setSalesBooking(creditLimitProposal.getSalesBooking());
            creditLimit.setCompany(creditLimitProposal.getCompany());
            creditLimit.setOrganization(creditLimitProposal.getOrganization());

            if (creditLimitTerm == CreditLimitTerm.LT) {
                creditLimitRepository.inactiveAllByCompanyAndDistributor(creditLimitProposal.getCompany().getId()
                        , creditLimitProposal.getDistributor().getId()); // inactive previous all credit limit
            } else if (creditLimitTerm == CreditLimitTerm.ST) {
                if (isShortTermExistByCompanyAndDistributorAndStartDateOrEndDate(
                        creditLimitProposal.getCompany().getId(), creditLimitProposal.getDistributor().getId(),
                        creditLimitProposal.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        creditLimitProposal.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))) {
                    throw new IllegalArgumentException("Already exist with this Short Term Credit Limit Start Date and End Date");
                }
            }

            creditLimit = creditLimitRepository.save(creditLimit);
            System.out.println(creditLimit);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public boolean isShortTermExistByCompanyAndDistributorAndStartDateOrEndDate(Long companyId, Long distributorId, String startDate, String endDate) {
        boolean isExist = false;
        List<Map> existedList = creditLimitRepository.getExistShortTermListListByCompanyAndDistributorAndStartDateOrEndDate(companyId, distributorId, startDate, endDate);
        if (existedList.size() > 0) {
            isExist = true;
        }
        return isExist;
    }

    public List<Map<String, Object>> getCreditLimitHistoryList(
            Long companyId, List<Long> locationIds, List<Long> salesOfficerIds,
            List<Long> distributorIds, LocalDate startDate, LocalDate endDate) {
        return //null
                creditLimitRepository.getCreditLimitHistoryList(
                companyId, locationIds, salesOfficerIds, distributorIds,
                startDate, endDate);
    }
}
