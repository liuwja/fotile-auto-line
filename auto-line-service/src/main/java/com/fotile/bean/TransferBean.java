package com.fotile.bean;

import java.util.List;

public class TransferBean {
    private String recid;
    private String serialNumber;
    private boolean pass;
    private String workCenterName;
    private List<String> defectCodes;
    private String productionLine;
    private Integer stepMark;

    public String getRecid() {
        return recid;
    }

    public void setRecid(String recid) {
        this.recid = recid;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public boolean isPass() {
        return pass;
    }

    public void setPass(boolean pass) {
        this.pass = pass;
    }

    public String getWorkCenterName() {
        return workCenterName;
    }

    public void setWorkCenterName(String workCenterName) {
        this.workCenterName = workCenterName;
    }

    public List<String> getDefectCodes() {
        return defectCodes;
    }

    public void setDefectCodes(List<String> defectCodes) {
        this.defectCodes = defectCodes;
    }

    public String getProductionLine() {
        return productionLine;
    }

    public void setProductionLine(String productionLine) {
        this.productionLine = productionLine;
    }

    public Integer getStepMark() {
        return stepMark;
    }

    public void setStepMark(Integer stepMark) {
        this.stepMark = stepMark;
    }
}
