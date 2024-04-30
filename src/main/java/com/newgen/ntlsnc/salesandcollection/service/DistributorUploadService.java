package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.FileDownloadService;
import com.newgen.ntlsnc.common.FileUploadService;
import com.newgen.ntlsnc.common.enums.CreditLimitTerm;
import com.newgen.ntlsnc.globalsettings.entity.Bank;
import com.newgen.ntlsnc.globalsettings.entity.BankBranch;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.service.BankBranchService;
import com.newgen.ntlsnc.globalsettings.service.BankService;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.salesandcollection.dto.DistributorUploadDto;
import com.newgen.ntlsnc.salesandcollection.entity.*;
import com.newgen.ntlsnc.salesandcollection.repository.*;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUserCompanyMapping;
import com.newgen.ntlsnc.usermanagement.repository.ApplicationUserCompanyMappingRepository;
import com.newgen.ntlsnc.usermanagement.repository.ApplicationUserRepository;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserCompanyMappingService;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * @author sunipa
 * Created on 21/12/23 9:29 AM
 */
@Service
public class DistributorUploadService {
    @Autowired
    DistributorRepository distributorRepository;
    @Autowired
    ProprietorRepository proprietorRepository;
    @Autowired
    DistributorGuarantorRepository guarantorRepository;
    @Autowired
    DistributorSalesOfficerMapRepository salesOfficerMapRepository;
    @Autowired
    CreditLimitRepository creditLimitRepository;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    DistributorTypeService distributorTypeService;
    @Autowired
    BankService bankService;
    @Autowired
    BankBranchService bankBranchService;
    @Autowired
    ApplicationUserRepository applicationUserRepository;
    @Autowired
    DistributorService distributorService;
    @Autowired
    ApplicationUserCompanyMappingService userCompanyMappingService;
    @Autowired
    ApplicationUserCompanyMappingRepository userCompanyMappingRepository;

    public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    static String[] HEADERs = { "Id", "Title", "Description", "Published" };
    static String SHEET = "Distributors";

