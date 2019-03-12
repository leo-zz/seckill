package com.leozz.entity;

import java.math.BigDecimal;

public class Coupon {
    private Long id;

    private Byte type;

    private BigDecimal discountLimit;

    private BigDecimal discountAmount;

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

    public BigDecimal getDiscountLimit() {
        return discountLimit;
    }

    public void setDiscountLimit(BigDecimal discountLimit) {
        this.discountLimit = discountLimit;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
}