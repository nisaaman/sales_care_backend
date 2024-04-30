package com.newgen.ntlsnc.supplychainmanagement.service;

import com.newgen.ntlsnc.common.CommonConstant;
import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.InvTransactionType;
import com.newgen.ntlsnc.common.enums.StoreType;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.Product;
import com.newgen.ntlsnc.globalsettings.repository.DepotRepository;
import com.newgen.ntlsnc.globalsettings.service.*;
import com.newgen.ntlsnc.supplychainmanagement.dto.InvReceiveDto;
import com.newgen.ntlsnc.supplychainmanagement.dto.InvTransactionDetailsDto;
import com.newgen.ntlsnc.supplychainmanagement.dto.InvTransactionDto;
import com.newgen.ntlsnc.supplychainmanagement.entity.*;
import com.newgen.ntlsnc.supplychainmanagement.repository.InvReceiveRepository;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import com.newgen.ntlsnc.usermanagement.entity.Permission;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import com.newgen.ntlsnc.usermanagement.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author kamal
 * @Date ১৭/৪/২২
 */

@Service
public class InvReceiveService implements IService<InvReceive> {
    @Autowired
    InvTransactionService invTransactionService;
    @Autowired
    InvReceiveRepository invReceiveRepository;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    DepotRepository depotRepository;
    @Autowired
    DepotService depotService;
    @Autowired
    BatchService batchService;
    @Autowired
    StoreService storeService;
    @Autowired
    ProductService productService;
    @Autowired
    AccountingYearService accountingYearService;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    InvClaimService invClaimService;
    @Autowired
    DocumentSequenceService documentSequenceService;
    @Autowired
    PermissionService permissionService;

    @Transactional
    @Override
    public InvReceive create(Object object) {
        InvReceiveDto invReceiveDto = (InvReceiveDto) object;

        //====================================== InvTransaction part start =================
        InvTransactionDto invTransactionDto = (InvTransactionDto) invReceiveDto.getInvTransactionDto();
        InvTransaction invTransaction = new InvTransaction();
        invTransaction.setTransactionDate(LocalDate.now());
        invTransaction.setTransactionType(InvTransactionType.valueOf(invTransactionDto.getTransactionType()));
        invTransaction.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (invReceiveDto.getCompanyId() != null) {
            Organization company = organizationService.findById(invReceiveDto.getCompanyId());
            invTransaction.setCompany(company);
        }
        if (!this.validate(invTransaction)) {
            return null;
        }
        invTransaction = invTransactionService.save(invTransaction);
//====================================== InvTransaction part end =================
//====================================== InvReceive part start =================
        InvReceive invReceive = new InvReceive();
        invReceive.setReceiveNo(
                documentSequenceService.getSequenceByDocumentId(
                        CommonConstant.DOCUMENT_ID_FOR_PRODUCTION_RECEIVE));
        invReceive.setReceiveDate(LocalDate.now()); //yyyy-MM-dd
        invReceive.setRemarks(invReceiveDto.getRemarks());
        invReceive.setInvTransaction(invTransaction);
        invReceive.setOrganization(organizationService.getOrganizationFromLoginUser());
        if (invReceiveDto.getCompanyId() != null) {
            invReceive.setCompany(organizationService.findById(invReceiveDto.getCompanyId()));
        }
        if (invReceiveDto.getDepotId() != null) {
            invReceive.setDepot(depotRepository.findById(invReceiveDto.getDepotId()).orElse(null));
        }
        if (!this.validate(invReceive)) {
            return null;
        }
        invReceive = invReceiveRepository.save(invReceive);


//====================================== InvReceive part end =================

        List<InvTransactionDetails> invTransactionDetailsList = getInvTransactionDetails(invReceiveDto.getInvTransactionDetails(), invTransaction);
        invTransactionService.saveAllTransactionDetails(invTransactionDetailsList);
        return invReceive;
    }

