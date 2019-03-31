package com.leozz.util.cache;

import com.leozz.dao.CouponMapper;
import com.leozz.dao.UserCouponRecordMapper;
import com.leozz.dto.CouponTypeDTO;
import com.leozz.entity.CouponType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 优惠券类型信息的本地缓存，用户拥有优惠券的信息仍然是从数据库查询
 *
 * @Author: leo-zz
 * @Date: 2019/3/18 10:04
 */
@Component
public class CouponLocalCache {


    @Autowired
    CouponMapper couponMapper;

    @Autowired
    UserCouponRecordMapper userCouponRecordMapper;

    private Map<Long, CouponType> couponMap = new ConcurrentHashMap<>();


    /**
     * 根据商品秒杀的价格筛选可用的优惠券,即商品秒杀价格高于优惠券的使用门槛
     * 先不考虑缓存，后期可以在user中增加优惠券的信息
     *
     * @param userId
     * @param price
     * @return 先按照类别排序(先指定品类，然后全品类)，同类别的按照面值大小排序
     */
    public List<CouponTypeDTO> selectUsableCouponByUserId(Long userId, double price) {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("userId", userId);
        paramMap.put("price", price);
        return couponMapper.selectUsableCouponByUserId(paramMap);
    }

    /**
     * 冻结优惠券，拿到用户锁后进行
     * 缓存中暂不维护用户拥有的优惠券信息
     *
     * @param recordId
     * @param userId
     * @return
     */
    public int frozenCouponById(Long recordId, Long userId, Long activityId) {
        return checkAndUpdateCouponById(recordId, userId, activityId, 1);

    }

    private int checkAndUpdateCouponById(Long recordId, Long userId, Long activityId, int status) {
        //判断是冻结优惠券还是使用优惠券
        if (activityId > 0) {
            //冻结优惠券-先检查当前用户是否有对应优惠券
            HashMap<String, Long> paraMap1 = new HashMap<>();
            paraMap1.put("userId", userId);
            paraMap1.put("recordId", recordId);
            int count = userCouponRecordMapper.checkCouponIsUsable(paraMap1);
            if (count <= 0) {
                return count;
            }
        }
        HashMap<String, Object> paraMap2 = new HashMap<>();
        paraMap2.put("recordId", recordId);
        paraMap2.put("status", status);
        if (activityId > 0) {
            paraMap2.put("activityId", activityId);
        }
        return userCouponRecordMapper.updateStatusById(paraMap2);
    }


    /**
     * 先使用缓存，如果没有再从数据库中查询。
     *
     * @param couponId
     * @return
     */
    public CouponType selectCouponById(Long couponId) {
        CouponType couponType = couponMap.get(couponId);
        if (couponType == null) {
            couponType = couponMapper.selectByPrimaryKey(couponId);
            couponMap.put(couponId, couponType);
        }
        return couponType;
    }


    /**
     * 使用优惠券
     *
     * @param recordId
     * @param userId
     * @return
     */
    public int deductCouponById(Long recordId, Long userId) {
        return checkAndUpdateCouponById(recordId, userId, 0l, 2);
    }
}
