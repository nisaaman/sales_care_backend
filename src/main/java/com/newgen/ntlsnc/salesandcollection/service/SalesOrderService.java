package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.CommonConstant;
import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.common.enums.SalesBookingStatus;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.Product;
import com.newgen.ntlsnc.globalsettings.service.*;
import com.newgen.ntlsnc.notification.email.dto.EmailDto;
import com.newgen.ntlsnc.notification.email.service.EmailService;
import com.newgen.ntlsnc.notification.pushNotification.dto.PushNotificationRequest;
import com.newgen.ntlsnc.notification.pushNotification.service.PushNotificationService;
import com.newgen.ntlsnc.notification.sms.dto.SmsDto;
import com.newgen.ntlsnc.notification.sms.service.SmsService;
import com.newgen.ntlsnc.salesandcollection.dto.SalesOrderDetailsDto;
import com.newgen.ntlsnc.salesandcollection.dto.SalesOrderDto;
import com.newgen.ntlsnc.salesandcollection.entity.*;
import com.newgen.ntlsnc.salesandcollection.repository.SalesOrderDetailsRepository;
import com.newgen.ntlsnc.salesandcollection.repository.SalesOrderRepository;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import com.newgen.ntlsnc.usermanagement.service.ReportingManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author kamal
 * @Date ১২/৪/২২
 */

@Service
public class SalesOrderService implements IService<SalesOrder> {
    @Autowired
    OrganizationService organizationService;
    @Autowired
    @Lazy
    SalesBookingService salesBookingService;
    @Autowired
    ProductTradePriceService productTradePriceService;
    @Autowired
    SalesOrderRepository salesOrderRepository;
    @Autowired
    SalesOrderDetailsRepository salesOrderDetailsRepository;
    @Autowired
    LocationManagerMapService locationManagerMapService;
    @Autowired
    AccountingYearService accountingYearService;
    @Autowired
    DocumentSequenceService documentSequenceService;
    @Autowired
    ProductService productService;
    @Autowired
    TradeDiscountService tradeDiscountService;
    @Autowired
    ReportingManagerService reportingManagerService;
    @Autowired
    PushNotificationService pushNotificationService;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    EmailService emailService;
    @Autowired
    SmsService smsService;

    @Transactional
    @Override
    public SalesOrder create(Object object) {
        SalesOrderDto salesOrderDto = (SalesOrderDto) object;
        SalesOrder salesOrder = new SalesOrder();
        salesOrder.setOrderNo(documentSequenceService.getSequenceByDocumentId(CommonConstant.DOCUMENT_ID_FOR_SALES_ORDER));
        salesOrder.setOrderDate(LocalDate.now());    //yyyy-MM-dd
        salesOrder.setDeliveryDate(LocalDate.parse(salesOrderDto.getDeliveryDate()));    //yyyy-MM-dd
        salesOrder.setApprovalStatus(ApprovalStatus.PENDING);
        salesOrder.setOrganization(organizationService.getOrganizationFromLoginUser());
        if (salesOrderDto.getCompanyId() != null) {
            Organization company = organizationService.findById(salesOrderDto.getCompanyId());
            salesOrder.setCompany(company);
        }
        if (salesOrderDto.getSalesBookingId() != null) {
            SalesBooking salesBooking = salesBookingService.findById(salesOrderDto.getSalesBookingId());
            salesOrder.setSalesBooking(salesBooking);
        }

        salesOrder.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (!this.validate(salesOrder)) {
            return null;
        }
        salesOrder = salesOrderRepository.save(salesOrder);
        List<SalesOrderDetails> salesOrderDetailsList = getSalesOrderDetailsList(
                salesOrderDto.getSalesOrderDetailsDto(), salesOrder, false);
        salesOrderDetailsRepository.saveAll(salesOrderDetailsList);

        return salesOrder;
    }

