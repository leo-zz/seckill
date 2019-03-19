package com.leozz.util.cache;

import com.leozz.dao.SecOrderMapper;
import com.leozz.dto.SecOrderDto;
import com.leozz.entity.SecOrder;
import com.leozz.entity.User;
import com.leozz.util.UserDefThreadPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: leo-zz
 * @Date: 2019/3/18 13:19
 */
@Component
public class OrderLocalCache {

    @Autowired
    UserLocalCache userLocalCache;

    @Autowired
    SecOrderMapper orderMapper;

    private ConcurrentHashMap<Long,SecOrderDto> map=new ConcurrentHashMap<>();

    /**
     *  对用户操作进行加锁，避免用户重复下单
     *  先检查缓存，再检查数据库
     * @param secActivityId
     * @param userId
     * @return
     */
    public int selectByUserAndActivity(Long secActivityId, Long userId) {
        User user = userLocalCache.selectUserById(userId);
        Map<String, Long> paramMap = new HashMap<>(2);
        paramMap.put("secActivityId",secActivityId);
        paramMap.put("userId",userId);
        synchronized (user){
            return orderMapper.selectOrderCountByUserAndActivity(paramMap);
        }
    }

    public int insert(SecOrderDto secOrder) {
        map.put(secOrder.getId(),secOrder);//放入缓存，
        // 是否异步存入数据库
//        UserDefThreadPool.jobExecutor.execute(() -> {
        return  orderMapper.insert(secOrder);
//        });
    }

    public SecOrderDto getSecOrderDtoById(long orderId) {
        SecOrderDto secOrderDto=null;
        secOrderDto = map.get(orderId);
        if(secOrderDto==null){
            SecOrder secOrder = orderMapper.selectByPrimaryKey(orderId);
            //查找订单、优惠券、积分的信息
            //secOrder.
        }
        return secOrderDto;

    }

    /**
     *  写入本地缓存并批量写入数据库
     * @param orderId
     * @return
     */
    public int updateOrderById(long orderId) {
    }
}