    @Transactional
    @Override
    public InvReceive update(Long id, Object object) {
        InvReceiveDto invReceiveDto = (InvReceiveDto) object;
        InvTransaction invTransaction = invTransactionService.findById(invReceiveDto.getInvTransactionId());
        if (invTransaction == null) {
            return null;
        }

//====================================== InvTransaction part start =================
        InvTransactionDto invTransactionDto = (InvTransactionDto) invReceiveDto.getInvTransactionDto();
        invTransaction.setTransactionDate(LocalDate.parse(invTransactionDto.getTransactionDate()));

        if (!this.validate(invTransaction)) {
            return null;
        }
        invTransaction = invTransactionService.save(invTransaction);

//====================================== InvTransaction part end =================
//====================================== InvReceive part start =================

        Optional<InvReceive> optionalInvReceive = invReceiveRepository.findById(invReceiveDto.getId());
        if (!optionalInvReceive.isPresent()) {
            return null;
        }
        InvReceive invReceive = optionalInvReceive.get();
        invReceive.setReceiveNo(
                documentSequenceService.getSequenceByDocumentId(
                        CommonConstant.DOCUMENT_ID_FOR_PRODUCTION_RECEIVE));

        //invReceive.setReceiveDate(LocalDate.parse(invReceiveDto.getReceiveDate())); //yyyy-MM-dd
        invReceive.setRemarks(invReceiveDto.getRemarks());
        invReceive.setOrganization(organizationService.getOrganizationFromLoginUser());
        if (invReceiveDto.getCompanyId() != null) {
            invReceive.setCompany(organizationService.findById(invReceiveDto.getCompanyId()));
        }
        if (invReceiveDto.getDepotId() != null) {
            invReceive.setDepot(depotService.findById(invReceiveDto.getDepotId()));
        }
        if (!this.validate(invReceive)) {
            return null;
        }
        invReceive = invReceiveRepository.save(invReceive);

//====================================== InvReceive part end =================

        List<InvTransactionDetails> invTransactionDetailsList = getInvTransactionDetails(invReceiveDto.getInvTransactionDetails(), invTransaction);
        invTransactionService.saveAllTransactionDetails(invTransactionDetailsList);
        return invReceive;
    }

