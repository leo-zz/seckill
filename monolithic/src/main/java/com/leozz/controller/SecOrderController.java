package com.leozz.controller;

import com.leozz.entity.SecOrder;
import com.leozz.service.SecOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: leo-zz
 * @Date: 2019/3/14 8:54
 */
@RestController
@RequestMapping("/secorder")
public class SecOrderController {

    @Autowired
    private SecOrderService secOrderService;

    //提交订单，然后跳转到付款页面。参数需要等到具体写代码时才能确定
    @RequestMapping("/submit")
    public boolean submitOrder(SecOrder order, long couponIDs) {
        boolean result = false;
        int i = secOrderService.submitOrder(order, couponIDs);
        if (i != -1) {
            result = true;
        }
        return result;
    }


    //订单付款，付款成功则跳转付款成功页面
    @RequestMapping("/pay")
    public boolean payOrder(long orderId) {
        return secOrderService.paytheOrder(orderId);
    }

}