    @Transactional
    @Override
    public SalesOrder update(Long id, Object object) {

        SalesOrderDto salesOrderDto = (SalesOrderDto) object;
        Optional<SalesOrder> salesOrderOptional = salesOrderRepository.findById(id);
        if (!salesOrderOptional.isPresent()) {
            return null;
        }
        SalesOrder salesOrder = salesOrderOptional.get();
        //salesOrder.setOrderNo(salesOrderDto.getOrderNo());
        //salesOrder.setOrderDate(LocalDate.parse(salesOrderDto.getOrderDate()));    //yyyy-MM-dd
//        salesOrder.setDeliveryDate(LocalDate.parse(salesOrderDto.getDeliveryDate()));    //yyyy-MM-dd
//        salesOrder.setOrganization(organizationService.getOrganizationFromLoginUser());
//        if (salesOrderDto.getCompanyId() != null) {
//            salesOrder.setCompany(organizationService.findById(salesOrderDto.getCompanyId()));
//        }
//        if (salesOrderDto.getSalesBookingId() != null) {
//            salesOrder.setSalesBooking(salesBookingService.findById(salesOrderDto.getSalesBookingId()));
//        }
//        if (!this.validate(salesOrder)) {
//            return null;
//        }
        salesOrder = salesOrderRepository.save(salesOrder);
        List<SalesOrderDetails> salesOrderDetailsList = getSalesOrderDetailsList(
                salesOrderDto.getSalesOrderDetailsDto(), salesOrder, true);
        salesOrderDetailsRepository.saveAll(salesOrderDetailsList);
        return salesOrder;
    }

