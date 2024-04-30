package com.newgen.ntlsnc.supplychainmanagement.service;

import com.newgen.ntlsnc.common.CommonConstant;
import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.*;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.Product;
import com.newgen.ntlsnc.globalsettings.entity.Store;
import com.newgen.ntlsnc.globalsettings.service.DocumentSequenceService;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.globalsettings.service.ProductService;
import com.newgen.ntlsnc.globalsettings.service.StoreService;
import com.newgen.ntlsnc.salesandcollection.entity.SalesOrderDetails;
import com.newgen.ntlsnc.salesandcollection.entity.SalesReturnProposal;
import com.newgen.ntlsnc.salesandcollection.entity.SalesReturnProposalDetails;
import com.newgen.ntlsnc.salesandcollection.service.SalesOrderService;
import com.newgen.ntlsnc.supplychainmanagement.dto.InvDeliveryChallanDto;
import com.newgen.ntlsnc.supplychainmanagement.dto.InvTransactionDetailsDto;
import com.newgen.ntlsnc.supplychainmanagement.dto.InvTransactionDto;
import com.newgen.ntlsnc.supplychainmanagement.entity.Batch;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvTransaction;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvTransactionDetails;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvTransfer;
import com.newgen.ntlsnc.supplychainmanagement.repository.InvTransactionDetailsRepository;
import com.newgen.ntlsnc.supplychainmanagement.repository.InvTransactionRepository;
import com.newgen.ntlsnc.supplychainmanagement.repository.InvTransferRepository;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
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
 * @Date ২৬/৫/২২
 */
@Service
public class InvTransactionService implements IService<InvTransaction> {
    @Autowired
    OrganizationService organizationService;
    @Autowired
    InvTransactionRepository invTransactionRepository;
    @Autowired
    BatchService batchService;
    @Autowired
    StoreService storeService;
    @Autowired
    ProductService productService;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    SalesOrderService salesOrderService;
    @Autowired
    InvTransactionDetailsRepository invTransactionDetailsRepository;
    @Autowired
    InvDeliveryChallanService invDeliveryChallanService;
    @Autowired
    InvTransferRepository invTransferRepository;
    @Autowired
    DocumentSequenceService documentSequenceService;
    @Autowired
    PickingService pickingService;
    @Autowired
    StockService stockService;

    @Autowired
    PickingDetailsService pickingDetailsService;

    @Transactional
    @Override
    public InvTransaction create(Object object) {
        InvTransactionDto invTransactionDto = (InvTransactionDto) object;
        InvTransaction invTransaction = new InvTransaction();

        if (null == invTransactionDto.getTransactionDate()) {
            invTransaction.setTransactionDate( LocalDate.now());
        } else
            invTransaction.setTransactionDate(LocalDate.parse(invTransactionDto.getTransactionDate()));

        invTransaction.setTransactionType(InvTransactionType.valueOf(invTransactionDto.getTransactionType()));

        invTransaction.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (invTransactionDto.getCompanyId() != null) {
            invTransaction.setCompany(organizationService.findById(invTransactionDto.getCompanyId()));
        }

        invTransaction = invTransactionRepository.save(invTransaction);

        /*start for delivery challan transaction*/
        if (invTransactionDto.getTransactionType().equals("DELIVERY_CHALLAN")) {
            InvDeliveryChallanDto invDeliveryChallanDto = invTransactionDto.getInvDeliveryChallanDto();

            invDeliveryChallanDto.setInvTransactionId(invTransaction.getId());
            invDeliveryChallanDto.setDeliveryDate(LocalDate.now().toString());
            invDeliveryChallanDto.setOrganizationId(
                    organizationService.getOrganizationFromLoginUser().getId());

            invDeliveryChallanService.create(invDeliveryChallanDto, invTransaction);
        }

        if (! invTransactionDto.getTransactionType().equals("TRANSFER_RECEIVE")) {
            List<InvTransactionDetails> invTransactionDetailsList =
                    getInvTransactionDetailsList(
                            invTransactionDto.getInvTransactionDetailsDtoList(),
                            invTransaction, invTransactionDto.getTransactionType().toString());
        }
        /*end for delivery challan transaction */

        /*start for TRANSFER_RECEIVE*/
        else if (invTransactionDto.getTransactionType().equals("TRANSFER_RECEIVE")) {
            getTransferReceiveInfo(invTransactionDto.getId(), invTransactionDto, invTransaction);
        }
        /*end for TRANSFER_RECEIVE*/

        return invTransaction;
    }

