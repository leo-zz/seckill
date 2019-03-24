package com.leozz.util.cache;

import com.leozz.dao.CouponMapper;
import com.leozz.entity.Coupon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 优惠券类型信息的本地缓存，用户拥有优惠券的信息仍然是从数据库查询
 * @Author: leo-zz
 * @Date: 2019/3/18 10:04
 */
@Component
public class CouponLocalCache {


    @Autowired
    CouponMapper couponMapper;

    private Map<Long, Coupon> couponMap = new ConcurrentHashMap<>();


    /**
     *  根据商品秒杀的价格筛选可用的优惠券,即商品秒杀价格高于优惠券的使用门槛
     *  先不考虑缓存，后期可以在user中增加优惠券的信息
     * @param userId
     * @param price
     * @return 先按照类别排序(先指定品类，然后全品类)，同类别的按照面值大小排序
     */
    public List<Coupon> selectUsableCouponByUserId(Long userId, double price) {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("userId",userId);
        paramMap.put("price",price);
        return  couponMapper.selectUsableCouponByUserId(paramMap);
    }

    /**
     *  冻结优惠券
     * @param couponId
     * @return
     */
    public int frozenCouponById(Long couponId) {
        return   couponMapper.frozenCouponById(couponId);
    }

    public Coupon selectById(Long couponId) {
        return null;
    }
}
