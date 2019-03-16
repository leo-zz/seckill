package com.leozz.dto;

/**
 * @Author: leo-zz
 * @Date: 2019/3/16 8:26
 */
public class SubmitDTO {

    private Long userId;
    private Long activityId;

    private Long fullrangeCouponId;
    private Long couponId;

    private int usedPoint;


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

    public int getUsedPoint() {
        return usedPoint;
    }

    public void setUsedPoint(int usedPoint) {
        this.usedPoint = usedPoint;
    }
}
