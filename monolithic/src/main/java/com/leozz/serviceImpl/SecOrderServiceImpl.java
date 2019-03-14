package com.leozz.serviceImpl;

import com.leozz.entity.SecOrder;
import com.leozz.service.SecOrderService;

/**
 * @Author: leo-zz
 * @Date: 2019/3/14 10:11
 */
public class SecOrderServiceImpl implements SecOrderService {
    @Override
    public boolean hasUserPlacedtheOrder(Long secActivityId, Long userId) {
        return false;
    }

    @Override
    public int submittheOrder(SecOrder order, long couponIDs) {
        return 0;
    }

    @Override
    public boolean paytheOrder(long orderId) {
        return false;
    }
}
