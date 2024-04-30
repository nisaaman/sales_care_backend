package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.common.enums.BudgetType;
import com.newgen.ntlsnc.globalsettings.entity.AccountingYear;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.ProductTradePrice;
import com.newgen.ntlsnc.globalsettings.service.LocationService;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.globalsettings.service.ProductTradePriceService;
import com.newgen.ntlsnc.salesandcollection.dto.MonthWiseSalesAndCollectionBudgetDetailsDto;
import com.newgen.ntlsnc.salesandcollection.dto.MonthWiseSalesAndCollectionBudgetDto;
import com.newgen.ntlsnc.salesandcollection.entity.SalesBudget;
import com.newgen.ntlsnc.salesandcollection.entity.SalesBudgetDetails;
import com.newgen.ntlsnc.salesandcollection.repository.SalesBudgetDetailsRepository;
import com.newgen.ntlsnc.salesandcollection.repository.SalesBudgetRepository;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author anika
 * @Date ১৩/৪/২২
 */
@Service
public class MonthWiseSalesAndCollectionBudgetService implements IService<SalesBudget> {
    @Autowired
    ProductTradePriceService productTradePriceService;
    @Autowired
    SalesBudgetDetailsRepository salesBudgetDetailsRepository;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    DistributorService distributorService;
    @Autowired
    LocationService locationService;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    SalesBudgetRepository salesBudgetRepository;

    @Transactional
    @Override
    public SalesBudget create(Object object) {
        MonthWiseSalesAndCollectionBudgetDto monthWiseSalesAndCollectionBudgetDto = (MonthWiseSalesAndCollectionBudgetDto) object;
        SalesBudget salesBudget = new SalesBudget();
       // monthWiseSalesAndCollectionBudget.setMonth(monthWiseSalesAndCollectionBudgetDto.getMonth());
        //monthWiseSalesAndCollectionBudget.setYear(monthWiseSalesAndCollectionBudgetDto.getYear());
       // monthWiseSalesAndCollectionBudget.setTargetValue(monthWiseSalesAndCollectionBudgetDto.getTargetValue());
        salesBudget.setApprovalStatus(ApprovalStatus.PENDING);
        salesBudget.setBudgetDate(LocalDate.parse(monthWiseSalesAndCollectionBudgetDto.getBudgetDate()));
        salesBudget.setOrganization(organizationService.getOrganizationFromLoginUser());
        salesBudget.setTargetType(BudgetType.valueOf(monthWiseSalesAndCollectionBudgetDto.getTargetType()));

   /*     if (monthWiseSalesAndCollectionBudgetDto.getDistributorId() != null) {
            salesBudget.setDistributor(distributorService.findById(monthWiseSalesAndCollectionBudgetDto.getDistributorId()));
        }
        if (monthWiseSalesAndCollectionBudgetDto.getLocationId() != null) {
            salesBudget.setLocation(locationService.findById(monthWiseSalesAndCollectionBudgetDto.getLocationId()));
        }
        if (monthWiseSalesAndCollectionBudgetDto.getSalesOfficerId() != null) {
            salesBudget.setSalesOfficer(applicationUserService.findById(monthWiseSalesAndCollectionBudgetDto.getSalesOfficerId()));
        }*/
        if (!this.validate(salesBudget)) {
            return null;
        }
        salesBudget = salesBudgetRepository.save(salesBudget);
        List<SalesBudgetDetails> monthWiseSalesAndCollectionBudgetDetailList = getMonthWiseSalesAndCollectionBudgetDetailsList(monthWiseSalesAndCollectionBudgetDto.getMonthWiseSalesAndCollectionBudgetDetailsDtoList(), salesBudget);
        salesBudgetDetailsRepository.saveAll(monthWiseSalesAndCollectionBudgetDetailList);
        return salesBudget;
    }

