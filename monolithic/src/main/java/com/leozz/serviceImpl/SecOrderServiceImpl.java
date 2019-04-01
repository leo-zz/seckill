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
        preSubmitOrderDTO.setStockPercent(secActivityService.calcStockPercent(secActivity));

        //商品信息省略，页面传递即可，无需从接口再次获取。
        // Goods goods = activitiesLocalCache.getGoodsByActivityId(secActivityId);
        //收件人信息（每个用户都不一样的信息直接从数据库拿，下同）
        DeliveryAddr addr = deliveryAddrMapper.selectDefaultByUserId(userId);
        if (addr == null) {
            throw new RuntimeException("请先设置收货地址");
        }
        preSubmitOrderDTO.setDeliveryAddr(addr);

        //优惠券信息（筛选满足条件的优惠券，按照优惠券类别和面额排序，面额大的放到前面）
        List<CouponTypeDTO> coupons = couponLocalCache.selectUsableCouponByUserId(userId, seckillPrice.doubleValue());
        if (coupons.size() > 0) {
            preSubmitOrderDTO.setCoupons(coupons);
        }
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

        //0、检查用户是否已经参与过该活动
        synchronized (user) {
            boolean b = secActivityService.hasUserPartaked(activityId, userId);
            if (b) {
                throw new RuntimeException("只能参与一次");
            }
        }

        SecOrderDto secOrderDto = new SecOrderDto();
        SecActivity secActivity = activitiesLocalCache.getSecActivityById(activityId);
        BigDecimal seckillPrice = secActivity.getSeckillPrice();

        //TODO 校验订单价格是否存在问题

        // 1.冻结库存（活动服务）
        blockActivityStock(secActivity);

        // 2.冻结优惠券（优惠券服务）
        seckillPrice = frozenCoupon(submitDTO, secOrderDto, seckillPrice);

        // 3.冻结积分（用户服务）
        seckillPrice = frozenUserPoint(submitDTO, secOrderDto, seckillPrice);

        // 4.核对订单金额是否有误
        double orderAmount = submitDTO.getOrderAmount();
        if (seckillPrice.doubleValue() != orderAmount) {
            //TODO 回滚库存冻结、优惠券冻结、积分冻结
            throw new RuntimeException("订单创建失败，订单金额出现异常。接" +
                    "收到的订单金额为：" + orderAmount + "，系统计算订单金额为：" + seckillPrice);
        }
        // 5.保存并返回订单DTO
        return saveAndReturnOrderId(submitDTO, secOrderDto, seckillPrice);
    }


    @Override
    @Transactional
    public ResultDTO paytheOrder(long orderId, Long userId1) {
        //0.检查订单状态
        SecOrderDto order = getAndCheckOrder(orderId);
        Long activityId = order.getActivityId();
        //1.检查账号信息是否一致
        Long userId = order.getUserId();
        if (!userId.equals(userId1)) {
            throw new RuntimeException("账号信息不一致");
        }

        //2.支付并更新订单状态
        payAndUpdateOrder(orderId, order);

        //3.扣除活动中的库存,对抢购活动加锁，以防止超卖
        deductStockAndUpdateActivity(activityId);

        //4.扣除优惠券
        deductCoupons(order, userId);

        //5.扣除积分
        deductUserPoint(order, activityId, userId);

        //6.创建运单，不是必须要实时反馈，可以异步执行。
        wayBillService.createWayBill(order);

        //7.返还购物奖励积分（通常是在确认收货时进行）和优惠券。

        return new ResultDTO(true, "订单支付成功");
    }


    @Override
    /**
     * 取消订单，是提交订单的逆过程
     */
    @Transactional
    public ResultDTO cancleOrder(long orderId, Long userId1) {
        //0,检查订单状态
        SecOrderDto order = getAndCheckOrder(orderId);
        Long activityId = order.getActivityId();

        //1.检查账号信息是否一致
        Long userId = order.getUserId();
        if (!userId.equals(userId1)) {
            throw new RuntimeException("账号信息不一致");
        }

        //2.取消冻结库存
        unBlockActivityStock(activityId);

        //3.取消冻结优惠券
        unFrozenCoupon(order, userId);

        //4.取消冻结积分
        unFrozenUserPoint(order, activityId, userId);

        //5.更新订单状态
        return cancelAndUpdateOrder(order);
    }

    /**
     * 更新订单状态
     * @param order
     * @return
     */
    private ResultDTO cancelAndUpdateOrder(SecOrderDto order) {
        order.setStatus((byte) 4);
        int num = orderLocalCache.updateOrderById(order.getId());
        if (num == 1) {
            return new ResultDTO(true, "订单取消成功");
        } else {
            //TODO 回滚库存冻结、优惠券冻结、积分冻结、订单插入缓存等
            throw new RuntimeException("订单取消失败");
        }
    }

    /**
     * 取消冻结积分
     * @param order
     * @param activityId
     * @param userId
     */
    private void unFrozenUserPoint(SecOrderDto order, Long activityId, Long userId) {
        //检查订单是否使用用户积分
        if(!order.getPointUsage())return;

        User user = userLocalCache.selectUserById(userId);
        int usedPoint = order.getUsedPoint();
        if (usedPoint > 0) {
            //加锁
            synchronized (user) {
                Integer blockedPoint = user.getBlockedMembershipPoint();
                if (usedPoint <= blockedPoint) {
                    //要取消冻结的积分值肯定小于或等于总的冻结积分值
                    throw new RuntimeException("用户积分出现异常");
                }
                user.setBlockedMembershipPoint(blockedPoint-usedPoint);

                //记录积分变更
                PointRecord record = new PointRecord();
                record.setActivityId(activityId);
                record.setUserId(userId);
                record.setCause((byte)1);//购物扣除积分

                record.setStatus((byte) 3); //状态3表示已撤销
                record.setUpdateDate(new Date());
                userLocalCache.unfrozenUserPointById(userId, record);
            }
        }
    }

    /**
     * 取消冻结优惠券
     * @param order
     * @param userId
     */
    private void unFrozenCoupon(SecOrderDto order, Long userId) {
        //检查订单是否使用优惠券
        if(!order.getCouponUsage()){
            return;
        }
        User user = userLocalCache.selectUserById(userId);
        Long fullrangeCouponId = order.getFullrangeCouponId();
        Long couponId = order.getCouponId();
        if (fullrangeCouponId > 0)
            unfrozenCouponById(user, fullrangeCouponId, true);
        if (couponId > 0)
            unfrozenCouponById(user, couponId, false);
    }

    private void unfrozenCouponById(User user, Long couponId, boolean isFullrange) {
        Long userId = user.getId();
        synchronized (user) {
            long i = couponLocalCache.unfrozenCouponById(couponId, userId);
            if (i != 1) {
                //TODO 回滚库存冻结和优惠券冻结
                throw new RuntimeException(isFullrange ? "全品类优惠券取消冻结出现异常" : "指定品类优惠券取消冻结出现异常");
            }
        }
    }

    /**
     * 取消冻结库存
     * @param activityId
     */
    private void unBlockActivityStock(Long activityId) {
        SecActivity secActivity = activitiesLocalCache.getSecActivityById(activityId);
        synchronized (secActivity) {
            //同步，进行库存校验
            Integer seckillCount = secActivity.getSeckillCount();
            Integer stockCount = secActivity.getSeckillStock();
            if (seckillCount <= stockCount) {
                //抛出异常，回滚。
                throw new RuntimeException("库存出现异常");
            }
            // （公共数据）先在本地缓存中冻结库存，再批量提交到数据库
            activitiesLocalCache.updateBlockedStockById(activityId, true);
        }
    }

    /**
     * 保存并返回订单DTO
     *
     * @param submitDTO
     * @param secOrderDto
     * @param seckillPrice
     * @return
     */
    private ResultDTO<Long> saveAndReturnOrderId(SubmitDTO submitDTO, SecOrderDto secOrderDto, BigDecimal seckillPrice) {
        Long activityId = submitDTO.getActivityId();
        Long userId = submitDTO.getUserId();

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

    /**
     * 检查并冻结用户积分
     *
     * @param submitDTO
     * @param secOrderDto
     * @param seckillPrice
     * @return
     */
    private BigDecimal frozenUserPoint(SubmitDTO submitDTO, SecOrderDto secOrderDto, BigDecimal seckillPrice) {
        Long activityId = submitDTO.getActivityId();
        Long userId = submitDTO.getUserId();
        User user = userLocalCache.selectUserById(userId);
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

                //记录积分变更
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
        } else {
            secOrderDto.setPointUsage(false);
        }
        return seckillPrice;
    }

    /**
     * 检查优惠券的使用情况，并冻结相应的优惠券
     *
     * @param submitDTO
     * @param secOrderDto
     * @param seckillPrice
     * @return
     */
    private BigDecimal frozenCoupon(SubmitDTO submitDTO, SecOrderDto secOrderDto, BigDecimal seckillPrice) {
        //TODO 同一个用户同时只能抢购一个商品，使用优惠券和积分要加用户锁。
        //TODO 对用户信息、订单信息进行缓存。
        Long activityId = submitDTO.getActivityId();
        Long userId = submitDTO.getUserId();
        User user = userLocalCache.selectUserById(userId);
        //拿到优惠券的ID
        Long fullrangeCouponId = submitDTO.getFullrangeCouponId();
        Long couponId = submitDTO.getCouponId();
        secOrderDto.setCouponUsage(false);
        if (fullrangeCouponId > 0)
            seckillPrice = frozenCouponById(activityId, user, secOrderDto, seckillPrice, fullrangeCouponId, true);
        if (couponId > 0)
            seckillPrice = frozenCouponById(activityId, user, secOrderDto, seckillPrice, couponId, false);
        return seckillPrice;
    }

    /**
     * 检查优惠券是否可用，如果可用则将优惠券的信息set到dto中，并且扣减订单的金额。
     *
     * @param activityId
     * @param user
     * @param secOrderDto
     * @param seckillPrice
     * @param couponId
     * @param isFullrange  是否为全品类券
     * @return
     */
    private BigDecimal frozenCouponById(Long activityId, User user, SecOrderDto secOrderDto, BigDecimal seckillPrice, Long couponId, boolean isFullrange) {
        Long userId = user.getId();
        Long couponTypeId;
        synchronized (user) {
            couponTypeId = couponLocalCache.frozenCouponById(couponId, userId, activityId);
            if (couponTypeId < 1) {
                //TODO 回滚库存冻结和优惠券冻结
                throw new RuntimeException(isFullrange ? "全品类优惠券不可用" : "指定品类优惠券不可用");
            }
        }
        if (isFullrange) {
            secOrderDto.setFullrangeCouponId(couponId);
        } else {
            secOrderDto.setCouponId(couponId);
        }
        //根据优惠券的ID，拿到优惠券类型的信息
        CouponType couponType = couponLocalCache.selectCouponById(couponTypeId);
        //达到优惠券使用标准，扣减订单价格
        if (seckillPrice.doubleValue() > couponType.getUsageLimit().doubleValue()) {
            //divide是除法，subtract是减法
            seckillPrice = seckillPrice.subtract(couponType.getCouponValue());
        }
        secOrderDto.setCouponUsage(true);
        return seckillPrice;
    }

    /**
     * 检查库存是否充足，防止超卖，如果库存充足则冻结库存
     *
     * @param secActivity
     */
    private void blockActivityStock(SecActivity secActivity) {
        Long activityId = secActivity.getId();
        synchronized (secActivity) {
            //同步，进行库存校验
            Integer stockCount = secActivity.getSeckillStock();
            Integer blockedStockCount = secActivity.getSeckillBlockedStock();
            if (stockCount <= blockedStockCount) {
                //抛出异常，回滚。
                throw new RuntimeException("库存不足");
            }
            // （公共数据）先在本地缓存中冻结库存，再批量提交到数据库
            activitiesLocalCache.updateBlockedStockById(activityId, false);
        }
    }

    /**
     * 如果订单使用了用户积分，则扣除积分，并进行记录
     *
     * @param order
     * @param activityId
     * @param userId
     */
    private void deductUserPoint(SecOrderDto order, Long activityId, Long userId) {
        if (order.getPointUsage()) {

            Integer usedPoint = order.getUsedPoint();
            User user = userLocalCache.selectUserById(userId);
            Integer point = user.getMembershipPoint();
            Integer blockedPoint = user.getBlockedMembershipPoint();
            if (point >= blockedPoint & blockedPoint >= usedPoint) {
                user.setMembershipPoint(point - usedPoint);
                user.setBlockedMembershipPoint(blockedPoint - usedPoint);

                //创建用户积分使用记录
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
    }

    /**
     * 如果订单使用了优惠券，则扣除优惠券
     *
     * @param order
     * @param userId
     */
    private void deductCoupons(SecOrderDto order, Long userId) {
        if (order.getCouponUsage()) {
            Long fullrangeCouponId = order.getFullrangeCouponId();
            if (fullrangeCouponId > 0) {
                long i = couponLocalCache.deductCouponById(fullrangeCouponId, userId);
                if (i != 1) {
                    throw new RuntimeException("全品类优惠券不存在");
                }
            }
            Long couponId = order.getCouponId();
            if (couponId > 0) {
                long i = couponLocalCache.deductCouponById(couponId, userId);
                if (i != 1) {
                    throw new RuntimeException("单品优惠券不存在");
                }
            }
        }
    }

    /**
     * 扣除活动中的库存,对抢购活动加锁，以防止超卖;
     * 如果所有商品订单都已支付，那么活动提前结束
     *
     * @param activityId
     */
    private void deductStockAndUpdateActivity(Long activityId) {
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
    }

    /**
     * 支付并更新订单状态
     *
     * @param orderId
     * @param order
     */
    private void payAndUpdateOrder(long orderId, SecOrderDto order) {
        String orderAmount = order.getAmount().toString();//支付金额
        //供支付渠道回调，更新订单支付状态。
        //省略
        System.out.println("支付金额：" + orderAmount);
        order.setStatus((byte) 1);
        order.setPayDate(new Date());
        int num = orderLocalCache.updateOrderById(orderId);
        if (num != 1) {
            throw new RuntimeException("订单支付失败");
        }
    }

    /**
     * 检查订单状态，如果是待支付状态，
     * 可以进行付款或取消订单操作
     *
     * @param orderId
     * @return
     */
    private SecOrderDto getAndCheckOrder(long orderId) {
        //检查订单状态
        SecOrderDto order = orderLocalCache.getSecOrderDtoById(orderId);
        if (order.getStatus() > 0) {
            throw new RuntimeException("订单状态错误");
        }
        return order;
    }

}
