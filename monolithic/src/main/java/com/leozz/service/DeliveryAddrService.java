package com.leozz.service;

import com.leozz.entity.DeliveryAddr;

/**
 * @Author: leo-zz
 * @Date: 2019/3/13 10:44
 */
public interface DeliveryAddrService {

    /**
     *  查询用户的默认收货信息。
     * @param userId 用户Id
     * @return  用户的默认收货信息
     */
    DeliveryAddr getDefaultDeliveryAddr(Long userId);

    //新增收货人信息

    //更新收货人信息
}
