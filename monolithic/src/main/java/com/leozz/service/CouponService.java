package com.leozz.service;

import com.leozz.entity.CouponType;

import java.util.List;

/**
 * @Author: leo-zz
 * @Date: 2019/3/13 10:44
 */
public interface CouponService {

    /**
     *  获取秒杀活动可用的优惠券信息，并按照面额从大到小进行排列。对优惠券的类别和可用额度进行校验。
     * @param secActivityId 活动编号的id
     * @param userId 用户Id
     * @return 可用的优惠券列表
     */
    List<CouponType>  getAvaliableCouponList(Long secActivityId, Long userId);

    //领取优惠券

    //定时任务：优惠券自动过期

    //冻结优惠券
    boolean frozenCoupon(Long couponId,Long orderId);

    //使用优惠券，先冻结后使用
    boolean useCoupon(Long couponId,Long orderId);

}
