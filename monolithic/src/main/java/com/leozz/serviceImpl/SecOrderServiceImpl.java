package com.leozz.serviceImpl;

import com.leozz.dao.DeliveryAddrMapper;
import com.leozz.dto.*;
import com.leozz.entity.*;
import com.leozz.service.PayService;
import com.leozz.service.SecActivityService;
import com.leozz.service.SecOrderService;
import com.leozz.service.WayBillService;
import com.leozz.util.cache.ActivitiesLocalCache;
import com.leozz.util.cache.CouponLocalCache;
import com.leozz.util.cache.OrderLocalCache;
import com.leozz.util.cache.UserLocalCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    SecActivityService secActivityService;


    @Override
    public PreSubmitOrderDTO preSubmitOrder(Long secActivityId, Long userId) {
        PreSubmitOrderDTO preSubmitOrderDTO = new PreSubmitOrderDTO(true, "预下单成功");

        //从缓存中拿取秒杀活动、商品信息（所有用户都会用到的信息放到缓存中）
        SecActivity secActivity = activitiesLocalCache.getSecActivityById(secActivityId);
        BigDecimal seckillPrice = secActivity.getSeckillPrice();
        long countDownTime = secActivity.getEndDate().getTime() - System.currentTimeMillis();
        preSubmitOrderDTO.setCountDownTime(countDownTime > 0 ? countDownTime : 0);
        Integer seckillCount = secActivity.getSeckillCount();
        Integer seckillStock = secActivity.getSeckillStock();
        Integer seckillBlockedStock = secActivity.getSeckillBlockedStock();
        preSubmitOrderDTO.setStockPercent((seckillStock-seckillBlockedStock)*100/seckillCount);

        //商品信息省略，页面传递即可，无需从接口再次获取。
        // Goods goods = activitiesLocalCache.getGoodsByActivityId(secActivityId);

        //收件人信息（每个用户都不一样的信息直接从数据库拿，下同）
        DeliveryAddr addr = deliveryAddrMapper.selectDefaultByUserId(userId);

        preSubmitOrderDTO.setDeliveryAddr(addr);

        //优惠券信息（筛选满足条件的优惠券，按照优惠券类别和面额排序，面额大的放到前面）
        List<CouponTypeDTO> coupons = couponLocalCache.selectUsableCouponByUserId(userId, seckillPrice.doubleValue());
        preSubmitOrderDTO.setCoupons(coupons);
        //TODO 优惠券的使用推荐，一个大面额的优惠券可能没有两个小面额的优惠券优惠的金额大，前期让用户自己选择所使用的优惠券
        //积分信息
        User user = userLocalCache.selectUserById(userId);
        Integer point = user.getMembershipPoint();
        Integer blockedPoint = user.getBlockedMembershipPoint();
        preSubmitOrderDTO.setPoint(point - blockedPoint);

        return preSubmitOrderDTO;
    }

    @Override
    //事务，如果订单未提交，那么对于优惠券和积分的冻结不能提交到数据库。
    //TODO引入事务后，就必须要在需要回滚的点抛出异常，并且增加异常管理。
    // 即以抛出非受检异常的方式返回错误信息
    @Transactional
    public ResultDTO<Long> submitOrder(SubmitDTO submitDTO) {
        Long activityId = submitDTO.getActivityId();
        Long userId = submitDTO.getUserId();
        User user = userLocalCache.selectUserById(userId);
        SecOrderDto secOrderDto = new SecOrderDto();

        //0、检查用户是否已经参与过该活动
        synchronized (user) {
            boolean b = secActivityService.hasUserPartaked(activityId, userId);
            if (b) {
                throw new RuntimeException("只能参与一次");
            }
        }

        // 1.冻结库存（活动服务）
        SecActivity secActivity = activitiesLocalCache.getSecActivityById(activityId);
        BigDecimal seckillPrice = secActivity.getSeckillPrice();

        //TODO 校验订单价格是否存在问题
        //1.1同步，进行库存校验
        synchronized (secActivity) {
            Integer stockCount = secActivity.getSeckillStock();
            Integer blockedStockCount = secActivity.getSeckillBlockedStock();
            if (stockCount <= blockedStockCount) {
                //抛出异常，回滚。
                throw new RuntimeException("库存不足");
            }
            // （公共数据）先在本地缓存中冻结库存，再批量提交到数据库
            activitiesLocalCache.updateBlockedStockById(activityId);
        }

        // 2.冻结优惠券（优惠券服务）
        //TODO 同一个用户同时只能抢购一个商品，使用优惠券和积分要加用户锁。
        //TODO 对用户信息、订单信息进行缓存。
        Long fullrangeCouponId = submitDTO.getFullrangeCouponId();
        Long couponId = submitDTO.getCouponId();
        secOrderDto.setCouponUsage(false);
        if (fullrangeCouponId > 0) {
            CouponType fullrangeCouponType = couponLocalCache.selectCouponById(fullrangeCouponId);
            synchronized (user) {
                int i = couponLocalCache.frozenCouponById(fullrangeCouponId, userId, activityId);
                if (i != 1) {
                    //TODO 回滚库存冻结和优惠券冻结
                    throw new RuntimeException("全品类优惠券不可用");
                }
            }
            secOrderDto.setFullrangeCouponId(fullrangeCouponId);
            //达到优惠券使用标准，扣减订单价格
            if (seckillPrice.doubleValue() > fullrangeCouponType.getUsageLimit().doubleValue()) {
                //divide是除法，subtract是减法
                seckillPrice = seckillPrice.subtract(fullrangeCouponType.getCouponValue());
            }
            secOrderDto.setCouponUsage(true);
        }
        if (couponId > 0) {
            CouponType couponType = couponLocalCache.selectCouponById(couponId);
            synchronized (user) {
                int i = couponLocalCache.frozenCouponById(couponId, userId, activityId);
                if (i != 1) {
                    //TODO 回滚库存冻结和优惠券冻结
                    throw new RuntimeException("单品优惠券不可用");
                }
            }
            secOrderDto.setCouponId(couponId);
            //达到优惠券使用标准，扣减订单价格
            if (seckillPrice.doubleValue() > couponType.getUsageLimit().doubleValue()) {
                seckillPrice = seckillPrice.subtract(couponType.getCouponValue());
            }
            if (!secOrderDto.getCouponUsage()) {
                secOrderDto.setCouponUsage(true);
            }
        }

        // 3.冻结积分（用户服务）
        int usedPoint = submitDTO.getUsedPoint();
        if (usedPoint > 0) {
            //加锁
            synchronized (user) {
                Integer point = user.getMembershipPoint();
                Integer blockedPoint = user.getBlockedMembershipPoint();
                if (point < usedPoint + blockedPoint) {
                    //TODO 回滚库存冻结和优惠券冻结
                    throw new RuntimeException("用户积分不足");
                }

                user.setBlockedMembershipPoint(usedPoint + blockedPoint);

                PointRecord record = new PointRecord();
                record.setActivityId(activityId);
                record.setUserId(userId);
                record.setCause((byte) 1);//购物扣除积分
                record.setUpdateAmount(usedPoint);
                record.setStatus((byte) 0);
                record.setUpdateDate(new Date());

                userLocalCache.frozenUserPointById(userId, record);
                //扣减订单价格
                seckillPrice = seckillPrice.subtract(new BigDecimal(usedPoint / 100));
                secOrderDto.setPointUsage(true);
                secOrderDto.setUsedPoint(usedPoint);
            }
        }else {
            secOrderDto.setPointUsage(false);
        }

        double orderAmount = submitDTO.getOrderAmount();
        if (seckillPrice.doubleValue() != orderAmount) {
            //TODO 回滚库存冻结、优惠券冻结、积分冻结
            throw new RuntimeException("订单创建失败，订单金额出现异常。接" +
                    "收到的订单金额为：" + orderAmount + "，系统计算订单金额为：" + seckillPrice);
        }
        secOrderDto.setUserId(userId);
        secOrderDto.setActivityId(activityId);
        secOrderDto.setCreateDate(new Date());
        secOrderDto.setDeliveryAddrId(submitDTO.getDeliveryAddrId());
        secOrderDto.setStatus((byte) 0);
        secOrderDto.setOrderChannel(submitDTO.getOrderChannel());
        secOrderDto.setAmount(seckillPrice);

        //放入订单缓存中
        int num = orderLocalCache.insert(secOrderDto);
        if (num == 1) {
            return new ResultDTO<Long>(true, secOrderDto.getId(), "订单创建成功");
        } else {
            //TODO 回滚库存冻结、优惠券冻结、积分冻结、订单插入缓存等
            throw new RuntimeException("订单创建失败");
        }
    }

    @Override
    @Transactional
    public ResultDTO paytheOrder(long orderId, Long userId1) {
        SecOrderDto order = orderLocalCache.getSecOrderDtoById(orderId);
        if (order.getStatus() > 0) {
            throw new RuntimeException("订单状态错误");
        }
        Long userId = order.getUserId();

        if (!userId.equals(userId1)) {
            throw new RuntimeException("账号信息不一致");
        }

        double orderAmount = order.getAmount().doubleValue();//支付金额
        //供支付渠道回调，更新订单支付状态。
        Long activityId = order.getActivityId();
        order.setStatus((byte) 1);
        order.setPayDate(new Date());
        int num = orderLocalCache.updateOrderById(orderId);
        if (num != 1) {
            throw new RuntimeException("订单支付失败");
        }

        //扣除活动中的库存,对抢购活动加锁
        SecActivity secActivity = activitiesLocalCache.getSecActivityById(activityId);
        synchronized (secActivity) {
            Integer stockCount = secActivity.getSeckillStock();
            Integer blockedStockCount = secActivity.getSeckillBlockedStock();
            if (stockCount >= blockedStockCount & blockedStockCount > 0) {
                // （公共数据）先在本地缓存中冻结库存，
                secActivity.setSeckillStock(--stockCount);//扣减库存
                secActivity.setSeckillBlockedStock(--blockedStockCount);//扣减冻结库存
            } else {
                throw new RuntimeException("商品库存出现异常");
            }
            //判断活动是否抢完
            if (stockCount == 0) {
                secActivity.setEndDate(new Date()); //记录活动的持续时间，便于分析数据
            }
            // 批量提交到数据库,考虑是否可以拿到同步范围外面
            activitiesLocalCache.updateAfterPayOrder(activityId);
        }

        //扣除优惠券
        if (order.getCouponUsage()) {
            Long fullrangeCouponId = order.getFullrangeCouponId();
            if (fullrangeCouponId > 0) {
                int i = couponLocalCache.deductCouponById(fullrangeCouponId, userId);
                if (i != 1) {
                    throw new RuntimeException("全品类优惠券不存在");
                }
            }
            Long couponId = order.getCouponId();
            if (couponId > 0) {
                int i = couponLocalCache.deductCouponById(couponId, userId);
                if (i != 1) {
                    throw new RuntimeException("单品优惠券不存在");
                }
            }
        }
        //扣除积分
        if (order.getPointUsage()) {

            Integer usedPoint = order.getUsedPoint();
            User user = userLocalCache.selectUserById(userId);
            Integer point = user.getMembershipPoint();
            Integer blockedPoint = user.getBlockedMembershipPoint();
            if (point >= blockedPoint & blockedPoint >= usedPoint) {
                user.setMembershipPoint(point - usedPoint);
                user.setBlockedMembershipPoint(blockedPoint - usedPoint);

                PointRecord record = new PointRecord();
                record.setActivityId(activityId);
                record.setUserId(userId);
                record.setCause((byte) 1);//购物扣除积分

                record.setStatus((byte) 1);
                record.setUpdateDate(new Date());

                userLocalCache.deductUserPointById(userId, record);
            } else {
                throw new RuntimeException("用户积分不足");
            }
        }
        //创建运单，不是必须要实时反馈，可以异步执行。
        //创建失败的话不用回滚订单，物流单可以通过MQ确保创建成功，
        boolean wayBill = wayBillService.createWayBill(order);
        if (!wayBill) {
            return new ResultDTO(false, "物流单创建失败");
        }
        return new ResultDTO(true, "订单支付成功");
    }

    @Override
    public ResultDTO cancletheOrder(long orderId, Long userId) {
        return null;
    }
}
