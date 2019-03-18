package com.leozz.util.cache;

import com.leozz.dto.SecOrderDto;
import com.leozz.entity.SecOrder;
import org.springframework.stereotype.Component;

/**
 * @Author: leo-zz
 * @Date: 2019/3/18 13:19
 */
@Component
public class OrderLocalCache {
    public int selectByUserAndActivity(Long secActivityId, Long userId) {
    }

    public int insert(SecOrderDto secOrder) {
    }

    public SecOrderDto getOrderById(long orderId) {
    }

    /**
     *  写入本地缓存并批量写入数据库
     * @param orderId
     * @return
     */
    public int updateOrderById(long orderId) {
    }
}
