package com.leozz.dto;

import com.leozz.entity.Coupon;

/**
 * @Author: leo-zz
 * @Date: 2019/3/25 11:05
 */
public class CouponDTO extends  Coupon {
    private Long recordId;

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }
}
