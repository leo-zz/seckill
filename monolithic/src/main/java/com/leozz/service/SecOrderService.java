package com.leozz.service;

import com.leozz.dto.PreSubmitOrderDTO;
import com.leozz.dto.ResultDTO;
import com.leozz.dto.SubmitDTO;
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
    boolean hasUserPlacedOrder(Long secActivityId, Long userId);

    /**
     * 获取预下单页面的数据，包括商品信息，收件人，优惠券，积分，秒杀活动（比如：秒杀倒计时，库存百分比，秒杀价格等），支付方式（略），配送信息（略）等信息。
     */
    PreSubmitOrderDTO preSubmitOrder(Long secActivityId, Long userId);

    /**
     *  提交订单的流程：冻结库存（活动服务），冻结优惠券（优惠券服务），冻结积分（用户服务），创建订单
     * @param submitDTO 订单信息，包含秒杀活动、商品、用户、支付方式、收件人信息等信息。
     * @return  创建后的订单号，如果返回-1，表示订单提交失败。
     */
    ResultDTO submitOrder(SubmitDTO submitDTO);

    /**
     *  支付订单的流程：扣减冻结库存（），使用冻结优惠券，扣减冻结积分，更新订单支付信息
     *  后续流程：异步创建运单、增加积分、
     * @param orderId 订单id
     * @return true表示订单支付成功
     */
    ResultDTO paytheOrder(long orderId);


}
