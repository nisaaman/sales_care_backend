package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.dto.SemesterDto;
import com.newgen.ntlsnc.globalsettings.entity.AccountingYear;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.Semester;
import com.newgen.ntlsnc.globalsettings.repository.SemesterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * @author sagor
 * Created on 4/4/22 11:09 AM
 */

@Service
public class SemesterService implements IService<Semester> {

    @Autowired
    OrganizationService organizationService;
    @Autowired
    SemesterRepository semesterRepository;
    @Autowired
    AccountingYearService accountingYearService;

    @Override
    public boolean validate(Object object) {
        return true;
    }

    @Override
    @Transactional
    public Semester create(Object object) {

        SemesterDto semesterDto = (SemesterDto) object;
        Semester semester = new Semester();
//        Organization organization = organizationService.findById(semesterDto.getOrganizationId());
//        semester.setOrganization(organization);
        semester.setOrganization(organizationService.getOrganizationFromLoginUser());
        AccountingYear accountingYear = accountingYearService.findById(semesterDto.getAccountingYearId());
        semester.setStartDate(LocalDate.parse(semesterDto.getStartDate()));   //yyyy-MM-dd
        semester.setEndDate(LocalDate.parse(semesterDto.getEndDate()));   //yyyy-MM-dd
        semester.setSemesterName(semesterDto.getSemesterName());
        semester.setAccountingYear(accountingYear);


        if (!this.validate(semester)) {
            return null;
        }
        return semesterRepository.save(semester);
    }

    @Override
    @Transactional
    public Semester update(Long id, Object object) {

        SemesterDto semesterDto = (SemesterDto) object;
        Semester semester = semesterRepository.findById(semesterDto.getId()).get();
        semester.setOrganization(organizationService.getOrganizationFromLoginUser());
        AccountingYear accountingYear = accountingYearService.findById(semesterDto.getAccountingYearId());
        semester.setStartDate(LocalDate.parse(semesterDto.getStartDate()));    //yyyy-MM-dd
        semester.setEndDate(LocalDate.parse(semesterDto.getEndDate()));    //yyyy-MM-dd
        semester.setSemesterName(semesterDto.getSemesterName());
        semester.setAccountingYear(accountingYear);

        if (!this.validate(semester)) {
            return null;
        }
        return semesterRepository.save(semester);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            Semester semester = findById(id);
            semester.setIsDeleted(true);
            semesterRepository.save(semester);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Semester findById(Long id) {
        try {
            Optional<Semester> optionalSemester = semesterRepository.findById(id);
            if (!optionalSemester.isPresent()) {
                throw new Exception("Semester Not exist with id " + id);
            }
            return optionalSemester.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<Semester> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return semesterRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Transactional
    public List<Semester> createAll(List<SemesterDto> semesterDtoList, AccountingYear accountingYear) {
        try {
            List<Semester> semesterList = new ArrayList<>();
            semesterDtoList.forEach((s) -> {
                Semester semester = new Semester();
                if (s.getId() != null) {
                    semester = findById(s.getId());
                } else {
                    semester.setOrganization(accountingYear.getOrganization());
                    semester.setAccountingYear(accountingYear);
                }
                semester.setSemesterName(s.getSemesterName().trim());
                semester.setStartDate(LocalDate.parse(s.getStartDate()));
                semester.setEndDate(LocalDate.parse(s.getEndDate()));
                semester.setIsDeleted(false);
                semesterList.add(semester);
            });
            return semesterRepository.saveAll(semesterList);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Boolean deleteAllByAccountingYearId(Long accountingYearId) {
        try {
            semesterRepository.deleteAllByAccountingYearId(accountingYearId);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    public Optional<Semester> findSemesterByDate(LocalDate currentDate, Long companyId) {
        return semesterRepository.findByDateRangeAndCompany(currentDate, currentDate, companyId);
    }

    public Map<String, LocalDate> getSemesterDate(Long id) {
        Map<String, LocalDate> dateMap = new HashMap<>();
        LocalDate startDate = null;
        LocalDate endDate = null;

        Semester semester =
                findById(id);

        if (semester != null) {
            startDate = semester.getStartDate();
            endDate = semester.getEndDate();
        }
        dateMap.put("startDate", startDate);
        dateMap.put("endDate", endDate);

        return dateMap;
    }

    public List<Map> getAllSemesterByAccountingYearId(Long accountingYearId) {
        return semesterRepository.getAllByAccountingYearId(accountingYearId);
    }

    public List<Semester> getAllByAccountingYearId(Long accountingYearId) {
        return semesterRepository.findAllByAccountingYearIdAndIsDeletedFalseAndIsActiveTrue(accountingYearId);
    }

    public Semester getCurrentSemesterByCompany(Long companyId) {
        try {
            Semester semester = new Semester();
            Optional<Semester> optionalSemester = findSemesterByDate(LocalDate.now(), companyId);
            if (optionalSemester.isPresent()) {
                semester = optionalSemester.get();
            }
            return semester;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map> getAllCurrentAndFutureSemesterByCompany(Long companyId) {
        return semesterRepository.getAllCurrentAndFutureSemesterByCompany(companyId);
    }

    //private validation. not database level
    public Boolean isDuplicateNameExistInSemesterList(List<SemesterDto> semesterDtoList) {
        for (int i = 0; i < semesterDtoList.size(); i++) {
            semesterDtoList.get(i).getSemesterName();
            for (int j = 0; j < semesterDtoList.size(); j++) {
                if (i != j) {
                    boolean isEqual = semesterDtoList.get(i).getSemesterName().trim().equals(semesterDtoList.get(j).getSemesterName().trim());
                    if (isEqual) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
