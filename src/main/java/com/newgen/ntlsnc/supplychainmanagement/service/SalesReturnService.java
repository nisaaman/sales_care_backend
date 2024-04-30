package com.newgen.ntlsnc.supplychainmanagement.service;

import com.newgen.ntlsnc.common.AmountUtil;
import com.newgen.ntlsnc.common.CommonConstant;
import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.CalculationType;
import com.newgen.ntlsnc.globalsettings.entity.Designation;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.service.*;
import com.newgen.ntlsnc.notification.pushNotification.dto.PushNotificationRequest;
import com.newgen.ntlsnc.notification.pushNotification.service.PushNotificationService;
import com.newgen.ntlsnc.reports.service.CommonReportsService;
import com.newgen.ntlsnc.salesandcollection.entity.PaymentCollection;
import com.newgen.ntlsnc.salesandcollection.entity.SalesInvoice;
import com.newgen.ntlsnc.salesandcollection.entity.SalesReturnProposal;
import com.newgen.ntlsnc.salesandcollection.entity.SalesReturnProposalDetails;
import com.newgen.ntlsnc.salesandcollection.service.CreditDebitNoteService;
import com.newgen.ntlsnc.salesandcollection.service.DistributorService;
import com.newgen.ntlsnc.salesandcollection.service.SalesInvoiceService;
import com.newgen.ntlsnc.salesandcollection.service.SalesReturnProposalService;
import com.newgen.ntlsnc.supplychainmanagement.dto.SalesReturnDetailsDto;
import com.newgen.ntlsnc.supplychainmanagement.dto.SalesReturnDto;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvTransaction;
import com.newgen.ntlsnc.supplychainmanagement.entity.SalesReturn;
import com.newgen.ntlsnc.supplychainmanagement.repository.SalesReturnRepository;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import net.sf.jasperreports.engine.JasperPrint;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author marziah
 * @Date 17/04/22
 */

@Service
public class SalesReturnService implements IService<SalesReturn> {
    @Autowired
    InvTransactionService invTransactionService;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    SalesReturnRepository salesReturnRepository;
    @Autowired
    SalesReturnProposalService salesReturnProposalService;
    @Autowired
    ProductService productService;
    @Autowired
    DepotService depotService;
    @Autowired
    DistributorService distributorService;
    @Autowired
    BatchService batchService;
    @Autowired
    StoreService storeService;
    @Autowired
    DocumentSequenceService documentSequenceService;
    @Autowired
    SalesInvoiceService salesInvoiceService;
    @Autowired
    ReportService reportService;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    CreditDebitNoteService creditDebitNoteService;
    @Autowired
    CommonReportsService commonReportsService;
    @Autowired
    PushNotificationService pushNotificationService;

    @Autowired
    InvDeliveryChallanService invDeliveryChallanService;

