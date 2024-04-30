package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.dto.BankBranchDto;
import com.newgen.ntlsnc.globalsettings.entity.Bank;
import com.newgen.ntlsnc.globalsettings.entity.BankAccount;
import com.newgen.ntlsnc.globalsettings.entity.BankBranch;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.repository.BankBranchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author Mou
 * Created on 5/3/22 02:7 PM
 */

@Service
public class BankBranchService implements IService<BankBranch> {

    @Autowired
    BankBranchRepository bankBranchRepository;
    @Autowired
    BankService bankService;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    BankAccountService bankAccountService;

    @Override
    @Transactional
    public BankBranch create(Object object) {

        BankBranchDto bankBranchDto = (BankBranchDto) object;
        BankBranch bankBranch = new BankBranch();

//        if (bankBranchDto.getOrganizationId() != null) {
//            bankBranch.setOrganization(organizationService.findById(bankBranchDto.getOrganizationId()));
//        }
        if (bankBranchDto.getBankId() != null) {
            bankBranch.setBank(bankService.findById(bankBranchDto.getBankId()));
        }
        if(bankBranchDto.getAddress()!= null){
            bankBranch.setAddress(bankBranchDto.getAddress());
        }
        bankBranch.setName(bankBranchDto.getName());
        bankBranch.setContactNumber(bankBranchDto.getContactNumber());
        //bankBranch.setAddress(bankBranchDto.getAddress());
        bankBranch.setEmail(bankBranchDto.getEmail());
        bankBranch.setIsActive(bankBranchDto.getIsActive());
        bankBranch.setOrganization(organizationService.getOrganizationFromLoginUser());
        if (!validate(bankBranch)) {
            return null;
        }
        return bankBranchRepository.save(bankBranch);
    }

    @Override
    @Transactional
    public BankBranch update(Long id, Object object) {
        BankBranchDto bankBranchDto = (BankBranchDto) object;
        BankBranch bankBranch = this.findById(id);

//        try {
//            if (bankBranch == null) {
//                throw new Exception("Bank Branch Not exist");
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e.getMessage());
//        }

