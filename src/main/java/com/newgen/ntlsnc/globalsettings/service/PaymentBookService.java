package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.dto.PaymentBookDto;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.PaymentBook;
import com.newgen.ntlsnc.globalsettings.entity.Store;
import com.newgen.ntlsnc.globalsettings.repository.PaymentBookRepository;
import com.newgen.ntlsnc.salesandcollection.entity.PaymentCollection;
import com.newgen.ntlsnc.salesandcollection.service.PaymentCollectionService;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author anika
 * @Date ১/৬/২২
 */
@Service
public class PaymentBookService implements IService<PaymentBook> {

    @Autowired
    OrganizationService organizationService;
    @Autowired
    LocationService locationService;

    @Autowired
    PaymentBookRepository paymentBookRepository;
    @Autowired
    LocationManagerMapService locationManagerMapService;
    @Autowired
    ApplicationUserService applicationUserService;
    @Transactional
    @Override
    public PaymentBook create(Object object) {

        PaymentBookDto paymentBookDto = (PaymentBookDto) object;
        PaymentBook paymentBook = new PaymentBook();

        paymentBook.setBookNumber(paymentBookDto.getBookNumber());
        paymentBook.setIsActive(paymentBookDto.getStatus());
        paymentBook.setFromMrNo(paymentBookDto.getFromMrNo());
        paymentBook.setToMrNo(paymentBookDto.getToMrNo());
        if(paymentBookDto.getIssueDate() == null){
            paymentBook.setIssueDate(LocalDate.now());
        }else {
            paymentBook.setIssueDate(LocalDate.parse(paymentBookDto.getIssueDate()));
        }
        paymentBook.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (paymentBookDto.getCompanyId() != null) {
            paymentBook.setCompany(organizationService.findById(paymentBookDto.getCompanyId()));
        }

        if (paymentBookDto.getPaymentBookLocationId() != null) {
            paymentBook.setPaymentBookLocation(locationService.findById(paymentBookDto.getPaymentBookLocationId()));
        }
        Optional<PaymentBook> paymentBookOptional = paymentBookRepository.findByCompanyIdAndPaymentBookLocationIdAndIsActiveTrue(paymentBookDto.getCompanyId(),paymentBookDto.getPaymentBookLocationId());
        if(paymentBookOptional.isPresent()){
            if(paymentBookDto.getStatus()){
                throw new RuntimeException("Payment Book No. "+paymentBookOptional.get().getBookNumber() +" is Active . Please First inactive this payment book!");
            }
        }

        if (!this.validate(paymentBook)) {
            return null;
        }

        return paymentBookRepository.save(paymentBook);
    }

    @Transactional
    @Override
    public PaymentBook update(Long id, Object object) {

        try{
            PaymentBookDto paymentBookDto = (PaymentBookDto) object;
            Optional<PaymentBook> paymentBookOptional = paymentBookRepository.findByCompanyIdAndPaymentBookLocationIdAndIsActiveTrue(paymentBookDto.getCompanyId(),paymentBookDto.getPaymentBookLocationId());
            if(paymentBookOptional.isPresent()){
                if(paymentBookDto.getStatus()){
                    throw new RuntimeException("Payment Book No. "+paymentBookOptional.get().getBookNumber() +" is Active . Please First inactive this payment book!");
                }
            }
            PaymentBook paymentBook = paymentBookRepository.findByIdAndIsDeletedFalse(paymentBookDto.getId());

            paymentBook.setBookNumber(paymentBookDto.getBookNumber());
            paymentBook.setIsActive(paymentBookDto.getStatus());
            paymentBook.setFromMrNo(paymentBookDto.getFromMrNo());
            paymentBook.setToMrNo(paymentBookDto.getToMrNo());
            paymentBook.setIssueDate(LocalDate.parse(paymentBookDto.getIssueDate()));
            Organization organization = organizationService.getOrganizationFromLoginUser();
            paymentBook.setOrganization(organization);

            if (paymentBookDto.getCompanyId() != null) {
                paymentBook.setCompany(organizationService.findById(paymentBookDto.getCompanyId()));
            }

            if (paymentBookDto.getPaymentBookLocationId() != null) {
                paymentBook.setPaymentBookLocation(locationService.findById(paymentBookDto.getPaymentBookLocationId()));
            }

//            List<PaymentCollection> paymentCollectionList = paymentCollectionService.findByPaymentBookId(organization, paymentBookDto.getCompanyId(),id);
//            if(paymentCollectionList.size()>0){
//                throw new Exception("Payment Book already in use");
//            }
            if (!this.validate(paymentBook)) {
                return null;
            }
            return paymentBookRepository.save(paymentBook);
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }


    }

