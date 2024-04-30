package com.newgen.ntlsnc.subscriptions.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.SubscriptionDurationType;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.subscriptions.dto.SubscriptionPackageDto;
import com.newgen.ntlsnc.subscriptions.entity.SubscriptionPackage;
import com.newgen.ntlsnc.subscriptions.repository.SubscriptionPackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author Mou
 * Created on 4/3/22 11:27 AM
 */

@Service
public class SubscriptionPackageService implements IService<SubscriptionPackage> {
    @Autowired
    SubscriptionPackageRepository subscriptionPackageRepository;

    @Override
    @Transactional
    public SubscriptionPackage create(Object object) {
        SubscriptionPackageDto subscriptionPackageDto = (SubscriptionPackageDto) object;
        SubscriptionPackage subscriptionPackage = new SubscriptionPackage();
        subscriptionPackage.setName(subscriptionPackageDto.getName());
        subscriptionPackage.setPrice(subscriptionPackageDto.getPrice());
        subscriptionPackage.setDuration(Float.parseFloat(subscriptionPackageDto.getDuration()));
        subscriptionPackage.setDurationType(SubscriptionDurationType.valueOf(subscriptionPackageDto.getDurationType()));
        subscriptionPackage.setNumberOfUnits(subscriptionPackageDto.getNumberOfUnits());
        subscriptionPackage.setNumberOfUsers(subscriptionPackageDto.getNumberOfUsers());
        if (!validate(subscriptionPackage)) {
            return null;
        }
        return subscriptionPackageRepository.save(subscriptionPackage);
    }

    @Override
    @Transactional
    public SubscriptionPackage update(Long id, Object object) {
        SubscriptionPackageDto subscriptionPackageDto = (SubscriptionPackageDto) object;
        SubscriptionPackage subscriptionPackage = this.findById(id);
        subscriptionPackage.setName(subscriptionPackageDto.getName());
        subscriptionPackage.setPrice(subscriptionPackageDto.getPrice());
        subscriptionPackage.setDuration(Float.parseFloat(subscriptionPackageDto.getDuration()));
        subscriptionPackage.setDurationType(SubscriptionDurationType.valueOf(subscriptionPackageDto.getDurationType()));
        subscriptionPackage.setNumberOfUnits(subscriptionPackageDto.getNumberOfUnits());
        subscriptionPackage.setNumberOfUsers(subscriptionPackageDto.getNumberOfUsers());
        if (!validate(subscriptionPackage)) {
            return null;
        }
        return subscriptionPackageRepository.save(subscriptionPackage);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            SubscriptionPackage subscriptionPackage = findById(id);
            subscriptionPackage.setIsDeleted(true);
            subscriptionPackageRepository.save(subscriptionPackage);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public SubscriptionPackage findById(Long id) {
        try {
            Optional<SubscriptionPackage> optionalSubscriptionPackage = subscriptionPackageRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalSubscriptionPackage.isPresent()) {
                throw new Exception("Subscription Package Not exist with id " + id);
            }
            return optionalSubscriptionPackage.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<SubscriptionPackage> findAll() {
        Organization organization = new Organization();
        return subscriptionPackageRepository.findAllByIsDeletedFalse();
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }
}
