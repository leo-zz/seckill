package com.leozz.controller;

import com.leozz.dto.PreSubmitOrderDTO;
import com.leozz.dto.ResultDTO;
import com.leozz.dto.SubmitDTO;
import com.leozz.entity.SecOrder;
import com.leozz.service.SecOrderService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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
    @ApiOperation(value = "提交订单")
    @RequestMapping("/submit")
    public ResultDTO submitOrder(HttpServletRequest request,SubmitDTO submitDTO) {
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("userId");
        //判断用户是否登录
        if (userId == null) {
            return new ResultDTO<PreSubmitOrderDTO>(false, "请先登录");
        }
        Long userId1 = submitDTO.getUserId();
        if(!userId.equals(userId1)){
            return new ResultDTO<PreSubmitOrderDTO>(false, "账号信息不一致");
        }

        return secOrderService.submitOrder(submitDTO);
    }


    //订单付款，付款成功则跳转付款成功页面
    @ApiOperation(value = "支付订单",notes = "从路径中获取订单号")
    @RequestMapping("/pay/{orderId}")
    public ResultDTO payOrder(HttpServletRequest request,long orderId) {
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("userId");
        //判断用户是否登录
        if (userId == null) {
            return new ResultDTO(false, "请先登录");
        }
        return secOrderService.paytheOrder(orderId,userId);
    }

}
