package com.leozz.controller;

import com.leozz.dto.ResultDTO;
import com.leozz.dto.SubmitDTO;
import com.leozz.entity.SecOrder;
import com.leozz.service.SecOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

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
    public ResultDTO submitOrder(SubmitDTO submitDTO) {

        return secOrderService.submitOrder(submitDTO);
    }


    //订单付款，付款成功则跳转付款成功页面
    @RequestMapping("/pay")
    public ResultDTO payOrder(long orderId) {
        return secOrderService.paytheOrder(orderId);
    }

}
