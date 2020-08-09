package com.fotile.constant;

public interface IStorageType {

    /**
     * 成品入库
     */
    String PRODUCT_INPUT = "成品入库";
    /**
     * 成品出库
     */
    String PRODUCT_OUTPUT = "成品出库";
    /**
     * 成品撤销
     */
    String PRODUCT_CANCEL = "成品撤销";
    /**
     * 成品撤销发货
     */
    String PRODUCT_CANCEL_OUTPUT = "成品退库";

    String INNER_BACK = "内部领用退库";
    String INNER_OUTPUT = "内部领用出库";
}
