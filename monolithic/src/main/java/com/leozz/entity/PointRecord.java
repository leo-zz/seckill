package com.leozz.entity;

import java.util.Date;

public class PointRecord {
    private Long id;

    private Long orderId;

    private Byte cause;

    private Integer updateAmount;

    private Date updateDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Byte getCause() {
        return cause;
    }

    public void setCause(Byte cause) {
        this.cause = cause;
    }

    public Integer getUpdateAmount() {
        return updateAmount;
    }

    public void setUpdateAmount(Integer updateAmount) {
        this.updateAmount = updateAmount;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
}