    @Transactional
    @Override
    public InvTransaction update(Long id, Object object) {
        InvTransactionDto invTransactionDto = (InvTransactionDto) object;
        InvTransaction invTransaction = this.findById(invTransactionDto.getId());
        invTransaction.setTransactionDate(LocalDate.parse(invTransactionDto.getTransactionDate()));
        invTransaction.setTransactionType(InvTransactionType.RETURN);
        invTransaction.setOrganization(organizationService.getOrganizationFromLoginUser());
        if (invTransactionDto.getCompanyId() != null) {
            invTransaction.setCompany(organizationService.findById(invTransactionDto.getCompanyId()));
        }

        invTransaction = invTransactionRepository.save(invTransaction);
        List<InvTransactionDetails> invTransactionDetailsList =
                getInvTransactionDetailsList(invTransactionDto.getInvTransactionDetailsDtoList(),
                        invTransaction, null);
        invTransactionDetailsRepository.saveAll(invTransactionDetailsList);
        return invTransaction;

    }

    @Transactional
    public InvTransaction startNewTransaction(Object object) {

        InvTransactionDto invTransactionDto = (InvTransactionDto) object;
        InvTransaction invTransaction = new InvTransaction();
        invTransaction.setTransactionDate(LocalDate.now());
        invTransaction.setTransactionType(InvTransactionType.valueOf(
                invTransactionDto.getTransactionType()));
        invTransaction.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (invTransactionDto.getCompanyId() != null) {
            invTransaction.setCompany(organizationService.findById(invTransactionDto.getCompanyId()));
        }

        invTransaction = save(invTransaction);
        return invTransaction;
    }

    public List<InvTransactionDetails> getInvTransactionDetails(
            List<InvTransactionDetailsDto> invTransactionDetailsDtoList, InvTransaction invTransaction) {
        List<InvTransactionDetails> invTransactionDetailsList = new ArrayList<>();

        for (InvTransactionDetailsDto itdd : invTransactionDetailsDtoList) {
            InvTransactionDetails invTransactionDetails = new InvTransactionDetails();
            if (itdd.getId() != null) {
                invTransactionDetails = invTransactionDetailsRepository.findById(itdd.getId()).get();
            }
            invTransactionDetails.setQuantity(itdd.getQuantity());
            Product product = productService.findById(itdd.getProductId());
            invTransactionDetails.setQuantityInUom((float) (product.getItemSize() * itdd.getQuantity()));
            invTransactionDetails.setRate(itdd.getRate());
            invTransactionDetails.setOrganization(organizationService.getOrganizationFromLoginUser());
            invTransactionDetails.setInvTransaction(invTransaction);

            if (itdd.getBatchId() != null) {
                invTransactionDetails.setBatch(batchService.findById(itdd.getBatchId()));
            }
            Store fromStore = storeService.findById(itdd.getFromStoreId());
            invTransactionDetails.setFromStore(fromStore);

            Store toStore = storeService.findById(itdd.getToStoreId());
            invTransactionDetails.setToStore(toStore);

            if (itdd.getProductId() != null) {
                invTransactionDetails.setProduct(product);
            }
            invTransactionDetailsList.add(invTransactionDetails);
        }
        return invTransactionDetailsList;
    }

    @Transactional
    @Override
    public boolean delete(Long id) {
        InvTransaction invTransaction = findById(id);
        invTransaction.setIsDeleted(true);
        invTransactionRepository.save(invTransaction);
        List<InvTransactionDetails> invTransactionDetailsList = invTransactionDetailsRepository.findAllByInvTransaction(invTransaction);
        invTransactionDetailsList.forEach(invTransactionDetails -> invTransactionDetails.setIsDeleted(true));
        invTransactionDetailsRepository.saveAll(invTransactionDetailsList);
        return true;
    }

