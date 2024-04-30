package com.newgen.ntlsnc.common.enums;

/**
 * @author Newaz Sharif
 * @since 25th Aug, 22
 */
public enum DistributorWiseCollectionBudgetTemplate {
    PRODUCT_ID("PRODUCT_ID", "Product_Id"),
    PRODUCT_NAME("PRODUCT_NAME", "Product_Name"),
    DISTRIBUTOR_ID("DISTRIBUTOR_ID", "Distributor_Id"),
    DISTRIBUTOR_NAME("DISTRIBUTOR_NAME", "Distributor_Name"),
    PRODUCT_TRADE_PRICE("PRODUCT_TRADE_PRICE", "Product_Trade_price");
    private String code;
    private String name;

    DistributorWiseCollectionBudgetTemplate(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
