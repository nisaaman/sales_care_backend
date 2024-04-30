package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.*;
import com.newgen.ntlsnc.common.enums.FileType;
import com.newgen.ntlsnc.globalsettings.entity.*;
import com.newgen.ntlsnc.globalsettings.service.*;
import com.newgen.ntlsnc.salesandcollection.dto.DistributorDto;
import com.newgen.ntlsnc.salesandcollection.dto.DistributorGuarantorDto;
import com.newgen.ntlsnc.salesandcollection.dto.DistributorSalesOfficerMapDto;
import com.newgen.ntlsnc.salesandcollection.dto.ProprietorDto;
import com.newgen.ntlsnc.salesandcollection.entity.*;
import com.newgen.ntlsnc.salesandcollection.repository.DistributorRepository;
import com.newgen.ntlsnc.salesandcollection.repository.DistributorSalesOfficerMapRepository;
import com.newgen.ntlsnc.supplychainmanagement.service.SalesReturnService;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import com.newgen.ntlsnc.usermanagement.service.ReportingManagerService;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author sagor
 * Created on 5/4/22 10:29 AM
 */
@Service
public class DistributorService implements IService<Distributor> {

    @Autowired
    OrganizationService organizationService;
    @Autowired
    DistributorRepository distributorRepository;
    @Autowired
    DistributorTypeService distributorTypeService;
    @Autowired
    LocationService locationService;
    @Autowired
    BankBranchService bankBranchService;
    @Autowired
    SemesterService semesterService;
    @Autowired
    DocumentService documentService;
    @Autowired
    DistributorSalesOfficerMapRepository distributorSalesOfficerMapRepository;
    @Autowired
    DistributorSalesOfficerMapService distributorSalesOfficerMapService;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    ReportingManagerService reportingManagerService;
    @Autowired
    DepotService depotService;
    @Autowired
    LocationManagerMapService locationManagerMapService;
    @Autowired
    ProprietorService proprietorService;
    @Autowired
    DistributorGuarantorService distributorGuarantorService;
    @Autowired
    CreditLimitService creditLimitService;

    @Autowired
    FileUploadService fileUploadService;
    @Autowired
    FileDownloadService fileDownloadService;
    @Autowired
    AccountingYearService accountingYearService;
    @Autowired
    SalesReturnProposalService salesReturnProposalService;
    @Autowired
    SalesReturnService salesReturnService;

    private static final String DISTRIBUTOR_LOGO_UPLOAD_DIRECTORY = "distributor";
    private static final String DISTRIBUTOR_LOGO_UPLOAD_PREFIX = "distributor_";
    private static final String UNDERSCORE = "_";
    private static final String FILE_TYPE = "distributor_logo";


