package com.leozz.serviceImpl;

import com.leozz.entity.Coupon;
import com.leozz.service.CouponService;

import java.util.List;

/**
 * @Author: leo-zz
 * @Date: 2019/3/14 10:08
 */
public class CouponServiceImpl implements CouponService {
    @Override
    public List<Coupon> getAvaliableCouponList(Long secActivityId, Long userId) {
        return null;
    }

    @Override
    public boolean frozenCoupon(Long couponId, Long orderId) {
        return false;
    }

    @Override
    public boolean useCoupon(Long couponId, Long orderId) {
        return false;
    }
}
