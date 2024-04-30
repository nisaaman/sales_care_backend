package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.FileDownloadService;
import com.newgen.ntlsnc.common.FileUploadService;
import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.FileType;
import com.newgen.ntlsnc.globalsettings.dto.OrganizationDto;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.repository.OrganizationRepository;
import com.newgen.ntlsnc.security.auth.UserDetailsImpl;
import com.newgen.ntlsnc.subscriptions.entity.SubscriptionPackage;
import com.newgen.ntlsnc.subscriptions.service.SubscriptionPackageService;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUserCompanyMapping;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserCompanyMappingService;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserService;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrganizationService implements IService<Organization> {

    @Autowired
    OrganizationRepository organizationRepository;

    @Autowired
    SubscriptionPackageService subscriptionPackageService;
    @Autowired
    FileStorageService fileStorageService;
    @Autowired
    DocumentService documentService;
    @Autowired
    ApplicationUserCompanyMappingService applicationUserCompanyMappingService;
    @Lazy
    @Autowired
    LocationTreeService locationTreeService;
    @Autowired
    ApplicationUserService applicationUserService;
    @Autowired
    FileDownloadService fileDownloadService;
    @Autowired
    FileUploadService fileUploadService;

    private static final String CLASS_NAME = "organization";
    private static final String DUPLICATE_NAME = " name can not be duplicate";
    private static final String COMPANY_LOGO_UPLOAD_DIRECTORY = "organization";
    private static final String COMPANY_LOGO_UPLOAD_PREFIX = "organization_";
    private static final String UNDERSCORE = "_";

    @Override
    public boolean validate(Object object) {
        Organization organization = (Organization) object;
        boolean optionalOrganizationEmail = false;
        boolean optionalOrganizationWebAddress = false;

        if (organization.getId() == null) {
            optionalOrganizationEmail =
                    organizationRepository.existsByEmail(
                            organization.getEmail().trim());

            if (organization.getWebAddress() != null){
                if (!organization.getWebAddress().trim().equals("")){
                    optionalOrganizationWebAddress =
                            organizationRepository.existsByWebAddressIgnoreCase(
                                    organization.getWebAddress().trim());
                }

            }

        }
        if (optionalOrganizationEmail) {
            throw new RuntimeException("Email already exist.");
        }
        if (optionalOrganizationWebAddress) {
            throw new RuntimeException("Website already exist.");
        }

        return true;
    }

    @Override
    @Transactional
    public Organization create(Object object) {
        try {
            OrganizationDto organizationDto = (OrganizationDto) object;
            Organization organization = new Organization();
            SubscriptionPackage subscriptionPackage = (organizationDto.getSubscriptionPackageId() == null) ? null :
                    subscriptionPackageService.findById(organizationDto.getSubscriptionPackageId());
            Organization parent = (organizationDto.getParentId() == null) ? null :
                    this.findById(organizationDto.getParentId());

            Optional<Organization> organizationNameOpt = organizationRepository.findByNameIgnoreCaseAndIsDeletedFalse(organizationDto.getName());
            if (organizationNameOpt.isPresent() && organizationNameOpt.get().getId() != organization.getId()) {
                throw new Exception(CLASS_NAME + DUPLICATE_NAME);
            }
            organization.setName(organizationDto.getName().trim());
            organization.setAddress(organizationDto.getAddress());
            Optional<Organization> organizationShortNameOpt = organizationRepository.findByShortNameIgnoreCaseAndIsDeletedFalse(organizationDto.getShortName());
            if (organizationShortNameOpt.isPresent() && organizationShortNameOpt.get().getId() != organization.getId()) {
                throw new Exception(CLASS_NAME + " short" + DUPLICATE_NAME);
            }
            organization.setShortName(organizationDto.getShortName().trim());
            organization.setEmail(organizationDto.getEmail().trim());
            organization.setWebAddress(organizationDto.getWebAddress());
            organization.setContactNumber(organizationDto.getContactNumber());
            organization.setContactPerson(organizationDto.getContactPerson().trim());
            organization.setRemarks(organizationDto.getRemarks());
            organization.setParent(parent);
            organization.setSubscriptionPackage(subscriptionPackage);

            if (organizationDto.getIsNewLocation()) { //if new location tree is created
                organization.setLocationTree(locationTreeService.create(organizationDto.getLocationTreeDto()));
            } else {  // existing location tree select from dropdown
                organization.setLocationTree(locationTreeService.findById(organizationDto.getLocationTreeId()));
            }
            if (!this.validate(organization)) {
                return null;
            }
            organizationRepository.save(organization);

            //Organization Logo upload
//            if(organizationDto.getFiles() !=null){
//
//                MultipartFile file = organizationDto.getFiles();
//                String filePath = COMPANY_LOGO_UPLOAD_DIRECTORY;
//                String prefix = COMPANY_LOGO_UPLOAD_PREFIX;
//                String fileName = prefix + DateUtil.getTimestampFromUtilDate(new Date()).toString()
//                        + UNDERSCORE + file.getOriginalFilename();
//                String uploadPath = filePath + File.separator + fileName;
//                documentService.save(COMPANY_LOGO_UPLOAD_DIRECTORY,uploadPath , organization.getId()
//                        ,file.getOriginalFilename() , FileType.LOGO.getCode(), parent, organization.getId(),file.getSize());
//
//                //Saving file
//                fileStorageService.upload(file, filePath, fileName);
//            }
            if (organizationDto.getFiles() != null) {

                String filePath = fileUploadService.fileUpload(organizationDto.getFiles(), FileType.LOGO.getCode(),
                        COMPANY_LOGO_UPLOAD_DIRECTORY,
                        organization.getId(), organization.getId());

                documentService.save(COMPANY_LOGO_UPLOAD_DIRECTORY, filePath, organization.getId(),
                        fileUploadService.getFileNameFromFilePath(filePath), FileType.LOGO.getCode()
                        , organization, organization.getId(),organizationDto.getFiles().getSize());
            }

            return organization;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    @Transactional
    public Organization update(Long id, Object object) {
        try {
            OrganizationDto organizationDto = (OrganizationDto) object;
            Organization organization = organizationRepository.findById(organizationDto.getId()).get();
            Optional<Organization> optionalOrganizationEmail =
                    organizationRepository.findByEmailAndIdIsNot(organizationDto.getEmail(), id);

            if (optionalOrganizationEmail.isPresent()) {
                throw new RuntimeException("Email already exist.");
            }
            if (organizationDto.getWebAddress() != null){
                if (!organizationDto.getWebAddress().trim().equals("")){
                    Optional<Organization> optionalOrganizationWebAddress =
                            organizationRepository.findByWebAddressAndIdIsNot(
                                    organizationDto.getWebAddress().trim(), id);
                    if (optionalOrganizationWebAddress.isPresent()) {
                        throw new RuntimeException("Website already exist.");
                    }
                }

            }

            SubscriptionPackage subscriptionPackage = (organizationDto.getSubscriptionPackageId() == null) ? null :
                    subscriptionPackageService.findById(organizationDto.getSubscriptionPackageId());
            Organization parent = this.getOrganizationFromLoginUser();

            Optional<Organization> organizationNameOpt = organizationRepository.findByNameIgnoreCaseAndIsDeletedFalse(organizationDto.getName());
            if (organizationNameOpt.isPresent() && organizationNameOpt.get().getId() != organization.getId()) {
                throw new Exception(CLASS_NAME + DUPLICATE_NAME);
            }
            organization.setName(organizationDto.getName().trim());
            organization.setAddress(organizationDto.getAddress());
            Optional<Organization> organizationShortNameOpt = organizationRepository.findByShortNameIgnoreCaseAndIsDeletedFalse(organizationDto.getShortName());
            if (organizationShortNameOpt.isPresent() && organizationShortNameOpt.get().getId() != organization.getId()) {
                throw new Exception(CLASS_NAME + " short" + DUPLICATE_NAME);
            }
            organization.setShortName(organizationDto.getShortName().trim());
            organization.setEmail(organizationDto.getEmail().trim());
            organization.setWebAddress(organizationDto.getWebAddress());
            organization.setContactNumber(organizationDto.getContactNumber());
            organization.setContactPerson(organizationDto.getContactPerson().trim());
            organization.setRemarks(organizationDto.getRemarks());
            organization.setSubscriptionPackage(subscriptionPackage);

            if (organizationDto.getIsNewLocation()) { //if new location tree is created
                organization.setLocationTree(locationTreeService.create(organizationDto.getLocationTreeDto()));
            } else if (organization.getLocationTree() != null
                    && !organizationDto.getLocationTreeId().equals(organization.getLocationTree().getId())) {  // existing location tree select from dropdown
                organization.setLocationTree(locationTreeService.findById(organizationDto.getLocationTreeId()));
            }

            try {
                organizationRepository.save(organization);
            } catch (DataIntegrityViolationException ex) {
                throw new RuntimeException("Email or Website already exist.");
            }
            //Organization Logo upload
            if (organizationDto.getFiles() != null) {
                if (organizationDto.getDocumentId() != null) {
                    documentService.delete(organizationDto.getDocumentId());
                }
//                MultipartFile file = organizationDto.getFiles();
//                String filePath = COMPANY_LOGO_UPLOAD_DIRECTORY;
//                String prefix = COMPANY_LOGO_UPLOAD_PREFIX;
//                String fileName = prefix + DateUtil.getTimestampFromUtilDate(new Date()).toString()
//                        + UNDERSCORE + file.getOriginalFilename();
//                String uploadPath = filePath + File.separator + fileName;
//                documentService.save(COMPANY_LOGO_UPLOAD_DIRECTORY, uploadPath, organization.getId(),
//                        file.getOriginalFilename(), FileType.LOGO.getCode(), parent, organization.getId(),file.getSize());
//
//                //Saving file
//                fileStorageService.upload(file, filePath, fileName);

                String filePath = fileUploadService.fileUpload(organizationDto.getFiles(), FileType.LOGO.getCode(),
                        COMPANY_LOGO_UPLOAD_DIRECTORY,
                        organization.getId(), organization.getId());

//                documentService.save(COMPANY_LOGO_UPLOAD_DIRECTORY, filePath, organization.getId(),
//                        fileUploadService.getFileNameFromFilePath(filePath), FileType.LOGO.getCode()
//                        , organization, organization.getId(),organizationDto.getFiles().getSize());
            }

            return organization;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        Organization organization = this.findById(id);
        if (organization == null)
            return false;

        organization.setIsDeleted(true);
        organizationRepository.save(organization);

        return true;
    }

    @Override
    public Organization findById(Long id) {
        try {
            Optional<Organization> optionalOrganization = organizationRepository.findById(id);
            if (!optionalOrganization.isPresent()) {
                throw new Exception("Organization not exist");
            }
            return optionalOrganization.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Organization findByIdAndIsDeletedFalseAndActiveTrue(Long organizationId) {

        try {
            Optional<Organization> optionalOrganization = organizationRepository.
                    findByIdAndIsDeletedFalseAndIsActiveTrue(organizationId);
            if (optionalOrganization.isPresent()) {
                return optionalOrganization.get();
            }

        } catch (Exception e) {
           return new Organization();
        }
        return new Organization();
    }

    public ResponseEntity<?> getByIdWithLogo(Long id) {
        return ResponseEntity.ok(getOrganizationLogo(id));
    }

    public Map getOrganizationLogo(Long id) {
        try {
            Map map = new HashMap();
            Map<String, Object> organization = organizationRepository.findByIdWithLogo(id);
            map.put("organization", organization);

            if (organization.get("file_path") != null) {
                byte[] bytes = fileDownloadService.fileDownload(organization.get("file_path").toString());
                String imageString = new String(Base64.encodeBase64(bytes), "UTF-8");
                map.put("imageString", imageString);
            }
            return map;
        } catch (Exception ex) {
            return new HashMap();
        }
    }

    public byte[] getOrganizationLogoByteData(Long id) {
        try {

            Map<String, Object> organization = organizationRepository.findByIdWithLogo(id);

            if (organization.get("file_path") != null) {
                return fileDownloadService.fileDownload(organization.get("file_path").toString());

            }

        } catch (Exception ex) {
            return new byte[]{};
        }
        return new byte[]{};
    }

    @Override
    public List<Organization> findAll() {
        return organizationRepository.findAllByIsDeletedFalseAndIsActiveTrue();
    }

    public List<Organization> findAllCompanyByLoginUserOrganization() {
        ApplicationUser applicationUser = applicationUserService.getApplicationUserFromLoginUser();
        return findAllUserCompanyMappingByOrganization(applicationUser.getOrganization().getId());
    }

    public List<Organization> findAllUserCompanyMappingByOrganization(Long organizationId) {
        return organizationRepository.findAllByIsDeletedFalseAndIsActiveTrueAndParentId(organizationId);
    }

    @Transactional
    public Organization updateActiveStatus(Long id) {
        Optional<Organization> optionalOrganization = organizationRepository.findById(id);

        try {
            if (!optionalOrganization.isPresent()) {
                throw new Exception("Company does not exist!");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        Organization organization = optionalOrganization.get();
        organization.setIsActive(!organization.getIsActive());
        return organizationRepository.save(organization);
    }

    public List<Map<String, Object>> getAllWithLogo() {
        try {
            Organization organization = this.getOrganizationFromLoginUser();
            List<Map<String, Object>> organizationList = organizationRepository.findAllWithLogo(organization != null ? organization.getId() : 0);
            List<Map<String, Object>> organizationListModified = new ArrayList<>();
            organizationList.parallelStream()
                            .forEach(org -> {
                                Map<String, Object> map = new HashMap<>(org);
                                if (map.get("file_path") != null) {
                                    map.put("imageString", new String(Base64.encodeBase64(
                                            fileDownloadService.fileDownload(map.get("file_path").toString()))));
                                }
                                organizationListModified.add(map);
                            });

//            long startOldFor = System.nanoTime();
//            for(Map<String, Object> org : organizationList){
//                Map<String, Object> map = new HashMap<>(org);
//                if (map.get("file_path") != null) {
//                    long startTime = System.nanoTime();
//                    byte[] bytes = fileDownloadService.fileDownload(map.get("file_path").toString());
//                    long elapsedTime = System.nanoTime() - startTime;
//                    System.out.println("Total execution time to create 1000K objects in Java in millis: "
//                            + elapsedTime/1000000);
//
//                    String imageString = new String(Base64.encodeBase64(bytes), "UTF-8");
//                    map.put("imageString", imageString);
//                }
//                organizationListModified.add(map);
//            }
//            long elapsedTimeForOld = System.nanoTime() - startOldFor;
//
//            System.out.println("Total execution time in For in milli : " + elapsedTimeForOld/1000000);
            return organizationListModified;
            //return organizationRepository.findAllWithLogo(organization != null ? organization.getId() : 0);

        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public List<Map<String, Object>> getAllCompanyByLoginUser() {
        try {

            List<Map<String, Object>> organizationList = this.getAllWithLogo();

            List<ApplicationUserCompanyMapping> applicationUserCompanyMappingList =
                    applicationUserCompanyMappingService.findAllByUser();

            List<Map<String, Object>> organizationFilteredList = organizationList.stream()
                    .filter(it-> ((boolean) it.get("is_active")) && applicationUserCompanyMappingList.stream()
                            .map(m-> m.getCompany().getId().toString()).collect(Collectors.toList())
                            .contains(it.get("id").toString())).collect(Collectors.toList());

            return organizationFilteredList;

        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    /// convert file / image into base64 binary for sending to UI via json
    private static String encodeFileToBase64Binary(File file) throws Exception{
        FileInputStream fileInputStreamReader = new FileInputStream(file);
        byte[] bytes = new byte[(int)file.length()];
        fileInputStreamReader.read(bytes);
        return new String(Base64.encodeBase64(bytes), "UTF-8");
    }

    public List<Organization> getAllChildByParentId(Long parentId) {
        return organizationRepository.findAllByParentAndIsDeletedFalseAndIsActiveTrue(findById(parentId));
    }

    public Organization getOrganizationFromLoginUser(){
        UserDetailsImpl userDetailsImpl = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetailsImpl.getOrganization();
    }

    public Long getOrganizationIdFromLoginUser(){
        return getOrganizationFromLoginUser().getId();
    }

    public String getShortName(Long organizationId) {

        return findById(organizationId).getShortName();
    }

 }
