package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.dto.AccountingYearDto;
import com.newgen.ntlsnc.globalsettings.entity.AccountingYear;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.Semester;
import com.newgen.ntlsnc.globalsettings.repository.AccountingYearRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Mou
 * Created on 4/3/22 11:27 AM
 */

@Service
public class AccountingYearService implements IService<AccountingYear> {
    @Autowired
    AccountingYearRepository accountingYearRepository;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    SemesterService semesterService;

    @Override
    public boolean validate(Object object) {
        return true;
    }

    @Override
    @Transactional
    public AccountingYear create(Object object) {
        try {
            AccountingYearDto accountingYearDto = (AccountingYearDto) object;
            if (semesterService.isDuplicateNameExistInSemesterList(accountingYearDto.getSemesterList())) {
                throw new IllegalArgumentException("Duplicate Semester Name Exist");
            } else if (getExistListByCompanyAndName(accountingYearDto.getCompanyId(), accountingYearDto.getFiscalYearName()).size() > 0) {
                throw new IllegalArgumentException("Already exist with this Fiscal Year Name");
            } else if (getExistListByCompanyAndStartDateOrEndDate(accountingYearDto.getCompanyId(), accountingYearDto.getStartDate(), accountingYearDto.getEndDate()).size() > 0) {
                throw new IllegalArgumentException("Already exist with this Fiscal Year Start Date and End Date");
            }
            AccountingYear accountingYear = new AccountingYear();
            accountingYear.setFiscalYearName(accountingYearDto.getFiscalYearName());
            accountingYear.setStartDate(LocalDate.parse(accountingYearDto.getStartDate()));    //yyyy-MM-dd
            accountingYear.setEndDate(LocalDate.parse(accountingYearDto.getEndDate()));     //yyyy-MM-dd
            accountingYear.setCompany(organizationService.findById(accountingYearDto.getCompanyId()));
            accountingYear.setOrganization(organizationService.getOrganizationFromLoginUser());

            if (!validate(accountingYear)) {
                return null;
            }
            accountingYear = accountingYearRepository.save(accountingYear);

            List<Semester> semesterList = semesterService.createAll(accountingYearDto.getSemesterList(), accountingYear);
            return accountingYear;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public AccountingYear update(Long id, Object object) {
        AccountingYearDto accountingYearDto = (AccountingYearDto) object;
        accountingYearDto.setFiscalYearName(accountingYearDto.getFiscalYearName().trim());
        if (getExistListByCompanyAndNameExceptId(accountingYearDto.getCompanyId(), accountingYearDto.getFiscalYearName(), accountingYearDto.getId()).size() > 0) {
            throw new IllegalArgumentException("Already exist with this Fiscal Year Name");
        } else if (getExistListByCompanyAndStartDateOrEndDateExceptId(accountingYearDto.getCompanyId(), accountingYearDto.getStartDate(), accountingYearDto.getEndDate(), accountingYearDto.getId()).size() > 0) {
            throw new IllegalArgumentException("Already exist with this Fiscal Year Start Date and End Date");
        }
        AccountingYear accountingYear = findById(id);
        accountingYear.setFiscalYearName(accountingYearDto.getFiscalYearName());
        accountingYear.setStartDate(LocalDate.parse(accountingYearDto.getStartDate()));   //yyyy-MM-dd
        accountingYear.setEndDate(LocalDate.parse(accountingYearDto.getEndDate()));   //yyyy-MM-dd
        accountingYear.setCompany(organizationService.findById(accountingYearDto.getCompanyId()));

        if (!validate(accountingYear)) {
            return null;
        }
        accountingYear = accountingYearRepository.save(accountingYear);

        semesterService.deleteAllByAccountingYearId(id);
        List<Semester> semesterList = semesterService.createAll(accountingYearDto.getSemesterList(), accountingYear);
        return accountingYear;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            AccountingYear accountingYear = findById(id);
            accountingYear.setIsDeleted(true);
            accountingYearRepository.save(accountingYear);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public AccountingYear findById(Long id) {
        try {
            Optional<AccountingYear> optionalAccountingYear = accountingYearRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalAccountingYear.isPresent()) {
                throw new Exception("Account Year Not exist with id " + id);
            }
            return optionalAccountingYear.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<AccountingYear> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return accountingYearRepository.findAllByOrganizationAndIsDeletedFalse(organization);

    }

    public List<AccountingYear> findAllByCompanyId(Long companyId) {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return accountingYearRepository.findOrganizationORCompany(organization.getId(), companyId);

    }

    public Map<String, LocalDate> getAccountingYearDate(Long id) {
        Map<String, LocalDate> dateMap = new HashMap<>();
        LocalDate startDate = null;
        LocalDate endDate = null;
        if (id != null) {
        AccountingYear accountingYear =
                findById(id);

        if (accountingYear != null) {
            startDate = accountingYear.getStartDate();
            endDate = accountingYear.getEndDate();
        }
        dateMap.put("startDate", startDate);
        dateMap.put("endDate", endDate);
        }
        return dateMap;
    }

    public Long getCurrentAccountingYearId(Long companyId, LocalDate toDate) {
        Optional<AccountingYear> accountingYear = accountingYearRepository.findCurrentAccountingYear(
                companyId, toDate);

        return (accountingYear.isPresent()) == true ? accountingYear.get().getId() : null;
    }

    public Map getCurrentAccountingYear(Long companyId, LocalDate toDate) {
        Optional<AccountingYear> accountingYear = accountingYearRepository.findCurrentAccountingYear(
                companyId, toDate);
        Map accountingYearMap = new HashMap();
        accountingYearMap.put("id", accountingYear.get().getId());
        accountingYearMap.put("fiscal_year_name", accountingYear.get().getFiscalYearName());
        return accountingYearMap;
    }

    public AccountingYear getAccountingYearByDateRange(LocalDate startDate, LocalDate endDate, Long companyId) {
        return accountingYearRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndCompanyIdAndIsDeletedFalse(startDate, endDate,companyId);
    }

    public List<Map<String, Object>> getAllByCompanyId(Long companyId) {
        return accountingYearRepository.getAllByCompanyId(companyId);
    }

    public List<Map> getAllAccountingYearWithSemesterListByCompanyId(Long companyId) {
        List<Map> mapList = new ArrayList<>();
        List<Map<String, Object>> accountingYearList = getAllByCompanyId(companyId);
        accountingYearList.forEach(a -> {
            Map accountingYearMap = new HashMap();
            accountingYearMap.put("id", a.get("id"));
            accountingYearMap.put("fiscal_year_name", a.get("fiscal_year_name"));
            accountingYearMap.put("fiscal_year_start_date", a.get("fiscal_year_start_date"));
            accountingYearMap.put("fiscal_year_end_date", a.get("fiscal_year_end_date"));
            accountingYearMap.put("start_date_formated", a.get("start_date_formated"));
            accountingYearMap.put("end_date_formated", a.get("end_date_formated"));
            accountingYearMap.put("fiscal_year_days", a.get("fiscal_year_days"));
            accountingYearMap.put("status", a.get("status"));
            accountingYearMap.put("semesterList", semesterService.getAllSemesterByAccountingYearId(Long.parseLong(a.get("id").toString())));
            mapList.add(accountingYearMap);
        });
        return mapList;
    }

    public Map getWithSemestersByAccountingYear(Long accountingYearId) {
        Map response = new HashMap();
        AccountingYear accountingYear = findById(accountingYearId);
        response.put("accountingYear", accountingYear);
        response.put("semesterList", semesterService.getAllByAccountingYearId(accountingYearId));
        return response;
    }

    public List<Map> getExistListByCompanyAndStartDateOrEndDate(Long companyId, String startDate, String endDate) {
        return accountingYearRepository.getExistListByCompanyAndStartDateOrEndDate(companyId, startDate, endDate);
    }

    public List<Map> getExistListByCompanyAndStartDateOrEndDateExceptId(Long companyId, String startDate, String endDate, Long id) {
        return accountingYearRepository.getExistListByCompanyAndStartDateOrEndDateExceptId(companyId, startDate, endDate, id);
    }

    public List<Map> getExistListByCompanyAndName(Long companyId, String name) {
        return accountingYearRepository.getExistListByCompanyAndName(companyId, name);
    }

    public List<Map> getExistListByCompanyAndNameExceptId(Long companyId, String name, Long id) {
        return accountingYearRepository.getExistListByCompanyAndNameExceptId(companyId, name, id);
    }

    public List<Map<String, Object>> getRecentAccountingYear(Long companyId) {

        return getAllByCompanyId(companyId).stream()
                .limit(5)
                .collect(Collectors.toList());
    }

    public Long getPreviousAccountingYear(Long accountingYearId, Long companyId) {
        Map<String, Object> objectMap = accountingYearRepository.getPreviousAccountingYear(
                accountingYearId, companyId);

        if (objectMap.size() > 0 && objectMap.get("id") != null)
            return Long.parseLong(String.valueOf(objectMap.get("id")));

        else  return null;
    }
}
