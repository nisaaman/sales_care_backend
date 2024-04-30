package com.newgen.ntlsnc.usermanagement.service;

import com.newgen.ntlsnc.common.FileDownloadService;
import com.newgen.ntlsnc.common.FileUploadService;
import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.FileType;
import com.newgen.ntlsnc.common.enums.UserType;
import com.newgen.ntlsnc.globalsettings.entity.Document;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.repository.DocumentRepository;
import com.newgen.ntlsnc.globalsettings.service.*;
import com.newgen.ntlsnc.notification.pushNotification.service.PushNotificationService;
import com.newgen.ntlsnc.security.auth.UserDetailsImpl;
import com.newgen.ntlsnc.usermanagement.dto.ApplicationUserDto;
import com.newgen.ntlsnc.usermanagement.entity.*;
import com.newgen.ntlsnc.usermanagement.repository.ApplicationUserRepository;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author kamal
 * @Date ১৮/৫/২২
 */

@Service
public class ApplicationUserService implements IService<ApplicationUser> {
    @Autowired
    ApplicationUserRepository applicationUserRepository;
    @Autowired
    DepartmentService departmentService;
    @Autowired
    DesignationService designationService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private ReportingManagerService reportingManagerService;
    @Autowired
    private ApplicationUserCompanyMappingService applicationUserCompanyMappingService;
    @Autowired
    private LocationManagerMapService locationManagerMapService;
    @Autowired
    private DepotService depotService;
    @Autowired
    FileUploadService fileUploadService;
    @Autowired
    DocumentService documentService;
    @Autowired
    FileDownloadService fileDownloadService;
    @Autowired
    DocumentRepository documentRepository;
    @Autowired
    ApplicationUserRoleMapService applicationUserRoleMapService;
    @Autowired
    RoleService roleService;

    private static final String APPLICATION_USER_IMAGE_UPLOAD_DIRECTORY = "application_user";
    private Logger logger = LoggerFactory.getLogger(ApplicationUserService.class);

    @Override
    public ApplicationUser create(Object object) {

        ApplicationUserDto applicationUserDto = (ApplicationUserDto) object;

        ApplicationUser applicationUser = new ApplicationUser();

        applicationUser.setName(applicationUserDto.getName());
        applicationUser.setEmail(applicationUserDto.getEmail());
        applicationUser.setPassword(passwordEncoder.encode(applicationUserDto.getPassword()));
        applicationUser.setMobile(applicationUserDto.getMobile());
        applicationUser.setReferenceNo(applicationUserDto.getReferenceNo());
        applicationUser.setIsActive(applicationUserDto.getIsActive());

        if (applicationUserDto.getDepartmentId() != null) {
            applicationUser.setDepartment(departmentService.findById(applicationUserDto.getDepartmentId()));
        }
        if (applicationUserDto.getDesignationId() != null) {
            applicationUser.setDesignation(designationService.findById(applicationUserDto.getDesignationId()));
        }

        Organization organization = organizationService.getOrganizationFromLoginUser();
        applicationUser.setOrganization(organization);

        if (!this.validate(applicationUser)) {
            return null;
        }

        applicationUserRepository.save(applicationUser);

        if (applicationUserDto.getProfileImage() != null) {

            String filePath = fileUploadService.fileUpload(applicationUserDto.getProfileImage(), FileType.PHOTO.getCode(),
                    APPLICATION_USER_IMAGE_UPLOAD_DIRECTORY,
                    organization.getId(), organization.getId());

            documentService.save(APPLICATION_USER_IMAGE_UPLOAD_DIRECTORY, filePath, applicationUser.getId(),
                    fileUploadService.getFileNameFromFilePath(filePath), FileType.PHOTO.getCode()
                    , organization, organization.getId(), applicationUserDto.getProfileImage().getSize());

        }
        return applicationUser;
    }

