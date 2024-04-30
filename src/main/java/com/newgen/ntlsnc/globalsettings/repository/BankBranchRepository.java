package com.newgen.ntlsnc.globalsettings.repository;

import com.newgen.ntlsnc.globalsettings.entity.Bank;
import com.newgen.ntlsnc.globalsettings.entity.BankBranch;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Mou
 * Created on 5/3/22 02:08 PM
 */
@Repository
public interface BankBranchRepository extends JpaRepository<BankBranch, Long> {
    Optional<BankBranch> findByIdAndIsDeletedFalse(Long id);

    List<BankBranch> findAllByOrganizationAndIsDeletedFalseAndBankIsActiveIsTrue(Organization organization);

    List<BankBranch> findByBankIdAndIsDeletedFalse(Long bankId);

    Optional<BankBranch> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    Optional<BankBranch> findByOrganizationAndBankAndNameIgnoreCaseAndIsDeletedFalse(Organization organization, Bank bank, String name);

    Optional<BankBranch> findByOrganizationAndIdIsNotAndContactNumber(Organization organization, Long id, String contactNumber);

    Optional<BankBranch> getByOrganizationAndContactNumber(Organization organization, String contactNumber);

    Optional<BankBranch> getByOrganizationAndEmailAndIdIsNot(Organization organization, String email, Long id);

    Optional<BankBranch> getByOrganizationAndEmail(Organization organization, String email);

    Optional<BankBranch> findByOrganizationIdAndBankIdAndIdIsNotAndNameIgnoreCaseAndIsDeletedFalse(Long organizationId, Long bankId, Long id, String trim);


    List<BankBranch> findAllByOrganizationIdAndBankIdAndAddressIgnoreCaseAndIsDeletedFalse(Long organizationId, Long bankId, String address);

    List<BankBranch> findAllByOrganizationAndBankIdAndIsActiveTrueAndIsDeletedFalse(Organization organization, Long bankId);

    Optional<BankBranch> findByOrganizationIdAndBankIdAndAddressIgnoreCaseAndIdIsNotAndIsDeletedFalse(Long id, Long id1, String address, Long id2);
}
