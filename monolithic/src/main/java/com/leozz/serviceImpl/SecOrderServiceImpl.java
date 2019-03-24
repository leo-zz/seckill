package com.leozz.serviceImpl;

import com.leozz.dao.DeliveryAddrMapper;
import com.leozz.dto.PreSubmitOrderDTO;
import com.leozz.dto.ResultDTO;
import com.leozz.dto.SecOrderDto;
import com.leozz.dto.SubmitDTO;
import com.leozz.entity.*;
import com.leozz.service.PayService;
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

    @Autowired
    PayService payService;


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

        //优惠券信息（筛选满足条件的优惠券，按照优惠券类别和面额排序，面额大的放到前面）
        List<Coupon> coupons = couponLocalCache.selectUsableCouponByUserId(userId, seckillPrice.doubleValue());
        preSubmitOrderDTO.setCoupons(coupons);
        //TODO 优惠券的使用推荐，一个大面额的优惠券可能没有两个小面额的优惠券优惠的金额大，前期让用户自己选择所使用的优惠券
//        BigDecimal price = seckillPrice;
//        for (Coupon coupon : coupons) {
//            if (price.doubleValue() > coupon.getUsageLimit().doubleValue()) {
//                preSubmitOrderDTO.setFullrangeCoupon(coupon);
//                //不能使用lambda表达式，因为lambda表达式中不允许使用非final的局部变量。
//                switch (coupon.getType()) {
//                    case 0:
//                        price = price.divide(coupon.getCouponValue());
//                        break;
//                    case 1:
//
//                        preSubmitOrderDTO.setCoupon(coupon);
//                        break;
//                }
//            }
//        }
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
        BigDecimal seckillPrice = secActivity.getSeckillPrice();

        //同步，进行库存校验
        synchronized (secActivity) {
            Integer stockCount = secActivity.getSeckillStock();
            Integer blockedStockCount = secActivity.getSeckillBlockedStock();
            if (stockCount <= blockedStockCount) {
                return new ResultDTO(false, "库存不足");
            }
            // （公共数据）先在本地缓存中冻结库存，再批量提交到数据库
            activitiesLocalCache.updateBlockedStockById(activityId);
        }



        // 冻结优惠券（优惠券服务）
        //TODO 同一个用户同时只能抢购一个商品，使用优惠券和积分要加用户锁。
        //TODO 对用户信息、订单信息进行缓存。
        Long fullrangeCouponId = submitDTO.getFullrangeCouponId();
        Long couponId = submitDTO.getCouponId();
        if (fullrangeCouponId > 0) {
            Coupon fullrangeCoupon = couponLocalCache.selectById(fullrangeCouponId);
            int i = couponLocalCache.frozenCouponById(fullrangeCouponId);
            if (i != 1) {
                return new ResultDTO(false, "全品类优惠券不存在");
            }
            secOrderDto.setFullrangeCouponId(fullrangeCouponId);
            //达到优惠券使用标准，扣减订单价格
            if (seckillPrice.doubleValue() > fullrangeCoupon.getUsageLimit().doubleValue()) {
                seckillPrice = seckillPrice.divide(fullrangeCoupon.getCouponValue());
            }
            secOrderDto.setCouponUsage(true);
        }
        if (couponId > 0) {
            Coupon coupon = couponLocalCache.selectById(couponId);
            int i = couponLocalCache.frozenCouponById(couponId);
            if (i != 1) {
                return new ResultDTO(false, "单品优惠券不存在");
            }
            secOrderDto.setCouponId(couponId);
            //达到优惠券使用标准，扣减订单价格
            if (seckillPrice.doubleValue() > coupon.getUsageLimit().doubleValue()) {
                seckillPrice = seckillPrice.divide(coupon.getCouponValue());
            }
            if (!secOrderDto.getCouponUsage()) {
                secOrderDto.setCouponUsage(true);
            }
        }

        // 冻结积分（用户服务）
        int usedPoint = submitDTO.getUsedPoint();
        if (usedPoint > 0) {
            User user = userLocalCache.selectUserById(userId);
            Integer point = user.getMembershipPoint();
            Integer blockedPoint = user.getBlockedMembershipPoint();
            if (point < usedPoint + blockedPoint) {
                return new ResultDTO(false, "用户积分不足");
            }
            user.setBlockedMembershipPoint(usedPoint + blockedPoint);
            userLocalCache.updateUserPointById(userId);
            //扣减订单价格
            seckillPrice = seckillPrice.divide(new BigDecimal(usedPoint / 100));
            secOrderDto.setPointUsage(true);
            secOrderDto.setUsedPoint(usedPoint);
        }

        secOrderDto.setUserId(userId);
        secOrderDto.setActivityId(activityId);
        secOrderDto.setCreateDate(new Date());
        secOrderDto.setDeliveryAddrId(submitDTO.getDeliveryAddrId());
        secOrderDto.setStatus((byte) 0);
        secOrderDto.setOrderChannel((byte) 0);
        secOrderDto.setAmount(seckillPrice);

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
        SecOrderDto order = orderLocalCache.getSecOrderDtoById(orderId);
        double orderAmount = order.getAmount().doubleValue();//支付金额
        //供支付渠道回调，更新订单支付状态。
        Long activityId = order.getActivityId();
        order.setStatus((byte) 1);
        order.setPayDate(new Date());
        int num = orderLocalCache.updateOrderById(orderId);
        if (num != 1) {
            return new ResultDTO(false, "订单支付失败");
        }

        //扣除活动中的库存
        SecActivity secActivity = activitiesLocalCache.getSecActivityById(activityId);
        synchronized (secActivity) {
            Integer stockCount = secActivity.getSeckillStock();
            Integer blockedStockCount = secActivity.getSeckillBlockedStock();
            if (stockCount >= blockedStockCount & blockedStockCount > 0) {
                // （公共数据）先在本地缓存中冻结库存，
                secActivity.setSeckillStock(--stockCount);//扣减库存
                secActivity.setSeckillBlockedStock(--blockedStockCount);//扣减冻结库存
            } else {
                return new ResultDTO(false, "商品库存出现异常");
            }
            //判断活动是否抢完
            if (stockCount == blockedStockCount) {
                if (stockCount == 0) {
                    secActivity.setStatus((byte) 4); //商品已经卖完，活动结束
                    secActivity.setEndDate(new Date()); //记录活动的持续时间，便于分析数据
                }
                secActivity.setStatus((byte) 3); //商品都被锁定，没有可卖库存，除非有人取消订单。
            }
        }
        // 批量提交到数据库
        activitiesLocalCache.updateById(activityId);

        //扣除优惠券
        if (order.getCouponUsage()) {
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
        if (order.getPointUsage()) {
            Long userId = order.getUserId();
            Integer usedPoint = order.getUsedPoint();
            User user = userLocalCache.selectUserById(userId);
            Integer point = user.getMembershipPoint();
            Integer blockedPoint = user.getBlockedMembershipPoint();
            if (point >= blockedPoint & blockedPoint >= usedPoint) {
                user.setMembershipPoint(point - usedPoint);
                user.setBlockedMembershipPoint(blockedPoint - usedPoint);
                userLocalCache.updateUserPointById(userId);
            } else {
                return new ResultDTO(false, "用户积分不足");
            }
        }
        //创建运单，不是必须要实时反馈，可以异步执行。
        boolean wayBill = wayBillService.createWayBill(order);
        if (!wayBill) {
            return new ResultDTO(false, "物流单创建失败");
        }
        return new ResultDTO(true, "订单支付成功");
    }
}