    @Override
    public ApplicationUser update(Long id, Object object) {

        ApplicationUserDto applicationUserDto = (ApplicationUserDto) object;
        ApplicationUser applicationUser = this.getUserById(applicationUserDto.getId());

        applicationUser.setName(applicationUserDto.getName());
        applicationUser.setPassword(passwordEncoder.encode(applicationUserDto.getPassword()));
        applicationUser.setMobile(applicationUserDto.getMobile());
        applicationUser.setReferenceNo(applicationUserDto.getReferenceNo());
        applicationUser.setIsActive(applicationUserDto.getIsActive());

        if (applicationUserDto.getDepartmentId() != null) {
            applicationUser.setDepartment(departmentService.findById(applicationUserDto.getDepartmentId()));
        }
        if (applicationUserDto.getDesignationId() != null) {
            applicationUser.setDesignation(designationService.findById(applicationUserDto.getDesignationId()));
        }

        Organization organization = organizationService.getOrganizationFromLoginUser();
        applicationUser.setOrganization(organization);

        if (!this.validate(applicationUser)) {
            return null;
        }
        applicationUserRepository.save(applicationUser);

        if (applicationUserDto.getProfileImage() != null) {

            Document document = documentService.getDocumentInfoByRefIdAndRefTable(applicationUser.getId(), APPLICATION_USER_IMAGE_UPLOAD_DIRECTORY);

            if (document != null) {
                document.setIsDeleted(true);
                documentRepository.save(document);
            }

            String filePath = fileUploadService.fileUpload(applicationUserDto.getProfileImage(), FileType.PHOTO.getCode(),
                    APPLICATION_USER_IMAGE_UPLOAD_DIRECTORY,
                    organization.getId(), organization.getId());

            documentService.save(APPLICATION_USER_IMAGE_UPLOAD_DIRECTORY, filePath, applicationUser.getId(),
                    fileUploadService.getFileNameFromFilePath(filePath), FileType.PHOTO.getCode()
                    , organization, organization.getId(), applicationUserDto.getProfileImage().getSize());

        }

        return applicationUser;

    }

