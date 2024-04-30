package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.dto.BankAccountDto;
import com.newgen.ntlsnc.globalsettings.entity.*;
import com.newgen.ntlsnc.globalsettings.repository.BankAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author Mou
 * Created on 6/3/22 10:35 AM
 */

@Service
public class BankAccountService implements IService<BankAccount> {
    @Autowired
    OrganizationService organizationService;
    @Autowired
    BankBranchService bankBranchService;
    @Autowired
    BankAccountRepository bankAccountRepository;


    @Override
    public BankAccount create(Object object) {
        BankAccountDto bankAccountDto = (BankAccountDto) object;
        BankAccount bankAccount = new BankAccount();
        bankAccount.setAccountNumber(bankAccountDto.getAccountNumber());
        bankAccount.setIsActive(bankAccountDto.getIsActive());
        bankAccount.setOrganization(organizationService.getOrganizationFromLoginUser());
        if (bankAccountDto.getBankBranchId() != null) {
            bankAccount.setBranch(bankBranchService.findById(bankAccountDto.getBankBranchId()));
        }

        if (!validate(bankAccount)) {
            return null;
        }
        return bankAccountRepository.save(bankAccount);
    }

    @Override
    public BankAccount update(Long id, Object object) {
        BankAccountDto bankAccountDto = (BankAccountDto) object;
        BankAccount bankAccount = bankAccountRepository.findById(id).get();
        bankAccount.setIsActive(bankAccountDto.getIsActive());
        bankAccount.setOrganization(organizationService.getOrganizationFromLoginUser());
        bankAccount.setAccountNumber(bankAccountDto.getAccountNumber());
        if (bankAccountDto.getBankBranchId() != null) {
            bankAccount.setBranch(bankBranchService.findById(bankAccountDto.getBankBranchId()));
        }
        try {
            if (bankAccount == null) {
                throw new Exception("Bank Account Not exist");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        if (!validate(bankAccount)) {
            return null;
        }
        return bankAccountRepository.save(bankAccount);
    }

    @Override
    public boolean delete(Long id) {

        try {
            BankAccount bankAccount = bankAccountRepository.findById(id).get();
            if (bankAccount == null) {
                throw new Exception("Branch Not exist");
            }
            bankAccount.setIsDeleted(true);
            bankAccountRepository.save(bankAccount);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    public BankAccount findById(Long id) {
        try {
            Optional<BankAccount> optionalBankAccount = bankAccountRepository.findByIdAndIsDeletedFalse(id);
            if (!optionalBankAccount.isPresent()) {
                throw new Exception("Bank Account Not exist with id " + id);
            }
            return optionalBankAccount.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<BankAccount> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return bankAccountRepository.findAllByOrganizationAndIsDeletedFalseAndBranchIsActiveIsTrue(organization);
    }

    @Override
    public boolean validate(Object object) {

        BankAccount bankAccount= (BankAccount) object;
        Optional<BankAccount> optionalBankAccount = Optional.empty();

        if(bankAccount.getId() == null){
            optionalBankAccount=bankAccountRepository.findByOrganizationAndAccountNumberIgnoreCaseAndIsDeletedFalse(
                    bankAccount.getOrganization(),bankAccount.getAccountNumber());
        }
        else{
            optionalBankAccount = bankAccountRepository.findByOrganizationAndIdIsNotAndAccountNumberIgnoreCaseAndIsDeletedFalse(
                    bankAccount.getOrganization(), bankAccount.getId(), bankAccount.getAccountNumber());
        }
        if(optionalBankAccount.isPresent()){
            throw new RuntimeException("Account already exist");
        }

        return true;
    }

    public List<BankAccount> findAllBankAccountByBankBranch(Long branchId) {
        return bankAccountRepository.findByBranchIdAndIsDeletedFalse(branchId);
    }

    public List<BankAccount> findAllActiveBankAccount(Long branchId) {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        List<BankAccount> bankAccountList= bankAccountRepository.findAllByOrganizationAndBranchIdAndIsDeletedFalseAndIsActiveTrue(organization,branchId);
        return bankAccountList;

    }
}
