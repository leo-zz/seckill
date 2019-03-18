package com.leozz.dto;

import com.leozz.entity.SecOrder;

/**
 * @Author: leo-zz
 * @Date: 2019/3/18 14:21
 */
public class SecOrderDto extends SecOrder {


    private Long fullrangeCouponId;
    private Long couponId;

    private Integer usedPoint;

    public SecOrderDto() {
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