    @Override
    public boolean delete(Long id) {

        try {
            Optional<ApplicationUser> optionalApplicationUser = applicationUserRepository.findById(id);
            if (!optionalApplicationUser.isPresent()) {
                throw new Exception("Application User Not exist");
            }

            List<ApplicationUserCompanyMapping> applicationUserCompanyMappingList = applicationUserCompanyMappingService.findAllApplicationUserCompanyMappingByApplicationUser(optionalApplicationUser.get().getOrganization(), optionalApplicationUser.get());
            if (applicationUserCompanyMappingList.size() > 0) {
                throw new RuntimeException("This User already in use.");
            }


            ApplicationUser applicationUser = optionalApplicationUser.get();
            applicationUser.setIsDeleted(true);
            applicationUserRepository.save(applicationUser);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    public ApplicationUser findById(Long id) {

        try {
            Optional<ApplicationUser> optionalApplicationUser = applicationUserRepository.findById(id);
            if (!optionalApplicationUser.isPresent()) {
                throw new Exception("Application User Not exist with id " + id);
            }
            return optionalApplicationUser.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    public List<ApplicationUser> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        //return applicationUserRepository.findAllByOrganizationAndIsDeletedFalse(organization);

        //New code -> Filtered by user role
        ApplicationUser loginUser = this.getApplicationUserFromLoginUser();
        List<ApplicationUserRoleMap> applicationUserRoleMapList =
                applicationUserRoleMapService.findAllByApplicationUser(loginUser);
        Set<String> roleSet = applicationUserRoleMapList.stream().map(it->it.getRole().getName())
                .collect(Collectors.toSet());

        if(roleSet.contains(UserType.ROLE_SUPER_ADMIN.getCode())){
            return applicationUserRepository.findAllByOrganizationAndIsDeletedFalse(organization);
        }else if(roleSet.contains(UserType.ROLE_ADMIN.getCode())){
            Role role = roleService.findByRoleName(UserType.ROLE_SUPER_ADMIN.getCode());
            List<ApplicationUser> applicationUsers = applicationUserRoleMapService.findAllByRole(role.getId()).stream()
                .map(ApplicationUserRoleMap::getApplicationUser).collect(Collectors.toList());

            return applicationUserRepository.findAllByOrganizationAndIsDeletedFalse(organization).stream()
                .filter(it-> !applicationUsers.contains(it)).collect(Collectors.toList());
        }else {
            //Get all mapped company by login user
            List<ApplicationUserCompanyMapping> applicationUserCompanyMappingList1 =
                applicationUserCompanyMappingService.findAllApplicationUserCompanyMappingByApplicationUser(organization,
                    this.getApplicationUserFromLoginUser());

            //Get all mapped companies user
            List<ApplicationUserCompanyMapping> applicationUserCompanyMappingList2 =
                applicationUserCompanyMappingService.findAllApplicationUserCompanyMappingByCompany(organization,
                    applicationUserCompanyMappingList1.stream().map(ApplicationUserCompanyMapping::getCompany)
                .collect(Collectors.toList()));

            return applicationUserCompanyMappingList2.stream().map(ApplicationUserCompanyMapping::getApplicationUser)
                .collect(Collectors.toList());
        }
    }

    public List<ApplicationUser> findAllActiveUser() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return applicationUserRepository.findAllByOrganizationAndIsDeletedFalseAndIsActiveTrue(organization);
    }

    @Override
    public boolean validate(Object object) {
        ApplicationUser applicationUser = (ApplicationUser) object;
        Optional<ApplicationUser> optionalApplicationUser = Optional.empty();
        Optional<ApplicationUser> optionalApplicationUserEmail = Optional.empty();
        Optional<ApplicationUser> optionalApplicationUserContactNo = Optional.empty();

        if (applicationUser.getId() == null) {
            optionalApplicationUser = applicationUserRepository.findByOrganizationAndNameIgnoreCaseAndIsDeletedFalse(
                    applicationUser.getOrganization(), applicationUser.getName().trim());

            optionalApplicationUserEmail = applicationUserRepository.findByOrganizationAndEmailIgnoreCase(
                    applicationUser.getOrganization(), applicationUser.getEmail().trim());

            optionalApplicationUserContactNo = applicationUserRepository.findByOrganizationAndMobile(
                    applicationUser.getOrganization(), applicationUser.getMobile().trim());
        } else {
            optionalApplicationUser = applicationUserRepository.findByOrganizationAndIdIsNotAndNameIgnoreCaseAndIsDeletedFalse(
                    applicationUser.getOrganization(), applicationUser.getId(), applicationUser.getName().trim());

            optionalApplicationUserEmail = applicationUserRepository.findByOrganizationAndIdIsNotAndEmailIgnoreCase(
                    applicationUser.getOrganization(), applicationUser.getId(), applicationUser.getEmail().trim());

            optionalApplicationUserContactNo = applicationUserRepository.findByOrganizationAndIdIsNotAndMobile(
                    applicationUser.getOrganization(), applicationUser.getId(), applicationUser.getMobile().trim());
        }

        /*if (optionalApplicationUser.isPresent()) {
            throw new RuntimeException("User name already exist.");
        }*/

        if (optionalApplicationUserEmail.isPresent()) {
            throw new RuntimeException("User email already exist.");
        }

        if (optionalApplicationUserContactNo.isPresent()) {
            throw new RuntimeException("User contact number already exist.");
        }

        return true;
    }

    public ApplicationUser getUserById(Long id) {
        try {
            Optional<ApplicationUser> optionalApplicationUser = applicationUserRepository.findById(id);
            if (!optionalApplicationUser.isPresent()) {
                throw new Exception("User does not exist");
            }
            return optionalApplicationUser.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Long> getSoListByLocationManager(Long managerId) {
        try {
            List<Long> soList = applicationUserRepository.getSoListByLocationManager(managerId);
            return soList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Boolean checkLoginUserIsSo(Long companyId, Long userLoginId) {
        Long reportingTo =
                reportingManagerService.getReportingTo(companyId, userLoginId);
        List<Long> downLayer =
                reportingManagerService.getManagerHierarchyDown(companyId, userLoginId);
        if (null != reportingTo && downLayer.size() == 0) {
            return true;
        } else return false;
    }

    public Boolean checkLoginUserIsManager(Long companyId, Long userLoginId) {
        Long applicationUser =
                reportingManagerService.checkLoginUserIsManager(companyId, userLoginId);
        if (null != applicationUser) {
            return true;
        } else return false;
    }

    public Boolean checkLoginUserIsDepotManager(Long companyId, Long userLoginId) {
        Map depotMap =
                depotService.getDepotByLoginUserId(companyId, userLoginId);
        if (depotMap.size() != 0) {
            return true;
        } else
            return false;
    }

    public ApplicationUser getApplicationUserFromLoginUser() {
        UserDetailsImpl userDetailsImpl = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ((applicationUserRepository.findById(userDetailsImpl.getId()).get()));
    }

    public Set<Permission> getPermissionsFromLoginUser() {
        UserDetailsImpl userDetailsImpl = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetailsImpl.getPermissions();
    }

    public Map<String, Object> getMe() {
        Map<String, Object> session = new HashMap<>();

        try {
            ApplicationUser applicationUser = getApplicationUserFromLoginUser();

            List<Map<String, Object>> organizationList = organizationService.getAllCompanyByLoginUser();

            session.put("userId", applicationUser.getId());
            session.put("userName", applicationUser.getName());
            session.put("referenceNo", applicationUser.getReferenceNo());
            session.put("mobileNo", applicationUser.getMobile());
            session.put("email", applicationUser.getEmail());
            session.put("designation", applicationUser.getDesignation());
            session.put("department", applicationUser.getDepartment());
            session.put("parentCompany", applicationUser.getOrganization());
            session.put("companies", organizationList);
            session.put("permissions", getPermissionsFromLoginUser());

            return session;

        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public List<Map<String, Object>> getSoDetailsByLocation(Long locationId, Long companyId) {

        try {
            return applicationUserRepository.getSoDetailsByLocationManager(locationId, companyId);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Map<String, Object> getSoDetails(Long companyId) {
        ApplicationUser applicationUser = getApplicationUserFromLoginUser();
        Map<String, Object> soDetails = new HashMap<>();
        soDetails.put("userId", applicationUser.getId());
        soDetails.put("name", applicationUser.getName());
        soDetails.put("referenceNo", applicationUser.getReferenceNo());
        soDetails.put("mobileNo", applicationUser.getMobile());
        soDetails.put("email", applicationUser.getEmail());
        soDetails.put("designationName", applicationUser.getDesignation().getName());
        soDetails.put("departmentName", applicationUser.getDepartment().getName());
        soDetails.put("parentCompanyName", applicationUser.getOrganization().getName());
        soDetails.put("companies", applicationUserCompanyMappingService.getAllCompanyListByUser(applicationUser.getOrganization(), applicationUser));
        soDetails.put("permissions", getPermissionsFromLoginUser());
        if (companyId != null) {
            soDetails.putAll(locationManagerMapService.getSalesOfficerLocation(applicationUser.getId(), applicationUser.getOrganization().getId(), companyId));
        }
        return soDetails;
    }

    public Long getApplicationUserIdFromLoginUser() {
        UserDetailsImpl userDetailsImpl = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetailsImpl.getId();
    }

    public Long getOrganizationIdFromLoginUser() {
        return this.getApplicationUserFromLoginUser().getOrganization().getId();
    }

    public List<ApplicationUser> findAllApplicationUserByDepartment(Long departmentId) {
        return applicationUserRepository.findByDepartmentIdAndIsDeletedFalse(departmentId);
    }

    public ApplicationUserDto getApplicationUserDetailsWithProfileImage(Long id) {

        ApplicationUserDto applicationUserDto = new ApplicationUserDto();

        Optional<ApplicationUser> applicationUserOptional = applicationUserRepository.findByIdAndIsDeletedFalse(id);

        ApplicationUser applicationUser = applicationUserOptional.get();

        applicationUserDto.setId(applicationUser.getId());
        applicationUserDto.setName(applicationUser.getName());
        applicationUserDto.setEmail(applicationUser.getEmail());
        applicationUserDto.setMobile(applicationUser.getMobile());
        applicationUserDto.setDepartmentId(applicationUser.getDepartment().getId());
        applicationUserDto.setDesignationId(applicationUser.getDesignation().getId());
        applicationUserDto.setReferenceNo(applicationUser.getReferenceNo());
        applicationUserDto.setIsActive(applicationUser.getIsActive());

        String filePath = documentService.getDocumentFilePath(APPLICATION_USER_IMAGE_UPLOAD_DIRECTORY, applicationUser.getId(), FileType.PHOTO);

        if ((!filePath.equals("")) && filePath != null) {

            String s = new String(Base64.encodeBase64(fileDownloadService.fileDownload(
                    documentService.getFilePath(applicationUser.getId(), APPLICATION_USER_IMAGE_UPLOAD_DIRECTORY))));
            applicationUserDto.setFilePath(s);
        }

        return applicationUserDto;
    }

    public ApplicationUser updateUserStatus(Long id, Object object) {

        ApplicationUserDto applicationUserDto = (ApplicationUserDto) object;
        ApplicationUser applicationUser = this.getUserById(applicationUserDto.getId());

        applicationUser.setIsActive(applicationUserDto.getIsActive());

        applicationUserRepository.save(applicationUser);

        return applicationUser;

    }


    @Transactional
    public List<ApplicationUser> findAllByCompany(Long companyId) {
        return applicationUserRepository.getByCompany(companyId);
    }

    public List<ApplicationUser> findAllApplicationUserByDesignation(Long designationId) {
        return applicationUserRepository.findAllByDesignationIdAndIsDeletedFalse(designationId);
    }

    public List<ApplicationUser> findAllActiveApplicationUser(Long designationId) {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        List<ApplicationUser> applicationUserList = applicationUserRepository.findAllByOrganizationAndDesignationIdAndIsDeletedFalseAndIsActiveTrue(organization, designationId);
        return applicationUserList;
    }

    public List<ApplicationUser> findAllActiveApplicationUserByDepartment(Long departmentId) {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        List<ApplicationUser> applicationUserList = applicationUserRepository.findAllByOrganizationAndDepartmentIdAndIsDeletedFalseAndIsActiveTrue(organization, departmentId);
        return applicationUserList;
    }

    public List<ApplicationUser> getByCompanyWithoutZonalManager(Long companyId) {
        return applicationUserRepository.getByCompanyWithoutZonalManager(companyId);
    }

    public List<Map<String, Object>> getSearchUser(String searchString) {
        try {
            return applicationUserRepository.searchUser(searchString);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map<String, Object>> getUserListByCompanyWise(List<Long> companyIds) {
        return applicationUserCompanyMappingService.getUserListByCompanyWise(companyIds);
    }

    public List<Map<String, Object>>  getSalesOfficerByLocation(Long locationId, Long companyId) {
        Long applicationUserId = getApplicationUserIdFromLoginUser();
        List<Long> soIds = locationManagerMapService.getSalesOfficerListFromUserLoginIdOrLocationId(applicationUserId, locationId, companyId);
        return applicationUserRepository.findAllSO(soIds);
    }

    public List<Map<String, Object>> getSalesOfficerByLocationWise(Long companyId, List<Long> locationIds) {
        return applicationUserRepository.getSalesOfficerByLocationWise(companyId,locationIds, locationIds.size());
    }

    public void addUserFCMId(Map fcmKey) {
        Long applicationUserId = getApplicationUserIdFromLoginUser();
        ApplicationUser applicationUser = this.getUserById(applicationUserId);
        applicationUser.setFcmId(String.valueOf(fcmKey.get("fcmId")));
        applicationUserRepository.save(applicationUser);

    }

    public String getUserFCMId(Long applicationUserId) {
        try {
            return this.getUserById(applicationUserId).getFcmId();
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}
