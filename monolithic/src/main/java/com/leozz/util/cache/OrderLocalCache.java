package com.leozz.util.cache;

import com.leozz.dao.PointRecordMapper;
import com.leozz.dao.SecOrderMapper;
import com.leozz.dao.UserCouponRecordMapper;
import com.leozz.dto.SecOrderDto;
import com.leozz.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: leo-zz
 * @Date: 2019/3/18 13:19
 */
@Component
public class OrderLocalCache {

    @Autowired
    CouponLocalCache couponLocalCache;

    @Autowired
    UserLocalCache userLocalCache;

    @Autowired
    SecOrderMapper orderMapper;

    @Autowired
    PointRecordMapper pointRecordMapper;

    @Autowired
    UserCouponRecordMapper userCouponRecordMapper;

    private Map<Long, SecOrderDto> orderMap = new ConcurrentHashMap<>();

    /**
     * 对用户操作进行加锁，避免用户重复下单
     * 先检查缓存，再检查数据库
     *
     * @param secActivityId
     * @param userId
     * @return
     */
    public int selectByUserAndActivity(Long secActivityId, Long userId) {
        User user = userLocalCache.selectUserById(userId);
        Map<String, Long> paramMap = new HashMap<>(2);
        paramMap.put("secActivityId", secActivityId);
        paramMap.put("userId", userId);
        synchronized (user) {
            return orderMapper.selectOrderCountByUserAndActivity(paramMap);
        }
    }

    public int insert(SecOrderDto secOrder) {
        orderMap.put(secOrder.getId(), secOrder);//放入缓存，
        // 是否异步存入数据库   UserDefThreadPool.jobExecutor.execute(() -> {
        return orderMapper.insert(secOrder);
//        });
    }

    public SecOrderDto getSecOrderDtoById(long orderId) {
        SecOrderDto secOrderDto = null;
        secOrderDto = orderMap.get(orderId);
        if (secOrderDto == null) {
            //查找订单信息
            SecOrder secOrder = orderMapper.selectByPrimaryKey(orderId);
            secOrderDto = new SecOrderDto(secOrder);
            //查找优惠券
            if (secOrder.getCouponUsage()) {
                List<UserCouponRecord> records = userCouponRecordMapper.selectRecordsByOrder(secOrder.getId());

                for (UserCouponRecord record : records) {
                    Long couponId = record.getCouponId();
                    Coupon coupon = couponLocalCache.selectById(couponId);
                    switch (coupon.getType()) {
                        case 0:
                            secOrderDto.setFullrangeCouponId(couponId);
                            break;
                        case 1:
                            secOrderDto.setCouponId(couponId);
                            break;
                    }
                }
            }
            //查找积分信息
            if (secOrder.getPointUsage()) {
                PointRecord record = pointRecordMapper.selectRecordByOrder(secOrder.getId());
                secOrderDto.setUsedPoint(record.getUpdateAmount());
            }
        }
        return secOrderDto;

    }

    /**
     * 写入本地缓存并批量写入数据库
     *
     * @param orderId
     * @return
     */
    public int saveOrderToDBById(long orderId) {
        SecOrderDto secOrderDto = orderMap.get(orderId);
        return orderMapper.updateByPrimaryKeySelective(secOrderDto);
    }
}
