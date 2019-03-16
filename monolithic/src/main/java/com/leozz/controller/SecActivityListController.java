package com.leozz.controller;

import com.leozz.dto.PreSubmitOrderDTO;
import com.leozz.dto.ResultDTO;
import com.leozz.dto.SecActivityDTO;
import com.leozz.service.SecActivityService;
import com.leozz.service.SecOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @Author: leo-zz
 * @Date: 2019/3/14 8:33
 */
@RestController
@RequestMapping("/secactivity")
public class SecActivityListController {

    @Autowired
    private SecActivityService secActivityService;

    @Autowired
    private SecOrderService secOrderService;
    /**
     * 获取当前秒杀活动列表
     * @return 秒杀活动列表页需要的数据
     */
    @RequestMapping("/list")
    public List<SecActivityDTO> getSecActivityList(HttpServletRequest request){
        HttpSession session = request.getSession();
        Long userId = (Long)session.getAttribute("userId");
        //根据userId获取秒杀活动列表，是否要通过mybatis关联查询？
        return secActivityService.getSecActivityList(userId);
    }

    /**
     * 点击按钮，参与秒杀活动，参与成功则跳转到预下单页面。
     * @param secActivityId
     * @return 下单页需要的数据
     */
    @RequestMapping("/partake")
    public PreSubmitOrderDTO partakeSecActivity(HttpServletRequest request,Long secActivityId){
        HttpSession session = request.getSession();
        Long userId = (Long)session.getAttribute("userId");

        PreSubmitOrderDTO preSubmitOrderDTO =null;
        //尝试参与秒杀活动
        ResultDTO resultDTO = secActivityService.partakeSecActivity(secActivityId, userId);
        if(resultDTO.isResult()){
            //将用户当前参与的秒杀活动放入session
            session.setAttribute("secActivityId",secActivityId);
            //获取预下单页面的信息
            preSubmitOrderDTO = secOrderService.preSubmitOrder(secActivityId, userId);
        }else{
            preSubmitOrderDTO = new PreSubmitOrderDTO(false,resultDTO.getMsg());
        }
        return preSubmitOrderDTO;
    }
}
