package com.leozz.util.cache;

import com.leozz.dao.CouponMapper;
import com.leozz.entity.Coupon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/** 优惠券类型信息的本地缓存，用户拥有优惠券的信息仍然是从数据库查询
 * @Author: leo-zz
 * @Date: 2019/3/18 10:04
 */
@Component
public class CouponLocalCache {


    @Autowired
    CouponMapper couponMapper;


    /**
     *  根据商品秒杀的价格筛选可用的优惠券
     * @param userId
     * @param price
     * @return
     */
    public List<Coupon> selectUsableCouponByUserId(Long userId, double price) {
        return  couponMapper.selectUsableCouponByUserId(userId,price);
    }

    /**
     *  冻结优惠券
     * @param couponId
     * @return
     */
    public int frozenCouponById(Long couponId) {
        return couponMapper.frozenCouponById(couponId);
    }
}
