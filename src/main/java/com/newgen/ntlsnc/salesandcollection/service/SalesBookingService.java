package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.CommonConstant;
import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.ApprovalActor;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.common.enums.SalesBookingStatus;
import com.newgen.ntlsnc.common.enums.StoreType;
import com.newgen.ntlsnc.globalsettings.entity.*;
import com.newgen.ntlsnc.globalsettings.service.*;
import com.newgen.ntlsnc.multilayerapproval.repository.MultiLayerApprovalPathRepository;
import com.newgen.ntlsnc.multilayerapproval.service.MultiLayerApprovalPathService;
import com.newgen.ntlsnc.notification.pushNotification.dto.PushNotificationRequest;
import com.newgen.ntlsnc.notification.pushNotification.service.PushNotificationService;
import com.newgen.ntlsnc.salesandcollection.dto.SalesBookingDetailsDto;
import com.newgen.ntlsnc.salesandcollection.dto.SalesBookingDto;
import com.newgen.ntlsnc.salesandcollection.entity.*;
import com.newgen.ntlsnc.salesandcollection.repository.SalesBookingDetailsRepository;
import com.newgen.ntlsnc.salesandcollection.repository.SalesBookingRepository;
import com.newgen.ntlsnc.supplychainmanagement.service.InvTransactionService;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import com.newgen.ntlsnc.usermanagement.entity.ReportingManager;
import com.newgen.ntlsnc.usermanagement.repository.ReportingManagerRepository;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import com.newgen.ntlsnc.usermanagement.service.ReportingManagerService;
import com.newgen.ntlsnc.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author kamal
 * @Date ১১/৪/২২
 */

@Service
public class SalesBookingService implements IService<SalesBooking> {
    @Autowired
    OrganizationService organizationService;
    @Autowired
    SalesBookingRepository salesBookingRepository;
    @Autowired
    SalesBookingDetailsRepository salesBookingDetailsRepository;
    @Autowired
    DepotService depotService;
    @Autowired
    DistributorService distributorService;
    @Autowired
    SemesterService semesterService;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    ProductTradePriceService productTradePriceService;
    @Autowired
    AccountingYearService accountingYearService;
    @Autowired
    SalesBookingDetailsService salesBookingDetailsService;
    @Autowired
    LocationService locationService;
    @Autowired
    SalesOrderService salesOrderService;
    @Autowired
    InvTransactionService invTransactionService;
    @Autowired
    PaymentCollectionAdjustmentService adjustmentService;
    @Autowired
    InvoiceNatureService invoiceNatureService;
    @Autowired
    ProductService productService;
    @Autowired
    DocumentSequenceService documentSequenceService;
    @Autowired
    TradeDiscountService tradeDiscountService;
    @Autowired
    ReportingManagerService reportingManagerService;
    @Autowired
    CreditLimitService creditLimitService;
    @Autowired
    StoreService storeService;
    @Autowired
    PushNotificationService pushNotificationService;
    @Autowired
    MultiLayerApprovalPathService approvalPathService;
    @Autowired
    ReportingManagerRepository reportingManagerRepository;
    @Autowired
    MultiLayerApprovalPathRepository multiLayerApprovalPathRepository;
    private static final DecimalFormat df = new DecimalFormat("#.##");

    @Transactional
    @Override
    public SalesBooking create(Object object) {
        SalesBookingDto salesBookingDto = (SalesBookingDto) object;
        SalesBooking salesBooking = new SalesBooking();
        salesBooking.setBookingNo(salesBookingDto.getBookingNo());
        salesBooking.setApprovalStatus(ApprovalStatus.PENDING);
        salesBooking.setBookingDate(LocalDateTime.parse(salesBookingDto.getBookingDate()));    //yyyy-MM-dd
        salesBooking.setTentativeDeliveryDate(LocalDate.parse(salesBookingDto.getTentativeDeliveryDate()));    //yyyy-MM-dd
        salesBooking.setNotes(salesBookingDto.getNotes());
        salesBooking.setOrganization(organizationService.getOrganizationFromLoginUser());
        if (salesBookingDto.getCompanyId() != null) {
            Organization company = organizationService.findById(salesBookingDto.getCompanyId());
            salesBooking.setCompany(company);
        }
        if (salesBookingDto.getDepotId() != null) {
            Depot depot = depotService.findById(salesBookingDto.getDepotId());
            salesBooking.setDepot(depot);
        }
        if (salesBookingDto.getDistributorId() != null) {
            Distributor distributor = distributorService.findById(salesBookingDto.getDistributorId());
            salesBooking.setDistributor(distributor);
        }
        if (salesBookingDto.getSalesOfficerId() != null) {
            ApplicationUser salesOfficer = applicationUserService.findById(salesBookingDto.getSalesOfficerId());
            salesBooking.setSalesOfficer(salesOfficer);
        }
        if (salesBookingDto.getSemesterId() != null) {
            Semester semester = semesterService.findById(salesBookingDto.getSemesterId());
            salesBooking.setSemester(semester);
        }
        if (!this.validate(salesBooking)) {
            return null;
        }
        salesBooking = salesBookingRepository.save(salesBooking);
        List<SalesBookingDetails> salesBookingDetailsList = getSalesBookingDetails(salesBookingDto.getSalesBookingDetailsList(), salesBooking);
        salesBookingDetailsRepository.saveAll(salesBookingDetailsList);
        return salesBooking;
    }

    @Transactional
    @Override
    public SalesBooking update(Long id, Object object) {

        SalesBookingDto salesBookingDto = (SalesBookingDto) object;
        Optional<SalesBooking> salesBookingOptional = salesBookingRepository.findById(id);
        if (!salesBookingOptional.isPresent()) {
            return null;
        }
        SalesBooking salesBooking = salesBookingOptional.get();
        salesBooking.setBookingNo(salesBookingDto.getBookingNo());
        salesBooking.setApprovalStatus(ApprovalStatus.valueOf(salesBookingDto.getApprovalStatus()));
        salesBooking.setBookingDate(LocalDateTime.parse(salesBookingDto.getBookingDate()));   //yyyy-MM-dd
        salesBooking.setTentativeDeliveryDate(LocalDate.parse(salesBookingDto.getTentativeDeliveryDate()));    //yyyy-MM-dd
        salesBooking.setNotes(salesBookingDto.getNotes());
        salesBooking.setOrganization(organizationService.getOrganizationFromLoginUser());
        if (salesBookingDto.getCompanyId() != null) {
            salesBooking.setCompany(organizationService.findById(salesBookingDto.getCompanyId()));
        }
        if (salesBookingDto.getDepotId() != null) {
            salesBooking.setDepot(depotService.findById(salesBookingDto.getDepotId()));
        }
        if (salesBookingDto.getDistributorId() != null) {
            salesBooking.setDistributor(distributorService.findById(salesBookingDto.getDistributorId()));
        }
        if (salesBookingDto.getSalesOfficerId() != null) {
            salesBooking.setSalesOfficer(applicationUserService.findById(salesBookingDto.getSalesOfficerId()));
        }
        if (salesBookingDto.getSemesterId() != null) {
            salesBooking.setSemester(semesterService.findById(salesBookingDto.getSemesterId()));
        }
        if (!this.validate(salesBooking)) {
            return null;
        }
        salesBooking = salesBookingRepository.save(salesBooking);
        List<SalesBookingDetails> salesBookingDetailsList = getSalesBookingDetails(salesBookingDto.getSalesBookingDetailsList(), salesBooking);
        salesBookingDetailsRepository.saveAll(salesBookingDetailsList);
        return salesBooking;
    }

