package com.leozz.controller;

import com.leozz.dto.OrderResultPage;
import com.leozz.entity.SecOrder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: leo-zz
 * @Date: 2019/3/14 8:54
 */
@RestController
@RequestMapping("/secorder")
public class SecOrderController {

    //下单，然后跳转订单结果页面。参数需要等到具体写代码时才能确定
    @RequestMapping("/submit")
    public OrderResultPage submitOrder(SecOrder order, long couponIDs){
        return null;
    }

}
