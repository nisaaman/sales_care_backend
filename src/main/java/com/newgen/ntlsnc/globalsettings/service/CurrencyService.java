package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.dto.CurrencyDto;
import com.newgen.ntlsnc.globalsettings.entity.BankAccount;
import com.newgen.ntlsnc.globalsettings.entity.Currency;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author kamal
 */

@Service
public class CurrencyService implements IService<Currency> {
    @Autowired
    CurrencyRepository currencyRepository;
    @Autowired
    OrganizationService organizationService;

    @Override
    @Transactional
    public Currency create(Object object) {

        CurrencyDto currencyDto = (CurrencyDto) object;
        Currency currency = new Currency();
        currency.setName(currencyDto.getName());
        currency.setDescription(currencyDto.getDescription());
        currency.setIsActive(currencyDto.getIsActive());
        currency.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (!this.validate(currency)) {
            return null;
        }
        return currencyRepository.save(currency);
    }

    @Override
    @Transactional
    public Currency update(Long id, Object object) {
        CurrencyDto currencyDto = (CurrencyDto) object;
        Currency currency = currencyRepository.findById(id).get();
        currency.setName(currencyDto.getName());
        currency.setDescription(currencyDto.getDescription());
        currency.setIsActive(currencyDto.getIsActive());
        currency.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (!this.validate(currency)) {
            return null;
        }
        return currencyRepository.save(currency);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {

        try {
            Currency currency = currencyRepository.findById(id).get();
            if (currency == null) {
                throw new Exception("Currency Not exist");
            }
            currency.setIsDeleted(true);
            currencyRepository.save(currency);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Currency findById(Long id) {
        try {
            Optional<Currency> optionalCurrency = currencyRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalCurrency.isPresent()) {
                throw new Exception("Currency Not exist with id " + id);
            }
            return optionalCurrency.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<Currency> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return currencyRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Override
    public boolean validate(Object object) {
//        BankAccount bankAccount= (BankAccount) object;
//        Optional<BankAccount> optionalBankAccount = Optional.empty();
//
//        if(bankAccount.getId() == null){
//            optionalBankAccount=bankAccountRepository.findByOrganizationAndAccountNumberIgnoreCaseAndIsDeletedFalse(
//                    bankAccount.getOrganization(),bankAccount.getAccountNumber());
//        }
//        else{
//            optionalBankAccount = bankAccountRepository.findByOrganizationAndIdIsNotAndAccountNumberIgnoreCaseAndIsDeletedFalse(
//                    bankAccount.getOrganization(), bankAccount.getId(), bankAccount.getAccountNumber());
//        }
//        if(optionalBankAccount.isPresent()){
//            throw new RuntimeException("Account already exist");
//        }
         Currency currency = (Currency) object;
         Optional<Currency> optionalCurrency = Optional.empty();
         if(currency.getId()==null){
             optionalCurrency = currencyRepository.findByOrganizationAndNameIgnoreCaseAndIsDeletedFalse(
                     currency.getOrganization(),currency.getName());
         }
         else {
             optionalCurrency=currencyRepository.findByOrganizationAndIdIsNotAndNameIgnoreCaseAndIsDeletedFalse(
                     currency.getOrganization(), currency.getId(),currency.getName());
         }
         if(optionalCurrency.isPresent()){
             throw new RuntimeException("Currency already exist");
         }

        return true;
    }
}
