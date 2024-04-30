package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.CommonConstant;
import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.common.enums.IntactType;
import com.newgen.ntlsnc.common.enums.StoreType;
import com.newgen.ntlsnc.globalsettings.entity.AccountingYear;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.PackSize;
import com.newgen.ntlsnc.globalsettings.entity.Store;
import com.newgen.ntlsnc.globalsettings.service.*;
import com.newgen.ntlsnc.salesandcollection.dto.SalesReturnProposalDetailsDto;
import com.newgen.ntlsnc.salesandcollection.dto.SalesReturnProposalDto;
import com.newgen.ntlsnc.salesandcollection.entity.SalesReturnProposal;
import com.newgen.ntlsnc.salesandcollection.entity.SalesReturnProposalDetails;
import com.newgen.ntlsnc.salesandcollection.repository.SalesInvoiceRepository;
import com.newgen.ntlsnc.salesandcollection.repository.SalesReturnProposalDetailsRepository;
import com.newgen.ntlsnc.salesandcollection.repository.SalesReturnProposalRepository;
import com.newgen.ntlsnc.supplychainmanagement.service.BatchService;
import com.newgen.ntlsnc.supplychainmanagement.service.InvDeliveryChallanService;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author marziah
 * @Date 20/04/22
 */

@Service
public class SalesReturnProposalService implements IService<SalesReturnProposal> {
    @Autowired
    OrganizationService organizationService;
    @Autowired
    SalesReturnProposalRepository salesReturnProposalRepository;
    @Autowired
    SalesReturnProposalDetailsRepository salesReturnProposalDetailsRepository;
    @Autowired
    DistributorService distributorService;
    @Autowired
    SalesInvoiceRepository salesInvoiceRepository;
    @Autowired
    SalesInvoiceService salesInvoiceService;
    @Autowired
    ProductService productService;
    @Autowired
    BatchService batchService;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    DocumentSequenceService documentSequenceService;
    @Autowired
    InvDeliveryChallanService invDeliveryChallanService;
    @Autowired
    AccountingYearService accountingYearService;
    @Autowired
    ProductTradePriceService productTradePriceService;
    @Autowired
    TradeDiscountService tradeDiscountService;
    @Autowired
    DepotLocationMapService depotLocationMapService;

