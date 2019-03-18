package com.leozz.dto;

/**
 * @Author: leo-zz
 * @Date: 2019/3/16 8:26
 */
public class SubmitDTO {

    //用户信息
    private Long userId;
    //活动信息
    private Long activityId;

    //收货地址信息
    private Long deliveryAddrId;

    //订单来源
    private Byte orderChannel;

    //优惠券信息
    private Long fullrangeCouponId;
    private Long couponId;
    //积分信息
    private int usedPoint;

    //订单金额，秒杀价格减去优惠券的优惠，减去积分的余额。
    private double orderAmount;


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

    public Byte getOrderChannel() {
        return orderChannel;
    }

    public void setOrderChannel(Byte orderChannel) {
        this.orderChannel = orderChannel;
    }

    public double getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(double orderAmount) {
        this.orderAmount = orderAmount;
    }

    public int getUsedPoint() {
        return usedPoint;
    }

    public void setUsedPoint(int usedPoint) {
        this.usedPoint = usedPoint;
    }
}
