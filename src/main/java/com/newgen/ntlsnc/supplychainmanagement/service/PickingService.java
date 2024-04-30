package com.newgen.ntlsnc.supplychainmanagement.service;

import com.newgen.ntlsnc.common.CommonConstant;
import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.PickingStatus;
import com.newgen.ntlsnc.common.enums.StoreType;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.Store;
import com.newgen.ntlsnc.globalsettings.service.DocumentSequenceService;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.globalsettings.service.ProductService;
import com.newgen.ntlsnc.salesandcollection.service.SalesOrderService;
import com.newgen.ntlsnc.supplychainmanagement.dto.PickingDetailsDto;
import com.newgen.ntlsnc.supplychainmanagement.dto.PickingDetailsListDto;
import com.newgen.ntlsnc.supplychainmanagement.dto.PickingDto;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvReceive;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvTransactionDetails;
import com.newgen.ntlsnc.supplychainmanagement.entity.Picking;
import com.newgen.ntlsnc.supplychainmanagement.entity.PickingDetails;
import com.newgen.ntlsnc.supplychainmanagement.repository.PickingDetailsRepository;
import com.newgen.ntlsnc.supplychainmanagement.repository.PickingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author kamal
 * @Date ১৮/৪/২২
 */

@Service
public class PickingService implements IService<Picking> {
    @Autowired
    PickingRepository pickingRepository;
    @Autowired
    PickingDetailsRepository pickingDetailsRepository;
    @Autowired
    ProductService productService;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    SalesOrderService salesOrderService;
    @Autowired
    DocumentSequenceService documentSequenceService;

    @Autowired
    InvDeliveryChallanService invDeliveryChallanService;

    @Autowired
    PickingDetailsService pickingDetailsService;
    @Autowired
    InvTransactionService invTransactionService;

    @Transactional
    @Override
    public Picking create(Object object) {
        PickingDto pickingDto = (PickingDto) object;
        Picking picking = new Picking();

        picking.setPickingDate(LocalDate.now());//yyyy-MM-dd
        if(pickingDto.getReason() != ""){
            picking.setReason(pickingDto.getReason().trim());
        }
        picking.setStatus(PickingStatus.PENDING.getCode());
        picking.setPickingNo(
                documentSequenceService.getSequenceByDocumentId(
                        CommonConstant.DOCUMENT_ID_FOR_PICKING));

        picking.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (pickingDto.getCompanyId() != null) {
            picking.setCompany(organizationService.findById(pickingDto.getCompanyId()));
        }
        if (!this.validate(picking)) {
            return null;
        }
        picking = pickingRepository.save(picking);
        List<PickingDetails> pickingDetailsList =
                getPickingDetailsList(pickingDto.getPickingDetailsDtoList(), picking);
        if (pickingDetailsList.size() != pickingDto.getPickingDetailsDtoList().size()) {
            return null;
        }
        pickingDetailsRepository.saveAll(pickingDetailsList);
        return picking;
    }

    @Transactional
    @Override
    public Picking update(Long id, Object object) {
        PickingDto pickingDto = (PickingDto) object;
        Optional<Picking> optionalPicking = pickingRepository.findById(id);
        if (!optionalPicking.isPresent()) {
            return null;
        }
        Picking picking = optionalPicking.get();
        picking.setPickingDate(LocalDate.now());//yyyy-MM-dd
        picking.setOrganization(organizationService.getOrganizationFromLoginUser());
        if (pickingDto.getCompanyId() != null) {
            picking.setCompany(organizationService.findById(pickingDto.getCompanyId()));
        }
        if (!this.validate(picking)) {
            return null;
        }
        picking = pickingRepository.save(picking);
        List<PickingDetails> pickingDetailsList = getPickingDetailsList(pickingDto.getPickingDetailsDtoList(), picking);
        pickingDetailsRepository.saveAll(pickingDetailsList);
        return picking;
    }