    @Transactional
    @Override
    public InvTransaction findById(Long id) {
        try {
            Optional<InvTransaction> optionalInvTransaction = invTransactionRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalInvTransaction.isPresent()) {
                throw new Exception("Inventory Transaction Not exist with id " + id);
            }
            return optionalInvTransaction.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    @Override
    public List<InvTransaction> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return invTransactionRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }

    private List<InvTransactionDetails> getInvTransactionDetailsList(
            List<InvTransactionDetailsDto> invTransactionDetailsDtoList,
            InvTransaction invTransaction,
            String invTransactionType) {
        List<InvTransactionDetails> invTransactionDetailsList = new ArrayList<>();

        for (InvTransactionDetailsDto itd : invTransactionDetailsDtoList) {
            InvTransactionDetails invTransactionDetails = new InvTransactionDetails();

            if (invTransactionType.equals("DELIVERY_CHALLAN")) {
                invTransactionDetails.setRate(
                        getWeightedAverageRate(itd.getProductId(), invTransaction.getCompany().getId()));
            }
            else {
                invTransactionDetails.setRate(itd.getRate());
            }

            invTransactionDetails.setInvTransaction(invTransaction);
            invTransactionDetails.setQuantity(itd.getQuantity());
            Product product = productService.findById(itd.getProductId());
            invTransactionDetails.setQuantityInUom((float) (product.getItemSize() * itd.getQuantity()));

            //if pack entry found
            /*invTransactionDetails.setQuantityInUom(getQuantityInUom(
                    itd.getProductId(), itd.getQuantity()));*/

            invTransactionDetails.setQaStatus(QaStatus.valueOf(itd.getQaStatus()));

            if (itd.getQaById() != null)
                invTransactionDetails.setQaBy(applicationUserService.findById(itd.getQaById()));

            if (itd.getQaDate() != null)
                invTransactionDetails.setQaDate(LocalDateTime.parse(itd.getQaDate(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            invTransactionDetails.setOrganization(organizationService.getOrganizationFromLoginUser());

            invTransactionDetails.setSalesOrderDetails(
                    salesOrderService.findSalesOrderDetailsById(itd.getSalesOrderDetailsId()));

            if (itd.getId() != null) {
                invTransactionDetails = invTransactionDetailsRepository.findById(itd.getId()).get();
            }
            if (itd.getBatchId() != null) {
                Batch batch = batchService.findById(itd.getBatchId());
                invTransactionDetails.setBatch(batch);
            }
            if (itd.getProductId() != null) {
                invTransactionDetails.setProduct(productService.findById(itd.getProductId()));
            }
            if (itd.getToStoreId() != null) {
                Store store = storeService.findById(itd.getToStoreId());
                invTransactionDetails.setFromStore(store);
            }
            else {
                Map store = storeService.getStore(String.valueOf(StoreType.REGULAR));
                invTransactionDetails.setFromStore(
                        storeService.findById(Long.parseLong(store.get("id").toString())));
            }
            if (itd.getFromStoreId() != null) {
                Long id = itd.getFromStoreId();
                Store store = storeService.findById(id);
                invTransactionDetails.setFromStore(store);
            }

            if (invTransactionType.equals("DELIVERY_CHALLAN") && itd.getPickingId()!=null) {
                invTransactionDetails.setPickingDetails(pickingDetailsService.findById(itd.getPickingDetailsId()));

                //pickingDetailsService.updatePickingDetails(invTransactionDetails,
                //itd.getSalesOrderDetailsId(), itd.getPickingId());
            }

            invTransactionDetailsRepository.save(invTransactionDetails);

        }
        return invTransactionDetailsList;
    }

    @Transactional
    public InvTransaction save(InvTransaction invTransaction) {
        return invTransactionRepository.save(invTransaction);
    }

    @Transactional
    public List<InvTransactionDetails> saveAllTransactionDetails(List<InvTransactionDetails> invTransactionDetailsList) {
        return invTransactionDetailsRepository.saveAll(invTransactionDetailsList);
    }

    @Transactional
    public void getTransferReceiveInfo (Long invTransactionId,
                                         InvTransactionDto invTransactionDto,
                                         InvTransaction invTransaction) {
        List<InvTransactionDetails> invTransactionDetailsSaveList = new ArrayList<>();
        InvTransaction invTransactionReceive = this.findById(invTransactionId);
        InvTransfer invTransferReceive =
                invTransferRepository.
                        findByInvTransactionIdAndIsActiveTrueAndIsDeletedFalse(
                                invTransactionId).get();

        InvTransfer invTransfer = new InvTransfer();
        invTransfer.setTransferNo(
                documentSequenceService.getSequenceByDocumentId(
                        CommonConstant.DOCUMENT_ID_FOR_INV_TRANSFER_RCV));
        invTransfer.setFromDepot(invTransferReceive.getFromDepot());
        invTransfer.setToDepot(invTransferReceive.getToDepot());
        invTransfer.setInvTransaction(invTransaction);
        invTransfer.setCompany(invTransferReceive.getCompany());
        invTransfer.setOrganization(organizationService.getOrganizationFromLoginUser());
        invTransfer.setRemarks(invTransactionDto.getRemarks());
        invTransfer.setInvTransfer(invTransferReceive);

        if (null == invTransactionDto.getTransactionDate()) {
            invTransfer.setTransferDate(LocalDate.now());
        } else
            invTransfer.setTransferDate(
                    LocalDate.parse(invTransactionDto.getTransactionDate()));

        invTransferRepository.save(invTransfer);

        List<InvTransactionDetails> invTransactionDetailsList =
                invTransactionDetailsRepository.findAllByInvTransaction(invTransactionReceive);

        for (InvTransactionDetails itd : invTransactionDetailsList) {
            InvTransactionDetails invTransactionDetails = new InvTransactionDetails();
            invTransactionDetails.setInvTransaction(invTransaction);
            invTransactionDetails.setRate(
                        getWeightedAverageRate(itd.getProduct().getId(), invTransferReceive.getCompany().getId()));

            invTransactionDetails.setQuantity(itd.getQuantity());
            invTransactionDetails.setQuantityInUom(getQuantityInUom(
                    itd.getProduct().getId(), itd.getQuantity().intValue()));

            invTransactionDetails.setOrganization(organizationService.getOrganizationFromLoginUser());

            if (itd.getBatch().getId() != null) {
                Batch batch = batchService.findById(itd.getBatch().getId());
                invTransactionDetails.setBatch(batch);
            }
            if (itd.getProduct().getId() != null) {
                Product product = productService.findById(itd.getProduct().getId());
                invTransactionDetails.setProduct(product);
            }
            if (invTransactionDto.getStoreType() != null) {
                Map store = storeService.getStore(String.valueOf(invTransactionDto.getStoreType()));
                invTransactionDetails.setToStore(storeService.findById(
                        Long.parseLong(store.get("id").toString())));
                /*invTransactionDetails.setToStore(itd.getFromStore());*/
                invTransactionDetails.setFromStore(itd.getToStore());

                if (invTransactionDto.getStoreType().equals("QUARANTINE")) {
                    invTransactionDetails.setQaStatus(QaStatus.IN_PROGRESS);
                    if (itd.getQaBy() == null)
                        invTransactionDetails.setQaBy(applicationUserService.getApplicationUserFromLoginUser());
                    else
                        invTransactionDetails.setQaBy(applicationUserService.getUserById(itd.getQaBy().getId()));

                    if (itd.getQaDate() != null)
                        invTransactionDetails.setQaDate(LocalDateTime.parse(itd.getQaDate().toString(),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    else
                        invTransactionDetails.setQaDate(LocalDateTime.now());
                }
            }
            /*else {
                Map store = storeService.getStore(String.valueOf(StoreType.REGULAR));
                invTransactionDetails.setToStore(
                        storeService.findById(Long.parseLong(store.get("id").toString())));
            }*/
            invTransactionDetailsSaveList.add(invTransactionDetails);
        }
        invTransactionDetailsRepository.saveAll(invTransactionDetailsSaveList);
    }

    @Transactional
    public InvTransactionDetails findInvTransactionDetailsById(Long id) {
        return invTransactionDetailsRepository.findInvTransactionDetailsById(id);
    }

    @Transactional
    public List<InvTransactionDetails> findAllInvTransactionDetails(Long id) {
        return invTransactionDetailsRepository.findAllInvTransactionDetailsByInvTransactionId(id);
    }

    public Map findOrderItemDelivery(Long salesOrderDetailsId) {
        return invTransactionDetailsRepository.findOrderItemDelivery(salesOrderDetailsId);
    }

    public List<Map> findOrderItemsDelivery(List<SalesOrderDetails> salesOrderDetailsList) {
        return invTransactionDetailsRepository.findOrderItemsDelivery(salesOrderDetailsList);
    }

    public List<Map> findInvoiceOfBooking(Long bookingId) {
        return invTransactionDetailsRepository.findInvoiceOfBooking(bookingId);
    }

    public Map<String, Object> getProductStock (Long companyId,
                                                Long productId, Long depotId, Long storeId) {

        Map<String, Object> stockMap =
                invTransactionDetailsRepository.getProductStock(companyId,
                        productId, depotId, storeId);

        return stockMap;
    }

    public List<InvTransactionDetails> findAllInvTransactionDetailsByStore(Long storeId) {
        return invTransactionDetailsRepository.findByToStoreIdAndIsDeletedFalse(storeId);
    }

    public List<Map>  getProductBatchListWithStock (Long companyId,
                                                    Long productId, Long depotId) {
        Store store = storeService.getStoreByOrganizationAndStoreType(
                organizationService.getOrganizationFromLoginUser(), StoreType.REGULAR);
        List<Map> batchListWithStock =
                invTransactionDetailsRepository.getProductBatchListWithStock(
                        companyId, productId, depotId, store.getId());

        return batchListWithStock;
    }

    private Float getQuantityInUom (Long productId, Integer quantity) {

        Float quantityInUom = 0.0F;
        if (productId != null) {
            Integer packSize =
                    productService.findById(productId).getPackSize().getPackSize();
            quantityInUom = Float.valueOf(packSize * quantity);
        }
        return quantityInUom;
    }

    public Float getWeightedAverageRate (Long productId, Long companyId) {

        Float weightedAverageRate = 0.0F;
        Long organizationId = organizationService
                .getOrganizationFromLoginUser().getId();
        if (productId != null) {
            weightedAverageRate =
                    invTransactionDetailsRepository
                            .getWeightedAverageRate(productId, companyId);
        }
        return weightedAverageRate;
    }

    public Map makeBatchStockMap (Map product, Long depotId) {

        Map map = new HashMap<>();
        Long companyId = Long.parseLong(String.valueOf(product.get("company_id")));
        Long productId = Long.parseLong(String.valueOf(product.get("id")));
        Long orderId = Long.parseLong(String.valueOf(product.get("order_id")));
        Long orderDetailsId = Long.parseLong(String.valueOf(product.get("sales_order_details_id")));
        Long remainingQuantity = pickingService.getRemainingOrderQuantity(orderId, orderDetailsId, product);
        /*List<Map> batchList = getProductBatchListWithStock(
                companyId, productId, depotId);*/
        List<Map<String, Object>>  batchList =
                stockService.getProductWiseBatchStockInfo(companyId, productId, StoreType.REGULAR.toString());

        map.putAll(product);
        map.put("batchList", batchList);
        Map<String, Object> stockQuantity = productService.getProductWiseStock(companyId, depotId, productId, StoreType.REGULAR.toString());
        //Map<String, Object> blockQuantityMap = productService.getStockBlockedQuantity(companyId, depotId, productId);
        List<String> statusList = Stream.of(PickingStatus.PENDING.getCode(),
                PickingStatus.CONFIRMED.getCode()).collect(Collectors.toList());

        Map<String, Object> pickedBlockQuantityMap = productService.getPickedBlockedQuantity(companyId, depotId, productId, statusList);
        Map<String, Object> pickingSummary = invDeliveryChallanService.getOrderProductPickingSummary(orderId, productId);

        Double blockedQuantity = 0.0D;
        Double atpQuantity = 0.0D;
        Double pickedBlockedQuantity = 0.0D;
        Double orderProductPickQuantity = 0.0D;
        Double available_stock = 0.0D;

        if (stockQuantity.size() > 0)
            available_stock = Double.parseDouble(stockQuantity.get("available_stock").toString());

        if (pickedBlockQuantityMap.size() > 0 && pickedBlockQuantityMap.get("pickedBlockedQuantity") !=null)
            pickedBlockedQuantity = (Double) pickedBlockQuantityMap.get("pickedBlockedQuantity");

            atpQuantity = available_stock - pickedBlockedQuantity;

        if (atpQuantity >= 0)
            map.put("atpQuantity", atpQuantity);
        else
            map.put("atpQuantity", 0.0D);

        if (pickingSummary.size() > 0)
            orderProductPickQuantity = Double.parseDouble(pickingSummary.get("product_pick_quantity").toString());

        map.put("orderProductPickQuantity", orderProductPickQuantity);
        map.put("stockQuantity", available_stock);
        map.put("remainingQuantity", remainingQuantity);
        map.put("pickedBlockQuantityMap", pickedBlockQuantityMap);

        return map;
    }

    public String getTransferReceivedStatus (Long invTransactionId) {
        String receivedStatus = "";
        InvTransfer invTransfer =
                invTransferRepository.
                        findByInvTransactionIdAndIsActiveTrueAndIsDeletedFalse(invTransactionId).get();

        Long invTransferReferenceNo = invTransfer.getId();
        /*InvTransfer transferReceived = invTransferRepository.
                findByInvTransferIdAndIsActiveTrueAndIsDeletedFalse(invTransferReferenceNo).get();*/

        Map transferReceived = invTransferRepository.
                getTransferReceived(invTransferReferenceNo);

        if (transferReceived.size()>0) {
            receivedStatus = CommonConstant.LIFE_CYCLE_STATUS_COMPLETED;
        }
        else receivedStatus = CommonConstant.LIFE_CYCLE_STATUS_PENDING;

        return receivedStatus;
    }

    @Transactional
    public InvTransaction createForSalesReturn(SalesReturnProposal salesReturnProposal) {
        try {
            InvTransaction invTransaction = new InvTransaction();
            invTransaction.setTransactionDate(LocalDate.now());
            invTransaction.setTransactionType(InvTransactionType.RETURN);
            invTransaction.setOrganization(salesReturnProposal.getOrganization());
            invTransaction.setCompany(salesReturnProposal.getCompany());
            invTransaction = invTransactionRepository.save(invTransaction);
            return invTransaction;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public void createDetailsForSalesReturn(List<SalesReturnProposalDetails> salesReturnProposalDetailsList, InvTransaction invTransaction) {
        try {
            List<InvTransactionDetails> invTransactionDetailsList = new ArrayList<>();
            Store store = storeService.getStoreByOrganizationAndStoreType(invTransaction.getOrganization(), StoreType.QUARANTINE);
            salesReturnProposalDetailsList.forEach(s -> {
                InvTransactionDetails d = new InvTransactionDetails();
                d.setQuantity(s.getQuantity());
                d.setQuantityInUom(s.getProduct().getItemSize() * s.getProduct().getPackSize().getPackSize() * s.getQuantity());
                d.setRate(s.getRate());
                d.setBatch(s.getBatch());
                d.setToStore(store);
                d.setProduct(s.getProduct());
                d.setInvTransaction(invTransaction);
                d.setOrganization(s.getOrganization());
                invTransactionDetailsList.add(d);
            });
            invTransactionDetailsRepository.saveAll(invTransactionDetailsList);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    @Transactional
    public void setProductionManufacturingCost(
            InvTransactionDetails invTransactionDetails) {
        invTransactionDetailsRepository.save(invTransactionDetails);
    }

    public Optional<InvTransactionDetails> getChallanDetailsByPickingDetailsId(Long pickingDetailsId) {
        return invTransactionDetailsRepository.findByPickingDetailsIdAndIsActiveTrueAndIsDeletedFalse(pickingDetailsId);
    }
}