    @Transactional
    @Override
    public boolean delete(Long id) {
//        ============================================ InvReceive start=======================================
        Optional<InvReceive> optionalInvReceive = invReceiveRepository.findById(id);
        try {
            if (!optionalInvReceive.isPresent()) {
                throw new Exception("Inventory Receive not exist");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        InvReceive invReceive = optionalInvReceive.get();
        invReceive.setIsDeleted(true);
        invReceiveRepository.save(invReceive);
//        ============================================ InvReceive end =======================================
//        ============================================ InvTransaction start =======================================
        InvTransaction invTransaction = invTransactionService.findById(invReceive.getInvTransaction().getId());
        try {
            if (invTransaction == null) {
                throw new Exception("Inventory Transaction not exist");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        invTransaction.setIsDeleted(true);
        invTransactionService.save(invTransaction);
//        ============================================ InvTransaction end =======================================
        List<InvTransactionDetails> invTransactionDetailsList = invTransactionService.findAllInvTransactionDetails(id);
        invTransactionDetailsList.forEach(invTransactionDetails -> invTransactionDetails.setIsDeleted(true));
        invTransactionService.saveAllTransactionDetails(invTransactionDetailsList);

        return true;
    }

    @Override
    public InvReceive findById(Long id) {
        try {
            Optional<InvReceive> optionalInvReceive = invReceiveRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalInvReceive.isPresent()) {
                throw new Exception("Invoice Receive Not exist with id " + id);
            }
            return optionalInvReceive.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<InvReceive> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return invReceiveRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

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
                invTransactionDetails.setToStore(storeService.findById(itdd.getToStoreId()));
            }
            if (itdd.getProductId() != null) {
                invTransactionDetails.setProduct(productService.findById(itdd.getProductId()));
            }
            invTransactionDetailsList.add(invTransactionDetails);
        }
        return invTransactionDetailsList;
    }

    public List<Map<String, Object>> getProductionReceiveStoreList(Long companyId) {
        Organization organization = organizationService.findById(companyId).getParent();
        Long organizationId = organization == null ? companyId : organization.getId();
        return storeService.getStoreList(organizationId,
                Arrays.asList(String.valueOf(StoreType.REGULAR), String.valueOf(StoreType.QUARANTINE)));
    }

    public List<Map<String, Object>> getInvProductionReceiveDetailsDataList(
            Long companyId, Long accountingYearId) {

        Map<String, LocalDate> accountingYear = new HashMap<>();
        if (accountingYearId == null) {
            Long accId = accountingYearService.getCurrentAccountingYearId(
                    companyId, LocalDate.now());

            if (accId != null)
                accountingYear = accountingYearService.getAccountingYearDate(accId);
            else
                return new ArrayList<>();

        } else
            accountingYear = accountingYearService.getAccountingYearDate(
                    accountingYearId);

        Map depotMap =
                depotService.getDepotByLoginUserId(companyId,
                        applicationUserService.getApplicationUserFromLoginUser().getId());

        if (permissionService.hasAccess("MANUFACTURING_COST")) {
            return invReceiveRepository.getInvProductionReceiveDetailsDataListToMfCost(
                    companyId, accountingYear.get("startDate"), accountingYear.get("endDate"));
        }
        else {
            return invReceiveRepository.getInvProductionReceiveDetailsDataList(
                    companyId, Long.parseLong(String.valueOf(!depotMap.isEmpty() ? depotMap.get("id") : -1))
                    , accountingYear.get("startDate"), accountingYear.get("endDate"));
        }

    }

    public Map<String, Object> getTransferTransactionListToReceive(
            Long companyId, Long accountingYearId, Long depotId) {

        if (companyId == null) {
            return null;
        }
        Map<String, Object> returnMap = new HashMap<>();
        List<Map> resultList = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.CEILING);
        LocalDate startDate = null;
        LocalDate endDate = null;
        Long userLoginId = applicationUserService.getApplicationUserFromLoginUser().getId();

        if (accountingYearId != null) {
            Map<String, LocalDate> dateMap =
                    accountingYearService.getAccountingYearDate(accountingYearId);
            if (dateMap != null) {
                startDate = dateMap.get("startDate");
                endDate = dateMap.get("endDate");
            }
        }

        Boolean isDepotManager =
                applicationUserService.checkLoginUserIsDepotManager(
                        companyId, userLoginId);
        Map depotMap =
                depotService.getDepotByLoginUserId(companyId, userLoginId);

        if (depotMap.size() > 0) {
            Long userDepotId = Long.parseLong(depotMap.get("id").toString());
            List<String> transactionTypeList =
                    Stream.of(InvTransactionType.TRANSFER_SENT.toString()).collect(Collectors.toList());
            List<String> storeTypeList =
                    Stream.of(StoreType.IN_TRANSIT.toString()).collect(Collectors.toList());
            List<Map<String, Object>> listMap =
                    invReceiveRepository.findTransferTransactionListToReceive(
                            companyId, startDate, endDate, depotId, userDepotId,
                            transactionTypeList, storeTypeList);

                /*List<Map<String, Object>> distinctElements = listMap.stream()
                        .filter( distinctByKey(p -> p.get("id") ) )
                        .collect( Collectors.toList() );*/

            resultList = listMap.stream()
                    .map(elt -> makeProductMapToTransferReceive(elt))
                    .collect(Collectors.toList());

            returnMap.put("totalQuantity",
                    listMap.stream().filter(o -> o.containsKey("quantity"))
                            .mapToDouble(o -> Double.parseDouble(o.get("quantity").toString())).sum());
        }

        returnMap.put("transferTransactionListToReceive", resultList);

        return returnMap;
    }

    private Map makeProductMapToTransferReceive(Map transactionMap) {

        Map map = new HashMap<>();
        List<Map> productList = new ArrayList<>();
        List<Map> productListWithBatch = new ArrayList<>();
        Long invTransactionId = Long.parseLong(String.valueOf(transactionMap.get("id")));
        String receivedStatus = invTransactionService.getTransferReceivedStatus(invTransactionId);
        String claimedStatus = "";

        if ("Pending".equals(receivedStatus)) {
            productList =
                    invReceiveRepository.findProductListByTransactionId(invTransactionId);
            productListWithBatch = productList.stream()
                    .map(elt -> makeTransferReceiveBatchQtyMap(elt, invTransactionId))
                    .collect(Collectors.toList());
        } else if (transactionMap.get("transaction_receive_id") != null) {
            Long transactionReceiveId =
                    Long.parseLong(String.valueOf(transactionMap.get("transaction_receive_id")));
            productList = invReceiveRepository.findProductListToClaim(transactionReceiveId);
            productListWithBatch = productList.stream()
                    .map(elt -> makeTransferReceiveBatchQtyMap(elt, transactionReceiveId))
                    .collect(Collectors.toList());
            Optional<InvClaim> invClaim = invClaimService.findClaimByInvTransaction(transactionReceiveId);
            if (invClaim.isPresent()) {
                claimedStatus = "Claimed";
            }
        }

        map.put("receiveStore", storeService.getStore(String.valueOf(StoreType.QUARANTINE)));
        map.put("receivedStatus", receivedStatus);
        map.put("claimedStatus", claimedStatus);
        map.put("productList", productListWithBatch);
        map.putAll(transactionMap);

        return map;
    }

    public Map makeTransferReceiveBatchQtyMap(Map product, Long invTransactionId) {

        Map map = new HashMap<>();
        Long productId = Long.parseLong(String.valueOf(product.get("id")));

        List<Map> batchList = invReceiveRepository.getTransferReceiveBatchQtyMap(
                productId, invTransactionId);

        map.putAll(product);
        map.put("batchList", batchList);

        return map;
    }

    public void setProductionManufacturingCost(
            ArrayList<Map> objectList) {
        for (Map itdd : objectList) {
            if (itdd.get("id") != null) {
                InvTransactionDetails invTransactionDetails =
                        invTransactionService.findInvTransactionDetailsById(Long.parseLong(itdd.get("id").toString()));
                invTransactionDetails.setRate(Float.parseFloat(itdd.get("manFacCost").toString()));
                invTransactionService.setProductionManufacturingCost(invTransactionDetails);
            }
        }
    }

}
