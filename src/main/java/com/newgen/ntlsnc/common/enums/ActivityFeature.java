package com.newgen.ntlsnc.common.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nisa
 * @date 6/9/22
 * @time 1:53 PM
 */

public enum ActivityFeature {
    /**
     *Mobile APP Features
     *------------------------------------------------------------------------------------------------------------------
     */
    SALES_BOOKING_MOBILE("SALES_BOOKING_MOBILE", "Sales Booking Mobile"),
    PAYMENT_COLLECTION_MOBILE("PAYMENT_COLLECTION_MOBILE", "Payment Collection Mobile"),
    SALES_RETURN_PROPOSAL_MOBILE("SALES_RETURN_PROPOSAL_MOBILE", "Sales Return Proposal Mobile"),
    ORD_CALCULATOR_MOBILE("ORD_CALCULATOR_MOBILE", "Ord Calculator Mobile"),
    DISTRIBUTOR_LEDGER_MOBILE("DISTRIBUTOR_LEDGER_MOBILE", "Distributor Ledger Mobile"),
    PRODUCT_DETAILS_MOBILE("PRODUCT_DETAILS_MOBILE", "Product Details Mobile"),
    CREDIT_LIMIT_PROPOSAL_MOBILE("CREDIT_LIMIT_PROPOSAL_MOBILE", "Credit Limit Proposal Mobile"),
    APPROVALS_MOBILE("APPROVALS_MOBILE", "Approvals Mobile"),
    INVOICE_LIST_MOBILE("INVOICE_LIST_MOBILE", "Invoice List Mobile"),
    INVOICE_ACKNOWLEDGEMENT_MOBILE("INVOICE_ACKNOWLEDGEMENT_MOBILE", "Invoice Acknowledgement Mobile"),

    /**
     *Web App Features
     *------------------------------------------------------------------------------------------------------------------
     */
    //Company
    LOCATION("LOCATION", "Location Setup "),
    ORGANIZATION("ORGANIZATION", "Company Setup"),

    //Production
    BATCH_PREPARATION("BATCH_PREPARATION", "Batch Preparation"),
    QUALITY_ASSURANCE("QUALITY_ASSURANCE", "Quality Assurance"),
    BATCH_PREPARATION_PRODUCT("BATCH_PREPARATION_PRODUCT", "Batch Preparation Product"),
    MATERIAL_PLAN("MATERIAL_PLAN", "Material Plan"),

    //Inventory -> Configure
    PRODUCT_CONFIGURE("PRODUCT_CONFIGURE", "Product Configure"),
    DEPOT_CONFIGURE("DEPOT_CONFIGURE", "Depot Configure"),

    //Inventory -> Stock
    STOCK_DATA("STOCK_DATA", "Stock Data"),
    SALES_RETURN("SALES_RETURN", "Sales Return"),
//    RECEIVE_AND_QA("RECEIVE_AND_QA", "Receive & QA"),
    STORE_MOVEMENT("STORE_MOVEMENT", "Store Movement"),
    STOCK_DAMAGE_DECLARATION("STOCK_DAMAGE_DECLARATION", "Stock Damage Declaration"),
    PRODUCT_OPENING_STOCK("PRODUCT_OPENING_STOCK", "Product Opening Stock"),
    //Inventory -> Stock -> Sales Order
    SALES_BOOKING_CONFIRMATION("SALES_BOOKING_CONFIRMATION", "Sales Booking Confirmation"), //Sales Booking
    SALES_ORDER("SALES_ORDER", "Sales Order"),
    PICKING_LIST("PICKING_LIST", "Picking List"),
    DELIVERY_CHALLAN("DELIVERY_CHALLAN", "Delivery Challan"),
    INVOICE("INVOICE", "Invoice"),

    //Inventory -> Stock -> Stock Transfer
    PRODUCTION_RECEIVE("PRODUCTION_RECEIVE", "Production Receive"),
    STOCK_RECEIVE("STOCK_RECEIVE", "Stock Receive"),
    STOCK_SEND("STOCK_SEND", "Stock Send"),
    MANUFACTURING_COST("MANUFACTURING_COST", "Manufacturing Cost Setup"),

    //Sales & Collection -> Sales
    SALES_DATA_VIEW("SALES_DATA_VIEW", "Sales Data View"),
    SALES_BOOKING_VIEW("SALES_BOOKING_VIEW", "Sales Booking View"),
    SALES_ORDER_VIEW("SALES_ORDER_VIEW", "Sales Order View"),
    SALES_RETURN_VIEW("SALES_RETURN_VIEW", "Sales Return View"),
    TRADE_PRICE_VIEW("TRADE_PRICE_VIEW", "Trade Price View"),
    TRADE_DISCOUNT_VIEW("TRADE_DISCOUNT_VIEW", "Trade Discount View"),
    SALES_BUDGET_VIEW("SALES_BUDGET_VIEW", "Sales Budget View"),

    //Sales & Collection -> Payment Collection
    COLLECTION_DATA_VIEW("COLLECTION_DATA_VIEW", "Collection Data View"),
    INVOICES_VIEW("INVOICES_VIEW", "Invoices View"),
    ORD_VIEW("ORD_VIEW", "ORD View"),
    ORD_CALCULATOR_VIEW("ORD_CALCULATOR_VIEW", "ORD Calculator View"),
    COLLECTION_BUDGET_VIEW("COLLECTION_BUDGET_VIEW", "Collection Budget View"),

