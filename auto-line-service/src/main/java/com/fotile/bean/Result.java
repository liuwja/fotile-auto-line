package com.fotile.bean;

import com.fotile.constant.IConstant;

public class Result implements IConstant {
    private Integer status;
    private String reason;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