    @Transactional
    @Override
    public boolean delete(Long id) {
        try {
            Optional<PaymentBook> optionalPaymentBook = paymentBookRepository.findById(id);
            if (!optionalPaymentBook.isPresent()) {
                throw new Exception("Payment Book Not exist");
            }
            PaymentBook paymentBook = optionalPaymentBook.get();
            paymentBook.setIsDeleted(true);
            paymentBookRepository.save(paymentBook);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    @Override
    public PaymentBook findById(Long id) {
        try {
            Optional<PaymentBook> optionalPaymentBook = paymentBookRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalPaymentBook.isPresent()) {
                throw new Exception("Payment Book Not exist with id " + id);
            }
            return optionalPaymentBook.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    @Override
    public List<PaymentBook> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return paymentBookRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Transactional
    @Override
    public boolean validate(Object object) {
        PaymentBook paymentBook = (PaymentBook) object;
        Optional<PaymentBook> optionalPaymentBook = Optional.empty();

        if(paymentBook.getId() == null) {
            optionalPaymentBook = paymentBookRepository.findByOrganizationAndCompanyAndBookNumberIgnoreCaseAndIsDeletedFalse(
                    paymentBook.getOrganization(),paymentBook.getCompany(), paymentBook.getBookNumber().trim());
        } else {
            optionalPaymentBook = paymentBookRepository.findByOrganizationAndCompanyAndIdIsNotAndBookNumberIgnoreCaseAndIsDeletedFalse(
                    paymentBook.getOrganization(),paymentBook.getCompany(), paymentBook.getId(), paymentBook.getBookNumber().trim());
        }

        if(optionalPaymentBook.isPresent()){
            throw new RuntimeException("Payment Book Number already exist.");
        }
        return true;
    }

    @Transactional
    public PaymentBook findPaymentBookByCompanyId(Long companyId) {
        try {
            ApplicationUser applicationUser= applicationUserService.getApplicationUserFromLoginUser();
            Organization organization = organizationService.getOrganizationFromLoginUser();
            Long locationId = locationManagerMapService.getLoggedInUserLocationId(applicationUser.getId(), organization.getId(), companyId);
            Optional<PaymentBook> optionalPaymentBook = paymentBookRepository.findByCompanyIdAndPaymentBookLocationIdAndIsActiveTrue(companyId,locationId);
            if (!optionalPaymentBook.isPresent()) {
                throw new Exception("Payment Book Not exist with company id " + companyId);
            }
            return optionalPaymentBook.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map> findAllByCompanyIdAndLocationId(Long companyId, Long paymentBookLocationId) {
        return paymentBookRepository.findAllByCompanyIdAndPaymentBookLocationId(companyId,paymentBookLocationId);
    }

    @Transactional
    public PaymentBook updateActiveStatus(Long id) {
        Optional<PaymentBook> optionalPaymentBook = paymentBookRepository.findById(id);

        try {
            if (!optionalPaymentBook.isPresent()) {
                throw new Exception("Payment Book does not exist!");
            }

            Optional<PaymentBook> paymentBookOptional = paymentBookRepository.findByCompanyIdAndPaymentBookLocationIdAndIsActiveTrue(optionalPaymentBook.get().getCompany().getId(),optionalPaymentBook.get().getPaymentBookLocation().getId());

                if(paymentBookOptional.isPresent()){
                    if(!optionalPaymentBook.get().getIsActive()){
                    throw new Exception("Payment Book No. "+paymentBookOptional.get().getBookNumber() +" is Active . Please First inactive this payment book!");
                    }
                }

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        PaymentBook paymentBook = optionalPaymentBook.get();
        paymentBook.setIsActive(!paymentBook.getIsActive());
        return paymentBookRepository.save(paymentBook);
    }

    public PaymentBook findPaymentBookByCompanyAndLocation(Long companyId,Long locationId) {
        try {
                Optional<PaymentBook> optionalPaymentBook = paymentBookRepository.findByCompanyIdAndPaymentBookLocationIdAndIsActiveTrue(companyId, locationId);
            return optionalPaymentBook.orElse(null);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public PaymentBook updateToMrNo(PaymentBook paymentBook) {
        return paymentBookRepository.save(paymentBook);
    }

    public Long findLatestMoneyReceiptNo(Long paymentBookId) {
        try {
            Long moneyReceiptNo =
                    paymentBookRepository.findLatestMoneyReceiptNo(paymentBookId);
            if (null == moneyReceiptNo) {
                return new Long(1);
            }
            return ++moneyReceiptNo;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
