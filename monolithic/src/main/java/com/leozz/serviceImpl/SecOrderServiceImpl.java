package com.leozz.serviceImpl;

import com.leozz.dao.SecOrderMapper;
import com.leozz.dto.PreSubmitOrderDTO;
import com.leozz.entity.SecOrder;
import com.leozz.service.SecOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: leo-zz
 * @Date: 2019/3/14 10:11
 */
@Service
public class SecOrderServiceImpl implements SecOrderService {

    @Autowired
    SecOrderMapper secOrderMapper;

    @Override
    public boolean hasUserPlacedOrder(Long secActivityId, Long userId) {
        //查询用户是否存在秒杀活动对应的订单
        //TODO 需要加锁，避免下单后数据库更新订单的时间差内，有用户重复下单；比如给用户添加一个秒杀锁，一个用户只能参与一个秒杀活动。
        int i=secOrderMapper.selectByUserAndActivity(secActivityId, userId);
        return i>0;
    }

    @Override
    public PreSubmitOrderDTO preSubmitOrder(Long secActivityId, Long userId) {
        return null;
    }

    @Override
    public int submitOrder(SecOrder order, long couponIDs) {
        return 0;
    }

    @Override
    public boolean paytheOrder(long orderId) {
        return false;
    }
}
