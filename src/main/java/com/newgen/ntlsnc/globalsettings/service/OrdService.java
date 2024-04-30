package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.CommonUtilityService;
import com.newgen.ntlsnc.common.enums.CalculationType;
import com.newgen.ntlsnc.globalsettings.entity.Location;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.OverridingDiscount;
import com.newgen.ntlsnc.globalsettings.repository.OverridingDiscountRepository;
import com.newgen.ntlsnc.salesandcollection.entity.Distributor;
import com.newgen.ntlsnc.salesandcollection.entity.SalesInvoice;
import com.newgen.ntlsnc.salesandcollection.repository.DistributorRepository;
import com.newgen.ntlsnc.salesandcollection.service.DistributorService;
import com.newgen.ntlsnc.salesandcollection.service.SalesInvoiceService;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import com.newgen.ntlsnc.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author kamal
 * @Date ৪/৭/২২
 */

@Service
public class OrdService {
    @Autowired
    OrganizationService organizationService;
    @Autowired
    DistributorService distributorService;
    @Autowired
    OverridingDiscountSetupService overridingDiscountService;
    @Autowired
    LocationService locationService;
    @Autowired
    AccountingYearService accountingYearService;
    @Autowired
    SemesterService semesterService;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    OverridingDiscountRepository overridingDiscountRepository;
    @Autowired
    DistributorRepository distributorRepository;
    @Autowired
    SalesInvoiceService salesInvoiceService;


