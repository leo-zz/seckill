package com.leozz.util.cache;

import com.leozz.dao.PointRecordMapper;
import com.leozz.dao.SecOrderMapper;
import com.leozz.dao.UserCouponRecordMapper;
import com.leozz.dto.SecOrderDto;
import com.leozz.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
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

    //存放order
    private Map<Long, SecOrderDto> orderMap = new ConcurrentHashMap<>();

    //用于存放用户和活动的对应关系
    private Set<String> activityUserSets = new HashSet<>();

    /**
     * 对用户操作进行加锁，避免用户重复下单
     * 先检查缓存，再检查数据库
     * TODO 场景中，绝大多数情况下是用户未参与过秒杀活动，如果按照上面的逻辑，每次都会查询数据库。先屏蔽掉缓存环节
     * @param secActivityId
     * @param userId
     * @return
     */
    public boolean selectByUserAndActivity(Long secActivityId, Long userId) {

//        boolean contains = activityUserSets.contains(secActivityId.toString() + userId.toString());
//        //如果缓存中有记录，则直接返回
//        if (contains) {
//            return contains;
//        }
        //尽可能的缩小加锁范围
        Map<String, Long> paramMap = new HashMap<>(2);
        paramMap.put("secActivityId", secActivityId);
        paramMap.put("userId", userId);
        User user = userLocalCache.selectUserById(userId);
        //如果没有记录，则再向数据库中确认一下
        synchronized (user) {
            return orderMapper.selectOrderCountByUserAndActivity(paramMap)>0;
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

    public int updateOrderById(long orderId) {
        return 0;
    }
}
