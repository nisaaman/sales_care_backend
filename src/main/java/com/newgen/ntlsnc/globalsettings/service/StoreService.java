package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.StoreType;
import com.newgen.ntlsnc.globalsettings.dto.StoreDto;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.Store;
import com.newgen.ntlsnc.globalsettings.repository.StoreRepository;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvTransactionDetails;
import com.newgen.ntlsnc.supplychainmanagement.service.InvTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author kamal
 * @Date ১৬/৪/২২
 */

@Service
public class StoreService implements IService<Store> {
    @Autowired
    StoreRepository storeRepository;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    InvTransactionService invTransactionService;
    @Autowired
    EnumeService enumeService;
    @Transactional
    @Override
    public Store create(Object object) {
        StoreDto storeDto = (StoreDto) object;
        Store store = new Store();

        store.setName(storeDto.getName());
        store.setShortName(storeDto.getShortName());
        store.setDescription(storeDto.getDescription());
        store.setStoreType(StoreType.valueOf(storeDto.getStoreType()));
        store.setIsActive(storeDto.getIsActive());
        store.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (!this.validate(store)) {
            return null;
        }
        return storeRepository.save(store);
    }

    @Transactional
    @Override
    public Store update(Long id, Object object) {
        StoreDto storeDto = (StoreDto) object;
        Store store = this.findById(id);

        store.setName(storeDto.getName());
        store.setShortName(storeDto.getShortName());
        store.setDescription(storeDto.getDescription());
        store.setStoreType(StoreType.valueOf(storeDto.getStoreType()));
        store.setIsActive(storeDto.getIsActive());
        store.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (!this.validate(store)) {
            return null;
        }
        return storeRepository.save(store);
    }

    @Transactional
    @Override
    public boolean delete(Long id) {
        try {

            Optional<Store> store = storeRepository.findById(id);
            if (!store.isPresent()) {
                throw new Exception("Store not exist.");
            }

            List<InvTransactionDetails> invTransactionDetailsList = invTransactionService.findAllInvTransactionDetailsByStore(store.get().getId());
            if(invTransactionDetailsList.size() > 0){
                throw new RuntimeException("This Store already in use.");
            }

            store.get().setIsDeleted(true);
            storeRepository.save(store.get());

            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Store findById(Long id) {
        try {
            Optional<Store> optionalStore = storeRepository.findByIdAndIsDeletedFalse(id);
            if (!optionalStore.isPresent()) {
                throw new Exception("Store Not exist with id " + id);
            }
            return optionalStore.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<Store> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return storeRepository.findAllByOrganizationAndIsDeletedFalseAndIsActiveTrue(organization);
    }

    @Override
    public boolean validate(Object object) {
        Store store = (Store) object;
        Optional<Store> optionalStore = Optional.empty();
        Optional<Store> optionalStore1 = Optional.empty();

        if(store.getId() == null) {
            optionalStore = storeRepository.findByOrganizationAndNameIgnoreCaseAndIsDeletedFalse(
                    store.getOrganization(), store.getName().trim());
            optionalStore1 = storeRepository.findByOrganizationAndStoreTypeAndIsDeletedFalse(store.getOrganization(),store.getStoreType());
        } else {
            optionalStore = storeRepository.findByOrganizationAndIdIsNotAndNameIgnoreCaseAndIsDeletedFalse(
                    store.getOrganization(), store.getId(), store.getName().trim());

            optionalStore1 = storeRepository.findByOrganizationAndIdIsNotAndStoreTypeAndIsDeletedFalse(store.getOrganization(),store.getId(),store.getStoreType());
        }

        if(optionalStore.isPresent()){
            throw new RuntimeException("Store name already exist.");
        }

        if(optionalStore1.isPresent()){
            throw new RuntimeException("Store type already exist.");
        }

        return true;
    }

    public Map getStore (String storeType) {
        Optional<Store> optionalStore = Optional.empty();
        Map store = storeRepository.getStore(organizationService.getOrganizationFromLoginUser().getId(), storeType);
        return store;
    }

    public Store getStoreByOrganizationAndStoreType(Organization organization, StoreType storeType) {
        try {
            Optional<Store> optionalStore = storeRepository.findByOrganizationAndStoreTypeAndIsDeletedFalse(organization, storeType);
            if (optionalStore.isPresent()) {
                return optionalStore.get();
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map<String, Object>> getStoreList(Long organizationId, List<String> storeTypes) {
        return storeRepository.findAllStoreOfAOrganization(organizationId, storeTypes);

    }
    public List<Map>  findStoreTypeSelected() {
        return enumeService.findStoreTypeSelected();

    }

}
