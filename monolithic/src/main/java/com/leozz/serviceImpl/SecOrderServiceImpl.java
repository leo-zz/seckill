package com.leozz.serviceImpl;

import com.leozz.dao.CouponMapper;
import com.leozz.dao.DeliveryAddrMapper;
import com.leozz.dao.SecOrderMapper;
import com.leozz.dao.UserMapper;
import com.leozz.dto.PreSubmitOrderDTO;
import com.leozz.dto.ResultDTO;
import com.leozz.dto.SubmitDTO;
import com.leozz.entity.*;
import com.leozz.service.SecOrderService;
import com.leozz.util.cache.ActivitiesLocalCache;
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
    SecOrderMapper secOrderMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    CouponMapper couponMapper;

    @Autowired
    DeliveryAddrMapper deliveryAddrMapper;

    @Autowired
    ActivitiesLocalCache activitiesLocalCache;

    @Override
    public boolean hasUserPlacedOrder(Long secActivityId, Long userId) {
        //查询用户是否存在秒杀活动对应的订单
        //TODO 需要加锁，避免下单后数据库更新订单的时间差内，有用户重复下单；比如给用户添加一个秒杀锁，一个用户只能参与一个秒杀活动。
        int i = secOrderMapper.selectByUserAndActivity(secActivityId, userId);
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
        preSubmitOrderDTO.setRecipientName(addr.getRecipientName());
        preSubmitOrderDTO.setRecipientTel(addr.getRecipientTel());
        preSubmitOrderDTO.setRecipientAddr(addr.getRecipientAddr());

        //优惠券信息（筛选满足条件的优惠券，按照优惠券类别和面额排序，）
        List<Coupon> coupons = couponMapper.selectUsableCouponByUserId(userId, seckillPrice.doubleValue());
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
        User user = userMapper.selectByPrimaryKey(userId);
        int point = user.getMembershipPoint();
        int frozenPoint = user.getFrozenMembershipPoint();
        preSubmitOrderDTO.setPoint(point - frozenPoint);

        return preSubmitOrderDTO;
    }

    @Override
    public ResultDTO submitOrder(SubmitDTO submitDTO) {
        Long activityId = submitDTO.getActivityId();
        Long userId = submitDTO.getUserId();
        SecOrder secOrder = new SecOrder();


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
        // 再刷新到数据库中，批量提交到数据库，
        activitiesLocalCache.addBlockedStockCountById(activityId);

        // 冻结优惠券（优惠券服务）
        //TODO 同一个用户同时只能抢购一个商品，使用优惠券和积分要加用户锁。
        //TODO 对用户信息、订单信息进行缓存。
        Long fullrangeCouponId = submitDTO.getFullrangeCouponId();
        Long couponId = submitDTO.getCouponId();
        if (fullrangeCouponId > 0) {
            int i = couponMapper.frozenCouponById(fullrangeCouponId);
            if (i != 1) {
                return new ResultDTO(false, "全品类优惠券不存在");
            }
        }
        if (couponId > 0) {
            int i = couponMapper.frozenCouponById(couponId);
            if (i != 1) {
                return new ResultDTO(false, "单品优惠券不存在");
            }
        }

        // 冻结积分（用户服务）
        int usedPoint = submitDTO.getUsedPoint();
        User user = userMapper.selectByPrimaryKey(userId);
        Integer point = user.getMembershipPoint();
        Integer blockedPoint = user.getBlockedMembershipPoint();
        if (point < usedPoint+blockedPoint) {
            return new ResultDTO(false, "用户积分不足");
        }
        userMapper.blockedPointById(usedPoint+blockedPoint);

        // 创建订单
        secOrder.setActivityId(activityId);
        secOrder.setCreateDate(new Date());
        secOrder.setDeliveryAddrId();

        return 0;
    }

    @Override
    public boolean paytheOrder(long orderId) {
        return false;
    }
}
