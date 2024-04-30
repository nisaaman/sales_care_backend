package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.dto.BankDto;
import com.newgen.ntlsnc.globalsettings.entity.Bank;
import com.newgen.ntlsnc.globalsettings.entity.BankBranch;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.repository.BankRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author kamal
 */

@Service
public class BankService implements IService<Bank> {

    @Autowired
    BankRepository bankRepository;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    BankBranchService bankBranchService;

    @Override
    @Transactional
    public Bank create(Object object) {
        BankDto bankDto = (BankDto) object;
        Bank bank = new Bank();
        bank.setName(bankDto.getName());
        bank.setBankShortName(bankDto.getBankShortName());
        bank.setDescription(bankDto.getDescription());
        bank.setIsActive(bankDto.getIsActive());
        bank.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (!this.validate(bank)) {
            return null;
        }

        return bankRepository.save(bank);
    }

    @Override
    @Transactional
    public Bank update(Long id, Object object) {
        BankDto bankDto = (BankDto) object;
        Bank bank = this.findById(id);
        bank.setName(bankDto.getName());
        bank.setBankShortName(bankDto.getBankShortName());
        bank.setDescription(bankDto.getDescription());
        bank.setIsActive(bankDto.getIsActive());
        bank.setOrganization(organizationService.getOrganizationFromLoginUser());
        if (!this.validate(bank)) {
            return null;
        }
        return bankRepository.save(bank);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            Optional<Bank> bank = bankRepository.findById(id);
            if (!bank.isPresent()) {
                throw new Exception("Bank not exist.");
            }

            List<BankBranch> bankBranchList = bankBranchService.findAllBranchByBank(bank.get().getId());
            if(bankBranchList.size() > 0){
                throw new RuntimeException("This bank already in use.");
            }

            bank.get().setIsDeleted(true);
            bankRepository.save(bank.get());
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Bank findById(Long id) {
        try {
            Optional<Bank> optionalBank = bankRepository.findByIdAndIsDeletedFalse(id);
            if (!optionalBank.isPresent()) {
                throw new Exception("Bank Not exist with id " + id);
            }
            return optionalBank.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<Bank> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return bankRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Override
    public boolean validate(Object object) {
        Bank bank = (Bank) object;
        Optional<Bank> optionalBank = Optional.empty();

        if(bank.getId() == null) {
            optionalBank = bankRepository.findByOrganizationAndNameIgnoreCaseAndIsDeletedFalse(
                    bank.getOrganization(), bank.getName().trim());
        } else {
            optionalBank = bankRepository.findByOrganizationAndIdIsNotAndNameIgnoreCaseAndIsDeletedFalse(
                    bank.getOrganization(), bank.getId(), bank.getName().trim());
        }

        if(optionalBank.isPresent()){
            throw new RuntimeException("Bank name already exist.");
        }

        return true;
    }

    public List<Map<String, Object>> getBankList() {

        List<Map<String, Object>> resultList = new ArrayList<>();

        findAll().forEach( bank -> {

            Map<String, Object> map = new HashMap<>();
            map.put("id", bank.getId());
            map.put("name", bank.getName());

            resultList.add(map);
        });

        return resultList;
    }




    public List<Map<String, Object>> getAllActiveBankList() {

        List<Map<String, Object>> resultList = new ArrayList<>();
        Organization organization = organizationService.getOrganizationFromLoginUser();
        List<Bank> bankList =bankRepository.findAllByOrganizationAndIsActiveTrueAndIsDeletedFalse(organization);
        bankList.forEach( bank -> {

            Map<String, Object> map = new HashMap<>();
            map.put("id", bank.getId());
            map.put("name", bank.getName());

            resultList.add(map);
        });

        return resultList;
    }

    public Bank findByBankShortNameAndIsDeletedFalse(String shortName) {
        try {
            Optional<Bank> optionalBank =
                    bankRepository.findByBankShortNameAndIsDeletedFalse(shortName);
            if (!optionalBank.isPresent()) {
                throw new Exception("Bank Not exist with id " + shortName);
            }
            return optionalBank.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
