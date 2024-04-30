package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.CommonUtilityService;
import com.newgen.ntlsnc.common.enums.OverDueInterval;
import com.newgen.ntlsnc.globalsettings.service.AccountingYearService;
import com.newgen.ntlsnc.globalsettings.service.DepotLocationMapService;
import com.newgen.ntlsnc.globalsettings.service.DepotService;
import com.newgen.ntlsnc.globalsettings.service.LocationService;
import com.newgen.ntlsnc.salesandcollection.dto.SalesInvoiceOverview;
import com.newgen.ntlsnc.salesandcollection.dto.SalesInvoicesDetails;
import com.newgen.ntlsnc.salesandcollection.repository.SalesInvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/**
 * @author Newaz Sharif
 * @since 9th Aug, 22
 */

@Service
public class SalesInvoiceOverviewService {

    @Autowired
    SalesInvoiceRepository salesInvoiceRepository;
    @Autowired
    DistributorService distributorService;
    @Autowired
    DistributorCompanyMapService distributorCompanyMapService;
    @Autowired
    LocationService locationService;
    @Autowired
    AccountingYearService accountingYearService;
    @Autowired
    DepotService depotService;
    @Autowired
    DepotLocationMapService depotLocationMapService;

    public SalesInvoiceOverview getDistributorWiseSalesInvoiceOverview(
            Long companyId, Long locationId, String asOnDateStr,
            Long userLoginId, Integer dueStatusValue) {

        List<Map<String,Object>> resultList = new ArrayList<>();
        List<Long> distributors = new ArrayList<>();;
        LocalDate asOnDate;
        Map depotMap = depotService.getDepotByLoginUserId(companyId,userLoginId);
        Long depotId = !depotMap.isEmpty() ? Long.parseLong(String.valueOf(depotMap.get("id"))) : null;

        if (locationId == null && depotId != null) {

            List<Map> locationListMap = depotLocationMapService.getLocationListOfDepot(companyId, depotId);
            for (Map location : locationListMap) {
                Long depotLocation = !location.isEmpty() ? Long.parseLong(
                        String.valueOf(location.get("id"))) : null;

                distributors.addAll(distributorService.getDistributorListFromSalesOfficer(
                        locationService.getLocationManager(companyId, depotLocation), companyId));

               if(distributors == null)
                    distributors = distributorCompanyMapService.getDistributorListFromCompany(companyId);
            }

//            distributors = distributorService.getDistributorListFromSalesOfficer(
//                    userLoginId, companyId);

        } else if(locationId == null && depotId == null) {
            distributors = distributorCompanyMapService.getDistributorListFromCompany(companyId);

        } else {
            distributors = distributorService.getDistributorListFromSalesOfficer(
                   userLoginId, companyId);
            if(distributors == null)
                    distributors = distributorService.getDistributorListFromSalesOfficer(
                        locationService.getLocationManager(companyId, locationId), companyId);
        }

        if(asOnDateStr == null){
            asOnDate = LocalDate.now();
        } else {
            asOnDate = LocalDate.parse(asOnDateStr);
        }

        List<Map<String,Object>> distributorWiseSalesInvoices = salesInvoiceRepository
                .getDistributorWiseSalesInvoiceOverview(companyId, distributors,
                        asOnDate, dueStatusValue);

        return new SalesInvoiceOverview(distributorWiseSalesInvoices.stream()
                                           .filter(inv -> Objects.nonNull(inv.get("ledgerBalance")))
                                           .mapToDouble(inv -> (double) inv.get("ledgerBalance"))
                                           .sum(), distributorWiseSalesInvoices);

    }
    public SalesInvoicesDetails getDistributorSalesInvoiceDetails(
            Long distributorId, Long companyId, String invoiceNature,
            List<String> overDueIntervals, LocalDate asOnDate, Integer notDueStatus,
            String isAcknowledged) {

        final List<Map<String,Object>> resultList = new ArrayList<>();

        Map<String, List<Integer>> invOverDueIntervalMap = CommonUtilityService
                                .invoiceOverDueIntervalMap.get();

        if(overDueIntervals == null || overDueIntervals.size() == 0) {
            resultList.addAll(salesInvoiceRepository
                    .getDistributorInvoicesDetails(distributorId, companyId,
                            invoiceNature,
                            invOverDueIntervalMap.get(OverDueInterval.DEFAULT.getCode()).get(0),
                            invOverDueIntervalMap.get(OverDueInterval.DEFAULT.getCode()).get(1), asOnDate,notDueStatus,
                            isAcknowledged));
        }else {
            overDueIntervals.forEach(intv -> {
                List<Integer> intervalDays = invOverDueIntervalMap.get(intv);
                resultList.addAll(salesInvoiceRepository
                        .getDistributorInvoicesDetails(distributorId, companyId,
                                invoiceNature, intervalDays.get(0), intervalDays.get(1), asOnDate, 0,
                                isAcknowledged));
            });
        }
        SalesInvoicesDetails salesInvoicesDetails =  new SalesInvoicesDetails();
        salesInvoicesDetails.setTotalInvoiceAmount(resultList.parallelStream()
                .mapToDouble(inv -> Double.parseDouble(String.valueOf(inv.get("invoiceAmount"))))
                .sum());
        salesInvoicesDetails.setTotalInvoiceBalance(resultList.parallelStream()
                .mapToDouble(inv -> Double.parseDouble(String.valueOf(inv.get("invoiceBalance"))))
                .sum());
        salesInvoicesDetails.setTotalOrdAmount(resultList.parallelStream()
                .mapToDouble(inv -> Double.parseDouble(String.valueOf(inv.get("ordAmount"))))
                .sum());
        salesInvoicesDetails.setSalesInvoicesDetails(resultList);

        salesInvoicesDetails.setTotalOverdueAmount(
        resultList.parallelStream()
                .filter(inv -> Integer.parseInt(String.valueOf(inv.get("overDueDays"))) > 0)
                .mapToDouble(inv -> Double.parseDouble(String.valueOf(inv.get("invoiceBalance"))))
                .sum());

        return salesInvoicesDetails;
    }

}
