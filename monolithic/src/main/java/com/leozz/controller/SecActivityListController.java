package com.leozz.controller;

import com.leozz.dto.PreSubmitOrderDTO;
import com.leozz.dto.ResultDTO;
import com.leozz.dto.SecActivityDTO;
import com.leozz.service.SecActivityService;
import com.leozz.service.SecOrderService;
import com.leozz.util.TimeRecorder;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Time;
import java.util.List;

/**
 * @Author: leo-zz
 * @Date: 2019/3/14 8:33
 */
@RestController
@RequestMapping("/activity")
public class SecActivityListController {

    @Autowired
    private SecActivityService secActivityService;

    @Autowired
    private SecOrderService secOrderService;

    /**
     * 获取当前秒杀活动列表
     *
     * @return 秒杀活动列表页需要的数据
     */
    @ApiOperation(value = "获取秒杀活动列表", notes = "需要从session中拿取userId")
    @RequestMapping("/list")
    public ResultDTO<List<SecActivityDTO>> getSecActivityList(HttpServletRequest request) {
        TimeRecorder.accessTime.set(System.currentTimeMillis());
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("userId");
        //判断用户是否登录
        if (userId == null) {
            return new ResultDTO<List<SecActivityDTO>>(false, "请先登录");
        }
        try {
            //根据userId获取秒杀活动列表
            ResultDTO<List<SecActivityDTO>> secActivityList = secActivityService.getSecActivityList(userId);
            return secActivityList;
        } catch (Exception e) {
            return new ResultDTO<List<SecActivityDTO>>(false, e.getMessage());
        }
    }

    /**
     * 点击按钮，参与秒杀活动，参与成功则跳转到预下单页面。
     *
     * @param secActivityId 活动id
     * @return 下单页需要的数据
     */
    @ApiOperation(value = "参与秒杀活动", notes = "从访问路径中拿取活动id，从session中拿取userId")
    @RequestMapping("/partake")
    public PreSubmitOrderDTO partakeSecActivity(HttpServletRequest request, @RequestParam Long secActivityId) {
        //将访问的时间戳保存到threadLocal中，以便于跨方法访问同一个变量
        TimeRecorder.accessTime.set(System.currentTimeMillis());
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("userId");
        //判断用户是否登录
        if (userId == null) {
            return new PreSubmitOrderDTO(false, "请先登录");
        }
        PreSubmitOrderDTO preSubmitOrderDTO = null;
        //尝试参与秒杀活动
        ResultDTO resultDTO = secActivityService.partakeSecActivity(secActivityId, userId);
        if (resultDTO.isResult()) {
            //将用户当前参与的秒杀活动放入session
            session.setAttribute("secActivityId", secActivityId);
            //获取预下单页面的信息
            preSubmitOrderDTO = secOrderService.preSubmitOrder(secActivityId, userId);
            return preSubmitOrderDTO;
        } else {
            return new PreSubmitOrderDTO(false, resultDTO.getMsg());
        }
    }
}