    @Transactional
    @Override
    public boolean delete(Long id) {
        Optional<Picking> optionalPicking = pickingRepository.findById(id);
        try {
            if (!optionalPicking.isPresent()) {
                throw new Exception("Picking not exist");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        Picking picking = optionalPicking.get();
        picking.setIsDeleted(true);
        pickingRepository.save(picking);
        List<PickingDetails> pickingDetailsList = pickingDetailsRepository.findAllByPicking(picking);
        pickingDetailsList.forEach(pickingDetails -> pickingDetails.setIsDeleted(true));
        pickingDetailsRepository.saveAll(pickingDetailsList);
        return true;
    }

    @Override
    public Picking findById(Long id) {
        try {
            Optional<Picking> optionalPicking = pickingRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalPicking.isPresent()) {
                throw new Exception("Picking Not exist with id " + id);
            }
            return optionalPicking.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<Picking> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return pickingRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }


    private List<PickingDetails> getPickingDetailsList(List<PickingDetailsDto> pickingDetailsDtoList, Picking picking) {
        List<PickingDetails> pickingDetailsList = new ArrayList<>();
        if (validateCreatePicking(pickingDetailsDtoList)) {

        for (PickingDetailsDto pdd : pickingDetailsDtoList) {
            PickingDetails pickingDetails = new PickingDetails();
            if (pdd.getId() != null) {
                pickingDetails = pickingDetailsRepository.findById(pdd.getId()).get();
            }
            pickingDetails.setQuantity(pdd.getQuantity());
            pickingDetails.setOrganization(picking.getOrganization());
            pickingDetails.setPicking(picking);

            if(pdd.getSalesOrderDetailsId() != null){
                pickingDetails.setSalesOrderDetails(salesOrderService.findSalesOrderDetailsById(pdd.getSalesOrderDetailsId()));
            }

            if (pdd.getProductId() != null) {
                pickingDetails.setProduct(productService.findById(pdd.getProductId()));
            }
            if (pdd.getSalesOrderId() != null) {
                pickingDetails.setSalesOrder(salesOrderService.findById(pdd.getSalesOrderId()));
            }
            pickingDetailsList.add(pickingDetails);
        }

        }
        return pickingDetailsList;
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }

    private boolean validateCreatePicking (
            List<PickingDetailsDto> pickingDetailsDtoList) {

        for (PickingDetailsDto pdd : pickingDetailsDtoList) {
            Long productId = pdd.getProductId();
            Long orderId = pdd.getSalesOrderId();
            Long salesOrderDetailsId = pdd.getSalesOrderDetailsId();
            Map orderItemChallanInfo =
                    invDeliveryChallanService.getOrderItemChallanInformation(
                            salesOrderDetailsId);
            Long companyId = Long.parseLong(orderItemChallanInfo.get("company_id").toString());
            Long depotId = Long.parseLong(orderItemChallanInfo.get("depot_id").toString());

            Long orderPickingRemainQuantity = getRemainingOrderQuantity(
                    orderId, salesOrderDetailsId, orderItemChallanInfo);
            Map<String, Object> stockQuantity =
                    productService.getProductWiseStock(companyId, depotId, productId,
                            StoreType.REGULAR.toString());

            List<String> statusList = Stream.of(PickingStatus.PENDING.getCode(),
                    PickingStatus.CONFIRMED.getCode()).collect(Collectors.toList());
            Map<String, Object> pickedBlockQuantityMap =
                    productService.getPickedBlockedQuantity(companyId, depotId,
                            productId, statusList);

            /*Map<String, Object> pickingSummary =
                    invDeliveryChallanService.getOrderProductPickingSummary(orderId, productId);*/

            Double atpQuantity = 0.0D;
            Double pickedBlockedQuantity = 0.0D;
            Double available_stock = 0.0D;

            if (stockQuantity.size() > 0)
                available_stock = Double.parseDouble(stockQuantity.get("available_stock").toString());

            if (pickedBlockQuantityMap.size() > 0 && pickedBlockQuantityMap.get("pickedBlockedQuantity") != null)
                pickedBlockedQuantity = (Double) pickedBlockQuantityMap.get("pickedBlockedQuantity");

            atpQuantity = available_stock - pickedBlockedQuantity;

            if (orderPickingRemainQuantity < pdd.getQuantity()) {
                return false;

            }
            /*else if (orderPickingRemainQuantity > atpQuantity) {
                return false;
            }*/
            /*else if (orderPickingRemainQuantity > available_stock) {
                return false;
            }*/
        }
        return true;
    }

    public Long getRemainingOrderQuantity(
            Long orderId, Long orderDetailsId,
            Map product) {

        Long orderQuantity = Long.parseLong(String.valueOf(product.get("order_quantity")));
        Organization organization = organizationService.getOrganizationFromLoginUser();
        List<PickingDetails> pickingDetails =
                pickingDetailsRepository.findAllByOrganizationAndSalesOrderIdAndSalesOrderDetailsIdAndIsActiveTrueAndIsDeletedFalse(organization,orderId,orderDetailsId);
        Long remainingQuantity = orderQuantity;
        if(pickingDetails.size()>0) {
            for (PickingDetails pickingItem : pickingDetails) {
                Long pickedQuantity = Long.valueOf(pickingItem.getQuantity());

                if (pickingItem.getGoodQty() !=null)
                    pickedQuantity = Long.valueOf(pickingItem.getGoodQty());

                Optional<InvTransactionDetails> transactionDetails =
                        invTransactionService.getChallanDetailsByPickingDetailsId(
                                pickingItem.getId());

                if (! pickingItem.getPicking().getStatus().equals("CANCELLED")) {
                    if (transactionDetails.isPresent()) {
                        /*added pertial picking-challan-quantity to remaining quantity*/
                        pickedQuantity = transactionDetails.get().getQuantity().longValue();
                    }
                    remainingQuantity = remainingQuantity - pickedQuantity;
                }
            }
        }

        if (product.get("order_challan_quantity") != null) {
            Double orderChallanQuantity =
                    Double.parseDouble(product.get("order_challan_quantity").toString());
            remainingQuantity = remainingQuantity - orderChallanQuantity.longValue();
            if (remainingQuantity < 0)
                remainingQuantity = 0L;
        }
        return remainingQuantity;
    }

    public Boolean getPickedItemConfirmedStatus(Long pickingDetailsId){

        Optional<PickingDetails> optionalPickingDetails =
                pickingDetailsRepository.
                        findByIdAndIsActiveTrueAndIsDeletedFalse(
                        pickingDetailsId);
        if (optionalPickingDetails.isPresent()) {
            PickingDetails pickingItem = optionalPickingDetails.get();
            if (pickingItem.getGoodQty() != null) {
                return  true;
            }
        }
        return false;
    }

    public String getPickedStatus(Long pickingId) {

        Optional<Picking> optionalPicking =
                pickingRepository.
                        findByIdAndIsActiveTrueAndIsDeletedFalse(
                                pickingId);
        if (optionalPicking.isPresent()) {
            Picking picking = optionalPicking.get();
            if (picking.getStatus() != null) {
                return  picking.getStatus();
            }
        }
        return null;
    }

    public List<Map<String, Object>> getOrderListByPickingIdWise(Long pickingId) {
        return pickingRepository.getOrderListByPickingId(pickingId);
    }

    public List<Map<String, Object>> getPickingList(Long pickingId, Long companyId) {
        return pickingRepository.findByPickingList(pickingId, companyId);
    }

    public List<Map> getPickingListByDistributorWise(Long companyId, Long distributorId) {
        List<Map> pickingList = invDeliveryChallanService.getPickingListByDistributorWiseWithoutDate(companyId, distributorId);
        return pickingList;
    }

    public List<Map> getProductListByPickingId(Long pickingId) {
        List<Map> productList = pickingDetailsService.getProductListByPickingWise(pickingId);
        productList = productList.stream()
                .map(elt -> pickingProductMap(elt))
                .collect(Collectors.toList());
        return productList;
    }

    public Map pickingProductMap (Map product) {

        Map map = new HashMap<>();
        Long orderQuantity = Long.parseLong(String.valueOf(product.get("order_quantity")));
        Long product_id = Long.parseLong(String.valueOf(product.get("product_id")));
        Long sales_order_details_id = Long.parseLong(String.valueOf(product.get("sales_order_details_id")));
        Long pickedConfirmedUndeliverdQuantityTotal= 0L;
        Long remainingQuantity = 0L;

        if (product.get("order_challan_quantity") != null) {
            Double orderChallanQuantity =
                    Double.parseDouble(product.get("order_challan_quantity").toString());
            List<Map> pickedConfirmedUndeliverdList =
                    pickingDetailsRepository.getPickedConfirmedUndeliverdQuantityBySalesOrderDetailsId(
                            product_id, sales_order_details_id);
            for (Map pickedConfirmedUndeliverdMap: pickedConfirmedUndeliverdList) {
                if (pickedConfirmedUndeliverdMap.get("undeliveredQuantity") != null) {
                    Double pickedConfirmedUndeliverdQuantity =
                            Double.parseDouble(pickedConfirmedUndeliverdMap.get("undeliveredQuantity").toString());
                    pickedConfirmedUndeliverdQuantityTotal = (long) (pickedConfirmedUndeliverdQuantityTotal + pickedConfirmedUndeliverdQuantity);
                }
            }

            remainingQuantity = (long) (orderQuantity -
                                (orderChallanQuantity.longValue() + pickedConfirmedUndeliverdQuantityTotal));
            if (remainingQuantity < 0)
                remainingQuantity = 0L;
        }
        map.putAll(product);
        map.put("remainingQuantity", remainingQuantity);

        return map;
    }

    public void setConfirmPickingStatus(PickingDetailsListDto pickingDetailsListDto) {
        try {
           Optional<Picking> optionalPicking = pickingRepository.findById(pickingDetailsListDto.getPickingId());
           Picking picking = optionalPicking.get();
           picking.setStatus(pickingDetailsListDto.getStatus());
           pickingRepository.save(picking);
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Picking rejectPicking(Object object) {
        PickingDto pickingDto = (PickingDto) object;
        Optional<Picking> optionalPicking = pickingRepository.findById(pickingDto.getId());
        if (!optionalPicking.isPresent()) {
            return null;
        }
        Picking picking = optionalPicking.get();
        List<Map> challanInfo = invDeliveryChallanService.getPickingDeliveredInfo(picking.getId());
        if (challanInfo.size()> 0 ) {
            return null;
        }
        picking.setPickingDate(LocalDate.now());//yyyy-MM-dd
        picking.setOrganization(organizationService.getOrganizationFromLoginUser());
        if (pickingDto.getCompanyId() != null) {
            picking.setCompany(organizationService.findById(pickingDto.getCompanyId()));
        }
        picking.setReason(pickingDto.getReason());
        picking.setStatus(PickingStatus.CANCELLED.getCode());
        if (!this.validate(picking)) {
            return null;
        }
        return pickingRepository.save(picking);
    }

}