    @Transactional
    @Override
    public SalesReturnProposal create(Object object) {
        try {
            SalesReturnProposalDto salesReturnProposalDto = (SalesReturnProposalDto) object;
            SalesReturnProposal salesReturnProposal = new SalesReturnProposal();
            Optional<SalesReturnProposal> optionalSalesReturnProposal =
                    salesReturnProposalRepository
                            .findByCompanyIdAndSalesInvoiceIdAndDeliveryChallanIdAndDistributorIdAndApprovalStatusAndIsActiveTrueAndIsDeletedFalse(salesReturnProposalDto.getCompanyId(), salesReturnProposalDto.getSalesInvoiceId(), salesReturnProposalDto.getDeliveryChallanId(),
                                    salesReturnProposalDto.getDistributorId(), ApprovalStatus.DRAFT);
            ApprovalStatus approvalStatus = ApprovalStatus.valueOf(salesReturnProposalDto.getApprovalStatus());
//            for final submit empty validation
            if (approvalStatus == ApprovalStatus.PENDING) {
                if (optionalSalesReturnProposal.isPresent()) {
                    List<SalesReturnProposalDetails> existDetailsList = salesReturnProposalDetailsRepository.findAllBySalesReturnProposalIdAndIsActiveTrueAndIsDeletedFalse(optionalSalesReturnProposal.get().getId());
                    if (existDetailsList.size() == 0) {
                        throw new RuntimeException("No return quantity found");
                    }
                } else {
                    throw new RuntimeException("No return quantity found");
                }
            }

            if (!optionalSalesReturnProposal.isPresent()) { // create
                salesReturnProposal.setProposalNo(documentSequenceService.getSequenceByDocumentId(CommonConstant.DOCUMENT_ID_FOR_SALES_RETURN_PROPOSAL));
                salesReturnProposal.setDeliveryChallan(invDeliveryChallanService.findById(salesReturnProposalDto.getDeliveryChallanId()));
                salesReturnProposal.setDistributor(distributorService.findById(salesReturnProposalDto.getDistributorId()));
                salesReturnProposal.setSalesInvoice(salesInvoiceService.findById(salesReturnProposalDto.getSalesInvoiceId()));
                salesReturnProposal.setCompany(organizationService.findById(salesReturnProposalDto.getCompanyId()));
                salesReturnProposal.setSalesOfficer(applicationUserService.getApplicationUserFromLoginUser());
                salesReturnProposal.setOrganization(organizationService.getOrganizationFromLoginUser());
                salesReturnProposal.setSalesOfficer(applicationUserService.getApplicationUserFromLoginUser());
                salesReturnProposal.setInvoiceFromDate(LocalDate.parse(
                        salesReturnProposalDto.getInvoiceFromDate(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                salesReturnProposal.setInvoiceToDate(LocalDate.parse(
                        salesReturnProposalDto.getInvoiceToDate(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            } else {
                salesReturnProposal = optionalSalesReturnProposal.get();
            }

            salesReturnProposal.setApprovalStatus(approvalStatus);
            //when submit from mobile
            if (salesReturnProposal.getApprovalStatus() == ApprovalStatus.PENDING) {
                salesReturnProposal.setProposalDate(LocalDate.parse(salesReturnProposalDto.getProposalDate()));    //yyyy-MM-dd
                salesReturnProposal.setReturnReason(salesReturnProposalDto.getReturnReason());
            }

            if (!this.validate(salesReturnProposal)) {
                return null;
            }
            salesReturnProposal = salesReturnProposalRepository.save(salesReturnProposal);

            if (salesReturnProposal.getApprovalStatus() == ApprovalStatus.DRAFT) {  // details only save when Add to Cart. always come 1 items at a time when draft
                SalesReturnProposalDetails salesReturnProposalDetails = getSalesReturnProposalDetails(salesReturnProposalDto.getSalesReturnProposalDetailsDtoList().get(0), salesReturnProposal);
                salesReturnProposalDetailsRepository.save(salesReturnProposalDetails);
            } else {  //in final submit, all inactive will be deleted
                List<SalesReturnProposalDetails> salesReturnProposalDetailsList = salesReturnProposalDetailsRepository.findAllBySalesReturnProposalIdAndIsActiveFalse(salesReturnProposal.getId());
                salesReturnProposalDetailsList.forEach(d -> d.setIsDeleted(true));
                salesReturnProposalDetailsRepository.saveAll(salesReturnProposalDetailsList);
            }

            return salesReturnProposal;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    @Override
    public SalesReturnProposal update(Long id, Object object) {
        return null;
    }

    @Transactional
    @Override
    public boolean delete(Long id) {
        try {
            SalesReturnProposal salesReturnProposal = findById(id);
            salesReturnProposal.setIsDeleted(true);
            salesReturnProposalRepository.save(salesReturnProposal);
            List<SalesReturnProposalDetails> salesReturnProposalDetailsList = salesReturnProposalDetailsRepository.findAllBySalesReturnProposal(salesReturnProposal);
            salesReturnProposalDetailsList.forEach(salesReturnProposalDetails -> salesReturnProposalDetails.setIsDeleted(true));
            salesReturnProposalDetailsRepository.saveAll(salesReturnProposalDetailsList);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public boolean inactiveChild(Long deliveryChallanId, Long productId) {
        try {
            Optional<SalesReturnProposal> optionalSalesReturnProposal = salesReturnProposalRepository.findByDeliveryChallanIdAndApprovalStatusAndIsActiveTrueAndIsDeletedFalse(deliveryChallanId, ApprovalStatus.DRAFT);
            List<SalesReturnProposalDetails> salesReturnProposalDetailsList = salesReturnProposalDetailsRepository.findAllBySalesReturnProposalIdAndProductIdAndIsDeletedFalse(optionalSalesReturnProposal.get().getId(), productId);
            salesReturnProposalDetailsList.forEach(d -> {
                d.setIsActive(false);
                d.setQuantity(0f);
            });
            salesReturnProposalDetailsRepository.saveAll(salesReturnProposalDetailsList);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public SalesReturnProposal findById(Long id) {
        try {
            Optional<SalesReturnProposal> optionalSalesReturnProposal = salesReturnProposalRepository.findById(id);
            if (!optionalSalesReturnProposal.isPresent()) {
                throw new Exception("Sales Return Proposal Not exist");
            }
            return optionalSalesReturnProposal.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public SalesReturnProposalDetails findChildByChildId(Long salesReturnProposalDetailsId) {
        try {
            Optional<SalesReturnProposalDetails> optional = salesReturnProposalDetailsRepository.findById(salesReturnProposalDetailsId);
            if (!optional.isPresent()) {
                throw new Exception("Sales Return Proposal Details Not exist with id " + salesReturnProposalDetailsId);
            }
            return optional.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<SalesReturnProposal> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return salesReturnProposalRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    public SalesReturnProposal findBySalesInvoiceIdAndIsActiveTrueAndIsDeletedFalse(Long salesInvoiceId) {
        return salesReturnProposalRepository.findBySalesInvoiceIdAndIsActiveTrueAndIsDeletedFalse(salesInvoiceId).get();
    }

    private SalesReturnProposalDetails getSalesReturnProposalDetails(SalesReturnProposalDetailsDto salesReturnProposalDetailsDto, SalesReturnProposal salesReturnProposal) {
        SalesReturnProposalDetails salesReturnProposalDetails = new SalesReturnProposalDetails();
        Optional<SalesReturnProposalDetails> optional = salesReturnProposalDetailsRepository.findBySalesReturnProposalIdAndProductIdAndBatchIdAndIsDeletedFalse(salesReturnProposal.getId(),
                salesReturnProposalDetailsDto.getProductId(), salesReturnProposalDetailsDto.getBatchId());

        if (!optional.isPresent()) { // create operation
            salesReturnProposalDetails.setProduct(productService.findById(salesReturnProposalDetailsDto.getProductId()));
            salesReturnProposalDetails.setOrganization(salesReturnProposal.getOrganization());
            salesReturnProposalDetails.setSalesReturnProposal(salesReturnProposal);
            salesReturnProposalDetails.setProductTradePrice(productTradePriceService.findById(salesReturnProposalDetailsDto.getProductTradePriceId()));
            if (salesReturnProposalDetailsDto.getTradeDiscountId() != null) { // discount is optional
                salesReturnProposalDetails.setTradeDiscount(tradeDiscountService.findById(salesReturnProposalDetailsDto.getTradeDiscountId()));
            }

            salesReturnProposalDetails.setBatch(batchService.findById(salesReturnProposalDetailsDto.getBatchId()));
            salesReturnProposalDetails.setRate(salesReturnProposalDetailsDto.getRate());
        } else { // update
            salesReturnProposalDetails = optional.get();
        }
        salesReturnProposalDetails.setIsActive(true);
        IntactType intactType = IntactType.valueOf(salesReturnProposalDetailsDto.getIntactType());
        salesReturnProposalDetails.setIntactType(IntactType.IP);  // always IP

        if (intactType == IntactType.BU) {
            salesReturnProposalDetails.setQuantity(Float.valueOf(String.valueOf(salesReturnProposalDetailsDto.getQuantity() / salesReturnProposalDetails.getProduct().getItemSize())));
        } else if (intactType == IntactType.MC) {
            PackSize packSize = salesReturnProposalDetails.getProduct().getPackSize();
            salesReturnProposalDetails.setQuantity(Float.valueOf(String.valueOf(packSize.getPackSize() * salesReturnProposalDetailsDto.getQuantity())));
        } else if (intactType == IntactType.IP) {
            salesReturnProposalDetails.setQuantity(Float.valueOf(String.valueOf(salesReturnProposalDetailsDto.getQuantity())));
        }
        return salesReturnProposalDetails;
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }

    public List<Map> getProductWiseSalesReturnProposalDetailsListByDeliveryChallanId(Long deliveryChallanId) {
        return salesReturnProposalRepository.getProductWiseSalesReturnProposalDetailsListByDeliveryChallanId(deliveryChallanId);
    }

    public Map getApprovalStatusSummaryByCompanyAndSalesOfficer(Long companyId, Long salesOfficerId) {
        return salesReturnProposalRepository.getApprovalStatusSummaryByCompanyAndSalesOfficer(companyId, salesOfficerId);
    }

    public Map getApprovalStatusSummaryByCompanyAndLoginSalesOfficer(Long companyId) {
        Long salesOfficerId = applicationUserService.getApplicationUserFromLoginUser().getId();
        return getApprovalStatusSummaryByCompanyAndSalesOfficer(companyId, salesOfficerId);
    }

    public List<Map> getReturnProposalProductListByDeliveryChallanId(Long deliveryChallanId) {
        List<Map> returnProductList = salesReturnProposalRepository.findReturnProposalProductDetailsListByDeliveryChallanId(deliveryChallanId);
        return returnProductList;
    }

    public Map getSummaryBySalesReturn(Long salesReturnProposalId) {  // for mobile
        Map infoMap = new HashMap();
        infoMap.put("detailsInfo", getDetailsInfo(salesReturnProposalId));
        infoMap.put("returnProductDetailsList", getReturnProductDetailsListBySalesReturnProposal(salesReturnProposalId));
        return infoMap;
    }

    public List<Map> getReturnProductDetailsListBySalesReturnProposal(Long salesReturnProposalId) {
        return salesReturnProposalRepository.getReturnProductDetailsListBySalesReturnProposal(salesReturnProposalId);
    }

    public Map getDetailsInfo(Long salesReturnProposalId) {
        return salesReturnProposalRepository.getDetailsInfo(salesReturnProposalId);
    }

    public List<Map> getProposalListForReturnByCompanyAndLocationAndAccountingYear(Long companyId, Long locationId, Long accountingYearId) {
        String fromDate = null;
        String toDate = null;
        List<Map> mapList;
        if (accountingYearId != null) { // null = All
            AccountingYear accountingYear = accountingYearService.findById(accountingYearId);
            fromDate = accountingYear.getStartDate().toString();
            toDate = accountingYear.getEndDate().toString();
        }

        mapList = getProposalListForReturnByCompanyAndLocationAndDateRange(companyId, locationId, fromDate, toDate);
        return mapList;
    }

    public List<Map> getProposalListForReturnByCompanyAndLocationAndDateRange(Long companyId, Long locationId, String fromDate, String toDate) {
        List<Map> mapList = salesReturnProposalRepository.getProposalListForReturnByCompanyAndLocationAndDateRange(companyId, locationId, fromDate, toDate);
        return mapList;
    }

    public Map getSalesReturnProposalSummaryAndDetailsInfoById(
            Long salesReturnProposalId
            ,LocalDate invoiceFromDate, LocalDate invoiceToDate
            ,Long distributorId) { // for web
        Map infoMap = new HashMap();
        List<Map<String, Object>> detailsList = salesReturnProposalRepository.getSalesReturnProposalDetailsListBySalesReturnProposalId(salesReturnProposalId);
        Map breadCrumbMap = salesReturnProposalRepository.getSalesReturnProposalInfosById(salesReturnProposalId);
        infoMap.put("proposalInfosForBreadCrumb", breadCrumbMap);
        // query return multiple data. improvement needed
        /*infoMap.put("locationOfDepot", depotLocationMapService.getLocationOfDepot(
                Long.parseLong(breadCrumbMap.get("company_id").toString())
                , Long.parseLong(breadCrumbMap.get("depot_id").toString())));*/

        infoMap.put("detailsList", detailsList);
        return infoMap;
    }

    public List<SalesReturnProposalDetails> getSalesReturnProposalDetailsListBySalesReturnProposalId(Long salesReturnProposalId) {
        try {
            return salesReturnProposalDetailsRepository.findAllBySalesReturnProposalIdAndIsActiveTrueAndIsDeletedFalse(salesReturnProposalId);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public SalesReturnProposal save(SalesReturnProposal salesReturnProposal) {
        try {
            return salesReturnProposalRepository.save(salesReturnProposal);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map<String, Object>> getPendingListForApproval(
            Long companyId, List<Long> soList, String approvalActor, String approvalStatus,
            Integer level, Long approvalStepId, Long multiLayerApprovalPathId, Long approvalActorId, String approvalStepName) {
        return salesReturnProposalRepository.getPendingListForApproval(
                companyId, soList, approvalStatus, approvalActor, level, approvalStepId, multiLayerApprovalPathId, approvalActorId, ApprovalStatus.PENDING.getName(), approvalStepName);
    }

    public void updateApprovalStatus(Long id, ApprovalStatus approvalStatus) {
        try {
            Optional<SalesReturnProposal> salesReturnProposalOptional = salesReturnProposalRepository.findById(id);
            if (!salesReturnProposalOptional.isPresent()) {
                return;
            }
            SalesReturnProposal salesReturnProposal = salesReturnProposalOptional.get();
            salesReturnProposal.setApprovalStatus(approvalStatus);
            salesReturnProposal.setApprovalDate(LocalDate.now());
            salesReturnProposalRepository.save(salesReturnProposal);
        } catch (Exception e) {
            throw new RuntimeException("Sales Return Proposal approval process can't be Executed. Something went wrong!");
        }
    }

    public Float getSalesReturnProposalAmountById(Long salesReturnProposalId) {
        try {
            Float returnAmount = 0.0f;
            returnAmount = Float.parseFloat(salesReturnProposalRepository.getSalesReturnProposalAmountById(salesReturnProposalId).get("return_amount").toString());
            return returnAmount;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map> getCustomerInvoiceListBatch(
             Long batchId
            ,LocalDate invoiceFromDate, LocalDate invoiceToDate
            ,Long distributorId) {
        return salesReturnProposalRepository.getCustomerInvoiceListBatch(
                batchId, distributorId, invoiceFromDate, invoiceToDate);
    }

    public List<Map> getCustomerChallanListInvoice(
            Long invoiceId, LocalDate invoiceFromDate,
            LocalDate invoiceToDate, Long distributorId) {
        return salesReturnProposalRepository.getCustomerChallanListInvoice(
                invoiceId, distributorId, invoiceFromDate, invoiceToDate);
    }

    public List<Map> getCustomerReceivedBatchListProduct(
            Long companyId, Long productId, LocalDate invoiceFromDate,
            LocalDate invoiceToDate, Long distributorId) {
        return salesReturnProposalRepository.getDistributorReceivedBatchListProduct(
                companyId, distributorId, productId, invoiceFromDate, invoiceToDate
        );
    }

    public SalesReturnProposalDetails findBySalesReturnProposalDetailsId(Long id) {
        try {
            Optional<SalesReturnProposalDetails> salesReturnProposalDetailsOptional = salesReturnProposalDetailsRepository.findById(id);
            if (!salesReturnProposalDetailsOptional.isPresent()) {
                throw new Exception("Sales Return Proposal Details Not exist");
            }
            return salesReturnProposalDetailsOptional.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public boolean saveAll(List<SalesReturnProposal> salesReturnProposalList){
        salesReturnProposalRepository.saveAll(salesReturnProposalList);
        return true;
    }

    @Transactional
    public boolean saveAllForDetails(List<SalesReturnProposalDetails> salesReturnProposalDetailsList){
        salesReturnProposalDetailsRepository.saveAll(salesReturnProposalDetailsList);
        return true;
    }


}
