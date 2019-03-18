package com.leozz.entity;

import java.math.BigDecimal;

public class Coupon {
    private Long id;

    private Byte type;

    private String couponName;

    private BigDecimal usageLimit;

    private BigDecimal couponValue;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }

    public String getCouponName() {
        return couponName;
    }

    public void setCouponName(String couponName) {
        this.couponName = couponName == null ? null : couponName.trim();
    }

    public BigDecimal getUsageLimit() {
        return usageLimit;
    }

    public void setUsageLimit(BigDecimal usageLimit) {
        this.usageLimit = usageLimit;
    }

    public BigDecimal getCouponValue() {
        return couponValue;
    }

    public void setCouponValue(BigDecimal couponValue) {
        this.couponValue = couponValue;
    }
}