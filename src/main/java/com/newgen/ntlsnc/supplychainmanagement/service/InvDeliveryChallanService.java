package com.newgen.ntlsnc.supplychainmanagement.service;

import com.newgen.ntlsnc.common.CommonConstant;
import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.InvTransactionType;
import com.newgen.ntlsnc.common.enums.PickingStatus;
import com.newgen.ntlsnc.common.enums.VehicleOwnership;
import com.newgen.ntlsnc.common.enums.VehicleType;
import com.newgen.ntlsnc.globalsettings.dto.VehicleDto;
import com.newgen.ntlsnc.globalsettings.entity.*;
import com.newgen.ntlsnc.globalsettings.service.*;
import com.newgen.ntlsnc.salesandcollection.entity.Distributor;
import com.newgen.ntlsnc.salesandcollection.service.DistributorService;
import com.newgen.ntlsnc.salesandcollection.service.SalesOrderService;
import com.newgen.ntlsnc.supplychainmanagement.dto.InvDeliveryChallanDto;
import com.newgen.ntlsnc.supplychainmanagement.dto.InvTransactionDetailsDto;
import com.newgen.ntlsnc.supplychainmanagement.dto.InvTransactionDto;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvDeliveryChallan;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvTransaction;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvTransactionDetails;
import com.newgen.ntlsnc.supplychainmanagement.repository.InvDeliveryChallanRepository;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author anika
 * @Date ১৭/৪/২২
 */
@Service
public class InvDeliveryChallanService implements IService<InvDeliveryChallan> {

    @Autowired
    InvDeliveryChallanRepository invDeliveryChallanRepository;
    @Autowired
    InvTransactionService invTransactionService;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    DepotService depotService;
    @Autowired
    VehicleService vehicleService;
    @Autowired
    SalesOrderService salesOrderService;
    @Autowired
    BatchService batchService;
    @Autowired
    StoreService storeService;
    @Autowired
    ProductService productService;
    @Autowired
    DistributorService distributorService;
    @Autowired
    AccountingYearService accountingYearService;

    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    LocationService locationService;
    @Autowired
    DocumentSequenceService documentSequenceService;
    @Autowired
    LocationTypeService locationTypeService;
    @Autowired
    ReportService reportService;


