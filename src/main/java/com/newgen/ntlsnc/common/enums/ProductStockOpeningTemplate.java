package com.newgen.ntlsnc.common.enums;

/**
 * @author Newaz Sharif
 * @since 22th Aug, 22
 */
public enum ProductStockOpeningTemplate {
    PRODUCT_CATEGORY_ID("PRODUCT_CATEGORY_ID", "id"),
    PRODUCT_ID("PRODUCT_ID", "productId"),
    PRODUCT_NAME("PRODUCT_NAME", "productName"),
    PRODUCT_SKU("PRODUCT_SKU", "productSku"),
    CARTON_SIZE("CARTON_SIZE", "packSize"),
    PACK_SIZE("PACK_SIZE", "itemSize"),
    UoM("UoM", "uom"),
    OPENING_STOCK("OPENING_STOCK", "opening_stock"),
    RATE("RATE", "rate"),
    VALUE("VALUE", "value"),
    ;

    private String code;
    private String name;

    ProductStockOpeningTemplate(String code, String name) {
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
