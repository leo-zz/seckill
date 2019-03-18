package com.leozz.serviceImpl;

import com.leozz.dao.DeliveryAddrMapper;
import com.leozz.dto.PreSubmitOrderDTO;
import com.leozz.dto.ResultDTO;
import com.leozz.dto.SecOrderDto;
import com.leozz.dto.SubmitDTO;
import com.leozz.entity.*;
import com.leozz.service.SecOrderService;
import com.leozz.service.WayBillService;
import com.leozz.util.cache.ActivitiesLocalCache;
import com.leozz.util.cache.CouponLocalCache;
import com.leozz.util.cache.OrderLocalCache;
import com.leozz.util.cache.UserLocalCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @Author: leo-zz
 * @Date: 2019/3/14 10:11
 */
@Service
public class SecOrderServiceImpl implements SecOrderService {

    @Autowired
    DeliveryAddrMapper deliveryAddrMapper;

    @Autowired
    ActivitiesLocalCache activitiesLocalCache;

    @Autowired
    CouponLocalCache couponLocalCache;

    @Autowired
    OrderLocalCache orderLocalCache;

    @Autowired
    UserLocalCache userLocalCache;

    @Autowired
    WayBillService wayBillService;


    @Override
    public boolean hasUserPlacedOrder(Long secActivityId, Long userId) {
        //查询用户是否存在秒杀活动对应的订单
        //TODO 需要加锁，避免下单后数据库更新订单的时间差内，有用户重复下单；比如给用户添加一个秒杀锁，一个用户只能参与一个秒杀活动。
        int i = orderLocalCache.selectByUserAndActivity(secActivityId, userId);
        return i > 0;
    }

    @Override
    public PreSubmitOrderDTO preSubmitOrder(Long secActivityId, Long userId) {
        PreSubmitOrderDTO preSubmitOrderDTO = new PreSubmitOrderDTO(true, "预下单成功");

        //从缓存中拿取秒杀活动、商品信息（所有用户都会用到的信息放到缓存中）
        SecActivity secActivity = activitiesLocalCache.getSecActivityById(secActivityId);
        BigDecimal seckillPrice = secActivity.getSeckillPrice();
        long countDownTime = secActivity.getEndDate().getTime() - System.currentTimeMillis();
        preSubmitOrderDTO.setCountDownTime(countDownTime > 0 ? countDownTime : 0);
        preSubmitOrderDTO.setStockPercent(secActivity.getStockPercent());

        //商品信息省略，页面传递即可，无需从接口再次获取。
        // Goods goods = activitiesLocalCache.getGoodsByActivityId(secActivityId);

        //收件人信息（每个用户都不一样的信息直接从数据库拿，下同）
        DeliveryAddr addr = deliveryAddrMapper.selectDefaultByUserId(userId);

        preSubmitOrderDTO.setDeliveryAddr(addr);

        //优惠券信息（筛选满足条件的优惠券，按照优惠券类别和面额排序，）
        List<Coupon> coupons = couponLocalCache.selectUsableCouponByUserId(userId, seckillPrice.doubleValue());
        coupons.forEach(coupon -> {
            switch (coupon.getType()) {
                case 0:
                    preSubmitOrderDTO.setFullrangeCoupon(coupon);
                    break;
                case 1:
                    preSubmitOrderDTO.setCoupon(coupon);
                    break;
            }
        });
        //积分信息
        User user = userLocalCache.selectUserById(userId);
        Integer point = user.getMembershipPoint();
        Integer blockedPoint = user.getBlockedMembershipPoint();
        preSubmitOrderDTO.setPoint(point - blockedPoint);

        return preSubmitOrderDTO;
    }

