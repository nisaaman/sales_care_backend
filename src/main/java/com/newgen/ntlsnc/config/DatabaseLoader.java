package com.newgen.ntlsnc.config;

import com.newgen.ntlsnc.common.CommonConstant;
import com.newgen.ntlsnc.common.enums.ActivityFeature;
import com.newgen.ntlsnc.common.enums.SubscriptionDurationType;
import com.newgen.ntlsnc.globalsettings.controller.*;
import com.newgen.ntlsnc.globalsettings.entity.*;
import com.newgen.ntlsnc.globalsettings.repository.*;
import com.newgen.ntlsnc.multilayerapproval.controller.ApprovalStepController;
import com.newgen.ntlsnc.multilayerapproval.controller.ApprovalStepFeatureController;
import com.newgen.ntlsnc.multilayerapproval.controller.MultiLayerApprovalPathController;
import com.newgen.ntlsnc.reports.controller.CommonReportsController;
import com.newgen.ntlsnc.salesandcollection.configuration.StockOpningBalanceBatchConfiguration;
import com.newgen.ntlsnc.salesandcollection.controller.*;
import com.newgen.ntlsnc.salesandcollection.entity.DistributorType;
import com.newgen.ntlsnc.salesandcollection.repository.DistributorTypeRepository;
import com.newgen.ntlsnc.subscriptions.entity.SubscriptionPackage;
import com.newgen.ntlsnc.subscriptions.repository.SubscriptionPackageRepository;
import com.newgen.ntlsnc.supplychainmanagement.controller.*;
import com.newgen.ntlsnc.supplychainmanagement.entity.InterStoreStockMovement;
import com.newgen.ntlsnc.usermanagement.controller.*;
import com.newgen.ntlsnc.usermanagement.entity.*;
import com.newgen.ntlsnc.usermanagement.repository.*;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.newgen.ntlsnc.common.CommonConstant.ROLE_PREFIX;

/**
 * @author liton
 * Created on 4/19/22 11:23 AM
 */

@Component
public class DatabaseLoader implements CommandLineRunner {

    @Autowired
    private SubscriptionPackageRepository subscriptionPackageRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private DesignationRepository designationRepository;
    @Autowired
    private ApplicationUserRepository applicationUserRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    ApplicationUserRoleMapRepository applicationUserRoleMapRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    ApplicationUserCompanyMappingRepository applicationUserCompanyMappingRepository;

    @Autowired
    DocumentSequenceRepository documentSequenceRepository;

    @Autowired
    DistributorTypeRepository distributorTypeRepository;

    @Autowired
    InvoiceNatureRepository invoiceNatureRepository;

    @Autowired
    FeatureInfoRepository featureInfoRepository;

    private final Log logger = LogFactory.getLog(this.getClass());

    @Override
    public void run(String... args) throws Exception {
        if (applicationUserRepository.count() == 0)
            loadData();
    }

