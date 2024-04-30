package com.newgen.ntlsnc.common;

/**
 * @author liton
 * Created on 4/3/22 3:25 PM
 */
public class CommonConstant {
    public static final String SFTP_SERVER_ROOT_DIR_NAME = "snc";
    public static final String CREATE_SAVE_MESSAGE = " has been Saved Successfully.";
    public static final String CREATE_ADJUST_MESSAGE = " has been Adjusted Successfully.";
    public static final String CREATE_SUCCESS_MESSAGE = " has been Created Successfully.";
    public static final String ADDTOCART_SUCCESS_MESSAGE = " Product Added To Cart Successfully.";
    public static final String UPDATE_SUCCESS_MESSAGE = "  Successfully Updated.";
    public static final String DELETE_SUCCESS_MESSAGE = " Deleted Successfully.";
    public static final String APPROVE_SUCCESS_MESSAGE = " Approved Successfully.";
    public static final String REJECT_SUCCESS_MESSAGE = " Rejected Successfully.";
    public static final String RECEIVE_SUCCESS_MESSAGE = " has been Received Successfully.";
    public static final String CREATE_ERROR_MESSAGE = " Can't be Created. Something went wrong!";
    public static final String UPDATE_ERROR_MESSAGE = " Can't be Updated. Something went wrong!";
    public static final String DELETE_ERROR_MESSAGE = " Can't be Deleted. Something went wrong!.";
    public static final String DELETE_EXIST_MESSAGE = " Can't be Deleted. Already exists in ";
    public static final String STOCK_MOVEMENT_SUCCESS_MESSAGE = "Stock has been Moved Successfully.";

    public static final String CANCEL_ERROR_MESSAGE = " Can't be Cancel. Something went wrong!.";

    public static final String PICKING_DELIVERED_MESSAGE = " Can't be Cancel. Already delivered!.";
    public static final String ROLE_PREFIX = "ROLE_";

    public static final int DOCUMENT_ID_FOR_DEPOT_ID = 1; // For Depot ID
    public static final int DOCUMENT_ID_FOR_PRODUCT_SKU = 2; // For PRODUCT SKU
    public static final int DOCUMENT_ID_FOR_LOCATION_TREE = 3;
    public static final String LIFE_CYCLE_STATUS_PENDING = "Pending";
    public static final String LIFE_CYCLE_STATUS_INPROGRESS = "In Progress";
    public static final String LIFE_CYCLE_STATUS_COMPLETED = "Completed";

    public static final int DOCUMENT_ID_FOR_BOOKING_NO = 4;

    public static final String CONTENT_UPLOAD_ROOT_FOLDER = "uploads";
    public static final String DISTRIBUTOR_LOGO_UPLOAD_DIRECTORY = "distributor";

    public static final int DOCUMENT_ID_FOR_PAYMENT_NO = 5;
    public static final int DOCUMENT_ID_FOR_SALES_RETURN_PROPOSAL = 6;
    public static final int DOCUMENT_ID_FOR_SALES_RETURN = 7;
    public static final int DOCUMENT_ID_FOR_CREDIT_DEBIT_NOTE = 8;
    public static final int DOCUMENT_ID_FOR_SALES_ORDER = 9;
    public static final int DOCUMENT_ID_FOR_SALES_INVOICE = 10;
    public static final int DOCUMENT_ID_FOR_DELIVERY_CHALLAN = 11;
    public static final int DOCUMENT_ID_FOR_INV_TRANSFER = 12;

    public static final int DOCUMENT_ID_FOR_INV_TRANSFER_RCV = 13;
    public static final int DOCUMENT_ID_FOR_INTER_STORE_STOCK_MOVEMENT = 14;
    public static final String COLLECTION_BUDGET_FIELD = "collectionBudget";
    public static final String COLLECTION_AMOUNT_FIELD = "collectionAmount";
    public static final String PAYMENT_COLLECTION_ENTITY = "PaymentCollection";
    public static final String SALES_INVOICE_ACKNOWLEDGEMENT = "SalesInvoice";
    public static final String PROPRIETOR_LOGO_UPLOAD_DIRECTORY = "proprietor";
    public static final String GUARANTOR_LOGO_UPLOAD_DIRECTORY = "guarantor";
    public static final String SALES_BUDGET_FILE_UPLOAD_PATH = "src/main/resources/uploads/sales-budget/";
    public static final String COLLECTION_BUDGET_FILE_UPLOAD_PATH = "src/main/resources/uploads/collection-budget/";
    public static final int DOCUMENT_ID_FOR_CREDIT_LIMIT_PROPOSAL = 15;
    public static final int DOCUMENT_ID_FOR_DAMAGE_DECLARATION = 16;
    public static final int DOCUMENT_ID_FOR_PICKING = 17;

    public static final int DOCUMENT_ID_FOR_PRODUCTION_RECEIVE = 18;

    public static final String REPORT_FILE_PATH = "reports/";

    public static final String STOCK_OPENING_FILE_UPLOAD_PATH = "src/main/resources/uploads/stock-opening/";
    public static final String DISTRIBUTOR_OPENING_BALANCE_FILE_UPLOAD_PATH = "src/main/resources/uploads/distributor-opening-balance/";

    public static final Long CREDIT_INVOICE_NATURE = 1L;
    public static final Long CASH_INVOICE_NATURE = 2L;


}
