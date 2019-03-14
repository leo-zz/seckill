package com.leozz.serviceImpl;

import com.leozz.entity.DeliveryAddr;
import com.leozz.service.DeliveryAddrService;
import org.springframework.stereotype.Service;

/**
 * @Author: leo-zz
 * @Date: 2019/3/14 10:09
 */
@Service
public class DeliveryAddrServiceImpl implements DeliveryAddrService {
    @Override
    public DeliveryAddr getDefaultDeliveryAddr(Long userId) {
        return null;
    }
}