    @Transactional
    @Override
    public boolean delete(Long id) {
        Optional<SalesOrder> salesOrderOptional = salesOrderRepository.findById(id);
        try {
            if (!salesOrderOptional.isPresent()) {
                throw new Exception("Sales Order not exist");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        SalesOrder salesOrder = salesOrderOptional.get();
        salesOrder.setIsDeleted(true);
        salesOrderRepository.save(salesOrder);

        List<SalesOrderDetails> salesOrderDetailsList = salesOrderDetailsRepository.findAllBySalesOrder(salesOrder);
        salesOrderDetailsList.forEach(salesBookingDetails -> salesBookingDetails.setIsDeleted(true));
        salesOrderDetailsRepository.saveAll(salesOrderDetailsList);
        return true;
    }

    @Override
    public SalesOrder findById(Long id) {
        try {
            Optional<SalesOrder> optionalSalesOrder = salesOrderRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalSalesOrder.isPresent()) {
                throw new Exception("Sales order Not exist with id " + id);
            }
            return optionalSalesOrder.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    @Override
    public List<SalesOrder> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return salesOrderRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    private List<SalesOrderDetails> getSalesOrderDetailsList(
            List<SalesOrderDetailsDto> salesOrderDetailsDtoList, SalesOrder salesOrder,
            boolean isForUpdate) {
        List<SalesOrderDetails> salesOrderDetailsList = new ArrayList<>();
        for (SalesOrderDetailsDto sod : salesOrderDetailsDtoList) {
            SalesOrderDetails salesOrderDetails;
            if (sod.getSalesOrderDetailsId() != null) {
                Optional<SalesOrderDetails> salesOrderDetailsOpt =
                        salesOrderDetailsRepository.findById(sod.getSalesOrderDetailsId());
                salesOrderDetails = salesOrderDetailsOpt.get();
            } else {
                salesOrderDetails = new SalesOrderDetails();
            }

            if (sod.getSalesOrderDetailsId() != null) {
                salesOrderDetails = salesOrderDetailsRepository.findById(sod.getSalesOrderDetailsId()).get();
            }
            salesOrderDetails.setQuantity(sod.getOrderQuantity());
            salesOrderDetails.setSalesOrder(salesOrder);
            salesOrderDetails.setOrganization(salesOrder.getOrganization());

            SalesBookingDetails salesBookingDetails = salesBookingService.
                    findSalesBookingDetailsBySalesBookingDetailsId(
                            sod.getSalesBookingDetailsId());

            Product product = productService.findById(sod.getProductId());
            salesBookingDetails.setProduct(product);
            salesBookingDetails.setProductTradePrice(
                    productTradePriceService.findById(sod.getProductTradePriceId()));

            if (sod.getTradeDiscountId() != null)
                salesBookingDetails.setTradeDiscount(
                        tradeDiscountService.findById(sod.getTradeDiscountId()));
            if (!isForUpdate && sod.getRemainingBookingQuantity().intValue() == sod.getOrderQuantity().intValue())
                salesBookingDetails.setSalesBookingStatus(SalesBookingStatus.ORDER_CONVERTED);
            else if (isForUpdate && sod.getBookingQuantity().intValue() == sod.getOrderQuantity().intValue())
                salesBookingDetails.setSalesBookingStatus(SalesBookingStatus.ORDER_CONVERTED);
            else
                salesBookingDetails.setSalesBookingStatus(SalesBookingStatus.PARTIAL_ORDER_CONVERTED);

            salesOrderDetails.setSalesBookingDetails(salesBookingDetails);

            salesOrderDetailsList.add(salesOrderDetails);
        }
        return salesOrderDetailsList;
    }


    public SalesOrderDetails findSalesOrderDetailsById(Long id) {
        try {
            Optional<SalesOrderDetails> optionalSalesOrderDetails = salesOrderDetailsRepository.findById(id);
            if (!optionalSalesOrderDetails.isPresent()) {
                throw new Exception("Sales order details Not exist with id " + id);
            }
            return optionalSalesOrderDetails.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }

    public List<SalesOrder> getSalesOrderListByBookingId(Long bookingId) {
        List<SalesOrder> salesOrderList =
                salesOrderRepository.getSalesOrderListByBookingId(bookingId);

        return salesOrderList;
    }

    public List<SalesOrderDetails> getSalesOrderProducts(Long orderId) {
        List<SalesOrderDetails> salesOrderItemList =
                salesOrderRepository.getSalesOrderProducts(orderId);

        return salesOrderItemList;
    }

    public List<Map<String, Object>> getSaleOrderListBySO(Long companyId, LocalDateTime startDate, LocalDateTime endDate, Long semesterId, List<Long> soList) {
        return salesOrderRepository.findSaleOrderListBySO(companyId, startDate, endDate, semesterId, soList);
    }

    public List<Map<String, Object>> getSalesBookingDetailsForSalesOrderCreation(
            Long companyId, Long accountingYearId, Long semesterId, Long userLoginId,
            Long locationId) {

        if (accountingYearId == null) {
            accountingYearId = accountingYearService.getCurrentAccountingYearId(companyId, LocalDate.now());
        }
        Map<String, LocalDate> accountingYear = accountingYearService.getAccountingYearDate(
                accountingYearId);
        List<Long> salesOfficers;
        if (locationId == null) {
            salesOfficers = locationManagerMapService.
                    getSalesOfficerListFromUserLoginIdOrLocationId(
                            userLoginId, locationId, companyId);

            if (salesOfficers == null) {
                salesOfficers = reportingManagerService.getAllSalesOfficeFromCompany(companyId);
            }
        } else
            salesOfficers = locationManagerMapService.
                    getSalesOfficerListFromUserLoginIdOrLocationId(
                            userLoginId, locationId, companyId);

        return salesBookingService.getSalesBookingListForSalesOrderCreation(
                companyId, accountingYear.get("startDate").atStartOfDay(),
                accountingYear.get("endDate").atStartOfDay(), semesterId, salesOfficers,
                Arrays.asList(String.valueOf(ApprovalStatus.APPROVED)));
    }

    public List<Map<String, Object>> getSalesBookingAndSalesOrderDetails(Long salesBookingId) {

        return salesBookingService.getSalesBookingAndSalesOrderDetails(salesBookingId);
    }

    public List<Map<String, Object>> getSalesBookingDetailsInSalesOrder(
            Long salesBookingId, Long salesOrderId) {

        return salesOrderRepository.getSalesBookingDetailsInSalesOrder(salesBookingId, salesOrderId);
    }

    public List<Map<String, Object>> getUndeliveredSalesOrderList(
            Long companyId, Long accountingYearId, Long semesterId) {

        Map<String, LocalDate> accountingYear = accountingYearService.getAccountingYearDate(
                accountingYearId);

        return salesOrderRepository.undeliveredSalesOrderList(companyId,
                accountingYear.get("startDate"), accountingYear.get("endDate"), semesterId);
    }

    public List<Map<String, Object>> getPendingListForApproval(Long companyId, List<Long> soList, String approvalActor, Integer level, Long approvalStepId, Long multiLayerApprovalPathId, Long approvalActorId, String approvalStepName) {
        return salesOrderRepository.getPendingListForApproval(
                companyId, soList, ApprovalStatus.PENDING.toString(), approvalActor, level, approvalStepId, multiLayerApprovalPathId, approvalActorId, ApprovalStatus.PENDING.getName(), approvalStepName);
    }

    public void updateApprovalStatus(Long id, ApprovalStatus approvalStatus) {
        try {
            Optional<SalesOrder> salesOrderOptional = salesOrderRepository.findById(id);
            if (!salesOrderOptional.isPresent()) {
                return;
            }
            SalesOrder salesOrder = salesOrderOptional.get();
            salesOrder.setApprovalStatus(approvalStatus);
            salesOrder.setApprovalDate(LocalDate.now());
            salesOrderRepository.save(salesOrder);
        } catch (Exception e) {
            throw new RuntimeException("Sales Order process can't be Executed. Something went wrong!");
        }
    }

    public Map findSalesOrderWithSo(Long orderId) {
        return salesOrderRepository.findSalesOrderWithSo(orderId);
    }

    public void sendPushNotificationToOrderApproval(Long orderId) {
        Map orderInfo = findSalesOrderWithSo(orderId);
        Long salesOfficerId = Long.valueOf(String.valueOf(orderInfo.get("sales_officer_id")));
        PushNotificationRequest pushNotificationRequest = new PushNotificationRequest();
        String userFCMId = applicationUserService.getUserFCMId(salesOfficerId);
        if (userFCMId != null) {
            pushNotificationRequest.setTitle("Order Approval");
            pushNotificationRequest.setToken(userFCMId);
            pushNotificationRequest.setMessage(orderInfo.get("order_no")
                    + " of " + orderInfo.get("booking_no")
                    + " is approved.");
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

    public void sendMailToOrderApproval(Long orderId) {
        Map orderInfo = findSalesOrderWithSo(orderId);
        EmailDto emailDto = new EmailDto();
        //emailDto.setRecipient("shamsun.nahar@newgen-bd.com");
        if (orderInfo.get("sales_officer_email") != null) {
            emailDto.setRecipient(String.valueOf(orderInfo.get("sales_officer_email")));
            emailDto.setSubject("Order Approval");
            emailDto.setMsgBody(orderInfo.get("order_no")
                    + " of " + orderInfo.get("booking_no")
                    + " is approved.");

            if (orderInfo.get("sales_officer_email") != null)
                emailService.sendSimpleMail(emailDto);
        }
    }

    public void sendSmsNotificationToOrderApproval(Long orderId) {
        Map orderInfo = findSalesOrderWithSo(orderId);
        String dis_contact_no = "8801738300224";
        //String dis_contact_no = String.valueOf(orderInfo.get("dis_contact_no"));
        //String dis_contact_no = "8801576611928";
        if (dis_contact_no != null) {
            SmsDto smsDto = new SmsDto();
            smsDto.setPhoneNumber(dis_contact_no);
            smsDto.setMessage(orderInfo.get("order_no")
                    + " of " + orderInfo.get("booking_no")
                    + " is approved.");

            smsService.sendSms(smsDto);
        }
    }

}