    @Transactional
    @Override
    public boolean delete(Long id) {
        Optional<SalesBooking> salesBookingOptional = salesBookingRepository.findById(id);
        try {
            if (!salesBookingOptional.isPresent()) {
                throw new Exception("Sales Booking not exist");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        SalesBooking salesBooking = salesBookingOptional.get();
        salesBooking.setIsDeleted(true);
        salesBookingRepository.save(salesBooking);
        List<SalesBookingDetails> salesBookingDetailsList = salesBookingDetailsRepository.findAllBySalesBooking(salesBooking);
        salesBookingDetailsList.forEach(salesBookingDetails -> salesBookingDetails.setIsDeleted(true));
        salesBookingDetailsRepository.saveAll(salesBookingDetailsList);
        return true;
    }

    @Override
    public SalesBooking findById(Long id) {
        try {
            Optional<SalesBooking> optionalSalesBooking = salesBookingRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalSalesBooking.isPresent()) {
                throw new Exception("Sales Booking does not exist with id " + id);
            }
            return optionalSalesBooking.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<SalesBooking> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return salesBookingRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    private List<SalesBookingDetails> getSalesBookingDetails(List<SalesBookingDetailsDto> salesBookingDetailsDto, SalesBooking salesBooking) {

        List<SalesBookingDetails> salesBookingDetailsList = new ArrayList<>();
        for (SalesBookingDetailsDto sbd : salesBookingDetailsDto) {
            SalesBookingDetails salesBookingDetails = new SalesBookingDetails();
            if (sbd.getId() != null) {
                salesBookingDetails = salesBookingDetailsRepository.findById(sbd.getId()).get();
            }
            salesBookingDetails.setQuantity(sbd.getQuantity());
            salesBookingDetails.setFreeQuantity(sbd.getFreeQuantity());
            salesBookingDetails.setSalesBooking(salesBooking);
            salesBookingDetails.setOrganization(salesBooking.getOrganization());

            if (sbd.getProductId() != null) {
                Product product = productService.findById(sbd.getProductId());
                salesBookingDetails.setProduct(product);
            }
            if (sbd.getProductTradePriceId() != null) {
                ProductTradePrice productTradePrice = productTradePriceService.findById(sbd.getProductTradePriceId());
                salesBookingDetails.setProductTradePrice(productTradePrice);
                salesBookingDetails.setProduct(productTradePrice.getProduct());
            }
            salesBookingDetailsList.add(salesBookingDetails);
        }


        return salesBookingDetailsList;
    }

    public List<Map<String, Object>> findAllSalesBookingList() {
        return salesBookingRepository.findAllSalesBookingList();
    }

    public SalesBookingDetails findSalesBookingDetailsBySalesBookingDetailsId(Long salesBookingDetailsId) {
        return salesBookingDetailsRepository.findById(salesBookingDetailsId).orElse(null);
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }


    public Map<String, Object> getSalesBookingFilteredList(Map<String, Object> searchParams) {
        Map<String, Object> returnMap = new HashMap<>();
        Map<Long, Object> childLocationMap = new HashMap<>();
        List<Long> soList = new ArrayList<>();
        List<Map<String, Object>> listMap = null;
        if (searchParams.get("companyId") == null) {
            return null;
        }
        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.CEILING);

        String companyId =  searchParams.get("companyId").toString();
        Long userLoginId = applicationUserService.getApplicationUserFromLoginUser().getId();
        Long fiscalYearIdL;
        Long locationIdL;
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        Long semesterIdL = null;
        Long companyIdL = Long.valueOf(companyId);
        String isBookingToConfirm = (String) searchParams.get("isBookingToConfirm");
        if (searchParams.get("semesterId")  != null
                && !searchParams.get("semesterId").toString().isEmpty()) {
            String semesterId = searchParams.get("semesterId").toString();
            semesterIdL = Long.valueOf(semesterId);
        }

        if (searchParams.get("accountingYearId") !=null
                && !searchParams.get("accountingYearId").toString().isEmpty()
                && searchParams.get("accountingYearId") != "undefined") {
            String accountingYearId = searchParams.get("accountingYearId").toString();
            fiscalYearIdL = Long.valueOf(accountingYearId);
            Map<String, LocalDate> dateMap =
                    accountingYearService.getAccountingYearDate(fiscalYearIdL);

            if (dateMap != null) {
                LocalDate startDate = dateMap.get("startDate");
                LocalDate endDate = dateMap.get("endDate");
                startDateTime = startDate.atStartOfDay();
                endDateTime = endDate.atStartOfDay();
            }
        }

        if (searchParams.get("locationId") != null
                && !searchParams.get("locationId").toString().isEmpty()) {
            String locationId = searchParams.get("locationId").toString();
            locationIdL = Long.valueOf(locationId);
            childLocationMap =
                    locationService.getChildLocationsByParent(
                            companyIdL, locationIdL, childLocationMap);
            soList = locationService.getSoListByLocation(companyIdL, childLocationMap);

        } else {

            Boolean isManager = applicationUserService.checkLoginUserIsManager(companyIdL, userLoginId);
            Boolean isSo = applicationUserService.checkLoginUserIsSo(companyIdL, userLoginId);
            Boolean isDepotManager = applicationUserService.checkLoginUserIsDepotManager(companyIdL, userLoginId);

            if (isManager) {
                locationIdL = locationService.getManagerLocation(companyIdL, userLoginId);
                childLocationMap =
                        locationService.getChildLocationsByParent(
                                companyIdL, locationIdL, childLocationMap);
                soList = locationService.getSoListByLocation(companyIdL, childLocationMap);

            } else if (isSo && searchParams.get("isBookingToConfirm") == null) {
                soList.add(userLoginId);
            }
            else if (isDepotManager) {
               /* && searchParams.get("isBookingToConfirm") != null
                        && !searchParams.get("isBookingToConfirm").toString().isEmpty()
                        && "Y".equals(isBookingToConfirm)*/
                List<Map> areaList = depotService.findDepotAreaList(companyIdL, userLoginId);
                for (Map area : areaList) {
                    locationIdL = Long.parseLong(area.get("id").toString());
                    Map<Long, Object> childLocationMap1 =
                            locationService.getChildLocationsByParent(companyIdL, locationIdL,
                                    childLocationMap);
                    childLocationMap.putAll(childLocationMap1);
                }
                soList = locationService.getSoListByLocation(companyIdL, childLocationMap);
                /*soList = locationManagerMapService
                        .getSalesOfficerListFromUserLoginIdOrLocationId(userLoginId, null, companyIdL);*/
                        //locationService.getSoListByLocation(companyIdL, childLocationMap);
            }
            else {
                List<Location> parentLocations =
                        locationService.getParentLocationsList(companyIdL);
                for (Location parentLocation : parentLocations) {
                    Map<Long, Object> childLocationMap1 =
                            locationService.getChildLocationsByParent(companyIdL, parentLocation.getId(),
                                    childLocationMap);
                    childLocationMap.putAll(childLocationMap1);
                }
                soList = locationService.getSoListByLocation(companyIdL, childLocationMap);
            }
        }
        if (searchParams.get("isOrderList") != null
                && !searchParams.get("isOrderList").toString().isEmpty()) {
            returnMap.put("salesOrderList", salesOrderService.getSaleOrderListBySO(companyIdL, startDateTime, endDateTime, semesterIdL,soList));

        } else if(searchParams.get("isBookingList") != null
                && !searchParams.get("isBookingList").toString().isEmpty()) {
            List<String> approvalStatusList = Stream.of(ApprovalStatus.DRAFT.toString(),
                    ApprovalStatus.PENDING.toString() ,
                    ApprovalStatus.REJECTED.toString()).collect(Collectors.toList());
            returnMap.put("salesBookingList", salesBookingRepository.findSalesBookingList(
                    companyIdL, startDateTime, endDateTime, semesterIdL, soList, approvalStatusList));
        }

        else if (searchParams.get("isBookingToConfirm") != null
                && !searchParams.get("isBookingToConfirm").toString().isEmpty()
                && "Y".equals(isBookingToConfirm)) {
            String status = (String) searchParams.get("status");
            double totalBookingAmount = 0.0D;
            int totalBooking = 0;
            List<String> approvalStatusList = null;
            if (status !=null
                    && !searchParams.get("status").toString().isEmpty()) {
                approvalStatusList = Collections.singletonList(status);
            }
            else{
                approvalStatusList = Stream.of(
                        ApprovalStatus.REJECTED.toString(),
                        ApprovalStatus.APPROVED.toString()).collect(Collectors.toList());
            }
            listMap = salesBookingRepository.findSalesBookingList(
                    companyIdL, startDateTime, endDateTime, semesterIdL, soList, approvalStatusList);

            totalBookingAmount =
                    listMap.stream().filter(o -> o.containsKey("booking_amount"))
                            .mapToDouble(o -> Double.parseDouble(o.get("booking_amount").toString())).sum();

            totalBooking =
                    listMap.stream().filter(o -> o.containsKey("booking_count"))
                            .mapToInt(o -> Integer.parseInt(o.get("booking_count").toString())).sum();

            returnMap.put("discountedAmount",
                    listMap.stream().filter(o -> o.containsKey("discounted_amount"))
                            .mapToDouble(o -> Double.parseDouble(o.get("discounted_amount").toString())).sum());
            returnMap.put("freeQuantity",
                    listMap.stream().filter(o -> o.containsKey("free_quantity"))
                            .mapToDouble(o -> Double.parseDouble(o.get("free_quantity").toString())).sum());
            returnMap.put("salesBookingListToConfirm", listMap);
            returnMap.put("totalBookingAmount", totalBookingAmount);
            returnMap.put("totalBooking", totalBooking);
            returnMap.put("approvalStatus", status);
        }
        else {
            List<String> approvalStatusList = Stream.of(ApprovalStatus.DRAFT.toString(),
                    ApprovalStatus.PENDING.toString() ,
                    ApprovalStatus.REJECTED.toString(),
                    ApprovalStatus.AUTHORIZATION_FLOW.toString(),
                    ApprovalStatus.APPROVED.toString()).collect(Collectors.toList());

            List<Map<String, Object>> salesBookingList = salesBookingRepository.findSalesBookingList(
                    companyIdL, startDateTime, endDateTime, semesterIdL, soList, approvalStatusList);

            returnMap.put("bookingSummary",
                    salesBookingList.stream().filter(o -> o.containsKey("booking_amount"))
                            .mapToDouble(o -> Double.parseDouble(o.get("booking_amount").toString())).sum());

            returnMap.put("totalBookingQuantity",
                    salesBookingList.stream().filter(o -> o.containsKey("booking_quantity"))
                        .mapToInt(o -> Integer.parseInt(o.get("booking_quantity").toString())).sum());

            returnMap.put("discountedAmount",
                    salesBookingList.stream().filter(o -> o.containsKey("discounted_amount"))
                            .mapToDouble(o -> Double.parseDouble(o.get("discounted_amount").toString())).sum());
            returnMap.put("freeQuantity",
                    salesBookingList.stream().filter(o -> o.containsKey("free_quantity"))
                            .mapToDouble(o -> Double.parseDouble(o.get("free_quantity").toString())).sum());

            returnMap.put("salesBookingList", salesBookingList);

        }
        return returnMap;
    }

    Long getLocationManager(Long companyId, Long locationId) {
        Long locationManagerId =
                locationService.getLocationManager(companyId, locationId);
        return locationManagerId;
    }

    /*Long getSoLocation(Long userLoginId) {
        Long reportingTo =
                locationService.getReportingTo(userLoginId);
        Long locationId = locationService.getManagerLocation(reportingTo);

        return locationId;
    }*/

    public Map<String, Object> getSalesBookingDetails(Long bookingId) {
        Map<String, Object> returnMap = new HashMap<>();
        List<Map> salesBookingDetails;

        if (bookingId == null) {
            return null;
        }
        salesBookingDetails = salesBookingRepository.findSalesBookingDetails(bookingId);
        SalesBooking salesBooking = salesBookingRepository.getById(bookingId);
        Float creditLimit =
                creditLimitService.getDistributorLimit(
                        salesBooking.getDistributor().getId(),
                        salesBooking.getCompany().getId());

        returnMap.put("creditLimit", creditLimit);
        returnMap.put("salesBookingDetails", salesBookingDetails);

        return returnMap;
    }

    private String findBookingStatus(Long bookingId) {
        Boolean isStockConfirmed = true;
        String isStockConfirmedStatus = "";
        List<Boolean> productStatus = new ArrayList<>();
        List<SalesBookingDetails> salesBookingDetailsList =
                salesBookingDetailsService.findSalesBookingProductsByBookingId(bookingId);
        for (SalesBookingDetails salesBookingDetails : salesBookingDetailsList) {
            String bookingStatus = salesBookingDetails.getSalesBookingStatus().getCode();
            if ("SALES_BOOKED".equals(bookingStatus)) {
                productStatus.add(false);
            }  else if ("TICKET_REQUESTED".equals(bookingStatus)) {
                productStatus.add(false);
            }
        }

        if (productStatus.contains(false)) {
            isStockConfirmedStatus = LIFE_CYCLE_STATUS_INPROGRESS;
        } else {
            isStockConfirmedStatus = LIFE_CYCLE_STATUS_COMPLETED;
        }
        return isStockConfirmedStatus;
    }

    private String findBookingToOrderStatus(Long bookingId) {
        Boolean isOrderConverted = true;
        String bookingOrderStatus = "";
        List<Boolean> productStatus = new ArrayList<>();
        List<SalesBookingDetails> salesBookingDetailsList =
                salesBookingDetailsService.findSalesBookingProductsByBookingId(bookingId);

        for (SalesBookingDetails salesBookingDetails : salesBookingDetailsList) {
            List<SalesOrder> salesOrderList =
                    salesOrderService.getSalesOrderListByBookingId(bookingId);
            String bookingStatus = salesBookingDetails.getSalesBookingStatus().getCode();
            if (salesOrderList.size() == 0) {
                bookingOrderStatus = LIFE_CYCLE_STATUS_PENDING;
                return bookingOrderStatus;
            } else {
                if ("SALES_BOOKED".equals(bookingStatus)) {
                    productStatus.add(false);
                } else if ("ORDER_CONVERTED".equals(bookingStatus)) {
                    productStatus.add(true);
                } else if ("TICKET_REQUESTED".equals(bookingStatus)) {
                    productStatus.add(false);
                } else if ("TICKET_CONFIRMED".equals(bookingStatus)) {
                    productStatus.add(true);
                } else if ("TICKET_REJECTED".equals(bookingStatus)) {
                    productStatus.add(true);
                }
                if (productStatus.contains(false)) {
                    isOrderConverted = false;
                }

                if (isOrderConverted) {
                    bookingOrderStatus = LIFE_CYCLE_STATUS_COMPLETED;
                } else {
                    bookingOrderStatus = LIFE_CYCLE_STATUS_INPROGRESS;
                }
            }
        }

        return bookingOrderStatus;
    }

    private Map<String, Object> findBookingDeliveryStatus(Long bookingId, String bookingOrderStatus) {

        Map<String, Object> orderDeliveryStatusMap = new HashMap<>();
        int isDeliveryPending = 0;
        String bookingOrderDeliveryStatus = "";
        List<Boolean> orderDelivery = new ArrayList<>();

        List<SalesOrder> orderList =
                salesOrderService.getSalesOrderListByBookingId(bookingId);

        for (SalesOrder order : orderList) {
            List<SalesOrderDetails> orderItemList =
                    salesOrderService.getSalesOrderProducts(order.getId());

            List<Map> itemsDelivery =
                    invTransactionService.findOrderItemsDelivery(orderItemList);

            if (itemsDelivery.size() == 0) {
                orderDelivery.add(false);
                isDeliveryPending++;
            } else {
                orderDelivery.add(true);
            }
        }

        if (isDeliveryPending == orderList.size()) {
            bookingOrderDeliveryStatus = LIFE_CYCLE_STATUS_PENDING;
        } else {
            if (orderDelivery.contains(false)
                    && !bookingOrderStatus.equals(LIFE_CYCLE_STATUS_COMPLETED)) {
                bookingOrderDeliveryStatus = LIFE_CYCLE_STATUS_INPROGRESS;
            } else {
                bookingOrderDeliveryStatus = LIFE_CYCLE_STATUS_COMPLETED;
            }
        }

        orderDeliveryStatusMap.put("orderList", orderList);
        orderDeliveryStatusMap.put("bookingOrderDeliveryStatus", bookingOrderDeliveryStatus);

        return orderDeliveryStatusMap;
    }

    private String findBookingInvoiceAcknowledgeStatus(Long bookingId,
                                                       String bookingOrderStatus,
                                                       String bookingInvoiceStatus,
                                                       List<Map> bookingInvoiceList) {

        String bookingInvoiceAcknowledgeStatus = "";
        List<Boolean> acknowledgementList = new ArrayList<>();

        for (Map bookingInvoice : bookingInvoiceList) {
            Boolean is_accepted = (Boolean) bookingInvoice.get("is_accepted");
            if (is_accepted){
                acknowledgementList.add(is_accepted);
            }

        }
        if (bookingOrderStatus.equals(LIFE_CYCLE_STATUS_COMPLETED)
                && bookingInvoiceStatus.equals(LIFE_CYCLE_STATUS_COMPLETED)
                && acknowledgementList.size() == bookingInvoiceList.size()) {
            bookingInvoiceAcknowledgeStatus = LIFE_CYCLE_STATUS_COMPLETED;
        } else if (acknowledgementList.size() == 0) {
            bookingInvoiceAcknowledgeStatus = LIFE_CYCLE_STATUS_PENDING;
        } else
            bookingInvoiceAcknowledgeStatus = LIFE_CYCLE_STATUS_INPROGRESS;

        return bookingInvoiceAcknowledgeStatus;
    }

    private Map<String, Object> findBookingInvoiceStatus(Long bookingId,
                                                         String bookingOrderStatus, String bookingDeliveryStatus,
                                                         List<SalesOrder> orderList) {

        Map<String, Object> bookingInvoiceStatusMap = new HashMap<>();
        String bookingInvoiceStatus = "";
        List<Map> bookingInvoiceList =
                invTransactionService.findInvoiceOfBooking(bookingId);

        if (bookingInvoiceList.size() == 0) {
            bookingInvoiceStatus = LIFE_CYCLE_STATUS_PENDING;
        } else {
            if (bookingOrderStatus.equals(LIFE_CYCLE_STATUS_COMPLETED)
                    && bookingDeliveryStatus.equals(LIFE_CYCLE_STATUS_COMPLETED)
                    && bookingInvoiceList.size() == orderList.size()) {
                bookingInvoiceStatus = LIFE_CYCLE_STATUS_COMPLETED;
            } else bookingInvoiceStatus = LIFE_CYCLE_STATUS_INPROGRESS;

        }

        bookingInvoiceStatusMap.put("bookingInvoiceList", bookingInvoiceList);
        bookingInvoiceStatusMap.put("bookingInvoiceStatus", bookingInvoiceStatus);

        return bookingInvoiceStatusMap;
    }

    private String findBookingPaymentAdjustmentStatus(Long companyId, Long bookingId,
                                                      String bookingInvoiceAcknowledgeStatus,
                                                      List<Map> bookingInvoiceList) {

        String invoiceAdjustmentStatus = "";
        List<Long> invoiceIds = new ArrayList<>();

        for (Map bookingInvoice : bookingInvoiceList) {
            BigInteger salesInvoiceId = (BigInteger) bookingInvoice.get("sales_invoice_id");
            invoiceIds.add(salesInvoiceId.longValue());
        }

        List<Map> invoiceAdjustmentList =
                adjustmentService.findInvoiceAdjustments(companyId, invoiceIds);
        if (invoiceAdjustmentList.size() == 0) {
            invoiceAdjustmentStatus = LIFE_CYCLE_STATUS_PENDING;
        } else {
            if (bookingInvoiceAcknowledgeStatus.equals(LIFE_CYCLE_STATUS_COMPLETED)
                    && bookingInvoiceList.size() == invoiceAdjustmentList.size()) {
                invoiceAdjustmentStatus = LIFE_CYCLE_STATUS_COMPLETED;
            } else invoiceAdjustmentStatus = LIFE_CYCLE_STATUS_INPROGRESS;
        }

        return invoiceAdjustmentStatus;
    }


    public Map<String, Object> getBookingLifeCycleStatus(Long companyId, Long bookingId) {

        Map<String, Object> bookingLifeCycleMap = new HashMap<>();

        List<Map<String, Object>> activities = getBookingActivitiesList(bookingId);
//        Map<String, Object> activity = activities
//                .stream()
//                .collect(Collectors.toMap(s -> (String) s.get("key"),
//                        s -> (String) s.get("value")));

        Map<String, Object> activity = activities.stream()
                .filter(map -> map.containsValue("REJECTED"))
                .findFirst()
                .orElse(null);
        String bookingRejectedReasons = "";
        if (null != activity && null != activity.get("comments")) {
            bookingRejectedReasons = activity.get("comments").toString();
        }

        String bookingOrderStatus = findBookingToOrderStatus(bookingId);
        Map<String, Object> bookingDeliveryStatusMap =
                findBookingDeliveryStatus(bookingId, bookingOrderStatus);

        String bookingOrderDeliveryStatus =
                (String) bookingDeliveryStatusMap.get("bookingOrderDeliveryStatus");
        List<SalesOrder> orderList =
                (List<SalesOrder>) bookingDeliveryStatusMap.get("orderList");

        Map<String, Object> bookingInvoiceStatusMap =
                findBookingInvoiceStatus(bookingId, bookingOrderStatus,
                        bookingOrderDeliveryStatus, orderList);
        String bookingInvoiceStatus =
                (String) bookingInvoiceStatusMap.get("bookingInvoiceStatus");
        List<Map> bookingInvoiceList =
                (List<Map>) bookingInvoiceStatusMap.get("bookingInvoiceList");

        String bookingInvoiceAcknowledgeStatus =
                findBookingInvoiceAcknowledgeStatus(bookingId,
                        bookingOrderStatus, bookingInvoiceStatus, bookingInvoiceList);

        String bookingPaymentAdjustmentStatus =
                findBookingPaymentAdjustmentStatus(companyId, bookingId,
                bookingInvoiceAcknowledgeStatus, bookingInvoiceList);

        bookingLifeCycleMap.put("bookingRejectedReasons", bookingRejectedReasons);
        bookingLifeCycleMap.put("bookingOrderStatus", bookingOrderStatus);
        bookingLifeCycleMap.put("bookingDeliveryStatus", bookingOrderDeliveryStatus);
        bookingLifeCycleMap.put("bookingInvoiceStatus", bookingInvoiceStatus);
        bookingLifeCycleMap.put("bookingInvoiceAcknowledgeStatus", bookingInvoiceAcknowledgeStatus);
        bookingLifeCycleMap.put("bookingPaymentAdjustmentStatus", bookingPaymentAdjustmentStatus);

        if (LIFE_CYCLE_STATUS_COMPLETED.equals(bookingPaymentAdjustmentStatus)) {
            bookingLifeCycleMap.put("bookingStatus", LIFE_CYCLE_STATUS_COMPLETED);
        }
        else {
            bookingLifeCycleMap.put("bookingStatus", LIFE_CYCLE_STATUS_INPROGRESS);
        }

        return bookingLifeCycleMap;
    }

    @Transactional
    public SalesBooking saveAsDraft(Object object) {
        try {
        SalesBookingDto salesBookingDto = (SalesBookingDto) object;
        SalesBooking salesBooking = new SalesBooking();

        if (salesBookingDto.getIsFinalSubmit()) {
            salesBooking = salesBookingRepository.findById(salesBookingDto.getId()).get();
            salesBooking.setTentativeDeliveryDate(LocalDate.parse(salesBookingDto.getTentativeDeliveryDate()));
            salesBooking.setApprovalStatus(ApprovalStatus.PENDING);
            if (salesBookingDto.getNotes() != null) {
                salesBooking.setNotes(salesBookingDto.getNotes());
            }
            if (salesBookingDto.getDepotId() != null) {
                Depot depot = depotService.findById(salesBookingDto.getDepotId());
                salesBooking.setDepot(depot);
            }
            salesBooking = salesBookingRepository.save(salesBooking);

        } else {
            if (salesBookingDto.getId() == null) {
                salesBooking.setBookingNo(documentSequenceService.getSequenceByDocumentId(CommonConstant.DOCUMENT_ID_FOR_BOOKING_NO));

                salesBooking.setBookingDate(LocalDateTime.now());//yyyy-MM-dd current date
                salesBooking.setApprovalStatus(ApprovalStatus.DRAFT);

                if (salesBookingDto.getInvoiceNatureId() != null) {
                    InvoiceNature invoiceNature = invoiceNatureService.findById(salesBookingDto.getInvoiceNatureId());
                    salesBooking.setInvoiceNature(invoiceNature);
                }
                if (salesBookingDto.getDistributorId() != null) {
                    Distributor distributor = distributorService.findById(salesBookingDto.getDistributorId());
                    salesBooking.setDistributor(distributor);
                }
                if (salesBookingDto.getSemesterId() != null) {
                    Semester semester = semesterService.findById(salesBookingDto.getSemesterId());
                    salesBooking.setSemester(semester);
                }else if (salesBookingDto.getSemesterId() == null && salesBookingDto.getCompanyId() != null) {
                    Optional<Semester> semesterOptional = semesterService.findSemesterByDate(LocalDate.now(),salesBookingDto.getCompanyId());
                    if(semesterOptional.isPresent()){
                        salesBooking.setSemester(semesterOptional.get());
                    }
                }
                if (salesBookingDto.getSalesOfficerId() != null) {
                    ApplicationUser salesOfficer = applicationUserService.findById(salesBookingDto.getSalesOfficerId());
                    salesBooking.setSalesOfficer(salesOfficer);
                }
                if (salesBookingDto.getCompanyId() != null) {
                    Organization company = organizationService.findById(salesBookingDto.getCompanyId());
                    salesBooking.setCompany(company);
                }

                if (salesBookingDto.getDepotId() == null) {
                    Map map = depotService.getSalesOfficerDepotInfo(salesBookingDto.getCompanyId(),salesBookingDto.getSalesOfficerId());
                    if(map.size() > 0 && map.get("id") != null) {
                        Depot depot = depotService.findById(Long.parseLong(map.get("id").toString()));
                        salesBooking.setDepot(depot);
                    } else {
                        throw new RuntimeException("Depot not found for this Sales Officer!!");
                    }
                }

                Organization organization = organizationService.getOrganizationFromLoginUser();
                salesBooking.setOrganization(organization);
                Long soLocationId
                        = locationService.getSoLocationId(salesBookingDto.getCompanyId(),
                        salesBookingDto.getSalesOfficerId());
                salesBooking.setLocation(
                        locationService.findById(soLocationId));

            } else {
                salesBooking = salesBookingRepository.findById(salesBookingDto.getId()).get();

                if (salesBookingDto.getInvoiceNatureId() != null) {
                    InvoiceNature invoiceNature = invoiceNatureService.findById(salesBookingDto.getInvoiceNatureId());
                    if(!salesBooking.getInvoiceNature().equals(invoiceNature)){
                        return null;
                    }
                }
            }
            salesBooking = salesBookingRepository.save(salesBooking);

        }
        List<SalesBookingDetails> salesBookingDetailsList = getSalesBookingDetailsForAddToCart(salesBookingDto.getSalesBookingDetailsList(), salesBooking, salesBookingDto.getIsFinalSubmit());
        salesBookingDetailsRepository.saveAll(salesBookingDetailsList);

        return salesBooking;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private List<SalesBookingDetails> getSalesBookingDetailsForAddToCart(List<SalesBookingDetailsDto> salesBookingDetailsDtoList, SalesBooking salesBooking, Boolean isFinalSubmit) {

        List<SalesBookingDetails> salesBookingDetailsList = new ArrayList<>();

        for (SalesBookingDetailsDto sbd : salesBookingDetailsDtoList) {

            SalesBookingDetails salesBookingDetails = new SalesBookingDetails();
            if (sbd.getId() != null) {
                salesBookingDetails = salesBookingDetailsRepository.findById(sbd.getId()).get();
            }
            if (isFinalSubmit) {

                ProductTradePrice productTradePrice = productTradePriceService.findById(sbd.getProductTradePriceId());
                salesBookingDetails.setProductTradePrice(productTradePrice);

                if (sbd.getTradeDiscountId() != null) {
                    TradeDiscount tradeDiscount = tradeDiscountService.findById(sbd.getTradeDiscountId());
                    salesBookingDetails.setTradeDiscount(tradeDiscount);
                }

            } else {
                salesBookingDetails.setQuantity(sbd.getQuantity());
                salesBookingDetails.setFreeQuantity(sbd.getFreeQuantity());
                salesBookingDetails.setSalesBooking(salesBooking);
                salesBookingDetails.setSalesBookingStatus(SalesBookingStatus.SALES_BOOKED);
                salesBookingDetails.setOrganization(salesBooking.getOrganization());
                if (sbd.getProductId() != null) {
                    Product product = productService.findById(sbd.getProductId());
                    salesBookingDetails.setProduct(product);
                }
                if (sbd.getProductTradePriceId() != null) {
                    ProductTradePrice productTradePrice = productTradePriceService.findById(sbd.getProductTradePriceId());
                    salesBookingDetails.setProductTradePrice(productTradePrice);
                }

                if (sbd.getTradeDiscountId() != null) {
                    TradeDiscount tradeDiscount = tradeDiscountService.findById(sbd.getTradeDiscountId());
                    salesBookingDetails.setTradeDiscount(tradeDiscount);
                }
            }

            salesBookingDetailsList.add(salesBookingDetails);
        }

        return salesBookingDetailsList;
    }

    public Map<String, Object> findSalesBookingListForDraftedView(Long salesBookingId) {
        try {
            List<Map> salesBookingList = salesBookingRepository.findSalesBookingDetailsBySalesBookingId(salesBookingId);
            double totalDiscount = 0.0;
            double totalAmount = 0.0;
            double totalQuantity = 0.0;
            double totalFreeQuantity = 0.0;
            double productWiseTotalPriceWithDiscount = 0.0;
            double totalAmountWithOutDiscount = 0.0;


            List<Map> salesBookingListnew = new ArrayList<>();

            for (Map item: salesBookingList) {
                Map bookingItem = new HashMap<>();
                Integer quantity = (Integer) item.get("quantity");
                Integer freeQuantity = (Integer) item.get("freeQuantity");
                Float tradePrice = (Float) item.get("tradePrice");

                productWiseTotalPriceWithDiscount = (Double) item.get("productWiseTotalAmount");

                Long productId = Long.parseLong(String.valueOf(item.get("productId")));
                Product product = productService.findById(productId);
                Long productCategoryId = product.getProductCategory().getId();
                ProductCategory productCategory = getProductCategory(product.getProductCategory().getParent());

                /*
                String productCategoryPath = productCategoryService.getTopParentByChild(productCategoryId);
                Long parentCategory = null;
                if (productCategoryPath.contains("/")){
                    String [] arr = productCategoryPath.split("/");
                    parentCategory = Long.parseLong(String.valueOf(arr[0]));
                }*/

                double productWiseTotalPriceWithoutDiscount = tradePrice.doubleValue() * quantity.doubleValue();

                totalAmountWithOutDiscount += productWiseTotalPriceWithoutDiscount;
                totalAmount += productWiseTotalPriceWithDiscount;

                totalQuantity += quantity;
                totalFreeQuantity += freeQuantity;

                totalDiscount += productWiseTotalPriceWithoutDiscount - productWiseTotalPriceWithDiscount;

                bookingItem.putAll(item);
                bookingItem.put("parentCategory", productCategory != null ? productCategory.getId() : null);

                salesBookingListnew.add(bookingItem);
            }

            Map<String, Object> salesBookingViewList = new HashMap<>();
            totalQuantity += totalFreeQuantity;
            salesBookingViewList.put("totalAmount", Double.parseDouble(df.format(totalAmount)));
            salesBookingViewList.put("totalAmountWithOutDiscount", Double.parseDouble(df.format(totalAmountWithOutDiscount)));
            salesBookingViewList.put("totalQuantity", totalQuantity);
            salesBookingViewList.put("totalFreeQuantity", totalFreeQuantity);
            salesBookingViewList.put("totalFreeQuantity", totalFreeQuantity);
            salesBookingViewList.put("totalDiscount", Math.round(totalDiscount * 100.0) / 100.0);
            salesBookingViewList.put("salesBookingList", salesBookingListnew);

            return salesBookingViewList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public boolean deleteSalesBookingDetails(Long salesBookingDetailsId) {
        return salesBookingDetailsService.delete(salesBookingDetailsId);
    }

    public List<Map<String, Object>> getAllProductListWithSalesBookingId(Long semesterId, Long companyId, Long productCategoryId, Long invoiceNatureId, Long salesBookingId) {
        return salesBookingRepository.findAllProductListWithSalesBookingId(semesterId, companyId, productCategoryId, invoiceNatureId, salesBookingId);
    }

    public List<SalesBooking> findSalesBookingListForPaymentCollection(Long distributorId, Long companyId) {
        Optional<Semester> semester = semesterService.findSemesterByDate(LocalDate.now(),companyId);
        ApplicationUser applicationUser = applicationUserService.getApplicationUserFromLoginUser();
        return salesBookingRepository.findByDistributorIdAndSalesOfficerAndCompanyIdAndSemester(distributorId,applicationUser,companyId,semester.get());
    }

    public Map<String, Object>
    getSalesBookingToStockConfirm(Long bookingId) {
        Map<String, Object> returnMap = new HashMap<>();
        List<Map<String, Object>> salesBookingDetails;
        List<Map<String, Object>> resultList = new ArrayList<>();

        if (bookingId == null) {
            return null;
        }

        List<String> salesBookingStatusList = Stream.of(
                SalesBookingStatus.SALES_BOOKED.toString() ,
                SalesBookingStatus.STOCK_CONFIRMED.toString(),
                SalesBookingStatus.TICKET_REQUESTED.toString(),
                SalesBookingStatus.TICKET_CONFIRMED.toString(),
                SalesBookingStatus.ORDER_CONVERTED.toString(),
                SalesBookingStatus.PARTIAL_ORDER_CONVERTED.toString(),
                SalesBookingStatus.TICKET_REJECTED.toString()).collect(Collectors.toList());

        salesBookingDetails = salesBookingRepository.getSalesBookingToStockConfirm(
                bookingId, salesBookingStatusList);
        resultList = salesBookingDetails.stream()
                .map(elt -> makeProductMap(elt))
                .collect(Collectors.toList());
        returnMap.put("salesBookingDetails", resultList);
        return returnMap;
    }

    private Map<String, Object> makeProductMap (Map<String, Object> bookingItem) {

        Map<String, Object> map = new HashMap<>();
        Long userLoginId = applicationUserService.getApplicationUserFromLoginUser().getId();
        Long companyId = Long.parseLong(String.valueOf(bookingItem.get("company_id")));
        Long depotId = Long.parseLong(String.valueOf(bookingItem.get("depot_id")));
        Long productId = Long.parseLong(String.valueOf(bookingItem.get("product_id")));
        Store store = storeService.getStoreByOrganizationAndStoreType(
                organizationService.getOrganizationFromLoginUser(), StoreType.REGULAR);
        Long salesBookingDetailsId =
                Long.parseLong(bookingItem.get("booking_item_id").toString());

        Double blocked_quantity = 0.0D;
        Double stock_quantity = 0.0D;
        Float stock_quantity_uom = 0.0F;

        Map<String, Object> stockMap = productService.getProductWiseStock(companyId, depotId, productId, StoreType.REGULAR.toString());
        Map<String, Object> blockQuantityMap = productService.getStockBlockedQuantity(companyId, depotId, productId);

        if (stockMap.size() > 0)
            stock_quantity = Double.parseDouble(stockMap.get("available_stock").toString());

        /*if (stockMap.get("stock_quantity") != null) {
            stock_quantity =
                    Double.parseDouble(stockMap.get("stock_quantity").toString());
            stock_quantity_uom = Float.parseFloat(stockMap.get("stock_quantity_uom").toString());
        }*/

        if (blockQuantityMap.size() > 0 && blockQuantityMap.get("blockedQuantity") !=null )
            blocked_quantity = (Double) blockQuantityMap.get("blockedQuantity");

        Double booking_quantity = Double.parseDouble(bookingItem.get("booking_quantity").toString());

        map.put("booking_id", bookingItem.get("booking_id").toString());
        map.put("product_id", productId);
        map.put("booking_item_id", salesBookingDetailsId);
        map.put("product_sku", bookingItem.get("product_sku").toString());
        map.put("p_name", bookingItem.get("p_name").toString());
        map.put("item_status", bookingItem.get("item_status").toString());
        map.put("category_name", bookingItem.get("category_name").toString());
        map.put("booking_quantity", booking_quantity);
        map.put("free_quantity", bookingItem.get("free_quantity").toString());
        map.put("ticket_quantity", bookingItem.get("ticket_quantity"));
        map.put("confirm_quantity", bookingItem.get("confirm_quantity"));
        map.put("total_confirm_quantity", bookingItem.get("total_confirm_quantity"));

        if (bookingItem.get("trade_price") != null)
            map.put("trade_price", Double.parseDouble(bookingItem.get("trade_price").toString()));

        if (bookingItem.get("discounted_price") != null)
            map.put("discounted_price",
                    Double.parseDouble(bookingItem.get("discounted_price").toString()));
        if (bookingItem.get("booking_amount") != null)
            map.put("booking_amount",
                Double.parseDouble(bookingItem.get("booking_amount").toString()));
        if (bookingItem.get("discounted_amount") != null)
            map.put("discounted_amount",
                Double.parseDouble(bookingItem.get("discounted_amount").toString()));

        if (bookingItem.get("confirm_amount") != null)
            map.put("confirm_amount",
                    Double.parseDouble(bookingItem.get("confirm_amount").toString()));
        if (bookingItem.get("confirm_discounted_amount") != null)
            map.put("confirm_discounted_amount",
                    Double.parseDouble(bookingItem.get("confirm_discounted_amount").toString()));


        map.put("uom", bookingItem.get("uom").toString());
        map.put("stock_quantity", stock_quantity);
        map.put("short_quantity", (blocked_quantity + booking_quantity) - stock_quantity);
        map.put("stock_quantity_uom", stock_quantity_uom);
        map.put("blocked_quantity", blocked_quantity);
        map.put("atp_quantity", stock_quantity - blocked_quantity);

        return map;
    }

    public SalesBooking updateSalesBooking(
            Map salesBookingMap) {

        if (salesBookingMap == null) {
            return null;
        }

        Long bookingId = Long.valueOf(salesBookingMap.get("id").toString());
        SalesBooking salesBooking = this.findById(bookingId);
        salesBooking.setApprovalStatus(ApprovalStatus.valueOf(
                salesBookingMap.get("approvalStatus").toString()));

        salesBookingRepository.save(salesBooking);

        return salesBooking;
    }

    public List<Map> updateSalesBookingItem(
            List<Map> salesBookingItemList) {

        Map<String, Object> returnMap = new HashMap<>();
        if (salesBookingItemList == null) {
            return null;
        }
        Map salesBookingDetails =salesBookingItemList.stream().findFirst().get();
        Long bookingId =   Long.parseLong(salesBookingDetails.get("booking_id").toString());

        List<SalesBookingDetails> salesBookingDetailsList =
                getSalesBookingDetailsToStockConfirm(salesBookingItemList);
        salesBookingDetailsRepository.saveAll(salesBookingDetailsList);

        String bookingStatus = findBookingStatus(bookingId);
        if (bookingStatus.equals(LIFE_CYCLE_STATUS_COMPLETED)) {
            SalesBooking salesBooking = salesBookingRepository.findById(bookingId).get();
            salesBooking.setIsBookingStockConfirmed("Y");
            salesBookingRepository.save(salesBooking);
        }
        return salesBookingItemList;
    }

    public List<SalesBookingDetails> getSalesBookingDetailsToStockConfirm(
            List<Map> salesBookingItemList) {

        List<SalesBookingDetails> salesBookingDetailsList = new ArrayList<>();

        for (Map<String, Object> map : salesBookingItemList) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {

                String key = entry.getKey();

                if (key.equals("booking_item_id")) {
                    SalesBookingDetails salesBookingDetails = salesBookingDetailsRepository.getById(
                            Long.parseLong(entry.getValue().toString()));

                    /*update if still not order converted*/
                    if (! salesBookingDetails.getSalesBookingStatus().equals("ORDER_CONVERTED"))
                       salesBookingDetails.setSalesBookingStatus(
                            SalesBookingStatus.STOCK_CONFIRMED);

                    salesBookingDetailsList.add(salesBookingDetails);
                }
            }
        }
        return salesBookingDetailsList;
    }

    public Map<String, Object> getConfirmedBookingItemList(
            Long bookingId) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<Map<String, Object>> salesBookingDetails;
        Map<String, Object> returnMap = new HashMap<>();

        List<String> salesBookingStatusList = Stream.of(
                SalesBookingStatus.STOCK_CONFIRMED.toString()).collect(Collectors.toList());

        salesBookingDetails = salesBookingRepository.getSalesBookingToStockConfirm(bookingId, salesBookingStatusList);

        resultList = salesBookingDetails.stream()
                .map(elt -> makeProductMap(elt))
                .collect(Collectors.toList());

        Double totalBookingAmount =
                resultList.stream().filter(o -> o.containsKey("booking_amount"))
                        .mapToDouble(o -> Double.parseDouble(o.get("booking_amount").toString())).sum();

        Double totalDiscountAmount =
                resultList.stream().filter(o -> o.containsKey("discounted_amount"))
                        .mapToDouble(o -> Double.parseDouble(o.get("discounted_amount").toString())).sum();

        returnMap.put("totalBookingAmount", totalBookingAmount);
        returnMap.put("totalDiscountAmount", totalDiscountAmount);
        returnMap.put("resultList", resultList);

        return returnMap;
    }

    public List<Map<String, Object>> getSalesBookingListForSalesOrderCreation(
            Long companyId, LocalDateTime startDate, LocalDateTime endDate,
            Long semesterId, List<Long> soList, List<String> bookingApprovalStatus) {

        Map depotMap = depotService.getDepotByLoginUserId(companyId,
                applicationUserService.getApplicationUserIdFromLoginUser());
        Long depotId = !depotMap.isEmpty() ? Long.parseLong(String.valueOf(depotMap.get("id"))) : null;

        return salesBookingRepository.getSalesBookingListForSalesOrderCreation(
                companyId, startDate, endDate,
                soList, bookingApprovalStatus, depotId);
    }

    public List<Map<String, Object>> getSalesBookingAndSalesOrderDetails(Long salesBookingId) {

        return salesBookingRepository.getSalesBookingAndSalesOrderDetails(salesBookingId);
    }

    public List<Map<String, Object>> getPendingListForApproval(Long companyId, List<Long> soList, String approvalActor, Integer level,Long approvalStepId,Long multiLayerApprovalPathId,Long approvalActorId, String approvalStepName) {
        return salesBookingRepository.getPendingListForApproval(
                companyId, soList,  ApprovalStatus.PENDING.toString(), approvalActor, level, approvalStepId, multiLayerApprovalPathId, approvalActorId, ApprovalStatus.PENDING.getName(), approvalStepName);
    }

    public boolean updateApprovalStatus(Long id, ApprovalStatus approvalStatus) {
        Optional<SalesBooking> salesBookingOptional = salesBookingRepository.findById(id);
        if (!salesBookingOptional.isPresent()) {
            return false;
        }
        SalesBooking salesBooking = salesBookingOptional.get();
        salesBooking.setApprovalStatus(approvalStatus);
        salesBooking.setApprovalDate(LocalDate.now());
        salesBookingRepository.save(salesBooking);
        return true;
    }

    public List<Map> getPendingBookingListByCompanyAndDistributorAndSalesOfficer(Long companyId, Long distributorId, Long salesOfficerId){
        List<Map> list = new ArrayList<>();
         list = salesBookingRepository.getPendingBookingListByCompanyAndDistributorAndSalesOfficer(companyId,distributorId,salesOfficerId);
         return list;
    }

    public Float getTotalAmountBySalesBookingId(Long salesBookingId){
        Float totalAmount = 0.0f;
        Map map = salesBookingRepository.getTotalAmountBySalesBookingId(salesBookingId);
        totalAmount = Float.parseFloat(map.get("total_amount").toString());
        return totalAmount;
    }

    public ProductCategory getProductCategory(ProductCategory productCategory){
        while (productCategory.getParent() != null){
            productCategory = productCategory.getParent();
        }

        return productCategory;
    }

    public List<Map<String, Object>> getBookingActivitiesList(Long bookingId) {

        return salesBookingRepository.getSalesBookingActivitiesList(bookingId);
    }

    public void sendPushNotificationToBookingNextApprover(
            Long companyId, Long salesBookingId, Integer level) {

        SalesBooking salesBooking = findById(salesBookingId);
        List<Map<String,Object>> approvalPathList =
                approvalPathService.getMultiLayerApprovalPathByLevel(companyId, "SALES_BOOKING", level);
        Long applicationUserId = applicationUserService.getApplicationUserIdFromLoginUser();
        List<Map<String, Object>> approvalActorList = new ArrayList<>();
        List<ReportingManager> reportingManagers =
                reportingManagerService.getSalesTeam(salesBooking.getSalesOfficer().getId());
        List<Long> teamIds = reportingManagers.stream()
                .filter(Objects::nonNull) // Ensure the reporting manager object itself is not null
                .filter(e -> e.getReportingTo() != null) // Ensure the 'reportingTo' field is not null
                .map(e -> e.getReportingTo().getId()) // Directly get the 'id' as it's already a Long
                .collect(Collectors.toList());

        for (Map<String, Object> approvalPath: approvalPathList) {
            String approvalActor = approvalPath.get("approvalActor").toString();
            Long multiLayerApprovalPathId = Long.parseLong(approvalPath.get("multiLayerApprovalPathId").toString());
            List<Map<String, Object>> approvalList = new ArrayList<>();
            switch (ApprovalActor.valueOf(approvalActor)) {
                case ROLE:
                    approvalList.addAll(multiLayerApprovalPathRepository.getApprovalListByRole(companyId, "SALES_BOOKING", approvalActor, null, multiLayerApprovalPathId));
                    break;
                case DESIGNATION:
                    approvalList.addAll(multiLayerApprovalPathRepository.getApprovalListByDesignation(companyId, "SALES_BOOKING", approvalActor, null, multiLayerApprovalPathId));
                    break;
                case LOCATION_TYPE:
                    approvalList.addAll(multiLayerApprovalPathRepository.getApprovalListByLocationType(companyId, "SALES_BOOKING", approvalActor, multiLayerApprovalPathId, teamIds));
                    break;
                case DEPOT_IN_CHARGE:
                    //get so deopt manager
                    Depot depot = salesBooking.getDepot();
                    Map<String, Object> depotMap = new HashMap<>();
                    Long approvalActorId = depot.getDepotManager().getId();
                    depotMap.put("applicationUserId", approvalActorId);
                    approvalList.add(depotMap);
                    break;
                case APPLICATION_USER:
                    approvalList.addAll(multiLayerApprovalPathRepository.getApprovalListByApplicationUser(companyId, "SALES_BOOKING", approvalActor, null, multiLayerApprovalPathId));
                    break;
                default:
                    break;
            }
            approvalActorList.addAll(approvalList);
        }

        for (Map<String, Object> approvalActorMap : approvalActorList) {
            Long approvalActorId = approvalActorMap.get("applicationUserId") != null ? Long.parseLong(approvalActorMap.get("applicationUserId").toString()) : null;
            sendPushNotification(approvalActorId, salesBooking);
        }
    }

    public void sendPushNotification(Long approvalActorId, SalesBooking salesBooking) {
        PushNotificationRequest pushNotificationRequest = new PushNotificationRequest();
        String userFCMId = applicationUserService.getUserFCMId(approvalActorId);
        if (userFCMId != null) {
            pushNotificationRequest.setTitle("Booking Approval");
            pushNotificationRequest.setToken(userFCMId);
            pushNotificationRequest.setMessage(salesBooking.getBookingNo()
                    + " from customer " + salesBooking.getDistributor().getDistributorName()
                    + " is waiting for approval");
            Resource resource = new ClassPathResource("/images/notification-icon.png");
            try {
                URL url = resource.getURI().toURL();
                if (url != null)
                    pushNotificationRequest.setImage(String.valueOf(ImageIO.read(url)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            pushNotificationService.sendPushNotificationToToken(pushNotificationRequest);
        }
    }
}