    @Override
    @Transactional
    public Distributor create(Object object) {

        try {
            DistributorDto distributorDto = (DistributorDto) object;
            Distributor distributor = new Distributor();
            List<DistributorSalesOfficerMapDto> distributorSalesOfficerMapDtoList = distributorDto.getDistributorSalesOfficerMapDtoList();

            DistributorType distributorType = distributorTypeService.findById(distributorDto.getDistributorTypeId());
            if (distributorDto.getEmail() !=null &&
                    distributorDto.getEmail().trim() != null
                    && distributorDto.getEmail().trim() != "") {
                Optional<Distributor> distributorOptional = null;
                if (distributorDto.getId() == null ) {
                     distributorOptional =
                            distributorRepository.findByEmail(distributorDto.getEmail().trim());
                }
               else {
                    distributorOptional =
                            distributorRepository.findByEmailAndIdIsNot(
                                    distributorDto.getEmail().trim(), distributorDto.getId());
                }
                if (distributorOptional.isPresent()) {
                    throw new RuntimeException("Distributor Email already exist.");
                }
            }

            BankBranch bankBranch = bankBranchService.findById(distributorDto.getBranchId());
            distributor.setDistributorName(distributorDto.getDistributorName().trim());
            distributor.setContactNo(distributorDto.getContactNo());

            if (distributorDto.getId() != null) {
                distributor.setId(distributorDto.getId());
            }
            if (distributorDto.getEmail() != null) {
                distributor.setEmail(distributorDto.getEmail().trim());
            }
            if (distributorDto.getTradeLicenseNo() != null) {
                distributor.setTradeLicenseNo(distributorDto.getTradeLicenseNo().trim());
            }
            if (distributorDto.getPesticideLicenseNo() != null) {
                distributor.setPesticideLicenseNo(distributorDto.getPesticideLicenseNo().trim());
            }
            if (distributorDto.getSeedLicenseNo() != null) {
                distributor.setSeedLicenseNo(distributorDto.getSeedLicenseNo().trim());
            }
            if (distributorDto.getVatRegistrationNo() != null) {
                distributor.setVatRegistrationNo(distributorDto.getVatRegistrationNo().trim());
            }
            if (distributorDto.getTinRegistrationNo() != null) {
                distributor.setTinRegistrationNo(distributorDto.getTinRegistrationNo().trim());
            }

            distributor.setShipToAddress(distributorDto.getShipToAddress().trim());
            distributor.setBillToAddress(distributorDto.getBillToAddress().trim());
            distributor.setBankBranch(bankBranch);
            distributor.setGeoLatitude(distributorDto.getGeoLatitude().trim());
            distributor.setGeoLongitude(distributorDto.getGeoLongitude().trim());
            distributor.setRadius(distributorDto.getRadius().trim());
            distributor.setChequeNo(distributorDto.getChequeNo().trim());
            distributor.setChequeType(distributorDto.getChequeType().trim());
            distributor.setChequeAmount(distributorDto.getChequeAmount());
            distributor.setDistributorType(distributorType);
            Location location = locationService.findById(distributorSalesOfficerMapDtoList.get(0).getLocationId());
            distributor.setLocation(location);

            Organization organization = organizationService.getOrganizationFromLoginUser();
            distributor.setOrganization(organization);

            if (!this.validate(distributor, distributorDto.getId())) {
                return null;
            }

            distributorRepository.save(distributor);

            if (distributorDto.getDistributorLogo() != null) {

                String filePath = fileUploadService.fileUpload(distributorDto.getDistributorLogo(), FileType.LOGO.getCode(),
                        DISTRIBUTOR_LOGO_UPLOAD_DIRECTORY,
                        organization.getId(), organization.getId());

                documentService.save(DISTRIBUTOR_LOGO_UPLOAD_DIRECTORY, filePath, distributor.getId(),
                        fileUploadService.getFileNameFromFilePath(filePath), FileType.LOGO.getCode()
                        , organization, organization.getId(), distributorDto.getDistributorLogo().getSize());
            }


            List<ProprietorDto> proprietorDtoList = distributorDto.getProprietorDtoList();
            if (proprietorDtoList != null) {
                proprietorService.saveAll(proprietorDtoList, distributor, distributorDto.getProprietorLogoList());
            }
            List<DistributorGuarantorDto> distributorGuarantorDtoList = distributorDto.getDistributorGuarantorDtoList();
            if (distributorGuarantorDtoList != null) {
                distributorGuarantorService.saveAll(distributorGuarantorDtoList, distributor, distributorDto.getDistributorGuarantorLogoList());
            }
            if (distributorSalesOfficerMapDtoList != null) {
                distributorSalesOfficerMapService.saveAll(distributorSalesOfficerMapDtoList, distributor);
            }

            return distributor;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Distributor update(Long id, Object object) {
        try {
            DistributorDto distributorDto = (DistributorDto) object;
            Distributor distributor = this.findById(distributorDto.getId());

            if (distributorDto.getEmail()!=null &&
                    distributorDto.getEmail().trim() != null &&
                    distributorDto.getEmail().trim() != "") {
                Optional<Distributor> distributorOptional = distributorRepository.findByEmailAndIdIsNot(distributorDto.getEmail().trim(), distributorDto.getId());
                if (distributorOptional.isPresent()) {
                    throw new RuntimeException("Distributor Email already exist.");
                }
            }

            distributor.setDistributorName(distributorDto.getDistributorName().trim());
            distributor.setContactNo(distributorDto.getContactNo());
            distributor.setShipToAddress(distributorDto.getShipToAddress().trim());
            distributor.setBillToAddress(distributorDto.getBillToAddress().trim());
            distributor.setGeoLatitude(distributorDto.getGeoLatitude().trim());
            distributor.setGeoLongitude(distributorDto.getGeoLongitude().trim());
            distributor.setRadius(distributorDto.getRadius().trim());

            BankBranch bankBranch = bankBranchService.findById(distributorDto.getBranchId());
            distributor.setBankBranch(bankBranch);
            distributor.setChequeNo(distributorDto.getChequeNo().trim());
            distributor.setChequeType(distributorDto.getChequeType().trim());
            distributor.setChequeAmount(distributorDto.getChequeAmount());

            if (distributorDto.getEmail() != null) {
                distributor.setEmail(distributorDto.getEmail().trim());
            }
            if (distributorDto.getTradeLicenseNo() != null) {
                distributor.setTradeLicenseNo(distributorDto.getTradeLicenseNo().trim());
            }
            if (distributorDto.getPesticideLicenseNo() != null) {
                distributor.setPesticideLicenseNo(distributorDto.getPesticideLicenseNo().trim());
            }
            if (distributorDto.getSeedLicenseNo() != null) {
                distributor.setSeedLicenseNo(distributorDto.getSeedLicenseNo().trim());
            }
            if (distributorDto.getVatRegistrationNo() != null) {
                distributor.setVatRegistrationNo(distributorDto.getVatRegistrationNo().trim());
            }
            if (distributorDto.getTinRegistrationNo() != null) {
                distributor.setTinRegistrationNo(distributorDto.getTinRegistrationNo().trim());
            }

            if (distributorDto.getDistributorTypeId() != null) {
                DistributorType distributorType = distributorTypeService.findById(distributorDto.getDistributorTypeId());
                distributor.setDistributorType(distributorType);
            }

            if (!this.validate(distributor, distributorDto.getId())) {
                return null;
            }

            Organization organization = organizationService.getOrganizationFromLoginUser();
            distributorRepository.save(distributor);

            if (distributorDto.getDistributorLogo() != null) {
                String filePath = fileUploadService.fileUpload(distributorDto.getDistributorLogo(), FileType.LOGO.getCode(),
                        DISTRIBUTOR_LOGO_UPLOAD_DIRECTORY,
                        organization.getId(), organization.getId());

                documentService.save(DISTRIBUTOR_LOGO_UPLOAD_DIRECTORY, filePath, distributor.getId(),
                        fileUploadService.getFileNameFromFilePath(filePath), FileType.LOGO.getCode()
                        , organization, organization.getId(), distributorDto.getDistributorLogo().getSize());
            }

            List<ProprietorDto> proprietorDtoList = distributorDto.getProprietorDtoList();
            if (proprietorDtoList != null) {
                proprietorService.saveAll(proprietorDtoList, distributor, distributorDto.getProprietorLogoList());
            }
            List<DistributorGuarantorDto> distributorGuarantorDtoList = distributorDto.getDistributorGuarantorDtoList();
            if (distributorGuarantorDtoList != null) {
                distributorGuarantorService.saveAll(distributorGuarantorDtoList, distributor, distributorDto.getDistributorGuarantorLogoList());
            }
            List<DistributorSalesOfficerMapDto> distributorSalesOfficerMapDtoList = distributorDto.getDistributorSalesOfficerMapDtoList();
            if (distributorSalesOfficerMapDtoList != null) {
                distributorSalesOfficerMapService.saveAll(distributorSalesOfficerMapDtoList, distributor);
            }

            return distributor;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            Distributor distributor = findById(id);
            distributor.setIsDeleted(true);
            distributorRepository.save(distributor);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Distributor findById(Long id) {
        try {
            Optional<Distributor> optionalDistributor = distributorRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalDistributor.isPresent()) {
                throw new Exception("Distributor Not exist with id " + id);
            }
            return optionalDistributor.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<Distributor> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return distributorRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Override
    public boolean validate(Object object) {
        return false;
    }

    public List<Map<String, Object>> findAllDistributorListByCompanyId(Long companyId) {
        Optional<Semester> semester = semesterService.findSemesterByDate(LocalDate.now(), companyId);
        ApplicationUser applicationUser = applicationUserService.getApplicationUserFromLoginUser();
        List<Map<String, Object>> distributorList = new ArrayList<Map<String, Object>>();
        distributorRepository.findAllDistributorList(applicationUser.getId(), companyId).forEach(map -> {
            Map<String, Object> distributor = new HashMap<String, Object>();
            distributor.putAll(map);
            distributor.put("creditLimit", creditLimitService.getDistributorLimit(Long.parseLong(map.get("id").toString()), companyId));
            distributor.put("ledgerBalance", getDistributorLedgerBalance(companyId, Long.parseLong(map.get("id").toString()), LocalDate.now()));
            distributorList.add(distributor);
        });
        return distributorList;
    }

    public Double findDistributorCreditLimitByBookingNo(Long bookingId) {
        return distributorRepository.findDistributorCreditLimitByBookingNo(bookingId);
    }

    public List<Map<String, Object>> getSalesOfficerWiseDistributorsDetailsWithLedgerBalance(
            Long salesOfficerUserId, Long companyId, LocalDate fromDate, LocalDate asOnDate) throws Exception {

        List<Long> distributorList = getDistributorListFromSalesOfficer(salesOfficerUserId,
                companyId);

        return getDistributorsDetailsWithLedgerBalance(distributorList, companyId, fromDate, asOnDate);
    }

    public List<Map<String, Object>> getDistributorsDetailsWithLedgerBalance(
            List<Long> distributorList, Long companyId, LocalDate fromDate, LocalDate asOnDate) throws Exception {

        List<Map<String, Object>> distributorDetails = distributorRepository
                .getDistributorsDetailsWithLedgerBalance(distributorList, companyId, fromDate, asOnDate);

        List<Map<String, Object>> resultList = new ArrayList<>();
        distributorDetails.forEach(dis -> {
            Map<String, Object> map = new HashMap<>();
            String filePath = documentService.getDocumentFilePath("Distributor",
                    Long.parseLong(String.valueOf(dis.get("id"))), FileType.LOGO);

            map.put("id", dis.get("id"));
            map.put("distributorName", dis.get("distributorName"));
            map.put("contactNo", dis.get("contactNo"));
            map.put("ledgerBalance", dis.get("ledgerBalance"));
            map.put("distributorLogo", CommonUtilityService.convertFileImageToBase64String(
                    filePath, CommonConstant.CONTENT_UPLOAD_ROOT_FOLDER));

            resultList.add(map);
        });

        return resultList;
    }


    public List<Long> getDistributorListFromSalesOfficer(Long salesOfficerUserId, Long companyId) {
        List<Long> salesOfficerList = locationManagerMapService.getSalesOfficerListFromManager(
                salesOfficerUserId);

        return salesOfficerList.size() == 0 ? null : distributorSalesOfficerMapRepository
                .findAllDistributorBySalesOfficers(salesOfficerList, companyId);
    }

    public List<Distributor> getAllBySalesOfficerList(List<ApplicationUser> salesOfficerList) {
        List<DistributorSalesOfficerMap> distributorMapList = distributorSalesOfficerMapService.findAllBySalesOfficer(salesOfficerList);
        return distributorMapList.stream().map(disSoMap -> disSoMap.getDistributor()).collect(Collectors.toList());
    }

    public List<Distributor> getAllByCompanyAndSalesOfficerList(Long companyId, List<ApplicationUser> salesOfficerList) {
        List<DistributorSalesOfficerMap> distributorMapList = distributorSalesOfficerMapService.findAllByCompanyIdAndSalesOfficer(companyId, salesOfficerList);
        return distributorMapList.stream().map(disSoMap -> disSoMap.getDistributor()).collect(Collectors.toList());
    }

    public List<Distributor> getAllByCompanyIdAndLocationIdList(Long companyId, List<Long> locationIdList) {
        List<Location> territoryLocationList = new ArrayList<>();
        if (locationIdList.size() == 0) { // all location
            territoryLocationList = locationService.findAllTerritoryByCompanyId(companyId);
        } else { //selected location
            territoryLocationList = locationService.findAllTerritoryByCompanyIdAndLocationIdList(companyId, locationIdList);
        }

        List<ApplicationUser> salesOfficerList = reportingManagerService.findAllSalesOfficerByTerritoryLocationList(territoryLocationList);
        List<Distributor> distributorList = getAllByCompanyAndSalesOfficerList(companyId, salesOfficerList);
        return distributorList;
    }

    public double getDistributorLedgerBalance(Long companyId, Long distributorId, LocalDate asOnDate) {
        Object o = distributorRepository.getDistributorLedgerBalancePeriodicOrAsOnDate(
                companyId, distributorId, null, asOnDate);
        double distributorBalance = o == null ? 0.0 : Double.parseDouble(o.toString());
        return distributorBalance;
    }

    public Double getDistributorCreditLimit(Long distributorId, Long semesterId) {
        return distributorRepository.getDistributorCreditLimit(distributorId, semesterId);
    }


    public List<Map> getPaymentCollectionAdjustmentDistributorList(Long companyId, List<Long> distributorIdList) {
        //parameter locationIds.size() is used to set condition when location size is 0 then all location
        List<Map> distributorList = distributorRepository.getPaymentCollectionAdjustmentDistributorListByCompanyIdAndLocationList(companyId, distributorIdList);
        return distributorList;
    }

    public List<Map<String, Object>> getDistributorsDetailsWithPeriodicLedgerBalance(
            List<Long> distributorList, Long companyId, LocalDate fromDate, LocalDate asOnDate) {
        List<Map<String, Object>> distributorDetails = distributorRepository
                .getDistributorsDetailsWithLedgerBalance(distributorList, companyId, fromDate, asOnDate);
        return distributorDetails;
    }

    public List<Map<String, Object>> getDistributorsDetailsWithOpeningLedgerBalance(
            List<Long> distributorList, Long companyId, LocalDate toDate) {
        List<Map<String, Object>> distributorDetails = distributorRepository
                .getDistributorsDetailsWithLedgerBalance(distributorList, companyId, null, toDate);
        return distributorDetails;
    }

    public List<Map> getDistributorListWithLogo(List<Long> distributorIdList) {
        List<Distributor> distributors = distributorRepository.findAllByIdIn(distributorIdList);

        List<Map> resultList = new ArrayList<>();
        distributors.forEach(dis -> {
            Map<String, Object> map = new HashMap<>();
            String filePath = documentService.getDocumentFilePath("Distributor", dis.getId(), FileType.LOGO);

            map.put("id", dis.getId());
            map.put("distributorName", dis.getDistributorName());
            map.put("contactNo", dis.getContactNo());
            map.put("distributorLogo", CommonUtilityService.convertFileImageToBase64String(
                    filePath, CommonConstant.CONTENT_UPLOAD_ROOT_FOLDER));

            resultList.add(map);
        });

        return resultList;
    }


    public String getDistributorLogo(Long distributorId) {
        Map<String, Object> map = new HashMap<>();
        String filePath = documentService.getDocumentFilePath("Distributor",
                Long.parseLong(String.valueOf(distributorId)), FileType.LOGO);

        String distributorLogo = CommonUtilityService.convertFileImageToBase64String(
                filePath, CommonConstant.CONTENT_UPLOAD_ROOT_FOLDER);

        return distributorLogo;
    }

    public Map getDistributorDetailsInfo(Long companyId, Long distributorId) {
        Map distributorInfo = new HashMap();
        distributorInfo.put("info", findById(distributorId));
        distributorInfo.put("logo", getDistributorLogo(distributorId));
        distributorInfo.put("currentBalance", getDistributorLedgerBalance(companyId, distributorId, LocalDate.now()));
        Map locationForDistributor = depotService.getDepotAndTerritoryLocationByCompanyIdAndDistributorId(companyId, distributorId);
        distributorInfo.put("distributorLocation", locationForDistributor);
        if (locationForDistributor.size() != 0) { // when no sales officer found
            Map depotInfoMap = depotService.getSalesOfficerDepotInfo(companyId, Long.parseLong(locationForDistributor.get("sales_officer_id").toString()));
            distributorInfo.put("depotInfo", depotInfoMap);
        }
        return distributorInfo;
    }

    public Map getDistributorWithDepotAndLocation(Long companyId, Long distributorId) {
        Map distributorInfo = new HashMap();
        distributorInfo.put("info", findById(distributorId));
        distributorInfo.put("distributorLogo", getDistributorLogo(distributorId));
        Map locationForDistributor = depotService.getDepotAndTerritoryLocationByCompanyIdAndDistributorId(companyId, distributorId);
        distributorInfo.put("depotAndLocation", locationForDistributor);  // after depot business change,only Location info will get
        distributorInfo.put("distributorLocation", locationForDistributor); //it should be use now
        if (locationForDistributor.size() != 0) { // when no sales officer found
            Map depotInfoMap = depotService.getSalesOfficerDepotInfo(companyId, Long.parseLong(locationForDistributor.get("sales_officer_id").toString()));
            distributorInfo.put("depotInfo", depotInfoMap);
        }
        return distributorInfo;
    }

    public Double getPeriodicLedgerBalanceBySO(
            Long salesOfficerId, Long companyId, LocalDate fromDate, LocalDate asOnDate) {
        return distributorRepository
                .getPeriodicLedgerBalanceBySO(salesOfficerId, companyId, fromDate, asOnDate);
    }

    public List<Map<String, Object>> findList(Long userLoginId, Long locationId, Long companyId) {
        List<Long> saleOfficerList;
        Map<Long, Object> childLocationMap = new HashMap<>();
        if (locationId != null) {
            childLocationMap =
                    locationService.getChildLocationsByParent(
                            companyId, locationId, childLocationMap);
            saleOfficerList = locationService.getSoListByLocation(companyId, childLocationMap);
        } else {
            Boolean isManager = applicationUserService.checkLoginUserIsManager(companyId, userLoginId);
            Boolean isDepotManager = applicationUserService.checkLoginUserIsDepotManager(companyId, userLoginId);

            if (isManager) {
                locationId = locationService.getManagerLocation(companyId, userLoginId);
                childLocationMap =
                        locationService.getChildLocationsByParent(
                                companyId, locationId, childLocationMap);
                saleOfficerList = locationService.getSoListByLocation(companyId, childLocationMap);

            }
//            else if (isDepotManager) {
//                List<Map> areaList = depotService.findDepotAreaList(companyId, userLoginId);
//                for (Map area : areaList) {
//                    locationId = Long.parseLong(area.get("id").toString());
//                    Map<Long, Object> childLocationMap1 =
//                            locationService.getChildLocationsByParent(companyId, locationId,
//                                    childLocationMap);
//                    childLocationMap.putAll(childLocationMap1);
//                }
//                saleOfficerList = locationService.getSoListByLocation(companyId, childLocationMap);
//            }
            else {
                List<Location> parentLocations =
                        locationService.getParentLocationsList(companyId);
                for (Location parentLocation : parentLocations) {
                    Map<Long, Object> childLocationMap1 =
                            locationService.getChildLocationsByParent(companyId, parentLocation.getId(),
                                    childLocationMap);
                    childLocationMap.putAll(childLocationMap1);
                }
                saleOfficerList = locationService.getSoListByLocation(companyId, childLocationMap);
            }
        }

        Optional<Semester> semester = semesterService.findSemesterByDate(LocalDate.now(), companyId);
        List<Map<String, Object>> distributorList = new ArrayList<Map<String, Object>>();
        distributorRepository.findList(companyId, saleOfficerList).forEach(map -> {
            Map<String, Object> distributor = new HashMap<String, Object>();
            distributor.putAll(map);
            Float creditLimit = 0F;
            try {
                if ( map.get("distributorId") != null) {
                    creditLimit = creditLimitService.getDistributorLimit(
                            Long.parseLong(map.get("distributorId").toString()), companyId);
                }
            }
            catch (RuntimeException e) {
                System.out.println(e.getMessage());
            }

            distributor.put("creditLimit", creditLimit);
//            String s= new String(Base64.encodeBase64(fileDownloadService.fileDownload(
//                    documentService.getFilePath(Long.parseLong(map.get("distributorId").toString()),DISTRIBUTOR_LOGO_UPLOAD_DIRECTORY))));

//            map.put("filePath",s);
            distributorList.add(distributor);
        });
        return distributorList;

    }

    public DistributorDto getDistributorInfoWithProprietorAndGuarantor(Long id) {

        DistributorDto distributorDto = new DistributorDto();

        Optional<Distributor> distributorOpt = distributorRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);

        Distributor distributor = distributorOpt.get();
        distributorDto.setId(distributor.getId());
        distributorDto.setDistributorName(distributor.getDistributorName());
        distributorDto.setBillToAddress(distributor.getBillToAddress());
        distributorDto.setContactNo(distributor.getContactNo());
        distributorDto.setShipToAddress(distributor.getShipToAddress());
        distributorDto.setChequeAmount(distributor.getChequeAmount());
        distributorDto.setChequeNo(distributor.getChequeNo());
        distributorDto.setChequeType(distributor.getChequeType());
        distributorDto.setEmail(distributor.getEmail());
        distributorDto.setGeoLatitude(distributor.getGeoLatitude());
        distributorDto.setGeoLongitude(distributor.getGeoLongitude());
        distributorDto.setRadius(distributor.getRadius());
        distributorDto.setPesticideLicenseNo(distributor.getPesticideLicenseNo());
        distributorDto.setSeedLicenseNo(distributor.getSeedLicenseNo());
        distributorDto.setTinRegistrationNo(distributor.getTinRegistrationNo());
        distributorDto.setTradeLicenseNo(distributor.getTradeLicenseNo());
        distributorDto.setVatRegistrationNo(distributor.getVatRegistrationNo());
        BankBranch bankBranch = distributor.getBankBranch();
        distributorDto.setBranchId(bankBranch.getId());
        distributorDto.setBankId(bankBranch.getBank().getId());
        distributorDto.setDistributorTypeId(distributor.getDistributorType().getId());
        String filePath = documentService.getDocumentFilePath(DISTRIBUTOR_LOGO_UPLOAD_DIRECTORY, distributor.getId(), FileType.LOGO);

        if ((!filePath.equals("")) && filePath != null) {
//            distributorDto.setFilePath(filePath);
            String s = new String(Base64.encodeBase64(fileDownloadService.fileDownload(
                    documentService.getFilePath(distributor.getId(), DISTRIBUTOR_LOGO_UPLOAD_DIRECTORY))));
            distributorDto.setFilePath(s);
        }
        distributorDto.setProprietorDtoList(proprietorService.getProprietorInfoByDistributor(distributor));
        distributorDto.setDistributorGuarantorDtoList(distributorGuarantorService.getDistributorInfoWithGuarantorByDistributor(distributor));
        distributorDto.setDistributorSalesOfficerMapDtoList(distributorSalesOfficerMapService.getDistributorInfoWithSalesOfficerByDistributor(distributor));
        return distributorDto;
    }


    public List<Map> getDistributorListWithAsOnDateBalanceAndCurrentSemesterCreditLimit(Long companyId, List<Long> locationIds) {
        List<Location> territoryLocationList = new ArrayList<>();
        if (locationIds.size() == 0) { // all location
            territoryLocationList = locationService.findAllTerritoryByCompanyId(companyId);
        } else { //selected location
            territoryLocationList = locationService.findAllTerritoryByCompanyIdAndLocationIdList(companyId, locationIds);
        }

        List<ApplicationUser> salesOfficerList = reportingManagerService.findAllSalesOfficerByTerritoryLocationList(territoryLocationList);
        List<Long> salesOfficerIdList = salesOfficerList.stream().map(s -> s.getId()).collect(Collectors.toList());

        Optional<Semester> optionalSemester = semesterService.findSemesterByDate(LocalDate.now(), companyId);
        return getDistributorListWithAsOnDateBalanceAndSemesterWiseCreditLimit(companyId, salesOfficerIdList, optionalSemester.get().getId());
    }

    public List<Map> getDistributorListWithAsOnDateBalanceAndSemesterWiseCreditLimit(Long companyId, List<Long> salesOfficerList, Long semesterId) {
        List<Map> distributorDetails = distributorRepository.getDistributorListWithAsOnDateBalanceAndSemesterWiseCreditLimit(companyId, salesOfficerList);
        return distributorDetails;
    }

    public List<Map> getDistributorListWithOrderInfo(
            Long companyId, List<Long> salesOfficerList,
            LocalDate startDate, LocalDate endDate) {
        List<Map> distributorDetails =
                distributorRepository.getDistributorListWithOrderInfo(companyId, salesOfficerList,
                        startDate, endDate);
        return distributorDetails;
    }

    public Map getLedgerBalanceAndCreditLimitByDistributorIdAndCompanyId(Long companyId, Long distributorId) {

        Map distributorInfo = new HashMap();

        Float creditLimit = creditLimitService.getDistributorLimit(distributorId, companyId);

        double balance = getDistributorLedgerBalance(distributorId, companyId, LocalDate.now());

        Distributor distributor = this.findById(distributorId);

        String filePath = documentService.getDocumentFilePath("Distributor", distributorId, FileType.LOGO);

        distributorInfo.put("creditLimit", creditLimit);
        distributorInfo.put("balance", balance);
        distributorInfo.put("distributorType", distributor.getDistributorType().getName());
        distributorInfo.put("distributorLogo", CommonUtilityService.convertFileImageToBase64String(
                filePath, CommonConstant.CONTENT_UPLOAD_ROOT_FOLDER));

        return distributorInfo;
    }


    public Map getSalesOfficerWiseDistributorsDetailsWithLedgerBalanceWithTotalBalance(
            Long companyId, Long accountingYearId) throws Exception {
        Long salesOfficerUserId = applicationUserService.getApplicationUserIdFromLoginUser();

        AccountingYear accountingYear = accountingYearService.findById(accountingYearId);

        LocalDate startDate = accountingYear.getStartDate();
        LocalDate endDate = accountingYear.getEndDate();

        List<Long> salesOfficerList = Stream.of(salesOfficerUserId).collect(Collectors.toList());

        List<Long> distributorList = distributorSalesOfficerMapRepository
                .findAllDistributorBySalesOfficers(salesOfficerList, companyId);

        List<Map<String, Object>> distributorDetails = distributorRepository.getDistributorsDetailsWithLedgerBalance(distributorList, companyId, startDate, endDate);

        double totalBalance = 0.0;
        List<Map> resultList = new ArrayList<>();
        for (int i = 0; i < distributorDetails.size(); i++) {
            Map map = new HashMap<>();
            String filePath = documentService.getDocumentFilePath("Distributor",
                    Long.parseLong(String.valueOf(distributorDetails.get(i).get("id"))), FileType.LOGO);

            double tempValue = (double) distributorDetails.get(i).get("ledgerBalance");
            map.put("id", distributorDetails.get(i).get("id"));
            map.put("distributorName", distributorDetails.get(i).get("distributorName"));
            map.put("contactNo", distributorDetails.get(i).get("contactNo"));
            map.put("ledgerBalance", distributorDetails.get(i).get("ledgerBalance"));
            map.put("distributorLogo", CommonUtilityService.convertFileImageToBase64String(
                    filePath, CommonConstant.CONTENT_UPLOAD_ROOT_FOLDER));
            totalBalance = totalBalance + tempValue;
            resultList.add(map);
        }
        Map finalMap = new HashMap<>();
        finalMap.put("totalBalance", totalBalance);
        finalMap.put("list", resultList);


        return finalMap;
    }

    public List<Map<String, Object>> getDistributorLedgerTransaction(
            Long distributorId, Long companyId, LocalDate fromDate, LocalDate thruDate) {

        return distributorRepository.getDistributorLedgerTransaction(
                distributorId, companyId, fromDate, thruDate);
    }

    public boolean validate(Object object, Long id) {
        Distributor distributor = (Distributor) object;
        Optional<Distributor> optionalDistributor = Optional.empty();

        if (id == null) {
            optionalDistributor = distributorRepository.findByOrganizationAndDistributorNameIgnoreCaseAndIsDeletedFalse(
                    distributor.getOrganization(), distributor.getDistributorName().trim());
        } else {
            optionalDistributor = distributorRepository.findByOrganizationAndIdIsNotAndDistributorNameIgnoreCaseAndIsDeletedFalse(
                    distributor.getOrganization(), id, distributor.getDistributorName().trim());
        }
        if (optionalDistributor.isPresent()) {
            throw new RuntimeException("Distributor Name is already exist.");
        }
        return true;
    }

    public List<Map<String, Object>> getDistributorWithoutCreditLimit(Long companyId) {
        try {
            return distributorRepository.findDistributorForCreditLimit(companyId);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map<String, Object>> findLedgerDetails(
            Long distributorId, Long companyId,
            LocalDate startDate, LocalDate endDate) {
        try {
            List<Map<String, Object>> ledgerDetails = new ArrayList<>();
            List<Map<String, Object>> distributorOpeningBalanceList =
                    distributorRepository.getDistributorsDetailsWithLedgerBalance(
                            Stream.of(distributorId).collect(
                                    Collectors.toList()), companyId, null, startDate.minusDays(1));
            List<Map<String, Object>> distributorLedgerTransactionList =
                    getDistributorLedgerTransaction(distributorId, companyId, startDate, endDate);
            for (Map<String, Object> distributorOpeningBalance : distributorOpeningBalanceList) {
                BigDecimal distributorBalance = new BigDecimal(distributorOpeningBalance.get("ledgerBalance").toString());
                Map<String, Object> ledger = new HashMap<>();
                ledger.put("date", startDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
                ledger.put("year_and_month", startDate.format(DateTimeFormatter.ofPattern("MMM yyyy")));
                ledger.put("transactionType", "");
                ledger.put("transactionNumber","");
                ledger.put("description", "Opening Balance");
                if (distributorBalance.compareTo(BigDecimal.ZERO) >= 0) {
                    ledger.put("debit", distributorBalance.setScale(2, RoundingMode.HALF_UP));
                    ledger.put("credit", BigDecimal.ZERO);
                } else {
                    ledger.put("debit", BigDecimal.ZERO);
                    ledger.put("credit", distributorBalance.negate().setScale(2, RoundingMode.HALF_UP));
                }
                BigDecimal openingBalance = new BigDecimal(ledger.get("debit").toString()).setScale(4, RoundingMode.HALF_UP)
                        .subtract(new BigDecimal(ledger.get("credit").toString()).setScale(4, RoundingMode.HALF_UP));
                ledger.put("balance", openingBalance.setScale(2, RoundingMode.HALF_UP));
                ledgerDetails.add(ledger);

                for (Map<String, Object> ledgerTransaction : distributorLedgerTransactionList) {
                    BigDecimal returnAmounts = new BigDecimal(0);
//                    if ("Sales Return".equals(
//                            String.valueOf(ledgerTransaction.get("transactionType")).trim())) {
//                        Long returnId = Long.valueOf(String.valueOf(ledgerTransaction.get("row_id")));
//                        SalesInvoice salesInvoice = salesReturnProposalService.findById(returnId).getSalesInvoice();
//                        if (salesInvoice != null) {
//                            /*Map returnQuantityMap =
//                                    salesReturnService.getSalesQuantityAndReturnQuantity(salesInvoice.getId());
//                            Double returnQuantity = Double.parseDouble(returnQuantityMap.get("return_quantity").toString());
//                            Double saleQuantity = Double.parseDouble(returnQuantityMap.get("sales_quantity").toString());
//                            returnAmounts = (salesInvoice.getInvoiceDiscount()/saleQuantity) * returnQuantity;*/
//                            BigDecimal invoiceDiscount = new BigDecimal(salesInvoice.getInvoiceDiscount());
//                            BigDecimal invoiceAmount = new BigDecimal(salesInvoice.getInvoiceAmount()).
//                                    add(invoiceDiscount);
//
//                            returnAmounts =  (invoiceDiscount.multiply(
//                                    new BigDecimal(ledgerTransaction.get("credit").toString())).
//                                    divide(invoiceAmount)).setScale(4, RoundingMode.HALF_UP);
//                        }
//                    }

                    BigDecimal debit = new BigDecimal(ledgerTransaction.get("debit").toString());
                    BigDecimal credit = new BigDecimal(ledgerTransaction.get("credit").toString());
                    ledger = new HashMap<>();
                    ledger.put("transactionType", ledgerTransaction.get("transactionType"));
                    ledger.put("transactionNumber", ledgerTransaction.get("transactionNumber"));
                    ledger.put("date", ledgerTransaction.get("date"));
                    ledger.put("year_and_month", ledgerTransaction.get("year_and_month"));
                    ledger.put("description", ledgerTransaction.get("description"));
                    ledger.put("debit", debit);
//                    if (returnAmounts.compareTo(BigDecimal.ZERO) > 0) {
//                        openingBalance = openingBalance.add(debit.setScale(4, RoundingMode.HALF_UP))
//                                .subtract(credit).subtract(returnAmounts.negate()).setScale(4, RoundingMode.HALF_UP);
//                        BigDecimal creditReturn = new BigDecimal(ledgerTransaction.get("credit").toString())
//                                .subtract(returnAmounts);
//                        ledger.put("credit", creditReturn);
//                    }
//                    else {
                        openingBalance = openingBalance.add(debit.setScale(4, RoundingMode.HALF_UP))
                                .subtract(credit.setScale(4, RoundingMode.HALF_UP));
                        ledger.put("credit", credit);
                   // }
                    ledger.put("balance", openingBalance.setScale(2, RoundingMode.HALF_UP));
                    ledgerDetails.add(ledger);
                }
            }
            return ledgerDetails;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map<String, Object>> getDistributorListOfCompany(Long companyId, String searchString,  Long distributorId) {
        try {
            return distributorRepository.findDistributorListOfCompany(companyId, searchString, distributorId);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Float getDistributorLedgerBalance(Long companyId, Long distributorId) {
        try {
            Float balance = distributorRepository.getDistributorLedgerBalanceByCompanyAndDistributor(companyId, distributorId);

            return balance;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public float getDistributorAdvanceBalance(Long companyId, Long distributorId, LocalDateTime invoiceDate) {
        try {
            float balance = 0.0f;
            Map distributorCollectionInfo =
                    distributorRepository.getDistributorAdvanceBalance(companyId, distributorId, invoiceDate);
            //getDistributorLedgerBalance(companyId, distributorId);

            if (distributorCollectionInfo.size() >0) {
                balance = Float.valueOf(distributorCollectionInfo.get("remaining_advance_amount").toString());
            }
            return balance;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Long> getDistributorListOfSo(List<Long> salesOfficerList, Long companyId) {
        List<Map<String, Object>> distributorList =
                distributorRepository.findDistributorListOfSo(salesOfficerList, companyId);

        List<Long> distributorIds =
                distributorList.stream().map(s ->
                        Long.parseLong(s.get("id").toString())).collect(Collectors.toList());

        return distributorIds;
    }

    public List<Map<String, Object>> getDistributorListByCompanyAndSalesOfficerWise(Long companyId, List<Long> salesOfficerIds) {
        try {
            return distributorRepository.getDistributorListByCompanyAndSalesOfficerWise(companyId, salesOfficerIds);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Map<String, Object> findSoAssignedStatusDistributor(
            Long distributorId, Long companyId) {
        try {
            return distributorRepository.findSoAssignedStatusDistributor(distributorId, companyId);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Distributor updateDistributorStatus(Long id, Object object) {
        DistributorDto distributorDto = (DistributorDto) object;
        Distributor distributor = this.findDistributorById(id);

        distributor.setIsActive(distributorDto.getIsActive());

        distributorRepository.save(distributor);

        return distributor;
    }

    public Distributor findDistributorById(Long id) {
        try {
            Optional<Distributor> optionalDistributor = distributorRepository.findById(id);
            if (!optionalDistributor.isPresent()) {
                throw new Exception("Distributor does not exist");
            }
            return optionalDistributor.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map<String, Object>> getDistributorListByCompany(
            Long companyId) {
        return distributorRepository.
                getDistributorListByCompanyId(companyId);
    }

    public List<Map<String, Object>> findLedgerSummary(
            Long companyId, List<Long> distributorIds,
            List<Long> salesOfficerIds, List<Long> locationIds,
            LocalDate startDate, LocalDate endDate) {

        try {
            if (startDate == null) {
                startDate = LocalDate.now();
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }
            LocalDate openingBalanceDate = startDate.minusDays(1);
            List<Map<String, Object>> distributorSummaryBalanceList =
                    distributorRepository.getDistributorsLedgerSummary(
                            salesOfficerIds, distributorIds, locationIds, companyId, startDate, endDate,
                            openingBalanceDate);

            return distributorSummaryBalanceList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Optional<Distributor> findByName(Organization organization, String distributorName) {
       return distributorRepository.findByOrganizationAndDistributorNameIgnoreCaseAndIsDeletedFalse(
               organization, distributorName.trim());
    }

    public Long findByNameAndCompany(Organization organization, String distributorName) {
        return distributorRepository.findByNameAndCompany(
                distributorName.trim(), organization.getId());
    }

    public List<Long> findByCode(String distributorCode) {
        return distributorRepository.findByCode(
                distributorCode.trim());
    }
}