    public Map getOrdCalculatorList(Long companyId, List<Long> locationIds, String fromDate, String toDate) {
        try {
            Map response = new HashMap();

            List<Distributor> distributorListUnderLocations = distributorService.getAllByCompanyIdAndLocationIdList(companyId, locationIds);
            List<Long> distributorIdListUnderLocations = distributorListUnderLocations.stream().map(d -> d.getId()).collect(Collectors.toList());

            List<Map> ordEligibleDistributorList = getOrdEligibleDistributorList(companyId, distributorIdListUnderLocations);
            List<Long> distributorIds = ordEligibleDistributorList.stream().map(d -> Long.parseLong(d.get("distributor_id").toString())).collect(Collectors.toSet()).stream().collect(Collectors.toList());

            List<Map> distributorList = distributorService.getDistributorListWithLogo(distributorIds);

            LocalDate oneDayBeforeFromDate = DateUtil.getLocalDateFromString(fromDate, "yyyy-MM-dd").minusDays(1l);  // one day before the date
            List<Map<String, Object>> distributorsDetailListWithOpeningLedgerBalance = distributorService.getDistributorsDetailsWithOpeningLedgerBalance(distributorIds, companyId, oneDayBeforeFromDate);

            List<Map<String, Object>> distributorsDetailListPeriodicLedgerBalance = distributorService.getDistributorsDetailsWithPeriodicLedgerBalance(distributorIds, companyId,
                    DateUtil.getLocalDateFromString(fromDate, "yyyy-MM-dd"), DateUtil.getLocalDateFromString(toDate, "yyyy-MM-dd"));

            distributorList.forEach(d -> {
                Long id = Long.parseLong(d.get("id").toString());
                d.put("openingBalance", "0");
                d.put("periodicBalance", "0");
                distributorsDetailListWithOpeningLedgerBalance.forEach(p -> {
                    if (id == Long.parseLong(p.get("id").toString())) {
                        d.put("openingBalance", p.get("ledgerBalance").toString());
                    }
                });

                distributorsDetailListPeriodicLedgerBalance.forEach(p -> {
                    if (id == Long.parseLong(p.get("id").toString())) {
                        d.put("periodicBalance", p.get("ledgerBalance").toString());
                    }
                });
            });

            response.put("distributorList", distributorList);

            return response;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    public List<Map> getOrdEligibleDistributorList(Long companyId, List<Long> distributorIdList) {
        List<Map> distributorList = distributorService.getPaymentCollectionAdjustmentDistributorList(companyId, distributorIdList);

        Organization company = organizationService.findById(companyId);
        List<OverridingDiscount> overridingDiscounts = overridingDiscountService.findAllByCompany(company);

        List<Map> ordEligibleDistributorList = new ArrayList<>();
        distributorList.forEach(d -> {
            int day = Integer.parseInt(d.get("collection_duration").toString());
            LocalDate paymentDate = LocalDate.parse(d.get("payment_date").toString());
            overridingDiscounts.forEach(od -> {
                if (od.getFromDay() <= day && od.getToDay() >= day
                        && od.getSemester().getStartDate().compareTo(paymentDate) <= 0
                        && od.getSemester().getEndDate().compareTo(paymentDate) >= 0) {
                    ordEligibleDistributorList.add(d);
                }
            });
        });

        return ordEligibleDistributorList;
    }

    public Map getOrdList(
            Long companyId, Long locationId,
            Long accountingYearId, Long semesterId) {

        List<Map<String, Object>> resultList = new ArrayList<>();
        List<Long> soList = new ArrayList<>();
        Map<String, Object> returnMap = new HashMap<>();
        Map<Long, Object> childLocationMap = new HashMap<>();
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        LocalDate startDate = null;

        ApplicationUser applicationUser
                = applicationUserService.getApplicationUserFromLoginUser();

        if (companyId == null
                && applicationUser == null) {
            return null;
        }
        Long userLoginId = applicationUser.getId();
        if (accountingYearId != null) {
            Map<String, LocalDate> dateMap =
                    accountingYearService.getAccountingYearDate(accountingYearId);
            if (dateMap != null) {
                startDate = dateMap.get("startDate");
                LocalDate endDate = dateMap.get("endDate");
                startDateTime = startDate.atStartOfDay();
                endDateTime = endDate.atStartOfDay();
            }
        }

        if (semesterId != null) {
            Map<String, LocalDate> dateMap =
                    semesterService.getSemesterDate(semesterId);
            if (dateMap != null) {
                startDate = dateMap.get("startDate");
                LocalDate endDate = dateMap.get("endDate");
                startDateTime = startDate.atStartOfDay();
                endDateTime = endDate.atStartOfDay();
            }
        }

        if (locationId != null) {
            childLocationMap =
                    locationService.getChildLocationsByParent(
                            companyId, locationId, childLocationMap);
            soList = locationService.getSoListByLocation(companyId, childLocationMap);

        } else {

            Boolean isManager = applicationUserService.checkLoginUserIsManager(companyId, userLoginId);
            Boolean isSo = applicationUserService.checkLoginUserIsSo(companyId, userLoginId);

            if (isManager) {
                locationId = locationService.getManagerLocation(companyId, userLoginId);
                childLocationMap =
                        locationService.getChildLocationsByParent(
                                companyId, locationId, childLocationMap);
                soList = locationService.getSoListByLocation(companyId, childLocationMap);

            } else if (isSo) {
                soList.add(userLoginId);

            } else {
                List<Location> parentLocations =
                        locationService.getParentLocationsList(companyId);
                for (Location parentLocation : parentLocations) {
                    Map<Long, Object> childLocationMap1 =
                            locationService.getChildLocationsByParent(companyId, parentLocation.getId(),
                                    childLocationMap);
                    childLocationMap.putAll(childLocationMap1);
                }
                soList = locationService.getSoListByLocation(companyId, childLocationMap);
            }
        }

        List<Map<String, Object>> ordList = overridingDiscountRepository.getOrdList(
                companyId, startDateTime, endDateTime, soList);

        LocalDate finalStartDate = startDate;
        resultList = ordList.stream()
                .map(elt -> makeOrdMap(elt, finalStartDate))
                .collect(Collectors.toList());

        double totalCollectionAmount =
                ordList.stream().filter(o -> o.containsKey("collection_amount"))
                        .mapToDouble(o -> Double.parseDouble(o.get("collection_amount").toString())).sum();

        double totalOrdAmount =
                ordList.stream().filter(o -> o.containsKey("ord_amount"))
                        .mapToDouble(o -> Double.parseDouble(o.get("ord_amount").toString())).sum();

        returnMap.put("totalCollectionAmount", totalCollectionAmount);
        returnMap.put("totalOrdAmount", totalOrdAmount);
        returnMap.put("ordList", resultList);

        return returnMap;
    }

    private Map<String, Object> makeOrdMap (
            Map<String, Object> distributor,
            LocalDate startDate ) {

        Map<String, Object> map = new HashMap<>();
        Long distributorId = Long.parseLong(String.valueOf(distributor.get("id")));
        Long companyId = Long.parseLong(String.valueOf(distributor.get("company_id")));
        String distributorLogo =
                distributorService.getDistributorLogo(distributorId);
        map.put("distributor_id", distributorId);
        map.put("distributor_name", distributor.get("distributor_name").toString());
        map.put("adjusted_amount", distributor.get("adjusted_amount").toString());
        map.put("collection_amount", distributor.get("collection_amount").toString());
        map.put("ord_amount", distributor.get("ord_amount").toString());
        map.put("distributor_contact_no", distributor.get("contact_no").toString());
        map.put("total_invoice", distributor.get("total_invoice").toString());
        map.put("distributor_logo", distributorLogo);

        String ledgerBalance =
                String.valueOf(distributorRepository.getDistributorLedgerBalancePeriodicOrAsOnDate(companyId,
                distributorId, null, startDate));
        if (ledgerBalance == null || ledgerBalance.isEmpty()|| ledgerBalance.equals("null")) {
            map.put("ledger_balance", 0.00f);
        }
        else
            map.put("ledger_balance", Double.parseDouble(ledgerBalance));

        return map;
    }

    public Map getOrdCalculableData(Long companyId, Long distributorId, String ordCalculationDate) {
        DecimalFormat dfZero = new DecimalFormat("0.00");
        LocalDate calculationDate = DateUtil.getLocalDateFromString(ordCalculationDate, "yyyy-MM-dd");
        List<SalesInvoice> salesInvoiceList = salesInvoiceService.getOrdCalculatorEligibleInvoiceListByDistributorIdAndCompanyId(companyId, distributorId);
        List<Map> invoiceList = new ArrayList<>();

        float totalSuggestedPayment = 0.0f;
        float totalSalesAmount = 0.0f;
        float totalCommission = 0.0f;

        for (int i = 0; i < salesInvoiceList.size(); i++) {
            Long elapsedDay = DateUtil.getDaysDifferentBetweenTwoDate(calculationDate, salesInvoiceList.get(i).getInvoiceDate());

            OverridingDiscount overridingDiscount = overridingDiscountService.findByCompanyIdAndInvoiceNatureIdAndSemesterDateAndDay(companyId, salesInvoiceList.get(i).getInvoiceNature().getId(), salesInvoiceList.get(i).getInvoiceDate(), elapsedDay.intValue());
            if (overridingDiscount == null) {
                continue; // it will just stop the current iteration
            }

            Map invoice = new HashMap();

            float commission = 0.0f;
            float suggestedPayment = 0.0f;

            invoice.put("invoiceNo", salesInvoiceList.get(i).getInvoiceNo());
            invoice.put("currentBalance", salesInvoiceList.get(i).getRemainingAmount());
            invoice.put("elapsedDay", elapsedDay);
            if (overridingDiscount.getCalculationType() == CalculationType.PERCENTAGE) {
                invoice.put("ord", overridingDiscount.getOrd().toString() + "%");
                commission = (salesInvoiceList.get(i).getInvoiceAmount() - salesInvoiceList.get(i).getRemainingAmount()) >= salesInvoiceList.get(i).getVatAmount() ?
                        CommonUtilityService.calculatePercentage((salesInvoiceList.get(i).getRemainingAmount() - salesInvoiceList.get(i).getVatAmount()), overridingDiscount.getOrd().floatValue())
                        : CommonUtilityService.calculatePercentage(salesInvoiceList.get(i).getRemainingAmount(), overridingDiscount.getOrd().floatValue());
                suggestedPayment = salesInvoiceList.get(i).getRemainingAmount() - commission;
            } else {  // CalculationType.EQUAL
                invoice.put("ord", overridingDiscount.getOrd().toString());
                commission = overridingDiscount.getOrd().floatValue();
                suggestedPayment = salesInvoiceList.get(i).getRemainingAmount() - overridingDiscount.getOrd().floatValue();
            }
            invoice.put("commission", commission);
            invoice.put("suggestedPayment", suggestedPayment);

            totalCommission += commission;
            totalSuggestedPayment += suggestedPayment;
            totalSalesAmount += salesInvoiceList.get(i).getRemainingAmount();

            invoiceList.add(invoice);
        }

        Map responseData = new HashMap();
        responseData.put("invoiceList", invoiceList);
        responseData.put("totalSuggestedPayment", dfZero.format(totalSuggestedPayment));
        responseData.put("totalCommission", dfZero.format(totalCommission));
        responseData.put("totalSalesAmount", dfZero.format(totalSalesAmount));
        return responseData;
    }

}