    @Override
    //TODO 涉及事务的嵌套
    public ResultDTO submitOrder(SubmitDTO submitDTO) {
        Long activityId = submitDTO.getActivityId();
        Long userId = submitDTO.getUserId();
        SecOrderDto secOrderDto = new SecOrderDto();


        // 冻结库存（活动服务）
        SecActivity secActivity = activitiesLocalCache.getSecActivityById(activityId);
        //同步，进行库存校验
        synchronized (secActivity) {
            Integer stockCount = secActivity.getStockCount();
            Integer blockedStockCount = secActivity.getBlockedStockCount();
            if (!(stockCount > blockedStockCount)) {
                return new ResultDTO(false, "库存不足");
            }
            // （公共数据）先在本地缓存中冻结库存，
            secActivity.setBlockedStockCount(++blockedStockCount);//先加1后返回
        }
        // 批量提交到数据库，
        activitiesLocalCache.updateById(activityId);

        // 冻结优惠券（优惠券服务）
        //TODO 同一个用户同时只能抢购一个商品，使用优惠券和积分要加用户锁。
        //TODO 对用户信息、订单信息进行缓存。
        Long fullrangeCouponId = submitDTO.getFullrangeCouponId();
        Long couponId = submitDTO.getCouponId();
        if (fullrangeCouponId > 0) {
            int i = couponLocalCache.frozenCouponById(fullrangeCouponId);
            if (i != 1) {
                return new ResultDTO(false, "全品类优惠券不存在");
            }
            secOrderDto.setFullrangeCouponId(fullrangeCouponId);
            secOrderDto.setCouponUsage(true);
        }
        if (couponId > 0) {
            int i = couponLocalCache.frozenCouponById(couponId);
            if (i != 1) {
                return new ResultDTO(false, "单品优惠券不存在");
            }
            secOrderDto.setCouponId(couponId);
            if(!secOrderDto.isCouponUsage()){
                secOrderDto.setCouponUsage(true);
            }
        }

        // 冻结积分（用户服务）
        int usedPoint = submitDTO.getUsedPoint();
        if(usedPoint>0){
            User user = userLocalCache.selectUserById(userId);
            Integer point = user.getMembershipPoint();
            Integer blockedPoint = user.getBlockedMembershipPoint();
            if (point < usedPoint + blockedPoint) {
                return new ResultDTO(false, "用户积分不足");
            }
            user.setBlockedMembershipPoint(usedPoint + blockedPoint);
            userLocalCache.updateUserPointById(userId);
            secOrderDto.setPointUsage(true);
            secOrderDto.setUsedPoint(usedPoint);
        }

        secOrderDto.setUserId(userId);
        secOrderDto.setActivityId(activityId);
        secOrderDto.setCreateDate(new Date());
        secOrderDto.setDeliveryAddrId(submitDTO.getDeliveryAddrId());
        secOrderDto.setStatus((byte) 0);
        secOrderDto.setOrderChannel((byte) 0);
        secOrderDto.setAmount();
        //放入订单缓存中
        int num = orderLocalCache.insert(secOrderDto);
        if (num == 1) {
            return new ResultDTO(true, "订单创建成功");
        } else {
            return new ResultDTO(false, "订单创建失败");
        }
    }

    @Override
    public ResultDTO paytheOrder(long orderId) {
       SecOrderDto order= orderLocalCache.getOrderById(orderId);
        Long activityId = order.getActivityId();
        order.setStatus((byte)1);
        order.setPayDate(new Date());
        //供支付渠道回调，更新订单支付状态。
        int num =orderLocalCache.updateOrderById(orderId);
        if(num!=1){
            return new ResultDTO(false, "订单支付失败");
        }

        //扣除活动中的库存
        SecActivity secActivity = activitiesLocalCache.getSecActivityById(activityId);
        synchronized (secActivity){
            Integer stockCount = secActivity.getStockCount();
            Integer blockedStockCount = secActivity.getBlockedStockCount();
            if(stockCount>=blockedStockCount&blockedStockCount>0){
                // （公共数据）先在本地缓存中冻结库存，
                secActivity.setStockCount(--stockCount);//扣减库存
                secActivity.setBlockedStockCount(--blockedStockCount);//扣减冻结库存
            }else{
                return new ResultDTO(false, "商品库存出现异常");
            }
            //判断活动是否抢完
            if(stockCount==blockedStockCount){
                if(stockCount==0){
                    secActivity.setStatus((byte)4); //商品已经卖完，活动结束
                    secActivity.setEndDate(new Date()); //记录活动的持续时间，便于分析数据
                }
                secActivity.setStatus((byte)3); //商品都被锁定，没有可卖库存，除非有人取消订单。
            }
        }
        // 批量提交到数据库
        activitiesLocalCache.updateById(activityId);

        //扣除优惠券
        if(order.isCouponUsage()){
            Long fullrangeCouponId = order.getFullrangeCouponId();
            if (fullrangeCouponId > 0) {
                int i = couponLocalCache.frozenCouponById(fullrangeCouponId);
                if (i != 1) {
                    return new ResultDTO(false, "全品类优惠券不存在");
                }
            }
            Long couponId = order.getCouponId();
            if (couponId > 0) {
                int i = couponLocalCache.frozenCouponById(couponId);
                if (i != 1) {
                    return new ResultDTO(false, "单品优惠券不存在");
                }
            }
        }
        //扣除积分
        if(order.isPointUsage()){
            Integer usedPoint = order.getUsedPoint();
            User user = userLocalCache.selectUserById(order.getUserId());
            Integer point = user.getMembershipPoint();
            Integer blockedPoint = user.getBlockedMembershipPoint();
            if (point >=blockedPoint& blockedPoint>=usedPoint) {
                user.setMembershipPoint(point-usedPoint);
                user.setBlockedMembershipPoint(blockedPoint-usedPoint);
                userLocalCache.updateUserPointById(userId);
            }else {
                return new ResultDTO(false, "用户积分不足");
            }
        }
        //创建运单，不是必须要实时反馈，可以异步执行。
        boolean wayBill = wayBillService.createWayBill(order);
        if(!wayBill){
            return new ResultDTO(false, "物流单创建失败");
        }
        return new ResultDTO(true, "订单支付成功");
    }
}