        Optional<BankBranch> optionalBankBranch3 = bankBranchRepository.getByOrganizationAndEmailAndIdIsNot(
                bankBranch.getOrganization(), bankBranchDto.getEmail(), bankBranchDto.getId());
        if (optionalBankBranch3.isPresent()) {
            throw new RuntimeException("Email is already exist");
        }
        if (bankBranchDto.getBankId() != null) {
            bankBranch.setBank(bankService.findById(bankBranchDto.getBankId()));
        }
        bankBranch.setName(bankBranchDto.getName());
        Optional<BankBranch> optionalBankBranch2 = bankBranchRepository.findByOrganizationAndIdIsNotAndContactNumber(
                bankBranch.getOrganization(), bankBranchDto.getId(), bankBranchDto.getContactNumber());
        if (optionalBankBranch2.isPresent()) {
            throw new RuntimeException("Contact Number is already exist");
        }
        bankBranch.setContactNumber(bankBranchDto.getContactNumber());
        bankBranch.setAddress(bankBranchDto.getAddress());
        bankBranch.setEmail(bankBranchDto.getEmail());
        bankBranch.setIsActive(bankBranchDto.getIsActive());
        bankBranch.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (!validate(bankBranch)) {
            return null;
        }
        return bankBranchRepository.save(bankBranch);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {

        try {
            BankBranch bankBranch = bankBranchRepository.findById(id).get();
            //Long branchId= bankBranch.getId();
            if (bankBranch == null) {
                throw new Exception("Branch Not exist");
            }
            List<BankAccount> bankAccountList = bankAccountService.findAllBankAccountByBankBranch(bankBranch.getId());
            if (bankAccountList.size() > 0) {
                throw new RuntimeException("This Bank Branch already in use.");
            }

            bankBranch.setIsDeleted(true);
            bankBranchRepository.save(bankBranch);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public BankBranch findById(Long id) {
        try {
            Optional<BankBranch> optionalBankBranch = bankBranchRepository.findByIdAndIsDeletedFalse(id);
            if (!optionalBankBranch.isPresent()) {
                throw new Exception("Bank Branch is Not exist with id " + id);
            }
            return optionalBankBranch.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<BankBranch> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return bankBranchRepository.findAllByOrganizationAndIsDeletedFalseAndBankIsActiveIsTrue(organization);
    }

    @Override
    public boolean validate(Object object) {

        BankBranch bankBranch = (BankBranch) object;
        Optional<BankBranch> optionalBankBranch = Optional.empty();
        Optional<BankBranch> optionalBankBranch2 = Optional.empty();
        Optional<BankBranch> optionalBankBranch3 = Optional.empty();
        Optional<BankBranch> optionalBankBranch4 = Optional.empty();


        if (bankBranch.getId() == null) {         // execute when data will create
            optionalBankBranch = bankBranchRepository.findByOrganizationAndBankAndNameIgnoreCaseAndIsDeletedFalse(
                    bankBranch.getOrganization(), bankBranch.getBank(), bankBranch.getName());
            if (optionalBankBranch.isPresent()) {
                throw new RuntimeException("Branch Name is already exist");

            }
            optionalBankBranch2 = bankBranchRepository.getByOrganizationAndContactNumber(
                    bankBranch.getOrganization(), bankBranch.getContactNumber());
            if (optionalBankBranch2.isPresent()) {
                throw new RuntimeException("Contact Number is already exist");
            }
            optionalBankBranch3 = bankBranchRepository.getByOrganizationAndEmail(
                    bankBranch.getOrganization(), bankBranch.getEmail());
            if (optionalBankBranch3.isPresent()) {
                throw new RuntimeException("Email is already exist");
            }

            List<BankBranch> bankBranchList = bankBranchRepository.findAllByOrganizationIdAndBankIdAndAddressIgnoreCaseAndIsDeletedFalse(
                    bankBranch.getOrganization().getId(), bankBranch.getBank().getId(), bankBranch.getAddress());
            if (bankBranchList.size()>0) {
                throw new RuntimeException("Address is Duplicate");
            }


        } else {                                 // execute when data will update
            optionalBankBranch = bankBranchRepository.findByOrganizationIdAndBankIdAndIdIsNotAndNameIgnoreCaseAndIsDeletedFalse(
                    bankBranch.getOrganization().getId(), bankBranch.getBank().getId(), bankBranch.getId(), bankBranch.getName().trim());
            if (optionalBankBranch.isPresent()) {
                throw new RuntimeException("Branch Name is already exist");
            }
//            optionalBankBranch2 = bankBranchRepository.findByOrganizationAndIdIsNotAndContactNumber(
//                    bankBranch.getOrganization(), bankBranch.getId(), bankBranch.getContactNumber());
//            if (optionalBankBranch2.isPresent()) {
//                throw new RuntimeException("Contact Number is already exist");
//            }
//            optionalBankBranch3 = bankBranchRepository.getByOrganizationAndEmailAndIdIsNot(
//                    bankBranch.getOrganization(), bankBranch.getEmail(), bankBranch.getId());
//            if (optionalBankBranch3.isPresent()) {
//                throw new RuntimeException("Email is already exist");
//            }

            optionalBankBranch4 = bankBranchRepository.findByOrganizationIdAndBankIdAndAddressIgnoreCaseAndIdIsNotAndIsDeletedFalse(
                    bankBranch.getOrganization().getId(), bankBranch.getBank().getId(), bankBranch.getAddress(), bankBranch.getId());
            if (optionalBankBranch4.isPresent()) {
                throw new RuntimeException("Address is Duplicate");
            }
            if(bankBranch.getIsActive()==false){

                List<BankAccount> bankAccountList = bankAccountService.findAllActiveBankAccount(bankBranch.getId()) ;
                if (bankAccountList.size()>0) {
                    throw new RuntimeException("This Bank Branch already in use");
                }
            }
        }

        return true;
    }

    public List<BankBranch> findAllBranchByBank(Long bankId) {
        return bankBranchRepository.findByBankIdAndIsDeletedFalse(bankId);
    }
     public List<BankAccount> findAllActiveBankAccount(Long bankId){
        return bankAccountService.findAllActiveBankAccount(bankId);
     }

    public List<Map<String, Object>> getAllActiveBankListByBranch(Long bankId) {

        List<Map<String, Object>> resultList = new ArrayList<>();
        Organization organization = organizationService.getOrganizationFromLoginUser();
        List<BankBranch> bankBranchList =bankBranchRepository.findAllByOrganizationAndBankIdAndIsActiveTrueAndIsDeletedFalse(organization,bankId);
        bankBranchList.forEach( bankBranch -> {

            Map<String, Object> map = new HashMap<>();
            map.put("id", bankBranch.getId());
            map.put("name", bankBranch.getName());

            resultList.add(map);
        });

        return resultList;
    }
}
