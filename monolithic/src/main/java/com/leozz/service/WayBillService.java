package com.leozz.service;

import com.leozz.entity.SecOrder;

/**
 * @Author: leo-zz
 * @Date: 2019/3/13 10:51
 */
public interface WayBillService {

    //创建运单
    void createWayBill(SecOrder order);
}
