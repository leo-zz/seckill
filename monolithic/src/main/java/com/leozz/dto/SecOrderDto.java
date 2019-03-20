package com.leozz.dto;

import com.leozz.entity.SecOrder;

/**
 *  基于订单实体类SecOrder的扩充，可以使用继承的方式，也可以使用组合的方式
 * @Author: leo-zz
 * @Date: 2019/3/18 14:21
 */
public class SecOrderDto extends SecOrder {


    private Long fullrangeCouponId;
    private Long couponId;

    private Integer usedPoint;

    public SecOrderDto() {
    }

    public SecOrderDto(SecOrder secOrder) {
        super.setActivityId(secOrder.getActivityId());
        super.setAmount(secOrder.getAmount());
        super.setCouponUsage(secOrder.getCouponUsage());
        super.setCreateDate(secOrder.getCreateDate());
        super.setDeliveryAddrId(secOrder.getDeliveryAddrId());
        super.setId(secOrder.getId());
        super.setOrderChannel(secOrder.getOrderChannel());
        super.setPayDate(secOrder.getPayDate());
        super.setPointUsage(secOrder.getPointUsage());
        super.setStatus(secOrder.getStatus());
        super.setUserId(secOrder.getUserId());
    }

    public Long getFullrangeCouponId() {
        return fullrangeCouponId;
    }

    public void setFullrangeCouponId(Long fullrangeCouponId) {
        this.fullrangeCouponId = fullrangeCouponId;
    }

    public Long getCouponId() {
        return couponId;
    }

    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }

    public Integer getUsedPoint() {
        return usedPoint;
    }

    public void setUsedPoint(Integer usedPoint) {
        this.usedPoint = usedPoint;
    }
}
