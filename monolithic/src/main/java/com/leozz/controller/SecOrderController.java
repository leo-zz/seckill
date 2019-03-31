package com.leozz.controller;

import com.leozz.dto.PreSubmitOrderDTO;
import com.leozz.dto.ResultDTO;
import com.leozz.dto.SubmitDTO;
import com.leozz.entity.SecOrder;
import com.leozz.service.SecOrderService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.concurrent.Executors;

/**
 * @Author: leo-zz
 * @Date: 2019/3/14 8:54
 */
@RestController
@RequestMapping("/order")
public class SecOrderController {

    @Autowired
    private SecOrderService secOrderService;

    //提交订单，然后跳转到付款页面。参数需要等到具体写代码时才能确定
    @ApiOperation(value = "提交订单")
    @RequestMapping("/submit")
    public ResultDTO<Long> submitOrder(HttpServletRequest request, @RequestBody SubmitDTO submitDTO) {
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("userId");
        //判断用户是否登录
        if (userId == null) {
            return new ResultDTO<Long>(false, "请先登录");
        }
        Long userId1 = submitDTO.getUserId();
        if (!userId.equals(userId1)) {
            return new ResultDTO<Long>(false, "账号信息不一致");
        }
        try {
            return secOrderService.submitOrder(submitDTO);
        } catch (Exception e) {
            return new ResultDTO<Long>(false, e.getMessage());
        }

    }


    //订单付款，付款成功则跳转付款成功页面
    @ApiOperation(value = "支付订单", notes = "从路径中获取订单号")
    @RequestMapping("/pay")
    public ResultDTO payOrder(HttpServletRequest request, @RequestParam long orderId) {
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("userId");
        //判断用户是否登录
        if (userId == null) {
            return new ResultDTO(false, "请先登录");
        }
        try{
            ResultDTO resultDTO = secOrderService.paytheOrder(orderId, userId);
            return resultDTO;
        }catch (Exception e){
            return  new ResultDTO(false,e.getMessage());
        }

    }

    //取消订单
    @RequestMapping("/cancle")
    public ResultDTO cancleOrder(HttpServletRequest request, @RequestParam long orderId) {
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("userId");
        //判断用户是否登录
        if (userId == null) {
            return new ResultDTO(false, "请先登录");
        }
        return secOrderService.cancletheOrder(orderId, userId);
    }

}
