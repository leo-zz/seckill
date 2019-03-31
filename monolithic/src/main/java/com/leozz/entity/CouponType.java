package com.leozz.entity;

import java.math.BigDecimal;

public class CouponType {
    private Long id;

    private Byte category;

    private String couponName;

    private BigDecimal usageLimit;

    private BigDecimal couponValue;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Byte getCategory() {
        return category;
    }

    public void setCategory(Byte category) {
        this.category = category;
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