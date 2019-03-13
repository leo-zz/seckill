package com.leozz.service;

import com.leozz.entity.SecOrder;

/**
 * @Author: leo-zz
 * @Date: 2019/3/13 10:50
 */
public interface SecOrderService {


    /**
     *  检查用户是否已经参与过指定活动。
     *  确保一次秒杀活动中，同一个用户只能参与一次
     * @param secActivityId 活动编号的id
     * @param userId 用户Id
     * @return true表示用户已经下单，flase表示用户未下单
     */
    boolean hasUserPlacedtheOrder(Long secActivityId,Long userId);


    /**
     *  提交订单的流程：冻结库存（活动服务），冻结优惠券（优惠券服务），冻结积分（用户服务），创建订单
     * @param order 订单信息，包含秒杀活动、商品、用户、支付方式、收件人信息等信息。
     * @return  创建后的订单号，如果返回-1，表示订单提交失败。
     */
    int submittheOrder(SecOrder order,long couponIDs);

    /**
     *  支付订单的流程：扣减冻结库存（），使用冻结优惠券，扣减冻结积分，更新订单支付信息
     *  后续流程：异步创建运单、增加积分、
     * @param orderId 订单id
     * @return true表示订单支付成功
     */
    boolean paytheOrder(long orderId);


}