    private void loadData() {
        SubscriptionPackage subscriptionPackage = null;
        Organization organization = null;
//        Organization organization1 = null;
//        Organization organization2 = null;
        Department department = null;
        Designation designation = null;
        ApplicationUser applicationUser1 = null;
        ApplicationUser applicationUser2 = null;
        Role role1 = null;
        Role role2 = null;
        ApplicationUserRoleMap applicationUserRoleMap1 = null;
        ApplicationUserRoleMap applicationUserRoleMap2 = null;

        // Subscription Package saving
        if (subscriptionPackageRepository.count() == 0) {
            subscriptionPackage = new SubscriptionPackage();
            subscriptionPackage.setName("Free Trial");
            subscriptionPackage.setPrice(0.00);
            subscriptionPackage.setDuration(30f);
            subscriptionPackage.setDurationType(SubscriptionDurationType.DAY);
            subscriptionPackage.setNumberOfUnits(2);
            subscriptionPackage.setNumberOfUsers(10);
            subscriptionPackageRepository.save(subscriptionPackage);

            logger.info("SubscriptionPackage data saved.");
        }

        // Organization saving
        if (organizationRepository.count() == 0) {
            organization = new Organization();
            organization.setName("Babylon Group");
            organization.setAddress("2-B/1, Darussalam Road, Mirpur, Dhaka-1216, Bangladesh");
            organization.setShortName("BG");
            organization.setEmail("babylon@babylon-bd.com");
            organization.setWebAddress("https://www.babylongroup.com/");
            organization.setContactNumber("09609003300");
            organization.setContactPerson("Noman");
            organization.setRemarks("This is initial data.");
            organization.setParent(null);
            organization.setSubscriptionPackage(subscriptionPackage);
            organizationRepository.save(organization);

            /*organization1 = new Organization();
            organization1.setName("Newgen Technology Limited");
            organization1.setAddress("Lalmatia");
            organization1.setShortName("NTL");
            organization1.setEmail("hr@newgen-bd.com");
            organization1.setWebAddress("newgen-bd.com");
            organization1.setContactNumber("001-00001");
            organization1.setContactPerson("Noman");
            organization1.setRemarks("This is initial data.");
            organization1.setParent(organization);
            organization1.setSubscriptionPackage(subscriptionPackage);
            organizationRepository.save(organization1);*/
        }

        // Department saving
        if (departmentRepository.count() == 0) {
            department = new Department();
            department.setName("Admin");
            department.setDescription("Admin & HR");
            department.setOrganization(organization);
            departmentRepository.save(department);

            logger.info("Department data saved.");
        }

        // Designation saving
        if (designationRepository.count() == 0) {
            designation = new Designation();
            designation.setName("Admin");
            designation.setDescription("Local Admin");
            designation.setOrganization(organization);
            designationRepository.save(designation);

            logger.info("Designation data saved.");
        }

        // Application User saving
        if (applicationUserRepository.count() == 0) {
            applicationUser1 = new ApplicationUser();
            applicationUser1.setName("Sadmin");
            applicationUser1.setEmail("sadmin@test.com");
            applicationUser1.setPassword(passwordEncoder.encode("1234"));
            applicationUser1.setMobile("12341234567");
            applicationUser1.setDepartment(department);
            applicationUser1.setDesignation(designation);
            applicationUser1.setReferenceNo("abc123");
            applicationUser1.setOrganization(organization);
            applicationUserRepository.save(applicationUser1);

            applicationUser2 = new ApplicationUser();
            applicationUser2.setName("Admin");
            applicationUser2.setEmail("admin@test.com");
            applicationUser2.setPassword(passwordEncoder.encode("1234"));
            applicationUser2.setMobile("123412345671");
            applicationUser2.setDepartment(department);
            applicationUser2.setDesignation(designation);
            applicationUser2.setReferenceNo("abc1234");
            applicationUser2.setOrganization(organization);
            applicationUserRepository.save(applicationUser2);

            logger.info("Application user data saved.");
        }

        // Role saving
        if (roleRepository.count() == 0) {
            role1 = new Role();
            role1.setName(ROLE_PREFIX + "SUPER_ADMIN");
            role1.setDescription("Super Admin User");
            role1.setOrganization(organization);
            roleRepository.save(role1);

            role2 = new Role();
            role2.setName(ROLE_PREFIX + "ADMIN");
            role2.setDescription("Admin User");
            role2.setOrganization(organization);
            roleRepository.save(role2);

            logger.info("Role saved.");
        }

        // ApplicationUserRoleMap saving
        if (applicationUserRoleMapRepository.count() == 0) {
            applicationUserRoleMap1 = new ApplicationUserRoleMap();
            applicationUserRoleMap1.setApplicationUser(applicationUser1);
            applicationUserRoleMap1.setRole(role1);
            applicationUserRoleMap1.setOrganization(organization);
            applicationUserRoleMapRepository.save(applicationUserRoleMap1);

            applicationUserRoleMap2 = new ApplicationUserRoleMap();
            applicationUserRoleMap2.setApplicationUser(applicationUser2);
            applicationUserRoleMap2.setRole(role2);
            applicationUserRoleMap2.setOrganization(organization);
            applicationUserRoleMapRepository.save(applicationUserRoleMap2);

            logger.info("ApplicationUser role map saved.");
        }

        if (permissionRepository.count() == 0) {
            Permission permission = new Permission();
            permission.setRole(role1);
            permission.setOrganization(organization);
            permission.setActivityFeature(ActivityFeature.FEATURE_PERMISSION_SETUP);
            permission.setIsCreate(true);
            permission.setIsUpdate(true);
            permission.setIsDelete(true);
            permission.setIsView(true);
            permission.setIsAllView(true);
            permission.setOrganization(organization);
            permissionRepository.save(permission);
            logger.info("Permission save for role " + role1.getName());

            /*permission = new Permission();
            permission.setRole(role1);
            permission.setOrganization(organization);
            permission.setActivityFeature(ActivityFeature.USER_COMPANY_MAPPING);
            permission.setIsCreate(true);
            permission.setIsUpdate(true);
            permission.setIsDelete(true);
            permission.setIsView(true);
            permission.setIsAllView(true);
            permission.setOrganization(organization);
            permissionRepository.save(permission);
            logger.info("Permission save for role " + role2.getName());*/
        }

        if (applicationUserCompanyMappingRepository.count() == 0) {
            ApplicationUserCompanyMapping applicationUserCompanyMapping = new ApplicationUserCompanyMapping();
            applicationUserCompanyMapping.setOrganization(organization);
            applicationUserCompanyMapping.setApplicationUser(applicationUser1);
            applicationUserCompanyMapping.setCompany(organization);
            applicationUserCompanyMappingRepository.save(applicationUserCompanyMapping);
            logger.info("application User Company Mapping save for user " + applicationUser1.getName() +" and company "+ organization.getName());

            /*applicationUserCompanyMapping = new ApplicationUserCompanyMapping();
            applicationUserCompanyMapping.setOrganization(organization);
            applicationUserCompanyMapping.setApplicationUser(applicationUser2);
            applicationUserCompanyMapping.setCompany(organization1);
            applicationUserCompanyMappingRepository.save(applicationUserCompanyMapping);
            logger.info("Application User Company Mapping save for user " + applicationUser2.getName() +" and company "+ organization1.getName());*/
        }

        if(documentSequenceRepository.count() == 0){
            DocumentSequence documentSequence = new DocumentSequence();
            documentSequence.setDocumentId(CommonConstant.DOCUMENT_ID_FOR_DEPOT_ID);
            documentSequence.setDocumentName("Depot Setup");
            documentSequence.setDescription("Depot Setup");
            documentSequence.setSequenceLength(6);
            documentSequence.setMaxSequence(0);
            documentSequence.setPrefix("D");
            documentSequence.setPostfix(null);
            documentSequence.setOrganization(organization);
            documentSequenceRepository.save(documentSequence);

            documentSequence = new DocumentSequence();
            documentSequence.setDocumentId(CommonConstant.DOCUMENT_ID_FOR_PRODUCT_SKU);
            documentSequence.setDocumentName("Product Setup");
            documentSequence.setDescription("Product Setup");
            documentSequence.setSequenceLength(6);
            documentSequence.setMaxSequence(0);
            documentSequence.setPrefix("P");
            documentSequence.setPostfix(null);
            documentSequence.setOrganization(organization);
            documentSequenceRepository.save(documentSequence);

            documentSequence = new DocumentSequence();
            documentSequence.setDocumentId(CommonConstant.DOCUMENT_ID_FOR_LOCATION_TREE);
            documentSequence.setDocumentName("Location Tree");
            documentSequence.setDescription("Location Tree");
            documentSequence.setSequenceLength(6);
            documentSequence.setMaxSequence(0);
            documentSequence.setPrefix("LT");
            documentSequence.setPostfix(null);
            documentSequence.setOrganization(organization);
            documentSequenceRepository.save(documentSequence);

            documentSequence = new DocumentSequence();
            documentSequence.setDocumentId(CommonConstant.DOCUMENT_ID_FOR_BOOKING_NO);
            documentSequence.setDocumentName("Sales Booking");
            documentSequence.setDescription("Sales Booking");
            documentSequence.setSequenceLength(6);
            documentSequence.setMaxSequence(0);
            documentSequence.setPrefix("SB");
            documentSequence.setPostfix(null);
            documentSequence.setOrganization(organization);
            documentSequenceRepository.save(documentSequence);

            documentSequence = new DocumentSequence();
            documentSequence.setDocumentId(CommonConstant.DOCUMENT_ID_FOR_PAYMENT_NO);
            documentSequence.setDocumentName("Payment Collection");
            documentSequence.setDescription("Payment Collection");
            documentSequence.setSequenceLength(6);
            documentSequence.setMaxSequence(0);
            documentSequence.setPrefix("PC");
            documentSequence.setPostfix(null);
            documentSequence.setOrganization(organization);
            documentSequenceRepository.save(documentSequence);

            documentSequence = new DocumentSequence();
            documentSequence.setDocumentId(CommonConstant.DOCUMENT_ID_FOR_SALES_RETURN_PROPOSAL);
            documentSequence.setDocumentName("Sales Return Proposal");
            documentSequence.setDescription("Sales Return Proposal");
            documentSequence.setSequenceLength(6);
            documentSequence.setMaxSequence(0);
            documentSequence.setPrefix("SRP");
            documentSequence.setPostfix(null);
            documentSequence.setOrganization(organization);
            documentSequenceRepository.save(documentSequence);

            documentSequence = new DocumentSequence();
            documentSequence.setDocumentId(CommonConstant.DOCUMENT_ID_FOR_SALES_RETURN);
            documentSequence.setDocumentName("Sales Return");
            documentSequence.setDescription("Sales Return");
            documentSequence.setSequenceLength(6);
            documentSequence.setMaxSequence(0);
            documentSequence.setPrefix("SR");
            documentSequence.setPostfix(null);
            documentSequence.setOrganization(organization);
            documentSequenceRepository.save(documentSequence);

            documentSequence = new DocumentSequence();
            documentSequence.setDocumentId(CommonConstant.DOCUMENT_ID_FOR_CREDIT_DEBIT_NOTE);
            documentSequence.setDocumentName("Credit Debit Note");
            documentSequence.setDescription("Credit Debit Note");
            documentSequence.setSequenceLength(6);
            documentSequence.setMaxSequence(0);
            documentSequence.setPrefix("CDN");
            documentSequence.setPostfix(null);
            documentSequence.setOrganization(organization);
            documentSequenceRepository.save(documentSequence);

            documentSequence = new DocumentSequence();
            documentSequence.setDocumentId(CommonConstant.DOCUMENT_ID_FOR_SALES_ORDER);
            documentSequence.setDocumentName("Sales Order");
            documentSequence.setDescription("Sales Order");
            documentSequence.setSequenceLength(6);
            documentSequence.setMaxSequence(0);
            documentSequence.setPrefix("SO");
            documentSequence.setPostfix(null);
            documentSequence.setOrganization(organization);
            documentSequenceRepository.save(documentSequence);

            documentSequence = new DocumentSequence();
            documentSequence.setDocumentId(CommonConstant.DOCUMENT_ID_FOR_SALES_INVOICE);
            documentSequence.setDocumentName("Sales Invoice");
            documentSequence.setDescription("Sales Invoice");
            documentSequence.setSequenceLength(6);
            documentSequence.setMaxSequence(0);
            documentSequence.setPrefix("INV");
            documentSequence.setPostfix(null);
            documentSequence.setOrganization(organization);
            documentSequenceRepository.save(documentSequence);

            documentSequence = new DocumentSequence();
            documentSequence.setDocumentId(CommonConstant.DOCUMENT_ID_FOR_DELIVERY_CHALLAN);
            documentSequence.setDocumentName("Delivery Challan");
            documentSequence.setDescription("Delivery Challan");
            documentSequence.setSequenceLength(6);
            documentSequence.setMaxSequence(0);
            documentSequence.setPrefix("DC");
            documentSequence.setPostfix(null);
            documentSequence.setOrganization(organization);
            documentSequenceRepository.save(documentSequence);

            documentSequence = new DocumentSequence();
            documentSequence.setDocumentId(CommonConstant.DOCUMENT_ID_FOR_INV_TRANSFER);
            documentSequence.setDocumentName("Inventory Transfer");
            documentSequence.setDescription("Inventory Transfer");
            documentSequence.setSequenceLength(6);
            documentSequence.setMaxSequence(0);
            documentSequence.setPrefix("IT");
            documentSequence.setPostfix(null);
            documentSequence.setOrganization(organization);
            documentSequenceRepository.save(documentSequence);

            documentSequence = new DocumentSequence();
            documentSequence.setDocumentId(CommonConstant.DOCUMENT_ID_FOR_INV_TRANSFER_RCV);
            documentSequence.setDocumentName("Inventory Receive");
            documentSequence.setDescription("Inventory Receive");
            documentSequence.setSequenceLength(6);
            documentSequence.setMaxSequence(0);
            documentSequence.setPrefix("IR");
            documentSequence.setPostfix(null);
            documentSequence.setOrganization(organization);
            documentSequenceRepository.save(documentSequence);

            documentSequence = new DocumentSequence();
            documentSequence.setDocumentId(CommonConstant.DOCUMENT_ID_FOR_INTER_STORE_STOCK_MOVEMENT);
            documentSequence.setDocumentName("Inter Store Stock Movement");
            documentSequence.setDescription("Inter Store Stock Movement");
            documentSequence.setSequenceLength(6);
            documentSequence.setMaxSequence(0);
            documentSequence.setPrefix("ISM");
            documentSequence.setPostfix(null);
            documentSequence.setOrganization(organization);
            documentSequenceRepository.save(documentSequence);

            documentSequence = new DocumentSequence();
            documentSequence.setDocumentId(CommonConstant.DOCUMENT_ID_FOR_CREDIT_LIMIT_PROPOSAL);
            documentSequence.setDocumentName("Credit Limit Proposal");
            documentSequence.setDescription("Credit Limit Proposal");
            documentSequence.setSequenceLength(6);
            documentSequence.setMaxSequence(0);
            documentSequence.setPrefix("CLP");
            documentSequence.setPostfix(null);
            documentSequence.setOrganization(organization);
            documentSequenceRepository.save(documentSequence);

            documentSequence = new DocumentSequence();
            documentSequence.setDocumentId(CommonConstant.DOCUMENT_ID_FOR_DAMAGE_DECLARATION);
            documentSequence.setDocumentName("Damage Declaration");
            documentSequence.setDescription("Damage Declaration");
            documentSequence.setSequenceLength(6);
            documentSequence.setMaxSequence(0);
            documentSequence.setPrefix("DD");
            documentSequence.setPostfix(null);
            documentSequence.setOrganization(organization);
            documentSequenceRepository.save(documentSequence);

            documentSequence = new DocumentSequence();
            documentSequence.setDocumentId(CommonConstant.DOCUMENT_ID_FOR_PICKING);
            documentSequence.setDocumentName("Pecking List");
            documentSequence.setDescription("Pecking List");
            documentSequence.setSequenceLength(6);
            documentSequence.setMaxSequence(0);
            documentSequence.setPrefix("PK");
            documentSequence.setPostfix(null);
            documentSequence.setOrganization(organization);
            documentSequenceRepository.save(documentSequence);

            logger.info("Document sequence all initial data saved.");
        }

        if(distributorTypeRepository.count() == 0){
            DistributorType distributorType = new DistributorType();
            distributorType.setName("Platinum");
            distributorType.setOrganization(organization);
            distributorTypeRepository.save(distributorType);

            distributorType = new DistributorType();
            distributorType.setName("Silver");
            distributorType.setOrganization(organization);
            distributorTypeRepository.save(distributorType);

            distributorType = new DistributorType();
            distributorType.setName("Gold");
            distributorType.setOrganization(organization);
            distributorTypeRepository.save(distributorType);

            logger.info("Distributor type all initial data saved.");
        }

        if(invoiceNatureRepository.count() == 0){
            InvoiceNature invoiceNature = new InvoiceNature();
            invoiceNature.setName("Credit");
            invoiceNature.setDescription("Credit");
            invoiceNature.setOrganization(organization);
            invoiceNatureRepository.save(invoiceNature);

            invoiceNature = new InvoiceNature();
            invoiceNature.setName("Cash");
            invoiceNature.setDescription("Cash");
            invoiceNature.setOrganization(organization);
            invoiceNatureRepository.save(invoiceNature);

            logger.info("Invoice nature all initial data saved.");
        }

        if(featureInfoRepository.count() == 0){
            List<FeatureInfo> featureInfoList = new ArrayList<>() ;
            FeatureInfo featureInfo = null;

            //Mobile App Features Start
            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.SALES_BOOKING_MOBILE);
            featureInfo.setFeatureName(ActivityFeature.SALES_BOOKING_MOBILE.getName());
            featureInfo.setMenuUrl("");
            featureInfo.setControllerName(SalesBookingController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.PAYMENT_COLLECTION_MOBILE);
            featureInfo.setFeatureName(ActivityFeature.PAYMENT_COLLECTION_MOBILE.getName());
            featureInfo.setMenuUrl("");
            featureInfo.setControllerName(PaymentCollectionController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.SALES_RETURN_PROPOSAL_MOBILE);
            featureInfo.setFeatureName(ActivityFeature.SALES_RETURN_PROPOSAL_MOBILE.getName());
            featureInfo.setMenuUrl("");
            featureInfo.setControllerName(SalesReturnProposalController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.ORD_CALCULATOR_MOBILE);
            featureInfo.setFeatureName(ActivityFeature.ORD_CALCULATOR_MOBILE.getName());
            featureInfo.setMenuUrl("");
            featureInfo.setControllerName(OverridingDiscountController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.DISTRIBUTOR_LEDGER_MOBILE);
            featureInfo.setFeatureName(ActivityFeature.DISTRIBUTOR_LEDGER_MOBILE.getName());
            featureInfo.setMenuUrl("");
            featureInfo.setControllerName(DistributorController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.PRODUCT_DETAILS_MOBILE);
            featureInfo.setFeatureName(ActivityFeature.PRODUCT_DETAILS_MOBILE.getName());
            featureInfo.setMenuUrl("");
            featureInfo.setControllerName(ProductController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.CREDIT_LIMIT_PROPOSAL_MOBILE);
            featureInfo.setFeatureName(ActivityFeature.CREDIT_LIMIT_PROPOSAL_MOBILE.getName());
            featureInfo.setMenuUrl("");
            featureInfo.setControllerName(CreditLimitProposalController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.APPROVALS_MOBILE);
            featureInfo.setFeatureName(ActivityFeature.APPROVALS_MOBILE.getName());
            featureInfo.setMenuUrl("");
            featureInfo.setControllerName(MultiLayerApprovalPathController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.INVOICE_LIST_MOBILE);
            featureInfo.setFeatureName(ActivityFeature.INVOICE_LIST_MOBILE.getName());
            featureInfo.setMenuUrl("");
            featureInfo.setControllerName(SalesInvoiceController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.INVOICE_ACKNOWLEDGEMENT_MOBILE);
            featureInfo.setFeatureName(ActivityFeature.INVOICE_ACKNOWLEDGEMENT_MOBILE.getName());
            featureInfo.setMenuUrl("");
            featureInfo.setControllerName(SalesInvoiceController.class.getSimpleName());
            featureInfoList.add(featureInfo);
            //Mobile App Features End

            //Company
            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.LOCATION);
            featureInfo.setFeatureName(ActivityFeature.LOCATION.getName());
            featureInfo.setMenuUrl("/location/list");
            featureInfo.setControllerName(LocationTreeController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.ORGANIZATION);
            featureInfo.setFeatureName(ActivityFeature.ORGANIZATION.getName());
            featureInfo.setMenuUrl("/company/list");
            featureInfo.setControllerName(OrganizationController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            //Production
            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.BATCH_PREPARATION);
            featureInfo.setFeatureName(ActivityFeature.BATCH_PREPARATION.getName());
            featureInfo.setMenuUrl("/production/batch-preparation/batch-preparation-list");
            featureInfo.setControllerName(BatchController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.QUALITY_ASSURANCE);
            featureInfo.setFeatureName(ActivityFeature.QUALITY_ASSURANCE.getName());
            featureInfo.setMenuUrl("/production/production-qa/production-qa-setup");
            featureInfo.setControllerName(QualityInspectionController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.BATCH_PREPARATION_PRODUCT);
            featureInfo.setFeatureName(ActivityFeature.BATCH_PREPARATION_PRODUCT.getName());
            featureInfo.setMenuUrl("/production/batch-preparation/production-batch-preparation-product");
            featureInfo.setControllerName(BatchController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.MATERIAL_PLAN);
            featureInfo.setFeatureName(ActivityFeature.MATERIAL_PLAN.getName());
            featureInfo.setMenuUrl("/production/batch-preparation/production-batch-preparation-product-tickets");
            featureInfo.setControllerName(MaterialReceivePlanController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            //Inventory -> Configure
            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.PRODUCT_CONFIGURE);
            featureInfo.setFeatureName(ActivityFeature.PRODUCT_CONFIGURE.getName());
            featureInfo.setMenuUrl("/inventory/configure/product-configure/list");
            featureInfo.setControllerName(ProductController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.DEPOT_CONFIGURE);
            featureInfo.setFeatureName(ActivityFeature.DEPOT_CONFIGURE.getName());
            featureInfo.setMenuUrl("/inventory/configure/depot-configure/list");
            featureInfo.setControllerName(DepotController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            //Inventory -> Stock
            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.STOCK_DATA);
            featureInfo.setFeatureName(ActivityFeature.STOCK_DATA.getName());
            featureInfo.setMenuUrl("/inventory/stock/stock-list");
            featureInfo.setControllerName(StockDataController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.PRODUCT_OPENING_STOCK);
            featureInfo.setFeatureName(ActivityFeature.PRODUCT_OPENING_STOCK.getName());
            featureInfo.setMenuUrl("/inventory/stock/product-opening-stock");
            featureInfo.setControllerName(StockDataController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.SALES_RETURN);
            featureInfo.setFeatureName(ActivityFeature.SALES_RETURN.getName());
            featureInfo.setMenuUrl("/inventory/stock/sales-return/sales-return-list");
            featureInfo.setControllerName(SalesReturnController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.STORE_MOVEMENT);
            featureInfo.setFeatureName(ActivityFeature.STORE_MOVEMENT.getName());
            featureInfo.setMenuUrl("/inventory/stock/stock-store/stock-store-movement");
            featureInfo.setControllerName(InterStoreStockMovement.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.STOCK_DAMAGE_DECLARATION);
            featureInfo.setFeatureName(ActivityFeature.STOCK_DAMAGE_DECLARATION.getName());
            featureInfo.setMenuUrl("/inventory/stock/stock-damage/stock-damage-declaration");
            featureInfo.setControllerName(InterStoreStockMovement.class.getSimpleName());
            featureInfoList.add(featureInfo);

