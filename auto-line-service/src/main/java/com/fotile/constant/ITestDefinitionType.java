package com.fotile.constant;

public interface ITestDefinitionType {
    String ON_LINE = "在线检验";
    String REPAIR = "其他检验";

    /**
     * 检验类型为FQC
     */
    String FQC = "FQC";

    /**
     * 检验类型为OQC
     */
    String OQC = "OQC";

    /**
     * 检验类型为IQC
     */
    String IQC = "IQC";


    /**
     * 检验类型为UNIT_IQC
     */
    String UNIT_IQC = "UNIT_IQC";

    /**
     * FQC缺陷代码list
     */
    String FQC_DEFECT = "FQC_DefectCode";

    /**
     * FQC维修代码list
     */
    String FQC_REPAIR = "FQC_RepairCode";

    /**
     * OQC缺陷代码list
     */
    String OQC_DEFECT = "OQC_DefectCode";

    /**
     * OQC维修代码list
     */
    String OQC_REPAIR = "OQC_RepairCode";

    /**
     * OQC退库检验
     */
    String OQC_BACK="OQC_BACK";

    /**
     * OQC在库周期检
     */
    String OQC_ROUND="OQC_ROUND";
}