    @Override
    @Transactional
    public InvDeliveryChallan create(Object object) {

        InvDeliveryChallanDto invDeliveryChallanDto = (InvDeliveryChallanDto) object;
        InvDeliveryChallan invDeliveryChallan = new InvDeliveryChallan();

        //====================================== InvTransaction part start =================
        InvTransactionDto invTransactionDto = (InvTransactionDto) invDeliveryChallanDto.getInvTransactionDto();
        InvTransaction invTransaction = new InvTransaction();
        invTransaction.setTransactionDate(LocalDate.parse(invTransactionDto.getTransactionDate()));
        invTransaction.setTransactionType(InvTransactionType.DELIVERY_CHALLAN);
        invTransaction.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (invDeliveryChallanDto.getCompanyId() != null) {
            invTransaction.setCompany(organizationService.findById(invDeliveryChallanDto.getCompanyId()));
        }
        if (!this.validate(invTransaction)) {
            return null;
        }
        invTransaction = invTransactionService.save(invTransaction);

        //====================================== InvTransaction part end =================

        invDeliveryChallan.setRemarks(invDeliveryChallanDto.getRemarks());
        invDeliveryChallan.setDriverContactNo(invDeliveryChallanDto.getDriverContactNo());
        invDeliveryChallan.setDriverName(invDeliveryChallanDto.getDriverName());
        invDeliveryChallan.setVatChallanNo(invDeliveryChallanDto.getVatChallanNo());
        invDeliveryChallan.setDeliveryDate(LocalDate.parse(invDeliveryChallanDto.getDeliveryDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        invDeliveryChallan.setInvTransaction(invTransaction);
        invDeliveryChallan.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (invDeliveryChallanDto.getCompanyId() != null) {
            invDeliveryChallan.setCompany(organizationService.findById(invDeliveryChallanDto.getCompanyId()));
        }

        if (invDeliveryChallanDto.getDepotId() != null) {
            invDeliveryChallan.setDepot(depotService.findById(invDeliveryChallanDto.getDepotId()));
        }
        if (invDeliveryChallanDto.getVehicleId() != null) {
            invDeliveryChallan.setVehicle(vehicleService.findById(invDeliveryChallanDto.getVehicleId()));
        }

        if (invDeliveryChallanDto.getDistributorId() != null) {
            Distributor distributor = distributorService.findById(invDeliveryChallanDto.getDistributorId());
            invDeliveryChallan.setDistributor(distributor);
        }

        //List<InvTransactionDetails> invTransactionDetailsList =
        // getInvTransactionDetails(invDeliveryChallanDto.getInvTransactionDetailsDtoList(), invTransaction);
        //invTransactionService.saveAllTransactionDetails(invTransactionDetailsList);

        return invDeliveryChallanRepository.save(invDeliveryChallan);
    }

    public InvDeliveryChallan create(Object object, InvTransaction invTransaction) {

        InvDeliveryChallanDto invDeliveryChallanDto = (InvDeliveryChallanDto) object;
        InvDeliveryChallan invDeliveryChallan = new InvDeliveryChallan();

        invDeliveryChallan.setChallanNo(
                documentSequenceService.getSequenceByDocumentId(
                        CommonConstant.DOCUMENT_ID_FOR_DELIVERY_CHALLAN));

        invDeliveryChallan.setRemarks(invDeliveryChallanDto.getRemarks());
        invDeliveryChallan.setDriverContactNo(invDeliveryChallanDto.getDriverContactNo());
        invDeliveryChallan.setDriverName(invDeliveryChallanDto.getDriverName());
        invDeliveryChallan.setVatChallanNo(invDeliveryChallanDto.getVatChallanNo());
        invDeliveryChallan.setDeliveryDate(LocalDate.parse(invDeliveryChallanDto.getDeliveryDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        invDeliveryChallan.setInvTransaction(invTransaction);
        invDeliveryChallan.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (invDeliveryChallanDto.getCompanyId() != null) {
            invDeliveryChallan.setCompany(organizationService.findById(invDeliveryChallanDto.getCompanyId()));
        }

        if (invDeliveryChallanDto.getDepotId() != null) {
            invDeliveryChallan.setDepot(depotService.findById(invDeliveryChallanDto.getDepotId()));
        }
        if (invDeliveryChallanDto.getVehicleId() != null) {
            invDeliveryChallan.setVehicle(vehicleService.findById(invDeliveryChallanDto.getVehicleId()));
        } else{
            VehicleDto vehicleDto = new VehicleDto();
            vehicleDto.setRegistrationNo(invDeliveryChallanDto.getCustomVehicleNo());
            vehicleDto.setVehicleType(String.valueOf(VehicleType.N_A));
            vehicleDto.setVehicleOwnership(String.valueOf(VehicleOwnership.RENTAL));
            vehicleDto.setVehicleHeight(0.00F);
            vehicleDto.setVehicleWidth(0.00F);
            vehicleDto.setVehicleDepth(0.00F);
            vehicleDto.setIsActive(true);
            invDeliveryChallan.setVehicle(vehicleService.create(vehicleDto));

        }

        if (invDeliveryChallanDto.getDistributorId() != null) {
            Distributor distributor = distributorService.findById(invDeliveryChallanDto.getDistributorId());
            invDeliveryChallan.setDistributor(distributor);
        }

        return invDeliveryChallanRepository.save(invDeliveryChallan);
    }

    @Transactional
    @Override
    public InvDeliveryChallan update(Long id, Object object) {
        InvDeliveryChallanDto invDeliveryChallanDto = (InvDeliveryChallanDto) object;
        InvTransaction invTransaction = invTransactionService.findById(invDeliveryChallanDto.getInvTransactionId());
        if (invTransaction == null) {
            return null;
        }

//====================================== InvTransaction part start =================
        InvTransactionDto invTransactionDto = (InvTransactionDto) invDeliveryChallanDto.getInvTransactionDto();
        invTransaction.setTransactionDate(LocalDate.parse(invTransactionDto.getTransactionDate()));
        if (!this.validate(invTransaction)) {
            return null;
        }
        invTransaction = invTransactionService.save(invTransaction);

//====================================== InvTransaction part end =================

//====================================== InvDeliveryChallan part start =================


        Optional<InvDeliveryChallan> invDeliveryChallanOptional = invDeliveryChallanRepository.findById(invDeliveryChallanDto.getId());
        if (!invDeliveryChallanOptional.isPresent()) {
            return null;
        }
        InvDeliveryChallan invDeliveryChallan = invDeliveryChallanOptional.get();
        invDeliveryChallan.setRemarks(invDeliveryChallanDto.getRemarks());
        invDeliveryChallan.setDriverContactNo(invDeliveryChallanDto.getDriverContactNo());
        invDeliveryChallan.setDriverName(invDeliveryChallanDto.getDriverName());
        invDeliveryChallan.setVatChallanNo(invDeliveryChallanDto.getVatChallanNo());
        invDeliveryChallan.setDeliveryDate(LocalDate.parse(invDeliveryChallanDto.getDeliveryDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        invDeliveryChallan.setInvTransaction(invTransaction);
        invDeliveryChallan.setOrganization(organizationService.getOrganizationFromLoginUser());
        if (invDeliveryChallanDto.getCompanyId() != null) {
            Organization company = organizationService.findById(invDeliveryChallanDto.getCompanyId());
            invDeliveryChallan.setCompany(company);
        }

        if (invDeliveryChallanDto.getDepotId() != null) {
            Depot depot = depotService.findById(invDeliveryChallanDto.getDepotId());
            invDeliveryChallan.setDepot(depot);
        }
        if (invDeliveryChallanDto.getVehicleId() != null) {
            Vehicle vehicle = vehicleService.findById(invDeliveryChallanDto.getVehicleId());
            invDeliveryChallan.setVehicle(vehicle);
        }
        else{
            VehicleDto vehicleDto = new VehicleDto();
            vehicleDto.setRegistrationNo(invDeliveryChallanDto.getCustomVehicleNo());
            vehicleDto.setVehicleType(String.valueOf(VehicleType.N_A));
            vehicleDto.setVehicleOwnership(String.valueOf(VehicleOwnership.RENTAL));
            vehicleDto.setVehicleHeight(0.00F);
            vehicleDto.setVehicleWidth(0.00F);
            vehicleDto.setVehicleDepth(0.00F);
            vehicleDto.setIsActive(true);
            invDeliveryChallan.setVehicle(vehicleService.create(vehicleDto));

        }
//        if (invDeliveryChallanDto.getSalesOrderId() != null) {
//            SalesOrder salesOrder = salesOrderService.findById(invDeliveryChallanDto.getSalesOrderId());
//            invDeliveryChallan.setSalesOrder(salesOrder);
//        }
        if (invDeliveryChallanDto.getDistributorId() != null) {
            Distributor distributor = distributorService.findById(invDeliveryChallanDto.getDistributorId());
            invDeliveryChallan.setDistributor(distributor);
        }
        if (!this.validate(invDeliveryChallan)) {
            return null;
        }
        invDeliveryChallan = invDeliveryChallanRepository.save(invDeliveryChallan);

        List<InvTransactionDetails> invTransactionDetailsList = getInvTransactionDetails(invDeliveryChallanDto.getInvTransactionDetailsDtoList(), invTransaction);
        invTransactionService.saveAllTransactionDetails(invTransactionDetailsList);
        return invDeliveryChallan;
    }

    // ============================================ InvReceive start=======================================
    @Transactional
    @Override
    public boolean delete(Long id) {
        Optional<InvDeliveryChallan> invDeliveryChallanOptional = invDeliveryChallanRepository.findById(id);
        try {
            if (!invDeliveryChallanOptional.isPresent()) {
                throw new Exception("Inventory Delivery Challan not exist");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        InvDeliveryChallan invDeliveryChallan = invDeliveryChallanOptional.get();
        invDeliveryChallan.setIsDeleted(true);
        invDeliveryChallanRepository.save(invDeliveryChallan);
        // ============================================ InvReceive end =======================================

        // ============================================ InvTransaction start =======================================

        InvTransaction invTransaction = invDeliveryChallanOptional.get().getInvTransaction();
        invTransaction.setIsDeleted(true);
        invTransactionService.save(invTransaction);

        //============================================ InvTransaction end =======================================
        List<InvTransactionDetails> invTransactionDetailsList = invTransactionService.findAllInvTransactionDetails(id);
        invTransactionDetailsList.forEach(invTransactionDetails -> invTransactionDetails.setIsDeleted(true));
        invTransactionService.saveAllTransactionDetails(invTransactionDetailsList);
        return true;
    }

    @Transactional
    @Override
    public InvDeliveryChallan findById(Long id) {
        try {
            Optional<InvDeliveryChallan> optionalInvDeliveryChallan = invDeliveryChallanRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalInvDeliveryChallan.isPresent()) {
                throw new Exception("Inventory Delivery Challan Not exist with id " + id);
            }
            return optionalInvDeliveryChallan.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    @Override
    public List<InvDeliveryChallan> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return invDeliveryChallanRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Transactional
    @Override
    public boolean validate(Object object) {
        return true;
    }

    private List<InvTransactionDetails> getInvTransactionDetails(List<InvTransactionDetailsDto> invTransactionDetailsDtoList, InvTransaction invTransaction) {
        List<InvTransactionDetails> invTransactionDetailsList = new ArrayList<>();
        for (InvTransactionDetailsDto itdd : invTransactionDetailsDtoList) {
            InvTransactionDetails invTransactionDetails = new InvTransactionDetails();
            if (itdd.getId() != null) {
                invTransactionDetails = invTransactionService.findInvTransactionDetailsById(itdd.getId());
            }
            invTransactionDetails.setQuantity(itdd.getQuantity());
            Product product = productService.findById(itdd.getProductId());
            invTransactionDetails.setQuantityInUom((float) (product.getItemSize() * itdd.getQuantity()));
            invTransactionDetails.setRate(itdd.getRate());
            // invTransactionDetails.setInvItemStatus(InvItemStatus.valueOf(itdd.getInvItemStatus()));
            invTransactionDetails.setOrganization(invTransaction.getOrganization());
            invTransactionDetails.setInvTransaction(invTransaction);
            if (itdd.getBatchId() != null) {
                invTransactionDetails.setBatch(batchService.findById(itdd.getBatchId()));
            }
            if (itdd.getFromStoreId() != null) {
                invTransactionDetails.setFromStore(storeService.findById(itdd.getFromStoreId()));
            }
            if (itdd.getToStoreId() != null) {
                invTransactionDetails.setToStore(storeService.findById(itdd.getFromStoreId()));
            }
            if (itdd.getProductId() != null) {
                invTransactionDetails.setProduct(productService.findById(itdd.getProductId()));
            }
            invTransactionDetailsList.add(invTransactionDetails);
        }
        return invTransactionDetailsList;
    }

    public List<Map> getAllInvoiceAndChallanWiseDeliveryQuantityByCompanyAndDistributor(
            Long companyId, Long distributorId, Long invoiceNatureId,
            LocalDate fromDate, LocalDate toDate) {
        List<Map> quantityList =
                invDeliveryChallanRepository.findAllInvoiceAndChallanWiseDeliveryQuantityByCompanyAndDistributor(
                        companyId, distributorId, invoiceNatureId, fromDate, toDate);
        return quantityList;
    }

    public List<Map> getDistributorListWithTotalChallanNoByCompanyAndLocationAndAccountingYear(Long companyId, List<Long> locationIds, Long accountingYearId) {
        List<Location> locations = new ArrayList<>();
        List<Long> territoryIdList = new ArrayList<>();
        if (locationIds.size() != 0) {
            List<Location> mainLocationList = locationService.getAllLocationByIdListIn(locationIds); // get all location object comes from frontend
            LocationType maxLevelLocationType = locationTypeService.getLastLayerLocationTypeByCompanyId(companyId);
            locations = locationService.getAllChildLocationsByAnyParentLocationAndCompany(companyId, locationIds.get(0), locations);
            locations = Stream.concat(locations.stream(), mainLocationList.stream()).collect(Collectors.toList()); // concat frontend locations and other child locations
            locations = locations.stream().filter(l -> l.getLocationType() == maxLevelLocationType).collect(Collectors.toList());
            territoryIdList = locations.parallelStream().map(Location::getId).collect(Collectors.toList());
        }

        String fromDate = null;
        String toDate = null;
        List<Map> mapList;
        if (accountingYearId != null) { // null = All
            AccountingYear accountingYear = accountingYearService.findById(accountingYearId);
            fromDate = accountingYear.getStartDate().toString();
            toDate = accountingYear.getEndDate().toString();
        }

        mapList = getDistributorListWithTotalChallanNoByCompanyAndLocationListAndDateRange(companyId, territoryIdList, fromDate, toDate);
        return mapList;
    }

    public List<Map> getDistributorListWithTotalChallanNoByCompanyAndLocationListAndDateRange(Long companyId, List<Long> territoryLocationIdList, String fromDate, String toDate) {
        List<Map> mapList = invDeliveryChallanRepository.getDistributorListWithTotalChallanNoByCompanyAndLocationListAndDateRange(companyId, territoryLocationIdList, fromDate, toDate);
        return mapList;
    }

    public List<Map> getAllDeliveryChallanByCompanyAndInvoiceNatureAndDistributorAndDateRange(Long companyId, Long invoiceNatureId, Long distributorId, String fromDate, String toDate) {
        List<Map> list = invDeliveryChallanRepository.getAllDeliveryChallanByCompanyAndInvoiceNatureAndDistributorAndDateRange(companyId, invoiceNatureId, distributorId, fromDate, toDate); //fromDate = null = no date range
        return list;
    }

    public List<Map> getAllDeliveryChallanByCompanyAndInvoiceNatureAndDistributorAndAccountingYearId(Long companyId, Long invoiceNatureId, Long distributorId, Long accountingYearId) {
        String fromDate = null;
        String toDate = null;
        if (accountingYearId != null) { // null = All
            AccountingYear accountingYear = accountingYearService.findById(accountingYearId);
            fromDate = accountingYear.getStartDate().toString();
            toDate = accountingYear.getEndDate().toString();
        }
        List<Map> list = getAllDeliveryChallanByCompanyAndInvoiceNatureAndDistributorAndDateRange(companyId, invoiceNatureId, distributorId, fromDate, toDate);
        return list;
    }

    public Map<String, Object> getDepotDistributorListForChallan(
            Map<String, Object> searchParams) {
        Map<String, Object> returnMap = new HashMap<>();
        List<Map> resultList = new ArrayList<>();
        Double deliverableQuantity = 0.D;
        Long totalOrder = new Long(0);

        Map<Long, Object> childLocationMap = new HashMap<>();
        List<Long> soList = null;
        if (searchParams.get("companyId") == null) {
            return null;
        }
        Long companyId = Long.parseLong(searchParams.get("companyId").toString());
        Long userLoginId = applicationUserService.getApplicationUserFromLoginUser().getId();
        Long fiscalYearId;
        Long locationIdL;
        LocalDate startDate = null;
        LocalDate endDate = null;
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        Long semesterId = null;

        if (searchParams.get("semesterId") != null
                && !searchParams.get("semesterId").toString().isEmpty()) {
            semesterId = new Long(searchParams.get("semesterId").toString());
        }

        if (searchParams.get("accountingYearId") != null
                && !searchParams.get("accountingYearId").toString().isEmpty()
                && searchParams.get("accountingYearId") != "undefined") {
            fiscalYearId = new Long(searchParams.get("accountingYearId").toString());
            Map<String, LocalDate> dateMap =
                    accountingYearService.getAccountingYearDate(fiscalYearId);

            if (dateMap != null) {
                startDate = dateMap.get("startDate");
                endDate = dateMap.get("endDate");
                startDateTime = startDate.atStartOfDay();
                endDateTime = endDate.atStartOfDay();
            }
        }

        if (searchParams.get("locationId") != null
                && !searchParams.get("locationId").toString().isEmpty()) {
            String locationId = searchParams.get("locationId").toString();
            locationIdL = new Long(locationId);
            childLocationMap =
                    locationService.getChildLocationsByParent(
                            companyId, locationIdL, childLocationMap);
            soList = locationService.getSoListByLocation(companyId, childLocationMap);

        } else {

            Boolean isDepotManager = applicationUserService.checkLoginUserIsDepotManager(companyId, userLoginId);
            if (isDepotManager) {
                List<Map> areaList = depotService.findDepotAreaList(companyId, userLoginId);
                Map<Long, Object> finalChildLocationMap = childLocationMap;
                areaList.forEach((Map locationParams) -> {
                    Long locationId = Long.parseLong(locationParams.get("id").toString());
                    if (locationParams.get("id") != null) {
                        Map<Long, Object> childLocationMap1 =
                                locationService.getChildLocationsByParent(companyId, locationId,
                                        finalChildLocationMap);
                        finalChildLocationMap.putAll(childLocationMap1);
                    }
                });

                soList = locationService.getSoListByLocation(companyId, childLocationMap);
            }
        }

        if (soList != null && soList.size() > 0) {

            List<Map> distributorList = distributorService.
                    getDistributorListWithOrderInfo(
                            companyId, soList, startDate, endDate);

            LocalDateTime finalStartDateTime = startDateTime;
            LocalDateTime finalEndDateTime = endDateTime;
            resultList = distributorList.stream()
                    .map(elt -> makeDistributorMap(elt,
                            finalStartDateTime, finalEndDateTime, companyId, userLoginId))
                    .collect(Collectors.toList());

            deliverableQuantity =
                    resultList.stream().filter(o -> o.containsKey("deliverable_quantity"))
                            .mapToDouble(o -> Double.parseDouble(o.get("deliverable_quantity").toString())).sum();

            totalOrder =
                    resultList.stream().filter(o -> o.containsKey("order_count"))
                            .mapToLong(o -> Long.parseLong(o.get("order_count").toString())).sum();
        }

        resultList.removeIf(x -> Double.parseDouble(x.get("deliverable_quantity").toString()) == 0);

        returnMap.put("distributorList", resultList);
        returnMap.put("deliverableQuantity", deliverableQuantity);
        returnMap.put("totalOrder", totalOrder);
        return returnMap;
    }

    private Map makeDistributorMap(Map bookingItem,
                                   LocalDateTime startDateTime,
                                   LocalDateTime endDateTime,
                                   Long companyId, Long userLoginId) {

        Map map = new HashMap<>();
        Integer order_count = 0;
        Float deliverable_quantity = 0.0F;
        Long distributorId = Long.parseLong(String.valueOf(bookingItem.get("distributor_id")));

        Map orderSummary = invDeliveryChallanRepository.getDistributorOrderSummary(
                companyId, startDateTime, endDateTime, distributorId);

        if (orderSummary.get("order_count") != null) {
            order_count =
                    Integer.parseInt(orderSummary.get("order_count").toString());
        }

        if (orderSummary.get("deliverable_quantity") != null) {
            deliverable_quantity =
                    Float.parseFloat(orderSummary.get("deliverable_quantity").toString());
        }

        List<Map> orderList =
                invDeliveryChallanRepository.getDistributorOrderList(companyId,
                        startDateTime, endDateTime, distributorId);
        List<Map> pickingList =
                invDeliveryChallanRepository.getDistributorPickingList(companyId, startDateTime, endDateTime, distributorId, PickingStatus.CONFIRMED.getCode());

        map.put("depot", depotService.getDepotByLoginUserId(companyId, userLoginId));
        map.put("distributor_name", bookingItem.get("distributor_name"));
        map.put("ledger_balance", bookingItem.get("ledger_balance"));
        map.put("distributor_id", distributorId);
        map.put("contact_no", bookingItem.get("contact_no"));
        map.put("ship_to_address", bookingItem.get("ship_to_address"));
        map.put("company_id", companyId);
        map.put("order_count", order_count);
        map.put("deliverable_quantity", deliverable_quantity);
        /*map.put("distributorAndDepotInfo",
            distributorService.getDistributorWithDepotAndLocation(companyId, distributorId));*/
        map.put("orderList", orderList);
        map.put("pickingList", pickingList);

        return map;
    }

    public List<Map> getProductList(Long orderId, Long depotId) {

        List<Map> productList =
                invDeliveryChallanRepository.getProductList(orderId);

        List<Map> productListWithBatch = productList.stream()
                .map(elt -> invTransactionService.makeBatchStockMap(elt, depotId))
                .collect(Collectors.toList());

        return productListWithBatch;
    }

    public List<Map> getDriverList() {

        Organization organization =
                organizationService.getOrganizationFromLoginUser();
        List<Map> driverList =
                invDeliveryChallanRepository.getDriverList(organization.getId());

        return driverList;
    }


    @Transactional
    public List<InvDeliveryChallan> updateDeliveryChallanListForInvoiceCreate(List<InvDeliveryChallan> invDeliveryChallanList) {
        try {
            invDeliveryChallanList.forEach(d -> d.setHasInvoice(true));
            invDeliveryChallanRepository.saveAll(invDeliveryChallanList);
            return invDeliveryChallanRepository.saveAll(invDeliveryChallanList);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<InvDeliveryChallan> getAllInvDeliveryChallanByVehicle(Long vehicleId) {
        return invDeliveryChallanRepository.findByVehicleIdAndIsDeletedFalse(vehicleId);
    }

    public List<InvDeliveryChallan> getAllInvDeliveryChallanByIds(List<Long> deliveryChallanIds) {
        return invDeliveryChallanRepository.findAllByIdInAndIsActiveTrueAndIsDeletedFalse(deliveryChallanIds);
    }


    public List<Map<String, Object>> getDistributorWiseDeliveryChallanList(Long companyId, Long distributorId) {
        return invDeliveryChallanRepository.findDistributorWiseDeliveryChallanList(companyId, distributorId);
    }


    public List<Map<String, Object>> getDeliveryChallanDetails(Long deliveryChallanId) {

        return invDeliveryChallanRepository.findProductList(deliveryChallanId);

    }

    public List<Map> getProductListByPickingIdAndOrderId(Long pickingId, Long orderId, Long depotId) {

        List<Map> productList =
                invDeliveryChallanRepository.getProductListByPicking(pickingId, orderId);

        List<Map> productListWithBatch = productList.stream()
                .map(elt -> invTransactionService.makeBatchStockMap(elt, depotId))
                .collect(Collectors.toList());

        return productListWithBatch;
    }

    public List<Map> getPickingListByDistributorWiseWithoutDate(Long companyId, Long distributorId) {
        return invDeliveryChallanRepository.getDistributorPickingListAll(companyId, null, null, distributorId, null);
    }

    public Map getOrderProductPickingSummary(Long orderId, Long productId) {
        return invDeliveryChallanRepository.getOrderProductPickingSummary(orderId, productId);
    }

    public Map getOrderItemChallanInformation(Long salesOrderDetailsId) {
        return invDeliveryChallanRepository.getOrderItemChallanInformation(salesOrderDetailsId);
    }

    public List<Map> getPickingDeliveredInfo(Long pickingId) {
        return invDeliveryChallanRepository.getPickingDeliveredInfo(pickingId);
    }

}