            //Inventory -> Stock -> Sales Order
            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.SALES_BOOKING_CONFIRMATION);
            featureInfo.setFeatureName(ActivityFeature.SALES_BOOKING_CONFIRMATION.getName());
            featureInfo.setMenuUrl("/inventory/stock/sales-order/sales-booking-list");
            featureInfo.setControllerName(SalesBookingController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.SALES_ORDER);
            featureInfo.setFeatureName(ActivityFeature.SALES_ORDER.getName());
            featureInfo.setMenuUrl("/inventory/stock/sales-order/sales-order-list");
            featureInfo.setControllerName(SalesOrderController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.PICKING_LIST);
            featureInfo.setFeatureName(ActivityFeature.PICKING_LIST.getName());
            featureInfo.setMenuUrl("/inventory/stock/sales-order/picking-list");
            featureInfo.setControllerName(PickingController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.DELIVERY_CHALLAN);
            featureInfo.setFeatureName(ActivityFeature.DELIVERY_CHALLAN.getName());
            featureInfo.setMenuUrl("/inventory/stock/sales-order/delivery-challan-list");
            featureInfo.setControllerName(InvDeliveryChallanController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.INVOICE);
            featureInfo.setFeatureName(ActivityFeature.INVOICE.getName());
            featureInfo.setMenuUrl("/inventory/stock/sales-order/invoice-list");
            featureInfo.setControllerName(SalesInvoiceController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            //Inventory -> Stock -> Stock Transfer
            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.PRODUCTION_RECEIVE);
            featureInfo.setFeatureName(ActivityFeature.PRODUCTION_RECEIVE.getName());
            featureInfo.setMenuUrl("/inventory/stock/stock-transfer/production-receive");
            featureInfo.setControllerName(InvReceiveController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.STOCK_RECEIVE);
            featureInfo.setFeatureName(ActivityFeature.STOCK_RECEIVE.getName());
            featureInfo.setMenuUrl("/inventory/stock/stock-transfer/stock-receive");
            featureInfo.setControllerName(InvReceiveController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.STOCK_SEND);
            featureInfo.setFeatureName(ActivityFeature.STOCK_SEND.getName());
            featureInfo.setMenuUrl("/inventory/stock/stock-transfer/stock-send");
            featureInfo.setControllerName(InvTransferController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            //Sales & Collection -> Sales
            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.SALES_DATA_VIEW);
            featureInfo.setFeatureName(ActivityFeature.SALES_DATA_VIEW.getName());
            featureInfo.setMenuUrl("/salescollection/sales/sales-data");
            featureInfo.setControllerName(SalesOrderController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.SALES_BOOKING_VIEW);
            featureInfo.setFeatureName(ActivityFeature.SALES_BOOKING_VIEW.getName());
            featureInfo.setMenuUrl("/salescollection/sales/sales-booking");
            featureInfo.setControllerName(SalesBookingController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.SALES_ORDER_VIEW);
            featureInfo.setFeatureName(ActivityFeature.SALES_ORDER_VIEW.getName());
            featureInfo.setMenuUrl("/salescollection/sales/sales-order");
            featureInfo.setControllerName(SalesOrderController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.SALES_RETURN_VIEW);
            featureInfo.setFeatureName(ActivityFeature.SALES_RETURN_VIEW.getName());
            featureInfo.setMenuUrl("/salescollection/sales/sales-return");
            featureInfo.setControllerName(SalesReturnController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.TRADE_PRICE_VIEW);
            featureInfo.setFeatureName(ActivityFeature.TRADE_PRICE_VIEW.getName());
            featureInfo.setMenuUrl("/salescollection/sales/trade-price");
            featureInfo.setControllerName(ProductTradePriceController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.TRADE_DISCOUNT_VIEW);
            featureInfo.setFeatureName(ActivityFeature.TRADE_DISCOUNT_VIEW.getName());
            featureInfo.setMenuUrl("/salescollection/sales/trade-discount");
            featureInfo.setControllerName(TradeDiscountController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.SALES_BUDGET_VIEW);
            featureInfo.setFeatureName(ActivityFeature.SALES_BUDGET_VIEW.getName());
            featureInfo.setMenuUrl("/salescollection/sales/sales-budget/product-wise");
            featureInfo.setControllerName(SalesBudgetController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            //Sales & Collection -> Payment Collection
            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.COLLECTION_DATA_VIEW);
            featureInfo.setFeatureName(ActivityFeature.COLLECTION_DATA_VIEW.getName());
            featureInfo.setMenuUrl("/salescollection/payment-collection/collection-data");
            featureInfo.setControllerName(PaymentCollectionController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.INVOICES_VIEW);
            featureInfo.setFeatureName(ActivityFeature.INVOICES_VIEW.getName());
            featureInfo.setMenuUrl("/salescollection/payment-collection/invoices/distributor-wise");
            featureInfo.setControllerName(SalesInvoiceController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.ORD_VIEW);
            featureInfo.setFeatureName(ActivityFeature.ORD_VIEW.getName());
            featureInfo.setMenuUrl("/salescollection/payment-collection/ord");
            featureInfo.setControllerName(OverridingDiscountController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.ORD_CALCULATOR_VIEW);
            featureInfo.setFeatureName(ActivityFeature.ORD_CALCULATOR_VIEW.getName());
            featureInfo.setMenuUrl("/salescollection/payment-collection/ord-calculator");
            featureInfo.setControllerName(OverridingDiscountController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.COLLECTION_BUDGET_VIEW);
            featureInfo.setFeatureName(ActivityFeature.COLLECTION_BUDGET_VIEW.getName());
            featureInfo.setMenuUrl("/salescollection/payment-collection/collection-budget/distributor-wise");
            featureInfo.setControllerName(CollectionBudgetController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            //Sales & Collection -> Payment Adjustment
            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.PAYMENT_VERIFY);
            featureInfo.setFeatureName(ActivityFeature.PAYMENT_VERIFY.getName());
            featureInfo.setMenuUrl("/salescollection/payment-adjustment/payment-verify/all");
            featureInfo.setControllerName(PaymentCollectionController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.PAYMENT_ADJUSTMENT);
            featureInfo.setFeatureName(ActivityFeature.PAYMENT_ADJUSTMENT.getName());
            featureInfo.setMenuUrl("/salescollection/payment-adjustment/payment-adjustment/invoices-map");
            featureInfo.setControllerName(PaymentCollectionAdjustmentController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.ORD_SETTLEMENT);
            featureInfo.setFeatureName(ActivityFeature.ORD_SETTLEMENT.getName());
            featureInfo.setMenuUrl("/salescollection/payment-adjustment/ord-settlement/all-list");
            featureInfo.setControllerName(PaymentCollectionAdjustmentController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.CREDIT_DEBIT_NOTE);
            featureInfo.setFeatureName(ActivityFeature.CREDIT_DEBIT_NOTE.getName());
            featureInfo.setMenuUrl("/salescollection/payment-adjustment/credit-debit-note/all-list");
            featureInfo.setControllerName(CreditDebitNoteController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            //Sales & Collection -> Distributors
            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.DISTRIBUTOR_LIST);
            featureInfo.setFeatureName(ActivityFeature.DISTRIBUTOR_LIST.getName());
            featureInfo.setMenuUrl("/salescollection/distributors/distributors-list");
            featureInfo.setControllerName(DistributorController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.CREDIT_LIMIT_PROPOSAL);
            featureInfo.setFeatureName(ActivityFeature.CREDIT_LIMIT_PROPOSAL.getName());
            featureInfo.setMenuUrl("/salescollection/distributors/credit-limit-proposal");
            featureInfo.setControllerName(DistributorController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.DISTRIBUTOR_LEDGER_REPORT);
            featureInfo.setFeatureName(ActivityFeature.DISTRIBUTOR_LEDGER_REPORT.getName());
            featureInfo.setMenuUrl("/salescollection/distributors/reports");
            featureInfo.setControllerName(DistributorController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.DISTRIBUTOR_OPENING_BALANCE);
            featureInfo.setFeatureName(ActivityFeature.DISTRIBUTOR_OPENING_BALANCE.getName());
            featureInfo.setMenuUrl("/salescollection/distributors/opening-balance");
            featureInfo.setControllerName(DistributorController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.DISTRIBUTOR_UPLOAD);
            featureInfo.setFeatureName(ActivityFeature.DISTRIBUTOR_UPLOAD.getName());
            featureInfo.setMenuUrl("/salescollection/distributors/distributor-upload");
            featureInfo.setControllerName(DistributorController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            //Sales & Collection -> Configure
            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.TIMELINE_SETUP);
            featureInfo.setFeatureName(ActivityFeature.TIMELINE_SETUP.getName());
            featureInfo.setMenuUrl("/salescollection/configure/time-line-setup/list");
            featureInfo.setControllerName(AccountingYearController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.TRADE_PRICE_SETUP);
            featureInfo.setFeatureName(ActivityFeature.TRADE_PRICE_SETUP.getName());
            featureInfo.setMenuUrl("/salescollection/configure/trade-price-setup/list");
            featureInfo.setControllerName(ProductTradePriceController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.TRADE_DISCOUNT_SETUP);
            featureInfo.setFeatureName(ActivityFeature.TRADE_DISCOUNT_SETUP.getName());
            featureInfo.setMenuUrl("/salescollection/configure/trade-discount-setup/list");
            featureInfo.setControllerName(TradeDiscountController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.BUDGET_SETUP);
            featureInfo.setFeatureName(ActivityFeature.BUDGET_SETUP.getName());
            featureInfo.setMenuUrl("/salescollection/configure/budget-setup/distributor-wise-list");
            featureInfo.setControllerName(CollectionBudgetController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.ORD_SETUP);
            featureInfo.setFeatureName(ActivityFeature.ORD_SETUP.getName());
            featureInfo.setMenuUrl("/salescollection/configure/ord-overdue-setup/ord-list");
            featureInfo.setControllerName(OverridingDiscountController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.OVERDUE_SETUP);
            featureInfo.setFeatureName(ActivityFeature.OVERDUE_SETUP.getName());
            featureInfo.setMenuUrl("/salescollection/configure/ord-overdue-setup/overdue-list");
            featureInfo.setControllerName(InvoiceOverdueController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.PAYMENT_BOOK_SETUP);
            featureInfo.setFeatureName(ActivityFeature.PAYMENT_BOOK_SETUP.getName());
            featureInfo.setMenuUrl("/salescollection/configure/payment-book/payment-book-setup");
            featureInfo.setControllerName(PaymentBookController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.CREDIT_LIMIT_SETUP);
            featureInfo.setFeatureName(ActivityFeature.CREDIT_LIMIT_SETUP.getName());
            featureInfo.setMenuUrl("/salescollection/configure/credit-limit-setup/credit-limit-setup");
            featureInfo.setControllerName(CreditLimitController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            //User Management
            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.USER_PROFILE);
            featureInfo.setFeatureName(ActivityFeature.USER_PROFILE.getName());
            featureInfo.setMenuUrl("/user-management/profile-setup");
            featureInfo.setControllerName(ApplicationUserController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.ROLE_SETUP);
            featureInfo.setFeatureName(ActivityFeature.ROLE_SETUP.getName());
            featureInfo.setMenuUrl("/user-management/role-setup");
            featureInfo.setControllerName(ApplicationUserController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.USER_ROLE_MAPPING);
            featureInfo.setFeatureName(ActivityFeature.USER_ROLE_MAPPING.getName());
            featureInfo.setMenuUrl("/user-management/user-role-setup-new");
            featureInfo.setControllerName(ApplicationUserRoleMapController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.USER_COMPANY_MAPPING);
            featureInfo.setFeatureName(ActivityFeature.USER_COMPANY_MAPPING.getName());
            featureInfo.setMenuUrl("/user-management/user-company-map");
            featureInfo.setControllerName(ApplicationUserCompanyMappingController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.FEATURE_PERMISSION_SETUP);
            featureInfo.setFeatureName(ActivityFeature.FEATURE_PERMISSION_SETUP.getName());
            featureInfo.setMenuUrl("/user-management/feature-permission-setup");
            featureInfo.setControllerName(PermissionController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            //Approval Path Setup
            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.APPROVAL_STEP_SETUP);
            featureInfo.setFeatureName(ActivityFeature.APPROVAL_STEP_SETUP.getName());
            featureInfo.setMenuUrl("/approval-path/approval-step-setup");
            featureInfo.setControllerName(ApprovalStepController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.AUTHORIZATION_FEATURE);
            featureInfo.setFeatureName(ActivityFeature.AUTHORIZATION_FEATURE.getName());
            featureInfo.setMenuUrl("/approval-path/authorization-feature");
            featureInfo.setControllerName(ApprovalStepFeatureController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.MULTILAYER_APPROVAL_PATH);
            featureInfo.setFeatureName(ActivityFeature.MULTILAYER_APPROVAL_PATH.getName());
            featureInfo.setMenuUrl("/approval-path/multilayer-approval-path-setup");
            featureInfo.setControllerName(MultiLayerApprovalPathController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            //Master Configure
            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.BANK_SETUP);
            featureInfo.setFeatureName(ActivityFeature.BANK_SETUP.getName());
            featureInfo.setMenuUrl("/master-config/bank-setup");
            featureInfo.setControllerName(BankController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.BRANCH_SETUP);
            featureInfo.setFeatureName(ActivityFeature.BRANCH_SETUP.getName());
            featureInfo.setMenuUrl("/master-config/bank-branch-setup");
            featureInfo.setControllerName(BankBranchController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.BANK_ACCOUNT_SETUP);
            featureInfo.setFeatureName(ActivityFeature.BANK_ACCOUNT_SETUP.getName());
            featureInfo.setMenuUrl("/master-config/bank-account-setup");
            featureInfo.setControllerName(BankAccountController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.DEPARTMENT_SETUP);
            featureInfo.setFeatureName(ActivityFeature.DEPARTMENT_SETUP.getName());
            featureInfo.setMenuUrl("/master-config/department-setup");
            featureInfo.setControllerName(DepartmentController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.DESIGNATION_SETUP);
            featureInfo.setFeatureName(ActivityFeature.DESIGNATION_SETUP.getName());
            featureInfo.setMenuUrl("/master-config/designation-setup");
            featureInfo.setControllerName(DesignationController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.STORE_SETUP);
            featureInfo.setFeatureName(ActivityFeature.STORE_SETUP.getName());
            featureInfo.setMenuUrl("/master-config/store-setup");
            featureInfo.setControllerName(StoreController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.UOM_SETUP);
            featureInfo.setFeatureName(ActivityFeature.UOM_SETUP.getName());
            featureInfo.setMenuUrl("/master-config/uom-setup");
            featureInfo.setControllerName(UnitOfMeasurementController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.PACK_SIZE_SETUP);
            featureInfo.setFeatureName(ActivityFeature.PACK_SIZE_SETUP.getName());
            featureInfo.setMenuUrl("/master-config/pack-size-setup");
            featureInfo.setControllerName(PackSizeController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.VEHICLE_SETUP);
            featureInfo.setFeatureName(ActivityFeature.VEHICLE_SETUP.getName());
            featureInfo.setMenuUrl("/master-config/vehicle-setup");
            featureInfo.setControllerName(VehicleController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.REPORTING_MANAGER_SETUP);
            featureInfo.setFeatureName(ActivityFeature.REPORTING_MANAGER_SETUP.getName());
            featureInfo.setMenuUrl("/master-config/reporting-manager-setup");
            featureInfo.setControllerName(ReportingManagerController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.LOCATION_MANAGER_SETUP);
            featureInfo.setFeatureName(ActivityFeature.LOCATION_MANAGER_SETUP.getName());
            featureInfo.setMenuUrl("/master-config/location-manager-setup");
            featureInfo.setControllerName(LocationManagerMapController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.DEPOT_LOCATION_LEVEL_SETUP);
            featureInfo.setFeatureName(ActivityFeature.DEPOT_LOCATION_LEVEL_SETUP.getName());
            featureInfo.setMenuUrl("/master-config/depot-location-level-map-setup");
            featureInfo.setControllerName(LocationTreeController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.TERMS_CONDITION_SETUP);
            featureInfo.setFeatureName(ActivityFeature.TERMS_CONDITION_SETUP.getName());
            featureInfo.setMenuUrl("/master-config/terms-and-condition-setup");
            featureInfo.setControllerName(TermsAndConditionsController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.MANUFACTURING_COST);
            featureInfo.setFeatureName(ActivityFeature.MANUFACTURING_COST.getName());
            featureInfo.setMenuUrl("/inventory/stock/stock-transfer/production-receive");
            featureInfo.setControllerName(InvReceiveController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            //Reports
            featureInfo = new FeatureInfo();
            featureInfo.setActivityFeature(ActivityFeature.RECEIVABLE_INVOICE_STATEMENT_REPORT);
            featureInfo.setFeatureName(ActivityFeature.RECEIVABLE_INVOICE_STATEMENT_REPORT.getName());
            featureInfo.setMenuUrl("/report/mis-report/finance-report/ReceivableInvoiceStatement");
            featureInfo.setControllerName(CommonReportsController.class.getSimpleName());
            featureInfoList.add(featureInfo);

            featureInfoRepository.saveAll(featureInfoList);
            logger.info("Feature info all initial data saved.");
        }
    }
}