    @Transactional
    @Override
    public SalesBudget update(Long id, Object object) {
        MonthWiseSalesAndCollectionBudgetDto monthWiseSalesAndCollectionBudgetDto = (MonthWiseSalesAndCollectionBudgetDto) object;
        Optional<SalesBudget> monthWiseSalesAndCollectionBudgetOptional = salesBudgetRepository.findById(id);
        if (!monthWiseSalesAndCollectionBudgetOptional.isPresent()) {
            return null;
        }
        SalesBudget salesBudget = monthWiseSalesAndCollectionBudgetOptional.get();
        //monthWiseSalesAndCollectionBudget.setMonth(monthWiseSalesAndCollectionBudgetDto.getMonth());
       // monthWiseSalesAndCollectionBudget.setYear(monthWiseSalesAndCollectionBudgetDto.getYear());
       // monthWiseSalesAndCollectionBudget.setTargetValue(monthWiseSalesAndCollectionBudgetDto.getTargetValue());
        salesBudget.setApprovalStatus(ApprovalStatus.valueOf(monthWiseSalesAndCollectionBudgetDto.getApprovalStatus()));
        salesBudget.setBudgetDate(LocalDate.parse(monthWiseSalesAndCollectionBudgetDto.getBudgetDate()));
        salesBudget.setOrganization(organizationService.getOrganizationFromLoginUser());
        salesBudget.setTargetType(BudgetType.valueOf(monthWiseSalesAndCollectionBudgetDto.getTargetType()));
        /*if (monthWiseSalesAndCollectionBudgetDto.getDistributorId() != null) {
            salesBudget.setDistributor(distributorService.findById(monthWiseSalesAndCollectionBudgetDto.getDistributorId()));
        }
        if (monthWiseSalesAndCollectionBudgetDto.getLocationId() != null) {
            salesBudget.setLocation(locationService.findById(monthWiseSalesAndCollectionBudgetDto.getLocationId()));
        }
        if (monthWiseSalesAndCollectionBudgetDto.getSalesOfficerId() != null) {
            salesBudget.setSalesOfficer(applicationUserService.findById(monthWiseSalesAndCollectionBudgetDto.getSalesOfficerId()));
        }*/
        if (!this.validate(salesBudget)) {
            return null;
        }
        salesBudget = salesBudgetRepository.save(salesBudget);
        List<SalesBudgetDetails> monthWiseSalesAndCollectionBudgetDetailList = getMonthWiseSalesAndCollectionBudgetDetailsList(monthWiseSalesAndCollectionBudgetDto.getMonthWiseSalesAndCollectionBudgetDetailsDtoList(), salesBudget);
        salesBudgetDetailsRepository.saveAll(monthWiseSalesAndCollectionBudgetDetailList);
        return salesBudget;
    }

    @Transactional
    @Override
    public boolean delete(Long id) {
        Optional<SalesBudget> monthWiseSalesAndCollectionBudgetOptional = salesBudgetRepository.findById(id);
        try {
            if (!monthWiseSalesAndCollectionBudgetOptional.isPresent()) {
                throw new Exception("Month Wise Sales And Collection Budget not exist");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        SalesBudget salesBudget = monthWiseSalesAndCollectionBudgetOptional.get();
        salesBudget.setIsDeleted(true);
        salesBudgetRepository.save(salesBudget);
        salesBudgetDetailsRepository.deleteAllByMasterId(salesBudget.getId());
        return true;
    }

//    @Override
//    public MonthWiseSalesAndCollectionBudget findById(Long id) {
//        return monthWiseSalesAndCollectionBudgetRepository.findById(id).orElse(null);
//    }

    @Override
    public SalesBudget findById(Long id) {
        try {
            Optional<SalesBudget> optionalMonthWiseSalesAndCollectionBudget =
                    salesBudgetRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalMonthWiseSalesAndCollectionBudget.isPresent()) {
                throw new Exception("Month Wise Sales And Collection Budget Not exist with id " + id);
            }
            return optionalMonthWiseSalesAndCollectionBudget.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    public List<SalesBudget> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return salesBudgetRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    private List<SalesBudgetDetails> getMonthWiseSalesAndCollectionBudgetDetailsList(List<MonthWiseSalesAndCollectionBudgetDetailsDto> monthWiseSalesAndCollectionBudgetDetailsDtoList, SalesBudget salesBudget) {
        List<SalesBudgetDetails> salesBudgetDetailsList = new ArrayList<>();
        for (MonthWiseSalesAndCollectionBudgetDetailsDto mod : monthWiseSalesAndCollectionBudgetDetailsDtoList) {
            SalesBudgetDetails salesBudgetDetails = new SalesBudgetDetails();
            if (mod.getId() != null) {
                salesBudgetDetails = salesBudgetDetailsRepository.findById(mod.getId()).get();
            }
            //salesBudgetDetails.setQuantity(mod.getQuantity());
            salesBudgetDetails.setSalesBudget(salesBudget);
            if (mod.getProductTradePriceId() != null) {
                ProductTradePrice productTradePrice = productTradePriceService.findById(mod.getProductTradePriceId());
                //salesBudgetDetails.setProductTradePrice(productTradePrice);
                salesBudgetDetails.setProduct(productTradePrice.getProduct());
            }
            if (mod.getMonthWiseSalesAndCollectionBudgetId() != null) {
                salesBudgetDetails.setSalesBudget(salesBudgetRepository.findById(mod.getMonthWiseSalesAndCollectionBudgetId()).orElse(null));
            }

            salesBudgetDetailsList.add(salesBudgetDetails);
        }
        return salesBudgetDetailsList;
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }

    public Double getSalesTarget(Long accountingYearId, Long companyId, List<Integer> monthList){
        return salesBudgetRepository.getTotalSalesTarget(accountingYearId,companyId,monthList, ApprovalStatus.APPROVED.toString());
    }
}