    @Transactional
    @Override
    public SalesReturn create(Object object) {
        try {
            SalesReturnDto salesReturnDto = (SalesReturnDto) object;

            //SalesReturnProposal update
            SalesReturnProposal salesReturnProposal = salesReturnProposalService.findById(salesReturnDto.getSalesReturnProposalId());
            salesReturnProposal.setIsReturn(true);  // when return is created then ture
            salesReturnProposal = salesReturnProposalService.save(salesReturnProposal);

            // invTransaction create
            InvTransaction invTransaction = invTransactionService.createForSalesReturn(salesReturnProposal);

            //SalesReturnProposalDetails list create
            List<SalesReturnProposalDetails> salesReturnProposalDetailsList = salesReturnProposalService.getSalesReturnProposalDetailsListBySalesReturnProposalId(salesReturnProposal.getId());
            invTransactionService.createDetailsForSalesReturn(salesReturnProposalDetailsList, invTransaction);

            //salesReturn create
            SalesReturn salesReturn = new SalesReturn();
            salesReturn.setReturnNo(documentSequenceService.getSequenceByDocumentId(CommonConstant.DOCUMENT_ID_FOR_SALES_RETURN));
            salesReturn.setReturnDate(LocalDateTime.now());
            salesReturn.setReturnNote(salesReturnDto.getReturnNote());
            salesReturn.setDepot(salesReturnProposal.getDeliveryChallan().getDepot());
            salesReturn.setInvTransaction(invTransaction);
            salesReturn.setSalesReturnProposal(salesReturnProposal);
            salesReturn.setCompany(salesReturnProposal.getCompany());
            salesReturn.setOrganization(salesReturnProposal.getOrganization());
            salesReturn = salesReturnRepository.save(salesReturn);

            //---------------start Invoice remaining amount adjust
            SalesInvoice salesInvoice = salesReturnProposal.getSalesInvoice();
//            Float returnAmount = salesReturnProposalService.getSalesReturnProposalAmountById(salesReturnProposal.getId());
            Float returnAmount = salesReturnDto.getSalesReturnProposalTotalAmount();
            Float invoiceRemainingAmount = salesInvoice.getRemainingAmount();

            //------------start Create Debit note for return ord amount start---------------------------------
            if (salesInvoice.getOrdAmount() > 0) {
                Float returnOrd = (salesInvoice.getOrdAmount() * returnAmount) / salesInvoice.getInvoiceAmount();
                returnOrd = AmountUtil.round(returnOrd, 4);
                creditDebitNoteService.createForOrdFromReturn(salesReturn, salesInvoice, returnOrd);
                salesInvoice.setOrdAmount(AmountUtil.round(salesInvoice.getOrdAmount() - returnOrd, 4));
            }
            //------------end Create Debit note for return ord amount start---------------------------------
            // when special discount is exist then debit note will be createde
            if (!salesInvoice.getIsInvoiceDiscountReturned()) {
                /*no credit or debit note for sales return discount*/
                //creditDebitNoteService.createForSpecialDiscountOfInvoiceFromReturn(returnAmount, salesReturn, salesInvoice);
                salesInvoice.setIsInvoiceDiscountReturned(true);
            }
            if (invoiceRemainingAmount >= returnAmount) {
                salesInvoiceService.deductInvoiceRemainingAmount(salesInvoice, returnAmount);
            } else {
                // as per 05/12/2022 date discussion, remaining amount will be adjusted
                salesInvoiceService.deductInvoiceRemainingAmount(salesInvoice, salesInvoice.getRemainingAmount());
            }
            //---------------end Invoice remaining amount adjust

            return salesReturn;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void sendPushNotificationWhenSalesRetunConvertToSo(Object data) {
        SalesReturn salesReturn = (SalesReturn) data;
        SalesReturnProposal salesReturnProposal = salesReturnProposalService.findById(salesReturn.getSalesReturnProposal().getId());
        Long salesOfficerId = Long.valueOf(salesReturnProposal.getSalesOfficer().getId());
        PushNotificationRequest pushNotificationRequest = new PushNotificationRequest();
        String userFCMId = applicationUserService.getUserFCMId(salesOfficerId);
        pushNotificationRequest.setTitle("Sales Return Verify");
        pushNotificationRequest.setToken(userFCMId);
        pushNotificationRequest.setMessage(salesReturn.getReturnNo().toString()+" of this "+
                salesReturnProposal.getProposalNo()+ " is verified.");
        Resource resource = new ClassPathResource("/images/notification-icon.png");
        try {
            URL url = resource.getURI().toURL();
            if (url!=null)
                pushNotificationRequest.setImage(String.valueOf(ImageIO.read(url)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        pushNotificationService.sendPushNotificationToToken(pushNotificationRequest);
    }

    @Override
    public SalesReturn update(Long id, Object object) {
        return null;
    }

    @Override
    public boolean delete(Long id) {
        return true;
    }

    @Override
    public SalesReturn findById(Long id) {
        try {
            Optional<SalesReturn> optionalSalesReturn = salesReturnRepository.findById(id);
            if (!optionalSalesReturn.isPresent()) {
                throw new Exception("Sales Return Not exist");
            }
            return optionalSalesReturn.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<SalesReturn> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return salesReturnRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }

    public List<Map> getDistributorWiseSalesReturnListByCompanyAndSalesPerson(Long companyId, Long salesOfficerId) {
        return salesReturnRepository.getDistributorWiseSalesReturnListByCompanyAndSalesPerson(companyId, salesOfficerId);
    }

    public JasperPrint getSalesReturnReport(Map<String, Object> params) throws IOException, ParseException {
        Organization company = organizationService.findById(Long.parseLong(params.get("companyId").toString()));
        params.put("companyName", company.getName() + " (" + company.getShortName() + ")");
        params.put("companyAddress", company.getAddress());
        params.put("companyEmail", company.getEmail());
        params.put("companyWeb", company.getWebAddress());
        params.put("companyLogo", new ByteArrayInputStream(organizationService.getOrganizationLogoByteData(company.getId())));

        Map<String, Object> loginUser = applicationUserService.getMe();
        params.put("printedBy", loginUser.get("userName"));
        Object o = loginUser.get("designation");
        params.put("printedByDesignation", ((Designation) o).getName());
        params.put("productOwner", commonReportsService.getProductOwner());

        LocalDate localStartDate = null;
        LocalDate localEndDate = null;
        if (params.get("startDate").toString().equals("")|| params.get("startDate").toString().equals("Invalid date")) {
            params.put("startDate", null);
        } else {
            localStartDate = LocalDate.parse(params.get("startDate").toString(), DateTimeFormatter.ofPattern("yyyy-MM-d"));
            params.put("startDate", params.get("startDate").toString());
        }
        if (params.get("endDate").toString().equals("")|| params.get("endDate").toString().equals("Invalid date")) {
            params.put("endDate", null);
        } else {
            localEndDate = LocalDate.parse(params.get("endDate").toString(), DateTimeFormatter.ofPattern("yyyy-MM-d"));
            params.put("endDate", params.get("endDate").toString());
        }
        params.put("dateHeader", commonReportsService.getReportDate(localStartDate, localEndDate, params.get("dateType").toString()));

        params.put("locationIds", Arrays.asList(params.get("locationIds").toString().split(",")));
        params.put("distributorIds", Arrays.asList(params.get("disIds").toString().split(",")));
        params.put("salesOfficerIds", Arrays.asList(params.get("soIds").toString().split(",")));
        params.put("isNational", params.get("nationalLocationChecked").toString().equals("true") ? Boolean.TRUE : Boolean.FALSE);

        //product and category
        boolean isProductExist = params.get("prodtIds").toString().equals("") ? false : true;
        boolean isCategoryExist = params.get("productCategoryIds").toString().equals("") ? false : true;
        params.put("isProductExist", isProductExist);
        params.put("isCategoryExist", isCategoryExist);
        params.put("productIds", Arrays.asList(params.get("prodtIds").toString().split(",")));
        params.put("categoryIds", Arrays.asList(params.get("productCategoryIds").toString().split(",")));

        // jasper file selection
        String jasperFileName = "";
        if (params.get("reportType").toString().equals("SUMMARY")) {
            if (!"".equals(params.get("disIds").toString())) { //distributor wise
                if (isProductExist) {
                    params.put("reportType", "(Distributor And Product)");
                    jasperFileName = "DistributorAndProductWiseSalesReturnSummary"; // product wise
                } else if (isCategoryExist) {
                    if (params.get("categoryTypeLevel").toString().equals("2")){
                        params.put("reportType", "(Distributor And Product Sub-Category)");
                        jasperFileName = "DistributorAndProductSubCategoryWiseSalesReturnSummary"; // sub-category wise
                    }else {
                        params.put("reportType", "(Distributor And Product Category)");
                        jasperFileName = "DistributorAndProductCategoryWiseSalesReturnSummary"; // category wise
                    }
                } else {
                    params.put("reportType", "(Distributor)");
                    jasperFileName = "SalesReturnSummaryByDistributor"; // Distributor wise
                }
            } else if (!"".equals(params.get("soIds").toString())) {
                if (isProductExist) {
                    params.put("reportType", "(Sales Officer And Product)");
                    jasperFileName = "SalesOfficerAndProductWiseSalesReturnSummary"; // product wise
                } else if (isCategoryExist) {
                    if (params.get("categoryTypeLevel").toString().equals("2")){
                        params.put("reportType", "(Sales Officer And Product Sub-Category)");
                        jasperFileName = "SalesOfficerAndProductSubCategoryWiseSalesReturnSummary"; // sub-category wise
                    } else {
                        params.put("reportType", "(Sales Officer And Product Category)");
                        jasperFileName = "SalesOfficerAndProductCategoryWiseSalesReturnSummary"; // category wise
                    }
                } else {
                    params.put("reportType", "(Sales Officer)");
                    jasperFileName = "SalesReturnSummaryBySalesOfficer"; // Sales Officer wise
                }
            } else if (params.get("nationalLocationChecked").toString().equals("true")) { // national
                if (!"".equals(params.get("locationTypeData").toString())){
                    if (params.get("locationTypeData").toString().equals("Zone")){
                        if (isProductExist) {//zone wise
                            params.put("reportType", "(All Zone And Product)");
                            jasperFileName = "ZoneAndProductWiseSalesReturnSummary"; // product wise
                        } else if (isCategoryExist) {
                            if (params.get("categoryTypeLevel").toString().equals("2")){
                                params.put("reportType", "(All Zone And Sub-Category)");
                                jasperFileName = "ZoneAndSubCategoryWiseSalesReturnSummary"; // sub-category wise
                            } else {
                                params.put("reportType", "(All Zone And Category)");
                                jasperFileName = "ZoneAndCategoryWiseSalesReturnSummary"; // category wise
                            }
                        } else {
                            params.put("reportType", "(All Zone)");
                            jasperFileName = "SalesReturnSummaryByZone";//Location wise
                        }
                    } else if (params.get("locationTypeData").toString().equals("Area")) {
                        if (isProductExist) {
                            params.put("reportType", "(All Area And Product)");
                            jasperFileName = "AreaAndProductWiseSalesReturnSummary"; // product wise
                        } else if (isCategoryExist) {
                            if (params.get("categoryTypeLevel").toString().equals("2")){
                                params.put("reportType", "(All Area And Sub-Category)");
                                jasperFileName = "AreaAndSubCategoryWiseSalesReturnSummary"; // sub-category wise
                            } else {
                                params.put("reportType", "(All Area And Category)");
                                jasperFileName = "AreaAndCategoryWiseSalesReturnSummary"; // category wise
                            }
                        } else {
                            params.put("reportType", "(All Area)");
                            jasperFileName = "SalesReturnSummaryByArea"; // Area wise
                        }
                    } else if (params.get("locationTypeData").toString().equals("Territory")) {
                        if (isProductExist) {
                            params.put("reportType", "(All Territory And Product)");
                            jasperFileName = "TerritoryAndProductWiseSalesReturnSummary"; // product wise
                        } else if (isCategoryExist) {
                            if (params.get("categoryTypeLevel").toString().equals("2")){
                                params.put("reportType", "(All Territory And Sub-Category)");
                                jasperFileName = "TerritoryAndProductSubCategoryWiseSalesReturnSummary"; // sub-category wise
                            } else {
                                params.put("reportType", "(All Territory And Category)");
                                jasperFileName = "TerritoryAndCategoryWiseSalesReturnSummary"; // category wise
                            }
                        } else {
                            params.put("reportType", "(All Territory)");
                            jasperFileName = "SalesReturnSummaryByTerritory"; // Territory wise
                        }
                    }
                } else if (params.get("allChecked").toString().equals("true")){
                    if (isProductExist) {//zone wise
                        params.put("reportType", "(All Zone And Product)");
                        jasperFileName = "ZoneAndProductWiseSalesReturnSummary"; // product wise
                    }  else if (isCategoryExist) {
                        if (params.get("categoryTypeLevel").toString().equals("2")){
                            params.put("reportType", "(All Zone And Sub-Category)");
                            jasperFileName = "ZoneAndSubCategoryWiseSalesReturnSummary"; // sub-category wise
                        } else {
                            params.put("reportType", "(All Zone And Category)");
                            jasperFileName = "ZoneAndCategoryWiseSalesReturnSummary"; // category wise
                        }
                    } else {
                        params.put("reportType", "(All Zone)");
                        jasperFileName = "SalesReturnSummaryByZone";//Location wise
                    }
                }else {
                    params.put("reportType", "(National)");
                    jasperFileName = "SalesReturnSummaryByNational"; // national
                }

            } else if (params.get("locationTypeLevel").toString().equals("1")) { //zone wise
                if (params.get("allChecked").toString().equals("true")){
                    if (isProductExist) {
                        params.put("reportType", "(All Area And Product)");
                        jasperFileName = "AreaAndProductWiseSalesReturnSummary"; // product wise
                    }  else if (isCategoryExist) {
                        if (params.get("categoryTypeLevel").toString().equals("2")){
                            params.put("reportType", "(All Area And Sub-Category)");
                            jasperFileName = "AreaAndSubCategoryWiseSalesReturnSummary"; // sub-category wise
                        } else {
                            params.put("reportType", "(All Area And Category)");
                            jasperFileName = "AreaAndCategoryWiseSalesReturnSummary"; // category wise
                        }
                    } else {
                        params.put("reportType", "(All Area)");
                        jasperFileName = "SalesReturnSummaryByArea"; // Area wise
                    }
                }else {
                    if (isProductExist) {
                        params.put("reportType", "(Zone And Product)");
                        jasperFileName = "ZoneAndProductWiseSalesReturnSummary"; // product wise
                    }  else if (isCategoryExist) {
                        if (params.get("categoryTypeLevel").toString().equals("2")){
                            params.put("reportType", "(Zone And Sub-Category)");
                            jasperFileName = "ZoneAndSubCategoryWiseSalesReturnSummary"; // sub-category wise
                        } else {
                            params.put("reportType", "(Zone And Category)");
                            jasperFileName = "ZoneAndCategoryWiseSalesReturnSummary"; // category wise
                        }
                    } else {
                        params.put("reportType", "(Zone)");
                        jasperFileName = "SalesReturnSummaryByZone";//Location wise
                    }
                }

            } else if (params.get("locationTypeLevel").toString().equals("2")) {
                if (params.get("allChecked").toString().equals("true")){
                    if (isProductExist) {
                        params.put("reportType", "(All Territory And Product)");
                        jasperFileName = "TerritoryAndProductWiseSalesReturnSummary"; // product wise
                    }else if (isCategoryExist) {
                        if (params.get("categoryTypeLevel").toString().equals("2")){
                            params.put("reportType", "(All Territory And Sub-Category)");
                            jasperFileName = "TerritoryAndProductSubCategoryWiseSalesReturnSummary"; // sub-category wise
                        } else {
                            params.put("reportType", "(All Territory And Category)");
                            jasperFileName = "TerritoryAndCategoryWiseSalesReturnSummary"; // category wise
                        }
                    } else {
                        params.put("reportType", "(All Territory)");
                        jasperFileName = "SalesReturnSummaryByTerritory"; // Territory wise
                    }
                }else {
                    if (isProductExist) {
                        params.put("reportType", "(Area And Product)");
                        jasperFileName = "AreaAndProductWiseSalesReturnSummary"; // product wise
                    } else if (isCategoryExist) {
                        if (params.get("categoryTypeLevel").toString().equals("2")){
                            params.put("reportType", "(Area And Sub-Category)");
                            jasperFileName = "AreaAndSubCategoryWiseSalesReturnSummary"; // sub-category wise
                        } else {
                            params.put("reportType", "(Area And Category)");
                            jasperFileName = "AreaAndCategoryWiseSalesReturnSummary"; // category wise
                        }
                    } else {
                        params.put("reportType", "(Area)");
                        jasperFileName = "SalesReturnSummaryByArea"; // Area wise
                    }
                }

            } else if (params.get("locationTypeLevel").toString().equals("3")) {
                if (params.get("allChecked").toString().equals("true")){
                    params.put("isSoExist", params.get("soIds").toString().equals("") ? false : true);
                    if (isProductExist) {
                        params.put("reportType", "(All Sales Officer And Product)");
                        jasperFileName = "SalesOfficerAndProductWiseSalesReturnSummary"; // product wise
                    }  else if (isCategoryExist) {
                        if (params.get("categoryTypeLevel").toString().equals("2")){
                            params.put("reportType", "(All Sales Officer And Product Sub-Category)");
                            jasperFileName = "SalesOfficerAndProductSubCategoryWiseSalesReturnSummary"; // sub-category wise
                        } else {
                            params.put("reportType", "(All Sales Officer And Product Category)");
                            jasperFileName = "SalesOfficerAndProductCategoryWiseSalesReturnSummary"; // category wise
                        }
                    } else {
                        params.put("reportType", "(All Sales Officer)");
                        jasperFileName = "SalesReturnSummaryBySalesOfficer"; // Sales Officer wise
                    }
                }else {
                    if (isProductExist) {
                        params.put("reportType", "(Territory And Product)");
                        jasperFileName = "TerritoryAndProductWiseSalesReturnSummary"; // product wise
                    } else if (isCategoryExist) {
                        if (params.get("categoryTypeLevel").toString().equals("2")){
                            params.put("reportType", "(Territory And Sub-Category)");
                            jasperFileName = "TerritoryAndProductSubCategoryWiseSalesReturnSummary"; // sub-category wise
                        } else {
                            params.put("reportType", "(Territory And Category)");
                            jasperFileName = "TerritoryAndCategoryWiseSalesReturnSummary"; // category wise
                        }
                    } else {
                        params.put("reportType", "(Territory)");
                        jasperFileName = "SalesReturnSummaryByTerritory"; // Territory wise
                    }
                }
            }
        } else {
            params.put("isDistributorExist", params.get("disIds").toString().equals("") ? false : true);
            params.put("isSoExist", params.get("soIds").toString().equals("") ? false : true);
            if (params.get("isWithSum").toString().equals("true")) {
                if (params.get("reportFormat").equals("PDF")){
                    jasperFileName = "SalesReturnDetails"; // details with sum report
                } else {
                    jasperFileName = "SalesReturnDetailsExcel"; // details without sum report excel
                }
            } else {
                if (params.get("reportFormat").equals("PDF")){
                    jasperFileName = "SalesReturnDetailsWithOutSub"; // details without sum report
                } else {
                    jasperFileName = "SalesReturnDetailsWithOutSubExcel"; // details without sum report excel
                }
            }
        }
        //should remove those file
        // jasperFileName = "SalesReturnSummaryBySalesOfficer";
        // jasperFileName = "SalesReturnSummaryByTerritory"; // territory wise
//        jasperFileName = "SalesReturnSummaryByArea"; // area wise

        return reportService.getReport(jasperFileName, "/reports/salesReturn/", params);
    }

    public Map getSalesQuantityAndReturnQuantity(Long salesInvoiceId) {
        return salesReturnRepository.getSalesQuantityAndReturnQuantity(salesInvoiceId);
    }

    public float getSalesReturnAmount(Long companyId, Long distributorId, LocalDateTime invoiceDate) {
        Float returnAmount = 0.0f;
        if (salesReturnRepository.getSalesReturnAmount(companyId, distributorId, invoiceDate) != null) {
            returnAmount = salesReturnRepository.getSalesReturnAmount(companyId, distributorId, invoiceDate);
        }

        return returnAmount;
    }

    @Transactional
    public SalesReturn createWithProposal(Object object) {
        try {
            SalesReturnDto salesReturnDto = (SalesReturnDto) object;

            List<SalesReturnDetailsDto> salesReturnDetailsDtoList = salesReturnDto.getSalesReturnDetailsDtoList();

            SalesReturnProposal newSalesReturnProposal = null;
            List<SalesReturnProposal> salesReturnProposalList = new ArrayList<>();
            List<SalesReturnProposalDetails> salesReturnProposalDetailsList = new ArrayList<>();

            SalesReturnProposal proposedSalesReturnProposal = salesReturnProposalService.findById(salesReturnDto.getSalesReturnProposalId());
            for (SalesReturnDetailsDto salesReturnDetailsDto : salesReturnDetailsDtoList) {
                SalesReturnProposalDetails salesReturnProposalDetails = salesReturnProposalService.findBySalesReturnProposalDetailsId(salesReturnDetailsDto.getSalesReturnProposalDetailsId());
                if (salesReturnProposalDetails.getSalesReturnProposal().getDeliveryChallan().getId().equals(salesReturnDetailsDto.getDeliveryChallanId())) {
                    if (newSalesReturnProposal == null) {
                        newSalesReturnProposal = getSalesReturnProposal(salesReturnDetailsDto, salesReturnProposalDetails, proposedSalesReturnProposal);
                        salesReturnProposalList.add(newSalesReturnProposal);
                    }
                    if (newSalesReturnProposal != null) {
                        SalesReturnProposalDetails newSalesReturnProposalDetails = getSalesReturnProposalDetails(salesReturnDetailsDto, salesReturnProposalDetails, newSalesReturnProposal);
                        salesReturnProposalDetailsList.add(newSalesReturnProposalDetails);
                        salesReturnDetailsDto.setSetDone(true);
                    }
                }

                if (!salesReturnDetailsDto.isSetDone()) {
                    SalesReturnProposal salesReturnProposal = getSalesReturnProposal(salesReturnDetailsDto, salesReturnProposalDetails, proposedSalesReturnProposal);
                    salesReturnProposalList.add(salesReturnProposal);

                    SalesReturnProposalDetails newSalesReturnProposalDetails =  getSalesReturnProposalDetails(salesReturnDetailsDto, salesReturnProposalDetails, salesReturnProposal);
                    salesReturnProposalDetailsList.add(newSalesReturnProposalDetails);
                    salesReturnDetailsDto.setSetDone(true);

                }
            }
            proposedSalesReturnProposal.setIsProposalConvert(true);
            proposedSalesReturnProposal.setIsReturn(true);
            salesReturnProposalService.save(proposedSalesReturnProposal);
            salesReturnProposalService.saveAll(salesReturnProposalList);
            salesReturnProposalService.saveAllForDetails(salesReturnProposalDetailsList);

            List<SalesReturn> salesReturnList = new ArrayList<>();

            for (SalesReturnProposal salesReturnProposal : salesReturnProposalList) {

            //SalesReturnProposal update
            salesReturnProposal.setIsReturn(true);  // when return is created then ture
            salesReturnProposal = salesReturnProposalService.save(salesReturnProposal);

            // invTransaction create
            InvTransaction invTransaction = invTransactionService.createForSalesReturn(salesReturnProposal);

            //SalesReturnProposalDetails list create
            salesReturnProposalDetailsList = salesReturnProposalService.getSalesReturnProposalDetailsListBySalesReturnProposalId(salesReturnProposal.getId());

                invTransactionService.createDetailsForSalesReturn(salesReturnProposalDetailsList, invTransaction);

            //salesReturn create
            SalesReturn salesReturn = new SalesReturn();
            salesReturn.setReturnNo(documentSequenceService.getSequenceByDocumentId(CommonConstant.DOCUMENT_ID_FOR_SALES_RETURN));
            salesReturn.setReturnDate(LocalDateTime.now());
            salesReturn.setReturnNote(salesReturnDto.getReturnNote());
            salesReturn.setDepot(salesReturnProposal.getDeliveryChallan().getDepot());
            salesReturn.setInvTransaction(invTransaction);
            salesReturn.setSalesReturnProposal(salesReturnProposal);
            salesReturn.setCompany(salesReturnProposal.getCompany());
            salesReturn.setOrganization(salesReturnProposal.getOrganization());
            salesReturn = salesReturnRepository.save(salesReturn);

            //---------------start Invoice remaining amount adjust
            SalesInvoice salesInvoice = salesReturnProposal.getSalesInvoice();
//            Float returnAmount = salesReturnProposalService.getSalesReturnProposalAmountById(salesReturnProposal.getId());
            Float returnAmount =  getSalesReturnProposalTotalAmount(salesReturnProposal, salesReturnProposalDetailsList); //salesReturnDto.getSalesReturnProposalTotalAmount();
            Float invoiceRemainingAmount = salesInvoice.getRemainingAmount();

            //------------start Create Debit note for return ord amount start---------------------------------
            if (salesInvoice.getOrdAmount() > 0) {
                Float returnOrd = (salesInvoice.getOrdAmount() * returnAmount) / salesInvoice.getInvoiceAmount();
                returnOrd = AmountUtil.round(returnOrd, 4);
                creditDebitNoteService.createForOrdFromReturn(salesReturn, salesInvoice, returnOrd);
                salesInvoice.setOrdAmount(AmountUtil.round(salesInvoice.getOrdAmount() - returnOrd, 4));
            }
            //------------end Create Debit note for return ord amount start---------------------------------
            // when special discount is exist then debit note will be createde
            if (!salesInvoice.getIsInvoiceDiscountReturned()) {
                /*no credit or debit note for sales return discount*/
                //creditDebitNoteService.createForSpecialDiscountOfInvoiceFromReturn(returnAmount, salesReturn, salesInvoice);
                salesInvoice.setIsInvoiceDiscountReturned(true);
            }
            if (invoiceRemainingAmount >= returnAmount) {
                salesInvoiceService.deductInvoiceRemainingAmount(salesInvoice, returnAmount);
            } else {
                // as per 05/12/2022 date discussion, remaining amount will be adjusted
                salesInvoiceService.deductInvoiceRemainingAmount(salesInvoice, salesInvoice.getRemainingAmount());
            }
//            //---------------end Invoice remaining amount adjust

                salesReturnList.add(salesReturn);
            }
            return salesReturnList.get(0);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private Float getSalesReturnProposalTotalAmount(SalesReturnProposal salesReturnProposal, List<SalesReturnProposalDetails> salesReturnProposalDetailsList) {
        float totalProductPrice = 0F;
        for(SalesReturnProposalDetails salesReturnProposalDetails : salesReturnProposalDetailsList){
            float productPrice = salesReturnProposalDetails.getQuantity() * salesReturnProposalDetails.getProductTradePrice().getTradePrice();
            if(salesReturnProposalDetails.getTradeDiscount() != null){
                if(salesReturnProposalDetails.getTradeDiscount().getCalculationType().getCode().equals(CalculationType.PERCENTAGE.getCode())){
                    productPrice -= productPrice * salesReturnProposalDetails.getTradeDiscount().getDiscountValue() / 100;
                }
                if(salesReturnProposalDetails.getTradeDiscount().getCalculationType().getCode().equals(CalculationType.EQUAL.getCode())){
                    productPrice -= salesReturnProposalDetails.getTradeDiscount().getDiscountValue() ;
                }
            }
            totalProductPrice += productPrice;
        }
        totalProductPrice -= (salesReturnProposal.getSalesInvoice().getInvoiceDiscount() * totalProductPrice / ( salesReturnProposal.getSalesInvoice().getInvoiceDiscount() + salesReturnProposal.getSalesInvoice().getInvoiceAmount()));
        return totalProductPrice;
    }

    @NotNull
    private SalesReturnProposalDetails getSalesReturnProposalDetails(SalesReturnDetailsDto salesReturnDetailsDto, SalesReturnProposalDetails salesReturnProposalDetails, SalesReturnProposal newSalesReturnProposal) {
        SalesReturnProposalDetails newSalesReturnProposalDetails = new SalesReturnProposalDetails();
        newSalesReturnProposalDetails.setProduct(salesReturnProposalDetails.getProduct());
        newSalesReturnProposalDetails.setOrganization(salesReturnProposalDetails.getOrganization());
        newSalesReturnProposalDetails.setSalesReturnProposal(newSalesReturnProposal);
        newSalesReturnProposalDetails.setProductTradePrice(salesReturnProposalDetails.getProductTradePrice());
        if (salesReturnProposalDetails.getTradeDiscount() != null) {
            newSalesReturnProposalDetails.setTradeDiscount(salesReturnProposalDetails.getTradeDiscount());
        }
        newSalesReturnProposalDetails.setBatch(batchService.findById(salesReturnDetailsDto.getBatchId()));
        newSalesReturnProposalDetails.setRate(salesReturnProposalDetails.getRate());
        newSalesReturnProposalDetails.setQuantity(salesReturnDetailsDto.getQuantity());
        newSalesReturnProposalDetails.setIntactType(salesReturnProposalDetails.getIntactType());
        return newSalesReturnProposalDetails;
    }

    @NotNull
    private SalesReturnProposal getSalesReturnProposal(SalesReturnDetailsDto salesReturnDetailsDto, SalesReturnProposalDetails salesReturnProposalDetails, SalesReturnProposal refSalesReturnProposal) {
        SalesReturnProposal newSalesReturnProposal;
        newSalesReturnProposal = new SalesReturnProposal();
        newSalesReturnProposal.setProposalNo(documentSequenceService.getSequenceByDocumentId(CommonConstant.DOCUMENT_ID_FOR_SALES_RETURN_PROPOSAL));
        newSalesReturnProposal.setDeliveryChallan(invDeliveryChallanService.findById(salesReturnDetailsDto.getDeliveryChallanId()));
        newSalesReturnProposal.setDistributor(salesReturnProposalDetails.getSalesReturnProposal().getDistributor());
        newSalesReturnProposal.setSalesInvoice(salesInvoiceService.findById(salesReturnDetailsDto.getSalesInvoiceId()));
        newSalesReturnProposal.setCompany(salesReturnProposalDetails.getSalesReturnProposal().getCompany());
        newSalesReturnProposal.setSalesOfficer(salesReturnProposalDetails.getSalesReturnProposal().getSalesOfficer());
        newSalesReturnProposal.setOrganization(salesReturnProposalDetails.getSalesReturnProposal().getOrganization());
        newSalesReturnProposal.setInvoiceFromDate(salesReturnProposalDetails.getSalesReturnProposal().getInvoiceFromDate());
        newSalesReturnProposal.setInvoiceToDate(salesReturnProposalDetails.getSalesReturnProposal().getInvoiceToDate());
        newSalesReturnProposal.setProposalDate(salesReturnProposalDetails.getSalesReturnProposal().getProposalDate());    //yyyy-MM-dd
        newSalesReturnProposal.setReturnReason(salesReturnProposalDetails.getSalesReturnProposal().getReturnReason());
        newSalesReturnProposal.setApprovalStatus(salesReturnProposalDetails.getSalesReturnProposal().getApprovalStatus());
        newSalesReturnProposal.setApprovalDate(salesReturnProposalDetails.getSalesReturnProposal().getApprovalDate());
        newSalesReturnProposal.setRefSalesReturnProposal(refSalesReturnProposal);
        return newSalesReturnProposal;
    }


}
