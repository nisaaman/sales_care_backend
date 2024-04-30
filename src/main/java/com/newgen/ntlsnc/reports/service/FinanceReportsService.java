package com.newgen.ntlsnc.reports.service;

import com.newgen.ntlsnc.common.CommonUtilityService;
import com.newgen.ntlsnc.globalsettings.repository.LocationRepository;
import com.newgen.ntlsnc.globalsettings.service.LocationService;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.salesandcollection.repository.SalesInvoiceRepository;
import com.newgen.ntlsnc.salesandcollection.service.DistributorService;
import com.newgen.ntlsnc.salesandcollection.service.SalesInvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author sunipa
 * @date 26/09/22
 * @time 4:18 PM
 */
@Service
public class FinanceReportsService {
    @Autowired
    DistributorService distributorService;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    SalesInvoiceService salesInvoiceService;
    @Autowired
    SalesInvoiceRepository salesInvoiceRepository;
    @Autowired
    LocationService locationService;
    @Autowired
    LocationRepository locationRepository;
    public List<Map<String, Object>> getReceivableInvoiceStatementReport(
            Long companyId, Long invoiceNatureId, Long distributorId, LocalDate startDate,
            LocalDate endDate) {

        List<Map<String, Object>> reportRowList = new ArrayList<>();
        LocalDate asOnDate = LocalDate.now();

        try {
            List<Map<String, Object>> distributorList =
                    distributorService.getDistributorListOfCompany(companyId, null, distributorId);

           /*List<Object> distributorIds =
                    distributorList.stream().
                            filter(l -> l.containsKey("distributorId")).
                            collect(Collectors.toCollection(ArrayList::new));

            List<Long> distributorIdLs   = distributorIds.stream()
                    .map(emp->new Long(emp.toString()))
                    .collect(Collectors.toList());*/

            for (Map distributor : distributorList) {
                List<Map<String, Object>> resultList = new ArrayList<>();
                distributorId = Long.parseLong(distributor.get("distributorId").toString());

                List<Map<String, Object>> salesInvoiceList =
                        salesInvoiceRepository.getDistributorReceivableInvoicesDetails(
                                companyId, startDate, endDate, invoiceNatureId, distributorId,
                                asOnDate);
                List<Map<String, Object>> rowList = salesInvoiceList.stream()
                        .map(invoice -> makeInvoiceRowMap(invoice,
                                distributor))
                        .collect(Collectors.toCollection(ArrayList::new));
                if (rowList.size() > 0)
                    reportRowList.addAll(rowList);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return reportRowList;
    }

    private Map<String, Object> makeInvoiceRowMap(Map<String, Object> invoice,
                                                  Map distributor) {
        Map<String, List<Integer>> invOverDueIntervalMap = CommonUtilityService
                .invoiceOverDueIntervalMap.get();
        Map map = new HashMap<>();

        Integer asOnDiffDays = Integer.parseInt(invoice.get("asOnDiffDays").toString());
        Integer overdueDays = Integer.parseInt(invoice.get("overdueDays").toString());
        Double invoiceAmount = Double.parseDouble(invoice.get("invoice_amount").toString());
        Integer notdueDays = Integer.parseInt(invoice.get("notdueDays").toString());

        for (List<Integer> intervalDays : invOverDueIntervalMap.values()) {
            if (overdueDays >= intervalDays.get(0) && overdueDays <= intervalDays.get(1)) {
                map.put("level-" + intervalDays.get(0),
                        Double.parseDouble(invoice.get("remaining_amount").toString()));
            }
        }

        map.putAll(invoice);
        map.putAll(distributor);
        map.put("notdueDays", notdueDays);
        map.put("asOnDiffDays", asOnDiffDays);


        return map;
    }

    public List<Map<String, Object>> getInvoiceTypeWiseSummaryReport(
            Long companyId, Long salesOfficerId, LocalDate startDate, LocalDate endDate,
            Long distributorId, List<Long> locationIds) {

        LocalDate asOnDate = LocalDate.now();
        try {
            List<Map<String, Object>> salesInvoiceList = new ArrayList<>();

                    /*salesInvoiceRepository.getSoReceivableInvoicesDetails(
                            companyId, salesOfficerId, locationIds, startDate, endDate,
                            distributorId, asOnDate);*/

            return salesInvoiceList;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map<String, Object>> getInvoiceAgingData(
            Long companyId, List<Long> locationIds,
            List<Long> salesOfficerIds, List<Long> distributorIds,
            LocalDate startDate, LocalDate endDate, String reportType) {
        //List<Long> categoryIds, Long categoryTypeLevel, List<Long> productIds,

        List<Map<String, Object>> salesInvoiceList = new ArrayList<>();
        LocalDate asOnDate = LocalDate.now();

        /*Map<Long, Object> childLocationMap = new HashMap<>();
        List<Long> salesOfficerIdList = new ArrayList<>();
        if (locationId != null) {
            childLocationMap =
                    locationService.getChildLocationsByParent(
                            companyId, locationId, childLocationMap);
        }
        else {
            List<Location> parentLocations =
                    locationService.getParentLocationsList(companyId);
            for (Location parentLocation : parentLocations) {
                Map<Long, Object> childLocationMap1 =
                        locationService.getChildLocationsByParent(companyId, parentLocation.getId(),
                                childLocationMap);
                childLocationMap.putAll(childLocationMap1);
            }
        }

        if (salesOfficerId == null) {
            salesOfficerIdList = locationService.getSoListByLocation(companyId, childLocationMap);
        }
        else {
            salesOfficerIdList.add(salesOfficerId);
        }
        List<Long> locationIdList = new ArrayList(childLocationMap.keySet());*/
        locationRepository.SNC_CHILD_LOCATION_HIERARCHY(
                Long.parseLong(String.valueOf(companyId)));
        try {
            if ("SUMMARY".equals(reportType)) {
                /*if (categoryTypeLevel >0)
                    salesInvoiceList = salesInvoiceRepository.getInvoiceAgingDataProduct(companyId, locationIds,
                            categoryIds, productIds, salesOfficerIds, distributorIds,
                            startDate, endDate, asOnDate);
                else*/
                    salesInvoiceList = salesInvoiceRepository.getInvoiceAgingSummary(
                        companyId, locationIds, salesOfficerIds, distributorIds,
                        startDate, endDate, asOnDate);
            } else
                salesInvoiceList =
                        salesInvoiceRepository.getInvoiceAgingData(companyId, locationIds,
                                 salesOfficerIds, distributorIds,
                                    startDate, endDate, asOnDate);

            return salesInvoiceList;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
