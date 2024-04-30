package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.common.enums.CalculationType;
import com.newgen.ntlsnc.globalsettings.dto.TradeDiscountDto;
import com.newgen.ntlsnc.globalsettings.entity.*;
import com.newgen.ntlsnc.globalsettings.repository.TradeDiscountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Array;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author marziah
 */

@Service
public class TradeDiscountService implements IService<TradeDiscount> {

    @Autowired
    TradeDiscountRepository tradeDiscountRepository;
    @Autowired
    SemesterService semesterService;
    @Autowired
    ProductService productService;
    @Autowired
    InvoiceNatureService invoiceNatureService;
    @Autowired
    OrganizationService organizationService;

    @Override
    @Transactional
    public TradeDiscount create(Object object) {
        try {
            TradeDiscountDto tradeDiscountDto = (TradeDiscountDto) object;
            TradeDiscount tradeDiscount = new TradeDiscount();

            Optional<TradeDiscount> optionalExistedTradeDiscount = tradeDiscountRepository
                    .getByCompanyIdAndProductIdAndSemesterIdAndInvoiceNatureIdAndIsActiveTrueAndIsDeletedFalse(tradeDiscountDto.getCompanyId(),
                            tradeDiscountDto.getProductId(), tradeDiscountDto.getSemesterId(), tradeDiscountDto.getInvoiceNatureId());
            if (optionalExistedTradeDiscount.isPresent()) {
                throw new IllegalArgumentException("Already exist with this Trade Discount");
            }

            tradeDiscount.setCalculationType(CalculationType.valueOf(tradeDiscountDto.getCalculationType()));
            tradeDiscount.setDiscountName(tradeDiscountDto.getDiscountName());
            tradeDiscount.setDiscountValue(tradeDiscountDto.getDiscountValue());
            //@Todo will be pending
            tradeDiscount.setApprovalStatus(ApprovalStatus.APPROVED);

            tradeDiscount.setCompany(organizationService.findById(tradeDiscountDto.getCompanyId()));
            tradeDiscount.setProduct(productService.findById(tradeDiscountDto.getProductId()));
            tradeDiscount.setSemester(semesterService.findById(tradeDiscountDto.getSemesterId()));
            tradeDiscount.setInvoiceNature(invoiceNatureService.findById(tradeDiscountDto.getInvoiceNatureId()));
            tradeDiscount.setOrganization(organizationService.getOrganizationFromLoginUser());

            if (!this.validate(tradeDiscount)) {
                return null;
            }

            return tradeDiscountRepository.save(tradeDiscount);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public TradeDiscount update(Long id, Object object) {

        TradeDiscountDto tradeDiscountDto = (TradeDiscountDto) object;
        TradeDiscount tradeDiscount = tradeDiscountRepository.findById(tradeDiscountDto.getId()).get();
        tradeDiscount.setOrganization(organizationService.getOrganizationFromLoginUser());
        Organization companyId = organizationService.findById(tradeDiscountDto.getCompanyId());
        Semester semester = semesterService.findById(tradeDiscountDto.getSemesterId());
        Product product = productService.findById(tradeDiscountDto.getProductId());
        InvoiceNature invoiceNature = invoiceNatureService.findById(tradeDiscountDto.getInvoiceNatureId());
        tradeDiscount.setCalculationType(CalculationType.valueOf(tradeDiscountDto.getCalculationType()));
        tradeDiscount.setDiscountValue(tradeDiscountDto.getDiscountValue());
        tradeDiscount.setApprovalStatus(tradeDiscountDto.getApprovalStatus());

        tradeDiscount.setCompany(companyId);
        tradeDiscount.setProduct(product);
        tradeDiscount.setSemester(semester);
        tradeDiscount.setInvoiceNature(invoiceNature);

        if (!this.validate(tradeDiscount)) {
            return null;
        }
        return tradeDiscountRepository.save(tradeDiscount);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            Integer isExits = tradeDiscountRepository.existsByIdInSaleBookingDetailsEntity(id);
            if (isExits == 1) {
                throw new Exception("This Product can not be deleted.It's already used");
            } else {
                TradeDiscount tradeDiscount = findById(id);
                tradeDiscount.setIsDeleted(true);
                tradeDiscountRepository.save(tradeDiscount);
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public TradeDiscount findById(Long id) {
        try {
            Optional<TradeDiscount> optionalTradeDiscount = tradeDiscountRepository.findByIdAndIsDeletedFalseAndIsActiveTrue(id);
            if (!optionalTradeDiscount.isPresent()) {
                throw new Exception("Trade Discount Not exist with id " + id);
            }
            return optionalTradeDiscount.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<TradeDiscount> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return tradeDiscountRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }

    public List<Map<String, Object>> getAllTradeDiscountListByProductCategoryWise(
            Long[] categoryIds, Long accYearId) {
        try {
            List<Map<String, Object>> list = new ArrayList();
            List<Semester> semesterList = semesterService.getAllByAccountingYearId(accYearId);
            List<Long> semesterIds = semesterList.stream()
                    .map(Semester::getId).collect(Collectors.toList());

            List<Map<String, Object>> tradeDiscountList =
                    tradeDiscountRepository.findAllTradeDicountListByProductCategory(categoryIds, semesterIds);
            for (Map data : tradeDiscountList) {
                Map mMap = new HashMap();
                mMap.put("productCategory", data.get("productCategoryName"));
                mMap.put("id", data.get("id"));
                mMap.put("tradeDiscountId", data.get("tradeDiscountId"));
                mMap.put("name", data.get("name"));
                mMap.put("productSku", data.get("product_sku"));
                mMap.put("p_name", data.get("p_name"));
                //mMap.put("pack_size", data.get("pack_size"));
                //mMap.put("abbreviation", data.get("abbreviation"));
                mMap.put("tradePrice", data.get("trade_price"));
                mMap.put("semesterName", data.get("semester_name"));
                mMap.put("fiscalYearName", data.get("fiscal_year_name"));
                String str = (String) data.get("total");
                if (str != null) {
                    String[] arrOfTotalStr = str.split(",");
                    for (int i = 0; i < arrOfTotalStr.length; i++) {
                        String[] arrOfTotalIndexZero = arrOfTotalStr[i].split(" ");
                        if (arrOfTotalStr.length == 2) {
                            if (arrOfTotalIndexZero[0].equals("Cash") || arrOfTotalIndexZero[1].equals("Cash")) {
                                mMap.put("discountCashType", arrOfTotalIndexZero[0]);
                                mMap.put("cashValue", arrOfTotalIndexZero[1]);
                                mMap.put("cashType", arrOfTotalIndexZero[2] + " ");//Discount
                                mMap.put("cashAmount", arrOfTotalIndexZero[3]);
                            } else {
                                mMap.put("discountCreditType", arrOfTotalIndexZero[0]);
                                mMap.put("creditValue", arrOfTotalIndexZero[1]);
                                mMap.put("creditType", arrOfTotalIndexZero[2] + " ");//Discount
                                mMap.put("creditAmount", arrOfTotalIndexZero[3]);
                            }
                        } else if (arrOfTotalStr.length == 1) {
                            if (arrOfTotalIndexZero[0].equals("Cash")) {
                                mMap.put("discountCashType", arrOfTotalIndexZero[0]);
                                mMap.put("cashValue", arrOfTotalIndexZero[1]);
                                mMap.put("cashType", arrOfTotalIndexZero[2] + " "); //Discount
                                mMap.put("cashAmount", arrOfTotalIndexZero[3]);
                            } else {
                                mMap.put("discountCreditType", arrOfTotalIndexZero[0]);
                                mMap.put("creditValue", arrOfTotalIndexZero[1]);
                                mMap.put("creditType", arrOfTotalIndexZero[2] + " ");//Discount
                                mMap.put("creditAmount", arrOfTotalIndexZero[3]);
                            }
                        }
                    }
                }
                mMap.put("date", data.get("created_date"));
                list.add(mMap);
            }

            return list;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public TradeDiscount findByCompanyIdAndProductIdAndSemesterIdAndInvoiceNatureId(Long companyId, Long productId, Long semesterId, Long invoiceNatureId) {
        try {
            TradeDiscount tradeDiscount = new TradeDiscount();
            Optional<TradeDiscount> optionalTradeDiscount = tradeDiscountRepository.getByCompanyIdAndProductIdAndSemesterIdAndInvoiceNatureIdAndIsActiveTrueAndIsDeletedFalse(companyId, productId, semesterId, invoiceNatureId);
            if (optionalTradeDiscount.isPresent()) {
                tradeDiscount = optionalTradeDiscount.get();
            }
            return tradeDiscount;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public TradeDiscount findCurrentSemesterDiscountByCompanyIdAndProductId(Long companyId, Long productId, Long invoiceNatureId) {
        Semester semester = semesterService.getCurrentSemesterByCompany(companyId);
        return findByCompanyIdAndProductIdAndSemesterIdAndInvoiceNatureId(companyId, productId, semester.getId(), invoiceNatureId);
    }

    public List<Map> getAllByProductAndInvoiceNature(Long productId, Long invoiceNatureId) {
        try {
            List<Map> mapList = tradeDiscountRepository.getAllByProductAndInvoiceNature(productId, invoiceNatureId);
            return mapList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
