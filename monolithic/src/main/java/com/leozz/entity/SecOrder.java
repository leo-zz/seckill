package com.leozz.entity;

import java.math.BigDecimal;
import java.util.Date;

public class SecOrder {
    private Long id;

    private Long userId;

    private Long activityId;

    private Long deliveryAddrId;

    private BigDecimal amount;

    private Byte orderChannel;

    /**
     * 订单状态，0新建未支付，1已支付，2已发货，3已收货，4已退款，5已完成
     */
    private Byte status;

    private Date createDate;

    private Date payDate;

    private Boolean couponUsage;

    private Boolean pointUsage;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Long getDeliveryAddrId() {
        return deliveryAddrId;
    }

    public void setDeliveryAddrId(Long deliveryAddrId) {
        this.deliveryAddrId = deliveryAddrId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Byte getOrderChannel() {
        return orderChannel;
    }

    public void setOrderChannel(Byte orderChannel) {
        this.orderChannel = orderChannel;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getPayDate() {
        return payDate;
    }

    public void setPayDate(Date payDate) {
        this.payDate = payDate;
    }

    public Boolean getCouponUsage() {
        return couponUsage;
    }

    public void setCouponUsage(Boolean couponUsage) {
        this.couponUsage = couponUsage;
    }

    public Boolean getPointUsage() {
        return pointUsage;
    }

    public void setPointUsage(Boolean pointUsage) {
        this.pointUsage = pointUsage;
    }
}