    public boolean hasExcelFormat(MultipartFile file) {

        if (!TYPE.equals(file.getContentType())) {
            return false;
        }

        return true;
    }
    public void save(MultipartFile file, DistributorUploadDto distributorUploadDto) {
        try {
            excelToDistributors(file.getInputStream()
                    , distributorUploadDto);

        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }


    public List<Distributor> excelToDistributors(
            InputStream is, DistributorUploadDto distributorUploadDto) {
        List<Distributor> distributors = new ArrayList<Distributor>();

        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();
            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                // skip header
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }
                Iterator<Cell> cellsInRow = currentRow.iterator();
                Organization organization = organizationService.getOrganizationFromLoginUser();
                Distributor distributor = new Distributor();
                Proprietor proprietor = new Proprietor();
                DistributorGuarantor guarantor = new DistributorGuarantor();
                DistributorSalesOfficerMap salesOfficerMap = new DistributorSalesOfficerMap();
                CreditLimit creditLimit = new CreditLimit();
                String distributorName = "";

                int cellIdx = 0;
                Optional<ApplicationUser> applicationUser = Optional.empty();
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();
                    if(currentCell.getCellType() == CellType.STRING)
                    System.out.println("cellIdx : "+ cellIdx+" Row : "+currentCell.getStringCellValue());
                    else if (currentCell.getCellType() == CellType.NUMERIC) {
                        System.out.println("cellIdx : "+ cellIdx+" Row NUMERIC: "+currentCell.getNumericCellValue());
                    }else if (currentCell.getCellType() == CellType.BLANK) {
                        System.out.println("cellIdx : "+ cellIdx+" Row BLANK: "+currentCell.getNumericCellValue());
                    }
                    if(cellIdx == 0) {
                        String distributorNameInExcel = currentCell.getStringCellValue().trim();
                        String distributorCode = distributorNameInExcel.split(" ")[0];
                        List<Long> distributorIds = distributorService.findByCode(distributorCode);
                        if(distributorIds != null){
                            System.out.println("distributorIds : "+ distributorIds);
                            break;
                        }

                    }
                    switch (cellIdx) {
                        case 0:
                            distributor.setDistributorName(currentCell.getStringCellValue().trim());
                            distributorName = currentCell.getStringCellValue().trim();
                            break;

                        case 1:
                            String contactNo = String.valueOf(Double.valueOf((currentCell.getNumericCellValue())).longValue());
                            distributor.setContactNo("0"+contactNo);
                            break;

                        case 2:
                            DistributorType distributorType = distributorTypeService.findByName("Platinum");
                            distributor.setDistributorType(distributorType);
                            break;

                        case 3:
                            distributor.setShipToAddress(currentCell.getStringCellValue().trim());
                            break;

                        case 4:
                            distributor.setBillToAddress(currentCell.getStringCellValue().trim());
                            break;

                        case 5:
                            distributor.setGeoLatitude(
                                    currentCell.getCellType() == CellType.BLANK ? "123" :
                                            String.valueOf(Double.valueOf((currentCell.getNumericCellValue())).longValue()));
                            break;

                        case 6:
                            distributor.setGeoLongitude(
                                    currentCell.getCellType() == CellType.BLANK ? "123" :
                                            String.valueOf(Double.valueOf((currentCell.getNumericCellValue())).longValue()));
                            break;

                        case 7:
                            distributor.setRadius(
                                    currentCell.getCellType() == CellType.BLANK ? "123" :
                                            String.valueOf(Double.valueOf((currentCell.getNumericCellValue())).longValue()));
                            break;

                        case 8:
                            Bank bank = bankService.findByBankShortNameAndIsDeletedFalse(currentCell.getStringCellValue());
                            List<BankBranch> bankBranch = bankBranchService.findAllBranchByBank(bank.getId());
                            distributor.setBankBranch(bankBranch.get(0));
                            break;

                        case 9:
                            if (currentCell.getCellType() == CellType.NUMERIC) {
                                distributor.setChequeNo(String.valueOf(currentCell.getNumericCellValue()));
                            }
                            else distributor.setChequeNo("123");
                            break;

                        case 10:
                            distributor.setChequeType(currentCell.getStringCellValue());
                            break;

                        case 11:
                            if (currentCell.getCellType() == CellType.STRING) {
                                distributor.setChequeAmount(Double.valueOf("0.0"));
                            }else {
                                distributor.setChequeAmount(currentCell.getNumericCellValue());
                            }
                            break;

                        case 12:
                            distributor.setOrganization(organization);
                            break;

                        case 13:
                            applicationUser =
                                    applicationUserRepository.findById(Double.valueOf(currentCell.getNumericCellValue()).longValue());
                            salesOfficerMap.setSalesOfficer(applicationUser.get());
                            break;

                            /*distributor.setTradeLicenseNo(currentCell.getStringCellValue());
                            distributor.setPesticideLicenseNo(currentCell.getStringCellValue());
                            distributor.setSeedLicenseNo(currentCell.getStringCellValue());
                            distributor.setVatRegistrationNo(currentCell.getStringCellValue());
                            distributor.setTinRegistrationNo(currentCell.getStringCellValue());*/

                        case 14:
                            if (currentCell.getStringCellValue() != null) {
                                proprietor.setName(currentCell.getStringCellValue());
                            }
                            else
                                proprietor.setName("123");
                            break;

                        case 15:
                            String pcontactNo = currentCell.getCellType() != CellType.STRING
                                    ? String.valueOf(Double.valueOf((currentCell.getNumericCellValue())).longValue())
                                    : "123";
                            proprietor.setContactNo("0"+pcontactNo);
                            break;
                        case 16:
                            proprietor.setAddress(currentCell.getStringCellValue());
                            break;

                        default:
                            break;
                    }

                    cellIdx++;
                }

                if (validate(distributorName, organization,
                        organizationService.findById(distributorUploadDto.getCompanyId()).getId())) {


                distributorRepository.save(distributor);

                salesOfficerMap.setDistributor(distributor);
                salesOfficerMap.setFromDate(LocalDate.now());
                salesOfficerMap.setCompany(organizationService.findById(distributorUploadDto.getCompanyId()));
                salesOfficerMap.setOrganization(organization);
                salesOfficerMapRepository.save(salesOfficerMap);

                proprietor.setDistributor(distributor);
                proprietor.setOrganization(organization);
                proprietor.setNid("123");
                proprietorRepository.save(proprietor);

                guarantor.setName("123");
                guarantor.setDistributor(distributor);
                guarantor.setContactNo("123");
                guarantor.setNid("123");
                guarantor.setOrganization(organization);
                guarantorRepository.save(guarantor);

                creditLimit.setDistributor(distributor);
                creditLimit.setCreditLimitTerm(CreditLimitTerm.LT);
                creditLimit.setCreditLimit(1000000f);
                creditLimit.setOrganization(organization);
                creditLimit.setCompany(organizationService.findById(distributorUploadDto.getCompanyId()));
                creditLimitRepository.save(creditLimit);

                distributors.add(distributor);
                System.out.println("Distributor Size : " + distributors.size());
                }
            }

            workbook.close();

            return distributors;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage()
            + "Distributor Size : " + distributors.size());
        }
    }

    public boolean validate(String name, Organization organization, Long companyId) {
        Optional<Distributor> optionalDistributor = Optional.empty();
            optionalDistributor =
                    distributorRepository.findByOrganizationAndDistributorNameIgnoreCaseAndIsDeletedFalse(
                    organization, name.trim());
        if (optionalDistributor.isPresent()) {
            return false;
        }
        return true;
    }

}
