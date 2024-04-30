package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.CommonUtilityService;
import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.CreditLimitTerm;
import com.newgen.ntlsnc.globalsettings.dto.ProductTradePriceDto;
import com.newgen.ntlsnc.globalsettings.entity.*;
import com.newgen.ntlsnc.globalsettings.repository.ProductTradePriceRepository;
import com.newgen.ntlsnc.salesandcollection.entity.*;
import com.newgen.ntlsnc.salesandcollection.service.SalesBookingDetailsService;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import net.sf.jasperreports.engine.JasperPrint;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ProductTradePriceService implements IService<ProductTradePrice> {
    @Autowired
    OrganizationService organizationService;
    @Autowired
    ProductTradePriceRepository productTradePriceRepository;
    @Autowired
    ProductService productService;
    @Autowired
    CurrencyService currencyService;
    @Autowired
    SalesBookingDetailsService salesBookingDetailsService;
    @Autowired
    ReportService reportService;
    @Autowired
    ApplicationUserService applicationUserService;

    @Override
    @Transactional
    public ProductTradePrice create(Object object) {
        ProductTradePriceDto productTradePriceDto = (ProductTradePriceDto) object;

        //make previous current price to expire
        List<ProductTradePrice> previousActivePriceList = getAllCurrentTradePriceByProductId(productTradePriceDto.getProductId());
        previousActivePriceList.forEach(e -> e.setExpiryDate(LocalDateTime.now()));
        productTradePriceRepository.saveAll(previousActivePriceList);

        ProductTradePrice productTradePrice = new ProductTradePrice();
        productTradePrice.setTradePrice(productTradePriceDto.getTradePrice());
        productTradePrice.setOrganization(organizationService.getOrganizationFromLoginUser());
        productTradePrice.setProduct(productService.findById(productTradePriceDto.getProductId()));

        // Note: no currency input comes form frontend

        if (!this.validate(productTradePrice)) {
            return null;
        }

        productTradePrice = productTradePriceRepository.save(productTradePrice);
        return productTradePrice;
    }

    @Override
    @Transactional
    public ProductTradePrice update(Long id, Object object) {

        ProductTradePriceDto productTradePriceDto = (ProductTradePriceDto) object;
        ProductTradePrice productTradePrice = productTradePriceRepository.findById(productTradePriceDto.getId()).get();
        productTradePrice.setTradePrice(productTradePriceDto.getTradePrice());
        productTradePrice.setExpiryDate(LocalDateTime.parse(productTradePriceDto.getExpiryDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));//"2021-11-11 11:11:11"
        productTradePrice.setOrganization(organizationService.getOrganizationFromLoginUser());
//        if (productTradePriceDto.getOrganizationId() != null) {
//            productTradePrice.setOrganization(organizationService.findById(productTradePriceDto.getOrganizationId()));
//        }
        if (productTradePriceDto.getProductId() != null) {
            productTradePrice.setProduct(productService.findById(productTradePriceDto.getProductId()));
        }
        if (productTradePriceDto.getCurrencyId() != null) {
            productTradePrice.setCurrency(currencyService.findById(productTradePriceDto.getCurrencyId()));
        }
        if (!this.validate(productTradePrice)) {
            return null;
        }
        return productTradePriceRepository.save(productTradePrice);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            ProductTradePrice productTradePrice = findById(id);
            List<SalesBookingDetails> salesBookingDetailsList = salesBookingDetailsService.getAllByProductTradePrice(productTradePrice);
            if (salesBookingDetailsList.size() == 0) {
                productTradePrice.setIsDeleted(true);
                productTradePriceRepository.save(productTradePrice);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public ProductTradePrice findById(Long id) {
        try {
            Optional<ProductTradePrice> optionalProductTradePrice = productTradePriceRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalProductTradePrice.isPresent()) {
                throw new Exception("Product Trade Price Not exist with id " + id);
            }
            return optionalProductTradePrice.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<ProductTradePrice> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return productTradePriceRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }

    public Map findAllListByOrganizationIdAndProductCategoryId(Long companyId, List<Long> productCategoryIdList) {
        Map response = new HashMap();
        List<Map> tradePriceList = productTradePriceRepository.findAllListByOrganizationIdAndProductCategoryIdsAndTotalCategoryIdListSize(companyId, productCategoryIdList, productCategoryIdList.size());
        response.put("tradePrices", tradePriceList);
        return response;
    }

    public Map getTradePriceSumWithProductCountByCompany(Long companyId) {
        return productTradePriceRepository.getTradePriceSumWithProductCountByCompany(companyId);
    }

    public List<Map> getAllByProductId(Long productId) {
        return productTradePriceRepository.getAllByProductIdAndIsActiveTrueAndIsDeletedFalseOrderByExpiryDate(productId);
    }

    public List<ProductTradePrice> getAllCurrentTradePriceByProductId(Long productId) {
        return productTradePriceRepository.findAllByProductIdAndExpiryDateIsNullAndIsActiveTrueAndIsDeletedFalse(productId);
    }

    public JasperPrint getPriceHistoryReportByCategory(Map<String, Object> params) throws IOException, ParseException {
        //Long companyId = Long.parseLong(params.get("companyId").toString());
        Organization company = organizationService.findById(Long.parseLong(params.get("companyId").toString()));
        params.put("companyName", company.getName() + " (" + company.getShortName() + ")");
        params.put("companyAddress", company.getAddress());
        params.put("companyEmail", company.getEmail());
        params.put("companyWeb", company.getWebAddress());
        params.put("companyLogo", new ByteArrayInputStream(organizationService.getOrganizationLogoByteData(company.getId())));
        params.put("companyId", company.getId());

        Map<String, Object> loginUser = applicationUserService.getMe();
        params.put("printedBy", loginUser.get("userName"));
        Object o = loginUser.get("designation");
        params.put("printedByDesignation", ((Designation) o).getName());
        //date params
        String dateHeader = "";
        if (params.get("startDate").toString().equals("") || params.get("startDate").toString().equals("Invalid date")) {
            params.put("startDate", null);
        } else {
            LocalDate localDate = LocalDate.parse(params.get("startDate").toString(), DateTimeFormatter.ofPattern("yyyy-MM-d"));
            dateHeader += "Start Date: " + localDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) + "  ";
            params.put("startDate", params.get("startDate").toString());
        }
        if (params.get("endDate").toString().equals("")|| params.get("endDate").toString().equals("Invalid date")) {
            params.put("endDate", null);
        } else {
            LocalDate localDate = LocalDate.parse(params.get("endDate").toString(), DateTimeFormatter.ofPattern("yyyy-MM-d"));
            dateHeader += "End Date: " + localDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
            params.put("endDate", params.get("endDate").toString());
        }
        params.put("dateHeader", dateHeader);
        params.put("depotIds", Arrays.asList(params.get("depotIds").toString().split(",")));
        //product and category
        boolean isProductExist = params.get("prodtIds").toString().equals("") ? false : true;
        boolean isCategoryExist = params.get("productCategoryIds").toString().equals("") ? false : true;
        params.put("isProductExist", isProductExist);
        params.put("isCategoryExist", isCategoryExist);
        params.put("productIds", Arrays.asList(params.get("prodtIds").toString().split(",")));
        params.put("categoryIds", Arrays.asList(params.get("productCategoryIds").toString().split(",")));

        // jasper file selection
        String jasperFileName = "";
        if (params.get("reportType").toString().equals("DETAILS")) {
            params.put("isDepotExist", params.get("depotIds").toString().equals("[]") ? false : true);
            if (params.get("isWithSum").toString().equals("true")) {
                jasperFileName = "trade_price_change_history"; // details with sum report
            } else {
                jasperFileName = "trade_price_history_by_category"; // details without sum report
            }
        }

        return reportService.getReport(jasperFileName, "/reports/", params);

    }

    public String tradePriceUpload(MultipartFile tradePriceFile) {

            try {
                Workbook workbook = new XSSFWorkbook(tradePriceFile.getInputStream());
                Sheet sheet = workbook.getSheet("TradePrice");
                Iterator<Row> rows = sheet.iterator();
                int rowNumber = 0;
                while (rows.hasNext()) {
                    Row currentRow = rows.next();
                    if (rowNumber == 0) {
                        rowNumber++;
                        continue;
                    }
                    Iterator<Cell> cellsInRow = currentRow.iterator();

                    int cellIdx = 0;
                    ProductTradePrice productTradePrice = new ProductTradePrice();
                    productTradePrice.setOrganization(organizationService.getOrganizationFromLoginUser());

                    while (cellsInRow.hasNext()) {
                        Cell currentCell = cellsInRow.next();
                        if (currentCell.getCellType() == CellType.STRING)
                            System.out.println("cellIdx : " + cellIdx + " Row : " + currentCell.getStringCellValue());
                        else if (currentCell.getCellType() == CellType.NUMERIC) {
                            System.out.println("cellIdx : " + cellIdx + " Row NUMERIC: " + currentCell.getNumericCellValue());
                        } else if (currentCell.getCellType() == CellType.BLANK) {
                            System.out.println("cellIdx : " + cellIdx + " Row BLANK: " + currentCell.getNumericCellValue());
                        }



                        if (cellIdx == 0) {
                            if (currentCell.getCellType() == CellType.STRING) {
                                productTradePrice.setProduct(productService.findById(Long.valueOf(currentCell.getStringCellValue())));
                            }
                        }

                        if (cellIdx == 1) {
                            if (currentCell.getCellType() == CellType.NUMERIC) {
                                productTradePrice.setTradePrice(Double.valueOf(currentCell.getNumericCellValue()).floatValue());
                                if (productTradePrice.getTradePrice() != null) {
                                    List<ProductTradePrice> previousActivePriceList = getAllCurrentTradePriceByProductId(productTradePrice.getProduct().getId());
                                    previousActivePriceList.forEach(e -> e.setExpiryDate(LocalDateTime.now()));
                                    productTradePriceRepository.saveAll(previousActivePriceList);

                                    productTradePriceRepository.save(productTradePrice);
                                }
                            }

                            if (currentCell.getCellType() == CellType.BLANK) {
                                System.out.println(" Product Name : " + productTradePrice.getProduct().getName());
                            }

                        }

                        cellIdx++;
                    }


                }

        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }

       return "Upload done";
    }
}