    //Sales & Collection -> Payment Adjustment
    PAYMENT_VERIFY("PAYMENT_VERIFY", "Payment Verify"),
    PAYMENT_ADJUSTMENT("PAYMENT_ADJUSTMENT", "Payment Adjustment"),
    ORD_SETTLEMENT("ORD_SETTLEMENT", "ORD Settlement"),
    CREDIT_DEBIT_NOTE("CREDIT_DEBIT_NOTE", "Credit/Debit Note"),

    //Sales & Collection -> Distributors
    DISTRIBUTOR_LIST("DISTRIBUTOR_LIST", "Distributor List"),
    CREDIT_LIMIT_PROPOSAL("CREDIT_LIMIT_PROPOSAL", "Credit Limit Proposal"),
    DISTRIBUTOR_LEDGER_REPORT("DISTRIBUTOR_LEDGER_REPORT", "Distributor Ledger Report"),
    DISTRIBUTOR_OPENING_BALANCE("DISTRIBUTOR_OPENING_BALANCE", "Distributor Opening Balance"),

    DISTRIBUTOR_UPLOAD("DISTRIBUTOR_UPLOAD", "Distributor Upload"),

    //Sales & Collection -> Configure
    TIMELINE_SETUP("TIMELINE_SETUP", "Timeline Setup"),
    TRADE_PRICE_SETUP("TRADE_PRICE_SETUP", "Trade Price Setup"),
    TRADE_DISCOUNT_SETUP("TRADE_DISCOUNT_SETUP", "Trade Discount Setup"),
    BUDGET_SETUP("BUDGET_SETUP", "Budget Setup"),
    ORD_SETUP("ORD_SETUP", "ORD Setup"),
    OVERDUE_SETUP("OVERDUE_SETUP", "Overdue Setup"),
    PAYMENT_BOOK_SETUP("PAYMENT_BOOK_SETUP", "Payment Book Setup"),
    CREDIT_LIMIT_SETUP("CREDIT_LIMIT_SETUP", "Credit Limit Setup"),

    //User Management
    USER_PROFILE("USER_PROFILE", "User Profile"),
    ROLE_SETUP("ROLE_SETUP", "Role Setup"),
    USER_ROLE_MAPPING("USER_ROLE_MAPPING", "User Role Mapping"),
    USER_COMPANY_MAPPING("USER_COMPANY_MAPPING", "User Company Mapping"),
    FEATURE_PERMISSION_SETUP("FEATURE_PERMISSION_SETUP", "Feature Permission Setup"),

    //Approval Path Setup
    APPROVAL_STEP_SETUP("APPROVAL_STEP_SETUP", "Approval Step Setup"),
    AUTHORIZATION_FEATURE("AUTHORIZATION_FEATURE", "Authorization Feature"),
    MULTILAYER_APPROVAL_PATH("MULTILAYER_APPROVAL_PATH", "Multilayer Approval Path"),

    //Master Configure
    BANK_SETUP("BANK_SETUP", "Bank Setup"),
    BRANCH_SETUP("BRANCH_SETUP", "Branch Setup"),
    BANK_ACCOUNT_SETUP("BANK_ACCOUNT_SETUP", "Bank Account Setup"),
    DEPARTMENT_SETUP("DEPARTMENT_SETUP", "Department Setup"),
    DESIGNATION_SETUP("DESIGNATION_SETUP", "Designation Setup"),
    STORE_SETUP("STORE_SETUP", "Store Setup"),
    UOM_SETUP("UOM_SETUP", "Unit of Measurement Setup"),
    PACK_SIZE_SETUP("PACK_SIZE_SETUP", "Pack Size Setup"),
    VEHICLE_SETUP("VEHICLE_SETUP", "Vehicle Setup"),
    REPORTING_MANAGER_SETUP("REPORTING_MANAGER_SETUP", "Reporting Manager Setup"),
    LOCATION_MANAGER_SETUP("LOCATION_MANAGER_SETUP", "Location Manager Setup"),
    DEPOT_LOCATION_LEVEL_SETUP("DEPOT_LOCATION_LEVEL_SETUP", "Depot Location Level Setup"),
    TERMS_CONDITION_SETUP("TERMS_CONDITION_SETUP", "Terms & Condition Setup"),

    //Reports
    RECEIVABLE_INVOICE_STATEMENT_REPORT("RECEIVABLE_INVOICE_STATEMENT_REPORT", "Receivable Invoice Statement Report");

    private String code;
    private String name;
    ActivityFeature(String code, String name) {
        this.code = code;
        this.name = name;
    }
    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static List<Map<String, String>> getAll(){
        List<Map<String, String>> mapList = new ArrayList<>();

        for(ActivityFeature activityFeature : ActivityFeature.values()){
            Map<String, String> map = new HashMap<>();
            map.put("code", activityFeature.code);
            map.put("name", activityFeature.name);

            mapList.add(map);
        }

        return mapList;
    }
}
