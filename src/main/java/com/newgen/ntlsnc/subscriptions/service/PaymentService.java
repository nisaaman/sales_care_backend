package com.newgen.ntlsnc.subscriptions.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.subscriptions.dto.PaymentDto;
import com.newgen.ntlsnc.subscriptions.entity.Payment;
import com.newgen.ntlsnc.subscriptions.entity.SubscriptionPackage;
import com.newgen.ntlsnc.subscriptions.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * @author Mou
 * Created on 5/3/22 10:27 AM
 */

@Service
public class PaymentService implements IService<Payment> {
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    SubscriptionPackageService subscriptionPackageService;
    @Autowired
    OrganizationService organizationService;

    @Override
    @Transactional
    public Payment create(Object object) {
        PaymentDto paymentDto = (PaymentDto) object;
        Payment payment = new Payment();
        SubscriptionPackage subscriptionPackage = subscriptionPackageService.findById(paymentDto.getSubscriptionPackageId());
        payment.setOrganization(organizationService.getOrganizationFromLoginUser());
        payment.setName(paymentDto.getName());
        payment.setAmount(paymentDto.getAmount());
        payment.setPaymentDate(LocalDate.parse(paymentDto.getPaymentDate()));
        payment.setPaymentMethod(paymentDto.getPaymentMethod());
        payment.setSubscriptionPackage(subscriptionPackage);

        if (!validate(payment)) {
            return null;
        }
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public Payment update(Long id, Object object) {
        PaymentDto paymentDto = (PaymentDto) object;
        Payment payment = this.findById(id);
        SubscriptionPackage subscriptionPackage = subscriptionPackageService.findById(paymentDto.getSubscriptionPackageId());
        payment.setOrganization(organizationService.getOrganizationFromLoginUser());
        payment.setName(paymentDto.getName());
        payment.setAmount(paymentDto.getAmount());
        payment.setPaymentDate(LocalDate.parse(paymentDto.getPaymentDate()));
        payment.setPaymentMethod(paymentDto.getPaymentMethod());
        payment.setSubscriptionPackage(subscriptionPackage);

        if (!validate(payment)) {
            return null;
        }
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            Payment payment = findById(id);
            payment.setIsDeleted(true);
            paymentRepository.save(payment);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Payment findById(Long id) {
        try {
            Optional<Payment> optionalPayment = paymentRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalPayment.isPresent()) {
                throw new Exception("Payment Not exist with id " + id);
            }
            return optionalPayment.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<Payment> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return paymentRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }
}
